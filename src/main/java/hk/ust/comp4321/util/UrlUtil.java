package hk.ust.comp4321.util;

import java.net.URI;
import java.net.URISyntaxException;

public final class UrlUtil {
    private UrlUtil() {
    }

    public static String normalize(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        try {
            URI raw = new URI(url.trim());
            String scheme = raw.getScheme();
            if (scheme == null) {
                return null;
            }
            String lowerScheme = scheme.toLowerCase();
            if (!"http".equals(lowerScheme) && !"https".equals(lowerScheme)) {
                return null;
            }
            URI clean = new URI(
                lowerScheme,
                raw.getUserInfo(),
                raw.getHost() == null ? null : raw.getHost().toLowerCase(),
                raw.getPort(),
                raw.getPath(),
                raw.getQuery(),
                null
            ).normalize();
            return clean.toString();
        } catch (URISyntaxException ex) {
            return null;
        }
    }
}
