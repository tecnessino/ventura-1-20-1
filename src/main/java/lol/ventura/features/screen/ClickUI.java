package lol.ventura.features.screen;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.platform.GlStateManager;
import lol.ventura.features.properties.BooleanProperty;
import lol.ventura.features.properties.EnumProperty;
import lol.ventura.features.properties.NumberProperty;
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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class ClickUI extends Screen {
    public ClickUI() {
        super(Text.of("ClickUI"));

    }

    public static interface IClickable {
        void onClick(double x, double y, int button);
    }

    public static class ModuleState {
        public Animation switcher = new Animation();
        public Animation expandAnimation = new Animation();
        public Animation briefMarquee = new Animation();
        public boolean animateToZero = false;
        public boolean expanded = false;
        public HashMap<Property, PropertyState> properties = new HashMap<>();
    }

    public static class PropertyState {
        public Animation switcher = new Animation();
        public int index = 0;
    }

    public HashMap<Bounds, IClickable> locations = new HashMap<>();
    public Category selectedCategory = Category.COMBAT;
    public HashMap<Module, ModuleState> moduleStates = new HashMap<>();
    public Animation alphaNewCategory = new Animation();
    public Stopwatch caret = new Stopwatch();
    public boolean inSearchBar = false;
    public boolean caretState = false;
    public String searchTextBuffer = "";
    public Animation caretPosition = new Animation();
    public float scroll = 0, maxScroll = 0;
    QuartzSurface surface = new QuartzSurface();

    public float cx, cy;
    public boolean trzyma = false;
    public Vector2f positionOffset = new Vector2f(0, 0);

    public Animation selectedCategoryTransition = new Animation();

    protected ClickUI(Text title) {
        super(title);
        cx = (ScaledResolution.getWidth() * 0.5f) - (375 * 0.5f);
        cy = (ScaledResolution.getHeight() * 0.5f) - (120 * 0.5f);
    }

    public ArrayList<Bounds> scissors;

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (scroll >= 0 && amount == 1)
            return super.mouseScrolled(mouseX, mouseY, amount);

        scroll += (float) amount;
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        RenderUtil.getPreBloomShader().draw(context.getMatrices());
       // RenderUtil.getBlurShader().draw(context.getMatrices());

        surface.newFrame(mouseX, mouseY);

        if (SpotifyService.getSongPull().getTrackDetails() != null) {
            surface.begin("Spotify");

            StringBuilder authorsBuilder = new StringBuilder();
            for (int i = 0; i < SpotifyService.getSongPull().getTrackDetails().getArtists().length; i++) {
                authorsBuilder.append(SpotifyService.getSongPull().getTrackDetails().getArtists()[i].getName());

                if (i != SpotifyService.getSongPull().getTrackDetails().getArtists().length - 1)
                    authorsBuilder.append(", ");
            }
            String authors = authorsBuilder.toString();

            surface.text(SpotifyService.getSongPull().getTrackDetails().getName(), 12);
            surface.text(authors, 8);

            surface.pushDirection(QuartzSurface.FlexDirection.HORIZONTAL);
            surface.button("Previous", 8, () -> {
                try {
                    SpotifyService.getApi().skipUsersPlaybackToPreviousTrack().build().executeAsync();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            surface.space(2);
            surface.button("Pause", 8, () -> {
                try {
                    SpotifyService.getApi().pauseUsersPlayback().build().executeAsync();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            surface.space(2);
            surface.button("Next", 8, () -> {
                try {
                    SpotifyService.getApi().skipUsersPlaybackToNextTrack().build().executeAsync();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            surface.popDirection();
            surface.space(16);

            surface.end();
        }

        if (trzyma) {
            cx = mouseX - positionOffset.getX();
            cy = mouseY - positionOffset.getY();
        }

        locations.clear();

        RenderUtil.drawPreBloom((ctx) -> {
            RenderUtil.drawRoundedRect(cx, cy, 375, 270, 10, new Color(13, 12, 13));
            RenderUtil.drawRoundedRect(cx, cy, 120, 270, 10, new Color(16, 16, 16));
        });

        RenderUtil.drawRoundedRect(cx, cy, 375, 270, 10, new Color(13, 12, 13));
        RenderUtil.drawRoundedRect(cx, cy, 120, 270, 10, new Color(16, 16, 16));

        StencilUtil.bindWrite();

        RenderUtil.drawRoundedRectDepthed(context.getMatrices(), cx, cy, 375, 270, 10, new Color(13, 12, 13));
        RenderUtil.drawRoundedRectDepthed(context.getMatrices(), cx, cy, 120, 270, 10, new Color(16, 16, 16));

        StencilUtil.bindRead();

        glScissorBox(cx, cy, 375, 270);
        drawSideBar(cx, cy);
        drawInner(cx, cy);

        locations.put(new Bounds(cx, cy, 375, 30), (mx, my, mb) -> {
            positionOffset = new Vector2f((float) (mx - cx), (float) (my - cy));
            trzyma = true;
        });

        GlStateManager._disableScissorTest();


        RenderUtil.getPostBloomShader().draw(context.getMatrices());
        StencilUtil.disable();

        surface.endFrame(context);

    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        trzyma = false;
        surface.onClick(new org.joml.Vector2f((float) mouseX, (float) mouseY), GLFW.GLFW_RELEASE, button);

        return super.mouseReleased(mouseX, mouseY, button);
    }

    public void drawSideBar(float inX, float inY) {
        IFontRenderer font = FontRepository.getInstance().getFont("roboto");
        IFontRenderer icons = FontRepository.getInstance().getFont("icons");
        float x = inX + 15;
        float y = inY + 15;

        float finalY = y;
        RenderUtil.drawPostBloom((ctx) -> {
            font.drawString("Ventura", x, finalY, 12, Color.white);
        });
        font.drawString("Ventura", x, y, 12, Color.white);

        y += 20;

        RenderUtil.drawRoundedRect(x, y, 90, 20, 5, new Color(13, 12, 13));

        if (inSearchBar) {
            selectedCategory = null; //null means search is active

            if (caret.elapsed(500)) {
                caretState = !caretState;
                caret.reset();
            }

            caretPosition.update();
            caretPosition.animate(font.getWidth(searchTextBuffer, 8), 0.1f);

            if (caretState)
                RenderUtil.drawRoundedRect((float) (x + caretPosition.getValue()) + 10 + 0.5f, y + 10 - (font.getHeight(8) * 0.5f), 2, font.getHeight(8), 1, new Color(68, 69, 68));

            font.drawString(searchTextBuffer, x + 10, y + 10 - (font.getHeight(8) * 0.5f), 8, Color.white);
        } else {
            font.drawString("Search", x + 10, y + 10 - (font.getHeight(8) * 0.5f), 8, new Color(68, 69, 68));
        }

        locations.put(new Bounds(x, y, 90, 20), (mx, my, mb) -> {
            inSearchBar = true;
        });

        y += 40;

        float offset = y;
        for (Category category : Category.values()) {
            selectedCategoryTransition.update();
            selectedCategoryTransition.animate(255 - 115, 0.2f);

            icons.drawString(String.valueOf(category.getIcon()), x, offset + 1, 16,
                    selectedCategory == category ? new Color(255, 255, 255, 115 + (int) selectedCategoryTransition.getValue()) : new Color(115, 114, 115));
            // RenderUtil.drawRect(new MatrixStack(), x,offset, 10,10,
            //        selectedCategory == category ? new Color(255,255,255,115+(int) selectedCategoryTransition.getValue())  : new Color(115,114,115));
            font.drawString(category.getName(), x + 15, offset + 1.5f, 9,
                    selectedCategory == category ? new Color(255, 255, 255, 115 + (int) selectedCategoryTransition.getValue()) : new Color(115, 114, 115));

            locations.put(new Bounds(x, offset, 120 - 15, 10), (mx, my, button) -> {
                if (selectedCategory == category)
                    return;

                selectedCategory = category;
                selectedCategoryTransition = new Animation();
                alphaNewCategory = new Animation();
            });

            offset += 20;
        }
    }

    public void drawInner(float inX, float inY) {
        IFontRenderer font = FontRepository.getInstance().getFont("roboto");
        float x = inX + 120 + 15;
        float y = inY + 15;

        Collection<Module> md = selectedCategory != null ? ModuleRepository.getInstance().getModulesByCategory(selectedCategory) : null;

        if (md == null) {
            md = ModuleRepository.getInstance().getModules().stream().filter((module) -> module.getDescriptor().name().replaceAll(" ", "").toLowerCase().contains(searchTextBuffer.toLowerCase())).toList();
        }

        font.drawString(selectedCategory == null ? "Search" : selectedCategory.getName(), x, y, 12, new Color(89, 88, 88));

        y += scroll * 25;
        y += 20;

        ArrayList<Float> offsets = new ArrayList<>();
        ArrayList<Float> xes = new ArrayList<>();
        Collections.addAll(offsets, y, y);
        Collections.addAll(xes, x, x + 115 + 2.5f);
        int column = 0;
        for (Module m : md) {
            float off = offsets.get(column);

            off += drawModule(m, xes.get(column), off);

            offsets.set(column, off);
            if (column == 0)
                column = 1;
            else column = 0;
        }

        alphaNewCategory.update();
        alphaNewCategory.animate(255 - 100, 0.1f);
        RenderUtil.drawRoundedRect(inX + 120, inY, 375 - 120, 270, 6, new Color(13, 12, 13, (int) (255 - 100 - alphaNewCategory.getValue())));
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (inSearchBar)
            searchTextBuffer += chr;

        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && inSearchBar) {
            selectedCategory = Category.COMBAT;
            inSearchBar = false;
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        if (inSearchBar) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchTextBuffer.isEmpty())
                searchTextBuffer = searchTextBuffer.substring(0, searchTextBuffer.length() - 1);
        }

        return true;
        //return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public static float roundToPrecision(float value, float precision) {
        return Math.round(value / precision) * precision;
    }

    public float drawModule(Module m, float inX, float inY) {
        IFontRenderer font = FontRepository.getInstance().getFont("roboto");

        if (!moduleStates.containsKey(m)) {
            moduleStates.put(m, new ModuleState());
        }

        ModuleState state = moduleStates.get(m);

        float computedFromSettings = 45;
        float dziwka = 30;

        ArrayList<String> lines = new ArrayList<>();

        for (int i = 0; i < m.getDescriptor().brief().length(); i += 25) {
            int rest = m.getDescriptor().brief().length() - i;

            String s = m.getDescriptor().brief().substring(i, i + (Math.min(rest, 25)));
            lines.add(s.trim());
            dziwka += 10;
            computedFromSettings += 10;
        }

        var props = PropertyRepository.getInstance().getPropertiesFromModule(m);
        for (Property p : props) {
            computedFromSettings += 15; //For Title

            //this works for boolean and enum.

            if (p instanceof NumberProperty) {
                // to znaczy ze yoda byl zydem
                computedFromSettings += 10;
            }

            //color currently unimplemneted
        }

        if (!props.isEmpty())
            computedFromSettings -= 8;

        state.expandAnimation.update();
        state.expandAnimation.animate(state.expanded ? computedFromSettings : dziwka, 0.1f);

        float computeHeight = (float) state.expandAnimation.getValue();
        boolean matkapjeseks = computeHeight > dziwka + 5;

        glScissorBox(inX, inY, 115, computeHeight);

        RenderUtil.drawRoundedRect(inX, inY, 115, computeHeight, 5, new Color(16, 16, 16));

        locations.put(new Bounds(inX, inY, 115, 30), (mx, my, mb) -> {
            if (mb == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                state.expanded = !state.expanded;
            }
        });

        float x = inX + 8;
        float y = inY + 8;


        font.drawString(m.getDescriptor().name(), x, y, 10, Color.white);

        kurwa(state.switcher, x + 115 - 35, y, m::isEnabled, m::toggle, true);

        y += 16;

        for (String line : lines) {
            font.drawString(line, x, y, 8, new Color(115, 114, 115));
            y += 10;
        }

        if (state.briefMarquee.getValue() == m.getDescriptor().brief().length()) {
            state.animateToZero = true;
        } else if (state.briefMarquee.getValue() == 0) {
            state.animateToZero = false;
        }
        y += 8;

        if (computeHeight > 40) {
            if (props.isEmpty())
                font.drawString("No settings for you", x, y, 8, new Color(112, 111, 112));

            for (Property p : props) {
                if (!state.properties.containsKey(p))
                    state.properties.put(p, new PropertyState());

                PropertyState state1 = state.properties.get(p);

                font.drawString(p.getName(), x, y, 8, Color.white);

                if (p instanceof BooleanProperty) {
                    BooleanProperty booleanProperty = (BooleanProperty) p;

                    if (matkapjeseks) {
                        kurwa(state1.switcher, x + 115 - 35, y, booleanProperty::getValue, booleanProperty::toggle, true);
                    } else {
                        state1.switcher.update();
                        state1.switcher.animate(booleanProperty.getValue() ? 1 : 0, 0.1f);

                        RenderUtil.drawRoundedRect(x + 115 - 35, y, 22, 11, 4, new Color(13, 12, 17));
                        RenderUtil.drawRoundedRect((float) (x + 115 - 35 + (state1.switcher.getValue() * 10)), y + 1, 10, 10, 4, new Color(
                                255, 255, 255, (int) (33 + (state1.switcher.getValue() * (255 - 33)))
                        ));
                    }
                }

                if (p instanceof EnumProperty<?>) {
                    EnumProperty<?> property = (EnumProperty<?>) p;

                    float w = font.getWidth(property.getValueAsString(), 8);
                    float x2 = x + 99 - w;

                    state1.switcher.update();
                    state1.switcher.animate(x2, 0.1f);

                    RenderUtil.drawRoundedRect((float) (state1.switcher.getValue() - 4), y - 2, w + 8, font.getHeight(8) + 2, 4, new Color(15, 15, 15));

                    font.drawString(property.getValueAsString(),
                            (float) state1.switcher.getValue(),
                            y, 8, new Color(154, 154, 154));

                    if (matkapjeseks) {
                        locations.put(new Bounds((float) (state1.switcher.getValue() - 4), y - 2, w + 8, font.getHeight(8) + 2), (mx, my, mb) -> {
                            if (mb == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                                state1.index++;
                                if (state1.index == property.getEnumConstants().length) {
                                    state1.index = 0;
                                }
                            } else {
                                state1.index--;
                                if (state1.index < 0) {
                                    state1.index = 0;
                                }
                            }

                            property.setValue(state1.index);
                        });
                    }
                }

                if (p instanceof NumberProperty) {
                    NumberProperty numberProperty = (NumberProperty) p;
                    font.drawString(numberProperty.getValueAsString(),
                            x + 99 - font.getWidth(numberProperty.getValueAsString(), 8),
                            y, 8, new Color(154, 154, 154));
                    y += 10;

                    RenderUtil.drawRoundedRect(x, y, 115 - 15, 5, 2, new Color(93, 56, 231));

                    /*float min = numberProperty.min;
                    float max = numberProperty.max;
                    float value = Float.parseFloat(numberProperty.getValueAsString());

                    float normalized = (value - min) / (max - min);
                    float position = normalized * 95.0f;

                    state1.switcher.update();
                    state1.switcher.animate(position, 0.1f);

                    float finalY = y;
                    RenderUtil.drawPostBloom((ctx) -> {
                        if (state.expanded)
                            RenderUtil.drawRoundedRect((float) (x + state1.switcher.getValue()), finalY - 1, 7, 7, 2.5f, Color.white);
                    });
                    RenderUtil.drawRoundedRect((float) (x + state1.switcher.getValue()), y - 1, 7, 7, 2.5f, Color.white);

                    if (matkapjeseks) {
                        locations.put(new Bounds(x, y, 115 - 15, 5), (mx, my, mb) -> {
                            float delta = (float) (mx - x);
                            delta = Math.max(0, Math.min(delta, 95));
                            float scaledValue = min + (delta / 95.0f) * (max - min);

                            numberProperty.setValue(roundToPrecision(scaledValue, numberProperty.inc));
                        });
                    }*/
                }

                y += 15f;
            }
        }
        GlStateManager._disableScissorTest();
        return computeHeight + 5;
    }

    public void kurwa(Animation animator, float sx, float sy, Supplier<Boolean> jadlemgowno, Runnable click, boolean allowClicking)
    //Draw switcher
    {
        Color accent = new Color(255, 0, 0);

        RenderUtil.drawRoundedRect(sx, sy, 22, 11, 4, new Color(13, 12, 17));

        animator.update();
        animator.animate(Boolean.TRUE.equals(jadlemgowno.get()) ? 1 : 0, 0.1f); //im using this animation for few effects. 1 is optimal, we can scale it to different values.

        RenderUtil.drawPostBloom((ctx) -> {
            if (allowClicking)
                RenderUtil.drawRoundedRect((float) (sx + (animator.getValue() * 10)), sy + 1, 10, 10, 4, new Color(
                        255, 255, 255, (int) (33 + (animator.getValue() * (255 - 33)))
                ));
        });

        RenderUtil.drawRoundedRect((float) (sx + (animator.getValue() * 10)), sy + 1, 10, 10, 4, new Color(
                255, 255, 255, (int) (33 + (animator.getValue() * (255 - 33)))
        ));

        if (allowClicking) {
            locations.put(new Bounds(sx, sy, 22, 11), (mx, my, mb) -> {
                if (mb != GLFW.GLFW_MOUSE_BUTTON_LEFT)
                    return;
                click.run();
            });
        }
    }


    public void glScissorBox(final float x, final float y, final float width, final float height) {
        GlStateManager._enableScissorTest();

        final int scaling = (int) MinecraftClient.getInstance().getWindow().getScaleFactor();

        GlStateManager._scissorBox((int) (x * scaling), (int) ((MinecraftClient.getInstance().getWindow().getScaledHeight() - (y + height)) * scaling), (int) (width * scaling), (int) (height * scaling));
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        surface.onClick(new org.joml.Vector2f((float) mouseX, (float) mouseY), GLFW.GLFW_PRESS, button);

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
    public boolean shouldPause() {
        return false;
    }
}