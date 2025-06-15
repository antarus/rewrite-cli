package fr.rewrite.cli.application.dto;

public record Credentials(
  String username,
  String pat // Personal Access Token
) {}
