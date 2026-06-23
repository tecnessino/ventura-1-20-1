package lol.ventura.features.ui;

import lol.ventura.features.modules.render.Interface;
import lol.ventura.foundation.property.Property;
import lol.ventura.foundation.ui.Effect;
import lol.ventura.foundation.ui.ISimpleEffectRenderer;
import lol.ventura.foundation.ui.SimpleEffectBuilder;
import lol.ventura.misc.font.FontRepository;
import lol.ventura.misc.font.IFontRenderer;
import lol.ventura.misc.render.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Armor extends Effect {
    @Override
    public String getName() {
        return "Armor";
    }

    @Override
    public List<Property> getProperties() {
        return List.of();
    }

    @Override
    public Vector2f draw(DrawContext context, int x, int y) {
        if(MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().player.getInventory() == null)
            return new Vector2f(0,0);

        IFontRenderer font = Interface.getFont();
        ISimpleEffectRenderer renderer = Interface.getEffectRenderer();

        SimpleEffectBuilder builder2 = new SimpleEffectBuilder(4, Interface.getEffectRenderer());

        builder2.icon(FontRepository.getInstance().getFont("venturaicons"), "o", 8,3);
        builder2.text(font, "Armor");

        SimpleEffectBuilder builder = new SimpleEffectBuilder(4, renderer);

        ArrayList<ItemStack> itemy = new ArrayList<>(MinecraftClient.getInstance().player.getInventory().armor);
        Collections.reverse(itemy);

        float sum = 0;
        int itemsNotAir = 0;

        for (ItemStack stack : itemy) {
            if (stack.getItem() == Items.AIR)
                continue;

            itemsNotAir++;

            sum += 100 - (float) (stack.getDamage() * 100) / stack.getItem().getMaxDamage();
        }
        if(itemsNotAir == 0)
            return new Vector2f(0,0);
        builder2.draw(context, new Vector2f(x,y));

        sum = (sum * 100) / (itemsNotAir * 100);


        String statusText = "None";
        Color statusColor = Color.white;

        if(sum >= 75)
        {
            statusColor = Color.green;
            statusText = "Good";
        } else if(sum >= 50)
        {
            statusColor = Color.yellow;
            statusText = "Moderate";
        } else {
            statusColor = Color.red;
            statusText = "Low";
        }

        builder.text(font, statusText, statusColor);
        builder.splitter();


        for(ItemStack stack : itemy)
        {
            if(stack.getItem() == Items.AIR)
                continue;

            builder.sprite(MinecraftClient.getInstance()
                    .getItemRenderer()
                    .getModel(stack, null, null, 0)
                    .getParticleSprite());
        }

        builder.draw(context,new Vector2f(x,y+16+1));


        float offset = x + font.getWidth(statusText,8) + renderer.splitterOffset(new SimpleEffectBuilder.MiscInfo(4)) + 4+4+4;
        for(ItemStack stack : itemy)
        {
            if(stack.getItem() == Items.AIR)
                continue;

            float usagePercent = 100 - (float) (stack.getDamage() * 100) / stack.getItem().getMaxDamage();
            float usageScaled = 9 - ((float) (stack.getDamage() * 9) / stack.getItem().getMaxDamage()) - 1;

            Color tint = Color.white;

            if (usagePercent >= 75)
            {
                tint = Color.green;
            }
            else if (usagePercent >= 50)
            {
                tint = Color.yellow;
            }
            else
            {
                tint = Color.red;
            }


            RenderUtil.drawRoundedRect(offset,y +  11.5f+16+1,9,2,0.1f,new Color(89,88,88));
            RenderUtil.drawRoundedRect(offset,y +  11.5f+16+1,usageScaled,2,0.1f,tint);

            offset += 4 + 9;
        }

        return new Vector2f(builder.getWidth(), 16);
    }
}
