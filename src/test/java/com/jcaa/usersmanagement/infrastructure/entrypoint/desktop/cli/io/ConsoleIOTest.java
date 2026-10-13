package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ConsoleIO")
class ConsoleIOTest {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    private ConsoleIO consoleWith(final String input) {
        return new ConsoleIO(
                new Scanner(input), new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("readRequired() debe reintentar mientras el valor este en blanco")
    void shouldRetryWhileRequiredValueIsBlank() {
        final ConsoleIO console = consoleWith("\n   \n  value  \n");

        final String result = console.readRequired("Name: ");

        assertEquals("value", result);
        assertTrue(output().contains("Value cannot be blank"));
    }

    @Test
    @DisplayName("readOptional() debe devolver el texto recortado, o vacio")
    void shouldReadOptionalValue() {
        assertEquals("abc", consoleWith("  abc  \n").readOptional("Opt: "));
        assertEquals("", consoleWith("\n").readOptional("Opt: "));
    }

    @Test
    @DisplayName("readInt() debe reintentar hasta recibir un numero valido")
    void shouldRetryUntilValidInteger() {
        final ConsoleIO console = consoleWith("abc\n 42 \n");

        final int result = console.readInt("Option: ");

        assertEquals(42, result);
        assertTrue(output().contains("Please enter a number"));
    }

    @Test
    @DisplayName("println() y printf() deben escribir en la salida")
    void shouldWriteToOutput() {
        final ConsoleIO console = consoleWith("");

        console.println("hello");
        console.println();
        console.printf("%s-%d%n", "x", 7);

        assertTrue(output().contains("hello"));
        assertTrue(output().contains("x-7"));
    }
}