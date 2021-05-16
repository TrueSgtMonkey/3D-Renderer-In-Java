package a4;

import com.jogamp.common.nio.Buffers;
import java.nio.*;
import java.util.Vector;

import com.jogamp.opengl.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.GLContext;
import org.joml.*;

/** A barebones 3D object in which you have to specify the 3D object in terms 
	of float arrays */
public class HandMadeObject
{
	private float[] vertCoord;
	private float[] textCoord;
	private float[] normCoord;
	private float[] tanCoord;
	private int vao[] = new int[1];
	private int vbo[] = new int[4];
	private float maxVal = 0.0f;

	/**
	 * Initializes a basic object with no texture coordinates, normal coordinates, or tangent coordinates
	 * @param vCoord - Our vertex coordinates that will be passed into the shaders.
	 */
	public HandMadeObject(Float[] vCoord)
	{
		vertCoord = new float[vCoord.length];
		for(int i = 0; i < vCoord.length; i++)
		{
			vertCoord[i] = vCoord[i];
		}
		setupVBO();
		initBuffer(0, vertCoord);
	}
	
	private void setupVBO()
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);
	}

	/**
	 *
	 * Initializes a basic object with no normal coordinates nor tangent coordinates
	 * @param vCoord Our vertex coordinates that will be passed into the shaders.
	 * @param tCoord Our texture coordinates that will be passed into the shaders.
	 * @param tiled Scales the texture to our object's size
	 */
	public HandMadeObject(Float[] vCoord, Float[] tCoord, boolean tiled)
	{
		vertCoord = new float[vCoord.length];
		textCoord = new float[tCoord.length];
		for(int i = 0; i < vCoord.length; i++)
		{
			vertCoord[i] = vCoord[i];
			newMax(vCoord[i]);
		}
		for(int i = 0; i < tCoord.length; i++)
		{
			textCoord[i] = tCoord[i];
			if(tiled)
				textCoord[i] *= maxVal;
		}
		setupVBO();
		initBuffer(0, vertCoord);
		initBuffer(1, textCoord);
	}

	/**
	 * Initializes a basic object with no tangent coordinates
	 * @param vCoord Our vertex coordinates that will be passed into the shaders.
	 * @param tCoord Our texture coordinates that will be passed into the shaders.
	 * @param nCoord Our normal coordinates that will be passed into the shaders.
	 * @param tiled Scales the texture to our object's size
	 */
	public HandMadeObject(Float[] vCoord, Float[] tCoord, Float[] nCoord, boolean tiled)
	{
		vertCoord = new float[vCoord.length];
		textCoord = new float[tCoord.length];
		normCoord = new float[nCoord.length];
		for(int i = 0; i < vCoord.length; i++)
		{
			vertCoord[i] = vCoord[i];
			newMax(vCoord[i]);
		}
		for(int i = 0; i < tCoord.length; i++)
		{
			textCoord[i] = tCoord[i];
			if(tiled)
				textCoord[i] *= maxVal;
		}
		for(int i = 0; i < nCoord.length; i++)
			normCoord[i] = nCoord[i];
		setupVBO();
		initBuffer(0, vertCoord);
		initBuffer(1, textCoord);
		initBuffer(2, normCoord);
	}
	
	private void newMax(float num)
	{
		if(num < 0.0f)
			num *= -1.0f;
		if(maxVal < num)
			maxVal = num;
	}

	/**
	 * Initializes a basic object with no tangent coordinates
	 * @param vCoord Our vertex coordinates that will be passed into the shaders.
	 * @param tCoord Our texture coordinates that will be passed into the shaders.
	 * @param nCoord Our normal coordinates that will be passed into the shaders.
	 * @param tiled Scales the texture to our object's size
	 */
	public HandMadeObject(float[] vCoord, float[] tCoord, float[] nCoord, boolean tiled)
	{
		vertCoord = vCoord;
		for(int i = 0; i < vCoord.length; i++)
			newMax(vCoord[i]);
		textCoord = tCoord;
		if(tiled)
			for(int i = 0; i < textCoord.length; i++)
				textCoord[i] *= maxVal;
		normCoord = nCoord;
		setupVBO();
		initBuffer(0, vertCoord);
		initBuffer(1, textCoord);
		initBuffer(2, normCoord);
	}

	/**
	 * Used for .obj imports. Only type of constructor with this class that will enable normal mapping
	 * @param numVerts - the number of vertices in the object
	 * @param verts - the vertex coordinates created with an ImportedModel class
	 * @param textures - the texture coordinates created with an ImportedModel class
	 * @param norms - the normal coordinates created with an ImportedModel class
	 * @param tiled - Scales the texture to our object's size
	 */
	public HandMadeObject(int numVerts, Vector3f[] verts, Vector2f[] textures, Vector3f[] norms, boolean tiled)
	{
		Vector3f tans = new Vector3f();
		vertCoord = new float[numVerts * 3];
		textCoord = new float[numVerts * 2];
		normCoord = new float[numVerts * 3];
		tanCoord = new float[numVerts * 3];
		for(int i = 0; i < numVerts; i++)
		{
			vertCoord[i * 3] =     (float)(verts[i]).x();
			newMax(vertCoord[i * 3]);
			vertCoord[i * 3 + 1] = (float)(verts[i]).y();
			newMax(vertCoord[i * 3 + 1]);
			vertCoord[i * 3 + 2] = (float)(verts[i]).z();
			newMax(vertCoord[i * 3 + 2]);
			textCoord[i * 2] =     (float)(textures[i]).x();
			textCoord[i * 2 + 1] = (float)(textures[i]).y();
			normCoord[i * 3] =     (float)(norms[i]).x();
			normCoord[i * 3 + 1] = (float)(norms[i]).y();
			normCoord[i * 3 + 2] = (float)(norms[i]).z();
			if((((verts[i]).x() == 0.0f) && ((verts[i]).y() == 1.0f) &&  ((verts[i]).z() == 0.0f)) || (((verts[i]).x() == 0.0f) && ((verts[i]).y() == -1.0f) &&  ((verts[i]).z() == 0.0f)))
			{
				tans.set(0.0f, 0.0f, -1.0f);
			}
			else
			{
				tans.set(0.0f, 1.0f, 0.0f);
				tans.cross(verts[i]);
			}
			tanCoord[i * 3] = tans.x;
			tanCoord[i * 3 + 1] = tans.y;
			tanCoord[i * 3 + 2] = tans.z;
		}
		if(tiled)
		{
			for (int i = 0; i < textCoord.length; i++)
			{
				textCoord[i] *= maxVal;
			}
		}
		setupVBO();
		initBuffer(0, vertCoord);
		initBuffer(1, textCoord);
		initBuffer(2, normCoord);
		initBuffer(3, tanCoord);
	}

	/**
	 * Initializes the buffers of the object. Usually done in the constructor.
	 * @param spot - the spot of the object's vbo to initialize
	 * @param coord - the float array to pass into our vbo spot
	 */
	public void initBuffer(int spot, float[] coord)
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[spot]);
		FloatBuffer objBuf = Buffers.newDirectFloatBuffer(coord);
		gl.glBufferData(GL_ARRAY_BUFFER, objBuf.limit() * 4, objBuf, GL_STATIC_DRAW);
	}

	/**
	 * Gets the buffers of the object setup and ready for displaying. Still need to call drawArrays() afterwards.
	 * @param spot - the spot of the vbo to grab
	 * @param index - the location index of the in variable in glsl
	 * @param sizeGrab - the amount of objects to grab (at a time)
	 */
	public void displayBuffer(int spot, int index, int sizeGrab)
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[spot]);
		gl.glVertexAttribPointer(index, sizeGrab, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(index);
	}

	/**
	 * @return The vertex coordinates of the object.
	 */
	public float[] getVertCoord() { return vertCoord; }

	/**
	 * @return The texture coordinates of the object.
	 */
	public float[] getTextCoord() { return textCoord; }

	/**
	 * @return The normal coordinates of the object.
	 */
	public float[] getNormCoord() { return normCoord; }

	/**
	 * @return The tangent coordinates of the object created within the .obj constructor for this class.
	 */
	public float[] getTanCoord() { return tanCoord; }

	/**
	 * @return the scale of the object - the absolute largest point on the object.
	 */
	public float getMaxVal() { return maxVal; }
}