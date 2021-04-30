package a4;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import org.joml.*;
import java.lang.Math;

public class ViewMat
{
	//c is our camera location
	private Vector3f u, v, n, c;
	private Vector3f dir;
	private Matrix4f viewMat;
	private float speed, originalSpeed, rotSpeed;
	private float delta;
	private boolean isSprinting, toggleLines;
	private float rotX, rotY, rotZ;
	private Vector3f rotVec;
	
	public ViewMat()
	{
		rotX = rotY = rotZ = 0.0f;
		rotVec = new Vector3f();
		dir = new Vector3f();
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
	
	public Vector3f rotationVec() { return rotVec.set(rotX, rotY, rotZ); }
	
	public void setAttrib(float x, float y, float z, float speed, float rotSpeed)
	{
		c.set(x, y, z);
		this.speed = speed;
		originalSpeed = speed;
		this.rotSpeed = rotSpeed;
	}

	public Vector3f setDir()
	{
		dir.set(c);
		dir.add(n);
		return dir;
	}
	
	/** c is the translation Matrix4f to multiply by */
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
	
	public Matrix4f posMat()
	{
		viewMat.identity();
		viewMat.mul(1.0f, 0.0f, 0.0f, 0.0f,
					0.0f, 1.0f, 0.0f, 0.0f,
					0.0f, 0.0f, 1.0f, 0.0f,
					-c.x(), -c.y(), -c.z(), 1.0f);
		return viewMat;
	}
	
	public Matrix4f getViewMat() { return viewMat; }
	
	public void addPos(Vector3f vec)
	{
		Vector3f newVec = new Vector3f(vec.x(), vec.y(), vec.z());
		vec.mul(delta);
		vec.mul(speed);
		c.add(vec);
		vec.set(newVec.x(), newVec.y(), newVec.z());
	}
	
	public void addNeg(Vector3f vec)
	{
		Vector3f newVec = new Vector3f(vec.x(), vec.y(), vec.z());
		vec.negate();
		vec.mul(delta);
		vec.mul(speed);
		c.add(vec);
		vec.set(newVec.x(), newVec.y(), newVec.z());
	}
	
	public Vector3f u() { return u; }
	public Vector3f v() { return v; }
	public Vector3f n() { return n; }
	public Vector3f c() { return c; }
	
	public void setSprinting(boolean sprinting) { isSprinting = sprinting; }
	public boolean isSprinting() { return isSprinting; }
	
	public void horRot(float rS)
	{
		u.rotateY(rS);
		v.rotateY(rS);
		n.rotateY(rS);
		rotY += rS;
	}
	
	public void vertRot(float rS)
	{
		v.rotateAxis(rS, u.x(), u.y(), u.z());
		n.rotateAxis(rS, u.x(), u.y(), u.z());
		rotX += rS;
	}
	
	public float getDelta() { return delta; }
	
	public boolean getToggleLines() { return toggleLines; }
	public void setToggleLines(boolean toggleLines) { this.toggleLines = toggleLines; }
	
	public float getSpeed() { return speed; }
	public float getRotationSpeed() { return rotSpeed; }
}