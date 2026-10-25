package sonora.api.library;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TrackNameParserTest {

    @Test
    void separeArtisteEtTitreEtNettoieLeBruit() {
        var parsed = TrackNameParser.parse("JUL - WESH ALORS CLIP OFFICIEL 2015.mp3");
        assertThat(parsed.artist()).isEqualTo("JUL");
        assertThat(parsed.title()).isEqualTo("Wesh Alors");
        assertThat(parsed.artistFound()).isTrue();
    }

    @Test
    void retireLesParenthesesEtGardeLesAccents() {
        var parsed = TrackNameParser.parse("RnBoi - MON BÉBÉ (Visualizer).mp3");
        assertThat(parsed.artist()).isEqualTo("RnBoi");
        assertThat(parsed.title()).isEqualTo("Mon Bébé");
    }

    @Test
    void conserveLaCasseDUnTitreDejaMixte() {
        assertThat(TrackNameParser.parse("La Rvfleuze - Argent Sale A COLORS SHOW.mp3").title())
                .isEqualTo("Argent Sale");
    }

    @Test
    void sansSeparateurLArtisteEstInconnu() {
        var parsed = TrackNameParser.parse("ma chanson.wav");
        assertThat(parsed.artist()).isEqualTo(TrackNameParser.UNKNOWN_ARTIST);
        assertThat(parsed.title()).isEqualTo("ma chanson");
        assertThat(parsed.artistFound()).isFalse();
    }

    @Test
    void neVidePasUnTitreQuiNeContientQueDuBruit() {
        assertThat(TrackNameParser.cleanTitle("(Visualizer)")).isEqualTo("(Visualizer)");
    }
}
