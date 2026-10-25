package sonora.api.config;

import java.nio.file.Path;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Réglages "sonora.*" de application.properties.
 * Les dossiers sont lus en texte : la conversion String→Path de Spring les traiterait
 * comme des ressources du classpath et refuserait un chemin relatif comme "../music".
 */
@ConfigurationProperties(prefix = "sonora")
public record SonoraProperties(String musicDir, String dataDir, List<String> corsOrigins) {

    public SonoraProperties {
        corsOrigins = corsOrigins == null ? List.of() : List.copyOf(corsOrigins);
    }

    public Path musicPath() {
        return Path.of(musicDir).toAbsolutePath().normalize();
    }

    public Path dataPath() {
        return Path.of(dataDir).toAbsolutePath().normalize();
    }
}
