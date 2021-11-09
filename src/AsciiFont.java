import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * Creates a .png and a glyph-info .txt from a true type font file with
 * Ascii characters from 32 (space) to 126 (~)
 *
 *
 * @author Frederik Dahl
 * 08/11/2021
 */

public class AsciiFont {
    
    private static final class Glyph {
        private final char c;
        private float x;
        private float y;
        private float advance;
        private float lsb;
        private float rsb;
        private float decent;
        private float ascent;
        Glyph(char c) {
            this.c = c;
        }
        
        @Override
        public String toString() {
            return c + " " + x + " " + y + " " + advance + " " + lsb + " " + rsb + " " + decent + " " + ascent;
        }
    }
    
    private String name;
    private float fontAscent;
    private float fontDescent;
    private float fontLeading;
    private float size;
    private final Glyph[] glyphs = new Glyph[95];
    private AsciiFont() {}
    
    public static void create(InputStream fontStream, String outFolder, int size, boolean antiAlias) throws Exception{
        Font font = Font.createFont(Font.TRUETYPE_FONT,fontStream).deriveFont(Font.PLAIN,size);
        AsciiFont asciiFont = new AsciiFont();
        BufferedImage image = new BufferedImage(1,1, TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setFont(font);
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        FontRenderContext fontRenderContext = fontMetrics.getFontRenderContext();
        GlyphMetrics glyphMetrics;
        LineMetrics lineMetrics;
        GlyphVector glyphVector;
        
        char[] characters = new char[95];
        for (char i = 32; i < 127; i++) characters[i - 32] = i;
        
        glyphVector = font.createGlyphVector(fontRenderContext,characters);
        lineMetrics = fontMetrics.getLineMetrics(characters,0, characters.length, graphics2D);
        
        for (char i = 0; i < characters.length; i++) {
            Glyph glyph = new Glyph((char) (i+32));
            glyphMetrics = glyphVector.getGlyphMetrics(i);
            glyph.advance = glyphMetrics.getAdvance();
            glyph.lsb = glyphMetrics.getLSB();
            glyph.rsb = glyphMetrics.getRSB();
            glyph.decent = lineMetrics.getDescent();
            glyph.ascent = lineMetrics.getAscent();
            asciiFont.glyphs[i] = glyph;
        }
        asciiFont.name = font.getName().replaceAll("\\s+","");
        asciiFont.size = font.getSize();
        asciiFont.fontAscent = lineMetrics.getAscent();
        asciiFont.fontDescent = lineMetrics.getDescent();
        asciiFont.fontLeading = lineMetrics.getLeading();
        graphics2D.dispose();
        final int lineHeight = (int) Math.ceil(asciiFont.fontAscent + asciiFont.fontDescent);
        final int estimatedWidth = 8 * lineHeight;
        int imageHeight = lineHeight;
        int imageWidth = 0;
        float y = lineHeight;
        float x = 0;
        Glyph[] glyphs = asciiFont.glyphs;
        
        for (Glyph glyph : glyphs) {
            glyph.x = x; glyph.y = y;
            imageWidth = Math.max((int)(x + glyph.advance), imageWidth);
            x += glyph.advance;
            if (x >= estimatedWidth) {
                x = 0; y += lineHeight;
                imageHeight += lineHeight;
            }
        }
        image = new BufferedImage(imageWidth,imageHeight,TYPE_INT_ARGB);
        graphics2D = image.createGraphics();
        if (antiAlias) graphics2D.setRenderingHint(KEY_ANTIALIASING,VALUE_ANTIALIAS_ON);
        graphics2D.setFont(font);
        graphics2D.setColor(Color.WHITE);
        List<String> lines = new ArrayList<>(1+glyphs.length);
        lines.add(asciiFont.toString());
        
        for (Glyph glyph : glyphs) {
            graphics2D.drawString(String.valueOf(glyph.c),glyph.x,glyph.y);
            lines.add(glyph.toString());
        }
        graphics2D.dispose();
        File file = new File( outFolder + File.separator + asciiFont.name + ".png");
        ImageIO.write(image,"png",file);
        Path path = Paths.get(outFolder + File.separator + asciiFont.name + ".txt");
        Files.write(path,lines,US_ASCII,CREATE);
    }
    
    @Override
    public String toString() {
        return name + " " + size + " " + fontAscent + " " + fontDescent + " " + fontLeading;
    }
}
