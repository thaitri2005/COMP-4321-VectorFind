package hk.ust.comp4321.util;

public final class PorterStemmer {
    private PorterStemmer() {
    }

    public static String stem(String word) {
        if (word == null || word.length() < 3) {
            return word;
        }

        String w = word.toLowerCase();
        w = step1a(w);
        w = step1b(w);
        w = step1c(w);
        w = step2(w);
        w = step3(w);
        w = step4(w);
        w = step5a(w);
        w = step5b(w);
        return w;
    }

    private static boolean isConsonant(String word, int i) {
        char ch = word.charAt(i);
        if (ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u') {
            return false;
        }
        if (ch == 'y') {
            return i == 0 || !isConsonant(word, i - 1);
        }
        return true;
    }

    private static int measure(String word) {
        int m = 0;
        boolean inVowel = false;
        for (int i = 0; i < word.length(); i++) {
            if (!isConsonant(word, i)) {
                inVowel = true;
            } else if (inVowel) {
                m++;
                inVowel = false;
            }
        }
        return m;
    }

    private static boolean containsVowel(String word) {
        for (int i = 0; i < word.length(); i++) {
            if (!isConsonant(word, i)) {
                return true;
            }
        }
        return false;
    }

    private static boolean endsWithDoubleConsonant(String word) {
        int len = word.length();
        if (len < 2) {
            return false;
        }
        return word.charAt(len - 1) == word.charAt(len - 2) && isConsonant(word, len - 1);
    }

    private static boolean cvc(String word) {
        int len = word.length();
        if (len < 3) {
            return false;
        }
        if (!isConsonant(word, len - 1) || isConsonant(word, len - 2) || !isConsonant(word, len - 3)) {
            return false;
        }
        char ch = word.charAt(len - 1);
        return ch != 'w' && ch != 'x' && ch != 'y';
    }

    private static String replaceSuffix(String word, String suffix, String replacement, int minMeasure) {
        if (word.endsWith(suffix)) {
            String base = word.substring(0, word.length() - suffix.length());
            if (measure(base) > minMeasure) {
                return base + replacement;
            }
        }
        return word;
    }

    private static String step1a(String word) {
        if (word.endsWith("sses")) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("ies")) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("ss")) {
            return word;
        }
        if (word.endsWith("s")) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }

    private static String step1b(String word) {
        if (word.endsWith("eed")) {
            String base = word.substring(0, word.length() - 3);
            if (measure(base) > 0) {
                return base + "ee";
            }
            return word;
        }
        if (word.endsWith("ed")) {
            String base = word.substring(0, word.length() - 2);
            if (containsVowel(base)) {
                return step1bPost(base);
            }
            return word;
        }
        if (word.endsWith("ing")) {
            String base = word.substring(0, word.length() - 3);
            if (containsVowel(base)) {
                return step1bPost(base);
            }
        }
        return word;
    }

    private static String step1bPost(String word) {
        if (word.endsWith("at") || word.endsWith("bl") || word.endsWith("iz")) {
            return word + "e";
        }
        if (endsWithDoubleConsonant(word)) {
            char ch = word.charAt(word.length() - 1);
            if (ch != 'l' && ch != 's' && ch != 'z') {
                return word.substring(0, word.length() - 1);
            }
        }
        if (measure(word) == 1 && cvc(word)) {
            return word + "e";
        }
        return word;
    }

    private static String step1c(String word) {
        if (word.endsWith("y")) {
            String base = word.substring(0, word.length() - 1);
            if (containsVowel(base)) {
                return base + "i";
            }
        }
        return word;
    }

    private static String step2(String word) {
        String[][] rules = {
            {"ational", "ate"}, {"tional", "tion"}, {"enci", "ence"}, {"anci", "ance"},
            {"izer", "ize"}, {"abli", "able"}, {"alli", "al"}, {"entli", "ent"},
            {"eli", "e"}, {"ousli", "ous"}, {"ization", "ize"}, {"ation", "ate"},
            {"ator", "ate"}, {"alism", "al"}, {"iveness", "ive"}, {"fulness", "ful"},
            {"ousness", "ous"}, {"aliti", "al"}, {"iviti", "ive"}, {"biliti", "ble"}
        };
        for (String[] rule : rules) {
            if (word.endsWith(rule[0])) {
                String base = word.substring(0, word.length() - rule[0].length());
                if (measure(base) > 0) {
                    return base + rule[1];
                }
                return word;
            }
        }
        return word;
    }

    private static String step3(String word) {
        String[][] rules = {
            {"icate", "ic"}, {"ative", ""}, {"alize", "al"},
            {"iciti", "ic"}, {"ical", "ic"}, {"ful", ""}, {"ness", ""}
        };
        for (String[] rule : rules) {
            if (word.endsWith(rule[0])) {
                String base = word.substring(0, word.length() - rule[0].length());
                if (measure(base) > 0) {
                    return base + rule[1];
                }
                return word;
            }
        }
        return word;
    }

    private static String step4(String word) {
        String[] suffixes = {
            "al", "ance", "ence", "er", "ic", "able", "ible", "ant", "ement", "ment",
            "ent", "sion", "tion", "ou", "ism", "ate", "iti", "ous", "ive", "ize"
        };
        for (String suffix : suffixes) {
            if (word.endsWith(suffix)) {
                String base = word.substring(0, word.length() - suffix.length());
                if (("sion".equals(suffix) || "tion".equals(suffix)) && base.isEmpty()) {
                    continue;
                }
                if (measure(base) > 1) {
                    return base;
                }
                return word;
            }
        }
        return word;
    }

    private static String step5a(String word) {
        if (word.endsWith("e")) {
            String base = word.substring(0, word.length() - 1);
            int m = measure(base);
            if (m > 1 || (m == 1 && !cvc(base))) {
                return base;
            }
        }
        return word;
    }

    private static String step5b(String word) {
        if (measure(word) > 1 && endsWithDoubleConsonant(word) && word.endsWith("l")) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }
}
