package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io.ConsoleIO;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.controller.UserController;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserManagementCli")
class UserManagementCliTest {

    @Mock private UserController userController;

    private ByteArrayOutputStream buffer;

    @BeforeEach
    void setUp() {
        buffer = new ByteArrayOutputStream();
    }

    private UserManagementCli cliWith(final String... lines) {
        final String input = String.join("\n", lines) + "\n";
        final ConsoleIO console =
                new ConsoleIO(new Scanner(input), new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return new UserManagementCli(userController, console);
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("debe mostrar el banner, el menu y despedirse al elegir salir")
    void shouldShowBannerMenuAndExit() {
        cliWith("0").start();

        assertTrue(output().contains("Users Management System"));
        assertTrue(output().contains("Main Menu"));
        assertTrue(output().contains("Goodbye!"));
    }

    @Test
    @DisplayName("debe avisar cuando la opcion no existe y seguir")
    void shouldReportInvalidOption() {
        cliWith("99", "0").start();

        assertTrue(output().contains("Invalid option"));
    }

    @Test
    @DisplayName("debe pedir un numero de nuevo cuando el texto no es numerico")
    void shouldRetryWhenInputIsNotANumber() {
        cliWith("abc", "0").start();

        assertTrue(output().contains("Please enter a number"));
    }

    @Test
    @DisplayName("debe ejecutar el handler de la opcion elegida")
    void shouldRunSelectedHandler() {
        when(userController.listAllUsers()).thenReturn(List.of());

        cliWith("1", "0").start();

        assertTrue(output().contains("No users found."));
    }

    @Test
    @DisplayName("debe mostrar los errores de validacion")
    void shouldShowValidationErrors() {
        @SuppressWarnings("unchecked")
        final ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("name must not be blank");
        final ConstraintViolationException validationException =
                new ConstraintViolationException(Set.of(violation));
        when(userController.listAllUsers()).thenThrow(validationException);

        cliWith("1", "0").start();

        assertTrue(output().contains("Validation errors:"));
        assertTrue(output().contains("name must not be blank"));
    }

    @Test
    @DisplayName("debe mostrar los errores inesperados sin cerrar el programa")
    void shouldShowUnexpectedErrors() {
        when(userController.listAllUsers()).thenThrow(new IllegalStateException("boom"));

        cliWith("1", "0").start();

        assertTrue(output().contains("Unexpected error: boom"));
        assertTrue(output().contains("Goodbye!"));
    }
}