package lol.ventura.foundation.quartz;

import lol.ventura.features.screen.ClickUI;
import lol.ventura.misc.font.FontRepository;
import lol.ventura.misc.font.IFontRenderer;
import lol.ventura.misc.math.Bounds;
import lol.ventura.misc.render.RenderUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class QuartzSurface {

    private abstract class DrawCommand
    {
        public WindowInfo owner;
        public FlexDirection direction;
        protected float offsetX;
        protected float offsetY;
        public abstract void draw(DrawContext context);
    }

    private static interface IClickable
    {
        void onClick(double x, double y, int button);
    }

    public static enum FlexDirection {
        HORIZONTAL, VERTICAL
    }

    @RequiredArgsConstructor
    private static class WindowInfo
    {
        @Getter @Setter
        private float computedHeight = 0;

        @Getter @Setter
        private int backgroundDrawCommandIndex = 0;

        @Getter
        private final String title;

        @Getter @Setter
        private Vector2f location = new Vector2f(100,100);

        @Getter @Setter
        private Vector2f offsets = new Vector2f(0,0);

        public void expandHeight(float value)
        {
            this.computedHeight += value;
        }
    }

    private ArrayList<DrawCommand> drawCommands = new ArrayList<>();
    private HashMap<String, WindowInfo> windowInfos = new HashMap<>();
    private WindowInfo currentWindow = null;
    private Stack<FlexDirection> directionStack = new Stack<>();
    private HashMap<Bounds, IClickable> locations = new HashMap<>();
    private WindowInfo draggingWindow = null;
    private Vector2f dragOffset = new Vector2f(0,0);
    private Vector2f mousePosition = new Vector2f(0,0);

    public void pushDirection(FlexDirection direction)
    {
        directionStack.push(direction);
    }

    public void popDirection()
    {
        directionStack.pop();
    }

    private IFontRenderer getFont()
    {
        return FontRepository.getInstance().getFont("poppins");
    }

    private FlexDirection getDirection()
    {
        return directionStack.peek();
    }

    public void addDrawCommand(DrawCommand command)
    {
        command.direction = getDirection();
        command.owner = currentWindow;
        drawCommands.add(command);
    }

    public void addDrawCommandAtIndex(DrawCommand command, int index)
    {
        command.direction = getDirection();
        command.owner = currentWindow;
        drawCommands.add(index, command);
    }

    private void render(DrawContext context)
    {
        for(DrawCommand command : drawCommands)
        {
            command.offsetX = command.owner.getOffsets().x();
            command.offsetY = command.owner.getOffsets().y();

            command.draw(context);

            command.owner.setOffsets(new Vector2f(command.offsetX, command.offsetY));
        }
    }

    public void newFrame(float mx, float my)
    {
        directionStack.push(FlexDirection.VERTICAL);
        locations.clear();
        mousePosition = new Vector2f(mx,my);

        if(draggingWindow != null)
        {
            draggingWindow.setLocation(new Vector2f(mx - dragOffset.x, my - dragOffset.y));
        }
    }

    private void precheckComponent()
    {
        if(currentWindow == null)
            throw new IllegalStateException("Cannot add components without active window");
    }

    public void text(String text, int size)
    {
        precheckComponent();
        addDrawCommand(new DrawCommand() {
            @Override
            public void draw(DrawContext context) {
                getFont().drawString(text, offsetX, offsetY , size, Color.white, context);

                switch(direction)
                {
                    case VERTICAL -> offsetY+=getFont().getHeight(size);
                    case HORIZONTAL -> offsetX+=getFont().getWidth(text, size);
                }
            }
        });

        if(getDirection() == FlexDirection.VERTICAL) currentWindow.expandHeight(getFont().getHeight(size));
    }

    public void button(String text, int textSize, Runnable onClick)
    {
        precheckComponent();
        float padding = 8;

        addDrawCommand(new DrawCommand() {
            @Override
            public void draw(DrawContext context) {
                float width = getFont().getWidth(text, textSize) + (padding * 2);
                boolean hover = mousePosition.x >= offsetX && mousePosition.y >= offsetY && mousePosition.x <= offsetX + width && mousePosition.y <= offsetY + 16;


                RenderUtil.drawRoundedRect(
                        offsetX,
                        offsetY,
                        width,
                        16,
                        4,
                        hover ? new Color(51, 49, 49) : new Color(23, 22, 22)
                );

                RenderUtil.drawOutlineRoundedRect(
                        offsetX,
                        offsetY,
                        width,
                        16,
                        4,
                        new Color(29, 34, 31),
                        1
                );

                getFont().drawString(text, offsetX + (width * 0.5f) - (getFont().getWidth(text, textSize) * 0.5f), offsetY + (16 * 0.5f) - 5, textSize, new Color(200,200,200));

                locations.put(new Bounds(offsetX,offsetY,width,16), (mx,my, mb) -> onClick.run());

                switch(direction)
                {
                    case VERTICAL -> offsetY+=16;
                    case HORIZONTAL -> offsetX+=width;
                }
            }
        });

        if(getDirection() == FlexDirection.VERTICAL) currentWindow.expandHeight(16);
    }

    public void space(float value)
    {
        precheckComponent();
        addDrawCommand(new DrawCommand() {
            @Override
            public void draw(DrawContext context) {
                switch(direction)
                {
                    case VERTICAL -> offsetY+=value;
                    case HORIZONTAL -> offsetX+=value;
                }
            }
        });

        if(getDirection() == FlexDirection.VERTICAL) currentWindow.expandHeight(value);
    }

    public void begin(String title)
    {
        if(!windowInfos.containsKey(title))
            windowInfos.put(title, new WindowInfo(title));

        WindowInfo info = windowInfos.get(title);
        info.setBackgroundDrawCommandIndex(drawCommands.size());
        info.setComputedHeight(20+5); // base = header + minimal padding

        locations.put(new Bounds(info.getLocation().x, info.getLocation().y, 200, 16), (mx,my,mb) -> {
            draggingWindow = info;
            dragOffset = new Vector2f((float) (mx - info.getLocation().x), (float) (my - info.getLocation().y));
        });

        currentWindow = info;
    }

    public void end()
    {
        addDrawCommandAtIndex(new DrawCommand() {
            @Override
            public void draw(DrawContext context) {

                RenderUtil.drawPreBloom((ctx) -> {
                    RenderUtil.drawRoundedRect(
                            owner.getLocation().x(),
                            owner.getLocation().y(),
                            200,
                            owner.getComputedHeight(),
                            4,
                            new Color(23, 22, 22)
                    );
                });

                RenderUtil.drawPostBloom((ctx) -> {
                    getFont().drawString(owner.getTitle(), owner.getLocation().x()+6, owner.getLocation().y()+10-7f, 8,new Color(200,200,200));
                });

                RenderUtil.drawRoundedRect(
                        owner.getLocation().x(),
                        owner.getLocation().y(),
                        200,
                        owner.getComputedHeight(),
                        4,
                        new Color(23, 22, 22)
                );

                RenderUtil.drawOutlineRoundedRect(
                        owner.getLocation().x(),
                        owner.getLocation().y(),
                        200,
                        owner.getComputedHeight(),
                        4,
                        new Color(29, 34, 31),
                        1
                );

                RenderUtil.drawRoundedRect(owner.getLocation().x(), owner.getLocation().y(), 200,16,4,new Color(32,31,32));
                RenderUtil.drawOutlineRoundedRect(owner.getLocation().x(), owner.getLocation().y(), 200,16,4,new Color(29, 34, 31),1);
                getFont().drawString(owner.getTitle(), owner.getLocation().x()+6, owner.getLocation().y()+10-7f, 8,new Color(200,200,200));

                /*getFont().drawString(
                        owner.getTitle(),
                        owner.getLocation().x(),
                        owner.getLocation().y(),
                        16,
                        Color.white
                );*/
            }
        }, currentWindow.getBackgroundDrawCommandIndex());

        Vector2f offsets = new Vector2f(currentWindow.getLocation());
        offsets.add(5,20);
        currentWindow.setOffsets(offsets);
        currentWindow = null;
    }


    public void onClick(Vector2f position, int action, int button)
    {
        if(action == GLFW.GLFW_RELEASE)
        {
            draggingWindow = null;
        }

        if(action == GLFW.GLFW_PRESS)
        {
            float mouseX = position.x();
            float mouseY = position.y();
            for (var entry : locations.entrySet())
            {
                if(mouseX >= entry.getKey().getX()
                        && mouseY >= entry.getKey().getY()
                        && mouseX <= entry.getKey().getX() + entry.getKey().getWidth()
                        && mouseY <= entry.getKey().getY()+entry.getKey().getHeight())
                {
                    entry.getValue().onClick(mouseX,mouseY,button);
                }
            }
        }
    }

    public void endFrame(DrawContext context)
    {
        render(context);
        drawCommands.clear();
        popDirection();
    }
}
