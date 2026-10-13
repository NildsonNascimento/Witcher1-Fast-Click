import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.github.kwhat.jnativehook.*;
import com.github.kwhat.jnativehook.mouse.*;
import com.github.kwhat.jnativehook.keyboard.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Witcher1FastClick implements NativeMouseWheelListener, NativeKeyListener, NativeMouseListener {

    private Robot robot;
    private volatile boolean enabled = false;

    private JLabel statusLabel;
    private JButton toggleButton;
    private JCheckBox inventoryCheckBox;
    private FastClickUI.StatusDot statusDot;

    public volatile boolean inventoryEnabled = false;

    private volatile int delayMs = 50;
    private volatile int turboClicks = 5;

    private volatile boolean mouse5Held = false;

    private Map<String, Runnable> hotkeys = new HashMap<>();

    public Witcher1FastClick(JLabel label, JButton button) {
        this.statusLabel = label;
        this.toggleButton = button;

        try {
            robot = new Robot();
        } catch (Exception e) {
            e.printStackTrace();
        }

        registerDefaultHotkeys();
    }

    private void registerDefaultHotkeys() {
        // F6 liga/desliga
        hotkeys.put("F6", this::toggleMacro);
        // F7 modo inventário
        hotkeys.put("F7", this::toggleInventory);
        // Mouse4 (segurar)
        hotkeys.put("MOUSE4", this::startMouse5Loop);
    }

    private void toggleInventory() {
        inventoryEnabled = !inventoryEnabled;
        SwingUtilities.invokeLater(() -> inventoryCheckBox.setSelected(inventoryEnabled));
    }

    private void toggleMacro() {
        enabled = !enabled;
        SwingUtilities.invokeLater(this::refreshStatus);
    }

    /** Atualiza os elementos visuais conforme o estado atual do macro (roda na thread da FastClickUI). */
    private void refreshStatus() {
        statusLabel.setText(enabled ? "ATIVADO" : "DESATIVADO");
        statusLabel.setForeground(enabled ? FastClickUI.GREEN : FastClickUI.RED);
        statusDot.setActive(enabled);
        toggleButton.setText(enabled ? "DESLIGAR" : "LIGAR");
        ((FastClickUI.FlatButton) toggleButton).setAccent(enabled ? FastClickUI.RED : FastClickUI.GOLD);
    }

    // Rolagem do scroll do mouse
    @Override
    public void nativeMouseWheelMoved(NativeMouseWheelEvent e) {
        if (!enabled) return;
        // clickLeftMouse();
    }

    // Armazenar config em arquivo
    private static final String CONFIG_FILE = "config.properties";
    private Properties config = new Properties();

    private void loadConfig() {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            config.load(fis);
            delayMs = Integer.parseInt(config.getProperty("delay", "50"));
            turboClicks = Integer.parseInt(config.getProperty("turbo", "5"));
        } catch (Exception e) {
            // Primeira execução
            delayMs = 50;
            turboClicks = 5;
        }
    }

    private void saveConfig() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            config.setProperty("delay", String.valueOf(delayMs));
            config.setProperty("turbo", String.valueOf(turboClicks));
            config.setProperty("inventory", String.valueOf(inventoryEnabled));
            config.store(fos, "Nildo Witcher 1 Fast Click Config");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        if (!enabled) return;

        if (e.getButton() == NativeMouseEvent.BUTTON4) {
            mouse5Held = true;

            Runnable action = hotkeys.get("MOUSE4");
            if (action != null) action.run();
        }
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent e) {
        if (e.getButton() == NativeMouseEvent.BUTTON4) {
            mouse5Held = false;
        }
    }

    private void startMouse5Loop() {
        Thread t = new Thread(() -> {
            while (mouse5Held && enabled) {

                if (inventoryEnabled) {
                    robot.keyPress(KeyEvent.VK_I);
                    try {
                        Thread.sleep(delayMs); // segura a tecla pelo tempo do delay
                    } catch (InterruptedException ignored) {}
                    robot.keyRelease(KeyEvent.VK_I);
                }

                clickLeftMouse();

                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ignored) {}
            }
        });

        t.setDaemon(true);
        t.start();
    }

    private void clickLeftMouse() {
        for (int i = 0; i < turboClicks; i++) {
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        String key = NativeKeyEvent.getKeyText(e.getKeyCode());
        Runnable action = hotkeys.get(key);
        if (action != null) {
            action.run();
        }
    }

    @Override public void nativeKeyReleased(NativeKeyEvent e) {}
    @Override public void nativeKeyTyped(NativeKeyEvent e) {}

    // =========================
    // INTERFACE
    // =========================
    private static JPanel hotkeyRow(String key, String desc) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel chipWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        chipWrap.setOpaque(false);
        chipWrap.setPreferredSize(new Dimension(76, 24));
        chipWrap.add(new FastClickUI.KeyChip(key));
        row.add(chipWrap, BorderLayout.WEST);
        row.add(FastClickUI.label(desc, Font.PLAIN, 12.5f, FastClickUI.TEXT), BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return row;
    }

    private static JPanel settingRow(String title, String hint, JComponent control) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel t = FastClickUI.label(title, Font.BOLD, 13f, FastClickUI.TEXT);
        JLabel h = FastClickUI.label(hint, Font.PLAIN, 11f, FastClickUI.MUTED);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(t);
        text.add(h);

        row.add(text, BorderLayout.CENTER);
        JPanel east = new JPanel(new GridBagLayout());
        east.setOpaque(false);
        east.add(control);
        row.add(east, BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return row;
    }

    private static JLabel sectionTitle(String text) {
        JLabel l = FastClickUI.label(text, Font.BOLD, 11f, FastClickUI.GOLD);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        // espaçamento entre letras
        l.setFont(l.getFont().deriveFont(java.util.Collections.singletonMap(
                java.awt.font.TextAttribute.TRACKING, 0.12f)));
        return l;
    }

    public static void main(String[] args) {

        try {
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF);
            logger.setUseParentHandlers(false);

            GlobalScreen.registerNativeHook();

            JFrame frame = new JFrame("Nildo Witcher 1 Fast Click  |  Enhanced Edition");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.setResizable(false);
            frame.getContentPane().setBackground(FastClickUI.BG);

            // ---------- Painel esquerdo ----------
            JPanel leftPanel = new JPanel();
            leftPanel.setBackground(FastClickUI.BG);
            leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
            leftPanel.setBorder(new EmptyBorder(26, 26, 22, 22));
            leftPanel.setPreferredSize(new Dimension(390, 0));

            // Cabeçalho
            JLabel eyebrow = sectionTitle("THE WITCHER  ·  ENHANCED EDITION");
            JLabel title = FastClickUI.label("Witcher 1 Fast Click", Font.BOLD, 26f, FastClickUI.TEXT);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel subtitle = FastClickUI.label("Cliques rápidos e modo inventário", Font.PLAIN, 12.5f, FastClickUI.MUTED);
            subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

            leftPanel.add(eyebrow);
            leftPanel.add(Box.createVerticalStrut(4));
            leftPanel.add(title);
            leftPanel.add(Box.createVerticalStrut(2));
            leftPanel.add(subtitle);
            leftPanel.add(Box.createVerticalStrut(18));

            // Cartão de status
            FastClickUI.Card statusCard = new FastClickUI.Card(new BorderLayout(12, 0));
            statusCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            statusCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

            FastClickUI.StatusDot dot = new FastClickUI.StatusDot();
            JLabel statusCaption = FastClickUI.label("STATUS DO MACRO", Font.BOLD, 10.5f, FastClickUI.MUTED);
            JLabel statusLabel = FastClickUI.label("DESATIVADO", Font.BOLD, 18f, FastClickUI.RED);

            JPanel statusText = new JPanel();
            statusText.setOpaque(false);
            statusText.setLayout(new BoxLayout(statusText, BoxLayout.Y_AXIS));
            statusCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
            statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            statusText.add(statusCaption);
            statusText.add(statusLabel);

            JPanel dotWrap = new JPanel(new GridBagLayout());
            dotWrap.setOpaque(false);
            dotWrap.add(dot);

            FastClickUI.FlatButton button = new FastClickUI.FlatButton("LIGAR", true);
            button.setPreferredSize(new Dimension(120, 38));
            JPanel btnWrap = new JPanel(new GridBagLayout());
            btnWrap.setOpaque(false);
            btnWrap.add(button);

            statusCard.add(dotWrap, BorderLayout.WEST);
            statusCard.add(statusText, BorderLayout.CENTER);
            statusCard.add(btnWrap, BorderLayout.EAST);

            leftPanel.add(statusCard);
            leftPanel.add(Box.createVerticalStrut(12));

            // Aviso 60 FPS
            FastClickUI.Card fpsCard = new FastClickUI.Card(new BorderLayout(10, 0));
            fpsCard.setColors(new Color(201, 162, 39, 24), new Color(201, 162, 39, 120));
            fpsCard.setBorder(new EmptyBorder(10, 14, 10, 14));
            fpsCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            fpsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
            JLabel fpsIcon = FastClickUI.label("!", Font.BOLD, 15f, FastClickUI.GOLD);
            fpsIcon.setHorizontalAlignment(SwingConstants.CENTER);
            fpsIcon.setPreferredSize(new Dimension(22, 22));
            JLabel fpsText = new JLabel("<html>O jogo precisa estar travado em <b><font color='#C9A227'>60 FPS</font></b><br>para o efeito funcionar melhor.</html>");
            fpsText.setFont(FastClickUI.font(Font.PLAIN, 12f));
            fpsText.setForeground(FastClickUI.TEXT);
            fpsCard.add(fpsIcon, BorderLayout.WEST);
            fpsCard.add(fpsText, BorderLayout.CENTER);

            leftPanel.add(fpsCard);
            leftPanel.add(Box.createVerticalStrut(18));

            // Atalhos
            leftPanel.add(sectionTitle("ATALHOS"));
            leftPanel.add(Box.createVerticalStrut(8));
            FastClickUI.Card keysCard = new FastClickUI.Card(null);
            keysCard.setLayout(new BoxLayout(keysCard, BoxLayout.Y_AXIS));
            keysCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            keysCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));
            keysCard.add(hotkeyRow("F6", "Ligar / desligar o macro"));
            keysCard.add(Box.createVerticalStrut(7));
            keysCard.add(hotkeyRow("MOUSE4", "Segurar para executar"));
            keysCard.add(Box.createVerticalStrut(7));
            keysCard.add(hotkeyRow("F7", "Ativar / desativar modo inventário"));
            leftPanel.add(keysCard);
            leftPanel.add(Box.createVerticalStrut(18));

            // Configurações
            leftPanel.add(sectionTitle("CONFIGURAÇÕES"));
            leftPanel.add(Box.createVerticalStrut(8));

            JTextField delayField = FastClickUI.field("50");
            delayField.setPreferredSize(new Dimension(76, 30));
            JTextField turboField = FastClickUI.field("5");
            turboField.setPreferredSize(new Dimension(76, 30));

            Witcher1FastClick macro = new Witcher1FastClick(statusLabel, button);
            macro.statusDot = dot;

            macro.inventoryCheckBox = new FastClickUI.SwitchToggle();

            FastClickUI.Card settingsCard = new FastClickUI.Card(null);
            settingsCard.setLayout(new BoxLayout(settingsCard, BoxLayout.Y_AXIS));
            settingsCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            settingsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 232));
            settingsCard.add(settingRow("Delay (ms)", "Intervalo entre cada ciclo", delayField));
            settingsCard.add(Box.createVerticalStrut(10));
            settingsCard.add(settingRow("Turbo", "Cliques por ciclo", turboField));
            settingsCard.add(Box.createVerticalStrut(10));
            settingsCard.add(settingRow("Modo Inventário (F7)", "Alterna a tecla I a cada ciclo", macro.inventoryCheckBox));
            settingsCard.add(Box.createVerticalStrut(14));

            FastClickUI.FlatButton applyButton = new FastClickUI.FlatButton("APLICAR E SALVAR", false);
            applyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            applyButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            settingsCard.add(applyButton);

            JLabel feedback = FastClickUI.label(" ", Font.PLAIN, 11.5f, FastClickUI.MUTED);
            feedback.setAlignmentX(Component.LEFT_ALIGNMENT);
            settingsCard.add(Box.createVerticalStrut(6));
            settingsCard.add(feedback);

            leftPanel.add(settingsCard);
            leftPanel.add(Box.createVerticalGlue());

            // ---------- Painel direito (imagem) ----------
            Image img = ImageIO.read(Witcher1FastClick.class.getResource("img/Striga.jpg"));
            JPanel rightPanel = new ArtPanel(img);
            rightPanel.setLayout(new BorderLayout());

            // ---------- Lógica da interface ----------
            macro.inventoryCheckBox.addActionListener(e ->
                    macro.inventoryEnabled = macro.inventoryCheckBox.isSelected());

            // Carrega o arquivo config.properties
            macro.loadConfig();

            // Atualiza a interface com os valores salvos
            delayField.setText(String.valueOf(macro.delayMs));
            turboField.setText(String.valueOf(macro.turboClicks));

            button.addActionListener(e -> macro.toggleMacro());

            javax.swing.Timer feedbackTimer = new javax.swing.Timer(2500, e -> feedback.setText(" "));
            feedbackTimer.setRepeats(false);

            applyButton.addActionListener(e -> {
                try {
                    int d = Integer.parseInt(delayField.getText().trim());
                    int t = Integer.parseInt(turboField.getText().trim());
                    if (d < 1 || t < 1) throw new NumberFormatException();

                    macro.delayMs = d;
                    macro.turboClicks = t;
                    macro.saveConfig();

                    feedback.setForeground(FastClickUI.GREEN);
                    feedback.setText("Configurações aplicadas e salvas.");
                } catch (Exception ex) {
                    feedback.setForeground(FastClickUI.RED);
                    feedback.setText("Valores inválidos: use números inteiros maiores que 0.");
                }
                feedbackTimer.restart();
            });

            GlobalScreen.addNativeMouseWheelListener(macro);
            GlobalScreen.addNativeMouseListener(macro);
            GlobalScreen.addNativeKeyListener(macro);

            frame.add(leftPanel, BorderLayout.WEST);
            frame.add(rightPanel, BorderLayout.CENTER);

            frame.setSize(1000, 690);
            frame.setLocationRelativeTo(null);

            // Salva automaticamente ao fechar
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    macro.saveConfig();
                }
            });

            frame.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
