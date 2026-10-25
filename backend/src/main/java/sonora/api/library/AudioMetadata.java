package sonora.api.library;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

/** Ce qu'on sait lire dans un fichier audio : durée, tags ID3 et pochette. */
public record AudioMetadata(int durationSeconds, String title, String artist, byte[] artwork) {

    static {
        // jaudiotagger est très bavard sur java.util.logging.
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
    }

    private static final AudioMetadata EMPTY = new AudioMetadata(0, null, null, null);

    /** Lit le fichier ; renvoie des métadonnées vides si le format n'est pas lisible. */
    public static AudioMetadata read(Path file) {
        try {
            AudioFile audio = AudioFileIO.read(file.toFile());
            int duration = audio.getAudioHeader() == null ? 0 : audio.getAudioHeader().getTrackLength();
            Tag tag = audio.getTag();
            if (tag == null) {
                return new AudioMetadata(duration, null, null, null);
            }
            Artwork art = tag.getFirstArtwork();
            return new AudioMetadata(duration,
                    blankToNull(tag.getFirst(FieldKey.TITLE)),
                    blankToNull(tag.getFirst(FieldKey.ARTIST)),
                    art == null ? null : art.getBinaryData());
        } catch (Exception | LinkageError e) {
            return EMPTY;
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
