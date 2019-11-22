package SkyNetJR.Graphics;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Texture {
    private int Id;
    private int Height;
    private int Width;

    public int getId(){
        return Id;
    }
    public int getWidth(){
        return Width;
    }
    public int getHeight(){
        return Height;
    }

    public Texture(String file){
        BufferedImage bi;
        try {
            bi = ImageIO.read(new File(file));

            Width = bi.getWidth();
            Height = bi.getHeight();

            int[] pixels_raw = bi.getRGB(0, 0, Width, Height, null, 0, Width);
            ByteBuffer pixels = BufferUtils.createByteBuffer(pixels_raw.length);

            for (int i = 0; i < Width; i++) {
                for (int j = 0; j < Height; j++) {
                    int pixel = pixels_raw[i * Width * j];
                    pixels.put((byte)((pixel >> 16) & 0xFF));   // Red
                    pixels.put((byte)((pixel >> 8) & 0xFF));    // Green
                    pixels.put((byte)((pixel) & 0xFF));         // Blue
                    pixels.put((byte)((pixel >> 24) & 0xFF));   // Alpha
                }
            }

            pixels.flip();

            Id = glGenTextures();
            bind();

            //Setup filtering: interpolation of pixels when scaling up or down
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            //Setup wrap mode: handle pixels outside of the expected range
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, Width, Height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void bind(){
        glBindTexture(GL_TEXTURE_2D, Id);
    }
}
