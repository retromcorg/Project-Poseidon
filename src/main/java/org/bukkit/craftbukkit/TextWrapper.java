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
    public static final char COLOR_CHAR = '\u00A7'; // Poseidon - private -> public
    public static final Pattern COLOR_PATTERN = Pattern.compile(COLOR_CHAR + "[0-9a-fA-F]"); // Poseidon - Text wrap rework
    private static final char TEMP_CHAR = '\u03d5'; // Poseidon - Text wrap rework
    public static final int CHAT_WINDOW_WIDTH = 320; // Poseidon - private -> public
    public static final int CHAT_STRING_LENGTH = 119; // Poseidon - private -> public
    public static final String allowedChars = net.minecraft.server.FontAllowedCharacters.allowedCharacters; // Poseidon - private -> public

    public static String[] wrapText(final String input) {
        String text = sanitizeText(input);

        final StringBuilder out = new StringBuilder();
        int lineLength = 0;
        int lineWidth = 0;
        int tokenWidth;
        int tokenLength;
        // tokens are just individual words in the message
        String[] tokens = text.split(" ");
        String temp;

        String lastColorChar = COLOR_CHAR + "f";
        
        for (int i = 0; i < tokens.length; i++) {
            // append space to token unless it is last token
            if (i == tokens.length - 1) {
                temp = tokens[i];
            } else {
                temp = tokens[i] + " ";
            }

            // getting the last color char in the token to persist to new line
            Matcher m = COLOR_PATTERN.matcher(tokens[i]);
            while (m.find()) {
                lastColorChar = temp.substring(m.start(), m.end());
            }

            boolean lastCharIsColorCode = false;

            tokenLength = temp.length();
            lineLength += tokenLength;
            tokenWidth = widthInPixels(temp);
            lineWidth += tokenWidth;

            if (tokenLength >= CHAT_STRING_LENGTH || tokenWidth >= CHAT_WINDOW_WIDTH) {
                // this token is too big for one line so split it

                // reset the line accumulators since we will be taking up the whole line
                lineWidth = 0;
                lineLength = 0;

                for (int j = 0; j < temp.length(); j++) {
                    // color format checking
                    if (j < temp.length() - 1 && COLOR_PATTERN.matcher(temp.substring(j, j + 1)).matches()) {
                        // we have a color format, so increment length by 2 but this will not contribute to width
                        lineLength += 2;
                        lastCharIsColorCode = true;
                        lastColorChar = temp.substring(j, j + 1);
                    } else {
                        // no color format, so normal increments
                        lineWidth += characterWidths[temp.charAt(j)];
                        lineLength++;
                        lastCharIsColorCode = false;
                    }

                    if (lineLength >= CHAT_STRING_LENGTH || lineWidth >= CHAT_WINDOW_WIDTH) {
                        out.append(TEMP_CHAR);

                        if (lastCharIsColorCode && !lastColorChar.equals(COLOR_CHAR + "f")) {
                            // we need to move both the color char and the color specifier to the next line
                            out.append(lastColorChar);
                            lineLength = 2;
                        } else {
                            // not a color format, so just move one char to the next line
                            j--;
                            lineLength = 0;
                        }
                        lineWidth = 0;
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
                // only include color char if it is not white since default is white
                if (!lastColorChar.equals(COLOR_CHAR + "f")) {
                    out.append(lastColorChar);
                    lineLength = 2;
                } else {
                    lineLength = 0;
                }
                lineWidth = 0;
                i--;
                continue;
            }
            out.append(temp);
        }

        return out.toString().split(String.valueOf(TEMP_CHAR));
    }

    // Poseidon start - widthInPixels method

    /**
     * Calculates the width of a string in pixels based on Minecraft's character widths.
     * The maximum width for chat is 320 pixels (Use CHAT_WINDOW_WIDTH).
     *
     * @param text The input string.
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

    // Poseidon end

    // Poseidon start - Text wrap rework

    private static String sanitizeText(final String input) {
        String text = trimTrailing(input);

        // Remove all trailing whitespaces and color codes
        while (endsWithColor(text)) {
            text = trimTrailing(text.substring(0, text.length() - 2));
        }

        StringBuilder sb = new StringBuilder();
        char currentColor = TEMP_CHAR;

        // Filter out all redundant color codes
        for (int i = 0; i < text.length(); i++) {
            // If there are multiple color codes chained together, we will get the last one
            while (text.charAt(i) == COLOR_CHAR && i < text.length() - 1) {
                char color = text.charAt(++i);
                if (isValidColor(color)) {
                    currentColor = color;
                } else {
                    // The color is invalid, so the chain is over
                    i--;
                    break;
                }
                i++;
            }

            // If a new color has been found, append it
            if (currentColor != TEMP_CHAR) {
                sb.append(COLOR_CHAR).append(currentColor);
                currentColor = TEMP_CHAR;
            }

            sb.append(text.charAt(i));
        }

        text = sb.toString();

        // If the text starts with a white color code, remove it
        if (text.startsWith(COLOR_CHAR + "f") ||
            text.startsWith(COLOR_CHAR + "F")) {

            text = text.substring(2, text.length());
        }

        return text;
    }

    private static String trimTrailing(final String input) {
        int length = input.length();
        while (length > 0 && Character.isWhitespace(input.charAt(length - 1))) {
            length--;
        }
        return input.substring(0, length);
    }

    private static boolean endsWithColor(final String input) {
        return input.length() >= 2 && COLOR_PATTERN.matcher(input.substring(input.length() - 2, input.length())).matches();
    }

    private static boolean isValidColor(final char color) {
        return COLOR_PATTERN.matcher(String.valueOf(COLOR_CHAR) + color).matches();
    }

    // Poseidon end

}
