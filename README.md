Creates a .png and a glyph-info .txt from a true type font file with 
Ascii characters from 32 (space) to 126 (~)


1. Open the commandline in the folder containing the .jar
2. Type: [java -jar Fonts.jar] and press ENTER
3. Enter the name of the true type font: "some_font.ttf" (relative to the .jar folder)
4. Enter the font-size
5. Enter [y/n] for antialiasing
6. A .png and .txt file will be created in the Fonts.jar folder



The .txt file contains all information required to draw a bitmap font.

The first line of text:

font_name + " " + size + " " + maxAscent + " " + maxDescent + " " + font_leading

followed by glyph data:

character + " " + x0 + " " + y0 + " " + advance + " " + lsb + " " + rsb + " " + decent + " " + ascent


You can think of each glyph as a "block" (think type-writer)
where the origin of the block is (x0,y0) (relative to its placement on the .png).
(Note: The y-origin of the .png is in the top-left corner)
If you draw a string of text, the origin is like the pointer between each char,
separated by the characters advance (the width of the block).
All blocks share the height (fonts' maxAscent + maxDecent)
The fonts' "leading" is the recommended distance between lines of text.
The actual character width starts at x0 + lsb and ends at advance - rsb.
the actual character height starts at -y0 + maxAscent + char decent.
