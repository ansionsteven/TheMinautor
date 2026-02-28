package com.steven.theminautaur.menu;

/**
 * Système de coordonnées de référence (2048×1365) vers fenêtre réelle.
 * s = min(W/Wref, H/Href), ox/oy = offset centré.
 */
public final class MenuLayout {

    public static final double WREF = 2048;
    public static final double HREF = 1365;

    private double w = WREF, h = HREF;
    private double s = 1, ox = 0, oy = 0;

    public void update(double windowW, double windowH) {
        w = windowW;
        h = windowH;
        s = Math.min(w / WREF, h / HREF);
        ox = (w - WREF * s) / 2;
        oy = (h - HREF * s) / 2;
    }

    public double scale() { return s; }
    public double offsetX() { return ox; }
    public double offsetY() { return oy; }

    public double x(double xRef) { return ox + xRef * s; }
    public double y(double yRef) { return oy + yRef * s; }
    public double width(double wRef) { return wRef * s; }
    public double height(double hRef) { return hRef * s; }
}
