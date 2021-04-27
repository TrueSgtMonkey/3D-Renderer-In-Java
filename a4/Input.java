package a4;

import java.awt.event.*;
import org.joml.*;
import java.awt.AWTException;

/** A singleton used to make keyboard, mouse, and mouse movement available to
the entire program and not just a class. */
public class Input implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener
{
	private int keys[];
	private int mouseButtons[];
	private int lastMouse, lastKey;
	private boolean onScreen;
	private static Input input;
	private boolean centered, isCentering, mouseMoved, mouseWheelMoved;
	private Vector2f mousePos, oldMousePos, mouseMove;
	private int inputs, keyInputs, mouseInputs, mouseWheelTicks;
	
	private Input()
	{
		keys = new int[1024];
		inputs = 0;
		keyInputs = 0;
		mouseWheelTicks = 0;
		mouseInputs = 0;
		isCentering = false;
		lastKey = -1;
		lastMouse = -1;
		for(int i = 0; i < keys.length; i++)
			keys[i] = -1;
		mouseButtons = new int[32];
		for(int i = 0; i < mouseButtons.length; i++)
			mouseButtons[i] = -1;
		onScreen = false;
		centered = true;
		mouseMoved = false;
		mouseWheelMoved = false;
		mousePos = new Vector2f();
		oldMousePos = new Vector2f();
		mouseMove = new Vector2f();
	}
	
	public static Input get()
	{
		if(input == null)
		{
			input = new Input();
		}
		return input;
	}
	
	/**
		Will run in the display() function of Display class
		Reset things that need to be reset on each frame
	*/
	public void update()
	{
		if(!mouseMoved)
			mouseMove.zero();
		
		mouseMoved = false;
		mouseWheelTicks = 0;
		mouseWheelMoved = false;
	}
	
	public static void resetInstance() { input = null; } 
	
	/**
		keyboard input checks
	*/
	
	public int getNumInputs() { return inputs; }
	public int getNumMouseInputs() { return mouseInputs; }
	public int getNumKeyInputs() { return keyInputs; }
	
	public boolean isKeyPressed(int keycode)
	{
		return (keys[keycode] != -1);
	}
	
	public boolean isKeyReleased(int keycode)
	{
		return (keys[keycode] == -1);
	}
	
	/**
		Mouse button input checks
	*/
	public int getLastMousePress() { return lastMouse; }
	
	public boolean isMouseOnScreen() { return onScreen; }
	
	public boolean isMousePressed(int mousecode)
	{
		return (mouseButtons[mousecode] != -1);
	}
	
	public boolean isMouseReleased(int mousecode)
	{
		return (mouseButtons[mousecode] == -1);
	}
	
	public boolean getCentered() { return centered; }
	
	public Vector2f getMouseMotion() { return mouseMove; }
	public Vector2f getMousePos() { return mousePos; }
	
	public boolean isMouseMoved() { return mouseMoved; }
	
	public boolean isMouseWheelMoved() { return mouseWheelMoved; }
	
	public int mouseWheelTicks() { return mouseWheelTicks; }
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		if(!isKeyPressed(e.getKeyCode()))
		{
			inputs++;
			keyInputs++;
		}
		keys[e.getKeyCode()] = e.getKeyCode();
	}
	
	@Override
	public void keyTyped(KeyEvent e) 
	{
		lastKey = e.getKeyCode();
	}
	
	@Override
	public void keyReleased(KeyEvent e) 
	{
		if(isKeyPressed(e.getKeyCode()))
		{
			inputs--;
			keyInputs--;
		}
		keys[e.getKeyCode()] = -1;
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		lastMouse = e.getButton();
	}
	
	@Override
	public void mouseEntered(MouseEvent e)
	{
		onScreen = true;
	}
	
	@Override
	public void mouseExited(MouseEvent e)
	{
		onScreen = false;
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
		if(!isMousePressed(e.getButton()))
		{
			inputs++;
			mouseInputs++;
		}
		mouseButtons[e.getButton()] = e.getButton();
	}
	
	@Override
	public void mouseReleased(MouseEvent e)
	{
		if(isMousePressed(e.getButton()))
		{
			inputs--;
			mouseInputs--;
		}
		mouseButtons[e.getButton()] = -1;
	}
	
	
	@Override
	public void mouseMoved(MouseEvent e)
	{
		if(!isCentering)
		{
			mouseMoved = true;
			mousePos.set(e.getX(), e.getY());
			
			mouseMove.set(mousePos);
			mouseMove.sub(oldMousePos);
			
			oldMousePos.set(e.getX(), e.getY());
		}
		else
			isCentering = false;
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		mousePos.set(e.getX(), e.getY());
		
		mouseMove.set(mousePos);
		mouseMove.sub(oldMousePos);
		
		oldMousePos.set(e.getX(), e.getY());
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		mouseWheelMoved = true;
		mouseWheelTicks = e.getWheelRotation();
	}
	
	/**
		for debugging by printing out our mouse button or key arrays
	*/
	private void printKeys(int arr[])
	{
		for(int i = 0; i < arr.length; i++)
		{
			System.out.print(arr[i]);
			if(i != arr.length - 1)
				System.out.print(",");
			if(i % 16 == 0)
				System.out.println();
		}
	}
}