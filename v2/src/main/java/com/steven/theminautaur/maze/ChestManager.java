package com.steven.theminautaur.maze;

import java.util.HashSet;
import java.util.Set;

/**
 * Gestion des coffres (ouverts/fermés).
 */
public final class ChestManager {

    private final Set<String> opened = new HashSet<>();

    public boolean isOpened(int x, int y) {
        return opened.contains(x + "," + y);
    }

    public void setOpened(int x, int y) {
        opened.add(x + "," + y);
    }
}
