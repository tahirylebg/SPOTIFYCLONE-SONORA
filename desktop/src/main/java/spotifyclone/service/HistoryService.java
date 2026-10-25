package spotifyclone.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sauvegarde le nombre d'écoutes de chaque morceau dans ~/.sonora/ecoutes.tsv
 * (une ligne "nombre<TAB>chemin", du plus récent au plus ancien).
 */
public class HistoryService {

    private final Path fichier = Path.of(System.getProperty("user.home"), ".sonora", "ecoutes.tsv");

    public Map<String, Integer> charger() {
        Map<String, Integer> ecoutes = new LinkedHashMap<>();
        if (!Files.isRegularFile(fichier)) {
            return ecoutes;
        }
        try {
            for (String ligne : Files.readAllLines(fichier, StandardCharsets.UTF_8)) {
                int tab = ligne.indexOf('\t');
                if (tab <= 0) {
                    continue;
                }
                try {
                    ecoutes.put(ligne.substring(tab + 1), Integer.parseInt(ligne.substring(0, tab)));
                } catch (NumberFormatException ignore) {
                    // ligne corrompue : on l'ignore
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ecoutes;
    }

    public void sauvegarder(Map<String, Integer> ecoutes) {
        List<String> lignes = ecoutes.entrySet().stream()
                .map(e -> e.getValue() + "\t" + e.getKey())
                .collect(Collectors.toList());
        try {
            Files.createDirectories(fichier.getParent());
            Files.write(fichier, lignes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
