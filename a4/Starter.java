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
	private Random rand = new Random();
	private float randy, randmuch;
	private float[] speed = new float[3];
	private int randyLoc, ogLenLoc, speedLoc;
	private int adder = 0;
	private GLCanvas myCanvas;
	private int renderingProgram1, renderingProgram2, hairProgram;

	private float fov = 70.0f;

	private boolean heldDown[] = {false, false};

	private ViewMat view = new ViewMat();

	// model stuff
	private ArrayList<Scene> staticScenes = new ArrayList<Scene>();
	private Scene blueguy, redguy, chromeguy, lightball;
	private NoiseObject noisy, noiseguy, forcefield;
	private Scene noiseguyeye;

	private SceneObject skybox, refspear1, refspear2,  reflectcarrierlegs, windows, grassGeo, dirtGeo;
	
	//variables for moving scenes
	private Vector3f blueguyMove = new Vector3f();
	private Vector3f redguyMove = new Vector3f();
	private Vector3f chromeguyMove = new Vector3f();
	private Vector4f chromeguyRotate = new Vector4f();
	private Vector3f cameraLoc = new Vector3f(12.37f, 5.0f, 0.2f);
	private Vector3f lightLoc = new Vector3f(-5.82f, 192.0f, 6.561f);

	private Vector3f[] points = new Vector3f[8];
	
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

	//fog stuff
	private int fogLoc, fogColorLoc, fogStartLoc, fogEndLoc, fog = 1;
	private float[] fogColor = {0.7f, 0.8f, 0.9f, 1.0f};
	private float fogStart = 10.0f, fogEnd = 80.0f;

	// allocate variables for display() function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private FloatBuffer vec4vals = Buffers.newDirectFloatBuffer(4);
	private FloatBuffer vecvals = Buffers.newDirectFloatBuffer(3);
	private Matrix4f pMat = new Matrix4f();  // perspective matrix
	private Matrix4f vMat = new Matrix4f();  // view matrix
	private Matrix4f mMat = new Matrix4f();  // model matrix
	private Matrix4f mvMat = new Matrix4f(); // model-view matrix
	private Matrix4f invTrMat = new Matrix4f(); // inverse-transpose
	private int mvLoc, projLoc, nLoc, sLoc, timeLoc, hairTimeLoc;
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
	{
		setTitle("Assignment #4");
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
		
		Camera.get().setAttrib(cameraLoc.x(), cameraLoc.y(), cameraLoc.z(), 9.0f, 1.5707f);
		Camera.get().horRot(1.5707963267f);
		
		this.add(myCanvas);
		this.setVisible(true);
		Animator animator = new Animator(myCanvas);
		animator.start();
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	public void init(GLAutoDrawable drawable)
	{
		renderingProgram1 = Utils.createShaderProgram("a4/vert1shader.glsl", "a4/frag1shader.glsl");
		renderingProgram2 = Utils.createShaderProgram("a4/vert2shader.glsl", "a4/frag2shader.glsl");
		renderingProgramCubeMap = Utils.createShaderProgram("a4/vertCShader.glsl", "a4/fragCShader.glsl");
		hairProgram = Utils.createShaderProgram("a4/vertHairShader.glsl", "a4/geoHairShader.glsl", "a4/fragHairShader.glsl");

		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(fov), aspect, 0.1f, 1000.0f);
		//pMat.identity().setOrtho((float)-myCanvas.getWidth() * 0.125f, (float)myCanvas.getWidth() * 0.125f, (float)-myCanvas.getHeight() * 0.125f, (float)myCanvas.getHeight() * 0.125f, 1.0f, 100.0f);

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

	public void display(GLAutoDrawable drawable)
	{
		randy += rand.nextFloat() * 0.0016f;
		randmuch = rand.nextFloat() * 0.1f;

		for(int i = 0; i < 3; i++)
		{
			speed[i] = rand.nextFloat() * randmuch;
		}
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClearColor(fogColor[0], fogColor[1], fogColor[2], fogColor[3]);
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		//currentLightPos.set(view.c());
		currentLightPos.set(Camera.get().c().x, view.c().y, Camera.get().c().z);
		
		elapsedTime = System.currentTimeMillis() - startTime;
		tf = elapsedTime / 1000.0;
		add += (float)tf;
		
		//lightVmat.set(view.viewMat((float)tf));
		lightVmat.identity().setLookAt(view.c(), view.n(), view.v());
		//lightVmat.identity().setLookAt(currentLightPos.mul(1.5f), view.c(), up);
		lightPmat.identity().setPerspective((float) Math.toRadians(46.5f), 1.0f, 0.1f, 1000.0f);

		//lightPmat.identity().setOrtho(-64.0f, 64.0f, -64.0f, 64.0f, 1.0f, 200.0f);

		gl.glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer[0]);
		gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowTex[0], 0);
	
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
		noisy.passOne(renderingProgram1, lightPmat, lightVmat, noisy.getTranslation(), null, null);
		noiseguy.passOne(renderingProgram1, lightPmat, lightVmat, noiseguy.setTranslation(noiseguy.getTranslation().add(0.0f, (float)Math.sin(add) * 0.01f, 0.0f)), noiseguy.setRotation(noiseguy.getRotation().set((float)Math.cos(add + rand.nextFloat() * tf), 0.0f, 1.0f, 0.0f)), null);
		noiseguyeye.passOne(renderingProgram1, lightPmat, lightVmat, noiseguy.getTranslation(), noiseguy.getRotation(), null);
		//grassGeo.passOne(renderingProgram1, lightPmat, lightVmat, null, null, null);
		//dirtGeo.passOne(renderingProgram1, lightPmat, lightVmat, null, null, null);
		refspear2.passOne(renderingProgram1, lightPmat, lightVmat, null, null, null);
		reflectcarrierlegs.passOne(renderingProgram1, lightPmat, lightVmat, null, null, null);
		blueguy.passOne(renderingProgram1, lightPmat, lightVmat, blueguyMove.set(blueguy.getTranslation().x, blueguy.getTranslation().y + (float)Math.sin(add) * -0.35f, blueguy.getTranslation().z), null, null);
		redguy.passOne(renderingProgram1, lightPmat, lightVmat, redguyMove.set(redguy.getTranslation().x, redguy.getTranslation().y + (float)Math.sin(add * 2.5f) * 0.25f, redguy.getTranslation().z), null, null);
		chromeguy.passOne(renderingProgram1, lightPmat, lightVmat, chromeguyMove.set(chromeguy.getTranslation().x, chromeguy.getTranslation().y + (float)Math.sin(add * 0.5f) * 0.45f, chromeguy.getTranslation().z), chromeguyRotate.set(Camera.get().rotationVec().y - 1.5707963f, 0.0f, 1.0f, 0.0f), null);
		if(lightball.isVisible())
			lightball.passOne(renderingProgram1, lightPmat, lightVmat, Camera.get().c(), null, null);
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passTwo()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		vMat.set(Camera.get().viewMat((float)tf));
		gl.glUseProgram(renderingProgramCubeMap);

		if(fog == 0)
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
		fogLoc = gl.glGetUniformLocation(renderingProgram2, "fog.enabled");
		fogColorLoc = gl.glGetUniformLocation(renderingProgram2, "fog.color");
		fogStartLoc = gl.glGetUniformLocation(renderingProgram2, "fog.start");
		fogEndLoc = gl.glGetUniformLocation(renderingProgram2, "fog.end");
		
		gl.glProgramUniform1f(renderingProgram2, timeLoc, add);
		gl.glProgramUniform1i(renderingProgram2, skyBoxLoc, 1);
		gl.glProgramUniform1i(renderingProgram2, fogLoc, fog);
		gl.glProgramUniform1f(renderingProgram2, screenXLoc, scSizeX);
		gl.glProgramUniform1f(renderingProgram2, screenXLoc, scSizeY);
		gl.glProgramUniform1f(renderingProgram2, fogStartLoc, fogStart);
		gl.glProgramUniform1f(renderingProgram2, fogEndLoc, fogEnd);
		gl.glProgramUniform4fv(renderingProgram2, fogColorLoc, 1, fogColor, 0);

		gl.glProgramUniform1i(renderingProgram2, skyBoxLoc, 0);

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
		noiseguy.passTwo(renderingProgram2, mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, noiseguy.getTranslation(), noiseguy.getRotation(), null);
		noiseguyeye.passTwo(renderingProgram2, mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, noiseguy.getTranslation(), noiseguy.getRotation(), null);
		forcefield.passTwo(renderingProgram2, mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, null, null, null);
		reflectcarrierlegs.passTwo(renderingProgram2, mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, null, null, null);
		blueguy.passTwo(renderingProgram2, mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, blueguyMove.set(blueguy.getTranslation().x, blueguy.getTranslation().y + (float)Math.sin(add) * -0.35f, blueguy.getTranslation().z), null, null);
		redguy.passTwo(renderingProgram2, mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, redguyMove.set(redguy.getTranslation().x, redguy.getTranslation().y + (float)Math.sin(add * 2.5f) * 0.25f, redguy.getTranslation().z), null, null);
		chromeguy.passTwo(renderingProgram2, mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, chromeguyMove.set(chromeguy.getTranslation().x, chromeguy.getTranslation().y + (float)Math.sin(add * 0.5f) * 0.45f, chromeguy.getTranslation().z), chromeguyRotate.set(Camera.get().rotationVec().y - 1.5707963f, 0.0f, 1.0f, 0.0f), null);
		lightball.passTwo(renderingProgram2, mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, Camera.get().c(), null, null);
		windows.passTwo(renderingProgram2, mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, null, null, null);
		gl.glProgramUniform1f(renderingProgram2, timeLoc, add * 0.75f);
		noisy.passTwo(renderingProgram2, mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, noisy.getTranslation(), null, null);

		gl.glUseProgram(hairProgram);

		thisAmb = lmatAmb;
		thisDif = lmatDif;
		thisSpe = lmatSpe;
		thisShi = lmatShi;

		mvLoc = gl.glGetUniformLocation(hairProgram, "mv_matrix");
		projLoc = gl.glGetUniformLocation(hairProgram, "proj_matrix");
		nLoc = gl.glGetUniformLocation(hairProgram, "norm_matrix");
		sLoc = gl.glGetUniformLocation(hairProgram, "shadowMVP");
		fogLoc = gl.glGetUniformLocation(hairProgram, "fog.enabled");
		fogColorLoc = gl.glGetUniformLocation(hairProgram, "fog.color");
		fogStartLoc = gl.glGetUniformLocation(hairProgram, "fog.start");
		fogEndLoc = gl.glGetUniformLocation(hairProgram, "fog.end");
		hairTimeLoc = gl.glGetUniformLocation(hairProgram, "time");
		randyLoc = gl.glGetUniformLocation(hairProgram, "randy");
		ogLenLoc = gl.glGetUniformLocation(hairProgram, "ogLen");
		for(int i = 0; i < 3; i++)
		{
			speedLoc = gl.glGetUniformLocation(hairProgram, "speed[" + i + "]");
			gl.glProgramUniform1f(hairProgram, speedLoc, speed[i]);
		}

		gl.glProgramUniform1f(hairProgram, hairTimeLoc, add * (float)tf);
		gl.glProgramUniform1i(hairProgram, fogLoc, fog);
		gl.glProgramUniform1f(hairProgram, randyLoc, randy);
		gl.glProgramUniform1f(hairProgram, ogLenLoc, 1.25f);
		gl.glProgramUniform1f(hairProgram, fogStartLoc, fogStart);
		gl.glProgramUniform1f(hairProgram, fogEndLoc, fogEnd);
		gl.glProgramUniform4fv(hairProgram, fogColorLoc, 1, fogColor, 0);

		installLights(hairProgram, vMat);

		grassGeo.passTwo(hairProgram, mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, null, null, null);
		//forcefield.passTwo(hairProgram, mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, null, null, null);
		//noisy.passTwo(hairProgram, mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, null, null, null);

		installLights(hairProgram, vMat);
		randy = rand.nextFloat() * 4.0f;
		gl.glProgramUniform1f(hairProgram, randyLoc, randy);
		gl.glProgramUniform1f(hairProgram, ogLenLoc, 0.5f);
		dirtGeo.passTwo(hairProgram, mvLoc, projLoc, nLoc, sLoc, pMat, vMat, lightPmat, lightVmat, null, null, null);
	}
	
	private void setupVBuffers()
	{
		for(int j = 0; j < staticScenes.size(); j++)
		{
			//if(j != i)
			staticScenes.get(j).vBuffers();
		}
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

		skybox = new SceneObject(new ImportedModel("../skybox.obj"), false, Utils.loadCubeMap("skybox_shots"), -1);
		refspear1 = new SceneObject(new ImportedModel("../reflectspears/refspear1.obj"), false, skybox.getTexture(), Utils.loadTexture("normals/polestuffnorm.png", true));
		refspear2 = new SceneObject(new ImportedModel("../reflectspears/refspear2.obj"), false, skybox.getTexture(), Utils.loadTexture("normals/polestuffnorm.png", true));
		reflectcarrierlegs = new SceneObject(new ImportedModel("../reflectobjects/reflect_carrier_legs.obj"), false, skybox.getTexture(), Utils.loadTexture("normals/polestuffnorm.png", true));

		refspear1.setReflective(1);
		refspear1.setBumpy(1);
		refspear1.setBottomGear(1);
		refspear1.setBumpiness(new float[]{0.0625f, 1.38f});

		refspear2.setReflective(1);
		refspear2.setBumpy(1);
		refspear2.setBottomGear(1);
		refspear2.setBumpiness(new float[]{0.0625f, 1.38f});

		reflectcarrierlegs.setReflective(1);
		reflectcarrierlegs.setTransparent(true);
		reflectcarrierlegs.setTransparency(new float[]{0.5f, 0.8f});

		noisy = new NoiseObject(new ImportedModel("../noiseObjects/noise_beam.obj"), 128, 128, 128, 4);
		noisy.setScale(new Vector3f(16.0f, 16.0f, 16.0f));
		noisy.setTransparent(true);
		noisy.setTransparency(new float[]{0.25f, 0.85f});
		noisy.setBumpy(1);
		noisy.setBumpiness(new float[]{0.125f, 2.0f});
		noiseguy = new NoiseObject(new ImportedModel("../noiseguy/noiseguy.obj"), 64, 64, 64, 2, Utils.loadTexture("normals/bumpy.png", true));
		noiseguy.setTranslation(new Vector3f(-6.301f, 1.94f, -22.17f));
		noiseguy.setRotation(new Vector4f());
		noiseguy.setBumpy(1);
		noiseguy.setBumpiness(new float[]{1.25f, 20.0f});
		forcefield = new NoiseObject(new ImportedModel("../noiseObjects/forcefield.obj"), 128, 128, 128, 8);
		forcefield.setTransparent(true);
		forcefield.setTransparency(new float[]{0.35f, 0.65f});
		forcefield.setBumpy(1);
		forcefield.setBumpiness(new float[]{1.25f, 12.50f});

		noiseguyeye = new Scene("noiseguy/eyes", "noiseguy/textures", "noiseguy/normals", new boolean[]{false});
		//noisy.setReflective(1);

		windows = new SceneObject(new ImportedModel("../windows/windows.obj"), false, skybox.getTexture(), Utils.loadTexture("windows/windowsnorm.png", true));
		windows.setTransparent(true);
		windows.setTransparency(new float[]{0.3f, 0.5f});
		windows.setReflective(1);
		windows.setBumpy(1);
		windows.setBumpiness(new float[]{1.25f, 12.5f});


		grassGeo = new SceneObject(new ImportedModel("../grassgeo/grassgeo.obj"), true, Utils.loadTexture("level/textures/grass.png", true), -1);
		dirtGeo = new SceneObject(new ImportedModel("../grassgeo/dirtgeo.obj"), true, Utils.loadTexture("level/textures/dirtstuff.png", true), -1);

		gl.glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
		//blender by default uses CCW winding order
		skybox.setWindingOrder(GL_CW);
	
		//all the .obj initializations done
		boolean[] no = {false};
		boolean[] leveltile = {true, false, true, false, false, false};
		staticScenes.add(new Scene("level", "level/textures", "level/normals", leveltile));

		//just an example of how to set a sub-scene's object to be reflective
		//staticScenes.get(0).setOneTexture(skybox.getTexture(), 4);
		//staticScenes.get(0).setOneReflective(1, 4);

		staticScenes.add(new Scene("lightball", "lightball/textures", "normals", no, view.c(), null, new Vector3f(0.25f, 0.25f, 0.25f)));
		staticScenes.get(1).setVisible(true);
		staticScenes.add(new Scene("signscene", "signscene/textures", "normals", no, new Vector3f(-9.205f, -0.76875f,  2.731f), new Vector4f(2.3561944f, 0.0f, 1.0f, 0.0f), new Vector3f(0.35f, 0.35f, 0.35f)));
		staticScenes.add(new Scene("tablescene", "tablescene/textures", "normals", no, new Vector3f(-10.94f, -0.38875f, -0.2291f), null, new Vector3f(0.5f, 0.5f, 0.5f)));
		/*
		//unsure why, but if I make objects transparent, any objects that are behind them are not rendered
		// Only happens if they are rendered with pass2() before another object
		// they are being occluded?
		float[] sceneTrans = { 1.0f, 1.0f };
		for(int i = 0; i < staticScenes.size(); i++)
		{
			staticScenes.get(i).setAllTransparent(true);
			staticScenes.get(i).setAllTransparency(sceneTrans);
		}
		 */

		int index = 7;
		staticScenes.get(3).setOneReflective(1, index);
		staticScenes.get(3).setOneBumpy(1, index);
		staticScenes.get(3).setOneBumpiness(new float[]{15.0f, 13.8f}, index);
		staticScenes.get(3).setOneTexture(skybox.getTexture(), index);
		staticScenes.add(new Scene("reflectobjects/refmound", "reflectobjects/refmound/textures", "normals", no, null, null, null));
		staticScenes.get(4).setOneReflective(1, 0);
		staticScenes.get(4).setOneBumpy(1, 0);
		staticScenes.get(4).setOneBumpiness(new float[]{0.25f, 6.9f}, 0);
		staticScenes.get(4).setOneTexture(skybox.getTexture(), 0);

		boolean[] buildingtile = {false, false, false, false, false};
		staticScenes.add(new Scene("level/building", "level/building/textures", "level/building/normals", buildingtile, null, null, null));
		staticScenes.get(5).setOneTexture(skybox.getTexture(), 4);
		staticScenes.get(5).setOneReflective(1, 4);
		staticScenes.get(5).setOneBumpy(1, 4);
		staticScenes.get(5).setOneBumpiness(new float[]{0.125f, 1.25f}, 4);
		staticScenes.get(5).setOneBottomGear(1, 4);

		for(int i = 0; i < points.length; i++)
		{
			points[i] = new Vector3f();
		}

		blueguy = new Scene("blueguy", "blueguy/textures", "normals", no, new Vector3f(-8.767f,  0.06647f, -3.146f), new Vector4f(2.3561944f, 0.0f, 1.0f, 0.0f), new Vector3f(0.75f, 0.75f, 0.75f));
		blueguy.setOneBumpy(1, 0);
		blueguy.setOneBumpiness(new float[]{0.1875f, 2.5f}, 0);
		redguy = new Scene("redguy", "redguy/textures", "normals", no, new Vector3f(-11.12f,  0.1142f,  1.186f), new Vector4f(0.7853981f, 0.0f, 1.0f, 0.0f), new Vector3f(0.2f, 0.2f, 0.2f));
		redguy.setOneBumpy(1, 2);
		redguy.setOneBumpiness(new float[]{0.375f, 1.25f}, 2);
		chromeguy = new Scene("chromeguy", "chromeguy/textures", "normals", no, new Vector3f(0.0f, 7.3f, 0.0f), null, null);
		//making the body of this chromeguy chrome
		chromeguy.setOneReflective(1, 0);
		chromeguy.setOneBumpy(1, 0);
		chromeguy.setOneBumpiness(new float[]{0.075f, 1.25f}, 0);
		chromeguy.setOneTexture(skybox.getTexture(), 0);
		chromeguy.setOneTransparent(true, 0);
		chromeguy.setOneTransparency(new float[]{0.15f, 0.85f}, 0);
		lightball = new Scene("lightball", "lightball/textures", "normals", no, Camera.get().c(), null, new Vector3f(0.5f, 0.5f, 0.5f));
	}
	
	public void input()
	{
		if(Input.get().getNumKeyInputs() > 0)
		{
			if (Input.get().isKeyPressed(KeyEvent.VK_L))
			{
				if(!heldDown[0])
				{
					staticScenes.get(1).setVisible(!staticScenes.get(1).isVisible());
					System.out.println("lightball visibility changed...");
					heldDown[0] = true;
				}
			}
			else
				heldDown[0] = false;
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
			if(Input.get().isKeyPressed(KeyEvent.VK_F))
			{
				if(!heldDown[1])
				{
					if (fog != 0)
						fog = 0;
					else
						fog = 1;
					heldDown[1] = true;
				}
			}
			else
				heldDown[1] = false;
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
		{
			for(int i = 0; i < heldDown.length; i++)
				heldDown[i] = false;
		}
		
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
	{
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(fov), aspect, 0.1f, 1000.0f);
		//pMat.identity().setOrtho((float)-myCanvas.getWidth(), (float)myCanvas.getWidth(), (float)-myCanvas.getHeight(), (float)myCanvas.getHeight(), 1.0f, 1000.0f);

		setupShadowBuffers();
	}
}