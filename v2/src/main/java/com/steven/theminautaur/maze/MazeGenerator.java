package com.steven.theminautaur.maze;

import java.util.Random;

/**
 * Génère un labyrinthe avec couloirs et murs (algorithme simple).
 */
public final class MazeGenerator {

    private static final Random RND = new Random();

    public static Maze generate(int width, int height) {
        Maze m = new Maze(width, height);
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                m.set(x, y, Maze.WALL);

        carve(m, 1, 1, width - 2, height - 2);
        return m;
    }

    private static void carve(Maze m, int x0, int y0, int w, int h) {
        if (w < 3 || h < 3) return;
        int x1 = x0 + w - 1;
        int y1 = y0 + h - 1;
        for (int y = y0; y <= y1; y++) m.set(x0, y, Maze.FLOOR);
        for (int y = y0; y <= y1; y++) m.set(x1, y, Maze.FLOOR);
        for (int x = x0; x <= x1; x++) m.set(x, y0, Maze.FLOOR);
        for (int x = x0; x <= x1; x++) m.set(x, y1, Maze.FLOOR);

        if (w > 5 && h > 5) {
            int mx = x0 + w / 2;
            int my = y0 + h / 2;
            for (int x = x0; x <= x1; x++) m.set(x, my, Maze.FLOOR);
            for (int y = y0; y <= y1; y++) m.set(mx, y, Maze.FLOOR);
            int gap = 1 + RND.nextInt(2);
            m.set(mx, y0 + gap, Maze.FLOOR);
            m.set(mx, y1 - gap, Maze.FLOOR);
            m.set(x0 + gap, my, Maze.FLOOR);
            m.set(x1 - gap, my, Maze.FLOOR);
            carve(m, x0 + 1, y0 + 1, mx - x0 - 1, my - y0 - 1);
            carve(m, mx + 1, y0 + 1, x1 - mx - 1, my - y0 - 1);
            carve(m, x0 + 1, my + 1, mx - x0 - 1, y1 - my - 1);
            carve(m, mx + 1, my + 1, x1 - mx - 1, y1 - my - 1);
        }
    }
}
