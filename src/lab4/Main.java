package lab4;

import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.MotionPathListener;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;
import com.jme3.input.controls.ActionListener;


public class Main extends SimpleApplication implements ActionListener{

	Material mat_terrain;
	Material floor_mat;
	private static Box floor;
	private static Box box4;
	private Geometry blue;
	private Geometry red;
	private Geometry green;
	private MotionPath path1;
	private MotionPath path2;
	private MotionPath path3;
	private MotionEvent motionControl1;
	private MotionEvent motionControl2;
	private MotionEvent motionControl3;
	
	private Spatial sceneModel;
	private BulletAppState bulletAppState;
	private RigidBodyControl landscape;
	private CharacterControl player;
	private Vector3f walkDirection = new Vector3f();
	private boolean left = false, right = false, up = false, down = false;
	private Vector3f camDir = new Vector3f();
	private Vector3f camLeft = new Vector3f();


	public static void main(String[] args) {
		Main run = new Main();
		run.start();
	}
	
	private Node shootable;
	private Geometry mark;
	
	private Vector3f CamStartLoc = new Vector3f(2f, 2f, 25f);
	private Vector3f CamStartLook = new Vector3f(0f, 0f, 0f);
	Vector3f CamLoc = new Vector3f();
	private Vector3f CamLook = new Vector3f();
	private Vector3f CamGod = new Vector3f(0f, 150f, 0f);
	private Vector3f CamZero = new Vector3f(0f, 0f, 0f);
	private boolean changeCam = true;

	static{
		box4 = new Box(0.5f, 0.5f, 0.5f);
	}

	@Override
	public void simpleInitApp() {

		bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);
		
		viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
		flyCam.setMoveSpeed(30.0f);   
		/*
		cam.setLocation(CamStartLoc);
		cam.lookAt(CamStartLook, Vector3f.UNIT_Y);
		*/
		
		//initKeys();
		initInputs();
		setUpLight();
		initMark();
		initCrossHairs();

		
		assetManager.registerLocator("town.zip", ZipLocator.class);
		sceneModel = assetManager.loadModel("main.scene");
		//sceneModel = assetManager.loadModel("assets/Scenes/main.scene");
		sceneModel.setLocalScale(2f);
		

		CollisionShape sceneShape =	CollisionShapeFactory.createMeshShape((Node) sceneModel);
		landscape = new RigidBodyControl(sceneShape, 0);
		sceneModel.addControl(landscape);

		CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
	    player = new CharacterControl(capsuleShape, 0.05f);
	    player.setJumpSpeed(20);
	    player.setFallSpeed(30);
	    player.setGravity(30);
	    player.setPhysicsLocation(new Vector3f(0, 15, 0));
		
	    rootNode.attachChild(sceneModel);
	    bulletAppState.getPhysicsSpace().add(landscape);
	    bulletAppState.getPhysicsSpace().add(player);

	    
		shootable = new Node("Shootables");
		rootNode.attachChild(shootable);
		
		Cubes(shootable);
		//initFloor(shootable);
		//Motions();
			    
//		viewPort.setBackgroundColor(ColorRGBA.LightGray);

		/* Skybox */
		//rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));

		/* Sun */
		//       DirectionalLight sun = new DirectionalLight();
		//       sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
		//       rootNode.addLight(sun);

		/** Create a pivot node at (0,0,0) and attach it to the root node */
		//Node pivot = new Node("pivot");
		//Cubes(pivot);

		//rootNode.attachChild(pivot); // put this node in the scene

//		RunAnimation = true;
	}
	
	private void setUpLight() {
		// We add light so we see the scene
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(1.3f));
		rootNode.addLight(al);

		DirectionalLight dl = new DirectionalLight();
		dl.setColor(ColorRGBA.White);
		dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
		rootNode.addLight(dl);
	}
	
	public void Motions(){
		/* https://code.google.com/p/jmonkeyengine/source/browse/trunk/engine/src/test/jme3test/animation/TestMotionPath.java */
		path1 = new MotionPath();
		path1.setCycle(true);
		path1.addWayPoint(new Vector3f(-3, 0, 0));
		path1.addWayPoint(new Vector3f(-10, 0, 0));
		//		path1.enableDebugShape(assetManager, rootNode);

		motionControl1 = new MotionEvent(red, path1);
		motionControl1.setLoopMode(LoopMode.Loop);
		motionControl1.setDirectionType(MotionEvent.Direction.Path);
		motionControl1.setInitialDuration(10f);
		motionControl1.setSpeed(2f);

		path2 = new MotionPath();
		path2.setCycle(true);
		path2.addWayPoint(new Vector3f(3, 0, 0));
		path2.addWayPoint(new Vector3f(10, 0, 0));
		//		path2.enableDebugShape(assetManager, rootNode);

		motionControl2 = new MotionEvent(green, path2);
		motionControl2.setLoopMode(LoopMode.Loop);
		motionControl2.setDirectionType(MotionEvent.Direction.Path);
		motionControl2.setInitialDuration(10f);
		motionControl2.setSpeed(2f);

		path3 = new MotionPath();
		path3.setCycle(true);
		path3.addWayPoint(new Vector3f(0, 0, 0));
		path3.addWayPoint(new Vector3f(0, 4, 0));
		//		path3.enableDebugShape(assetManager, rootNode);

		motionControl3 = new MotionEvent(blue, path3);
		motionControl3.setLoopMode(LoopMode.Loop);
		motionControl3.setDirectionType(MotionEvent.Direction.Path);
		motionControl3.setInitialDuration(10f);
		motionControl3.setSpeed(2f);
	}

	public void initFloor(Node shootable){
		/* Floor */
		floor = new Box(100f, 0.1f, 100f);
		Geometry floor_geo = new Geometry("Floor", floor);
		floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		TextureKey key3 = new TextureKey("Textures/Terrain/splat/grass.jpg");
		key3.setGenerateMips(true);
		Texture tex3 = assetManager.loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);
		floor_mat.setTexture("ColorMap", tex3);
		floor_geo.setMaterial(floor_mat);
		floor_geo.setLocalTranslation(0, -1.5f, 0);
		rootNode.attachChild(floor_geo);
		shootable.attachChild(floor_geo);
	}
	
	public void Cubes(Node pivot){
		/* Cubes */
		Box box1 = new Box(1, 1, 1);
		blue = new Geometry("Box1", box1);
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", ColorRGBA.Blue);
		blue.setMaterial(mat);
		rootNode.attachChild(blue);              // make the cube appear in the scene

		Box box2 = new Box(1,1,1);      
		red = new Geometry("Box2", box2);
		red.setLocalTranslation(new Vector3f(-3,0,0));
		Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat2.setColor("Color", ColorRGBA.Red);
		red.setMaterial(mat2);
		rootNode.attachChild(red);              // make the cube appear in the scene

		Box box3 = new Box(1,1,1);      
		green = new Geometry("Box3", box3);
		green.setLocalTranslation(new Vector3f(3,0,0));
		Material mat3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat3.setColor("Color", ColorRGBA.Green);
		green.setMaterial(mat3);
		rootNode.attachChild(green);              // make the cube appear in the scene

		//		Box box4 = new Box(1,1,1);      
		//		Geometry yellow = new Geometry("Box3", box4);
		//		yellow.setLocalTranslation(new Vector3f(0,5,0));
		//		Material mat4 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		//		mat4.setColor("Color", ColorRGBA.Yellow);
		//		yellow.setMaterial(mat4);
		//		rootNode.attachChild(yellow);              // make the cube appear in the scene

		pivot.attachChild(blue);
		pivot.attachChild(red);
		pivot.attachChild(green);
	}

	public void Create_Box(Vector3f location){
		/** Create a brick geometry and attach to scene graph. */
		Geometry box_geo = new Geometry("box", box4);
		Material mat4 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat4.setColor("Color", ColorRGBA.Yellow);
		box_geo.setMaterial(mat4);
		rootNode.attachChild(box_geo);
		/** Position the box geometry  */
		box_geo.setLocalTranslation(location);
	}

	// Skapar en animation av de tre cuberna.
	public void initInputs() {
		/*
		inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_T));
		inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_F));
		inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_H));
		inputManager.addMapping("Stop", new KeyTrigger(KeyInput.KEY_G));
		inputManager.addMapping("Switch", new KeyTrigger(KeyInput.KEY_1));
		*/
	    inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
	    inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
	    inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
	    inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
	    inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addMapping("Fire", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addMapping("Switch", new KeyTrigger(KeyInput.KEY_1));
		inputManager.addListener(this, "Left", "Right", "Up", "Down", "Jump");
		inputManager.addListener(this, "Fire");
		inputManager.addListener(this, "Switch");
		
		/*
		ActionListener actionListener = new ActionListener() {
			public void onAction(String name, boolean keyPressed, float tpf) {
				/*
				if (name.equals("Up") && keyPressed) {
					motionControl3.play();
				}
				if (name.equals("Left") && keyPressed) {
					motionControl1.play();
				}
				if (name.equals("Right") && keyPressed) {
					motionControl2.play();
				}
				if (name.equals("Stop") && keyPressed){
					motionControl1.stop();
					motionControl2.stop();
					motionControl3.stop();
				}
			}
		};
		
		//inputManager.addListener(actionListener, "Up", "Left", "Right", "Stop");
		inputManager.addListener(actionListener, "Fire");
		inputManager.addListener(actionListener, "Switch");
		inputManager.addListener(actionListener, "Left", "Right", "Up", "Down", "Jump");
		*/
	}
	
	public void onAction(String name, boolean keyPressed, float tpf){
		
		if(name.equals("Fire") && keyPressed){
			CollisionResults results = new CollisionResults();
			Ray ray = new Ray(cam.getLocation(), cam.getDirection());
			shootable.collideWith(ray, results);

			System.out.println("----- Collisions? " + results.size() + "-----");
			for (int i = 0; i < results.size(); i++) {
				// For each hit, we know distance, impact point, name of geometry.
				float dist = results.getCollision(i).getDistance();
				Vector3f pt = results.getCollision(i).getContactPoint();
				String hit = results.getCollision(i).getGeometry().getName();
				System.out.println("* Collision #" + i);
				System.out.println("  You shot " + hit + " at " + pt + ", " + dist + " wu away.");		
			}
			// 5. Use the results (we mark the hit object)
			if (results.size() > 0) {
				// The closest collision point is what was truly hit:
				CollisionResult closest = results.getClosestCollision();
				// Let's interact - we mark the hit with a red dot.
				mark.setLocalTranslation(closest.getContactPoint());
				rootNode.attachChild(mark);
			} else {
				// No hits? Then remove the red mark.
				rootNode.detachChild(mark);
			}
		}
		
		if (name.equals("Switch") && keyPressed){
			if(changeCam){
				//flyCam.setEnabled(false);
				changeCam = false; 
				
				CamLoc = cam.getLocation();
				System.out.println("CamLoc Before: " + CamLoc);
				
				CamLook = cam.getDirection();
				System.out.println("CamLook Before: " + CamLook);
				
				cam.setLocation(CamGod);
				cam.lookAt(CamZero, Vector3f.UNIT_Y);
				
			}else{
				//flyCam.setEnabled(true);
				changeCam = true;
				System.out.println("CamLoc After: " + CamLoc);
				cam.setLocation(CamLoc);
				cam.lookAt(CamLook, Vector3f.UNIT_Y);
			}
		}
		
		if (name.equals("Left")){
			left = keyPressed;
		}else if(name.equals("Right")){
			right = keyPressed;
		}else if(name.equals("Up")){
			up = keyPressed;
		}else if(name.equals("Down")){
			down = keyPressed;
		}else if(name.equals("Jump")){
			if(keyPressed){
				player.jump();
			}
		}
	}
	
	public void simpleUpdate(float tpf){
		camDir.set(cam.getDirection()).multLocal(0.6f);
		camLeft.set(cam.getLeft()).multLocal(0.4f);
		walkDirection.set(0,0,0);
		if(left){
			walkDirection.addLocal(camLeft);
		}
		if(right){
			walkDirection.addLocal(camLeft.negate());
		}
		if(up){
			walkDirection.addLocal(camDir);
		}
		if(down){
			walkDirection.addLocal(camDir.negate());
		}
		player.setWalkDirection(walkDirection);
		cam.setLocation(player.getPhysicsLocation());
	}
	
	protected void initMark() {
		Sphere sphere = new Sphere(30, 30, 0.2f);
		mark = new Geometry("BOOM!", sphere);
		Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mark_mat.setColor("Color", ColorRGBA.Red);
		mark.setMaterial(mark_mat);
	}
	
	protected void initCrossHairs() {
		setDisplayStatView(false);
		guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
		BitmapText ch = new BitmapText(guiFont, false);
		ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
		ch.setText("+"); // crosshairs
		ch.setLocalTranslation( // center
				settings.getWidth() / 2 - ch.getLineWidth()/2, settings.getHeight() / 2 + ch.getLineHeight()/2, 0);
		guiNode.attachChild(ch);
	}
	

	//http://hub.jmonkeyengine.org/wiki/doku.php/jme3:beginner:hello_animation
	public void initKeys() {
		/*
		inputManager.addMapping("Up1", new KeyTrigger(KeyInput.KEY_I));
		inputManager.addMapping("Left1", new KeyTrigger(KeyInput.KEY_J));
		inputManager.addMapping("Right1", new KeyTrigger(KeyInput.KEY_L));
		inputManager.addMapping("Reset1", new KeyTrigger(KeyInput.KEY_K));
		inputManager.addListener(analogListener, "Up1");
		inputManager.addListener(analogListener, "Left1");
		inputManager.addListener(analogListener, "Right1");
		inputManager.addListener(analogListener, "Reset1");
		*/

	}

	/*
	//Om man vill kunna röra på cuberna med hjälp av tangentbordet.
	private AnalogListener analogListener = new AnalogListener() {
	    public void onAnalog(String name, float value, float tpf) {
	    	if (name.equals("Up1")) {
	    		Vector3f v = blue.getLocalTranslation();
	    		blue.setLocalTranslation(v.x, v.y + value*speed*5, v.z);
	    	}
	    	if (name.equals("Left1")) {
	    		Vector3f v = red.getLocalTranslation();
	    		red.setLocalTranslation(v.x - value*speed*5, v.y, v.z);
	    	}
	    	if (name.equals("Right1")) {
	    		Vector3f v = green.getLocalTranslation();
	    		green.setLocalTranslation(v.x + value*speed*5, v.y, v.z);
	    	}
	    	if (name.equals("Reset1")) {
	    		blue.setLocalTranslation(0,0,0);
	    		red.setLocalTranslation(-3,0,0);
	    		green.setLocalTranslation(3,0,0);
	    	}
	    }
	  };
	  */
}
