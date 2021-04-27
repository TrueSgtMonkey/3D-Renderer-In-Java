package a4;

import java.io.*;
import java.util.ArrayList;
import org.joml.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.*;

/**
	A static scene (doesn't move) that groups different obj files together to
	act as one. Uses groups of SceneObjects and groups of textures (ints) to
	come together into one cohesive model that moves around in relation to
	each .obj
*/
public class Scene
{
	private ArrayList<SceneObject> objects;
	private ArrayList<Integer> textures;
	private Vector3f translation, scale;
	private Vector4f rotation;
	private boolean stack;
	private boolean color;	//will use if no textures found
	
	/**
		Specify an object directory filled with OBJs to group together into a 
		"Scene" and specify a texture directory filled with PNGs to map those 
		textures onto the OBJs. It's good practice to make the same number of 
		textures and scenes. 
		@param folder : String -> specified OBJ folder to load from
		@param textureFolder : String -> specified PNG folder to load from
	*/
	public Scene(String folder, String textureFolder)
	{
		color = false;
		objects = new ArrayList<SceneObject>();
		this.stack = stack;
		textures = new ArrayList<Integer>();
		
		//using these as functions because there are two constructors
		getTextures(textureFolder);
		getOBJs(folder);
	}
	
	/**
		Specify an object directory filled with OBJs to group together into a 
		"Scene" and specify a texture directory filled with PNGs to map those 
		textures onto the OBJs. It's good practice to make the same number of 
		textures and scenes. 
		@param folder : String -> specified OBJ folder to load from
		@param textureFolder : String -> specified PNG folder to load from
	*/
	public Scene(String folder, String textureFolder, Vector3f translation, Vector4f rotation, Vector3f scale)
	{
		color = false;
		objects = new ArrayList<SceneObject>();
		textures = new ArrayList<Integer>();
		
		//using these as functions because there are two constructors
		getTextures(textureFolder);
		getOBJs(folder);
		this.stack = stack;
		
		if(translation != null)
			this.translation = translation;
		if(rotation != null)
			this.rotation = rotation;
		if(scale != null)
			this.scale = scale;
	}
	
	private void getTextures(String textureFolder)
	{
		File path2 = new File(textureFolder);
		
		//getting the textures from the specified folder
		if(path2.exists())
		{
			File[] files = path2.listFiles();
			for(int i = 0; i < files.length; i++)
			{
				//filtering out any other files besides .pngs
				if(files[i].getName().contains(".png") || files[i].getName().contains(".jpg"))
				{
					textures.add(Utils.loadTexture(textureFolder + "/" + files[i].getName(), true));
				}
			}
		}
		
		//no textures were found
		if(textures.size() == 0)
			color = true;
	}
	
	private void getOBJs(String folder)
	{
		File path = new File(folder);
		//getting the OBJs from the specified folder
		if(path.exists())
		{
			File[] files = path.listFiles();
			for(int i = 0, j = 0; i < files.length; i++)
			{
				//filtering out any files besides .objs
				if(files[i].getName().contains(".obj"))
				{
					if(!color)
					{
						objects.add(new SceneObject(new ImportedModel("../" + folder + "/" + files[i].getName()), false, textures.get(j)));
						j++;
						if(j == textures.size())
							j = 0;
					}
					else
					{
						objects.add(new SceneObject(new ImportedModel("../" + folder + "/" + files[i].getName()), false, 1));
					}
				}
			}
		}
	}
	
	public void vBuffers()
	{
		for(int i = 0; i < objects.size(); i++)
		{
			objects.get(i).displayBuffer(0, 0, 3);
		}
	}
	
	//for static objects (don't move)
	public void passOne(int renderingProgram1, Matrix4f lightPmat, Matrix4f lightVmat)
	{
		for(int i = 0; i < objects.size(); i++)
		{
			objects.get(i).passOne(renderingProgram1, lightPmat, lightVmat, translation, rotation, scale);
		}
	}
	
	//for movable objects
	public void passOne(int renderingProgram1, Matrix4f lightPmat, Matrix4f lightVmat, Vector3f translation, Vector4f rotation, Vector3f scale)
	{
		/* making sure we use our default values in case the user does not want
		to pass in one of these */
		if(translation == null)
			translation = this.translation;
		if(rotation == null)
			rotation = this.rotation;
		if(scale == null)
			scale = this.scale;
		for(int i = 0; i < objects.size(); i++)
		{
			objects.get(i).passOne(renderingProgram1, lightPmat, lightVmat, translation, rotation, scale);
		}
	}
	
	
	//for movable objects
	public void passTwo(int mvLoc, int projLoc, int nLoc, int sLoc, Matrix4f pMat, Matrix4f vMat, Matrix4f lightPmat, Matrix4f lightVmat)
	{
		for(int i = 0; i < objects.size(); i++)
		{
			objects.get(i).passTwo(mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, translation, rotation, scale);
		}
	}
	
	//for movable objects
	public void passTwo(int mvLoc, int projLoc, int nLoc, int sLoc, Matrix4f pMat, Matrix4f vMat, Matrix4f lightPmat, Matrix4f lightVmat, Vector3f translation, Vector4f rotation, Vector3f scale)
	{
		/* making sure we use our default values in case the user does not want
		to pass in one of these */
		if(translation == null)
			translation = this.translation;
		if(rotation == null)
			rotation = this.rotation;
		if(scale == null)
			scale = this.scale;
		for(int i = 0; i < objects.size(); i++)
		{
			objects.get(i).passTwo(mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, translation, rotation, scale);
		}
	}
	
	public void setTranslation(Vector3f translation) { this.translation = translation; }
	public Vector3f getTranslation() { return translation; }
	public void setRotation(Vector4f rotation) { this.rotation = rotation; }
	public Vector4f getRotation() { return rotation; }
	public void setScale(Vector3f scale) { this.scale = scale; }
	public Vector3f getScale() { return scale; }
}