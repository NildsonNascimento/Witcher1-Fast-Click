import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/** Tema e componentes visuais customizados (somente aparência, sem lógica do macro). */
public final class FastClickUI {

    private FastClickUI() {}

    // ---------- Paleta ----------
    public static final Color BG        = new Color(16, 16, 18);
    public static final Color CARD      = new Color(27, 27, 31);
    public static final Color CARD_LINE = new Color(48, 48, 54);
    public static final Color GOLD      = new Color(201, 162, 39);
    public static final Color GOLD_DARK = new Color(160, 128, 28);
    public static final Color TEXT      = new Color(232, 232, 235);
    public static final Color MUTED     = new Color(150, 150, 158);
    public static final Color GREEN     = new Color(72, 187, 120);
    public static final Color RED       = new Color(222, 80, 80);

    // ---------- Fontes ----------
    public static Font font(int style, float size) {
        return new Font("Segoe FastClickUI", style, 12).deriveFont(style, size);
    }

    public static void antialias(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    public static JLabel label(String text, int style, float size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(style, size));
        l.setForeground(color);
        return l;
    }

    // ---------- Cartão arredondado ----------
    public static class Card extends JPanel {
        private Color border = CARD_LINE;
        private Color fill = CARD;

        public Card(LayoutManager lm) {
            super(lm);
            setOpaque(false);
            setBorder(new EmptyBorder(14, 16, 14, 16));
        }

        public void setColors(Color fill, Color border) {
            this.fill = fill;
            this.border = border;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            antialias(g);
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g.setColor(border);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g.dispose();
            super.paintComponent(g0);
        }
    }

    // ---------- Indicador de status (bolinha) ----------
    public static class StatusDot extends JComponent {
        private boolean active;

        public StatusDot() {
            setPreferredSize(new Dimension(16, 16));
            setMinimumSize(new Dimension(16, 16));
        }

        public void setActive(boolean a) {
            this.active = a;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            antialias(g);
            Color c = active ? GREEN : RED;
            int cx = getWidth() / 2, cy = getHeight() / 2;
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 55));
            g.fillOval(cx - 8, cy - 8, 16, 16);
            g.setColor(c);
            g.fillOval(cx - 4, cy - 4, 8, 8);
            g.dispose();
        }
    }

    // ---------- Botão plano ----------
    public static class FlatButton extends JButton {
        private Color accent = GOLD;
        private boolean filled;
        private boolean hover;

        public FlatButton(String text, boolean filled) {
            super(text);
            this.filled = filled;
            setFont(font(Font.BOLD, 13f));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(9, 18, 9, 18));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            });
        }

        public void setAccent(Color c) {
            this.accent = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            antialias(g);
            int w = getWidth(), h = getHeight();
            boolean pressed = getModel().isPressed();

            if (filled) {
                Color base = pressed ? accent.darker() : (hover ? accent.brighter() : accent);
                g.setColor(base);
                g.fillRoundRect(0, 0, w, h, 10, 10);
                g.setColor(new Color(20, 20, 20));
            } else {
                if (hover || pressed) {
                    g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), pressed ? 60 : 35));
                    g.fillRoundRect(0, 0, w, h, 10, 10);
                }
                g.setColor(accent);
                g.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
            }

            FontMetrics fm = g.getFontMetrics(getFont());
            g.setFont(getFont());
            String t = getText();
            g.drawString(t, (w - fm.stringWidth(t)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    // ---------- Interruptor (switch) ----------
    public static class SwitchToggle extends JCheckBox {
        public SwitchToggle() {
            setOpaque(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(44, 24));
            setMinimumSize(new Dimension(44, 24));
            setMaximumSize(new Dimension(44, 24));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            antialias(g);
            int w = 44, h = 22;
            int y = (getHeight() - h) / 2;
            boolean on = isSelected();

            g.setColor(on ? GOLD : new Color(60, 60, 66));
            g.fillRoundRect(0, y, w, h, h, h);
            g.setColor(on ? new Color(25, 25, 25) : new Color(190, 190, 196));
            int knob = h - 6;
            int kx = on ? w - knob - 3 : 3;
            g.fillOval(kx, y + 3, knob, knob);
            g.dispose();
        }
    }

    // ---------- Chip de tecla ----------
    public static class KeyChip extends JComponent {
        private final String text;

        public KeyChip(String text) {
            this.text = text;
            setFont(font(Font.BOLD, 11f));
            FontMetrics fm = getFontMetrics(getFont());
            Dimension d = new Dimension(Math.max(44, fm.stringWidth(text) + 20), 24);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            antialias(g);
            int w = getWidth(), h = getHeight();
            g.setColor(new Color(40, 40, 46));
            g.fillRoundRect(0, 0, w - 1, h - 1, 8, 8);
            g.setColor(new Color(201, 162, 39, 140));
            g.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
            g.setFont(getFont());
            g.setColor(GOLD);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, (w - fm.stringWidth(text)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    // ---------- Campo de texto ----------
    public static JTextField field(String text) {
        JTextField f = new JTextField(text);
        f.setFont(font(Font.PLAIN, 13f));
        f.setForeground(TEXT);
        f.setCaretColor(GOLD);
        f.setBackground(new Color(20, 20, 23));
        f.setHorizontalAlignment(SwingConstants.CENTER);
        f.setSelectionColor(new Color(201, 162, 39, 120));
        f.setSelectedTextColor(Color.WHITE);
        final CompoundBorder normal = new CompoundBorder(new LineBorder(CARD_LINE, 1, true), new EmptyBorder(5, 8, 5, 8));
        final CompoundBorder focus  = new CompoundBorder(new LineBorder(GOLD, 1, true), new EmptyBorder(5, 8, 5, 8));
        f.setBorder(normal);
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { f.setBorder(focus); }
            @Override public void focusLost(FocusEvent e)   { f.setBorder(normal); }
        });
        return f;
    }
}
