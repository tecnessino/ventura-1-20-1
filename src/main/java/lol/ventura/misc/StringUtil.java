package lol.ventura.misc;

public final class StringUtil {
    public static String upperSnakeCaseToPascal(final String s) {
        if (s == null) return null;
        if (s.length() == 1) return Character.toString(s.charAt(0));
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
