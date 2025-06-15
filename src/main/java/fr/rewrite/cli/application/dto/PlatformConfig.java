package fr.rewrite.cli.application.dto;

public record PlatformConfig(
  String apiBaseUrl,
  String apiToken,
  String repoOwner,
  String repoName,
  String gitlabProjectId,
  String platform // : "github" ou "gitlab"
) {}
