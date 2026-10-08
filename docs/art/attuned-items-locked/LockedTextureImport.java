import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Atlas packaging only: every approved pixel is copied unchanged, with transparent padding. */
public final class LockedTextureImport {
    public static void main(String[] args) throws Exception {
        boolean check = args[0].equals("--check");
        for (int i = 1; i < args.length; i += 2) {
            Path sourcePath = Path.of(args[i]);
            Path targetPath = Path.of(args[i + 1]);
            BufferedImage source = ImageIO.read(sourcePath.toFile());
            int size = 1;
            while (size < Math.max(source.getWidth(), source.getHeight())) size *= 2;
            BufferedImage packaged = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = source.getRGB(0, 0, source.getWidth(), source.getHeight(), null, 0, source.getWidth());
            packaged.setRGB(0, 0, source.getWidth(), source.getHeight(), pixels, 0, source.getWidth());
            if (!check) {
                Files.createDirectories(targetPath.getParent());
                ImageIO.write(packaged, "PNG", targetPath.toFile());
            }
            BufferedImage actual = ImageIO.read(targetPath.toFile());
            if (actual.getWidth() != size || actual.getHeight() != size) throw new IllegalStateException("Canvas drift: " + targetPath);
            for (int y = 0; y < size; y++) for (int x = 0; x < size; x++) {
                int expected = x < source.getWidth() && y < source.getHeight() ? source.getRGB(x, y) : 0;
                if (actual.getRGB(x, y) != expected) throw new IllegalStateException("Approved pixel changed: " + targetPath + " at " + x + "," + y);
            }
            System.out.println(targetPath.getFileName() + ": every approved pixel/alpha preserved; " + size + "×" + size + " atlas canvas.");
        }
    }
}
