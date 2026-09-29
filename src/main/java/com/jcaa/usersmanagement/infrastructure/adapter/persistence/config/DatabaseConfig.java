package com.jcaa.usersmanagement.infrastructure.adapter.persistence.config;

import java.util.Locale;

public record DatabaseConfig(
    String engine,
    String host,
    int port,
    String databaseName,
    String username,
    String password,
    String sslMode) {

  public static final String ENGINE_MYSQL = "mysql";
  public static final String ENGINE_POSTGRESQL = "postgresql";

  private static final String MYSQL_URL_TEMPLATE =
      "jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

  private static final String POSTGRESQL_URL_TEMPLATE = "jdbc:postgresql://%s:%d/%s?sslmode=%s";

  private static final String UNSUPPORTED_ENGINE =
      "Motor de base de datos no soportado: '%s'. Use 'mysql' o 'postgresql'.";

  public String buildJdbcUrl() {
    final String normalizedEngine = engine.trim().toLowerCase(Locale.ROOT);
    return switch (normalizedEngine) {
      case ENGINE_MYSQL -> String.format(MYSQL_URL_TEMPLATE, host, port, databaseName);
      case ENGINE_POSTGRESQL ->
          String.format(POSTGRESQL_URL_TEMPLATE, host, port, databaseName, sslMode);
      default -> throw new IllegalArgumentException(String.format(UNSUPPORTED_ENGINE, engine));
    };
  }
}
