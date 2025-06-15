package fr.rewrite.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.rewrite.cli.application.dto.RewriteConfig;
import fr.rewrite.cli.config.*;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import lombok.Getter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
  name = "rewrite",
  mixinStandardHelpOptions = true,
  version = "Rewrite CLI 1.0",
  description = "Client CLI for OpenRewrite orchestrator."
)
public class RewriteCliClient implements Callable<Integer> {

  @Option(names = { "-c", "--config" }, description = "Path to the configuration file (default: ~/.rewrite/config.yaml)")
  private Path configFilePath;

  @Option(names = { "-C", "--context" }, description = "The name of the kubeconfig context to use.")
  private String contextName;

  @Option(names = { "-s", "--server" }, description = "Overrides the server URL from the config file.")
  private String serverUrlOverride;

  @Option(names = { "--repo-url" }, required = true, description = "URL of the Git repository to process.")
  private String repoUrl;

  @Option(names = { "--recipe" }, required = true, description = "Name of the OpenRewrite recipe to apply.")
  private String recipeName;

  // --- CORRECTION ICI : RETIRER required = true ---
  @Option(names = { "--git-pat" }, description = "Git Personal Access Token for repository operations (overrides config).")
  private String gitPatForGit;

  // --- CORRECTION ICI : RETIRER required = true ---
  @Option(names = { "--api-pat" }, description = "Platform API Token for PR/MR creation (overrides config).")
  private String gitPatForApi;

  @Option(names = { "--platform" }, required = true, description = "Git platform (e.g., github, gitlab).")
  private String platform;

  @Option(names = { "--base-branch" }, description = "Base branch for the changes (default: main).")
  private String baseBranch = "main";

  @Option(names = { "--maven-executable" }, description = "Path to Maven executable (e.g., /usr/bin/mvn).")
  private String mavenExecutablePath;

  @Option(names = { "--push-pr" }, defaultValue = "false", description = "Pushing changes and creating PR/MR.")
  private boolean pushAndPr;

  @Getter
  private String commitMessage = "Refactoring by OpenRewrite: AutoFix";

  @Getter
  private String prMrTitle = "Automated Refactoring";

  @Getter
  private String prMrDescription = "Automated refactoring applied using OpenRewrite.";

  @Getter
  private List<String> sourceExcludePatterns = Arrays.asList("target/", ".git/", ".mvn/");

  public static void main(String[] args) {
    int exitCode = new CommandLine(new RewriteCliClient()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Integer call() throws Exception {
    Config config;
    if (configFilePath != null) {
      config = ConfigLoader.loadConfig(configFilePath);
    } else {
      config = ConfigLoader.loadConfig();
    }

    String effectiveContextName = Optional.ofNullable(contextName).orElse(config.getCurrentContext());

    if (effectiveContextName == null) {
      System.err.println("Error: No current context set and no context specified via --context.");
      return 1;
    }

    NamedContext namedContext = config
      .getContexts()
      .stream()
      .filter(c -> c.getName().equals(effectiveContextName))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("Context '" + effectiveContextName + "' not found in config."));

    NamedCluster namedCluster = config
      .getClusters()
      .stream()
      .filter(cl -> cl.getName().equals(namedContext.getContext().getCluster()))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("Cluster '" + namedContext.getContext().getCluster() + "' not found in config."));

    NamedUser namedUser = config
      .getUsers()
      .stream()
      .filter(u -> u.getName().equals(namedContext.getContext().getUser()))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("User '" + namedContext.getContext().getUser() + "' not found in config."));

    String finalServerUrl = Optional.ofNullable(serverUrlOverride).orElse(namedCluster.getCluster().getServer());

    // --- CORRECTION ICI : LOGIQUE DE FALLBACK POUR LES PATs ---
    String effectiveGitPatForGit = Optional.ofNullable(this.gitPatForGit).orElse(namedUser.getUser().getGitPatForGit()); // Priorité à l'option CLI // Sinon, utiliser la config

    String effectiveGitPatForApi = Optional.ofNullable(this.gitPatForApi).orElse(namedUser.getUser().getGitPatForApi()); // Priorité à l'option CLI // Sinon, utiliser la config

    // Vérification des PATs après la résolution
    if (effectiveGitPatForGit == null || effectiveGitPatForGit.isEmpty()) {
      System.err.println(
        "Error: Git Personal Access Token for repository operations (--git-pat) is required but not provided in CLI or config."
      );
      return 1;
    }
    if (effectiveGitPatForApi == null || effectiveGitPatForApi.isEmpty()) {
      System.err.println("Error: Git Personal Access Token for API operations (--api-pat) is required but not provided in CLI or config.");
      return 1;
    }

    System.out.println(namedCluster);
    System.out.println(namedUser);
    HttpClient httpClient = createHttpClient(namedCluster.getCluster(), namedUser.getUser());

    RewriteConfig requestConfig = new RewriteConfig(
      repoUrl,
      recipeName,
      namedUser.getUser().getUsername(),
      effectiveGitPatForGit, // Utiliser la valeur résolue
      effectiveGitPatForApi, // Utiliser la valeur résolue
      platform,
      baseBranch,
      mavenExecutablePath,
      pushAndPr,
      sourceExcludePatterns,
      commitMessage,
      prMrTitle,
      prMrDescription
    );

    ObjectMapper objectMapper = new ObjectMapper();
    try {
      String jsonConfig = objectMapper.writeValueAsString(requestConfig);

      HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(finalServerUrl))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(jsonConfig))
        .build();

      System.out.println("Envoi de la requête au serveur : " + finalServerUrl + "...");
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      System.out.println("Statut de la réponse du serveur : " + response.statusCode());
      System.out.println("Corps de la réponse du serveur : " + response.body());

      if (response.statusCode() >= 200 && response.statusCode() < 300) {
        System.out.println("Processus de réécriture initié avec succès sur le serveur.");
        return 0;
      } else {
        System.err.println("Erreur lors de l'initiation du processus de réécriture sur le serveur.");
        return 1;
      }
    } catch (Exception e) {
      System.err.println("Échec de la communication avec le serveur de réécriture : " + e.getMessage());
      e.printStackTrace();
      return 1;
    }
  }

  private HttpClient createHttpClient(NamedCluster.Cluster clusterConfig, NamedUser.User userConfig) throws Exception {
    SSLContext sslContext = SSLContext.getInstance("TLS");

    KeyManagerFactory keyManagerFactory = null;
    if (userConfig.getClientKeystorePath() != null && !userConfig.getClientKeystorePath().isEmpty()) {
      KeyStore clientKeyStore = KeyStore.getInstance("PKCS12");
      try (InputStream is = new FileInputStream(userConfig.getClientKeystorePath())) {
        clientKeyStore.load(is, userConfig.getClientKeystorePassword().toCharArray());
      }
      keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keyManagerFactory.init(clientKeyStore, userConfig.getClientKeystorePassword().toCharArray());
    }

    TrustManager[] trustManagers = null;

    if (clusterConfig.isInsecureSkipTlsVerify()) {
      System.err.println("WARNING: insecureSkipTlsVerify is true. TLS certificate verification will be skipped.");
      trustManagers = new TrustManager[] {
        new X509TrustManager() {
          @Override
          public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
          }

          @Override
          public void checkClientTrusted(X509Certificate[] certs, String authType) {}

          @Override
          public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        },
      };
    } else if (userConfig.getClientTruststorePath() != null && !userConfig.getClientTruststorePath().isEmpty()) {
      KeyStore trustStore = KeyStore.getInstance("JKS");
      try (InputStream is = new FileInputStream(userConfig.getClientTruststorePath())) {
        String tsPassword = Optional.ofNullable(userConfig.getClientTruststorePassword()).orElse("");
        trustStore.load(is, tsPassword.toCharArray());
      }
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(trustStore);
      trustManagers = trustManagerFactory.getTrustManagers();
    } else if (clusterConfig.getCertificateAuthorityFile() != null && !clusterConfig.getCertificateAuthorityFile().isEmpty()) {
      KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
      trustStore.load(null, null);
      try (InputStream caIs = new FileInputStream(clusterConfig.getCertificateAuthorityFile())) {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate caCert = (X509Certificate) certFactory.generateCertificate(caIs);
        trustStore.setCertificateEntry("ca_cert", caCert);
      }
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(trustStore);
      trustManagers = trustManagerFactory.getTrustManagers();
    } else if (clusterConfig.getCertificateAuthorityData() != null && !clusterConfig.getCertificateAuthorityData().isEmpty()) {
      KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
      trustStore.load(null, null);
      CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
      X509Certificate caCert = (X509Certificate) certFactory.generateCertificate(
        new ByteArrayInputStream(Base64.getDecoder().decode(clusterConfig.getCertificateAuthorityData()))
      );
      trustStore.setCertificateEntry("ca_cert", caCert);
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(trustStore);
      trustManagers = trustManagerFactory.getTrustManagers();
    } else {
      System.out.println(
        "Aucun certificat CA ou truststore spécifié dans la configuration. Le truststore système par défaut sera utilisé pour la vérification du serveur."
      );
    }

    sslContext.init(keyManagerFactory != null ? keyManagerFactory.getKeyManagers() : null, trustManagers, null);

    return HttpClient.newBuilder().sslContext(sslContext).build();
  }
}
