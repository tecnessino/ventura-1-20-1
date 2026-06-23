package lol.ventura.features.modules.render;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import lol.ventura.features.events.Draw2DEvent;
import lol.ventura.features.events.TickEvent;
import lol.ventura.features.properties.BooleanProperty;
import lol.ventura.features.properties.EnumProperty;
import lol.ventura.features.properties.MultiProperty;
import lol.ventura.features.properties.NumberProperty;
import lol.ventura.features.ui.*;
import lol.ventura.features.ui.renderers.BlurRenderer;
import lol.ventura.features.ui.renderers.EmissiveRenderer;
import lol.ventura.foundation.event.EventBus;
import lol.ventura.foundation.themes.Themes;
import lol.ventura.foundation.ui.ISimpleEffectRenderer;
import lol.ventura.features.ui.renderers.AlternativeRenderer;
import lol.ventura.features.ui.renderers.ClassicRenderer;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import lol.ventura.foundation.module.ModuleRepository;
import lol.ventura.foundation.property.PropertyRepository;
import lol.ventura.foundation.ui.Effect;
import lol.ventura.misc.animation.Animation;
import lol.ventura.misc.font.FontRepository;
import lol.ventura.misc.font.IFontRenderer;
import lol.ventura.misc.math.Bounds;
import lol.ventura.misc.render.FramebufferManager;
import lol.ventura.misc.render.RenderUtil;
import lol.ventura.misc.render.ScaledResolution;
import lol.ventura.misc.storage.StorageService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraft.client.gui.widget.ClickableWidget.WIDGETS_TEXTURE;

@ModuleDescriptor(name = "Interface", category = Category.RENDER, brief = "Interfejs uzytkowniak wdmo", enableByDefault = true)
public class Interface extends Module {
    @Getter
    public final ArrayList<Effect> effects = new ArrayList<>();
    public boolean leftIsDown = false;


    @AllArgsConstructor @Getter
    public static enum GlobalFont {
        INTER("roboto", 3.5f),
        MONTSERRAT("montserrat", 4f),
        POPPINS("poppins",4.5f),
        SF("sf", 3.5f),
        GEIST("geist", 4f);

        public final String name;
        public final float fixedHalfHeight;
    }

    @AllArgsConstructor @Getter
    public static enum WidgetStyle {
        CLASSIC(new ClassicRenderer()),
        ALTERNATIVE(new AlternativeRenderer()),
        BLUR(new BlurRenderer()),
        EMISSIVE(new EmissiveRenderer());

        public final ISimpleEffectRenderer renderer;
    }

    public final MultiProperty<GlobalFont> globalFont = new MultiProperty<>("Global Font", false, GlobalFont.POPPINS);
    public final MultiProperty<WidgetStyle> widgetStyle = new MultiProperty<>("Widget Style", false, WidgetStyle.CLASSIC);
    public final MultiProperty<Themes> theme = new MultiProperty<>("Theme", false, Themes.VENTURA);
    public final BooleanProperty postfx = new BooleanProperty("Enable PostFX", true);
    public final MultiProperty<EffectEnum> enableWidget = new MultiProperty<>("Widgets", true, EffectEnum.WATERMARK);
    public final NumberProperty margin = new NumberProperty("Global margins", 1f, 0f, 5f, 0.1f);
    public static enum EffectEnum
    {
        WATERMARK, COORDINATES, EFFECTS, SPOTIFY, TARGETINFO, SCOREBOARD, KEYBINDS, BPS, TPS, NOTIFY, ARMOR, INVENTORY, ARRAYLIST
    }

    @AllArgsConstructor
    public static class InterfaceEntry
    {
        public Effect effect;
        public EffectEnum enableSupplier;
    }

    public Interface(ModuleDescriptor descriptor) {
        super(descriptor);
        addSettings(globalFont, widgetStyle, theme, postfx, enableWidget, margin);

        registerEffects(
                new InterfaceEntry(new Watermark(), EffectEnum.WATERMARK),
                new InterfaceEntry(new Coordinates(), EffectEnum.COORDINATES),
                new InterfaceEntry(new Effects(), EffectEnum.EFFECTS),
                new InterfaceEntry(new Spotify(), EffectEnum.SPOTIFY),
                new InterfaceEntry(new Keybinds(), EffectEnum.KEYBINDS),
                new InterfaceEntry(new TargetInfo(), EffectEnum.TARGETINFO),
                new InterfaceEntry(new BPS(), EffectEnum.BPS),
                new InterfaceEntry(new TPS(), EffectEnum.TPS),
                new InterfaceEntry(new Notify(), EffectEnum.NOTIFY),
                new InterfaceEntry(new Armor(), EffectEnum.ARMOR),
                new InterfaceEntry(new Inventory(), EffectEnum.INVENTORY),
                new InterfaceEntry(new lol.ventura.features.ui.ArrayList(), EffectEnum.ARRAYLIST),
                new InterfaceEntry(new Scoreboard(), EffectEnum.SCOREBOARD)
        );


        JsonObject object = StorageService.getInstance().getForClass(Interface.class);
        for(var keys : object.entrySet())
        {
            Optional<Effect> ef = effects.stream().filter((em) -> em.getName().equalsIgnoreCase(keys.getKey())).findFirst();
            if(ef.isEmpty())
                continue;
            Effect e = ef.get();
            e.bounds.setX(keys.getValue().getAsJsonObject().get("x").getAsFloat());
            e.bounds.setY(keys.getValue().getAsJsonObject().get("y").getAsFloat());
        }
    }

    private static final Animation hotbarAnimation = new Animation();

    public final IEventListener<TickEvent> tick = ev -> {
        for(Effect e : effects)
        {
            if(!e.enabled)
                continue;
            e.tick(ev);
        }
    };

    public static float getMargin()
    {
        return ModuleRepository.getInstance().getModule(Interface.class).margin.getValue().floatValue();
    }

    public static void begin2D(DrawContext context)
    {
        Draw2DEvent event = new Draw2DEvent(context);
        Interface hud =  ModuleRepository.getInstance().getModule(Interface.class);
        ESP.draw();
        hud.drawBeginPrivate(event);
    }

    public static void draw2D(DrawContext context)
    {
        Draw2DEvent event = new Draw2DEvent(context);
        Interface hud =  ModuleRepository.getInstance().getModule(Interface.class);

        EventBus.getInstance().emit(event);
        hud.drawEndPrivate(event);
        FramebufferManager.getInstance().scanForBrokenFramebuffers();
    }

    private static void renderHotbarItem(DrawContext context, int x, int y, float f, PlayerEntity player, ItemStack stack, int seed) {
        if (!stack.isEmpty()) {
            float g = (float)stack.getBobbingAnimationTime() - f;
            if (g > 0.0F) {
                float h = 1.0F + g / 5.0F;
                context.getMatrices().push();
                context.getMatrices().translate((float)(x + 8), (float)(y + 12), 0.0F);
                context.getMatrices().scale(1.0F / h, (h + 1.0F) / 2.0F, 1.0F);
                context.getMatrices().translate((float)(-(x + 8)), (float)(-(y + 12)), 0.0F);
            }

            context.drawItem(player, stack, x, y, seed);
            if (g > 0.0F) {
                context.getMatrices().pop();
            }

            context.drawItemInSlot(MinecraftClient.getInstance().textRenderer, stack, x, y);
        }
    }

    private static HashMap<ChatHudLine.Visible, Animation> fadeAnimations = new HashMap<>();

    public static void renderChat(DrawContext context, int currentTick, int mouseX, int mouseY, int lineHeight, List<ChatHudLine.Visible> visibleMessages, int scrolledLines)
    {
        IFontRenderer font = Interface.getFont();
        List<ChatHudLine.Visible> messages = new ArrayList<>(visibleMessages);
        //Collections.reverse(messages);

        int toIndex = Math.min(messages.size(), 10);
        messages  = messages.subList(0, toIndex);
        Collections.reverse(messages);


        float y = ScaledResolution.getHeight() - messages.size() * 10;


        int longest = 0;
        for(ChatHudLine.Visible v : messages)
        {
            float length = font.getWidthOrdered(v.content(), 8);
            if(longest < length)
                longest = (int) length;
        }

        Bounds b = new Bounds(10,y-15-15,(longest) + 15,messages.size()*10 + 15);

        //getEffectRenderer().drawBackground(context, new Vector2f( b.getX(),b.getY()), b.getWidth(),b.getHeight());
        RenderUtil.drawBlurRoundedRectWithTint(context.getMatrices(), b.getX(),b.getY(),b.getWidth(),b.getHeight(),5, new Color(0,0,0,100));


        float offset = y - 20;
        for(ChatHudLine.Visible v : messages)
        {
            if(!fadeAnimations.containsKey(v))
                fadeAnimations.put(v, new Animation());
            Animation animation = fadeAnimations.get(v);
            animation.update();
            animation.animate(255,0.1f);

            Color c = new Color(255,255,255, (int) animation.getValue());
            int argb = (c.getAlpha() << 24) | (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();

            font.drawStringOrdered(v.content(), 17, offset, 8, c);
            //context.drawText(mc.textRenderer, v.content(), 17, (int) offset, argb, true);
            offset += 10;
        }
    }

    public static void renderHotbar(float tickDelta, DrawContext context)
    {
        PlayerEntity playerEntity = mc.player;
        if (playerEntity != null) {
            ItemStack itemStack = playerEntity.getOffHandStack();
            Arm arm = playerEntity.getMainArm().getOpposite();
            int i = (int) (ScaledResolution.getWidth() / 2);
            int j = 182;
            int k = 91;
            context.getMatrices().push();
            context.getMatrices().translate(0.0F, 0.0F, -90.0F);
            //context.drawTexture(WIDGETS_TEXTURE, i - 91, (int) (ScaledResolution.getHeight() - 22), 0, 0, 182, 22);
            //RenderUtil.drawBlurRoundedRectWithTint(context.getMatrices(), i-91, (int) (ScaledResolution.getHeight() - 22), 182,22,6,new Color(0,0,0,133));

            getEffectRenderer().drawBackground(context, new Vector2f(i-91, (int) (ScaledResolution.getHeight() - 22)), 182,22);

            //context.drawTexture(WIDGETS_TEXTURE, i - 91 - 1 + playerEntity.getInventory().selectedSlot * 20, (int) (ScaledResolution.getHeight() - 22 - 1), 0, 22, 24, 22);
            hotbarAnimation.update();
            hotbarAnimation.animate(i - 91 + playerEntity.getInventory().selectedSlot * 20, 0.05f);
            RenderUtil.drawRoundedRect((float) hotbarAnimation.getValue(), (int) (ScaledResolution.getHeight() - 22), 22, 22, 5, new Color(200,200,200,36));
            if (!itemStack.isEmpty()) {
                if (arm == Arm.LEFT) {
                    context.drawTexture(WIDGETS_TEXTURE, i - 91 - 29, (int) (ScaledResolution.getHeight() - 23), 24, 22, 29, 24);
                } else {
                    context.drawTexture(WIDGETS_TEXTURE, i + 91, (int) (ScaledResolution.getHeight() - 23), 53, 22, 29, 24);
                }
            }

            context.getMatrices().pop();
            int l = 1;

            for(int m = 0; m < 9; ++m) {
                int n = i - 90 + m * 20 + 2;
                int o = (int) (ScaledResolution.getHeight() - 16 - 3);
                renderHotbarItem(context, n, o, tickDelta, playerEntity, (ItemStack)playerEntity.getInventory().main.get(m), l++);
            }

            if (!itemStack.isEmpty()) {
                int m = (int) (ScaledResolution.getHeight() - 16 - 3);
                if (arm == Arm.LEFT) {
                    renderHotbarItem(context, i - 91 - 26, m, tickDelta, playerEntity, itemStack, l++);
                } else {
                    renderHotbarItem(context, i + 91 + 10, m, tickDelta, playerEntity, itemStack, l++);
                }
            }

            RenderSystem.enableBlend();
            /*if (this.client.options.getAttackIndicator().getValue() == AttackIndicator.HOTBAR) {
                float f = this.client.player.getAttackCooldownProgress(0.0F);
                if (f < 1.0F) {
                    int n = this.scaledHeight - 20;
                    int o = i + 91 + 6;
                    if (arm == Arm.RIGHT) {
                        o = i - 91 - 22;
                    }

                    int p = (int)(f * 19.0F);
                    context.drawTexture(ICONS, o, n, 0, 94, 18, 18);
                    context.drawTexture(ICONS, o, n + 18 - p, 18, 112 - p, 18, p);
                }
            }*/

            RenderSystem.disableBlend();
        }
    }

    private void drawEndPrivate(Draw2DEvent event)
    {
        boolean drawPostFx = postfx.getValue();
        //boolean drawPostFx = postfx.getValue();

        JsonObject object = StorageService.getInstance().getForClass(Interface.class);

        for(Effect e : effects)
        {
            JsonObject serializated = new JsonObject();
            serializated.addProperty("x", e.bounds.getX());
            serializated.addProperty("y", e.bounds.getY());
            object.add(e.getName(), serializated);

            e.enabled = enableWidget.isEnabled(e.state);
            if(!e.enabled)
                continue;
            Vector2f size = e.draw(event.getContext(), (int) e.bounds.getX(), (int) e.bounds.getY());

            e.bounds.setWidth(size.getX());
            e.bounds.setHeight(size.getY());
        }

        if(drawPostFx)
            RenderUtil.getPostBloomShader().draw(event.getContext().getMatrices());
    }

    private void drawBeginPrivate(Draw2DEvent event)
    {
        boolean drawPostFx = postfx.getValue();
        if(drawPostFx)
        {
//            System.out.println(RenderUtil.getPreBloomShader().drawCalls.size());
            RenderUtil.getPreBloomShader().draw(event.getContext().getMatrices());
            RenderUtil.getBlurShader().draw(event.getContext().getMatrices());
        }
    }

    public final void registerEffects(final InterfaceEntry... effects)
    {
        this.effects.addAll(Arrays.stream(effects).map(ef -> ef.effect).toList());
        for(InterfaceEntry e : effects)
        {
            e.effect.bounds = new Bounds(0,0,0,0);
            e.effect.state = e.enableSupplier;

            PropertyRepository.getInstance().addProperties(this, e.effect.getProperties());
        }
    }

    public ISimpleEffectRenderer getWidgetStyle()
    {
        return widgetStyle.getSingleValue().getRenderer();
    }

    public static Themes getTheme()
    {
        Interface module = ModuleRepository.getInstance().getModule(Interface.class);
        return module.theme.getSingleValue();
    }

    public static IFontRenderer getFont()
    {
        Interface module = ModuleRepository.getInstance().getModule(Interface.class);
        return FontRepository.getInstance().getFont(module.globalFont.getSingleValue().getName());
    }

    public static float getFontFixedHalfHeight()
    {
        Interface module = ModuleRepository.getInstance().getModule(Interface.class);
        return module.globalFont.getSingleValue().getFixedHalfHeight();
    }

    public static ISimpleEffectRenderer getEffectRenderer() {
        Interface module = ModuleRepository.getInstance().getModule(Interface.class);
        return module.getWidgetStyle();
    }

    public void drawEdges(MatrixStack stack)
    {
    /*

        for(Effect e : effects)
        {
            float thickness = 1;
            RenderUtil.drawRect(stack, e.bounds.getX(), e.bounds.getY(), thickness, e.bounds.getHeight(), Color.yellow);
            RenderUtil.drawRect(stack, e.bounds.getX(), e.bounds.getY(), e.bounds.getWidth(), thickness, Color.yellow);
            RenderUtil.drawRect(stack, e.bounds.getX() + e.bounds.getWidth() - thickness, e.bounds.getY(), thickness, e.bounds.getHeight(), Color.yellow);
            RenderUtil.drawRect(stack, e.bounds.getX(), e.bounds.getY() + e.bounds.getHeight() - thickness, e.bounds.getWidth(), thickness, Color.yellow);
        }*/
    }

}
