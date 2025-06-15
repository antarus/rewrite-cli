package fr.rewrite.cli.config;

import lombok.Data;

@Data
public class NamedUser {

  private String name;
  private User user;

  @Data
  public static class User {

    private String username;
    private String gitPatForGit;
    private String gitPatForApi;
    private String clientCertificateData; // Contenu encodé en base64 du cert client (client.crt)
    private String clientCertificateFile; // Chemin vers le fichier client.crt
    private String clientKeyData; // Contenu encodé en base64 de la clé privée client (client.key)
    private String clientKeyFile; // Chemin vers le fichier client.key
    private String clientKeystorePath; // Chemin vers le keystore client (client.p12)
    private String clientKeystorePassword; // Mot de passe du keystore client
    private String clientTruststorePath; // Chemin vers le truststore client (client_truststore.jks)
    private String clientTruststorePassword; // Mot de passe du truststore client
  }
}
