package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.handler;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jcaa.usersmanagement.domain.exception.InvalidCredentialsException;
import com.jcaa.usersmanagement.domain.exception.UserAlreadyExistsException;
import com.jcaa.usersmanagement.domain.exception.UserNotFoundException;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io.ConsoleIO;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io.UserResponsePrinter;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.controller.UserController;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.CreateUserRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.LoginRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.UpdateUserRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.UserResponse;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Handlers de la CLI")
class UserHandlersTest {

    private static final UserResponse USER =
            new UserResponse("u-1", "John Doe", "john@example.com", "MEMBER", "ACTIVE");

    @Mock private UserController userController;

    private ByteArrayOutputStream buffer;

    @BeforeEach
    void setUp() {
        buffer = new ByteArrayOutputStream();
    }

    private ConsoleIO consoleWith(final String... lines) {
        final String input = String.join("\n", lines) + "\n";
        return new ConsoleIO(
                new Scanner(input), new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    // ── CreateUserHandler

    @Test
    @DisplayName("CreateUserHandler debe crear el usuario y mostrarlo")
    void shouldCreateUser() {
        final ConsoleIO console =
                consoleWith("u-1", "John Doe", "john@example.com", "Secret123!", "MEMBER");
        when(userController.createUser(any(CreateUserRequest.class))).thenReturn(USER);

        new CreateUserHandler(userController, console, new UserResponsePrinter(console)).handle();

        assertTrue(output().contains("User created successfully."));
        assertTrue(output().contains("John Doe"));
    }

    @Test
    @DisplayName("CreateUserHandler debe mostrar el error si el usuario ya existe")
    void shouldShowErrorWhenUserAlreadyExists() {
        final ConsoleIO console =
                consoleWith("u-1", "John Doe", "john@example.com", "Secret123!", "MEMBER");
        when(userController.createUser(any(CreateUserRequest.class)))
                .thenThrow(UserAlreadyExistsException.becauseEmailAlreadyExists("john@example.com"));

        new CreateUserHandler(userController, console, new UserResponsePrinter(console)).handle();

        assertTrue(output().contains("Error:"));
    }

    // ── UpdateUserHandler

    @Test
    @DisplayName("UpdateUserHandler debe actualizar el usuario sin cambiar la clave si va en blanco")
    void shouldUpdateUserWithBlankPassword() {
        final ConsoleIO console =
                consoleWith("u-1", "John", "john@example.com", "", "MEMBER", "ACTIVE");
        when(userController.updateUser(any(UpdateUserRequest.class))).thenReturn(USER);

        new UpdateUserHandler(userController, console, new UserResponsePrinter(console)).handle();

        assertTrue(output().contains("User updated successfully."));
    }

    @Test
    @DisplayName("UpdateUserHandler debe actualizar el usuario con una clave nueva")
    void shouldUpdateUserWithNewPassword() {
        final ConsoleIO console =
                consoleWith("u-1", "John", "john@example.com", "NewSecret123!", "MEMBER", "ACTIVE");
        when(userController.updateUser(any(UpdateUserRequest.class))).thenReturn(USER);

        new UpdateUserHandler(userController, console, new UserResponsePrinter(console)).handle();

        assertTrue(output().contains("User updated successfully."));
    }

    @Test
    @DisplayName("UpdateUserHandler debe mostrar el error si el usuario no existe")
    void shouldShowErrorWhenUpdatingMissingUser() {
        final ConsoleIO console =
                consoleWith("u-9", "John", "john@example.com", "", "MEMBER", "ACTIVE");
        when(userController.updateUser(any(UpdateUserRequest.class)))
                .thenThrow(UserNotFoundException.becauseIdWasNotFound("u-9"));

        new UpdateUserHandler(userController, console, new UserResponsePrinter(console)).handle();

        assertTrue(output().contains("Not found:"));
    }

    // ── DeleteUserHandler

    @Test
    @DisplayName("DeleteUserHandler debe eliminar el usuario")
    void shouldDeleteUser() {
        final ConsoleIO console = consoleWith("u-1");

        new DeleteUserHandler(userController, console).handle();

        verify(userController).deleteUser("u-1");
        assertTrue(output().contains("User deleted successfully."));
    }

    @Test
    @DisplayName("DeleteUserHandler debe mostrar el error si el usuario no existe")
    void shouldShowErrorWhenDeletingMissingUser() {
        final ConsoleIO console = consoleWith("u-9");
        doThrow(UserNotFoundException.becauseIdWasNotFound("u-9"))
                .when(userController)
                .deleteUser("u-9");

        new DeleteUserHandler(userController, console).handle();

        assertTrue(output().contains("Not found:"));
    }

    // ── FindUserByIdHandler

    @Test
    @DisplayName("FindUserByIdHandler debe mostrar el usuario encontrado")
    void shouldFindUser() {
        final ConsoleIO console = consoleWith("u-1");
        when(userController.findUserById("u-1")).thenReturn(USER);

        new FindUserByIdHandler(userController, console, new UserResponsePrinter(console)).handle();

        assertTrue(output().contains("John Doe"));
    }

    @Test
    @DisplayName("FindUserByIdHandler debe mostrar el error si no existe")
    void shouldShowErrorWhenUserIsNotFound() {
        final ConsoleIO console = consoleWith("u-9");
        when(userController.findUserById("u-9"))
                .thenThrow(UserNotFoundException.becauseIdWasNotFound("u-9"));

        new FindUserByIdHandler(userController, console, new UserResponsePrinter(console)).handle();

        assertTrue(output().contains("Not found:"));
    }

    // ── ListUsersHandler

    @Test
    @DisplayName("ListUsersHandler debe listar los usuarios")
    void shouldListUsers() {
        final ConsoleIO console = consoleWith();
        when(userController.listAllUsers()).thenReturn(List.of(USER));

        new ListUsersHandler(userController, new UserResponsePrinter(console)).handle();

        assertTrue(output().contains("Total: 1 user(s)"));
    }

    @Test
    @DisplayName("ListUsersHandler debe avisar si no hay usuarios")
    void shouldReportNoUsers() {
        final ConsoleIO console = consoleWith();
        when(userController.listAllUsers()).thenReturn(List.of());

        new ListUsersHandler(userController, new UserResponsePrinter(console)).handle();

        assertTrue(output().contains("No users found."));
    }

    // ── LoginHandler

    @Test
    @DisplayName("LoginHandler debe mostrar el usuario cuando el login es correcto")
    void shouldLogin() {
        final ConsoleIO console = consoleWith("john@example.com", "Secret123!");
        when(userController.login(any(LoginRequest.class))).thenReturn(USER);

        new LoginHandler(userController, console, new UserResponsePrinter(console)).handle();

        assertTrue(output().contains("Login successful."));
    }

    @Test
    @DisplayName("LoginHandler debe mostrar el error cuando las credenciales son invalidas")
    void shouldShowErrorWhenCredentialsAreInvalid() {
        final ConsoleIO console = consoleWith("john@example.com", "wrong");
        when(userController.login(any(LoginRequest.class)))
                .thenThrow(InvalidCredentialsException.becauseCredentialsAreInvalid());

        new LoginHandler(userController, console, new UserResponsePrinter(console)).handle();

        assertTrue(output().contains("Error:"));
    }
}
