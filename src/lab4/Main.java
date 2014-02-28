package lab4;

import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.MotionPathListener;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;
import com.jme3.input.controls.ActionListener;


public class Main extends SimpleApplication {

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
//	boolean RunAnimation;

	public static void main(String[] args) {
		Main run = new Main();
		run.start();
	}

	static{
		box4 = new Box(0.5f, 0.5f, 0.5f);
	}

	@Override
	public void simpleInitApp() {
		flyCam.setMoveSpeed(30.0f);     
		cam.setLocation(new Vector3f(2f, 2f, 25f));
		cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
		initKeys();
		initInputs();

		//	    viewPort.setBackgroundColor(ColorRGBA.LightGray);

		/* Skybox */
		rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));

		/* Sun */
		//       DirectionalLight sun = new DirectionalLight();
		//       sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
		//       rootNode.addLight(sun);

		/** Create a pivot node at (0,0,0) and attach it to the root node */
		Node pivot = new Node("pivot");
		Cubes(pivot);
		initFloor();
		BoxTail();
		rootNode.attachChild(pivot); // put this node in the scene

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

//		RunAnimation = true;
	}

	public void initFloor(){
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

	public void BoxTail(){
		float start = 0.5f / 4;
		for (int j = 0; j < 5; j++) {
			Vector3f vt = new Vector3f(j * 0.5f * 4 + start, 8, 0);
			Create_Box(vt);
		}
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
		inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_T));
		inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_F));
		inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_H));
		inputManager.addMapping("Stop", new KeyTrigger(KeyInput.KEY_G));

		ActionListener actionListener = new ActionListener() {
			public void onAction(String name, boolean keyPressed, float tpf) {
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
		inputManager.addListener(actionListener, "Up", "Left", "Right", "Stop");
	}

	//http://hub.jmonkeyengine.org/wiki/doku.php/jme3:beginner:hello_animation
	public void initKeys() {
		inputManager.addMapping("Up1", new KeyTrigger(KeyInput.KEY_I));
		inputManager.addMapping("Left1", new KeyTrigger(KeyInput.KEY_J));
		inputManager.addMapping("Right1", new KeyTrigger(KeyInput.KEY_L));
		inputManager.addMapping("Reset1", new KeyTrigger(KeyInput.KEY_K));
		inputManager.addListener(analogListener, "Up1");
		inputManager.addListener(analogListener, "Left1");
		inputManager.addListener(analogListener, "Right1");
		inputManager.addListener(analogListener, "Reset1");
	}

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
	  
	  /*
		public void yellowBox(){
			box4 = new Box(1,1,1);  
			Geometry yellow = new Geometry("Box4", box4);
			yellow.setLocalTranslation(new Vector3f(0,5,0));
			Material mat4 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			mat4.setColor("Color", ColorRGBA.Yellow);
			yellow.setMaterial(mat4);
			rootNode.attachChild(yellow);              // make the cube appear in the scene
			pivot.attachChild(yellow);

		}*/

}
