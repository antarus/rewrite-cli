package fr.rewrite.cli.application.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // Nécessaire pour la désérialisation JSON/YAML
@AllArgsConstructor
public class RewriteConfig {

  private String repoUrl;
  private String recipeName;
  private String gitUsername;
  private String gitPatForGit;
  private String gitPatForApi;
  private String platform;
  private String baseBranch;
  private String mavenExecutablePath;
  private boolean pushAndPr;
  // suppression de executionContext
  private List<String> sourceExcludePatterns;
  private String commitMessage;
  private String prMrTitle;
  private String prMrDescription;
  // suppression de localRepoDir
}
