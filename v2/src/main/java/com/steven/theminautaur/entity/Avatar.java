package com.steven.theminautaur.entity;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

import java.io.InputStream;

/**
 * Représentation avatar du joueur (image a1..a8 ou fallback).
 */
public class Avatar extends Entity {

    public Avatar(int avatarIndex) {
        String path = "assets/avatars/a" + Math.max(1, Math.min(8, avatarIndex)) + ".png";
        Image img = null;
        try (InputStream is = Avatar.class.getResourceAsStream("/" + path)) {
            if (is != null) img = new Image(is);
        } catch (Exception ignored) {}
        if (img != null) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(24);
            iv.setFitHeight(24);
            iv.setPreserveRatio(true);
            iv.setClip(new Circle(12, 12, 12));
            setView(iv);
        } else {
            javafx.scene.shape.Circle c = new javafx.scene.shape.Circle(12);
            c.setFill(javafx.scene.paint.Color.GREEN);
            setView(c);
        }
    }
}
