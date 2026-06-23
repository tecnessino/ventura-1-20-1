package lol.ventura.foundation.themes;

import lombok.Getter;
import net.minecraft.util.Formatting;

import java.awt.Color;

public enum Themes {
    VENTURA("Ventura",
            new Color(52, 154, 143),
            new Color(37, 69, 101),

            new Color(52, 154, 143),
            new Color(37, 69, 101),

            new Color(37, 69, 101),
            new Color(52, 154, 143),
            Formatting.AQUA
    ),

    HELL("Hell",
            new Color(255, 149, 0),
            new Color(255, 11, 247),

            new Color(255, 149, 0),
            new Color(157, 103, 35),

            new Color(195, 136, 49),
            new Color(255, 149, 0),
            Formatting.GOLD),

    PASTEL("Pastel",
            new Color(155, 221, 255),
            new Color(219, 107, 219),

            new Color(155, 221, 255),
            new Color(219, 107, 219),

            new Color(155, 221, 255),
            new Color(219, 107, 219),

            Formatting.LIGHT_PURPLE
    ),

    PINEAPPLE("Pineapple",
            new Color(255, 198, 97),
            new Color(252, 255, 120),

            new Color(255, 198, 97),
            new Color(252, 255, 120),

            new Color(255, 198, 97),
            new Color(252, 255, 120),

            Formatting.YELLOW
    ),

    TWILIGHT("Twilight",
            new Color(247, 163, 255),
            new Color(76, 255, 128),

            new Color(247, 163, 255),
            new Color(76, 255, 128),

            new Color(247, 163, 255),
            new Color(76, 255, 128),

            Formatting.GREEN
    ),

    SATIN("Satin",
            new Color(205, 0, 0),
            new Color(155, 0, 0),

            new Color(205, 0, 0),
            new Color(155, 0, 0),

            new Color(205, 0, 0),
            new Color(155, 0, 0),

            Formatting.DARK_RED
    ),

    DARK("Dark",
            new Color(86, 86, 86),
            new Color(128, 128, 128),

            new Color(86, 86, 86),
            new Color(128, 128, 128),

            new Color(86, 86, 86),
            new Color(128, 128, 128),
            Formatting.GRAY
    ),

    PEACH("Peach",
            new Color(255, 216, 190),
            new Color(255, 216, 140),

            new Color(255, 216, 140),
            new Color(255, 255, 255),

            new Color(255, 216, 190),
            new Color(255, 216, 140),
            Formatting.WHITE
    ),

    AQUATIC("Aquatic",
            new Color(151, 216, 190),
            new Color(167, 216, 140),

            new Color(167, 216, 140),
            new Color(255,255,255),

            new Color(151, 216, 190),
            new Color(167, 216, 140),
            Formatting.GREEN
    ),

    LAVA("Lava",
            new Color(255, 8, 0),
            new Color(255, 78, 0),

            new Color(200, 8, 0),
            new Color(255, 120, 0),

            new Color(255, 8, 0),
            new Color(255, 78, 0),
            Formatting.RED
    ),

    COZY("Cozy",
            new Color(170, 190, 255),
            new Color(219, 193, 255),

            new Color(170, 190, 255),
            new Color(219, 193, 255),

            new Color(170, 190, 255),
            new Color(219, 193, 255),
            Formatting.BLUE
    ),

    APPLE("Apple",
            new Color(195, 255, 168),
            new Color(244, 255, 168),

            new Color(244, 255, 168, 128),
            new Color(255, 255, 255),

            new Color(195, 255, 168),
            new Color(244, 255, 168),
            Formatting.YELLOW
    ),

    GRAPE("Grape",
            new Color(89, 0, 255),
            new Color(128, 0, 255),

            new Color(89, 0, 255),
            new Color(128, 0, 255),

            new Color(89, 0, 255),
            new Color(128, 0, 255),
            Formatting.DARK_AQUA
    ),

    LEMON("Lemon",
            new Color(255, 236, 0),
            new Color(167, 216, 0),

            new Color(255, 236, 0),
            new Color(167, 216, 0),

            new Color(255, 236, 0),
            new Color(167, 216, 0),
            Formatting.YELLOW
    ),

    SUNLIGHT("Sunlight",
            new Color(226, 140, 255),
            new Color(255, 253, 188),

            new Color(226, 140, 255),
            new Color(255, 253, 188),

            new Color(226, 140, 255),
            new Color(255, 253, 188),
            Formatting.LIGHT_PURPLE
    ),

    SEA("Sea",
            new Color(140, 255, 235),
            new Color(119, 255, 189),

            new Color(119, 255, 189),
            new Color(255, 255, 255),

            new Color(140, 255, 235),
            new Color(119, 255, 189),
            Formatting.BLUE
    ),

    SKY("Sky",
            new Color(0, 101, 255),
            new Color(0, 221, 255),

            new Color(0, 101, 255),
            new Color(0, 221, 255),

            new Color(0, 101, 255),
            new Color(0, 221, 255),
            Formatting.DARK_BLUE
    );



    @Getter
    String name;
    @Getter
    Color mainColor;
    @Getter
    Color secondColor;
    @Getter
    Color clickguiEnabledColor;
    @Getter
    Color clickguiEnabledTextColor;

    @Getter
    Color boolClickguiTheme;
    @Getter
    Color boolClickguiCircle;

    @Getter
    Formatting chatPrefixColor;

    @Getter
    String capePath;

    Themes(String name, Color mainColor, Color secondColor, Color clickguiEnabledColor, Color clickguiEnabledTextColor, Color boolClickguiTheme, Color boolClickguiCircle, Formatting chatPrefixColor) {
        this.name = name;
        this.mainColor = mainColor;
        this.secondColor = secondColor;
        this.clickguiEnabledColor = clickguiEnabledColor;
        this.clickguiEnabledTextColor = clickguiEnabledTextColor;
        this.boolClickguiTheme = boolClickguiTheme;
        this.boolClickguiCircle = boolClickguiCircle;
        this.chatPrefixColor = chatPrefixColor;
        this.capePath = "tecness/capes/" + name.toLowerCase() + ".png";
    }

    public static int fadeBetween(int color1, int color2, float offset) {
        if (offset > 1)
            offset = 1 - offset % 1;

        double invert = 1 - offset;
        int r = (int) ((color1 >> 16 & 0xFF) * invert +
                (color2 >> 16 & 0xFF) * offset);
        int g = (int) ((color1 >> 8 & 0xFF) * invert +
                (color2 >> 8 & 0xFF) * offset);
        int b = (int) ((color1 & 0xFF) * invert +
                (color2 & 0xFF) * offset);
        int a = (int) ((color1 >> 24 & 0xFF) * invert +
                (color2 >> 24 & 0xFF) * offset);
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF);
    }

    public int forOffset(long index) {
        return fadeBetween(mainColor.getRGB(), secondColor.getRGB(), ((System.currentTimeMillis() + (index / 3)) % 1000 / (1000 / 2.0f)));
    }
}
