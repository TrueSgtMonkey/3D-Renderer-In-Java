package a4;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.GLContext;
import org.joml.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.*;
import java.nio.*;

/**
	Brings textures with our 3D model along with other details. Extends the 
	ObjObject class and builds on it.
	
	This may have more information later on that is related to a scene instead
	of an obj 3D model. This is the reason why it extends ObjObject instead
	of just being an ObjObject.
*/
public class SceneObject extends ObjObject
{
	private boolean transparent = false;
	private float[] transparency = {0.3f, 0.7f};
	private int alphaLoc, flipLoc;

	private int bottomGear = 0;

	private int texture, normTexture;
	private Matrix4f invTrMat = new Matrix4f(); // inverse-transpose
	private int windingOrder = GL_CCW;
	private float[] bumpiness = new float[2];
	
	//this objects matrices that we will use if we decide not to use stack
	private Matrix4f mMat = new Matrix4f();
	private Matrix4f mvMat = new Matrix4f();
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private FloatBuffer vec2vals = Buffers.newDirectFloatBuffer(2);
	private Matrix4f shadowMVP1 = new Matrix4f();
	private Matrix4f shadowMVP2 = new Matrix4f();
	private Matrix4f b = new Matrix4f();

	private int reflective = 0, reflectiveLoc;
	private int bumpy = 0, bumpyLoc, bumpinessLoc;
	private int threeDLoc;
	
	private int sLoc, mvLoc;
	private int cubePLoc, cubeVLoc;
	
	public SceneObject(ImportedModel model, boolean objTiled, int texture, int normTexture)
	{
		//buffers will already be initialized by the time this is done
		super(model, objTiled);
		this.texture = texture;
		this.normTexture = normTexture;
		b.set(
			0.5f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.5f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.5f, 0.0f,
			0.5f, 0.5f, 0.5f, 1.0f);
	}
	
	public SceneObject(ImportedModel model, boolean objTiled, String filename, String normFilename, boolean textureTiled)
	{
		//buffers will already be initialized by the time this is done
		super(model, objTiled);
		//if the textures are tiled, the normal texture should be too
		this.texture = Utils.loadTexture(filename, textureTiled);
		this.normTexture = Utils.loadTexture(normFilename, textureTiled);
		b.set(
			0.5f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.5f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.5f, 0.0f,
			0.5f, 0.5f, 0.5f, 1.0f);
	}

	public void setMatTranslation(float x, float y, float z)
	{
		mMat.identity().setTranslation(x, y, z);
	}

	/**
	 * Use this if you want to use one image and only want to make the skybox
	 * */
	public void drawSkyBox(int mvLoc, int projLoc, Matrix4f vMat, Matrix4f pMat)
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);

		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));

		if(texture != -1)
		{
			gl.glActiveTexture(GL_TEXTURE5);
			gl.glBindTexture(GL_TEXTURE_2D, texture);
		}
		if(normTexture != -1)
		{
			gl.glActiveTexture(GL_TEXTURE2);
			gl.glBindTexture(GL_TEXTURE_2D, normTexture);
		}

		displayObjBuffers();

		gl.glDisable(GL_DEPTH_TEST);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(windingOrder);
		gl.glDrawArrays(GL_TRIANGLES, 0, getNumVertices());
		gl.glEnable(GL_DEPTH_TEST);
	}

	public void drawCubeMap(int renderingProgram, Matrix4f vMat, Matrix4f pMat)
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();


		cubePLoc = gl.glGetUniformLocation(renderingProgram, "proj_matrix");
		cubeVLoc = gl.glGetUniformLocation(renderingProgram, "v_matrix");

		gl.glUniformMatrix4fv(cubePLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(cubeVLoc, 1, false, vMat.get(vals));

		displayBuffer(0, 0, 3);


		if(texture != -1)
		{
			gl.glActiveTexture(GL_TEXTURE0);
			gl.glBindTexture(GL_TEXTURE_CUBE_MAP, texture);
		}

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(windingOrder);
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, getNumVertices());
		gl.glEnable(GL_DEPTH_TEST);
	}
	
	public void passOne(int renderingProgram1, Matrix4f lightPmat, Matrix4f lightVmat, Vector3f translation, Vector4f rotation, Vector3f scale)
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		mMat.identity();
		if(translation != null)
			mMat.translate(translation);
		if(rotation != null)
			mMat.rotate(rotation.x, rotation.y, rotation.z, rotation.w);
		if(scale != null)
			mMat.scale(scale);
		if(!transparent)
		{
			shadowMVP1.identity();
			shadowMVP1.mul(lightPmat);
			shadowMVP1.mul(lightVmat);
			shadowMVP1.mul(mMat);

			sLoc = gl.glGetUniformLocation(renderingProgram1, "shadowMVP");
			gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));

			displayBuffer(0, 0, 3);

			gl.glEnable(GL_CULL_FACE);
			gl.glFrontFace(windingOrder);
			gl.glEnable(GL_DEPTH_TEST);
			gl.glDepthFunc(GL_LEQUAL);

			gl.glDrawArrays(GL_TRIANGLES, 0, getNumVertices());
		}
	}
	
	public void passTwo(int renderingProgram, int mvLoc, int projLoc, int nLoc, int sLoc, Matrix4f pMat, Matrix4f vMat, Matrix4f lightPmat, Matrix4f lightVmat, Vector3f translation, Vector4f rotation, Vector3f scale)
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		mMat.identity();
		if(translation != null)
			mMat.translate(translation);
		if(rotation != null)
			mMat.rotate(rotation.x, rotation.y, rotation.z, rotation.w);
		if(scale != null)
			mMat.scale(scale);
		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);
		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);
		
		mvMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);

		reflectiveLoc = gl.glGetUniformLocation(renderingProgram, "reflective");
		bumpyLoc = gl.glGetUniformLocation(renderingProgram, "bumpy");
		bumpinessLoc = gl.glGetUniformLocation(renderingProgram, "bumpiness");
		threeDLoc = gl.glGetUniformLocation(renderingProgram, "threeD");
		
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
		gl.glProgramUniform2fv(renderingProgram, bumpinessLoc, 1, bumpiness, 0);
		gl.glUniform1i(reflectiveLoc, reflective);
		//gl.glUniform1i(threeDLoc, bottomGear);
		gl.glUniform1i(threeDLoc, bottomGear);
		gl.glUniform1i(bumpyLoc, bumpy);
		
		displayObjBuffers();

		if(texture != -1)
		{
			if (reflective == 0)
			{
				if(bottomGear == 0)
				{
					gl.glActiveTexture(GL_TEXTURE5);
					gl.glBindTexture(GL_TEXTURE_2D, texture);
				}
				else
				{
					gl.glActiveTexture(GL_TEXTURE3);
					gl.glBindTexture(GL_TEXTURE_3D, texture);
				}
			}
			else
			{
				gl.glActiveTexture(GL_TEXTURE1);
				gl.glBindTexture(GL_TEXTURE_CUBE_MAP, texture);
			}
		}
		if(normTexture != -1)
		{
			gl.glActiveTexture(GL_TEXTURE2);
			gl.glBindTexture(GL_TEXTURE_2D, normTexture);
		}

		gl.glEnable(GL_CULL_FACE);
		flipLoc = gl.glGetUniformLocation(renderingProgram, "flipNormal");
		alphaLoc = gl.glGetUniformLocation(renderingProgram, "alpha");
		if(!transparent)
		{
			gl.glFrontFace(windingOrder);
			gl.glEnable(GL_DEPTH_TEST);
			gl.glDepthFunc(GL_LEQUAL);
			gl.glProgramUniform1f(renderingProgram, flipLoc, 1.0f);
			gl.glProgramUniform1f(renderingProgram, alphaLoc, 1.0f);
			gl.glDrawArrays(GL_TRIANGLES, 0, getNumVertices());
		}
		else
		{
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			gl.glBlendEquation(GL_FUNC_ADD);
			gl.glCullFace(GL_FRONT);
			gl.glProgramUniform1f(renderingProgram, flipLoc, -1.0f);
			gl.glProgramUniform1f(renderingProgram, alphaLoc, transparency[0]);
			gl.glDrawArrays(GL_TRIANGLES, 0, getNumVertices());
			gl.glCullFace(GL_BACK);
			gl.glProgramUniform1f(renderingProgram, flipLoc, 1.0f);
			gl.glProgramUniform1f(renderingProgram, alphaLoc, transparency[1]);
			gl.glDrawArrays(GL_TRIANGLES, 0, getNumVertices());
			gl.glDisable(GL_BLEND);
		}
	}

	public int getWindingOrder() { return windingOrder; }
	public void setWindingOrder(int windingOrder) { this.windingOrder = windingOrder; }
	public int reflective() { return reflective; }
	public void setReflective(int reflective) { this.reflective = reflective; }
	public int bumpy() { return bumpy; }
	public void setBumpy(int bumpy) { this.bumpy = bumpy; }
	public void setBumpiness(float[] bumpiness)
	{
		this.bumpiness[0] = bumpiness[0];
		this.bumpiness[1] = bumpiness[1];
	}
	public void setBottomGear(int bottomGear) { this.bottomGear = bottomGear; }
	public int getBottomGear() { return bottomGear; }
	public int getTexture() { return texture; }
	public void setTexture(int texture) { this.texture = texture; }
	public int getNormTexture() { return normTexture; }
	public void setNormTexture(int normTexture) { this.normTexture = normTexture; }
	public boolean isTransparent() { return transparent; }
	public void setTransparent(boolean transparent) { this.transparent = transparent; }
	public float[] getTransparency() { return transparency; }
	public void setTransparency(float[] transparency) { this.transparency = transparency; }

}