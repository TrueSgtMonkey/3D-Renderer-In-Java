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

    /**
     * A SceneObject that has its texture calculated as a noise texture when it is constructed.
     * @param model - the model that the NoiseObject will use.
     * @param width - the noise width of the Noise Texture
     * @param height - the noise height of the Noise Texture
     * @param depth - the noise depth of the Noise Texture
     * @param zoom - How large the blocks of the noise will appear to us on the objects
     */
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

    /**
     * A SceneObject that has its texture calculated as a noise texture when it is constructed. Is built with a Normal
     * Texture as well
     * @param model - the model that the NoiseObject will use.
     * @param width - the noise width of the Noise Texture
     * @param height - the noise height of the Noise Texture
     * @param depth - the noise depth of the Noise Texture
     * @param zoom - How large the blocks of the noise will appear to us on the objects
     * @param normalTexture - A normal that will be applied on top of the Noise Texture
     */
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

    /**
     * Gives random values to all of the spots in the noise array built into the class
     */
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

    /**
     * Sets the location of the object.
     * @param translation - the location to either change or set to
     * @return - The updated location of the object. Copying the JOML functions
     */
    public Vector3f setTranslation(Vector3f translation)
    {
        this.translation = translation;
        return this.translation;
    }

    /**
     * Gets the current location of the object.
     * @return - the location of the object
     */
    public Vector3f getTranslation()
    {
        return translation;
    }

    /**
     * Sets the Vector4f version of the rotation and returns the result.
     * @param rotation - x val is the float radians that we are rotating our object by, and the y, z, and w values are
     *                 the axes that we are turning on
     * @return the rotation (Vector4f) - x val is the float radians that we are rotating our object by, and the y, z, and w values are
    the axes that we are turning on
     */
    public Vector4f setRotation(Vector4f rotation)
    {
        this.rotation = rotation;
        return this.rotation;
    }

    /**
     *
     * @return the rotation (Vector4f) - x val is the float radians that we are rotating our object by, and the y, z, and w values are
     *     the axes that we are turning on
     */
    public Vector4f getRotation()
    {
        return rotation;
    }

    /**
     * Set the scale as a Vector3f and return the result
     * @param scale - the vector3f you pass in to scale the object
     * @return the result of the new scaling
     */
    public Vector3f setScale(Vector3f scale)
    {
        this.scale = scale;
        return this.scale;
    }

    /**
     * get the scale as a Vector3f
     * @return the scale as a vector3f
     */
    public Vector3f getScale()
    {
        return scale;
    }
}
