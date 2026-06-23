package lol.ventura.features.screen;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.IntComparator;
import lol.ventura.features.command.ConfigCommand;
import lol.ventura.features.modules.render.Interface;
import lol.ventura.features.properties.BooleanProperty;
import lol.ventura.features.properties.EnumProperty;
import lol.ventura.features.properties.MultiProperty;
import lol.ventura.features.properties.NumberProperty;
import lol.ventura.features.shaders.BlurShader;
import lol.ventura.features.ui.Spotify;
import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleRepository;
import lol.ventura.foundation.property.Property;
import lol.ventura.foundation.property.PropertyRepository;
import lol.ventura.foundation.quartz.QuartzSurface;
import lol.ventura.misc.animation.Animation;
import lol.ventura.misc.font.FontRepository;
import lol.ventura.misc.font.IFontRenderer;
import lol.ventura.misc.math.Bounds;
import lol.ventura.misc.math.Stopwatch;
import lol.ventura.misc.render.RenderUtil;
import lol.ventura.misc.render.ScaledResolution;
import lol.ventura.misc.render.StencilUtil;
import lol.ventura.misc.spotify.SpotifyService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

public class NewClickUI extends Screen implements GameAccessor {
    public NewClickUI() {
        super(Text.of("ClickUI"));

    }

    public HashMap<Category, List<Module>> categoryCache = new HashMap<>();

    public static class ModuleState {
        public Animation colorAnim = new Animation();
        public Animation expandAnimation = new Animation();
        public boolean animateToZero = false;
        public boolean expanded = false;
        public float computedSettingsHeight = 0;
        public HashMap<Property, PropertyState> properties = new HashMap<>();
    }

    public static class PropertyState {
        public Animation progression = new Animation();
        public int index = 0;
        public double x = 0;
        public double y = 0;
    }


    public static Color interpolate(Color start, Color end, float t) {
        int r = (int) (start.getRed() + t * (end.getRed() - start.getRed()));
        int g = (int) (start.getGreen() + t * (end.getGreen() - start.getGreen()));
        int b = (int) (start.getBlue() + t * (end.getBlue() - start.getBlue()));
        int a = (int) (start.getAlpha() + t * (end.getAlpha() - start.getAlpha()));
        return new Color(r, g, b, a);
    }

    public HashMap<Module, ModuleState> moduleStates = new HashMap<>();
    public ModuleState draggedSliderOwner = null;
    public NumberProperty draggedSlider = null;
    public BlurShader blur;

    public static interface IClickable {
        void onClick(double x, double y, int button);
    }

    public void drawBlurRoundedRect(MatrixStack stack, float x, float y, float width, float height, float radius)
    {
        blur.drawCalls.add(new BlurShader.DrawCall(BlurShader.PrimitiveType.RECTANGLE, radius, radius,new Bounds(x,y,width,height)));
    }

    public void drawBlurRoundedRectWithTint(MatrixStack stack, float x, float y, float width, float height, float radius, Color tint)
    {
        blur.drawCalls.add(new BlurShader.DrawCall(BlurShader.PrimitiveType.RECTANGLE, radius, radius,new Bounds(x,y,width,height)));
        RenderUtil.drawRoundedRect(x,y,width,height,radius,tint);
    }

    public HashMap<Bounds, IClickable> locations = new HashMap<>();

    protected NewClickUI(Text title) {
        super(title);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        locations.clear();


        if(categoryCache.isEmpty())
        {
            for (Category c : Category.values()) {
                List<Module> modules = new ArrayList<>(
                        ModuleRepository.getInstance().getModulesByCategory(c)
                );

                modules.sort(Comparator.comparingInt(v -> v.getDescriptor().name().length()));
                Collections.reverse(modules);

                categoryCache.put(c, modules);
            }
        }

        if(blur == null)
            blur = new BlurShader();
        
        blur.draw(context.getMatrices());

        final Color theme = Interface.getTheme().getMainColor();
        final IFontRenderer renderer = FontRepository.getInstance().getFont("poppins");
        final IFontRenderer iconRenderer = FontRepository.getInstance().getFont("icons");
        final IFontRenderer viconRenderer = FontRepository.getInstance().getFont("venturaicons");
        final float CATEGORY_WIDTH = 120;
        final float CATEGORY_OFFSET = 5;
        final double SLIDER_WIDTH = CATEGORY_WIDTH - 8 - 8 - 10;

        if (draggedSlider != null) {
            PropertyState state = draggedSliderOwner.properties.get(draggedSlider);

            double min = draggedSlider.min;
            double max = draggedSlider.max;
            double delta2 = (double) (mouseX - state.x);
            delta2 = Math.max(0, Math.min(delta2, SLIDER_WIDTH));
            double scaledValue = min + (delta2 / SLIDER_WIDTH) * (max - min);

            draggedSlider.setValue(roundToPrecision(scaledValue, draggedSlider.inc));
        }

        drawBlurRoundedRect(context.getMatrices(), 0, 0, ScaledResolution.getWidth(), ScaledResolution.getHeight(), 0);

        float x = (ScaledResolution.getWidth() * 0.5f) - ((Category.values().length * (CATEGORY_WIDTH + CATEGORY_OFFSET)) * 0.5f);
        float y = 20;

        for (Category c : Category.values()) {
            Collection<Module> modules = ModuleRepository.getInstance().getModulesByCategory(c);
            float categoryHeight = 22.5f + (modules.size() * (20 + 2.5f)) + 0.5f;

            for (Module m : categoryCache.get(c)) {
                if (!moduleStates.containsKey(m)) {
                    moduleStates.put(m, new NewClickUI.ModuleState());
                }
                ModuleState state = moduleStates.get(m);

                float computedFromSettings = 0;
                var props = PropertyRepository.getInstance().getPropertiesFromModule(m);
                for (Property p : props) {
                    computedFromSettings += 16; //For Title
                    if (p instanceof NumberProperty) {
                        computedFromSettings += 10;
                    }

                    if(p instanceof MultiProperty<?>)
                    {
                        MultiProperty<?> prop = (MultiProperty<?>) p;

                        float multiWidth = 0;
                        float multiHeight = 16;
                        for(String s : prop.getEnumConstantsString())
                        {
                            float newWidth = renderer.getWidth(s, 8) + 7;
                            if(multiWidth + newWidth >= CATEGORY_WIDTH-20)
                            {
                                multiWidth = 0;
                                multiHeight += 10;
                            }
                            multiWidth += newWidth ;
                        }

                        computedFromSettings += multiHeight;
                    }
                }

                state.computedSettingsHeight = computedFromSettings;
                categoryHeight += (float) (state.expandAnimation.getValue() * computedFromSettings);
            }

            drawBlurRoundedRectWithTint(context.getMatrices(), x, y, CATEGORY_WIDTH, categoryHeight, 4, new Color(0, 0, 0, 100));

            float headerX = x + (CATEGORY_WIDTH * 0.5f) - (renderer.getWidth(c.getName(), 10) * 0.5f) + 7;
            float headerY = y + 5;

            iconRenderer.drawString(String.valueOf(c.getIcon()), headerX - 14, headerY + 1.5f, 14, Interface.getTheme().getMainColor());
            renderer.drawString(c.getName(), headerX, headerY, 10, new Color(200, 200, 200));

            y += 20.5f;
            for (Module m : categoryCache.get(c)) {
                ModuleState state = moduleStates.get(m);

                state.colorAnim.update();
                state.colorAnim.animate(m.isEnabled() ? 0 : 1, 0.1f);
                Color tint = interpolate(new Color(theme.getRed(), theme.getGreen(), theme.getBlue(), 60), new Color(255, 255, 255, 40), (float) state.colorAnim.getValue());

                state.expandAnimation.update();
                state.expandAnimation.animate(state.expanded ? 1 : 0, 0.1f);


                //RenderUtil.drawRoundedRect(x+5,y,CATEGORY_WIDTH-5-5,20,6,new Color(255,255,255, 120));
                RenderUtil.drawGradientRoundedRect(x + 5, y, CATEGORY_WIDTH - 5 - 5, (float) (20 + (state.computedSettingsHeight * state.expandAnimation.getValue())), 4, tint, new Color(255, 255, 255, 0));

                /*
                float finalX = x;
                float finalY = y;RenderUtil.drawPostBloom((ctx) -> {
                    RenderUtil.drawGradientRoundedRect(finalX +5, finalY,CATEGORY_WIDTH-5-5,20,4,tint, new Color(255,255,255,0));
                });*/

                locations.put(new Bounds(x + 5, y, CATEGORY_WIDTH - 5 - 5, 20), (mx, my, mb) -> {
                    if (mb == GLFW.GLFW_MOUSE_BUTTON_LEFT)
                        m.toggle();
                    else
                        state.expanded = !state.expanded;
                });

                //TODO: create drawStringShadow based on that
                renderer.drawString(m.getDescriptor().name(), x + 10.5f, y + Interface.GlobalFont.POPPINS.getFixedHalfHeight() + 0.5f, 8, new Color(0, 0, 0, 60));
                renderer.drawString(m.getDescriptor().name(), x + 10, y + Interface.GlobalFont.POPPINS.getFixedHalfHeight(), 8, Color.white);

                if (!PropertyRepository.getInstance().getPropertiesFromModule(m).isEmpty()) {
                    viconRenderer.drawCharRotated('p', x + CATEGORY_WIDTH - 21.5f, y + 6, (float) (state.expandAnimation.getValue() * 90.0f), Color.white, 10, context);
                    //viconRenderer.drawString("p",x+CATEGORY_WIDTH-21.5f,y+5,10,Color.white);
                }

                glScissorBox(x, y, CATEGORY_WIDTH, 20 + (float) (state.computedSettingsHeight * state.expandAnimation.getValue()));

                float locY = y + 20;
                var props = PropertyRepository.getInstance().getPropertiesFromModule(m);
                if(((double) Math.round(state.expandAnimation.getValue() * 100) / 100) > 0.05)
                {
                    for (Property p : props) {
                        if (!state.properties.containsKey(p))
                            state.properties.put(p, new PropertyState());
                        PropertyState pState = state.properties.get(p);

                        RenderUtil.drawGradientRoundedRect(x + 8, locY, CATEGORY_WIDTH - 8 - 8, 15 + (p instanceof NumberProperty ? 10 : 0), 2, tint, new Color(255, 255, 255, 0));
                        renderer.drawString(p.getName(), x + 10f, locY + 7.5f - Interface.GlobalFont.POPPINS.getFixedHalfHeight(), 8, Color.white);

                        if (p instanceof BooleanProperty) {
                            BooleanProperty pBool = (BooleanProperty) p;
                            pState.progression.update();
                            pState.progression.animate(pBool.getValue() ? 1 : 0, 0.05f);

                            float pX = x + CATEGORY_WIDTH - 8 - 8 - 6;
                            float pY = locY + 2;
                            RenderUtil.drawRoundedRect(pX, pY, 10, 10, 3, interpolate(interpolate(theme, new Color(0, 0, 0, 0), 0.5f), theme, (float) pState.progression.getValue()));
                            FontRepository.getInstance().getFont("venturaicons").drawString("q", pX + 1, pY + 2, 8, new Color(255, 255, 255, (int) (pState.progression.getValue() * 255)));

                            locations.put(new Bounds(x + 8, locY, CATEGORY_WIDTH - 8 - 8, 15), (mx, my, mb) -> {
                                if (!state.expanded)
                                    return;

                                if (mb == GLFW.GLFW_MOUSE_BUTTON_LEFT)
                                    pBool.setValue(!pBool.getValue());
                            });
                        } else if (p instanceof EnumProperty<?>) {
                            EnumProperty<?> pEnum = (EnumProperty<?>) p;

                            float pX = x + CATEGORY_WIDTH - 8 - 8 - renderer.getWidth(pEnum.getValueAsString(), 8);
                            float pY = locY;

                            renderer.drawString(pEnum.getValueAsString(), pX, pY + 7.5f - Interface.GlobalFont.POPPINS.getFixedHalfHeight(), 8, new Color(200, 200, 200));

                            locations.put(new Bounds(x + 8, locY, CATEGORY_WIDTH - 8 - 8, 15), (mx, my, mb) -> {
                                if (!state.expanded)
                                    return;

                                if (mb == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                                    pState.index++;
                                    if (pState.index == pEnum.getEnumConstants().length) {
                                        pState.index = 0;
                                    }
                                } else {
                                    pState.index--;
                                    if (pState.index < 0) {
                                        pState.index = 0;
                                    }
                                }

                                pEnum.setValue(pState.index);
                            });
                        }  else if(p instanceof MultiProperty<?>)
                        {
                            MultiProperty<?> prop = (MultiProperty<?>) p;

                            float multiWidth = 0;
                            float multiHeight = 16;
                            for(MultiProperty<?>.Entry s : prop.getEnumConstantsPaired())
                            {
                                float newWidth = renderer.getWidth(s.text, 8) + 7;
                                if(multiWidth + newWidth >= CATEGORY_WIDTH-20)
                                {
                                    multiWidth = 0;
                                    multiHeight += 10;
                                }

                                if(prop.isEnabledWildcard(s.value))
                                {
                                    RenderUtil.drawRoundedRect(x + 10 + multiWidth - 2, locY + multiHeight, newWidth - 2, 10, 2, tint);
                                    //RenderUtil.drawGradientRoundedRect(x + 10 + multiWidth - 2, locY + multiHeight, newWidth + 4, 10, 2, tint, new Color(255, 255, 255, 0));
                                }

                                locations.put(new Bounds(x + 10 + multiWidth - 2, locY + multiHeight, newWidth + 4, 10), (mx,my,mb) -> {
                                    prop.toggleWildcard(s.value);
                                });

                                renderer.drawString(s.text, x + 10 + multiWidth, locY + multiHeight, 8, Color.white);
                                multiWidth += newWidth ;
                            }

                            locY += multiHeight;
                        }
                        else if (p instanceof NumberProperty) {
                            NumberProperty pNumber = (NumberProperty) p;
                            float tX = x + CATEGORY_WIDTH - 8 - 8 - renderer.getWidth(pNumber.getValueAsString(), 8);
                            float tY = locY;
                            renderer.drawString(pNumber.getValueAsString(), tX, tY + 7.5f - Interface.GlobalFont.POPPINS.getFixedHalfHeight(), 8, new Color(200, 200, 200));

                            float sX = x + 10;
                            float sY = locY + 17;

                            double min = pNumber.min;
                            double max = pNumber.max;
                            float value = Float.parseFloat(pNumber.getValueAsString());

                            double normalized = (value - min) / (max - min);
                            double position = normalized * SLIDER_WIDTH;

                            pState.x = sX;
                            pState.y = sY;

                            RenderUtil.drawRoundedRect(sX, sY, CATEGORY_WIDTH - 8 - 8 - 10, 2, 0.1f, interpolate(theme, new Color(0, 0, 0, 0), 0.5f));
                            RenderUtil.drawRoundedRect(sX, sY, (float) position, 2, 0.1f, theme);

                            RenderUtil.drawRoundedRect((float) (sX + position - 0.5f), sY - 2f, 6, 6, 2, Color.white);

                            locations.put(new Bounds(sX, sY - 2, CATEGORY_WIDTH - 8 - 8 - 10, 6), (mx, my, mb) -> {
                                if(!state.expanded)
                                    return;

                                draggedSliderOwner = state;
                                draggedSlider = pNumber;
                                double delta2 = (float) (mx - sX);
                                delta2 = Math.max(0, Math.min(delta2, SLIDER_WIDTH));
                                double scaledValue = min + (delta2 / SLIDER_WIDTH) * (max - min);

                                pNumber.setValue(roundToPrecision(scaledValue, pNumber.inc));
                            });

                            locY += 10;
                        }

                        locY += 16;
                    }
                }

                GlStateManager._disableScissorTest();

                y += 20 + 2.5f + (float) (state.computedSettingsHeight * state.expandAnimation.getValue());
            }

            y = 20;
            x += CATEGORY_WIDTH + CATEGORY_OFFSET;
        }

        try {
            renderConfigList(context);
        } catch (Throwable ignored)
        {
            /// $$$$$$ throwable ignored
        }

    }

    public List<Animation> animationStorage = new ArrayList<>();
    public void renderConfigList(DrawContext context) throws IOException {
        final IFontRenderer renderer = FontRepository.getInstance().getFont("poppins");
        final List<String> configs = new ArrayList<String>(ConfigCommand.getConfigList());
        final float PADDING_Y = 4;
        final float PADDING_X = 8;

        configs.sort(Comparator.comparingDouble((name -> renderer.getWidth(name, 8))));
        Collections.reverse(configs);

        float sizeX = PADDING_X * 2 + Spotify.clamp(renderer.getWidth(configs.get(0), 8), 128, Integer.MAX_VALUE);
        float sizeY = PADDING_Y * 2 + ((configs.size()+1) * 14);

        float posX = 32;
        float posY = ScaledResolution.getHeight() - sizeY - 32;

        RenderUtil.drawRoundedRect(posX,posY,sizeX,sizeY,4, new Color(0,0,0,120));

        renderer.drawString("Configs",posX + PADDING_X, posY + PADDING_Y, 8, new Color(255,255,255,120));

        //RenderUtil.drawRoundedRect(posX + PADDING_X,posY + PADDING_Y + 16-3,sizeX - (PADDING_X*2),2,0.1f,new Color(255,255,255,120));
        RenderUtil.drawGradientRoundedRect(posX + PADDING_X,posY + PADDING_Y + 14-3,sizeX - (PADDING_X*2),2, 0.1f, new Color(255,255,255,160), new Color(255,255,255,80));

        for(int i = 1; i-1 <= configs.size(); i++)
        {
            if(animationStorage.size() <= i-1)
                animationStorage.add(new Animation());

            boolean isCurrentlyLoaded = ConfigCommand.currentLoadedConfig.equalsIgnoreCase(configs.get(i-1));

            Animation animation = animationStorage.get(i-1);
            animation.update();
            animation.animate(isCurrentlyLoaded ? 255 : 180, 0.1f);

            float entryX = posX + PADDING_X;
            float entryY = posY + PADDING_Y + (i*14);

            renderer.drawString(configs.get(i-1), entryX,entryY + (14*0.5f) - Interface.GlobalFont.POPPINS.getFixedHalfHeight(), 8, new Color(255,255,255, (int) animation.getValue()));

            FontRepository.getInstance().getFont("venturaicons").drawString("a", entryX + sizeX - (PADDING_X*3), entryY + (14*0.5f) - 3.5f, 8,Color.red);
            int finalI = i;
            locations.put(new Bounds(entryX, entryY, sizeX - (PADDING_X*3), 14), (mx, my, mb) -> {
                ConfigCommand.handleLoad(new String[]{"load", configs.get(finalI -1)}, true);
            });

            locations.put(new Bounds(entryX + sizeX - (PADDING_X*3), entryY, 8, 14), (mx, my, mb) -> {
                ConfigCommand.handleDelete(new String[]{"delete", configs.get(finalI -1)}, true);
            });
        }
    }

    public static double roundToPrecision(double value, double precision) {
        double scaled = Math.round(value / precision) * precision;
        return Math.round(scaled * 1_000_000d) / 1_000_000d;
    }

    public void glScissorBox(final float x, final float y, final float width, final float height) {
        GlStateManager._enableScissorTest();

        final int scaling = (int) MinecraftClient.getInstance().getWindow().getScaleFactor();

        GlStateManager._scissorBox((int) (x * scaling), (int) ((MinecraftClient.getInstance().getWindow().getScaledHeight() - (y + height)) * scaling), (int) (width * scaling), (int) (height * scaling));
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (var entry : locations.entrySet()) {
            if (mouseX >= entry.getKey().getX()
                    && mouseY >= entry.getKey().getY()
                    && mouseX <= entry.getKey().getX() + entry.getKey().getWidth()
                    && mouseY <= entry.getKey().getY() + entry.getKey().getHeight()) {
                entry.getValue().onClick(mouseX, mouseY, button);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggedSlider = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}