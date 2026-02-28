package com.steven.theminautaur.entity;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Coureur (joueur ou IA) dans le labyrinthe.
 */
public class Runner extends Entity {

    public Runner() {
        Circle c = new Circle(12);
        c.setFill(Color.GREEN);
        setView(c);
    }
}
