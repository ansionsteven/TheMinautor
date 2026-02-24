package theminautor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * The Minautor — Swing edition
 * - Menus horror + fond noir clignotant rouge irrégulier
 * - 5 modes: Avatar, Classique, Multijoueur, Survival, Bonus
 * - Labyrinthe à boucles (multi-connexions)
 * - Halo circulaire (vision) + warning "Il est proche" quand le minotaur approche
 * - Portes dynamiques (ouvrent/ferment) en garantissant un chemin vers l'objectif
 * - Coffres temps + portails (disparaissent après usage)
 * - IA (joueurs et minotaur) en BFS distance-map
 *
 * NOTE: Un seul fichier, plusieurs classes (top-level non public) : simple à coller dans NetBeans.
 */
public class TheMinautor {

    // ===================== APP SETTINGS =====================
    static final String APP_TITLE = "The Minautor";
    static final Preferences PREFS = Preferences.userNodeForPackage(TheMinautor.class);
    static final String PREF_AVATAR = "selected_avatar";
    static final int DEFAULT_AVATAR = 0;

    // Horror palette
    static final Color RED = new Color(200, 0, 0);
    static final Color DARK_RED = new Color(80, 0, 0);
    static final Color NEAR_BLACK = new Color(5, 5, 5);

    // Fonts: attempt horror-ish fallback
    static Font TITLE_FONT = new Font("Serif", Font.BOLD | Font.ITALIC, 56);
    static Font MENU_FONT  = new Font("Serif", Font.BOLD | Font.ITALIC, 28);
    static Font UI_FONT    = new Font("SansSerif", Font.BOLD, 16);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new AppFrame().setVisible(true);
        });
    }

    // ===================== TOP-LEVEL FRAME =====================
    static class AppFrame extends JFrame {
        final CardLayout cards = new CardLayout();
        final JPanel root = new JPanel(cards);

        final MainMenuPanel mainMenu;
        final AvatarMenuPanel avatarMenu;
        final ModeConfigPanel classicConfig;
        final ModeConfigPanel multiConfig;
        final ModeConfigPanel survivalConfig;
        final BonusConfigPanel bonusConfig;
        final GamePanel gamePanel;

        AppFrame() {
            super(APP_TITLE);

            // Max bounds avoids covering taskbar
            Rectangle max = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            setBounds(max);
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            mainMenu = new MainMenuPanel(this);
            avatarMenu = new AvatarMenuPanel(this);
            classicConfig = new ModeConfigPanel(this, GameMode.CLASSIQUE);
            multiConfig = new ModeConfigPanel(this, GameMode.MULTIJOUEUR);
            survivalConfig = new ModeConfigPanel(this, GameMode.SURVIVAL);
            bonusConfig = new BonusConfigPanel(this);
            gamePanel = new GamePanel(this);

            root.add(mainMenu, "main");
            root.add(avatarMenu, "avatar");
            root.add(classicConfig, "classic");
            root.add(multiConfig, "multi");
            root.add(survivalConfig, "survival");
            root.add(bonusConfig, "bonus");
            root.add(gamePanel, "game");

            setContentPane(root);

            showMainMenu();
        }

        void showMainMenu() {
            mainMenu.requestFocusInWindow();
            cards.show(root, "main");
        }
        void showAvatarMenu() {
            avatarMenu.syncFromPrefs();
            avatarMenu.requestFocusInWindow();
            cards.show(root, "avatar");
        }
        void showConfig(GameMode mode) {
            switch (mode) {
                case CLASSIQUE -> { classicConfig.syncDefaults(); cards.show(root, "classic"); classicConfig.requestFocusInWindow(); }
                case MULTIJOUEUR -> { multiConfig.syncDefaults(); cards.show(root, "multi"); multiConfig.requestFocusInWindow(); }
                case SURVIVAL -> { survivalConfig.syncDefaults(); cards.show(root, "survival"); survivalConfig.requestFocusInWindow(); }
                default -> showMainMenu();
            }
        }
        void showBonusConfig() {
            bonusConfig.syncDefaults();
            bonusConfig.requestFocusInWindow();
            cards.show(root, "bonus");
        }
        void startGame(GameSetup setup) {
            gamePanel.startNewGame(setup);
            cards.show(root, "game");
            gamePanel.requestFocusInWindow();
        }
    }

    // ===================== GAME MODE ENUMS =====================
    enum GameMode { AVATAR, CLASSIQUE, MULTIJOUEUR, SURVIVAL, BONUS }
    enum Role { JOUEUR, MINAUTOR }

    static class GameSetup {
        GameMode mode;
        Role role;
        int difficulty; // 1..5 ; unused in BONUS
        int selectedAvatar;
        int bonusLevel; // for BONUS mode
    }

    // ===================== HORROR BACKGROUND PANEL =====================
    static abstract class HorrorPanel extends JPanel {
        private final javax.swing.Timer blinkTimer;
        private long nextJitterAt = 0;
        private int blinkAlpha = 0; // 0..140
        private boolean rising = true;
        private int baseDelay = 35;

        HorrorPanel() {
            setBackground(Color.BLACK);
            setOpaque(true);
            setFocusable(true);

            blinkTimer = new javax.swing.Timer(baseDelay, e -> {
                // irregular jitter: change speed/target randomly
                long now = System.currentTimeMillis();
                if (now > nextJitterAt) {
                    nextJitterAt = now + 250 + (long)(Math.random() * 900);
                    baseDelay = 18 + (int)(Math.random() * 55);
                    blinkTimer.setDelay(baseDelay);
                }

                int step = 2 + (int)(Math.random() * 10);
                if (rising) blinkAlpha += step; else blinkAlpha -= step;

                if (blinkAlpha > 140) { blinkAlpha = 140; rising = false; }
                if (blinkAlpha < 0)   { blinkAlpha = 0;   rising = true;  }

                repaint();
            });
            blinkTimer.start();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            // base black
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // irregular red flash overlay
            g2.setComposite(AlphaComposite.SrcOver.derive(blinkAlpha / 255f));
            g2.setColor(DARK_RED);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.dispose();
        }

        void stopBlink() { blinkTimer.stop(); }
        void startBlink() { blinkTimer.start(); }
    }

    // ===================== MAIN MENU =====================
    static class MainMenuPanel extends HorrorPanel {
        final AppFrame frame;

        final String[] items = {"Avatar", "Classique", "Multijoueur", "Survival", "Bonus"};
        int focused = 0;

        MainMenuPanel(AppFrame frame) {
            this.frame = frame;
            setLayout(new BorderLayout());

            add(makeTitle("The Minautor"), BorderLayout.NORTH);

            JPanel left = new JPanel();
            left.setOpaque(false);
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.setBorder(new EmptyBorder(30, 40, 30, 40));

            // menu labels (not buttons): underline only when focused
            java.util.List<MenuItemLabel> labels = new ArrayList<>();
            for (int i = 0; i < items.length; i++) {
                MenuItemLabel lab = new MenuItemLabel(items[i]);
                int idx = i;
                lab.addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { focused = idx; updateFocus(labels); }
                    @Override public void mouseClicked(MouseEvent e) { activate(idx); }
                });
                labels.add(lab);
                left.add(lab);
                left.add(Box.createVerticalStrut(18));
            }
            updateFocus(labels);

            add(left, BorderLayout.WEST);

            JLabel help = new JLabel("<html><div style='width:520px;'>"
                    + "<span style='color:#ff3333;font-family:sans-serif;font-size:16px;'>"
                    + "Flèches ↑↓ pour naviguer, Entrée pour sélectionner.</span><br><br>"
                    + "<span style='color:#bbbbbb;font-family:sans-serif;font-size:14px;'>"
                    + "Astuce: plus la difficulté est haute, plus ton halo rétrécit, les portes s’affolent, "
                    + "et les IA deviennent... disons... moins polies.</span>"
                    + "</div></html>");
            help.setBorder(new EmptyBorder(30, 20, 30, 30));
            help.setOpaque(false);
            add(help, BorderLayout.CENTER);

            // key controls
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"), "up");
            getActionMap().put("up", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    focused = (focused - 1 + items.length) % items.length;
                    updateFocus(labels);
                }
            });
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"), "down");
            getActionMap().put("down", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    focused = (focused + 1) % items.length;
                    updateFocus(labels);
                }
            });
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "enter");
            getActionMap().put("enter", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    activate(focused);
                }
            });
        }

        void activate(int idx) {
            switch (idx) {
                case 0 -> frame.showAvatarMenu();
                case 1 -> frame.showConfig(GameMode.CLASSIQUE);
                case 2 -> frame.showConfig(GameMode.MULTIJOUEUR);
                case 3 -> frame.showConfig(GameMode.SURVIVAL);
                case 4 -> frame.showBonusConfig();
            }
        }

        void updateFocus(java.util.List<MenuItemLabel> labels) {
            for (int i = 0; i < labels.size(); i++) labels.get(i).setFocused(i == focused);
            repaint();
        }
    }

    static class MenuItemLabel extends JComponent {
        String text;
        boolean focused = false;

        MenuItemLabel(String text) {
            this.text = text;
            setPreferredSize(new Dimension(260, 40));
            setMaximumSize(new Dimension(260, 40));
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void setFocused(boolean f) { focused = f; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.setFont(MENU_FONT);
            g2.setColor(RED);

            FontMetrics fm = g2.getFontMetrics();
            int x = 0;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

            g2.drawString(text, x, y);

            if (focused) {
                int w = fm.stringWidth(text);
                int underlineY = y + 4;
                g2.setStroke(new BasicStroke(2.2f));
                g2.drawLine(x, underlineY, x + w, underlineY);
            }
            g2.dispose();
        }
    }

    static JComponent makeTitle(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setForeground(RED);
        t.setFont(TITLE_FONT);
        t.setBorder(new EmptyBorder(25, 40, 10, 10));
        p.add(t, BorderLayout.WEST);
        return p;
    }

    // ===================== AVATAR MENU =====================
    static class AvatarMenuPanel extends HorrorPanel {
        final AppFrame frame;
        int selected = DEFAULT_AVATAR;

        final Avatar[] avatars = Avatar.createDefault();
        final JLabel info = new JLabel("", SwingConstants.CENTER);

        // clickable boxes cache
        final java.util.List<Rectangle> avatarBoxes = new ArrayList<>();

        AvatarMenuPanel(AppFrame frame) {
            this.frame = frame;
            setLayout(new BorderLayout());

            add(makeTitle("Avatar"), BorderLayout.NORTH);

            // SOUTH container (info + buttons) => avoids layout bug and preserves clicks for bottom row
            JPanel south = new JPanel();
            south.setOpaque(false);
            south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
            south.setBorder(new EmptyBorder(8, 25, 22, 25));

            info.setForeground(new Color(255, 120, 120));
            info.setFont(new Font("SansSerif", Font.BOLD, 16));
            info.setAlignmentX(Component.CENTER_ALIGNMENT);
            info.setBorder(new EmptyBorder(8, 8, 10, 8));

            JPanel btnRow = new JPanel(new BorderLayout());
            btnRow.setOpaque(false);

            JButton back = horrorButton("← Retour");
            JButton ok = horrorButton("Valider");
            back.addActionListener(e -> frame.showMainMenu());
            ok.addActionListener(e -> {
                PREFS.putInt(PREF_AVATAR, selected);
                frame.showMainMenu();
            });

            btnRow.add(back, BorderLayout.WEST);
            btnRow.add(ok, BorderLayout.EAST);

            south.add(info);
            south.add(btnRow);

            add(south, BorderLayout.SOUTH);

            // key controls
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("LEFT"), "left");
            getActionMap().put("left", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { selected = (selected - 1 + avatars.length) % avatars.length; updateInfo(); repaint(); }
            });
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "right");
            getActionMap().put("right", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { selected = (selected + 1) % avatars.length; updateInfo(); repaint(); }
            });
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "enter");
            getActionMap().put("enter", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    PREFS.putInt(PREF_AVATAR, selected);
                    frame.showMainMenu();
                }
            });

            // mouse selection (click avatar)
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    Point p = e.getPoint();
                    for (int i = 0; i < avatarBoxes.size(); i++) {
                        if (avatarBoxes.get(i).contains(p)) {
                            selected = i;
                            updateInfo();
                            repaint();
                            break;
                        }
                    }
                }
            });

            updateInfo();
        }

        void syncFromPrefs() {
            selected = PREFS.getInt(PREF_AVATAR, DEFAULT_AVATAR);
            updateInfo();
            repaint();
        }

        void updateInfo() {
            info.setText("Avatar sélectionné : " + avatars[selected].name + "   (← → ou clic)");
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            avatarBoxes.clear();

            int w = getWidth();
            int h = getHeight();

            // draw 8 avatars grid (2 rows x 4)
            int cols = 4, rows = 2;
            int pad = 30;

            // reserve for title top and south (info+buttons)
            int topY = 110;
            int bottomReserve = 120;
            int usableH = Math.max(220, h - topY - bottomReserve);

            int cellW = (w - pad * 2) / cols;
            int cellH = usableH / rows;

            int idx = 0;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int x = pad + c * cellW;
                    int y = topY + r * cellH;

                    Rectangle box = new Rectangle(x + 18, y + 18, cellW - 36, cellH - 36);
                    avatarBoxes.add(new Rectangle(box));

                    // frame
                    g2.setComposite(AlphaComposite.SrcOver.derive(0.35f));
                    g2.setColor(new Color(120, 0, 0));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(box.x, box.y, box.width, box.height, 18, 18);

                    // avatar
                    g2.setComposite(AlphaComposite.SrcOver.derive(1f));
                    avatars[idx].draw(g2, box.x + box.width / 2, box.y + box.height / 2 - 8, Math.min(box.width, box.height) / 3);

                    // name
                    g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                    g2.setColor(new Color(255, 170, 170));
                    String nm = avatars[idx].name;
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(nm, box.x + (box.width - fm.stringWidth(nm)) / 2, box.y + box.height - 10);

                    // underline selected
                    if (idx == selected) {
                        g2.setColor(RED);
                        g2.setStroke(new BasicStroke(3f));
                        int ux1 = box.x + 20;
                        int ux2 = box.x + box.width - 20;
                        int uy = box.y + box.height + 8;
                        g2.drawLine(ux1, uy, ux2, uy);
                    }

                    idx++;
                }
            }

            g2.dispose();
        }
    }

    static JButton horrorButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setForeground(new Color(255, 120, 120));
        b.setBackground(new Color(20, 0, 0));
        b.setFont(new Font("SansSerif", Font.BOLD, 16));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 0, 0), 2),
                new EmptyBorder(10, 14, 10, 14)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}

// ===================== MODE CONFIG MENUS + AVATARS =====================
class ModeConfigPanel extends TheMinautor.HorrorPanel {
    final TheMinautor.AppFrame frame;
    final TheMinautor.GameMode mode;

    int difficulty = 3;
    TheMinautor.Role role = TheMinautor.Role.JOUEUR;

    final JLabel subtitle = new JLabel("", SwingConstants.CENTER);
    final JSlider slider = new JSlider(1, 5, 3);
    final JLabel phrase = new JLabel("", SwingConstants.CENTER);

    final JRadioButton rbJoueur = new JRadioButton("Joueur");
    final JRadioButton rbMinautor = new JRadioButton("Minautor");

    ModeConfigPanel(TheMinautor.AppFrame frame, TheMinautor.GameMode mode) {
        this.frame = frame;
        this.mode = mode;
        setLayout(new BorderLayout());

        add(TheMinautor.makeTitle(modeTitle(mode)), BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(new EmptyBorder(20, 50, 20, 50));

        subtitle.setForeground(new Color(255, 120, 120));
        subtitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        subtitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(subtitle);
        center.add(Box.createVerticalStrut(18));

        // difficulty slider
        slider.setOpaque(false);
        slider.setMajorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setSnapToTicks(true);
        slider.setForeground(new Color(255, 140, 140));
        slider.setAlignmentX(Component.CENTER_ALIGNMENT);

        phrase.setForeground(new Color(240, 170, 170));
        phrase.setFont(new Font("SansSerif", Font.BOLD, 18));
        phrase.setBorder(new EmptyBorder(12, 0, 0, 0));
        phrase.setAlignmentX(Component.CENTER_ALIGNMENT);

        slider.addChangeListener(e -> {
            difficulty = slider.getValue();
            phrase.setText(diffPhrase(difficulty));
        });

        center.add(labelLine("Difficulté"));
        center.add(slider);
        center.add(phrase);
        center.add(Box.createVerticalStrut(25));

        // role
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbJoueur);
        bg.add(rbMinautor);
        rbJoueur.setOpaque(false);
        rbMinautor.setOpaque(false);
        rbJoueur.setForeground(new Color(255, 120, 120));
        rbMinautor.setForeground(new Color(255, 120, 120));
        rbJoueur.setFont(new Font("SansSerif", Font.BOLD, 16));
        rbMinautor.setFont(new Font("SansSerif", Font.BOLD, 16));

        rbJoueur.setSelected(true);

        JPanel roles = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        roles.setOpaque(false);
        roles.add(rbJoueur);
        roles.add(rbMinautor);

        rbJoueur.addActionListener(a -> role = TheMinautor.Role.JOUEUR);
        rbMinautor.addActionListener(a -> role = TheMinautor.Role.MINAUTOR);

        center.add(labelLine("Choix du rôle"));
        center.add(roles);

        add(center, BorderLayout.CENTER);

        // bottom nav
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 25, 25, 25));

        JButton back = TheMinautor.horrorButton("← Retour");
        JButton go = TheMinautor.horrorButton("Entrez dans le labyrinthe →");

        back.addActionListener(e -> frame.showMainMenu());
        go.addActionListener(e -> launch());

        bottom.add(back, BorderLayout.WEST);
        bottom.add(go, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        // key binds
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "back");
        getActionMap().put("back", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { frame.showMainMenu(); }
        });

        syncDefaults();
    }

    void syncDefaults() {
        difficulty = 3;
        role = TheMinautor.Role.JOUEUR;
        slider.setValue(difficulty);
        phrase.setText(diffPhrase(difficulty));
        subtitle.setText("Pendant la partie : appuie sur r pour revenir au menu.");
        rbJoueur.setSelected(true);
        rbMinautor.setSelected(false);
    }

    void launch() {
        TheMinautor.GameSetup s = new TheMinautor.GameSetup();
        s.mode = mode;
        s.role = role;
        s.difficulty = difficulty;
        s.selectedAvatar = TheMinautor.PREFS.getInt(TheMinautor.PREF_AVATAR, TheMinautor.DEFAULT_AVATAR);
        s.bonusLevel = 1;
        frame.startGame(s);
    }

    static String modeTitle(TheMinautor.GameMode m) {
        return switch (m) {
            case CLASSIQUE -> "Classique";
            case MULTIJOUEUR -> "Multijoueur";
            case SURVIVAL -> "Survival";
            default -> "Mode";
        };
    }

    static JLabel labelLine(String txt) {
        JLabel l = new JLabel(txt, SwingConstants.CENTER);
        l.setForeground(new Color(255, 150, 150));
        l.setFont(new Font("SansSerif", Font.BOLD, 18));
        l.setBorder(new EmptyBorder(10, 0, 10, 0));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    static String diffPhrase(int d) {
        return switch (d) {
            case 1 -> "Niveau 1 : « Tu es faible et tu as peur »";
            case 2 -> "Niveau 2 : « tu commences à te sentir chaud ? »";
            case 3 -> "Niveau 3 : « On commence seulement à s’amuser »";
            case 4 -> "Niveau 4 : « tu vas transpirer… le minautor aussi »";
            case 5 -> "Niveau 5 : « tu viens de signer ton arrêt de mort »";
            default -> "";
        };
    }
}

// BONUS config: pas de difficulté, pas de minotaur jouable
class BonusConfigPanel extends TheMinautor.HorrorPanel {
    final TheMinautor.AppFrame frame;

    BonusConfigPanel(TheMinautor.AppFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());

        add(TheMinautor.makeTitle("Bonus"), BorderLayout.NORTH);

        JLabel txt = new JLabel("<html><div style='width:700px;text-align:center;'>"
                + "<span style='color:#ff7777;font-family:sans-serif;font-size:18px;font-weight:700;'>"
                + "Mode Bonus</span><br><br>"
                + "<span style='color:#bbbbbb;font-family:sans-serif;font-size:14px;'>"
                + "Même principe que Classique, mais à chaque victoire tu montes d’un niveau. "
                + "Au niveau N, tu as N minotaures. Une vie est récupérée tous les 2 niveaux. "
                + "Si tu perds, retour niveau 1.</span><br><br>"
                + "<span style='color:#ff9999;font-family:sans-serif;font-size:14px;'>"
                + "Pas de choix Minotaur ici. Tu es la proie. Bonne chance.</span>"
                + "</div></html>", SwingConstants.CENTER);
        txt.setBorder(new EmptyBorder(40, 40, 40, 40));
        add(txt, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 25, 25, 25));

        JButton back = TheMinautor.horrorButton("← Retour");
        JButton go = TheMinautor.horrorButton("Entrez dans le labyrinthe →");

        back.addActionListener(e -> frame.showMainMenu());
        go.addActionListener(e -> launch());

        bottom.add(back, BorderLayout.WEST);
        bottom.add(go, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
    }

    void syncDefaults() { /* nothing */ }

    void launch() {
        TheMinautor.GameSetup s = new TheMinautor.GameSetup();
        s.mode = TheMinautor.GameMode.BONUS;
        s.role = TheMinautor.Role.JOUEUR;
        s.difficulty = 3; // still affects halo a bit
        s.selectedAvatar = TheMinautor.PREFS.getInt(TheMinautor.PREF_AVATAR, TheMinautor.DEFAULT_AVATAR);
        s.bonusLevel = 1;
        frame.startGame(s);
    }
}

// ===================== AVATAR DEFINITIONS =====================
class Avatar {
    final String name;
    final int id;

    Avatar(int id, String name) { this.id = id; this.name = name; }

    // draw at center (cx,cy) with size
    void draw(Graphics2D g2, int cx, int cy, int size) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (id) {
            case 0 -> drawGhost(g2, cx, cy, size);
            case 1 -> drawSkull(g2, cx, cy, size);
            case 2 -> drawEye(g2, cx, cy, size);
            case 3 -> drawMask(g2, cx, cy, size);
            case 4 -> drawSpider(g2, cx, cy, size);
            case 5 -> drawKnife(g2, cx, cy, size);
            case 6 -> drawCandle(g2, cx, cy, size);
            case 7 -> drawBat(g2, cx, cy, size);
            default -> drawGhost(g2, cx, cy, size);
        }
    }

    static Avatar[] createDefault() {
        return new Avatar[] {
                new Avatar(0, "Spectre"),
                new Avatar(1, "Crâne"),
                new Avatar(2, "Œil"),
                new Avatar(3, "Masque"),
                new Avatar(4, "Araignée"),
                new Avatar(5, "Lame"),
                new Avatar(6, "Bougie"),
                new Avatar(7, "Chauve-souris")
        };
    }

    // ---- simple icon drawings (horror-ish) ----
    static void drawGhost(Graphics2D g2, int cx, int cy, int s) {
        g2.setColor(new Color(230, 230, 230));
        int w = s, h = (int)(s * 1.2);
        int x = cx - w/2, y = cy - h/2;
        RoundRectangle2D body = new RoundRectangle2D.Float(x, y, w, h, s/2f, s/2f);
        g2.fill(body);
        g2.setColor(Color.BLACK);
        g2.fillOval(cx - s/5, cy - s/6, s/7, s/7);
        g2.fillOval(cx + s/10, cy - s/6, s/7, s/7);
        g2.setStroke(new BasicStroke(3f));
        g2.drawArc(cx - s/6, cy - s/20, s/3, s/4, 210, 120);
    }

    static void drawSkull(Graphics2D g2, int cx, int cy, int s) {
        g2.setColor(new Color(235, 235, 235));
        g2.fillOval(cx - s/2, cy - s/2, s, s);
        g2.setColor(Color.BLACK);
        g2.fillOval(cx - s/4, cy - s/6, s/6, s/5);
        g2.fillOval(cx + s/8, cy - s/6, s/6, s/5);
        g2.fillRect(cx - s/6, cy + s/6, s/3, s/10);
    }

    static void drawEye(Graphics2D g2, int cx, int cy, int s) {
        g2.setColor(new Color(240, 240, 240));
        g2.fill(new Ellipse2D.Float(cx - s/2f, cy - s/4f, s, s/2f));
        g2.setColor(new Color(120, 0, 0));
        g2.fillOval(cx - s/8, cy - s/8, s/4, s/4);
        g2.setColor(Color.BLACK);
        g2.fillOval(cx - s/16, cy - s/16, s/8, s/8);
    }

    static void drawMask(Graphics2D g2, int cx, int cy, int s) {
        g2.setColor(new Color(220, 220, 220));
        g2.fillRoundRect(cx - s/2, cy - s/2, s, s, 26, 26);
        g2.setColor(new Color(120, 0, 0));
        g2.setStroke(new BasicStroke(4f));
        g2.drawLine(cx - s/3, cy - s/8, cx - s/12, cy - s/8);
        g2.drawLine(cx + s/12, cy - s/8, cx + s/3, cy - s/8);
        g2.setColor(Color.BLACK);
        g2.drawArc(cx - s/5, cy, s/2, s/3, 0, -180);
    }

    static void drawSpider(Graphics2D g2, int cx, int cy, int s) {
        g2.setColor(new Color(30, 30, 30));
        g2.fillOval(cx - s/6, cy - s/8, s/3, s/4);
        g2.fillOval(cx - s/8, cy, s/4, s/3);
        g2.setStroke(new BasicStroke(3f));
        for (int i = -3; i <= 3; i += 2) {
            g2.drawLine(cx, cy, cx + i*s/4, cy - s/4);
            g2.drawLine(cx, cy, cx + i*s/4, cy + s/4);
        }
    }

    static void drawKnife(Graphics2D g2, int cx, int cy, int s) {
        g2.setColor(new Color(200, 200, 200));
        Polygon blade = new Polygon();
        blade.addPoint(cx - s/8, cy - s/2);
        blade.addPoint(cx + s/8, cy - s/2);
        blade.addPoint(cx + s/6, cy + s/4);
        blade.addPoint(cx - s/6, cy + s/4);
        g2.fillPolygon(blade);
        g2.setColor(new Color(120, 0, 0));
        g2.fillRect(cx - s/5, cy + s/4, (int)(s*0.4), (int)(s*0.18));
    }

    static void drawCandle(Graphics2D g2, int cx, int cy, int s) {
        g2.setColor(new Color(220, 220, 220));
        g2.fillRoundRect(cx - s/6, cy - s/3, s/3, (int)(s*0.7), 12, 12);
        g2.setColor(new Color(255, 170, 50));
        g2.fillOval(cx - s/10, cy - s/2, s/5, s/4);
        g2.setColor(new Color(80, 0, 0));
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(cx, cy - s/3, cx, cy - s/2 + 6);
    }

    static void drawBat(Graphics2D g2, int cx, int cy, int s) {
        g2.setColor(new Color(20, 20, 20));
        Polygon p = new Polygon();
        p.addPoint(cx, cy);
        p.addPoint(cx - s/2, cy - s/6);
        p.addPoint(cx - s/3, cy + s/6);
        p.addPoint(cx - s/6, cy + s/12);
        p.addPoint(cx, cy + s/5);
        p.addPoint(cx + s/6, cy + s/12);
        p.addPoint(cx + s/3, cy + s/6);
        p.addPoint(cx + s/2, cy - s/6);
        g2.fillPolygon(p);
        g2.setColor(new Color(200, 0, 0));
        g2.fillOval(cx - 8, cy - 6, 5, 5);
        g2.fillOval(cx + 3, cy - 6, 5, 5);
    }
}

// ===================== GAME ENGINE =====================
class GamePanel extends JPanel {
    final TheMinautor.AppFrame frame;

    // game loop
    javax.swing.Timer tick;
    long lastMs = 0;

    // setup
    TheMinautor.GameSetup setup;

    // map
    Maze maze;
    int cellSize;
    int offsetX, offsetY;

    // entities
    java.util.List<Entity> runners = new ArrayList<>();
    java.util.List<Entity> minotaurs = new ArrayList<>();
    Entity human; // whichever role controlled by player
    Avatar[] avatarPool = Avatar.createDefault();

    // state
    boolean running = false;
    boolean gameOver = false;
    boolean win = false;

    boolean centerReachedGlobal = false; // for classic/bonus runner
    int lives = 3;
    int bonusLevel = 1;

    // timer classic/bonus
    int timeLeftMs = 10 * 60 * 1000;

    // UI message
    String fadeMsg = null;
    float fadeMsgAlpha = 0f;
    String topAlert = null;
    boolean topAlertBlink = false;

    // door tick
    int doorTickMs = 0;

    // input
    final Set<Integer> keysDown = new HashSet<>();

    GamePanel(TheMinautor.AppFrame frame) {
        this.frame = frame;
        setFocusable(true);
        setBackground(Color.BLACK);

        // KeyListener works if focus stays; use bindings too
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                keysDown.add(e.getKeyCode());
            }
            @Override public void keyReleased(KeyEvent e) {
                keysDown.remove(e.getKeyCode());
            }
        });

        // r to return to menu (lowercase sufficient). Keep R too.
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('r'), "back");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('R'), "back");
        getActionMap().put("back", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                stopGame();
                frame.showMainMenu();
            }
        });
    }

    void startNewGame(TheMinautor.GameSetup setup) {
        this.setup = setup;
        this.bonusLevel = Math.max(1, setup.bonusLevel);
        this.running = true;
        this.gameOver = false;
        this.win = false;
        this.centerReachedGlobal = false;
        this.topAlert = null;
        this.fadeMsg = null;
        this.fadeMsgAlpha = 0f;
        this.doorTickMs = 0;

        initMapAndEntities();

        if (tick != null) tick.stop();
        lastMs = System.currentTimeMillis();
        tick = new javax.swing.Timer(16, e -> step());
        tick.start();
        requestFocusInWindow();
    }

    void stopGame() {
        running = false;
        if (tick != null) tick.stop();
        tick = null;
    }

    void initMapAndEntities() {
        // map size based on window; avoid too many cells (performance)
        int w = getWidth() > 0 ? getWidth() : frame.getWidth();
        int h = getHeight() > 0 ? getHeight() : frame.getHeight();

        // Reserve top bar height
        int topBar = 52;
        int usableH = h - topBar;

        // choose cell size by resolution
        cellSize = (w >= 1600) ? 26 : (w >= 1200) ? 24 : 22;

        int cols = Math.max(25, Math.min(55, w / cellSize));
        int rows = Math.max(19, Math.min(45, usableH / cellSize));

        if (cols % 2 == 0) cols--;
        if (rows % 2 == 0) rows--;

        maze = new Maze(cols, rows);
        maze.generateLoopMaze();

        // Place features
        maze.placeCenter();
        maze.placeDoors(8 + setup.difficulty * 4);
        if (setup.mode == TheMinautor.GameMode.CLASSIQUE || setup.mode == TheMinautor.GameMode.BONUS) {
            maze.placeChests();
            maze.placePortals();
        }

        // offsets to center map
        offsetX = (w - cols * cellSize) / 2;
        offsetY = topBar + (usableH - rows * cellSize) / 2;

        runners.clear();
        minotaurs.clear();

        int selectedAvatar = setup.selectedAvatar;
        int aCount = avatarPool.length;

        Random rnd = new Random();

        Point[] corners = new Point[] {
                new Point(1, 1),
                new Point(cols - 2, 1),
                new Point(1, rows - 2),
                new Point(cols - 2, rows - 2)
        };

        if (setup.mode == TheMinautor.GameMode.CLASSIQUE) {
            lives = 3;
            timeLeftMs = 10 * 60 * 1000;
            centerReachedGlobal = false;

            Point sp = corners[rnd.nextInt(4)];
            Entity runner = Entity.runner(sp.x, sp.y, avatarPool[selectedAvatar], true);
            runners.add(runner);

            // minotaur: spawn not glued to center anymore (slightly away)
            Point mp = randomWalkableFarFromCenter(rnd);
            Entity mino = Entity.minotaur(mp.x, mp.y, false);
            minotaurs.add(mino);

            if (setup.role == TheMinautor.Role.JOUEUR) human = runner;
            else human = mino;

        } else if (setup.mode == TheMinautor.GameMode.MULTIJOUEUR) {
            lives = 1;
            timeLeftMs = 0;

            ArrayList<Integer> avatarIds = new ArrayList<>();
            for (int i = 0; i < aCount; i++) avatarIds.add(i);
            avatarIds.remove((Integer) selectedAvatar);
            Collections.shuffle(avatarIds);

            int humanCorner = rnd.nextInt(4);
            Entity humanRunner = Entity.runner(corners[humanCorner].x, corners[humanCorner].y, avatarPool[selectedAvatar], true);
            humanRunner.playerIndex = 1;
            humanRunner.ownedExit = corners[humanCorner];
            runners.add(humanRunner);

            int idx = 0;
            for (int i = 0; i < 4; i++) {
                if (i == humanCorner) continue;
                int av = avatarIds.get(idx++);
                Entity ai = Entity.runner(corners[i].x, corners[i].y, avatarPool[av], false);
                ai.playerIndex = runners.size() + 1;
                ai.ownedExit = corners[i];
                runners.add(ai);
            }

            Point mp = randomWalkableFarFromCenter(rnd);
            Entity mino = Entity.minotaur(mp.x, mp.y, setup.role == TheMinautor.Role.MINAUTOR);
            minotaurs.add(mino);

            human = (setup.role == TheMinautor.Role.JOUEUR) ? humanRunner : mino;

        } else if (setup.mode == TheMinautor.GameMode.SURVIVAL) {
            lives = 1;
            timeLeftMs = 0;

            ArrayList<Integer> avatarIds = new ArrayList<>();
            for (int i = 0; i < aCount; i++) avatarIds.add(i);
            avatarIds.remove((Integer) selectedAvatar);
            Collections.shuffle(avatarIds);

            Point sp = randomWalkableFarFromCenter(rnd);
            Entity hr = Entity.runner(sp.x, sp.y, avatarPool[selectedAvatar], true);
            hr.playerIndex = 1;
            runners.add(hr);

            for (int i = 0; i < 7; i++) {
                int av = avatarIds.get(i % avatarIds.size());
                Point sp2 = randomWalkableFarFromCenter(rnd);
                Entity ai = Entity.runner(sp2.x, sp2.y, avatarPool[av], false);
                ai.playerIndex = runners.size() + 1;
                runners.add(ai);
            }

            Point mp = randomWalkableFarFromCenter(rnd);
            Entity mino = Entity.minotaur(mp.x, mp.y, setup.role == TheMinautor.Role.MINAUTOR);
            minotaurs.add(mino);

            human = (setup.role == TheMinautor.Role.JOUEUR) ? hr : mino;

        } else if (setup.mode == TheMinautor.GameMode.BONUS) {
            if (setup.bonusLevel <= 1) lives = 3;
            if (bonusLevel > 1 && bonusLevel % 2 == 1) lives = Math.min(3, lives + 1);

            timeLeftMs = 10 * 60 * 1000;
            centerReachedGlobal = false;

            Point sp = corners[rnd.nextInt(4)];
            Entity runner = Entity.runner(sp.x, sp.y, avatarPool[selectedAvatar], true);
            runners.add(runner);

            // N minotaurs: spread them around (not stacked at center)
            for (int i = 0; i < bonusLevel; i++) {
                Point mp = randomWalkableFarFromCenter(rnd);
                Entity mino = Entity.minotaur(mp.x, mp.y, false);
                minotaurs.add(mino);
            }

            human = runner;

        } else {
            frame.showMainMenu();
        }
    }

    Point randomWalkableFarFromCenter(Random rnd) {
        for (int k = 0; k < 4000; k++) {
            int x = 1 + rnd.nextInt(maze.w - 2);
            int y = 1 + rnd.nextInt(maze.h - 2);
            if (!maze.walkable(x, y)) continue;
            int d = Math.abs(x - maze.center.x) + Math.abs(y - maze.center.y);
            if (d > (maze.w + maze.h) / 5) return new Point(x, y);
        }
        return new Point(1, 1);
    }

    void step() {
        if (!running) return;
        long now = System.currentTimeMillis();
        int dt = (int)Math.min(50, now - lastMs);
        lastMs = now;

        if (gameOver || win) {
            repaint();
            return;
        }

        if (setup.mode == TheMinautor.GameMode.CLASSIQUE || setup.mode == TheMinautor.GameMode.BONUS) {
            timeLeftMs -= dt;
            if (timeLeftMs <= 0) {
                timeLeftMs = 0;
                triggerGameOver();
                repaint();
                return;
            }
        }

        doorTickMs += dt;
        int doorPeriod = switch (setup.difficulty) {
            case 1 -> 1800;
            case 2 -> 1400;
            case 3 -> 1100;
            case 4 -> 850;
            case 5 -> 650;
            default -> 1100;
        };
        if (doorTickMs > doorPeriod) {
            doorTickMs = 0;
            toggleRandomDoorSafely();
        }

        int[][] distToCenter = maze.bfsDistances(maze.center.x, maze.center.y);

        handleHumanInput(dt);
        updateAI(dt, distToCenter);
        resolveInteractions();
        updateProximityAlert();

        if (fadeMsg != null) {
            fadeMsgAlpha -= dt / 800f;
            if (fadeMsgAlpha <= 0f) { fadeMsg = null; fadeMsgAlpha = 0f; }
        }

        repaint();
    }

    void handleHumanInput(int dt) {
        if (human == null) return;
        int speed = human.isMinotaur ? speedMinotaur() : speedRunner();
        human.moveAccumulator += dt * speed;

        if (human.moveAccumulator < 1000) return;
        int steps = (int)(human.moveAccumulator / 1000);
        human.moveAccumulator -= steps * 1000;

        for (int i = 0; i < steps; i++) {
            int dx = 0, dy = 0;
            if (keysDown.contains(KeyEvent.VK_LEFT)) dx = -1;
            else if (keysDown.contains(KeyEvent.VK_RIGHT)) dx = 1;
            else if (keysDown.contains(KeyEvent.VK_UP)) dy = -1;
            else if (keysDown.contains(KeyEvent.VK_DOWN)) dy = 1;

            if (dx != 0 || dy != 0) attemptMove(human, dx, dy);
        }
    }

    int speedRunner() {
        return switch (setup.difficulty) {
            case 1 -> 4;
            case 2 -> 5;
            case 3 -> 6;
            case 4 -> 7;
            case 5 -> 8;
            default -> 6;
        };
    }
    int speedMinotaur() {
        return switch (setup.difficulty) {
            case 1 -> 4;
            case 2 -> 6;
            case 3 -> 7;
            case 4 -> 8;
            case 5 -> 9;
            default -> 7;
        };
    }

    void updateAI(int dt, int[][] distToCenter) {
        for (Entity r : runners) {
            if (!r.alive) continue;
            if (r.isHumanControlled) continue;

            int sp = speedRunner();
            r.moveAccumulator += dt * sp;
            if (r.moveAccumulator < 1000) continue;
            int steps = (int)(r.moveAccumulator / 1000);
            r.moveAccumulator -= steps * 1000;

            for (int s = 0; s < steps; s++) {
                aiMoveRunner(r, distToCenter);
            }
        }

        for (Entity m : minotaurs) {
            if (!m.alive) continue;
            if (m.isHumanControlled) continue;

            int sp = speedMinotaur();
            m.moveAccumulator += dt * sp;
            if (m.moveAccumulator < 1000) continue;
            int steps = (int)(m.moveAccumulator / 1000);
            m.moveAccumulator -= steps * 1000;

            for (int s = 0; s < steps; s++) {
                aiMoveMinotaur(m, distToCenter, dt);
            }
        }
    }

    void aiMoveRunner(Entity r, int[][] distToCenter) {
        if (setup.mode == TheMinautor.GameMode.MULTIJOUEUR) {
            if (!r.centerReached) {
                stepTowardLowerDist(r, distToCenter);
            } else {
                if (r.ownedExit != null) {
                    int[][] distToExit = maze.bfsDistances(r.ownedExit.x, r.ownedExit.y);
                    stepTowardLowerDist(r, distToExit);
                }
            }
            return;
        }

        if (setup.mode == TheMinautor.GameMode.SURVIVAL) {
            Entity closest = closestMinotaur(r);
            if (closest == null) return;

            int aliveCount = (int) runners.stream().filter(x -> x.alive).count();
            if (aliveCount <= 1 && maze.exit != null) {
                int[][] distExit = maze.bfsDistances(maze.exit.x, maze.exit.y);
                stepTowardLowerDist(r, distExit);
                return;
            }

            stepAwayFrom(r, closest);
            return;
        }

        if (setup.mode == TheMinautor.GameMode.CLASSIQUE || setup.mode == TheMinautor.GameMode.BONUS) {
            if (!centerReachedGlobal) {
                stepTowardLowerDist(r, distToCenter);
            } else {
                if (maze.exit != null) {
                    int[][] distExit = maze.bfsDistances(maze.exit.x, maze.exit.y);
                    stepTowardLowerDist(r, distExit);
                }
            }
        }
    }

    // ✅ minotaure: plus de camping au centre => roam target global
    void aiMoveMinotaur(Entity m, int[][] distToCenter, int dt) {
        Entity target = chooseTargetRunner(m);
        boolean omniscient = (setup.mode == TheMinautor.GameMode.CLASSIQUE || setup.mode == TheMinautor.GameMode.BONUS)
                && centerReachedGlobal;

        if (setup.mode == TheMinautor.GameMode.MULTIJOUEUR && target != null && target.centerReached) omniscient = true;
        if (setup.mode == TheMinautor.GameMode.SURVIVAL) {
            int aliveCount = (int) runners.stream().filter(x -> x.alive).count();
            if (aliveCount <= 1 && maze.exit != null) omniscient = true;
        }

        int sight = 5 + (setup.difficulty * 2);
        boolean sees = false;
        if (target != null) {
            sees = omniscient || (manhattan(m, target) <= sight && maze.hasLineOfSight(m.x, m.y, target.x, target.y));
        }

        if (target != null && sees) {
            int[][] distToRunner = maze.bfsDistances(target.x, target.y);
            stepTowardLowerDist(m, distToRunner);
            m.roamCooldownMs = 0; // reset roam when in chase
            return;
        }

        // roam logic
        m.roamCooldownMs -= dt;
        if (m.roamTarget == null || m.roamCooldownMs <= 0 || (m.x == m.roamTarget.x && m.y == m.roamTarget.y)) {
            m.roamTarget = pickRoamTarget(m);
            m.roamCooldownMs = 1200 + (int)(Math.random() * 1400); // change target often
        }

        if (m.roamTarget != null) {
            int[][] distToRoam = maze.bfsDistances(m.roamTarget.x, m.roamTarget.y);
            stepTowardLowerDist(m, distToRoam);
        } else {
            // fallback
            stepTowardLowerDist(m, distToCenter);
        }
    }

    Point pickRoamTarget(Entity m) {
        Random rnd = new Random();
        // small chance to bias to center (protect), but mostly roam anywhere
        if (rnd.nextDouble() < 0.18) return maze.center;

        // pick random far-ish point
        for (int k = 0; k < 2000; k++) {
            Point p = maze.randomWalkableCell();
            int d = Math.abs(p.x - m.x) + Math.abs(p.y - m.y);
            if (d > (maze.w + maze.h) / 6) return p;
        }
        return maze.randomWalkableCell();
    }

    Entity chooseTargetRunner(Entity m) {
        Entity best = null;
        int bestD = Integer.MAX_VALUE;
        for (Entity r : runners) {
            if (!r.alive) continue;
            int d = Math.abs(r.x - m.x) + Math.abs(r.y - m.y);
            if (d < bestD) { bestD = d; best = r; }
        }
        return best;
    }

    Entity closestMinotaur(Entity r) {
        Entity best = null;
        int bestD = Integer.MAX_VALUE;
        for (Entity m : minotaurs) {
            if (!m.alive) continue;
            int d = manhattan(r, m);
            if (d < bestD) { bestD = d; best = m; }
        }
        return best;
    }

    int manhattan(Entity a, Entity b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    void stepTowardLowerDist(Entity e, int[][] dist) {
        int best = dist[e.y][e.x];
        int bx = e.x, by = e.y;

        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        shuffleDirs(dirs);

        for (int[] d : dirs) {
            int nx = e.x + d[0], ny = e.y + d[1];
            if (!maze.canMove(e.x, e.y, nx, ny)) continue;
            int nd = dist[ny][nx];
            if (nd >= 0 && nd < best) {
                best = nd;
                bx = nx; by = ny;
            }
        }
        if (bx != e.x || by != e.y) {
            e.x = bx; e.y = by;
        } else {
            for (int[] d : dirs) {
                int nx = e.x + d[0], ny = e.y + d[1];
                if (maze.canMove(e.x, e.y, nx, ny)) { e.x = nx; e.y = ny; break; }
            }
        }
    }

    void stepAwayFrom(Entity e, Entity threat) {
        int best = manhattan(e, threat);
        int bx = e.x, by = e.y;
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        shuffleDirs(dirs);
        for (int[] d : dirs) {
            int nx = e.x + d[0], ny = e.y + d[1];
            if (!maze.canMove(e.x, e.y, nx, ny)) continue;
            int nd = Math.abs(nx - threat.x) + Math.abs(ny - threat.y);
            if (nd > best) { best = nd; bx = nx; by = ny; }
        }
        if (bx != e.x || by != e.y) {
            e.x = bx; e.y = by;
        }
    }

    void shuffleDirs(int[][] dirs) {
        for (int i = dirs.length - 1; i > 0; i--) {
            int j = (int)(Math.random() * (i + 1));
            int[] tmp = dirs[i]; dirs[i] = dirs[j]; dirs[j] = tmp;
        }
    }

    void attemptMove(Entity e, int dx, int dy) {
        int nx = e.x + dx, ny = e.y + dy;
        if (maze.canMove(e.x, e.y, nx, ny)) { e.x = nx; e.y = ny; }
    }

    void resolveInteractions() {
        for (Entity r : runners) {
            if (!r.alive) continue;
            if (r.x == maze.center.x && r.y == maze.center.y && !r.centerReached) {
                r.centerReached = true;

                if (setup.mode == TheMinautor.GameMode.CLASSIQUE || setup.mode == TheMinautor.GameMode.BONUS) {
                    centerReachedGlobal = true;
                    if (maze.exit == null) {
                        maze.spawnExitRandomCorner();
                    }
                    showFade("La porte est ouverte");
                } else if (setup.mode == TheMinautor.GameMode.MULTIJOUEUR) {
                    showFade("Joueur " + r.playerIndex + " a ouvert sa porte");
                    r.exitOpened = true;
                }
            }
        }

        if (setup.mode == TheMinautor.GameMode.CLASSIQUE || setup.mode == TheMinautor.GameMode.BONUS) {
            Entity runner = runners.isEmpty() ? null : runners.get(0);
            if (runner != null && runner.alive && maze.exit != null && runner.x == maze.exit.x && runner.y == maze.exit.y) {
                triggerWin();
                return;
            }
        }

        if (setup.mode == TheMinautor.GameMode.MULTIJOUEUR) {
            for (Entity r : runners) {
                if (!r.alive) continue;
                if (!r.exitOpened) continue;
                if (r.ownedExit != null && r.x == r.ownedExit.x && r.y == r.ownedExit.y) {
                    win = true;
                    running = false;
                    showFade("Tu as échappé au Minautor… pour l’instant !");
                    return;
                }
            }
        }

        if (setup.mode == TheMinautor.GameMode.SURVIVAL) {
            for (Entity m : minotaurs) {
                if (!m.alive) continue;
                for (Entity r : runners) {
                    if (!r.alive) continue;
                    if (r.x == m.x && r.y == m.y) {
                        r.alive = false;
                    }
                }
            }

            int aliveCount = (int) runners.stream().filter(x -> x.alive).count();
            if (aliveCount <= 0) {
                triggerGameOver();
                return;
            }
            if (aliveCount == 1) {
                if (maze.exit == null) {
                    maze.spawnExitRandomCorner();
                    showFade("La porte s’est ouverte");
                }
                Entity last = runners.stream().filter(x -> x.alive).findFirst().orElse(null);
                if (last != null && maze.exit != null && last.x == maze.exit.x && last.y == maze.exit.y) {
                    triggerWin();
                    return;
                }
            }
        }

        if (setup.mode == TheMinautor.GameMode.CLASSIQUE || setup.mode == TheMinautor.GameMode.MULTIJOUEUR || setup.mode == TheMinautor.GameMode.BONUS) {
            for (Entity m : minotaurs) {
                if (!m.alive) continue;
                for (Entity r : runners) {
                    if (!r.alive) continue;
                    if (r.x == m.x && r.y == m.y) {
                        if (setup.mode == TheMinautor.GameMode.CLASSIQUE || setup.mode == TheMinautor.GameMode.BONUS) {
                            if (!r.hitCooldown) {
                                lives--;
                                r.hitCooldown = true;
                                r.hitCooldownMs = 900;
                                showFade("Le Minautor t'a touché");
                                if (lives <= 0) { triggerGameOver(); return; }
                            }
                        } else {
                            r.alive = false;
                        }
                    }
                }
            }
        }

        for (Entity r : runners) {
            if (r.hitCooldown) {
                r.hitCooldownMs -= 16;
                if (r.hitCooldownMs <= 0) r.hitCooldown = false;
            }
        }

        if (setup.mode == TheMinautor.GameMode.CLASSIQUE || setup.mode == TheMinautor.GameMode.BONUS) {
            Entity runner = runners.isEmpty() ? null : runners.get(0);
            if (runner != null && runner.alive) {
                Maze.Chest c = maze.chests.get(new Point(runner.x, runner.y));
                if (c != null && !c.taken) {
                    c.taken = true;
                    timeLeftMs += c.addMs;
                    showFade("+" + (c.addMs/1000) + " s");
                }

                Maze.Portal p = maze.portals.get(new Point(runner.x, runner.y));
                if (p != null && !p.used) {
                    p.used = true;
                    Point dest = maze.randomWalkableCell();
                    runner.x = dest.x; runner.y = dest.y;
                    showFade("Téléportation");
                }
            }

            for (Entity m : minotaurs) {
                if (!m.alive) continue;
                Maze.Portal p = maze.portals.get(new Point(m.x, m.y));
                if (p != null && !p.used) {
                    p.used = true;
                    Point dest = maze.randomWalkableCell();
                    m.x = dest.x; m.y = dest.y;
                }
            }
        }
    }

    void updateProximityAlert() {
        Entity focus = human;
        if (focus == null) return;

        Entity closest = focus.isMinotaur ? closestRunnerTo(focus) : closestMinotaur(focus);
        if (closest == null) { topAlert = null; return; }

        int d = Math.abs(focus.x - closest.x) + Math.abs(focus.y - closest.y);
        int threshold = 6 + (setup.difficulty * 2);
        if (d <= threshold) {
            topAlert = "Il est proche";
            topAlertBlink = (System.currentTimeMillis() / 180) % 2 == 0;
        } else {
            topAlert = null;
        }
    }

    Entity closestRunnerTo(Entity m) {
        Entity best = null;
        int bestD = Integer.MAX_VALUE;
        for (Entity r : runners) {
            if (!r.alive) continue;
            int d = Math.abs(r.x - m.x) + Math.abs(r.y - m.y);
            if (d < bestD) { bestD = d; best = r; }
        }
        return best;
    }

    void toggleRandomDoorSafely() {
        if (maze.doors.isEmpty()) return;
        Random rnd = new Random();
        Maze.Door door = maze.doors.get(rnd.nextInt(maze.doors.size()));
        boolean newClosed = !door.closed;

        if (newClosed) {
            door.closed = true;
            if (!isAllGoalsReachable()) door.closed = false;
        } else {
            door.closed = false;
        }
    }

    boolean isAllGoalsReachable() {
        if (setup.mode == TheMinautor.GameMode.CLASSIQUE || setup.mode == TheMinautor.GameMode.BONUS) {
            Entity runner = runners.isEmpty() ? null : runners.get(0);
            if (runner == null || !runner.alive) return true;
            Point goal = (!centerReachedGlobal) ? maze.center : maze.exit;
            if (goal == null) goal = maze.center;
            return maze.hasPath(runner.x, runner.y, goal.x, goal.y);
        }

        if (setup.mode == TheMinautor.GameMode.MULTIJOUEUR) {
            for (Entity r : runners) {
                if (!r.alive) continue;
                if (!r.centerReached) {
                    if (!maze.hasPath(r.x, r.y, maze.center.x, maze.center.y)) return false;
                } else if (r.ownedExit != null) {
                    if (!maze.hasPath(r.x, r.y, r.ownedExit.x, r.ownedExit.y)) return false;
                }
            }
            return true;
        }

        if (setup.mode == TheMinautor.GameMode.SURVIVAL) {
            int aliveCount = (int) runners.stream().filter(x -> x.alive).count();
            if (aliveCount == 1 && maze.exit != null) {
                Entity last = runners.stream().filter(x -> x.alive).findFirst().orElse(null);
                if (last == null) return true;
                return maze.hasPath(last.x, last.y, maze.exit.x, maze.exit.y);
            }
            return true;
        }

        return true;
    }

    void showFade(String msg) {
        fadeMsg = msg;
        fadeMsgAlpha = 1.0f;
    }

    void triggerGameOver() {
        gameOver = true;
        running = false;
        if (setup.mode == TheMinautor.GameMode.BONUS) bonusLevel = 1;
    }

    void triggerWin() {
        win = true;
        running = false;

        if (setup.mode == TheMinautor.GameMode.BONUS) {
            bonusLevel++;
            new javax.swing.Timer(700, e -> {
                ((javax.swing.Timer)e.getSource()).stop();
                TheMinautor.GameSetup ns = new TheMinautor.GameSetup();
                ns.mode = TheMinautor.GameMode.BONUS;
                ns.role = TheMinautor.Role.JOUEUR;
                ns.difficulty = setup.difficulty;
                ns.selectedAvatar = setup.selectedAvatar;
                ns.bonusLevel = bonusLevel;
                startNewGame(ns);
            }).start();
        }
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawMap(g2);
        drawEntities(g2);
        drawHalo(g2);
        drawTopBar(g2);

        if (fadeMsg != null) drawFadeMessage(g2);
        if (gameOver) drawEndOverlay(g2, "Game over");
        if (win && setup.mode != TheMinautor.GameMode.BONUS) drawEndOverlay(g2, "Tu as échappé au Minautor… pour l’instant !");

        g2.dispose();
    }

    void drawMap(Graphics2D g2) {
        if (maze == null) return;

        // floor
        g2.setColor(new Color(175, 175, 175));
        g2.fillRect(offsetX, offsetY, maze.w * cellSize, maze.h * cellSize);

        // ✅ walls thicker (mur ~ largeur chemin)
        float wallStroke = Math.max(8f, cellSize * 0.55f);
        g2.setColor(new Color(45, 45, 45));
        g2.setStroke(new BasicStroke(wallStroke, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));

        for (int y = 0; y < maze.h; y++) {
            for (int x = 0; x < maze.w; x++) {
                int px = offsetX + x * cellSize;
                int py = offsetY + y * cellSize;

                if (maze.wallN[y][x]) g2.drawLine(px, py, px + cellSize, py);
                if (maze.wallW[y][x]) g2.drawLine(px, py, px, py + cellSize);
                if (y == maze.h - 1 && maze.wallS[y][x]) g2.drawLine(px, py + cellSize, px + cellSize, py + cellSize);
                if (x == maze.w - 1 && maze.wallE[y][x]) g2.drawLine(px + cellSize, py, px + cellSize, py + cellSize);
            }
        }

        // doors
        for (Maze.Door d : maze.doors) {
            int x1 = d.x1, y1 = d.y1, x2 = d.x2, y2 = d.y2;
            int ax = offsetX + x1 * cellSize + cellSize/2;
            int ay = offsetY + y1 * cellSize + cellSize/2;
            int bx = offsetX + x2 * cellSize + cellSize/2;
            int by = offsetY + y2 * cellSize + cellSize/2;

            g2.setStroke(new BasicStroke(Math.max(10f, wallStroke * 0.85f)));
            g2.setColor(d.closed ? new Color(120, 0, 0) : new Color(70, 0, 0));
            g2.drawLine(ax, ay, bx, by);
        }

        // center marker
        int cx = offsetX + maze.center.x * cellSize;
        int cy = offsetY + maze.center.y * cellSize;
        g2.setColor(new Color(20, 160, 20));
        g2.fillRect(cx+2, cy+2, cellSize-4, cellSize-4);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(cx+4, cy+4, cx+cellSize-4, cy+cellSize-4);
        g2.drawLine(cx+cellSize-4, cy+4, cx+4, cy+cellSize-4);

        // exit marker
        if (maze.exit != null) {
            int ex = offsetX + maze.exit.x * cellSize;
            int ey = offsetY + maze.exit.y * cellSize;
            g2.setColor(new Color(140, 60, 200));
            g2.fillOval(ex+4, ey+4, cellSize-8, cellSize-8);
        }

        // ✅ chests: look more like chests + time text
        for (Maze.Chest c : maze.chests.values()) {
            if (c.taken) continue;
            int x = offsetX + c.pos.x * cellSize;
            int y = offsetY + c.pos.y * cellSize;

            // base chest
            g2.setColor(new Color(90, 45, 10));
            g2.fillRoundRect(x+5, y+8, cellSize-10, cellSize-13, 8, 8);

            // lid
            g2.setColor(new Color(120, 60, 15));
            g2.fillRoundRect(x+5, y+5, cellSize-10, (cellSize-10)/3, 8, 8);

            // metal band
            g2.setColor(new Color(220, 180, 80));
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(x+6, y+cellSize/2, x+cellSize-6, y+cellSize/2);

            // lock
            g2.fillRect(x + cellSize/2 - 2, y + cellSize/2 - 2, 4, 7);

            // time text
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(new Color(255, 230, 150));
            String t = "+" + (c.addMs/1000) + "s";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(t, x + (cellSize - fm.stringWidth(t))/2, y + 14);
        }

        // portals
        for (Maze.Portal p : maze.portals.values()) {
            if (p.used) continue;
            int x = offsetX + p.pos.x * cellSize;
            int y = offsetY + p.pos.y * cellSize;
            g2.setColor(new Color(0, 35, 85));
            g2.fillOval(x+4, y+4, cellSize-8, cellSize-8);
        }

        if (setup.mode == TheMinautor.GameMode.MULTIJOUEUR) {
            for (Entity r : runners) {
                if (!r.exitOpened || r.ownedExit == null) continue;
                int ex2 = offsetX + r.ownedExit.x * cellSize;
                int ey2 = offsetY + r.ownedExit.y * cellSize;
                g2.setColor(new Color(160, 120, 0));
                g2.fillRect(ex2+6, ey2+6, cellSize-12, cellSize-12);
            }
        }
    }

    void drawEntities(Graphics2D g2) {
        for (Entity r : runners) {
            if (!r.alive) continue;
            drawRunner(g2, r);
        }
        for (Entity m : minotaurs) {
            if (!m.alive) continue;
            drawMinotaur(g2, m);
        }

        if (human != null && human.isMinotaur) {
            boolean showAll = false;
            if (setup.mode == TheMinautor.GameMode.CLASSIQUE || setup.mode == TheMinautor.GameMode.BONUS) showAll = centerReachedGlobal;
            if (setup.mode == TheMinautor.GameMode.MULTIJOUEUR) showAll = runners.stream().anyMatch(r -> r.centerReached);
            if (setup.mode == TheMinautor.GameMode.SURVIVAL) {
                int alive = (int) runners.stream().filter(x -> x.alive).count();
                showAll = (alive <= 1 && maze.exit != null);
            }
            if (showAll) {
                for (Entity r : runners) {
                    if (!r.alive) continue;
                    int px = offsetX + r.x * cellSize + cellSize/2;
                    int py = offsetY + r.y * cellSize + cellSize/2;
                    g2.setColor(new Color(255, 180, 180));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawOval(px-8, py-8, 16, 16);
                }
            }
        }
    }

    void drawRunner(Graphics2D g2, Entity r) {
        int px = offsetX + r.x * cellSize + cellSize/2;
        int py = offsetY + r.y * cellSize + cellSize/2;
        int sz = (int)(cellSize * 0.7);
        if (r.avatar != null) r.avatar.draw(g2, px, py, sz);
        else {
            g2.setColor(Color.WHITE);
            g2.fillOval(px - sz/2, py - sz/2, sz, sz);
        }
    }

    void drawMinotaur(Graphics2D g2, Entity m) {
        int px = offsetX + m.x * cellSize + cellSize/2;
        int py = offsetY + m.y * cellSize + cellSize/2;
        int sz = (int)(cellSize * 0.85);

        g2.setColor(new Color(180, 0, 0));
        g2.fillOval(px - sz/2, py - sz/2, sz, sz);

        g2.setColor(new Color(70, 70, 70));
        Polygon left = new Polygon();
        left.addPoint(px - sz/2, py - sz/3);
        left.addPoint(px - sz/2 - sz/3, py - sz/2);
        left.addPoint(px - sz/4, py - sz/2 + 4);
        g2.fillPolygon(left);

        Polygon right = new Polygon();
        right.addPoint(px + sz/2, py - sz/3);
        right.addPoint(px + sz/2 + sz/3, py - sz/2);
        right.addPoint(px + sz/4, py - sz/2 + 4);
        g2.fillPolygon(right);

        g2.setColor(Color.BLACK);
        g2.fillOval(px - sz/6, py - sz/10, sz/10, sz/10);
        g2.fillOval(px + sz/12, py - sz/10, sz/10, sz/10);
    }

    void drawHalo(Graphics2D g2) {
        if (human == null || maze == null) return;

        // ✅ Halo smaller + stronger scaling
        int base = (int)(Math.min(maze.w, maze.h) * cellSize * 0.14);
        int radius = switch (setup.difficulty) {
            case 1 -> base + 70;
            case 2 -> base + 45;
            case 3 -> base + 25;
            case 4 -> base + 10;
            case 5 -> base - 5;
            default -> base + 25;
        };
        radius = Math.max(85, radius);

        float blink = 1.0f;
        if (topAlert != null) {
            blink = ((System.currentTimeMillis() / 120) % 2 == 0) ? 0.75f : 1.0f;
        }

        int cx = offsetX + human.x * cellSize + cellSize/2;
        int cy = offsetY + human.y * cellSize + cellSize/2;

        // ✅ outside = fully black (no leaking vision)
        BufferedImage mask = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D mg = mask.createGraphics();
        mg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        mg.setComposite(AlphaComposite.Src);
        mg.setColor(new Color(0,0,0,255));
        mg.fillRect(0,0,getWidth(),getHeight());

        mg.setComposite(AlphaComposite.Clear);
        int r = (int)(radius * blink);
        mg.fillOval(cx - r, cy - r, 2*r, 2*r);

        mg.dispose();
        g2.drawImage(mask, 0, 0, null);
    }

    void drawTopBar(Graphics2D g2) {
        int barH = 52;
        g2.setColor(new Color(10, 0, 0));
        g2.fillRect(0, 0, getWidth(), barH);
        g2.setColor(new Color(100, 0, 0));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(0, barH, getWidth(), barH);

        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.setColor(new Color(255, 120, 120));

        String modeTxt = "Mode: " + switch (setup.mode) {
            case CLASSIQUE -> "Classique";
            case MULTIJOUEUR -> "Multijoueur";
            case SURVIVAL -> "Survival";
            case BONUS -> "Bonus (Niveau " + bonusLevel + ")";
            default -> "???";
        };

        String livesTxt = (setup.mode == TheMinautor.GameMode.CLASSIQUE || setup.mode == TheMinautor.GameMode.BONUS)
                ? ("Vies: " + lives) : "Vies: —";

        String timeTxt = (setup.mode == TheMinautor.GameMode.CLASSIQUE || setup.mode == TheMinautor.GameMode.BONUS)
                ? ("Temps: " + formatTime(timeLeftMs)) : "Temps: —";

        g2.drawString(modeTxt, 18, 32);
        g2.drawString(livesTxt, 260, 32);
        g2.drawString(timeTxt, 380, 32);

        if (topAlert != null) {
            if (topAlertBlink) {
                g2.setColor(new Color(255, 30, 30));
                g2.drawString(topAlert, 560, 32);
            }
        }

        g2.setColor(new Color(180, 180, 180));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2.drawString("r : retour menu", getWidth() - 150, 32);
    }

    String formatTime(int ms) {
        int total = Math.max(0, ms / 1000);
        int m = total / 60;
        int s = total % 60;
        return String.format("%d:%02d", m, s);
    }

    void drawFadeMessage(Graphics2D g2) {
        g2.setComposite(AlphaComposite.SrcOver.derive(Math.max(0f, fadeMsgAlpha)));
        g2.setFont(new Font("SansSerif", Font.BOLD, 28));
        g2.setColor(new Color(255, 60, 60));

        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(fadeMsg)) / 2;
        int y = getHeight() - 70;
        g2.drawString(fadeMsg, x, y);

        g2.setComposite(AlphaComposite.SrcOver);
    }

    void drawEndOverlay(Graphics2D g2, String title) {
        g2.setComposite(AlphaComposite.SrcOver.derive(0.85f));
        g2.setColor(new Color(0,0,0));
        g2.fillRect(offsetX, offsetY, maze.w * cellSize, maze.h * cellSize);

        g2.setComposite(AlphaComposite.SrcOver.derive(1f));
        g2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 72));
        g2.setColor(new Color(200, 0, 0));

        FontMetrics fm = g2.getFontMetrics();
        int x = offsetX + (maze.w * cellSize - fm.stringWidth(title)) / 2;
        int y = offsetY + (maze.h * cellSize) / 2;
        g2.drawString(title, x, y);

        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.setColor(new Color(255, 170, 170));
        String hint = "Appuie sur r pour revenir au menu";
        int hx = offsetX + (maze.w * cellSize - g2.getFontMetrics().stringWidth(hint)) / 2;
        g2.drawString(hint, hx, y + 50);
    }
}

// ===================== ENTITY =====================
class Entity {
    int x, y;
    boolean alive = true;

    boolean isMinotaur;
    boolean isHumanControlled;

    Avatar avatar;

    // for runners
    int playerIndex = 0;
    boolean centerReached = false;
    boolean exitOpened = false;
    Point ownedExit = null;

    // movement accumulator
    double moveAccumulator = 0;

    // hit cooldown
    boolean hitCooldown = false;
    int hitCooldownMs = 0;

    // ✅ minotaur roaming
    Point roamTarget = null;
    int roamCooldownMs = 0;

    static Entity runner(int x, int y, Avatar avatar, boolean human) {
        Entity e = new Entity();
        e.x = x; e.y = y;
        e.isMinotaur = false;
        e.isHumanControlled = human;
        e.avatar = avatar;
        return e;
    }

    static Entity minotaur(int x, int y, boolean human) {
        Entity e = new Entity();
        e.x = x; e.y = y;
        e.isMinotaur = true;
        e.isHumanControlled = human;
        return e;
    }
}

// ===================== MAZE =====================
class Maze {
    final int w, h;

    // walls per cell
    boolean[][] wallN, wallS, wallE, wallW;

    // features
    Point center;
    Point exit = null;

    static class Door {
        int x1,y1,x2,y2;
        boolean closed = false;
        Door(int x1,int y1,int x2,int y2){this.x1=x1;this.y1=y1;this.x2=x2;this.y2=y2;}
        boolean matches(int ax,int ay,int bx,int by){
            return (x1==ax&&y1==ay&&x2==bx&&y2==by)||(x1==bx&&y1==by&&x2==ax&&y2==ay);
        }
    }
    java.util.List<Door> doors = new ArrayList<>();

    static class Chest {
        Point pos;
        int addMs;
        boolean taken=false;
        Chest(Point p,int addMs){this.pos=p;this.addMs=addMs;}
    }
    static class Portal {
        Point pos;
        boolean used=false;
        Portal(Point p){this.pos=p;}
    }
    Map<Point, Chest> chests = new HashMap<>();
    Map<Point, Portal> portals = new HashMap<>();

    Maze(int w, int h) {
        this.w = w; this.h = h;
        wallN = new boolean[h][w];
        wallS = new boolean[h][w];
        wallE = new boolean[h][w];
        wallW = new boolean[h][w];

        for (int y=0;y<h;y++) for(int x=0;x<w;x++){
            wallN[y][x]=true; wallS[y][x]=true; wallE[y][x]=true; wallW[y][x]=true;
        }
    }

    boolean in(int x,int y){ return x>=0 && y>=0 && x<w && y<h; }
    boolean walkable(int x,int y){ return in(x,y); }

    void generateLoopMaze() {
        Random rnd = new Random();

        boolean[][] vis = new boolean[h][w];
        int sx = 1 + rnd.nextInt(w-2);
        int sy = 1 + rnd.nextInt(h-2);

        java.util.List<int[]> frontier = new ArrayList<>();
        vis[sy][sx]=true;
        addFrontier(sx,sy,vis,frontier);

        while(!frontier.isEmpty()){
            int[] f = frontier.remove(rnd.nextInt(frontier.size()));
            int x=f[0], y=f[1], px=f[2], py=f[3];

            if(vis[y][x]) continue;
            removeWall(px,py,x,y);
            vis[y][x]=true;
            addFrontier(x,y,vis,frontier);
        }

        int extra = (w*h)/18;
        for (int k=0;k<extra;k++){
            int x=1+rnd.nextInt(w-2);
            int y=1+rnd.nextInt(h-2);
            int[][] dirs={{1,0},{-1,0},{0,1},{0,-1}};
            int[] d=dirs[rnd.nextInt(4)];
            int nx=x+d[0], ny=y+d[1];
            if(!in(nx,ny)) continue;
            if(rnd.nextDouble()<0.55){
                removeWall(x,y,nx,ny);
            }
        }

        for(int x=0;x<w;x++){
            wallN[0][x]=true; wallS[h-1][x]=true;
        }
        for(int y=0;y<h;y++){
            wallW[y][0]=true; wallE[y][w-1]=true;
        }
    }

    void addFrontier(int x,int y, boolean[][] vis, java.util.List<int[]> frontier){
        int[][] dirs={{1,0},{-1,0},{0,1},{0,-1}};
        for(int[] d:dirs){
            int nx=x+d[0], ny=y+d[1];
            if(!in(nx,ny)) continue;
            if(vis[ny][nx]) continue;
            frontier.add(new int[]{nx,ny,x,y});
        }
    }

    void removeWall(int x1,int y1,int x2,int y2){
        if(x2==x1+1 && y2==y1){ wallE[y1][x1]=false; wallW[y2][x2]=false; }
        if(x2==x1-1 && y2==y1){ wallW[y1][x1]=false; wallE[y2][x2]=false; }
        if(x2==x1 && y2==y1+1){ wallS[y1][x1]=false; wallN[y2][x2]=false; }
        if(x2==x1 && y2==y1-1){ wallN[y1][x1]=false; wallS[y2][x2]=false; }
    }

    void placeCenter(){
        center = new Point(w/2, h/2);
    }

    void spawnExitRandomCorner(){
        Random rnd = new Random();
        Point[] corners = new Point[] {
                new Point(1,1),
                new Point(w-2,1),
                new Point(1,h-2),
                new Point(w-2,h-2)
        };
        exit = corners[rnd.nextInt(4)];
    }

    void placeDoors(int count){
        doors.clear();
        Random rnd = new Random();

        java.util.List<Door> cand = new ArrayList<>();
        for(int y=1;y<h-1;y++){
            for(int x=1;x<w-1;x++){
                if(!wallE[y][x] && x+1<w-1) cand.add(new Door(x,y,x+1,y));
                if(!wallS[y][x] && y+1<h-1) cand.add(new Door(x,y,x,y+1));
            }
        }

        Collections.shuffle(cand, rnd);
        java.util.List<Door> chosen = new ArrayList<>();
        for(Door d : cand){
            if(chosen.size()>=count) break;
            boolean ok=true;
            for(Door o: chosen){
                if(Math.abs(o.x1-d.x1)+Math.abs(o.y1-d.y1) <= 1) { ok=false; break; }
                if(Math.abs(o.x2-d.x2)+Math.abs(o.y2-d.y2) <= 1) { ok=false; break; }
            }
            if(ok) chosen.add(d);
        }
        doors.addAll(chosen);
    }

    void placeChests(){
        chests.clear();
        Random rnd = new Random();
        int[] adds = new int[]{30_000, 45_000, 60_000};
        java.util.List<Integer> list = new ArrayList<>();
        for(int a: adds) list.add(a);
        Collections.shuffle(list, rnd);

        int placed=0;
        while(placed<3){
            Point p = randomWalkableCell();
            if(p.equals(center)) continue;
            if(exit!=null && p.equals(exit)) continue;
            if(chests.containsKey(p)) continue;
            chests.put(p, new Chest(p, list.get(placed)));
            placed++;
        }
    }

    void placePortals(){
        portals.clear();
        int placed=0;
        while(placed<2){
            Point p = randomWalkableCell();
            if(p.equals(center)) continue;
            if(chests.containsKey(p)) continue;
            if(portals.containsKey(p)) continue;
            portals.put(p, new Portal(p));
            placed++;
        }
    }

    Point randomWalkableCell(){
        Random rnd = new Random();
        int x=1+rnd.nextInt(w-2);
        int y=1+rnd.nextInt(h-2);
        return new Point(x,y);
    }

    boolean canMove(int x1,int y1,int x2,int y2){
        if(!in(x2,y2)) return false;
        for(Door d: doors){
            if(d.closed && d.matches(x1,y1,x2,y2)) return false;
        }
        if(x2==x1+1 && y2==y1) return !wallE[y1][x1];
        if(x2==x1-1 && y2==y1) return !wallW[y1][x1];
        if(x2==x1 && y2==y1+1) return !wallS[y1][x1];
        if(x2==x1 && y2==y1-1) return !wallN[y1][x1];
        return false;
    }

    int[][] bfsDistances(int tx,int ty){
        int[][] dist = new int[h][w];
        for(int y=0;y<h;y++) Arrays.fill(dist[y], -1);
        ArrayDeque<Point> q = new ArrayDeque<>();
        dist[ty][tx]=0;
        q.add(new Point(tx,ty));
        while(!q.isEmpty()){
            Point p=q.removeFirst();
            int d=dist[p.y][p.x];
            int[][] dirs={{1,0},{-1,0},{0,1},{0,-1}};
            for(int[] dd:dirs){
                int nx=p.x+dd[0], ny=p.y+dd[1];
                if(!in(nx,ny)) continue;
                if(dist[ny][nx]!=-1) continue;
                if(!canMove(p.x,p.y,nx,ny)) continue;
                dist[ny][nx]=d+1;
                q.add(new Point(nx,ny));
            }
        }
        return dist;
    }

    boolean hasPath(int sx,int sy,int tx,int ty){
        int[][] d = bfsDistances(tx,ty);
        return d[sy][sx] >= 0;
    }

    boolean hasLineOfSight(int x1,int y1,int x2,int y2){
        if(x1==x2){
            int dir = Integer.compare(y2,y1);
            int y=y1;
            while(y!=y2){
                int ny=y+dir;
                if(!canMove(x1,y,x1,ny)) return false;
                y=ny;
            }
            return true;
        }
        if(y1==y2){
            int dir = Integer.compare(x2,x1);
            int x=x1;
            while(x!=x2){
                int nx=x+dir;
                if(!canMove(x,y1,nx,y1)) return false;
                x=nx;
            }
            return true;
        }
        return false;
    }
}