package lol.ventura.features.ui;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import lol.ventura.features.events.TickEvent;
import lol.ventura.features.modules.render.Interface;
import lol.ventura.features.properties.NumberProperty;
import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.property.Property;
import lol.ventura.foundation.themes.Themes;
import lol.ventura.foundation.ui.Effect;
import lol.ventura.foundation.ui.SimpleEffectBuilder;
import lol.ventura.misc.font.FontRepository;
import lol.ventura.misc.font.IFontRenderer;
import lol.ventura.misc.render.RenderUtil;
import lol.ventura.misc.render.ScaledResolution;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Scoreboard extends Effect implements GameAccessor {
    public static ScoreboardObjective objective = null;

    @Override
    public String getName() {
        return "Scoreboard";
    }

    @Override
    public List<Property> getProperties() {
        return List.of();
    }

    public static final Pattern DOMAIN_PATTERN = Pattern.compile("^([a-zA-Z0-9]([-a-zA-Z0-9]{0,61}[a-zA-Z0-9])?\\.)?([a-zA-Z0-9]{1,2}([-a-zA-Z0-9]{0,252}[a-zA-Z0-9])?)\\.([a-zA-Z]{2,63})$/");

    @Override
    public Vector2f draw(DrawContext context, int inX, int inY) {
        if(objective == null)
            return new Vector2f(0,0);

        IFontRenderer font = Interface.getFont();

        net.minecraft.scoreboard.Scoreboard scoreboard = objective.getScoreboard();
        Collection<ScoreboardPlayerScore> collection = scoreboard.getAllPlayerScores(objective);
        List<ScoreboardPlayerScore> list = (List)collection.stream().filter((score) -> score.getPlayerName() != null && !score.getPlayerName().startsWith("#")).collect(Collectors.toList());
        if (list.size() > 15) {
            collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
        } else {
            collection = list;
        }

        List<Pair<ScoreboardPlayerScore, Text>> list2 = Lists.newArrayListWithCapacity(collection.size());
        Text text = objective.getDisplayName();
        int i = mc.textRenderer.getWidth(text);
        int j = i;
        int k = mc.textRenderer.getWidth(": ");

        for(ScoreboardPlayerScore scoreboardPlayerScore : collection) {
            Team team = scoreboard.getPlayerTeam(scoreboardPlayerScore.getPlayerName());
            Text text2 = Team.decorateName(team, Text.literal(scoreboardPlayerScore.getPlayerName()));
            list2.add(Pair.of(scoreboardPlayerScore, text2));
            j = Math.max(j, mc.textRenderer.getWidth(text2) + k + mc.textRenderer.getWidth(Integer.toString(scoreboardPlayerScore.getScore())));
        }


        int var10000 = collection.size();
        Objects.requireNonNull(mc.textRenderer);
        int l = var10000 * 9;
        int m = (int) (ScaledResolution.getHeight() / 2 + l / 3);
        int n = 3;
        int o = (int) (ScaledResolution.getWidth() - j - 3);
        int p = 0;
        int q = mc.options.getTextBackgroundColor(0.3F);
        int r = mc.options.getTextBackgroundColor(0.4F);



        int height = (list2.size()+1)*9 + 4;
        float width = mc.textRenderer.getWidth("venturaclient.eu");

        for(Pair<ScoreboardPlayerScore, Text> pair : list2) {
            //float len =  mc.textRenderer.getWidth(pair.getSecond());
            float len =  font.getWidthOrdered(pair.getSecond() .asOrderedText(),8);

            if(len > width)
                width = len;
        }

        if(width < mc.textRenderer.getWidth(text))
            width = mc.textRenderer.getWidth(text);

        RenderUtil.drawBlurRoundedRectWithTint(context.getMatrices(), inX-4,inY-6,width+12,height+8,5, new Color(0,0,0,100));


        Collections.reverse(list2);

        MutableText prefix = Text.literal("");
        String prefixText = "venturaclient.eu";
        int mainColor = Interface.getTheme().getMainColor().getRGB();
        int secondaryColor = Interface.getTheme().getSecondColor().getRGB();

        int maxIndex = prefixText.length() - 1;
        for (int i2 = 0; i2 < prefixText.length(); i2++) {
            float ratio = (float) i2 / (prefixText.length() - 1);
            int col = Themes.fadeBetween(mainColor, secondaryColor, ratio);
            String hex = String.format("#%06X", col & 0xFFFFFF);
            Text charText = Text.literal(prefixText.charAt(i2) + "")
                    .styled(s -> s.withColor(net.minecraft.text.TextColor.parse(hex))
                            .withBold(true));
            prefix.append(charText);
        }


        list2.add(new Pair<>(null, prefix));
        for(Pair<ScoreboardPlayerScore, Text> pair : list2) {
            if(!pair.getSecond().getString().contains("venturaclient") && DOMAIN_PATTERN.asMatchPredicate().test(pair.getSecond().getString()))
                continue;
            //System.out.println(pair.getSecond().getString());
            int t = m - p * 9;
            int u = (int) (ScaledResolution.getWidth() - 3 + 2);
            int var10001 = o - 2;
            if (p == 0) {
                var10001 = o - 2;
                Objects.requireNonNull(mc.textRenderer);
                TextRenderer var33 = mc.textRenderer;
                int var10003 = o + j / 2 - i / 2;
                Objects.requireNonNull(mc.textRenderer);
                font.drawStringOrdered(text.asOrderedText(),  (int) (inX + ((width + 8) * 0.5f) - (font.getWidthOrdered(text.asOrderedText(), 8) * 0.5f)), inY, 8, Color.white);
                //context.drawText(var33, text.asOrderedText(), (int) (inX + ((width + 8) * 0.5f) - (mc.textRenderer.getWidth(text) * 0.5f)), inY, -1, false);
            }

            ++p;
            Text text3 = (Text)pair.getSecond();
            Formatting var31 = Formatting.RED;
            Objects.requireNonNull(mc.textRenderer);
            Objects.requireNonNull(mc.textRenderer);
            //context.fill(var10001, t, u, t + 9, q);
            font.drawStringOrdered(text3.asOrderedText(), inX + 2, inY + 2, 8, Color.white);
            //context.drawText(mc.textRenderer, text3, inX + 2, inY + 2, -1, false);
            inY+=9;
        }
        return new Vector2f(width, height);
    }
}
