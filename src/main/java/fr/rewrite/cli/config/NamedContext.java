package fr.rewrite.cli.config;

import lombok.Data;

@Data
public class NamedContext {

  private String name;
  private Context context;

  @Data
  public static class Context {

    private String cluster;
    private String user;
  }
}
