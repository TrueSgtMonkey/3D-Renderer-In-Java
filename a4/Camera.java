package a4;

/**
m00 m10 m20 m30
m01 m11 m21 m31
m02 m12 m22 m32
m03 m13 m23 m33
*/

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import org.joml.*;
import java.lang.Math;

/** A singleton that represents the single camera used by OpenGL */
public class Camera
{
	//c is our camera location
	private Vector3f u, v, n, c;
	private Matrix4f viewMat, rotMat = new Matrix4f();
	private float speed, originalSpeed, rotSpeed;
	private float delta;
	private boolean isSprinting, toggleLines;
	private static Camera cam;
	private float rotX, rotY, rotZ;
	private Vector3f rotVec;

	/**
	 * singleton class
	 */
	private Camera()
	{
		rotX = rotY = rotZ = 0.0f;
		rotVec = new Vector3f();
		c = new Vector3f();
		u = new Vector3f(1.0f, 0.0f, 0.0f);
		v = new Vector3f(0.0f, 1.0f, 0.0f);
		n = new Vector3f(0.0f, 0.0f, 1.0f);
		viewMat = new Matrix4f();
		this.speed = 3.0f;
		originalSpeed = this.speed;
		isSprinting = false;
		this.rotSpeed = 0.1f;
		toggleLines = false;
		delta = 0.0f;
	}

	/** Get the single camera used by OpenGL and perform operations with it. */
	public static Camera get()
	{
		if(cam == null)
		{
			cam = new Camera();
		}
		return cam;
	}

	/** Get the rotation vector based on floats */
	public Vector3f rotationVec() { return rotVec.set(rotX, rotY, rotZ); }

	/** Delete the current OpenGL camera. */
	public static void resetInstance() { cam = null; }

	/**
	 * Sets the location, speed, and rotation speed
	 * @param x - x location
	 * @param y - y location
	 * @param z - z location
	 * @param speed - speed that the camera moves or translates
	 * @param rotSpeed - speed that the camera rotates
	 */
	public void setAttrib(float x, float y, float z, float speed, float rotSpeed)
	{
		c.set(x, y, z);
		this.speed = speed;
		originalSpeed = speed;
		this.rotSpeed = rotSpeed;
	}

	/**
	 * Gets the rotation matrix and multiplies it with the translation matrix.
	 * @param delta - The amount of time since the last frame - the camera will keep this value in a variable.
	 * @return - the rotation matrix multiplied by the translation matrix
	 */
	public Matrix4f viewMat(float delta)
	{
		if(isSprinting)
			speed = originalSpeed * 2.0f;
		else
			speed = originalSpeed;
		this.delta = delta;
		viewMat.identity();
		//starting off with the rotation matrix
		viewMat.mul(u.x(), v.x(), n.x(), 0.0f,
					u.y(), v.y(), n.y(), 0.0f,
					u.z(), v.z(), n.z(), 0.0f,
					0.0f, 0.0f, 0.0f, 1.0f);
		//now doing the translation matrix (where we want to be)
		// I think it is based on our camera location?
		viewMat.mul(1.0f, 0.0f, 0.0f, 0.0f,
					0.0f, 1.0f, 0.0f, 0.0f,
					0.0f, 0.0f, 1.0f, 0.0f,
					-c.x(), -c.y(), -c.z(), 1.0f);
		return viewMat;
	}

	/**
	 * Gets the rotation matrix from the camera without the translation matrix
	 * @param delta - the amount of time since the last frame - the Camera will keep this value
	 * @return - the rotation matrix
	 */
	public Matrix4f rotMat(float delta)
	{
		this.delta = delta;
		rotMat.identity();
		rotMat.mul(u.x(), v.x(), n.x(), 0.0f,
				u.y(), v.y(), n.y(), 0.0f,
				u.z(), v.z(), n.z(), 0.0f,
				0.0f, 0.0f, 0.0f, 1.0f);
		return rotMat;
	}

	/**
	 * Gets the translation matrix without the rotation matrix.
	 * @param delta - the amount of time since the last frame - the Camera will keep this value
	 * @return the translation matrix
	 */
	public Matrix4f posMat(float delta)
	{
		this.delta = delta;
		viewMat.identity();
		viewMat.mul(1.0f, 0.0f, 0.0f, 0.0f,
					0.0f, 1.0f, 0.0f, 0.0f,
					0.0f, 0.0f, 1.0f, 0.0f,
					-c.x(), -c.y(), -c.z(), 1.0f);
		return viewMat;
	}

	/**
	 * Gets the view matrix without creating a new one. WILL BE NULL IF YOU DON'T USE viewMat(float delta) FIRST!
	 * @return - the current view matrix on this frame
	 */
	public Matrix4f getViewMat() { return viewMat; }

	/**
	 * Adds the c vector by what is passed in (the c vector is used in the translation matrix)
	 * @param vec - The vector that is added to the c vector.
	 */
	public void addPos(Vector3f vec)
	{
		//have to do this because JOMLs pass by reference
		Vector3f newVec = new Vector3f(vec.x(), vec.y(), vec.z());
		vec.mul(delta);
		vec.mul(speed);
		c.add(vec);
		vec.set(newVec.x(), newVec.y(), newVec.z());
	}

	/**
	 * Subtracts the c vector by what is passed in (the c vector is used in the translation matrix)
	 * @param vec - The vector that is added to the c vector.
	 */
	public void addNeg(Vector3f vec)
	{
		Vector3f newVec = new Vector3f(vec.x(), vec.y(), vec.z());
		vec.negate();
		vec.mul(delta);
		vec.mul(speed);
		c.add(vec);
		vec.set(newVec.x(), newVec.y(), newVec.z());
	}

	/**
	 * @return The "right" vector in relation to the camera
	 */
	public Vector3f u() { return u; }

	/**
	 * @return The "up" vector in relation to the camera
	 */
	public Vector3f v() { return v; }

	/**
	 * @return The "forward" vector in relation to the camera
	 */
	public Vector3f n() { return n; }

	/**
	 * @return The Camera's position
	 */
	public Vector3f c() { return c; }

	/**
	 * Sets whether or not the camera is moving faster (sprinting) or not
	 * @param sprinting - true = move faster, false = move normally
	 */
	public void setSprinting(boolean sprinting) { isSprinting = sprinting; }

	/**
	 * Returns whether or not the camera is moving faster (sprinting) or not
	 * @return - isSprinting
	 */
	public boolean isSprinting() { return isSprinting; }

	/**
	 * Rotates the Camera horizontally with the u, v, and n vectors on the y-axis
	 * @param rS - the amount (in radians) to rotate by
	 */
	public void horRot(float rS)
	{
		u.rotateY(rS);
		v.rotateY(rS);
		n.rotateY(rS);
		rotY += rS;
	}

	/**
	 * Rotates the Camera vertically
	 * @param rS - the amount (in radians) to rotate by
	 */
	public void vertRot(float rS)
	{
		v.rotateAxis(rS, u.x(), u.y(), u.z());
		n.rotateAxis(rS, u.x(), u.y(), u.z());
		rotX += rS;
	}

	/**
	 * @return The amount of time since the last frame
	 */
	public float getDelta() { return delta; }

	/**
	 * @return How fast our camera is moving.
	 */
	public float getSpeed() { return speed; }

	/**
	 * @return How fast our camera is rotating.
	 */
	public float getRotationSpeed() { return rotSpeed; }
}