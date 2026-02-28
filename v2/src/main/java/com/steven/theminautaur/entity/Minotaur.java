package com.steven.theminautaur.entity;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Minotaure (adversaire ou joueur selon le mode).
 */
public class Minotaur extends Entity {

    public Minotaur() {
        Circle c = new Circle(14);
        c.setFill(Color.DARKRED);
        setView(c);
    }
}
