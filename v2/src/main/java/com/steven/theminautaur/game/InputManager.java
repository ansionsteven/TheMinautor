package com.steven.theminautaur.game;

import javafx.scene.input.KeyCode;

import java.util.HashSet;
import java.util.Set;

/**
 * État des touches (en jeu).
 */
public final class InputManager {

    private final Set<KeyCode> pressed = new HashSet<>();

    public void keyDown(KeyCode code) { pressed.add(code); }
    public void keyUp(KeyCode code) { pressed.remove(code); }
    public boolean isPressed(KeyCode code) { return pressed.contains(code); }
}
