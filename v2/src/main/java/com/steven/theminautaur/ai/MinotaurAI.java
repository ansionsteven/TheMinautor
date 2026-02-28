package com.steven.theminautaur.ai;

import com.steven.theminautaur.entity.Minotaur;
import com.steven.theminautaur.maze.Maze;

import java.util.List;

/**
 * IA du minotaure (poursuite du runner).
 */
public final class MinotaurAI {

    private final Minotaur minotaur;
    private final Maze maze;

    public MinotaurAI(Minotaur minotaur, Maze maze) {
        this.minotaur = minotaur;
        this.maze = maze;
    }

    public void update(double runnerX, double runnerY) {
        List<int[]> path = Pathing.findPath(maze,
            (int) minotaur.getCellX(), (int) minotaur.getCellY(),
            (int) runnerX, (int) runnerY);
        if (path.size() >= 2) {
            int[] next = path.get(1);
            minotaur.setCellPosition(next[0], next[1]);
        }
    }
}
