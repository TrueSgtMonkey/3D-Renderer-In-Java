package a4;

import org.joml.*;

/**
	Accepts a 3D model (.obj file) as an initializer instead of a float array. 
	Extends the HandMadeObject class and uses the ImportedModel class.
*/
public class ObjObject extends HandMadeObject
{
	private ImportedModel impObj;

	/**
	 * Create an object that uses the .obj file format. You may want to use SceneObjects instead.
	 * @param importedObject - the .obj file to be loaded in
	 * @param tiled - whether or not we want to stretch out the texture given by the max value vertex
	 */
	public ObjObject(ImportedModel importedObject, boolean tiled)
	{
		super(importedObject.getNumVertices(), importedObject.getVertices(), importedObject.getTexCoords(), importedObject.getNormals(), tiled);
		impObj = importedObject;
	}

	/**
	 * Initializes all the VBOs in the object. Done with the constructors in the super class
	 */
	public void initObjBuffers()
	{
		initBuffer(0, getVertCoord());
		initBuffer(1, getTextCoord());
		initBuffer(2, getNormCoord());
		initBuffer(3, getTanCoord());
	}

	/**
	 * Get the obj object ready to display with the VBOs
	 */
	public void displayObjBuffers()
	{
		displayBuffer(0, 0, 3);
		displayBuffer(1, 1, 2);
		displayBuffer(2, 2, 3);
		displayBuffer(3, 3, 3);
	}
	
	public int getNumVertices()
	{
		return impObj.getNumVertices();
	}
	
	public Vector3f[] getVertices()
	{
		return impObj.getVertices();
	}
	
	public Vector2f[] getTexCoords()
	{
		return impObj.getTexCoords();
	}
	
	public Vector3f[] getNormals()
	{
		return impObj.getNormals();
	}
}