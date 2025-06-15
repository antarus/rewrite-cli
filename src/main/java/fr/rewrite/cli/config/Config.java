package fr.rewrite.cli.config;

import java.util.List;
import lombok.Data;

@Data
public class Config {

  private String apiVersion = "v1"; // Comme kubectl
  private String currentContext;
  private List<NamedContext> contexts;
  private List<NamedCluster> clusters;
  private List<NamedUser> users;
}
