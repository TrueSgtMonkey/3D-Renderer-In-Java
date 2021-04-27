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
	private int texture;
	private Matrix4f invTrMat = new Matrix4f(); // inverse-transpose
	
	//this objects matrices that we will use if we decide not to use stack
	private Matrix4f mMat = new Matrix4f();
	private Matrix4f mvMat = new Matrix4f();
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4f shadowMVP1 = new Matrix4f();
	private Matrix4f shadowMVP2 = new Matrix4f();
	private Matrix4f b = new Matrix4f();
	
	private int sLoc, mLoc, mvLoc, projLoc, nLoc, vLoc;
	
	public SceneObject(ImportedModel model, boolean objTiled, int texture)
	{
		//buffers will already be initialized by the time this is done
		super(model, objTiled);
		this.texture = texture;
		b.set(
			0.5f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.5f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.5f, 0.0f,
			0.5f, 0.5f, 0.5f, 1.0f);
	}
	
	public SceneObject(ImportedModel model, boolean objTiled, String filename, boolean textureTiled)
	{
		//buffers will already be initialized by the time this is done
		super(model, objTiled);
		this.texture = Utils.loadTexture(filename, textureTiled);
		b.set(
			0.5f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.5f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.5f, 0.0f,
			0.5f, 0.5f, 0.5f, 1.0f);
	}
	
	public void passOne(int renderingProgram1, Matrix4f lightPmat, Matrix4f[] lightVmats, Vector3f translation, Vector4f rotation, Vector3f scale)
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		mMat.identity();
		if(translation != null)
			mMat.translate(translation);
		if(rotation != null)
			mMat.rotate(rotation.x, rotation.y, rotation.z, rotation.w);
		if(scale != null)
			mMat.scale(scale);
		/*
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmats[]);
		shadowMVP1.mul(mMat);
		*/
		mLoc = gl.glGetUniformLocation(renderingProgram1, "model");
		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		for(int i = 0; i < 6; i++)
		{
			//may need to multiply by model matrix
			sLoc = gl.glGetUniformLocation(renderingProgram1, "shadowMatrices[" + i + "]");
			gl.glUniformMatrix4fv(sLoc, 1, false, lightVmats[i].get(vals));
		}

		
		displayBuffer(0, 0, 3);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, getNumVertices());
	}
	
	public void passTwo(int renderingProgram2, Matrix4f pMat, Matrix4f vMat, Matrix4f lightPmat, Matrix4f lightVmat, Vector3f translation, Vector4f rotation, Vector3f scale)
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

		mvLoc = gl.glGetUniformLocation(renderingProgram2, "mv_matrix");
		projLoc = gl.glGetUniformLocation(renderingProgram2, "proj_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgram2, "norm_matrix");
		sLoc = gl.glGetUniformLocation(renderingProgram2, "shadowMVP");
		mLoc = gl.glGetUniformLocation(renderingProgram2, "model");
		vLoc = gl.glGetUniformLocation(renderingProgram2, "view");
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		
		displayObjBuffers();
		
		gl.glActiveTexture(GL_TEXTURE5);
		gl.glBindTexture(GL_TEXTURE_2D, texture);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, getNumVertices());
	}
}