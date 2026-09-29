package com.jcaa.usersmanagement.infrastructure.adapter.persistence.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DatabaseConfig")
class DatabaseConfigTest {

  private static final String HOST = "localhost";
  private static final String DB_NAME = "crud_usuarios";
  private static final String USER = "user";
  private static final String PASSWORD = "pass";

  @Test
  @DisplayName("buildJdbcUrl() genera URL de MySQL")
  void shouldBuildMySqlUrl() {
    // Arrange
    final DatabaseConfig config =
        new DatabaseConfig("mysql", HOST, 3306, DB_NAME, USER, PASSWORD, "prefer");

    // Act
    final String url = config.buildJdbcUrl();

    // Assert
    assertEquals(
        "jdbc:mysql://localhost:3306/crud_usuarios"
            + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
        url);
  }

  @Test
  @DisplayName("buildJdbcUrl() genera URL de PostgreSQL con sslmode")
  void shouldBuildPostgreSqlUrl() {
    // Arrange
    final DatabaseConfig config =
        new DatabaseConfig("PostgreSQL", HOST, 5432, DB_NAME, USER, PASSWORD, "require");

    // Act
    final String url = config.buildJdbcUrl();

    // Assert
    assertEquals("jdbc:postgresql://localhost:5432/crud_usuarios?sslmode=require", url);
  }

  @Test
  @DisplayName("buildJdbcUrl() falla con motor no soportado")
  void shouldFailWithUnsupportedEngine() {
    // Arrange
    final DatabaseConfig config =
        new DatabaseConfig("oracle", HOST, 1521, DB_NAME, USER, PASSWORD, "prefer");

    // Act & Assert
    assertThrows(IllegalArgumentException.class, config::buildJdbcUrl);
  }
}
