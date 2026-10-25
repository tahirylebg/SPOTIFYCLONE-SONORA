package sonora.api.web;

import java.text.Normalizer;
import java.util.Locale;

final class Text {

    private Text() {
    }

    /** Minuscules sans accents, pour que "bebe" trouve "Bébé". */
    static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
