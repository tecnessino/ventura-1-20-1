package lol.ventura.features.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import lol.ventura.features.modules.render.Interface;
import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.property.Property;
import lol.ventura.foundation.ui.Effect;
import lol.ventura.foundation.ui.ISimpleEffectRenderer;
import lol.ventura.foundation.ui.SimpleEffectBuilder;
import lol.ventura.misc.font.FontRepository;
import lol.ventura.misc.font.IFontRenderer;
import lol.ventura.misc.render.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;

import java.awt.*;
import java.util.List;

public class Inventory extends Effect implements GameAccessor {
    private static final int ROWS = 3;
    private static final int COLUMNS = 9;
    private static final int ITEM_SIZE = 16;
    private static final int SPACING = 1;
    private static final int PADDING = 2;
    private static final int HEADER_HEIGHT = 16;
    private static final int HOTBAR_SLOTS = 9;
    private static final int FONT_SIZE = 8;
    private static final float HEADER_Y_OFFSET = 2;
    private static final float BOX_Y_OFFSET = 2;
    private static final Color BOX_COLOR = new Color(0, 0, 0, 180);
    private static final Color LINE_COLOR = new Color(255, 255, 255, 50);

    @Override
    public String getName() {
        return "Inventory";
    }

    @Override
    public List<Property> getProperties() {
        return List.of();
    }

    @Override
    public Vector2f draw(DrawContext context, int x, int y) {
        if (mc.player == null || mc.player.getInventory() == null) {
            return new Vector2f(0, 0);
        }

        IFontRenderer font = Interface.getFont();
        ISimpleEffectRenderer renderer = Interface.getEffectRenderer();
        SimpleEffectBuilder builder = new SimpleEffectBuilder(4, renderer);
        builder.icon(FontRepository.getInstance().getFont("venturaicons"), "o", FONT_SIZE, 3);
        builder.text(font, "Inventory");
        builder.draw(context, new Vector2f(x, y));
        float headerWidth = builder.getWidth();

        int gridWidth = COLUMNS * (ITEM_SIZE + SPACING) - SPACING + PADDING * 2;
        int boxHeight = ROWS * (ITEM_SIZE + SPACING) - SPACING + PADDING * 2;
        int boxY = y + (int) HEADER_HEIGHT + (int) BOX_Y_OFFSET;


        renderer.drawBackground(context, new Vector2f(x,boxY),gridWidth,boxHeight);
        //RenderUtil.drawRoundedRect(x, boxY, gridWidth, boxHeight, 5, BOX_COLOR);

        int offsetX = x + PADDING;
        int offsetY = boxY + PADDING;
        drawSeparationLines(offsetX, offsetY, gridWidth, boxHeight);
        renderItems(context, font, offsetX, offsetY, mc.player.getInventory().main);

        return new Vector2f(Math.max(headerWidth, gridWidth), HEADER_HEIGHT + boxHeight + BOX_Y_OFFSET);
    }

    private void drawSeparationLines(int offsetX, int offsetY, int gridWidth, int boxHeight) {
        for (int row = 0; row <= ROWS; row++) {
            float lineY = offsetY + row * (ITEM_SIZE + SPACING) - SPACING;
            RenderUtil.drawRoundedRect(offsetX, lineY - 0.5f, gridWidth - PADDING * 2, 1, 0, LINE_COLOR);
        }

        for (int col = 0; col <= COLUMNS; col++) {
            float lineX = offsetX + col * (ITEM_SIZE + SPACING) - SPACING;
            RenderUtil.drawRoundedRect(lineX - 0.5f, offsetY, 1, boxHeight - PADDING * 2, 0, LINE_COLOR);
        }
    }

    private void renderItems(DrawContext context, IFontRenderer font, int offsetX, int offsetY, DefaultedList<ItemStack> inventory) {
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        DiffuseLighting.enableGuiDepthLighting();

        for (int i = HOTBAR_SLOTS; i < ROWS * COLUMNS + HOTBAR_SLOTS; i++) {
            ItemStack stack = inventory.get(i);
            if (stack.isEmpty()) continue;

            int adjustedIndex = i - HOTBAR_SLOTS;
            int row = adjustedIndex / COLUMNS;
            int column = adjustedIndex % COLUMNS;

            int itemX = offsetX + column * (ITEM_SIZE + SPACING);
            int itemY = offsetY + row * (ITEM_SIZE + SPACING);

            RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);

            context.drawItem(stack, itemX, itemY);
            context.drawItemInSlot(mc.textRenderer, stack, itemX, itemY);
        }

        DiffuseLighting.disableGuiDepthLighting();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }
}