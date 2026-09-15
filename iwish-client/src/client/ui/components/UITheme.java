package client.ui.components;

import java.awt.*;

public class UITheme {
    // Primary Colors (Indigo & Deep Slate)
    public static final Color PRIMARY = new Color(79, 70, 229);        // Indigo 600
    public static final Color PRIMARY_HOVER = new Color(67, 56, 202);  // Indigo 700
    public static final Color PRIMARY_LIGHT = new Color(238, 242, 255);// Indigo 50

    // Accent Colors
    public static final Color ACCENT = new Color(236, 72, 153);        // Pink 500
    public static final Color SUCCESS = new Color(34, 197, 94);        // Green 500
    public static final Color WARNING = new Color(245, 158, 11);       // Amber 500
    public static final Color DANGER = new Color(239, 68, 68);         // Red 500

    // Neutral Grayscale & Backgrounds
    public static final Color BG_DARK = new Color(15, 23, 42);         // Slate 900
    public static final Color BG_SIDEBAR = new Color(30, 41, 59);      // Slate 800
    public static final Color BG_MAIN = new Color(248, 250, 252);      // Slate 50
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER = new Color(226, 232, 240);       // Slate 200

    // Typography Colors
    public static final Color TEXT_MAIN = new Color(15, 23, 42);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);
    public static final Color TEXT_LIGHT = new Color(241, 245, 249);

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
}
