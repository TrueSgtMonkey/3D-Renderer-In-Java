package a4;

import java.nio.*;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.awt.event.MouseEvent;
import java.lang.Math;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.common.nio.Buffers;
import org.joml.*;

public class Starter extends JFrame implements GLEventListener
{	
	private int adder = 0;
	private GLCanvas myCanvas;
	private int renderingProgram1, renderingProgram2;

	private float fov = 70.0f;

	private boolean heldDown = false;

	private ViewMat view = new ViewMat();

	// model stuff
	private ArrayList<Scene> staticScenes = new ArrayList<Scene>();
	private Scene blueguy, redguy, chromeguy, lightball;

	private SceneObject skybox, refspear1, refspear2;
	
	//variables for moving scenes
	private Vector3f blueguyMove = new Vector3f();
	private Vector3f redguyMove = new Vector3f();
	private Vector3f chromeguyMove = new Vector3f();
	private Vector3f cameraLoc = new Vector3f(12.37f, 5.0f, 0.2f);
	private Vector3f lightLoc = new Vector3f(-9.153f, 192.0f, -0.75f);
	
	// white light properties
	private float[] globalAmbient = new float[] { 0.01f, 0.01f, 0.01f, 1.0f };
	private float[] lightAmbient = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
	private float[] lightDiffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
	private float[] lightSpecular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
	private float lightDistance = 10.0f;
	private float lightIntensity = 0.95f;
		
	// gold material
	private float[] GmatAmb = Utils.goldAmbient();
	private float[] GmatDif = Utils.goldDiffuse();
	private float[] GmatSpe = Utils.goldSpecular();
	private float GmatShi = Utils.goldShininess();
	
	// bronze material
	private float[] BmatAmb = Utils.bronzeAmbient();
	private float[] BmatDif = Utils.bronzeDiffuse();
	private float[] BmatSpe = Utils.bronzeSpecular();
	private float BmatShi = Utils.bronzeShininess();
	
	//old material
	private float[] lmatAmb = Utils.oldAmbient();
	private float[] lmatDif = Utils.oldDiffuse();
	private float[] lmatSpe = Utils.oldSpecular();
	private float lmatShi = Utils.oldShininess();
	
	private float[] thisAmb, thisDif, thisSpe, matAmb, matDif, matSpe;
	private float thisShi, matShi;
	
	// shadow stuff
	private int scSizeX, scSizeY;
	private int [] shadowTex = new int[1];
	private int [] shadowBuffer = new int[1];
	private Matrix4f lightVmat = new Matrix4f();
	private Matrix4f lightPmat = new Matrix4f();
	private Matrix4f shadowMVP1 = new Matrix4f();
	private Matrix4f shadowMVP2 = new Matrix4f();
	private Matrix4f b = new Matrix4f();

	//cubemap stuff
	private int renderingProgramCubeMap;
	private int skyboxTexture;
	private int cameraposLoc;

	// allocate variables for display() function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private FloatBuffer vecvals = Buffers.newDirectFloatBuffer(3);
	private Matrix4f pMat = new Matrix4f();  // perspective matrix
	private Matrix4f vMat = new Matrix4f();  // view matrix
	private Matrix4f mMat = new Matrix4f();  // model matrix
	private Matrix4f mvMat = new Matrix4f(); // model-view matrix
	private Matrix4f invTrMat = new Matrix4f(); // inverse-transpose
	private int mvLoc, projLoc, nLoc, sLoc, timeLoc;
	private int globalAmbLoc, ambLoc, diffLoc, specLoc, posLoc, mambLoc, mdiffLoc, mspecLoc, mshiLoc, distLoc;
	private int screenXLoc, screenYLoc, skyBoxLoc, intensityLoc;
	private float aspect, camScale;
	private Vector3f currentLightPos = new Vector3f();
	private float[] lightPos = new float[3];
	private float[] camPos = new float[3];
	private Vector3f origin = new Vector3f(0.0f, 0.0f, 0.0f);
	private Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
	
	private float add = 0.0f;
	private double tf = 0.0;
	private double startTime;
	private double elapsedTime;
	
	public Starter()
	{	setTitle("Assignment #4");
		setSize(1280, 720);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		camScale = 1.0f;
		
		this.addKeyListener(Input.get());
		myCanvas.addKeyListener(Input.get());
		myCanvas.addMouseMotionListener(Input.get());
		this.addMouseMotionListener(Input.get());
		myCanvas.addMouseListener(Input.get());
		this.addMouseListener(Input.get());
		myCanvas.addMouseWheelListener(Input.get());
		this.addMouseWheelListener(Input.get());
		
		Camera.get().setAttrib(cameraLoc.x(), cameraLoc.y(), cameraLoc.z(), 9.0f, 1.5f);
		Camera.get().horRot(1.5707963267f);
		
		this.add(myCanvas);
		this.setVisible(true);
		Animator animator = new Animator(myCanvas);
		animator.start();
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	public void display(GLAutoDrawable drawable)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		currentLightPos.set(view.c());
		
		elapsedTime = System.currentTimeMillis() - startTime;
		tf = elapsedTime / 1000.0;
		add += (float)tf;
		
		//lightVmat.set(view.viewMat((float)tf));
		lightVmat.identity().setLookAt(currentLightPos, origin, up);
		//lightVmat.identity().setLookAt(currentLightPos.mul(1.5f), view.c(), up);
		//currentLightPos.set(view.c());
		//lightVmat.identity().setLookAt(currentLightPos, origin, up);	// vector from light to origin
		//System.out.println(lightVmat.toString());
		lightPmat.identity().setPerspective((float) Math.toRadians(36.0f), aspect, 0.1f, 1000.0f);
		//lightPmat.identity().setOrtho(-96.0f, 96.0f, -96.0f, 96.0f, 1.0f, 200.0f);

		gl.glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer[0]);
		gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadowTex[0], 0);
	
		gl.glDrawBuffer(GL_NONE);
		gl.glEnable(GL_DEPTH_TEST);
		
		input();
		passOne();
		
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);
	
		gl.glDrawBuffer(GL_FRONT);
		
		passTwo();
		Input.get().update();
		startTime = System.currentTimeMillis();
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passOne()
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
	
		gl.glUseProgram(renderingProgram1);

		gl.glClear(GL_DEPTH_BUFFER_BIT);
		for(int i = 0; i < staticScenes.size(); i++)
		{
			if(staticScenes.get(i).isVisible())
				staticScenes.get(i).passOne(renderingProgram1, lightPmat, lightVmat);
		}

		refspear1.passOne(renderingProgram1, lightPmat, lightVmat, null, null, null);
		refspear2.passOne(renderingProgram1, lightPmat, lightVmat, null, null, null);
		blueguy.passOne(renderingProgram1, lightPmat, lightVmat, blueguyMove.set(blueguy.getTranslation().x, blueguy.getTranslation().y + (float)Math.sin(add) * -0.35f, blueguy.getTranslation().z), null, null);
		redguy.passOne(renderingProgram1, lightPmat, lightVmat, redguyMove.set(redguy.getTranslation().x, redguy.getTranslation().y + (float)Math.sin(add * 2.5f) * 0.25f, redguy.getTranslation().z), null, null);
		chromeguy.passOne(renderingProgram1, lightPmat, lightVmat, chromeguyMove.set(chromeguy.getTranslation().x, chromeguy.getTranslation().y + (float)Math.sin(add * 0.5f) * 0.45f, chromeguy.getTranslation().z), null, null);
		if(lightball.isVisible())
			lightball.passOne(renderingProgram1, lightPmat, lightVmat, Camera.get().c(), null, null);
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passTwo()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		vMat.set(Camera.get().viewMat((float)tf));
		gl.glUseProgram(renderingProgramCubeMap);

		skybox.drawCubeMap(renderingProgramCubeMap, vMat, pMat);

		gl.glUseProgram(renderingProgram2);
		
		mvLoc = gl.glGetUniformLocation(renderingProgram2, "mv_matrix");
		projLoc = gl.glGetUniformLocation(renderingProgram2, "proj_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgram2, "norm_matrix");
		sLoc = gl.glGetUniformLocation(renderingProgram2, "shadowMVP");
		timeLoc = gl.glGetUniformLocation(renderingProgram2, "time");
		screenXLoc = gl.glGetUniformLocation(renderingProgram2, "scX");
		screenYLoc = gl.glGetUniformLocation(renderingProgram2, "scY");
		skyBoxLoc = gl.glGetUniformLocation(renderingProgram2, "skybox");
		
		gl.glProgramUniform1f(renderingProgram2, timeLoc, add);
		gl.glProgramUniform1i(renderingProgram2, skyBoxLoc, 1);
		gl.glProgramUniform1f(renderingProgram2, screenXLoc, scSizeX);
		gl.glProgramUniform1f(renderingProgram2, screenXLoc, scSizeY);

		gl.glProgramUniform1i(renderingProgram2, skyBoxLoc, 0);

		/*
		currentLightPos.set(lightLoc);
		installLights(renderingProgram2, vMat);
		*/

		setupVBuffers();
		
		thisAmb = lmatAmb;
		thisDif = lmatDif;
		thisSpe = lmatSpe;
		thisShi = lmatShi;
		
		currentLightPos.set(view.c());
		//installLights(renderingProgram2, view.viewMat((float)tf));
		installLights(renderingProgram2, vMat);
		
		for(int i = 0; i < staticScenes.size(); i++)
		{
			// currentLightPos.set(lightLoc);
			// installLights(renderingProgram2, vMat);
			
			//gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[2]);
			//staticScenes.get(i).vBuffers();
			if(staticScenes.get(i).isVisible())
				staticScenes.get(i).passTwo(renderingProgram2, mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat);
		}
		
		thisAmb = GmatAmb;
		thisDif = GmatDif;
		thisSpe = GmatSpe;
		thisShi = GmatShi;
		
		installLights(renderingProgram2, vMat);

		refspear1.passTwo(renderingProgram2, mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, null, null, null);
		refspear2.passTwo(renderingProgram2, mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, null, null, null);
		blueguy.passTwo(renderingProgram2, mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, blueguyMove.set(blueguy.getTranslation().x, blueguy.getTranslation().y + (float)Math.sin(add) * -0.35f, blueguy.getTranslation().z), null, null);
		redguy.passTwo(renderingProgram2, mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, redguyMove.set(redguy.getTranslation().x, redguy.getTranslation().y + (float)Math.sin(add * 2.5f) * 0.25f, redguy.getTranslation().z), null, null);
		chromeguy.passTwo(renderingProgram2, mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, chromeguyMove.set(chromeguy.getTranslation().x, chromeguy.getTranslation().y + (float)Math.sin(add * 0.5f) * 0.45f, chromeguy.getTranslation().z), null, null);
		lightball.passTwo(renderingProgram2, mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, Camera.get().c(), null, null);
	}
	
	private void setupVBuffers()
	{
		for(int j = 0; j < staticScenes.size(); j++)
		{
			//if(j != i)
			staticScenes.get(j).vBuffers();
		}
	}

	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		renderingProgram1 = Utils.createShaderProgram("a4/vert1shader.glsl", "a4/frag1shader.glsl");
		renderingProgram2 = Utils.createShaderProgram("a4/vert2shader.glsl", "a4/frag2shader.glsl");
		renderingProgramCubeMap = Utils.createShaderProgram("a4/vertCShader.glsl", "a4/fragCShader.glsl");

		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(fov), aspect, 0.1f, 1000.0f);

		lightLoc.mul(camScale);
		view.setAttrib(lightLoc.x, lightLoc.y, lightLoc.z, 9.0f, 1.0f);
		view.vertRot(-1.5707963267948f);

		setupVertices();
		setupShadowBuffers();
				
		b.set(
			0.5f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.5f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.5f, 0.0f,
			0.5f, 0.5f, 0.5f, 1.0f);
		
		startTime = System.currentTimeMillis();
	}
	
	private void setupShadowBuffers()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		scSizeX = myCanvas.getWidth();
		scSizeY = myCanvas.getHeight();
	
		gl.glGenFramebuffers(1, shadowBuffer, 0);
	
		gl.glGenTextures(1, shadowTex, 0);
		gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32,
						scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
		
		// may reduce shadow border artifacts
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	}

	private void setupVertices()
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();

		skybox = new SceneObject(new ImportedModel("../skybox.obj"), false, Utils.loadCubeMap("skybox_shots"));
		refspear1 = new SceneObject(new ImportedModel("../reflectspears/refspear1.obj"), false, skybox.getTexture());
		refspear2 = new SceneObject(new ImportedModel("../reflectspears/refspear2.obj"), false, skybox.getTexture());
		refspear1.setReflective(1);
		refspear1.setBumpy(1);
		refspear1.setBumpiness(new float[]{0.125f, 13.8f});
		refspear2.setReflective(1);
		refspear2.setBumpy(1);
		refspear2.setBumpiness(new float[]{0.125f, 13.8f});

		gl.glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
		//blender by default uses CCW winding order
		skybox.setWindingOrder(GL_CW);
	
		//all the .obj initializations done
		boolean[] no = {false};
		boolean[] leveltile = {true, false, true, false, false, false};
		staticScenes.add(new Scene("level", "level/textures", leveltile));
		/*
		//just an example of how to set a sub-scene's object to be reflective
		staticScenes.get(0).setOneTexture(skybox.getTexture(), 4);
		staticScenes.get(0).setReflective(1, 4);
		*/
		staticScenes.add(new Scene("lightball", "lightball/textures", no, view.c(), null, new Vector3f(0.25f, 0.25f, 0.25f)));
		staticScenes.get(1).setVisible(true);
		staticScenes.add(new Scene("signscene", "signscene/textures", no, new Vector3f(-9.205f, -0.76875f,  2.731f), new Vector4f(2.3561944f, 0.0f, 1.0f, 0.0f), new Vector3f(0.35f, 0.35f, 0.35f)));
		staticScenes.add(new Scene("tablescene", "tablescene/textures", no, new Vector3f(-10.94f, -0.38875f, -0.2291f), null, new Vector3f(0.5f, 0.5f, 0.5f)));
		
		blueguy = new Scene("blueguy", "blueguy/textures", no, new Vector3f(-8.767f,  0.06647f, -3.146f), new Vector4f(2.3561944f, 0.0f, 1.0f, 0.0f), new Vector3f(0.75f, 0.75f, 0.75f));
		redguy = new Scene("redguy", "redguy/textures", no, new Vector3f(-11.12f,  0.1142f,  1.186f), new Vector4f(0.7853981f, 0.0f, 1.0f, 0.0f), new Vector3f(0.2f, 0.2f, 0.2f));
		chromeguy = new Scene("chromeguy", "chromeguy/textures", no, new Vector3f(0.0f, 7.3f, 0.0f), null, null);
		//making the body of this chromeguy chrome
		chromeguy.setOneReflective(1, 0);
//		chromeguy.setOneBumpy(1, 0);
//		chromeguy.setOneBumpiness(new float[]{0.075f, 1.25f}, 0);
		chromeguy.setOneTexture(skybox.getTexture(), 0);
		lightball = new Scene("lightball", "lightball/textures", no, Camera.get().c(), null, new Vector3f(0.5f, 0.5f, 0.5f));
	}
	
	public void input()
	{
		if(Input.get().getNumKeyInputs() > 0)
		{
			if (Input.get().isKeyPressed(KeyEvent.VK_L))
			{
				if(!heldDown)
				{
					staticScenes.get(1).setVisible(!staticScenes.get(1).isVisible());
					System.out.println("lightball visibility changed...");
					heldDown = true;
				}
			}
			else
				heldDown = false;
			if (Input.get().isKeyPressed(KeyEvent.VK_W))
				Camera.get().addNeg(Camera.get().n());
			if (Input.get().isKeyPressed(KeyEvent.VK_S))
				Camera.get().addPos(Camera.get().n());
			if (Input.get().isKeyPressed(KeyEvent.VK_A))
				Camera.get().addNeg(Camera.get().u());
			if (Input.get().isKeyPressed(KeyEvent.VK_D))
				Camera.get().addPos(Camera.get().u());
			if (Input.get().isKeyPressed(KeyEvent.VK_E))
				Camera.get().addNeg(Camera.get().v());
			if (Input.get().isKeyPressed(KeyEvent.VK_Q))
				Camera.get().addPos(Camera.get().v());
			//System.out.println(Camera.get().c());
			if (Input.get().isKeyPressed(KeyEvent.VK_UP))
				Camera.get().vertRot(Camera.get().getRotationSpeed() * (float) tf);
			if (Input.get().isKeyPressed(KeyEvent.VK_RIGHT))
				Camera.get().horRot(-Camera.get().getRotationSpeed() * (float) tf);
			if (Input.get().isKeyPressed(KeyEvent.VK_LEFT))
				Camera.get().horRot(Camera.get().getRotationSpeed() * (float) tf);
			if (Input.get().isKeyPressed(KeyEvent.VK_DOWN))
				Camera.get().vertRot(-Camera.get().getRotationSpeed() * (float) tf);
			if (Input.get().isKeyPressed(KeyEvent.VK_SHIFT))
				Camera.get().setSprinting(true);
			else
				Camera.get().setSprinting(false);
		}
		else
			heldDown = false;
		
		if(Input.get().isMouseWheelMoved())
		{
			//lightLoc.add(0.0f, -(float)Input.get().mouseWheelTicks() * (float)tf, 0.0f);
			view.setAttrib(view.c().x, view.c().y - (float)Input.get().mouseWheelTicks() * 
			(float)tf * view.getSpeed(), view.c().z, view.getSpeed(), view.getRotationSpeed());
			//System.out.println(view.c().toString());
		}
		
		if(Input.get().isMousePressed(MouseEvent.BUTTON1))
		{
			//lightLoc.add(-Input.get().getMouseMotion().x *(float) tf * camScale * 10.0f, 0.0f, -Input.get().getMouseMotion().y * (float)tf * camScale * 10.0f);
			view.setAttrib(view.c().x + -Input.get().getMouseMotion().x *(float) tf,
			view.c().y, view.c().z + -Input.get().getMouseMotion().y * (float)tf,
			view.getSpeed(), view.getRotationSpeed());
			System.out.println(view.c().toString());
		}
	}
	
	private void installLights(int renderingProgram, Matrix4f vMatrix)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
	
		currentLightPos.mulPosition(vMatrix);
		lightPos[0]=currentLightPos.x(); lightPos[1]=currentLightPos.y(); lightPos[2]=currentLightPos.z();
		
		// set current material values
		matAmb = thisAmb;
		matDif = thisDif;
		matSpe = thisSpe;
		matShi = thisShi;
		
		// get the locations of the light and material fields in the shader
		globalAmbLoc = gl.glGetUniformLocation(renderingProgram, "globalAmbient");
		ambLoc = gl.glGetUniformLocation(renderingProgram, "light.ambient");
		diffLoc = gl.glGetUniformLocation(renderingProgram, "light.diffuse");
		specLoc = gl.glGetUniformLocation(renderingProgram, "light.specular");
		posLoc = gl.glGetUniformLocation(renderingProgram, "light.position");
		distLoc = gl.glGetUniformLocation(renderingProgram, "light.distance");
		intensityLoc = gl.glGetUniformLocation(renderingProgram, "light.intensity");
		mambLoc = gl.glGetUniformLocation(renderingProgram, "material.ambient");
		mdiffLoc = gl.glGetUniformLocation(renderingProgram, "material.diffuse");
		mspecLoc = gl.glGetUniformLocation(renderingProgram, "material.specular");
		mshiLoc = gl.glGetUniformLocation(renderingProgram, "material.shininess");
		cameraposLoc = gl.glGetUniformLocation(renderingProgram, "camera.position");
	
		//  set the uniform light and material values in the shader
		gl.glProgramUniform4fv(renderingProgram, globalAmbLoc, 1, globalAmbient, 0);
		gl.glProgramUniform4fv(renderingProgram, ambLoc, 1, lightAmbient, 0);
		gl.glProgramUniform4fv(renderingProgram, diffLoc, 1, lightDiffuse, 0);
		gl.glProgramUniform4fv(renderingProgram, specLoc, 1, lightSpecular, 0);
		gl.glProgramUniform3fv(renderingProgram, posLoc, 1, lightPos, 0);
		gl.glProgramUniform1f(renderingProgram, distLoc, lightDistance);
		gl.glProgramUniform1f(renderingProgram, intensityLoc, lightIntensity);
		gl.glProgramUniform4fv(renderingProgram, mambLoc, 1, matAmb, 0);
		gl.glProgramUniform4fv(renderingProgram, mdiffLoc, 1, matDif, 0);
		gl.glProgramUniform4fv(renderingProgram, mspecLoc, 1, matSpe, 0);
		gl.glProgramUniform1f(renderingProgram, mshiLoc, matShi);
		camPos[0] = Camera.get().c().x; camPos[1] = Camera.get().c().y; camPos[2] = Camera.get().c().z;
		gl.glProgramUniform3fv(renderingProgram, cameraposLoc, 1, camPos, 0);
	}

	public static void main(String[] args) { new Starter(); }
	public void dispose(GLAutoDrawable drawable) {}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(fov), aspect, 0.1f, 1000.0f);

		setupShadowBuffers();
	}
}