package lol.ventura.foundation;

import lol.ventura.features.modules.render.Interface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import lol.ventura.foundation.themes.Themes;

public interface GameAccessor {
    MinecraftClient mc = MinecraftClient.getInstance();

    static void sendChatMessage(String message) {
        MutableText prefix = Text.literal("");
        String prefixText = "Ventura Client → ";
        int mainColor = Interface.getTheme().getMainColor().getRGB();
        int secondaryColor = Interface.getTheme().getSecondColor().getRGB();

        int maxIndex = prefixText.length() - 1;
        for (int i = 0; i < prefixText.length(); i++) {
            float ratio = (float) i / (prefixText.length() - 1);
            int col = Themes.fadeBetween(mainColor, secondaryColor, ratio);
            String hex = String.format("#%06X", col & 0xFFFFFF);
            Text charText = Text.literal(prefixText.charAt(i) + "")
                    .styled(s -> s.withColor(net.minecraft.text.TextColor.parse(hex))
                            .withBold(true));
            prefix.append(charText);
        }
        MutableText fullMessage = prefix.append(Text.literal(message).formatted(Formatting.WHITE));
        mc.inGameHud.getChatHud().addMessage(fullMessage);
    }

    static void sendPacket(Packet<?> packet) {
        if (mc.getNetworkHandler() == null) return;

        mc.getNetworkHandler().sendPacket(packet);
    }
}