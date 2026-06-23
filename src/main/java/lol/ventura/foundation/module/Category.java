package lol.ventura.foundation.module;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public enum Category {
    COMBAT("Combat", 'e'),
    MOVEMENT("Movement", 'd'),
    PLAYER("Player", 'c'),
    RENDER("Render", 'g');

    private final String name;
    private final char icon;
}
