package com.steven.theminautaur;

/**
 * État global de l'application : mode, difficulté, rôle, avatar.
 * La difficulté est gardée en mémoire pour le mode BONUS.
 */
public final class AppState {

    public enum Mode { CLASSIQUE, MULTIJOUEUR, SURVIVAL, BONUS }
    public enum Role { RUNNER, MINOTAUR }

    private static volatile Mode mode = Mode.CLASSIQUE;
    private static volatile int difficulty = 3;
    private static volatile Role role = Role.RUNNER;
    private static volatile int avatarIndex = 1; // 1..8

    private AppState() {}

    public static Mode getMode() { return mode; }
    public static void setMode(Mode m) { mode = m; }

    public static int getDifficulty() { return difficulty; }
    public static void setDifficulty(int d) {
        if (d >= 1 && d <= 5) difficulty = d;
    }

    public static Role getRole() { return role; }
    public static void setRole(Role r) { role = r; }

    public static int getAvatarIndex() { return avatarIndex; }
    public static void setAvatarIndex(int i) {
        if (i >= 1 && i <= 8) avatarIndex = i;
    }
}
