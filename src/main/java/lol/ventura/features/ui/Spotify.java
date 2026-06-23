package lol.ventura.features.ui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lol.ventura.features.modules.render.Interface;
import lol.ventura.features.shaders.BloomShader;
import lol.ventura.foundation.property.Property;
import lol.ventura.foundation.ui.Effect;
import lol.ventura.misc.animation.Animation;
import lol.ventura.misc.font.IFontRenderer;
import lol.ventura.misc.render.RenderUtil;
import lol.ventura.misc.spotify.SongPullThread;
import lol.ventura.misc.spotify.SpotifyService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public class Spotify extends Effect {
    @Override
    public String getName() {
        return "Spotify";
    }

    public Animation timeAnimation = new Animation();
    public String prevAlbumId = "";
    public String prevSongId = "";
    public NativeImageBackedTexture thumbnail = null;
    public float authorOffset = 0, titleOffset = 0;
    public float authorDirection = 1, titleDirection = 1;
    private BloomShader bloomShader;

    @Override
    public List<Property> getProperties() {
        return List.of();
    }

    private String msToMinutesSeconds(long ms) {
        long totalSeconds = ms / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    //tak ta java nie ma wbudowanego clampa xdddddddddddddd
    public static float clamp(float value, float min, float max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    @Override
    public Vector2f draw(DrawContext context, int x, int y) {
        if(bloomShader == null)
            bloomShader = new BloomShader();

        Map<@NotNull Character, @NotNull Character> diacritics = Map.of(
                'ą', 'a',
                'ć', 'c',
                'ę', 'e',
                'ł', 'l',
                'ń', 'n',
                'ó', 'o',
                'ś', 's',
                'ż', 'z',
                'ź', 'z'
        );

        if(!SpotifyService.isInitialized())
            return new Vector2f(0,0);


        IFontRenderer font = Interface.getFont();
        SongPullThread pull = SpotifyService.getSongPull();
        if(pull.getTrack() == null || pull.getTrackDetails() == null)
            return new Vector2f(0,0);

        String fixedTitle = pull.getTrack().getName();
       // for(var entry : diacritics.entrySet())
       // {
       //     fixedTitle = fixedTitle.replace(entry.getKey(), entry.getValue());
       // }

        if(!pull.getTrackDetails().getId().equalsIgnoreCase(prevSongId))
        {
            titleDirection = 1;
            authorDirection = 1;
            titleOffset = 0;
            authorOffset = 0;
        }

        if (!pull.getTrackDetails().getAlbum().getId().equalsIgnoreCase(prevAlbumId)) {
            if (thumbnail != null)
                thumbnail.close();


            String coverImageUrl = pull.getTrackDetails().getAlbum().getImages()[0].getUrl();

            try {
                URL coverUrl = new URL(coverImageUrl);
                HttpURLConnection httpConnection = (HttpURLConnection) coverUrl.openConnection();
                httpConnection.setRequestMethod("GET");

                try (InputStream stream = httpConnection.getInputStream()) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    BufferedImage coverImage = ImageIO.read(stream);
                    ImageIO.write(coverImage, "png", outputStream);

                    ByteBuffer buffer = BufferUtils.createByteBuffer(outputStream.toByteArray().length).put(outputStream.toByteArray());
                    buffer.flip();

                    thumbnail = new NativeImageBackedTexture(NativeImage.read(buffer));
                    thumbnail.setFilter(true, true);

                    String texturePath = "album_" + pull.getTrackDetails().getAlbum().getId();
                    MinecraftClient.getInstance().execute(() ->
                            MinecraftClient.getInstance().getTextureManager().registerTexture(Identifier.of("ventura", texturePath), thumbnail)
                    );
                }
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }




        float thickness = 1;
        float width = 196;
        float height = 48;
        Interface.getEffectRenderer().drawBackground(context, new Vector2f(x,y), width,height);

        bloomShader.drawCalls.add((ctx) -> {
            RenderSystem.setShaderTexture(0, thumbnail.getGlId());
            RenderUtil.drawTexturedRect(ctx, x+6,y+6,48-6-6,48-6-6);
        });
        bloomShader.draw(context.getMatrices());

        //RenderUtil.drawRoundedRect(x+6,y+6,48-6-6,48-6-6,4,new Color(255,255,255));
        RenderUtil.drawTexturedRoundedRect(x+6,y+6,48-6-6,48-6-6,6, thumbnail.getGlId());

        /*
        if(bloomedThumbnail.getValue())
        {
            RenderUtil.drawPostBloom((ctx) -> {
                RenderSystem.setShaderTexture(0, thumbnail.getGlId());
                RenderUtil.drawTexturedRect(ctx, x+6,y+6,48-6-6,48-6-6);
            });
        } else {
            RenderUtil.drawPreBloom((ctx) -> {
                RenderSystem.setShaderTexture(0, thumbnail.getGlId());
                RenderUtil.drawTexturedRect(ctx, x+6,y+6,48-6-6,48-6-6);
            });
        }*/

        StringBuilder authorsBuilder = new StringBuilder();
        for(int i = 0; i < pull.getTrackDetails().getArtists().length; i++)
        {
            authorsBuilder.append(pull.getTrackDetails().getArtists()[i].getName());

            if(i != pull.getTrackDetails().getArtists().length-1)
                authorsBuilder.append(", ");
        }
        String authors = authorsBuilder.toString();
        //for(var entry : diacritics.entrySet())
       // {
       //     authors = authors.replace(entry.getKey(), entry.getValue());
//        }


        boolean useTitleMarquee = font.getWidth(fixedTitle, 8) > 100;

        RenderUtil.drawPostBloom((ctx) -> {
            //do this in func soon
            GlStateManager._enableScissorTest();
            final int scaling = (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
            GlStateManager._scissorBox((int) ((x+6+48) * scaling), (int) ((MinecraftClient.getInstance().getWindow().getScaledHeight() - (y + height)) * scaling), (int) ((width - 6-48-10) * scaling), (int) (height * scaling));
            /// ////////////

            font.drawString(fixedTitle, x+6+48 - (useTitleMarquee ? clamp(titleOffset,0,font.getWidth(fixedTitle, 8) - 70) : 0),y+6,10,Color.white, context);

            GlStateManager._disableScissorTest();
        });

        //do this in func soon
        GlStateManager._enableScissorTest();
        final int scaling = (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
        GlStateManager._scissorBox((int) ((x+6+48) * scaling), (int) ((MinecraftClient.getInstance().getWindow().getScaledHeight() - (y + height)) * scaling), (int) ((width - 6-48-10) * scaling), (int) (height * scaling));
        /// ////////////





        font.drawString(fixedTitle, x+6+48 - (useTitleMarquee ? clamp(titleOffset,0,font.getWidth(fixedTitle, 8) - 90) : 0),y+6,10,Color.white, context);

        titleOffset += 0.075f *  titleDirection;

        if(titleOffset > font.getWidth(fixedTitle, 8) - 20)
        {
            titleDirection = -1;
        } else if(titleOffset < -30)
        {
            titleDirection = 1;
        }


        GlStateManager._disableScissorTest();

        //do this in func soon
        GlStateManager._enableScissorTest();
        GlStateManager._scissorBox((int) ((x+6+48) * scaling), (int) ((MinecraftClient.getInstance().getWindow().getScaledHeight() - (y + height)) * scaling), (int) ((width - 6-48-10) * scaling), (int) (height * scaling));
        /// ////////////

        boolean useAuthorMarquee = font.getWidth(authors.toString(), 8) > 150;

        font.drawString(authors.toString(),x+6+48 - (useAuthorMarquee ? clamp(authorOffset, 0, font.getWidth(authors.toString(), 8) - 145) : 0 ),y+18,8,new Color(211, 211, 211), context);

        if(useAuthorMarquee)
        {
            authorOffset += 0.075f * authorDirection;

            if(authorOffset > font.getWidth(authors.toString(), 8) - 100)
            {
                authorDirection = -1;
            } else if(authorOffset < -30)
            {
                authorDirection = 1;
            }
        }

        GlStateManager._disableScissorTest();

        timeAnimation.update();
        timeAnimation.animate(((float) pull.getProgress() / pull.getTrack().getDurationMs()) * 85, 1);

        float progress = (float) timeAnimation.getValue();


        String duration = msToMinutesSeconds(pull.getProgress()) + " / " + msToMinutesSeconds(pull.getTrack().getDurationMs());

//        RenderUtil.drawGradientRoundedRect(x+6+48 + 140 - font.getWidth(duration,8) - 0.5f - 5,y+37.5f-2,font.getWidth(duration,8) + 8,10+4,2,new Color(195, 195, 195,120),new Color(80, 81, 85,120));

//        font.drawString(duration, x+6+48 + 140 - font.getWidth(duration,8) - 0.5f,y+37.5f,8,new Color(255,255,255,150), context);

        RenderUtil.drawRoundedRect(x+6+48,y+35,85,4,1,new Color(89,88,88));
        RenderUtil.drawRoundedRect(x+6+48,y+35,progress,4,1,Color.white);

        font.drawString(duration, x+6+48 + 140 - font.getWidth(duration,8) -3,y+32f,8,new Color(255,255,255,150), context);

        prevAlbumId = pull.getTrackDetails().getAlbum().getId();
        prevSongId = pull.getTrackDetails().getId();

        return new Vector2f(width, height);
    }
}
