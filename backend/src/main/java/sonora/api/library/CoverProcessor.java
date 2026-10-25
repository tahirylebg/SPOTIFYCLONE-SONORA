package sonora.api.library;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Prépare les pochettes pour les clients : retire les bandes noires (miniatures YouTube),
 * recadre en carré au centre et redimensionne. Les clients n'ont plus qu'à afficher l'image.
 */
public final class CoverProcessor {

    private static final int SIZE = 600;
    private static final float DARK_THRESHOLD = 0.08f;

    private CoverProcessor() {
    }

    /** Renvoie un JPEG carré, ou null si l'image est illisible. */
    public static byte[] process(byte[] artwork) {
        try {
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(artwork));
            if (source == null) {
                return null;
            }
            int[] zone = usefulArea(source);
            int side = Math.min(zone[2], zone[3]);
            int x = zone[0] + (zone[2] - side) / 2;
            int y = zone[1] + (zone[3] - side) / 2;

            int target = Math.min(SIZE, side);
            BufferedImage out = new BufferedImage(target, target, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = out.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(source, 0, 0, target, target, x, y, x + side, y + side, null);
            g.dispose();

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ImageIO.write(out, "jpg", bytes);
            return bytes.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    /** {x, y, largeur, hauteur} de l'image sans ses bords sombres ; l'image entière si elle est sombre partout. */
    static int[] usefulArea(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int top = 0, bottom = h - 1, left = 0, right = w - 1;
        while (top < bottom && darkRow(img, top, 0, w - 1)) top++;
        while (bottom > top && darkRow(img, bottom, 0, w - 1)) bottom--;
        while (left < right && darkColumn(img, left, top, bottom)) left++;
        while (right > left && darkColumn(img, right, top, bottom)) right--;

        int width = right - left + 1;
        int height = bottom - top + 1;
        if (width < w / 2 || height < h / 2) {
            return new int[] { 0, 0, w, h };
        }
        return new int[] { left, top, width, height };
    }

    private static boolean darkRow(BufferedImage img, int y, int from, int to) {
        int step = Math.max(1, (to - from) / 32);
        for (int x = from; x <= to; x += step) {
            if (brightness(img.getRGB(x, y)) > DARK_THRESHOLD) {
                return false;
            }
        }
        return true;
    }

    private static boolean darkColumn(BufferedImage img, int x, int from, int to) {
        int step = Math.max(1, (to - from) / 32);
        for (int y = from; y <= to; y += step) {
            if (brightness(img.getRGB(x, y)) > DARK_THRESHOLD) {
                return false;
            }
        }
        return true;
    }

    private static float brightness(int rgb) {
        int r = (rgb >> 16) & 0xff, g = (rgb >> 8) & 0xff, b = rgb & 0xff;
        return Math.max(r, Math.max(g, b)) / 255f;
    }
}
