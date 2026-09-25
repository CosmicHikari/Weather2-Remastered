package net.mrbt0907.weather2.util;

public class StringUtils {
    public static String parseID(String str) {
        return str.trim().toLowerCase().replaceAll("[ _\\-]+", "_").replaceAll("[^a-z0-9_:]", "");
    }

    public static String toUpperCaseAlt(String str) {
        char[] newStr = new char[str.length()];
        boolean space = true;
        int size = str.length();

        for (int i = 0; i < size; i++) {
            Character c = str.charAt(i);
            if (c.equals(' ')) {
                space = true;
                newStr[i] = c;
            } else if (space && Character.isLowerCase(c)) {
                space = false;
                newStr[i] = Character.toUpperCase(c);
            } else
                newStr[i] = c;
        }

        return String.valueOf(newStr);
    }
}