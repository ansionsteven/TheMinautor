package com.steven.theminautaur.ai;

import com.steven.theminautaur.maze.Maze;

/**
 * Test de visibilité entre deux cellules (ligne de vue).
 */
public final class LineOfSight {

    public static boolean hasLineOfSight(Maze maze, int x0, int y0, int x1, int y1) {
        int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
        if (steps == 0) return true;
        for (int i = 1; i <= steps; i++) {
            double t = (double) i / steps;
            int x = (int) Math.round(x0 + t * (x1 - x0));
            int y = (int) Math.round(y0 + t * (y1 - y0));
            if (x == x1 && y == y1) return true;
            if (!maze.isWalkable(x, y)) return false;
        }
        return true;
    }
}
