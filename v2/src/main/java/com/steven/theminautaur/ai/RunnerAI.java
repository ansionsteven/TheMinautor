package com.steven.theminautaur.ai;

import com.steven.theminautaur.entity.Runner;
import com.steven.theminautaur.maze.Maze;

import java.util.List;

/**
 * IA du coureur (éviter le minotaure, atteindre la sortie).
 */
public final class RunnerAI {

    private final Runner runner;
    private final Maze maze;

    public RunnerAI(Runner runner, Maze maze) {
        this.runner = runner;
        this.maze = maze;
    }

    public void update(double minotaurX, double minotaurY) {
        // Stub : pourrait utiliser Pathing pour fuir ou avancer
    }
}
