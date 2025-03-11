package org.bukkit.craftbukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextWrapper {
    private static final int[] characterWidths = new int[] {
        1, 9, 9, 8, 8, 8, 8, 7, 9, 8, 9, 9, 8, 9, 9, 9,
        8, 8, 8, 8, 9, 9, 8, 9, 8, 8, 8, 8, 8, 9, 9, 9,
        4, 2, 5, 6, 6, 6, 6, 3, 5, 5, 5, 6, 2, 6, 2, 6,
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 5, 6, 5, 6,
        7, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 6, 6, 6, 6,
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 4, 6, 6,
        3, 6, 6, 6, 6, 6, 5, 6, 6, 2, 6, 5, 3, 6, 6, 6,
        6, 6, 6, 6, 4, 6, 6, 6, 6, 6, 6, 5, 2, 5, 7, 6,
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 3, 6, 6,
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6,
        6, 3, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 6, 2, 6, 6,
        8, 9, 9, 6, 6, 6, 8, 8, 6, 8, 8, 8, 8, 8, 6, 6,
        9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
        9, 9, 9, 9, 9, 9, 9, 9, 9, 6, 9, 9, 9, 5, 9, 9,
        8, 7, 7, 8, 7, 8, 8, 8, 7, 8, 8, 7, 9, 9, 6, 7,
        7, 7, 7, 7, 9, 6, 7, 8, 7, 6, 6, 9, 7, 6, 7, 1
    };
    public static final char COLOR_CHAR = '\u00A7';
    private static final char TEMP_CHAR = '\u03d5';
    public static final int CHAT_WINDOW_WIDTH = 320;
    public static final int CHAT_STRING_LENGTH = 119;
    public static final String allowedChars = net.minecraft.server.FontAllowedCharacters.allowedCharacters;

    public static String[] wrapText(final String text) {
        final StringBuilder out = new StringBuilder();
        int lineLength = 0;
        int lineWidth = 0;
        int tokenWidth;
        int tokenLength;
        // tokens are just individual words in the message
        String[] tokens = text.split(" ");
        Pattern p = Pattern.compile("(" + COLOR_CHAR + "[0-9a-fA-F])+");
        Pattern p2 = Pattern.compile(".*(" + COLOR_CHAR + "[0-9A-Fa-f])+.*$"); // to get just the last occurrence of color format in token
        String temp;

        String lastColorChar = COLOR_CHAR + "f";
        
        for (int i = 0; i < tokens.length; i++) {
            // append space to token unless it is last token
            if (i == tokens.length - 1) {
                temp = tokens[i];
            } else {
                temp = tokens[i] + " ";
            }

            boolean containsColorCodes;

            tokenLength = temp.length();
            lineLength += tokenLength;
            tokenWidth = widthInPixels(temp);
            lineWidth += tokenWidth;
            Matcher m = p2.matcher(tokens[i]);

            if (m.matches()) {
                // need to update the color to persist over new lines
                lastColorChar = m.group(1);
            }

            if (tokenLength >= CHAT_STRING_LENGTH || tokenWidth >= CHAT_WINDOW_WIDTH) {
                // this token is too big for one line so split it

                // reset the line accumulators since we will be taking up the whole line
                lineWidth = 0;
                lineLength = 0;

                for (int j = 0; j < temp.length(); j++) {
                    // color format checking
                    if (j < temp.length() - 1 && p.matcher(temp.substring(j, j + 1)).matches()) {
                        // we have a color format, so increment length by 2 but this will not contribute to width
                        lineLength += 2;
                        containsColorCodes = true;
                    } else {
                        // no color format, so normal increments
                        lineWidth += characterWidths[temp.charAt(j)];
                        lineLength++;
                        containsColorCodes = false;
                    }

                    if (lineLength >= CHAT_STRING_LENGTH || lineWidth >= CHAT_WINDOW_WIDTH) {
                        out.append(TEMP_CHAR);
                        out.append(lastColorChar);
                        lineLength = 2;
                        lineWidth = 0;

                        if (containsColorCodes) {
                            // we need to move both the color char and the color specifier to the next line
                            j -= 2;
                        } else {
                            // not a color format, so just move one char to the next line
                            j--;
                        }
                        continue;
                    }
                    out.append(temp.charAt(j));
                }
                continue;
            }
            // we have a smaller token than the max size so wrap based on token instead of char
            if (lineLength >= CHAT_STRING_LENGTH || lineWidth >= CHAT_WINDOW_WIDTH) {
                // try again on a new line
                out.append(TEMP_CHAR);
                out.append(lastColorChar);
                lineWidth = 2;
                lineLength = 0;
                i--;
                continue;
            }
            out.append(temp);
        }

        return out.toString().split(String.valueOf(TEMP_CHAR));
    }

    /**
     * Calculates the width of a string in pixels based on Minecraft's character widths.
     * The maximum width for chat is 320 pixels (Use CHAT_WINDOW_WIDTH).
     *
     * @param string The input string.
     * @return The width of the string in pixels.
     */
    public static int widthInPixels(final String text) {
        if (text == null || text.isEmpty())
            return 0;

        int output = 0;

        // literally yoinked from above and removed unnecessary components.
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            if (ch == COLOR_CHAR && i < text.length() - 1) {
                i++;
                continue;
            }

            int index = allowedChars.indexOf(ch);
            if (index == -1)
                continue;

            index += 32; // compensate for gap in allowed characters

            output += characterWidths[index];
        }

        return output;
    }
}
