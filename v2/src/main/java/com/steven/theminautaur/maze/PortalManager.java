package com.steven.theminautaur.maze;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestion des portails : liaison (x1,y1) <-> (x2,y2).
 */
public final class PortalManager {

    private final Map<String, int[]> target = new HashMap<>();

    public void link(int x1, int y1, int x2, int y2) {
        target.put(key(x1, y1), new int[] { x2, y2 });
        target.put(key(x2, y2), new int[] { x1, y1 });
    }

    public int[] getTarget(int x, int y) {
        return target.get(key(x, y));
    }

    private static String key(int x, int y) { return x + "," + y; }
}
