package fr.rewrite.cli.application.dto;

public record PullRequestDetails(
  String headBranch,
  String baseBranch,
  String title,
  String description,
  String commitId // ID du commit qui a déclenché la PR/MR
) {}
