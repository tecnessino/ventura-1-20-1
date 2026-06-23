package lol.ventura.misc.render;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lol.ventura.foundation.Service;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.lwjgl.opengl.GL46;

import java.util.List;

public class FramebufferManager extends Service {
    @Getter @Setter
    private static FramebufferManager instance = null;

    private int screenWidth = 0, screenHeight = 0;
    private int framebufferIndex = 0;
    private int halfResFramebufferIndex = 0;
    private final Multimap<Boolean, FramebufferEntry> framebuffers;

    @AllArgsConstructor @Getter @Setter
    public static class FramebufferEntry {
        private SimpleFramebuffer framebuffer;
        private boolean halfResolution;
    }

    public FramebufferManager()
    {
        this.framebuffers = ArrayListMultimap.create();
    }

    private SimpleFramebuffer create(boolean halfRes)
    {
        int width = halfRes ? screenWidth / 2 : screenWidth;
        int height = halfRes ? screenHeight / 2 : screenHeight;

        if (width <= 0 || height <= 0) {
            throw new RuntimeException("Attempted to allocate framebuffer with invalid dimensions: " + width + "x" + height);
        }

        SimpleFramebuffer fb = new SimpleFramebuffer(width, height, true, true);

        if (fb.fbo == -1) {
            throw new RuntimeException("Failed to create framebuffer: fbo == -1");
        }

        fb.setClearColor(0,0,0,0);

        return fb;
    }

    private void allocate(boolean halfRes)
    {
        this.framebuffers.put(halfRes, new FramebufferEntry(create(halfRes), halfRes));
    }

    public void handleResize(int width, int height)
    {
        this.screenWidth = width;
        this.screenHeight = height;

        for(FramebufferEntry entry : framebuffers.values())
        {
            entry.getFramebuffer().delete();
            entry.setFramebuffer(create(entry.isHalfResolution()));
        }
    }

    public void scanForBrokenFramebuffers()
    {
        //nie wiem czemu ale nie ktore fb sie rozpiedalaja same z sieibe??
        for(FramebufferEntry entry : framebuffers.values())
        {
            if(!GL46.glIsFramebuffer(entry.getFramebuffer().fbo))
            {
                entry.getFramebuffer().delete();
                entry.setFramebuffer(create(entry.isHalfResolution()));
            }
        }
    }

    public FramebufferEntry getFramebuffer(boolean halfRes)
    {
        List<FramebufferEntry> halfResBuffers = this.framebuffers.get(true).stream().toList();
        List<FramebufferEntry> buffers = this.framebuffers.get(false).stream().toList();

        if (halfRes && halfResFramebufferIndex >= halfResBuffers.size())
        {
            allocate(true);
            halfResBuffers = this.framebuffers.get(true).stream().toList();
        }

        if (!halfRes && framebufferIndex >= buffers.size())
        {
            allocate(false);
            buffers = this.framebuffers.get(false).stream().toList();
        }

        FramebufferEntry entry = null;

        if(halfRes)
            entry =  halfResBuffers.get(halfResFramebufferIndex++);
        else
            entry = buffers.get(framebufferIndex++);

        return entry;
    }
}
