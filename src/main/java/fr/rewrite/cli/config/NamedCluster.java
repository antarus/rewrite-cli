package fr.rewrite.cli.config;

import lombok.Data;

@Data
public class NamedCluster {

  private String name;
  private Cluster cluster;

  @Data
  public static class Cluster {

    private String server; // URL du serveur (ex: "https://localhost:8080/api/rewrite")
    private String certificateAuthorityData; // Contenu encodé en base64 du CA cert (ca.crt)
    private String certificateAuthorityFile; // Chemin vers le fichier ca.crt
    private boolean insecureSkipTlsVerify = false; // Pour désactiver la vérification du certificat (à éviter en prod)
  }
}
