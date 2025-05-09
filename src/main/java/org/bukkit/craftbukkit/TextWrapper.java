package org.bukkit.craftbukkit;

import com.legacyminecraft.poseidon.PoseidonConfig;

import java.util.ArrayList;
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

    // Poseidon start - Text wrap rework

    public static String[] wrapText(final String input) {
        if (input == null || input.isEmpty())
            return new String[0];

        String algorithm = PoseidonConfig.getInstance().getConfigString("settings.text-wrapping-algorithm.value");
        switch (algorithm.toLowerCase()) {
            case "vanilla": return wrapTextVanilla(input);
            case "poseidon": return wrapTextPoseidon(input);
            default: return wrapTextCraftBukkit(input);
        }
    }

    public static String[] wrapTextVanilla(final String input) {
        if (input == null || input.isEmpty())
            return new String[0];

        ArrayList<String> parts = new ArrayList<>();
        int length = input.length();

        for (int i = 0; i < length;) {
            String part = input.substring(i, Math.min(length, i + CHAT_STRING_LENGTH));

            // Consider all lines except the last one
            if (i + CHAT_STRING_LENGTH < length) {
                if (part.charAt(part.length() - 1) == COLOR_CHAR &&
                    isValidColor(input.charAt(i + CHAT_STRING_LENGTH))) {
                    // Move the COLOR_CHAR to the next part
                    // when: ".....§", "c....."
                    part = part.substring(0, part.length() - 1);
                } else if (part.charAt(part.length() - 2) == COLOR_CHAR &&
                           isValidColor(part.charAt(part.length() - 1))) {
                    // Move the COLOR_CHAR and the color code to the next part
                    // when: ".....§c", "....."
                    part = part.substring(0, part.length() - 2);
                }
            }

            parts.add(part);
            i += part.length();
        }

        return parts.toArray(new String[0]);
    }

    public static String[] wrapTextCraftBukkit(final String input) {
        if (input == null || input.isEmpty())
            return new String[0];

        final StringBuilder out = new StringBuilder();
        char colorChar = 'f';
        int lineWidth = 0;
        int lineLength = 0;

        // Go over the message char by char.
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);

            // Get the color
            if (ch == COLOR_CHAR && i < input.length() - 1) {
                // We might need a linebreak ... so ugly ;(
                if (lineLength + 2 > CHAT_STRING_LENGTH) {
                    out.append('\n');
                    lineLength = 0;
                    if (colorChar != 'f' && colorChar != 'F') {
                        out.append(COLOR_CHAR).append(colorChar);
                        lineLength += 2;
                    }
                }
                colorChar = input.charAt(++i);
                out.append(COLOR_CHAR).append(colorChar);
                lineLength += 2;
                continue;
            }

            // Figure out if it's allowed
            int index = allowedChars.indexOf(ch);
            if (index == -1) {
                // Invalid character .. skip it.
                continue;
            } else {
                // Sadly needed as the allowedChars string misses the first
                index += 32;
            }

            // Find the width
            final int width = characterWidths[index];

            // See if we need a linebreak
            if (lineLength + 1 > CHAT_STRING_LENGTH || lineWidth + width >= CHAT_WINDOW_WIDTH) {
                out.append('\n');
                lineLength = 0;

                // Re-apply the last color if it isn't the default
                if (colorChar != 'f' && colorChar != 'F') {
                    out.append(COLOR_CHAR).append(colorChar);
                    lineLength += 2;
                }
                lineWidth = width;
            } else {
                lineWidth += width;
            }
            out.append(ch);
            lineLength++;
        }

        // Return it split
        return out.toString().split("\n");
    }

    public static String[] wrapTextPoseidon(final String input) {
        if (input == null || input.isEmpty())
            return new String[0];

        return new String[0];
    }

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

            text = text.substring(2);
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
        return input.length() >= 2 && COLOR_PATTERN.matcher(input.substring(input.length() - 2)).matches();
    }

    private static boolean isValidColor(final char color) {
        return COLOR_PATTERN.matcher(String.valueOf(COLOR_CHAR) + color).matches();
    }

    // Poseidon end

    // Poseidon start - widthInPixels method

    /**
     * Calculates the width of a string in pixels based on Minecraft's character widths.
     * The maximum width for chat is 320 pixels (Use CHAT_WINDOW_WIDTH).
     *
     * @param input The input string.
     * @return The width of the string in pixels.
     */
    public static int widthInPixels(final String input) {
        if (input == null || input.isEmpty())
            return 0;

        int output = 0;

        // literally yoinked from above and removed unnecessary components.
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);

            if (ch == COLOR_CHAR && i < input.length() - 1) {
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

}
