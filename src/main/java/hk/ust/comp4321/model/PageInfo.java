package hk.ust.comp4321.model;

import java.io.Serializable;

public class PageInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int pageId;
    private final String url;
    private String title;
    private String lastModified;
    private long size;
    private boolean fetched;

    public PageInfo(int pageId, String url) {
        this.pageId = pageId;
        this.url = url;
        this.title = url;
        this.lastModified = "Unknown";
        this.size = 0L;
        this.fetched = false;
    }

    public int getPageId() {
        return pageId;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        if (lastModified != null && !lastModified.isBlank()) {
            this.lastModified = lastModified;
        }
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        if (size >= 0) {
            this.size = size;
        }
    }

    public boolean isFetched() {
        return fetched;
    }

    public void setFetched(boolean fetched) {
        this.fetched = fetched;
    }
}
