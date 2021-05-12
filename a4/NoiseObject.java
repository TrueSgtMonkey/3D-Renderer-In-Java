package a4;

import static com.jogamp.opengl.GL4.*;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.GLContext;
import org.joml.*;

import java.nio.ByteBuffer;
import java.util.Vector;

public class NoiseObject extends SceneObject
{
    //if they are null then they will not be applied
    private Vector3f translation = null, scale = null;
    private Vector4f rotation = null;
    private byte[] data;
    private double[] noise;
    private int zoom;
    private int width, height, depth;
    private java.util.Random rand = new java.util.Random();

    public NoiseObject(ImportedModel model, int width, int height, int depth, int zoom)
    {
        super(model, false, -1, -1);
        noise = new double[width * height * depth];
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.zoom = zoom;
        generateNoise();
        setBottomGear(1);
        data = new byte[width * height * depth * 4];
        setTexture(load3DTexture());
    }

    public NoiseObject(ImportedModel model, int width, int height, int depth, int zoom, int normalTexture)
    {
        super(model, false, -1, normalTexture);
        noise = new double[width * height * depth];
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.zoom = zoom;
        generateNoise();
        setBottomGear(1);
        data = new byte[width * height * depth * 4];
        setTexture(load3DTexture());
    }

    private void fillDataArray()
    {
        if(zoom < 2)
        {
            for(int i = 0; i < width; i++)
            {
                for(int j = 0; j < height; j++)
                {
                    for(int k = 0; k < depth; k++)
                    {
                        data[i * (width * height * 4) + j * (height * 4) + k*4] = (byte) (noise[i + height * (j + depth * k)] * 255);
                        data[i * (width * height * 4) + j * (height * 4) + k*4 + 1] = (byte) (noise[i + height * (j + depth * k)] * 255);
                        data[i * (width * height * 4) + j * (height * 4) + k*4 + 2] = (byte) (noise[i + height * (j + depth * k)] * 255);
                        data[i * (width * height * 4) + j * (height * 4) + k*4 + 3] = (byte)255;
                    }
                }
            }
        }
        else
        {
            for(int i = 0; i < width; i++)
            {
                for(int j = 0; j < height; j++)
                {
                    for(int k = 0; k < depth; k++)
                    {
                        data[i * (width * height * 4) + j * (height * 4) + k*4] = (byte) (noise[(i / zoom) + height * ((j / zoom) + depth * (k / zoom))] * 255);
                        data[i * (width * height * 4) + j * (height * 4) + k*4 + 1] = (byte) (noise[(i / zoom) + height * ((j / zoom) + depth * (k / zoom))] * 255);
                        data[i * (width * height * 4) + j * (height * 4) + k*4 + 2] = (byte) (noise[(i / zoom) + height * ((j / zoom) + depth * (k / zoom))] * 255);
                        data[i * (width * height * 4) + j * (height * 4) + k*4 + 3] = (byte)255;
                    }
                }
            }
        }
    }

    private int load3DTexture()
    {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        fillDataArray();
        ByteBuffer bb = Buffers.newDirectByteBuffer(data);

        int[] textureIDs = new int[1];
        gl.glGenTextures(1, textureIDs, 0);
        int textureID = textureIDs[0];

        gl.glBindTexture(GL_TEXTURE_3D, textureID);
        gl.glTexStorage3D(GL_TEXTURE_3D, 1, GL_RGBA8, width, height, depth);
        gl.glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0, width, height, depth, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, bb);
        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        return textureID;
    }

    public void generateNoise()
    {
        for(int x = 0; x < width; x++)
        {
            for(int y = 0; y < height; y++)
            {
                for(int z = 0; z < depth; z++)
                {
                    noise[x + height * (y + depth * z)] = rand.nextDouble();
                }
            }
        }
    }

    public Vector3f setTranslation(Vector3f translation)
    {
        this.translation = translation;
        return this.translation;
    }

    public Vector3f getTranslation()
    {
        return translation;
    }

    public Vector4f setRotation(Vector4f rotation)
    {
        this.rotation = rotation;
        return this.rotation;
    }

    public Vector4f getRotation()
    {
        return rotation;
    }

    public Vector3f setScale(Vector3f scale)
    {
        this.scale = scale;
        return this.scale;
    }

    public Vector3f getScale()
    {
        return scale;
    }


}
