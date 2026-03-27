package hk.ust.comp4321.storage;

import hk.ust.comp4321.model.PageInfo;
import hk.ust.comp4321.model.Posting;
import hk.ust.comp4321.model.TermVectorEntry;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SearchDb implements Closeable {
    private final RecordManager recman;

    private final HTree urlToPageId;
    private final HTree pageIdToUrl;
    private final HTree pageTable;

    private final HTree wordToWordId;
    private final HTree wordIdToWord;

    private final HTree forwardIndex;
    private final HTree bodyInverted;
    private final HTree titleInverted;

    private final HTree parentToChildren;
    private final HTree childToParents;

    private int nextPageId;
    private int nextWordId;

    public SearchDb(Path filePath) throws IOException {
        this.recman = RecordManagerFactory.createRecordManager(filePath.toString());

        this.urlToPageId = openOrCreateTree("urlToPageId");
        this.pageIdToUrl = openOrCreateTree("pageIdToUrl");
        this.pageTable = openOrCreateTree("pageTable");

        this.wordToWordId = openOrCreateTree("wordToWordId");
        this.wordIdToWord = openOrCreateTree("wordIdToWord");

        this.forwardIndex = openOrCreateTree("forwardIndex");
        this.bodyInverted = openOrCreateTree("bodyInverted");
        this.titleInverted = openOrCreateTree("titleInverted");

        this.parentToChildren = openOrCreateTree("parentToChildren");
        this.childToParents = openOrCreateTree("childToParents");

        this.nextPageId = maxIntegerKey(this.pageIdToUrl) + 1;
        this.nextWordId = maxIntegerKey(this.wordIdToWord) + 1;
    }

    private HTree openOrCreateTree(String name) throws IOException {
        long recid = recman.getNamedObject(name);
        if (recid != 0) {
            return HTree.load(recman, recid);
        }
        HTree tree = HTree.createInstance(recman);
        recman.setNamedObject(name, tree.getRecid());
        recman.commit();
        return tree;
    }

    private int maxIntegerKey(HTree tree) throws IOException {
        FastIterator iterator = tree.keys();
        int max = 0;
        Object key;
        while ((key = iterator.next()) != null) {
            if (key instanceof Integer value && value > max) {
                max = value;
            }
        }
        return max;
    }

    public synchronized int ensurePageId(String url) {
        Integer existing = getInteger(urlToPageId, url);
        if (existing != null) {
            return existing;
        }
        int pageId = nextPageId++;
        put(urlToPageId, url, pageId);
        put(pageIdToUrl, pageId, url);
        put(pageTable, pageId, new PageInfo(pageId, url));
        return pageId;
    }

    public synchronized int ensureWordId(String word) {
        Integer existing = getInteger(wordToWordId, word);
        if (existing != null) {
            return existing;
        }
        int wordId = nextWordId++;
        put(wordToWordId, word, wordId);
        put(wordIdToWord, wordId, word);
        return wordId;
    }

    public Integer getPageId(String url) {
        return getInteger(urlToPageId, url);
    }

    public PageInfo getPageInfo(int pageId) {
        return get(pageTable, pageId, PageInfo.class);
    }

    public void savePageInfo(PageInfo pageInfo) {
        put(pageTable, pageInfo.getPageId(), pageInfo);
    }

    public void addLink(int parentPageId, int childPageId) {
        Set<Integer> children = new HashSet<>(getSet(parentToChildren, parentPageId));
        children.add(childPageId);
        put(parentToChildren, parentPageId, children);

        Set<Integer> parents = new HashSet<>(getSet(childToParents, childPageId));
        parents.add(parentPageId);
        put(childToParents, childPageId, parents);
    }

    public void addBodyTerm(int pageId, String term, int position) {
        ensureWordId(term);
        addForwardTerm(pageId, term, position, false);
        addInvertedTerm(bodyInverted, term, pageId, position);
    }

    public void addTitleTerm(int pageId, String term, int position) {
        ensureWordId(term);
        addForwardTerm(pageId, term, position, true);
        addInvertedTerm(titleInverted, term, pageId, position);
    }

    public void clearPageIndex(int pageId) {
        Map<String, TermVectorEntry> existing = getTermVectorMap(forwardIndex, pageId);
        if (existing.isEmpty()) {
            return;
        }

        for (String term : existing.keySet()) {
            removePagePosting(bodyInverted, term, pageId);
            removePagePosting(titleInverted, term, pageId);
        }

        put(forwardIndex, pageId, new HashMap<String, TermVectorEntry>());
    }

    public void clearOutgoingLinks(int parentPageId) {
        Set<Integer> oldChildren = getSet(parentToChildren, parentPageId);
        if (oldChildren.isEmpty()) {
            return;
        }

        for (Integer childId : oldChildren) {
            Set<Integer> parents = new HashSet<>(getSet(childToParents, childId));
            if (parents.remove(parentPageId)) {
                put(childToParents, childId, parents);
            }
        }

        put(parentToChildren, parentPageId, new HashSet<Integer>());
    }

    private void addForwardTerm(int pageId, String term, int position, boolean title) {
        Map<String, TermVectorEntry> entries = new HashMap<>(getTermVectorMap(forwardIndex, pageId));
        TermVectorEntry entry = entries.computeIfAbsent(term, ignored -> new TermVectorEntry());
        if (title) {
            entry.addTitlePosition(position);
        } else {
            entry.addBodyPosition(position);
        }
        entries.put(term, entry);
        put(forwardIndex, pageId, entries);
    }

    private void addInvertedTerm(HTree target, String term, int pageId, int position) {
        Map<Integer, Posting> postings = new HashMap<>(getPostingMap(target, term));
        Posting posting = postings.computeIfAbsent(pageId, ignored -> new Posting());
        posting.addPosition(position);
        postings.put(pageId, posting);
        put(target, term, postings);
    }

    private void removePagePosting(HTree target, String term, int pageId) {
        Map<Integer, Posting> postings = new HashMap<>(getPostingMap(target, term));
        if (postings.remove(pageId) == null) {
            return;
        }
        put(target, term, postings);
    }

    public Map<Integer, PageInfo> allPages() {
        Map<Integer, PageInfo> result = new HashMap<>();
        try {
            FastIterator iterator = pageTable.keys();
            Object key;
            while ((key = iterator.next()) != null) {
                if (key instanceof Integer pageId) {
                    PageInfo info = getPageInfo(pageId);
                    if (info != null) {
                        result.put(pageId, info);
                    }
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read page table", ex);
        }
        return result;
    }

    public Map<String, TermVectorEntry> forwardTermsForPage(int pageId) {
        return new HashMap<>(getTermVectorMap(forwardIndex, pageId));
    }

    public Set<Integer> childrenOf(int pageId) {
        return new HashSet<>(getSet(parentToChildren, pageId));
    }

    public Set<Integer> parentsOf(int pageId) {
        return new HashSet<>(getSet(childToParents, pageId));
    }

    public String urlForPageId(int pageId) {
        return get(pageIdToUrl, pageId, String.class);
    }

    public void commit() {
        try {
            recman.commit();
        } catch (IOException ex) {
            throw new RuntimeException("Commit failed", ex);
        }
    }

    @Override
    public void close() throws IOException {
        recman.commit();
        recman.close();
    }

    @SuppressWarnings("unchecked")
    private Map<String, TermVectorEntry> getTermVectorMap(HTree tree, Object key) {
        Object value = getRaw(tree, key);
        if (value instanceof Map<?, ?> map) {
            return (Map<String, TermVectorEntry>) map;
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, Posting> getPostingMap(HTree tree, Object key) {
        Object value = getRaw(tree, key);
        if (value instanceof Map<?, ?> map) {
            return (Map<Integer, Posting>) map;
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private Set<Integer> getSet(HTree tree, Object key) {
        Object value = getRaw(tree, key);
        if (value instanceof Set<?> set) {
            return (Set<Integer>) set;
        }
        return Collections.emptySet();
    }

    private Integer getInteger(HTree tree, Object key) {
        Object value = getRaw(tree, key);
        if (value instanceof Integer i) {
            return i;
        }
        return null;
    }

    private <T> T get(HTree tree, Object key, Class<T> clazz) {
        Object value = getRaw(tree, key);
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        return null;
    }

    private Object getRaw(HTree tree, Object key) {
        try {
            return tree.get(key);
        } catch (IOException ex) {
            throw new RuntimeException("Read failed", ex);
        }
    }

    private void put(HTree tree, Object key, Object value) {
        try {
            tree.put(key, value);
        } catch (IOException ex) {
            throw new RuntimeException("Write failed", ex);
        }
    }
}
