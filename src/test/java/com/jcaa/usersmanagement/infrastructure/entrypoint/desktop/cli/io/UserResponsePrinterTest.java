package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.UserResponse;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserResponsePrinter")
class UserResponsePrinterTest {

    private static final UserResponse USER =
            new UserResponse("u-1", "John Doe", "john@example.com", "MEMBER", "ACTIVE");

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final ConsoleIO console =
            new ConsoleIO(new Scanner(""), new PrintStream(buffer, true, StandardCharsets.UTF_8));
    private final UserResponsePrinter printer = new UserResponsePrinter(console);

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("print() debe mostrar todos los datos del usuario")
    void shouldPrintUserData() {
        printer.print(USER);

        assertTrue(output().contains("u-1"));
        assertTrue(output().contains("John Doe"));
        assertTrue(output().contains("john@example.com"));
        assertTrue(output().contains("MEMBER"));
        assertTrue(output().contains("ACTIVE"));
    }

    @Test
    @DisplayName("printList() debe avisar cuando la lista esta vacia")
    void shouldPrintMessageWhenListIsEmpty() {
        printer.printList(List.of());

        assertTrue(output().contains("No users found."));
    }

    @Test
    @DisplayName("printList() debe mostrar el total y cada usuario")
    void shouldPrintTotalAndUsers() {
        printer.printList(List.of(USER));

        assertTrue(output().contains("Total: 1 user(s)"));
        assertTrue(output().contains("John Doe"));
    }
}