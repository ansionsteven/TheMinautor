package com.steven.theminautaur.maze;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestion des portes (ouvertes/fermées, clés, etc.).
 */
public final class DoorManager {

    private final Maze maze;
    private final Map<String, Boolean> open = new HashMap<>();

    public DoorManager(Maze maze) {
        this.maze = maze;
    }

    public boolean isOpen(int x, int y) {
        return open.getOrDefault(key(x, y), true);
    }

    public void setOpen(int x, int y, boolean o) {
        open.put(key(x, y), o);
    }

    private static String key(int x, int y) { return x + "," + y; }
}
