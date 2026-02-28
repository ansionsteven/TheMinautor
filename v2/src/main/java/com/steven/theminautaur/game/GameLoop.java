package com.steven.theminautaur.game;

import javafx.animation.AnimationTimer;

/**
 * Boucle de jeu (update à chaque frame).
 */
public final class GameLoop {

    private AnimationTimer timer;
    private Runnable onUpdate;

    public void setOnUpdate(Runnable r) { this.onUpdate = r; }

    public void start() {
        if (timer != null) timer.stop();
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (onUpdate != null) onUpdate.run();
            }
        };
        timer.start();
    }

    public void stop() {
        if (timer != null) { timer.stop(); timer = null; }
    }
}
