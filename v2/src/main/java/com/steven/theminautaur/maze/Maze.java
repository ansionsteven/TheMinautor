package com.steven.theminautaur.maze;

/**
 * Grille du labyrinthe : murs, portes, portails, coffres.
 * Coordonnées en cellules (int).
 */
public final class Maze {

    public static final int WALL = 1;
    public static final int FLOOR = 0;
    public static final int DOOR = 2;
    public static final int PORTAL = 3;
    public static final int CHEST = 4;

    private final int width;
    private final int height;
    private final int[][] grid;

    public Maze(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new int[height][width];
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public int get(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return WALL;
        return grid[y][x];
    }

    public void set(int x, int y, int value) {
        if (x >= 0 && x < width && y >= 0 && y < height)
            grid[y][x] = value;
    }

    public boolean isWalkable(int x, int y) {
        int v = get(x, y);
        return v == FLOOR || v == DOOR || v == PORTAL || v == CHEST;
    }
}
