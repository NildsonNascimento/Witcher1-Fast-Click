import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * Painel com imagem de fundo no modo "cover" (preenche sem distorcer),
 * com degradês nas bordas para integrar a arte ao painel de controles.
 */
public class ArtPanel extends JPanel {

    private final Image image;

    public ArtPanel(Image image) {
        this.image = image;
        setBackground(FastClickUI.BG);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();
        int iw = image.getWidth(null);
        int ih = image.getHeight(null);

        if (iw > 0 && ih > 0) {
            double scale = Math.max((double) w / iw, (double) h / ih);
            int dw = (int) Math.round(iw * scale);
            int dh = (int) Math.round(ih * scale);
            // centraliza horizontalmente e puxa levemente para o topo (rosto/figura)
            int dx = (w - dw) / 2;
            int dy = (int) ((h - dh) * 0.35);
            g.drawImage(image, dx, dy, dw, dh, null);
        }

        // Degradê esquerdo (funde com o painel de controles)
        g.setPaint(new GradientPaint(0, 0, new Color(FastClickUI.BG.getRed(), FastClickUI.BG.getGreen(), FastClickUI.BG.getBlue(), 255),
                140, 0, new Color(FastClickUI.BG.getRed(), FastClickUI.BG.getGreen(), FastClickUI.BG.getBlue(), 0)));
        g.fillRect(0, 0, 140, h);

        // Vinheta inferior
        g.setPaint(new GradientPaint(0, h - 160, new Color(0, 0, 0, 0), 0, h, new Color(0, 0, 0, 190)));
        g.fillRect(0, h - 160, w, 160);

        // Vinheta superior suave
        g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 110), 0, 90, new Color(0, 0, 0, 0)));
        g.fillRect(0, 0, w, 90);

        g.dispose();
    }
}
