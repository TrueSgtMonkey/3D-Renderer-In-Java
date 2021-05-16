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

	/**
	 * singleton class
	 */
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

	/**
	 * @return The only instance of this Input class. We only ever need 1.
	 */
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

	/**
	 * Deletes the only instance of this class.
	 */
	public static void resetInstance() { input = null; } 
	
	/*
		keyboard input checks
	*/

	/**
	 * @return the total number of inputs being pressed on either the mouse or the keyboard
	 */
	public int getNumInputs() { return inputs; }

	/**
	 * @return The total number of mouse inputs being pressed.
	 */
	public int getNumMouseInputs() { return mouseInputs; }

	/**
	 * @return The total number of keys being pressed.
	 */
	public int getNumKeyInputs() { return keyInputs; }

	/**
	 * Returns whether or not the integer value of the key corresponds to one of the keys being currently pressed down.
	 * @param keycode - The integer value of the current key being pressed.
	 * @return a boolean - true = that key is pressed.
	 */
	public boolean isKeyPressed(int keycode)
	{
		return (keys[keycode] != -1);
	}

	/**
	 * Returns whether or not the integer value of the key corresponds to a key that is not pressed.
	 * @param keycode - The integer value of the current key not being pressed.
	 * @return a boolean - true = key is not pressed
	 */
	public boolean isKeyReleased(int keycode)
	{
		return (keys[keycode] == -1);
	}
	
	/*
		Mouse button input checks
	*/

	/**
	 * @return the integer value of the last mouse button that was pressed.
	 */
	public int getLastMousePress() { return lastMouse; }

	/**
	 * @return Whether or not the mouse is currently inside the window
	 */
	public boolean isMouseOnScreen() { return onScreen; }

	/**
	 * Returns whether or not the integer value passed in corresponds to one of the mouse buttons being pressed.
	 * @param mousecode - int value that should correspond to a mouse value
	 * @return - a boolean - true = mouse button pressed.
	 */
	public boolean isMousePressed(int mousecode)
	{
		return (mouseButtons[mousecode] != -1);
	}

	/**
	 * Returns whether or not the integer value of the mouse input corresponds to a key that is not pressed.
	 * @param mousecode - The integer value of the current mouse input not being pressed.
	 * @return a boolean - true = mouse input is not pressed
	 */
	public boolean isMouseReleased(int mousecode)
	{
		return (mouseButtons[mousecode] == -1);
	}

	/**
	 * @return Whether or not the mouse is centered in the middle of the screen
	 */
	public boolean getCentered() { return centered; }

	/**
	 * @return the amount of change from the last location in the mouse
	 */
	public Vector2f getMouseMotion() { return mouseMove; }

	/**
	 * @return the current position of the mouse on the screen
	 */
	public Vector2f getMousePos() { return mousePos; }

	/**
	 * @return Wheteher or not the mouse is moved.
	 */
	public boolean isMouseMoved() { return mouseMoved; }

	/**
	 * @return Whether or not we are scrolling the mouse wheel
	 */
	public boolean isMouseWheelMoved() { return mouseWheelMoved; }

	/**
	 * @return The amount we scrolled the mouse (more ticks = more scrolling)
	 */
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