package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MenuOption")
class MenuOptionTest {

    @Test
    @DisplayName("fromNumber() debe devolver la opcion cuando el numero existe")
    void shouldReturnOptionWhenNumberExists() {
        assertEquals(MenuOption.LIST_USERS, MenuOption.fromNumber(1).orElseThrow());
        assertEquals(MenuOption.EXIT, MenuOption.fromNumber(0).orElseThrow());
        assertEquals(MenuOption.LOGIN, MenuOption.fromNumber(6).orElseThrow());
    }

    @Test
    @DisplayName("fromNumber() debe devolver vacio cuando el numero no existe")
    void shouldReturnEmptyWhenNumberDoesNotExist() {
        assertTrue(MenuOption.fromNumber(99).isEmpty());
        assertTrue(MenuOption.fromNumber(-1).isEmpty());
    }

    @Test
    @DisplayName("debe exponer el numero y la descripcion de cada opcion")
    void shouldExposeNumberAndDescription() {
        assertEquals(3, MenuOption.CREATE_USER.getNumber());
        assertEquals("Create user", MenuOption.CREATE_USER.getDescription());
        assertEquals(7, MenuOption.values().length);
    }
}