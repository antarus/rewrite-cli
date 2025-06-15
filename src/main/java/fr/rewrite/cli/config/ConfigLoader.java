package fr.rewrite.cli.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigLoader {

  private static final String CONFIG_FILE_NAME = "config.yaml";
  private static final String CONFIG_DIR_NAME = ".rewrite";

  /**
   * Loads the configuration from the default path (~/.rewrite/config.yaml).
   * If the file does not exist, a default one is created.
   * @return The loaded Config object.
   * @throws IOException If there's an error reading or creating the file.
   */
  public static Config loadConfig() throws IOException {
    Path configFilePath = Paths.get(System.getProperty("user.home"), CONFIG_DIR_NAME, CONFIG_FILE_NAME);
    return loadConfig(configFilePath); // Appelle la nouvelle méthode surchargée
  }

  /**
   * Loads the configuration from a specific given path.
   * If the file does not exist, a warning is printed and an empty Config is returned.
   * (We don't create a default file here, as this path is explicitly specified by the user).
   * @param specificConfigPath The explicit Path to the configuration file.
   * @return The loaded Config object.
   * @throws IOException If there's an error reading the file.
   */
  public static Config loadConfig(Path specificConfigPath) throws IOException {
    File configFile = specificConfigPath.toFile();

    if (!configFile.exists()) {
      System.err.println("Warning: Configuration file not found at " + specificConfigPath);
      // Créez un fichier de configuration d'exemple si inexistant LORS DE L'APPEL PAR DÉFAUT
      // Dans ce cas précis, si l'utilisateur spécifie un chemin qui n'existe pas,
      // on ne le crée pas automatiquement. On retourne une config vide ou on lève une exception.
      // Pour l'instant, nous retournerons une Config vide pour éviter de bloquer l'application.
      return new Config();
    }

    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    Config config = objectMapper.readValue(configFile, Config.class);

    config
      .getUsers()
      .forEach(u -> {
        if (u.getUser().getClientTruststorePath() != null && u.getUser().getClientTruststorePath().contains("~/" + CONFIG_DIR_NAME)) {
          u.getUser().setClientTruststorePath(u.getUser().getClientTruststorePath().replace("~", System.getProperty("user.home")));
        }
        if (u.getUser().getClientCertificateFile() != null && u.getUser().getClientCertificateFile().contains("~/" + CONFIG_DIR_NAME)) {
          u.getUser().setClientCertificateFile(u.getUser().getClientCertificateFile().replace("~", System.getProperty("user.home")));
        }
        if (u.getUser().getClientKeystorePath() != null && u.getUser().getClientKeystorePath().contains("~/" + CONFIG_DIR_NAME)) {
          u.getUser().setClientKeystorePath(u.getUser().getClientKeystorePath().replace("~", System.getProperty("user.home")));
        }
      });

    return config;
  }

  // Méthode pour créer le fichier de configuration par défaut, appelée uniquement par loadConfig() sans argument.
  private static void createDefaultConfigFile(Path configFilePath) throws IOException {
    Files.createDirectories(configFilePath.getParent());
    String defaultContent =
      "apiVersion: v1\n" +
      "currentContext: default-context\n" +
      "clusters:\n" +
      "  - name: default-cluster\n" +
      "    cluster:\n" +
      "      server: https://localhost:8443/api/rewrite\n" +
      "      insecureSkipTlsVerify: true # A NE PAS UTILISER EN PRODUCTION\n" +
      "      # certificateAuthorityFile: path/to/ca.crt\n" +
      "      # certificateAuthorityData: <base64 encoded ca.crt>\n" +
      "users:\n" +
      "  - name: default-user\n" +
      "    user:\n" +
      "      username: your-git-username\n" +
      "      password: your-git-pat\n" +
      "      # clientKeystorePath: path/to/client.p12\n" +
      "      # clientKeystorePassword: changeit\n" +
      "      # clientTruststorePath: path/to/client_truststore.jks\n" +
      "      # clientTruststorePassword: changeit\n" +
      "contexts:\n" +
      "  - name: default-context\n" +
      "    context:\n" +
      "      cluster: default-cluster\n" +
      "      user: default-user\n";
    Files.writeString(configFilePath, defaultContent);
    System.out.println("Created default configuration file at: " + configFilePath);
    System.out.println("Please edit this file with your actual server and user details, and certificate paths.");
  }
}
