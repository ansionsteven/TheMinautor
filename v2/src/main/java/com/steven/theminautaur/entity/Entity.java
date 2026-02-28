package com.steven.theminautaur.entity;

import javafx.scene.Node;

/**
 * Entité du jeu : position (cell/tile), représentation graphique.
 */
public abstract class Entity {

    protected double cellX;
    protected double cellY;
    protected Node view;

    public double getCellX() { return cellX; }
    public double getCellY() { return cellY; }
    public void setCellPosition(double x, double y) {
        this.cellX = x;
        this.cellY = y;
    }

    public Node getView() { return view; }
    public void setView(Node view) { this.view = view; }
}
