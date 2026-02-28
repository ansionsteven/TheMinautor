package com.steven.theminautaur.ai;

import com.steven.theminautaur.maze.Maze;

import java.util.ArrayList;
import java.util.List;

/**
 * Calcul de chemin (A* ou BFS) dans le labyrinthe.
 */
public final class Pathing {

    public static List<int[]> findPath(Maze maze, int x0, int y0, int x1, int y1) {
        List<int[]> path = new ArrayList<>();
        if (!maze.isWalkable(x1, y1)) return path;
        boolean[][] seen = new boolean[maze.getWidth()][maze.getHeight()];
        int[][] prev = new int[maze.getWidth() * maze.getHeight()][2];
        for (int i = 0; i < prev.length; i++) { prev[i][0] = -1; prev[i][1] = -1; }
        int[] queueX = new int[maze.getWidth() * maze.getHeight()];
        int[] queueY = new int[maze.getWidth() * maze.getHeight()];
        int head = 0, tail = 0;
        queueX[tail] = x0; queueY[tail] = y0; tail++;
        seen[x0][y0] = true;
        int[] dx = { -1, 1, 0, 0 };
        int[] dy = { 0, 0, -1, 1 };
        while (head < tail) {
            int x = queueX[head], y = queueY[head];
            head++;
            if (x == x1 && y == y1) {
                while (x != x0 || y != y0) {
                    path.add(0, new int[] { x, y });
                    int idx = y * maze.getWidth() + x;
                    int nx = prev[idx][0], ny = prev[idx][1];
                    if (nx < 0 || ny < 0) break;
                    x = nx; y = ny;
                }
                return path;
            }
            for (int d = 0; d < 4; d++) {
                int nx = x + dx[d], ny = y + dy[d];
                if (nx >= 0 && nx < maze.getWidth() && ny >= 0 && ny < maze.getHeight()
                    && maze.isWalkable(nx, ny) && !seen[nx][ny]) {
                    seen[nx][ny] = true;
                    prev[ny * maze.getWidth() + nx][0] = x;
                    prev[ny * maze.getWidth() + nx][1] = y;
                    queueX[tail] = nx; queueY[tail] = ny; tail++;
                }
            }
        }
        return path;
    }
}
