package sonora.api.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import sonora.api.history.PlayHistoryService;

/**
 * Teste l'API sur un faux dossier musique. Les fichiers ne sont pas de vrais mp3 :
 * on vérifie donc aussi que des métadonnées illisibles ne cassent rien.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    private static final Path MUSIC;
    private static final Path DATA;

    static {
        try {
            MUSIC = Files.createTempDirectory("sonora-music");
            DATA = Files.createTempDirectory("sonora-data");
            Files.write(MUSIC.resolve("JUL - WESH ALORS CLIP OFFICIEL 2015.mp3"), new byte[2048]);
            Files.write(MUSIC.resolve("RnBoi - MON BÉBÉ (Visualizer).mp3"), new byte[1024]);
            Files.write(MUSIC.resolve("notes.txt"), new byte[10]);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("sonora.music-dir", MUSIC::toString);
        registry.add("sonora.data-dir", DATA::toString);
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private PlayHistoryService history;

    @BeforeEach
    void resetHistory() {
        history.clear();
    }

    @Test
    void listeLesMorceauxAudioUniquement() throws Exception {
        mvc.perform(get("/api/tracks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].artist", is("JUL")))
                .andExpect(jsonPath("$[0].title", is("Wesh Alors")))
                .andExpect(jsonPath("$[0].coverUrl").doesNotExist())
                .andExpect(jsonPath("$[1].title", is("Mon Bébé")));
    }

    @Test
    void rechercheSansAccents() throws Exception {
        mvc.perform(get("/api/search").param("q", "bebe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tracks", hasSize(1)))
                .andExpect(jsonPath("$.tracks[0].artist", is("RnBoi")))
                .andExpect(jsonPath("$.artists", hasSize(0)));

        mvc.perform(get("/api/search").param("q", "jul"))
                .andExpect(jsonPath("$.artists[0].name", is("JUL")))
                .andExpect(jsonPath("$.artists[0].trackCount", is(1)));
    }

    @Test
    void historiqueDuPlusRecentAuPlusAncien() throws Exception {
        String jul = firstTrackId("JUL");
        String rnboi = firstTrackId("RnBoi");

        mvc.perform(post("/api/plays/" + jul)).andExpect(status().isCreated());
        mvc.perform(post("/api/plays/" + rnboi)).andExpect(status().isCreated());
        mvc.perform(post("/api/plays/" + jul)).andExpect(jsonPath("$.count", is(2)));

        mvc.perform(get("/api/plays"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].trackId", is(jul)))
                .andExpect(jsonPath("$[0].count", is(2)))
                .andExpect(jsonPath("$[1].trackId", is(rnboi)));
    }

    @Test
    void streamingAvecRange() throws Exception {
        String id = firstTrackId("JUL");
        mvc.perform(get("/api/tracks/" + id + "/audio").header("Range", "bytes=0-99"))
                .andExpect(status().isPartialContent())
                .andExpect(header().string("Content-Range", "bytes 0-99/2048"));
    }

    @Test
    void erreurs404() throws Exception {
        mvc.perform(get("/api/tracks/inconnu")).andExpect(status().isNotFound());
        mvc.perform(post("/api/plays/inconnu")).andExpect(status().isNotFound());
        mvc.perform(get("/api/tracks/" + firstTrackId("JUL") + "/cover")).andExpect(status().isNotFound());
    }

    @Test
    void artistesTriesAvecLeursMorceaux() throws Exception {
        mvc.perform(get("/api/artists"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("JUL")))
                .andExpect(jsonPath("$[0].trackIds", hasSize(1)))
                .andExpect(jsonPath("$[0].coverUrl").doesNotExist());
    }

    private String firstTrackId(String artist) throws Exception {
        String json = mvc.perform(get("/api/tracks")).andReturn().getResponse().getContentAsString();
        for (JsonNode track : mapper.readTree(json)) {
            if (track.get("artist").asText().equals(artist)) {
                return track.get("id").asText();
            }
        }
        throw new AssertionError("Aucun morceau de " + artist);
    }
}
