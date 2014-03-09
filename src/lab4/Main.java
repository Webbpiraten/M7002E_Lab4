package lab4;

import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;


public class Main extends SimpleApplication implements ActionListener{

	Material mat_terrain;
	Material floor_mat;
	private static Box box4;
	private Geometry blue;
	private Geometry red;
	private Geometry green;
	private Geometry yellow;
	private Geometry black;
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

	private Node shootable;
	private Geometry mark;
	
	Vector3f CamLoc = new Vector3f();
	private Vector3f PlayerLook = new Vector3f();
	private boolean changeCam = true;
	
	BitmapText ch;
	
	private boolean haveRemote = false;
	private boolean haveKey = false;
	private boolean haveDuplicator = false;
	
	Vector3f pt;
	
	private static final Sphere sphere;
	private RigidBodyControl    ball_phy;
	Material stone_mat;
	
	private AudioNode audio_gun;
	private AudioNode audio_nature;
	private AudioNode audioExplosion;
	
    private Node explosionEffect = new Node("explosionFX");
	private ParticleEmitter flame, flash, spark, roundspark, smoketrail, debris, shockwave;
	
    private static final int COUNT_FACTOR = 1;
    private static final float COUNT_FACTOR_F = 1f;

    private static final boolean POINT_SPRITE = true;
    private static final Type EMITTER_TYPE = POINT_SPRITE ? Type.Point : Type.Triangle;
    
    private float time = 0;
    private int state = 0;
    
    private int health = 100;
    private boolean dead = false;

    private BitmapText hudText;
    private boolean health_init = false;
	
	public static void main(String[] args) {
		Main run = new Main();
		run.start();
	}


	static{
		box4 = new Box(0.5f, 0.5f, 0.5f);
	    sphere = new Sphere(32, 32, 0.5f, true, false);
	    sphere.setTextureMode(TextureMode.Projected);
	}

	@Override
	public void simpleInitApp() {

		bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);
		
		flyCam.setMoveSpeed(30.0f);   
		
		shootable = new Node("Shootables");
		rootNode.attachChild(shootable);
		
		initInputs();
		setUpLight();
		initMark();
		initCrossHairs();
		Cubes(shootable);
		Motions();
		materialInit();
		Audio();
		Hud();
		
		Explosion();
		explosionEffect.setLocalScale(0.75f);
        renderManager.preloadScene(explosionEffect);
        rootNode.attachChild(explosionEffect);
        
		// Load the world
		assetManager.registerLocator("town.zip", ZipLocator.class);
		sceneModel = assetManager.loadModel("main.scene");
		sceneModel.setLocalScale(2f);
		
		CollisionShape sceneShape =	CollisionShapeFactory.createMeshShape((Node) sceneModel);
		landscape = new RigidBodyControl(sceneShape, 0);
		sceneModel.addControl(landscape);
		
	    sceneModel.setShadowMode(ShadowMode.CastAndReceive);
	    rootNode.attachChild(sceneModel);

		// Player
		CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
	    player = new CharacterControl(capsuleShape, 0.05f);
	    player.setJumpSpeed(20);
	    player.setFallSpeed(30);
	    player.setGravity(30);
	    player.setPhysicsLocation(new Vector3f(0, 10, 0));
		
	    bulletAppState.getPhysicsSpace().add(landscape);
	    bulletAppState.getPhysicsSpace().add(player);
	    
		// Make the world selectable.
		shootable.attachChild(sceneModel);
			    
		/* Skybox */
		rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
		
		// Bloom material
		FilterPostProcessor fpp=new FilterPostProcessor(assetManager);
		BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Objects);
		fpp.addFilter(bloom);
		viewPort.addProcessor(fpp);
			
	}
		
	private void makeCannonBall() {
		Geometry ball_geo = new Geometry("cannon ball", sphere);
		Material ball = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		ball.setColor("GlowColor", ColorRGBA.Orange);
		ball_geo.setMaterial(ball);
		ball_geo.setShadowMode(ShadowMode.CastAndReceive);
		rootNode.attachChild(ball_geo);
		
		ball_geo.setLocalTranslation(cam.getLocation().add(cam.getDirection().mult(3)));
		
		ball_phy = new RigidBodyControl(1f);
		ball_geo.addControl(ball_phy);
		
		bulletAppState.getPhysicsSpace().add(ball_phy);
		ball_phy.setLinearVelocity(cam.getDirection().multLocal(100));
	}

	private void Audio(){
		// https://www.sounddogs.com/previews/2226/mp3/507560_SOUNDDOGS__ma.mp3
		audio_gun = new AudioNode(assetManager, "assets/Sounds/fireblast.wav", false);
	    audio_gun.setPositional(false);
	    audio_gun.setLooping(false);
	    audio_gun.setVolume(0.08f);
	    rootNode.attachChild(audio_gun);
	    
	    //http://soundbible.com/1461-Big-Bomb.html
        audioExplosion = new AudioNode(assetManager, "assets/Sounds/explosion.wav", false);
        audioExplosion.setPositional(false);
        audioExplosion.setLooping(false);
        audioExplosion.setVolume(0.09f);
        rootNode.attachChild(audioExplosion);
	    
	    //http://www.dl-sounds.com/index.php?main_page=advanced_search_result&search_in_description=1&keyword=Stone+island&x=0&y=0
	    audio_nature = new AudioNode(assetManager, "assets/Sounds/Stone_island.wav", false);
	    audio_nature.setLooping(true);  // activate continuous playing
	    audio_nature.setPositional(false);   
	    audio_nature.setVolume(0.08f);
	    rootNode.attachChild(audio_nature);
	    audio_nature.play(); // play continuously!
	}
	
	private void setUpLight() {
		
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(1.3f));
		rootNode.addLight(al);

		DirectionalLight dl = new DirectionalLight();
		dl.setColor(ColorRGBA.White);
		dl.setDirection(new Vector3f(-.5f,-.5f,-.5f).normalizeLocal());
        
		rootNode.addLight(dl);
		
		// http://hub.jmonkeyengine.org/wiki/doku.php/jme3:advanced:light_and_shadow
		
		DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, 1024, 3);
		dlsr.setLight(dl);
		dlsr.setLambda(0.55f);
		dlsr.setShadowIntensity(0.6f);
		dlsr.setEdgeFilteringMode(EdgeFilteringMode.Nearest);
		viewPort.addProcessor(dlsr);
		
		DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, 1024, 3);
        dlsf.setLight(dl);
        dlsf.setLambda(0.55f);
        dlsf.setShadowIntensity(0.6f);
        dlsf.setEdgeFilteringMode(EdgeFilteringMode.Nearest);       
        dlsf.setEnabled(false);
        
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(dlsf);

        viewPort.addProcessor(fpp);
	}
	
	private void Explosion(){
			//https://code.google.com/p/jmonkeyengine/source/browse/branches/jme3/src/test/jme3test/effect/TestExplosionEffect.java?r=6075
	        flash = new ParticleEmitter("Flash", EMITTER_TYPE, 24 * COUNT_FACTOR);
	        flash.setSelectRandomImage(true);
	        flash.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, (float) (1f / COUNT_FACTOR_F)));
	        flash.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
	        flash.setStartSize(.1f);
	        flash.setEndSize(3.0f);
	        flash.setShape(new EmitterSphereShape(Vector3f.ZERO, .05f));
	        flash.setParticlesPerSec(0);
	        flash.setGravity(0, 0, 0);
	        flash.setLowLife(.2f);
	        flash.setHighLife(.2f);
	        flash.setInitialVelocity(new Vector3f(0, 5f, 0));
	        flash.setVelocityVariation(1);
	        flash.setImagesX(2);
	        flash.setImagesY(2);
	        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
	        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flash.png"));
	        mat.setBoolean("PointSprite", POINT_SPRITE);
	        flash.setMaterial(mat);
	        explosionEffect.attachChild(flash);

	        roundspark = new ParticleEmitter("RoundSpark", EMITTER_TYPE, 20 * COUNT_FACTOR);
	        roundspark.setStartColor(new ColorRGBA(1f, 0.29f, 0.34f, (float) (1.0 / COUNT_FACTOR_F)));
	        roundspark.setEndColor(new ColorRGBA(0, 0, 0, (float) (0.5f / COUNT_FACTOR_F)));
	        roundspark.setStartSize(1.2f);
	        roundspark.setEndSize(1.8f);
	        roundspark.setShape(new EmitterSphereShape(Vector3f.ZERO, 2f));
	        roundspark.setParticlesPerSec(0);
	        roundspark.setGravity(0, -.5f, 0);
	        roundspark.setLowLife(1.8f);
	        roundspark.setHighLife(2f);
	        roundspark.setInitialVelocity(new Vector3f(0, 3, 0));
	        roundspark.setVelocityVariation(.5f);
	        roundspark.setImagesX(1);
	        roundspark.setImagesY(1);
	        Material roundspark_mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
	        roundspark_mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/roundspark.png"));
	        roundspark_mat.setBoolean("PointSprite", POINT_SPRITE);
	        roundspark.setMaterial(roundspark_mat);
	        explosionEffect.attachChild(roundspark);

	        spark = new ParticleEmitter("Spark", Type.Triangle, 30 * COUNT_FACTOR);
	        spark.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, (float) (1.0f / COUNT_FACTOR_F)));
	        spark.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
	        spark.setStartSize(.5f);
	        spark.setEndSize(.5f);
	        spark.setFacingVelocity(true);
	        spark.setParticlesPerSec(0);
	        spark.setGravity(0, 5, 0);
	        spark.setLowLife(1.1f);
	        spark.setHighLife(1.5f);
	        spark.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 20, 0));
	        spark.getParticleInfluencer().setVelocityVariation(1);
	        spark.setImagesX(1);
	        spark.setImagesY(1);
	        Material spark_mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
	        spark_mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/spark.png"));
	        spark.setMaterial(spark_mat);
	        explosionEffect.attachChild(spark);

	        smoketrail = new ParticleEmitter("SmokeTrail", Type.Triangle, 22 * COUNT_FACTOR);
	        smoketrail.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, (float) (1.0f / COUNT_FACTOR_F)));
	        smoketrail.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
	        smoketrail.setStartSize(.2f);
	        smoketrail.setEndSize(1f);

//	        smoketrail.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
	        smoketrail.setFacingVelocity(true);
	        smoketrail.setParticlesPerSec(0);
	        smoketrail.setGravity(0, 1, 0);
	        smoketrail.setLowLife(.4f);
	        smoketrail.setHighLife(.5f);
	        smoketrail.setInitialVelocity(new Vector3f(0, 12, 0));
	        smoketrail.setVelocityVariation(1);
	        smoketrail.setImagesX(1);
	        smoketrail.setImagesY(3);
	        Material smokteTrail_mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
	        smokteTrail_mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/smoketrail.png"));
	        smoketrail.setMaterial(smokteTrail_mat);
	        explosionEffect.attachChild(smoketrail);

	        debris = new ParticleEmitter("Debris", Type.Triangle, 15 * COUNT_FACTOR);
	        debris.setSelectRandomImage(true);
	        debris.setRandomAngle(true);
	        debris.setRotateSpeed(FastMath.TWO_PI * 4);
	        debris.setStartColor(new ColorRGBA(1f, 0.59f, 0.28f, (float) (1.0f / COUNT_FACTOR_F)));
	        debris.setEndColor(new ColorRGBA(.5f, 0.5f, 0.5f, 0f));
	        debris.setStartSize(.2f);
	        debris.setEndSize(.2f);

//	        debris.setShape(new EmitterSphereShape(Vector3f.ZERO, .05f));
	        debris.setParticlesPerSec(0);
	        debris.setGravity(0, 12f, 0);
	        debris.setLowLife(1.4f);
	        debris.setHighLife(1.5f);
	        debris.setInitialVelocity(new Vector3f(0, 15, 0));
	        debris.setVelocityVariation(.60f);
	        debris.setImagesX(3);
	        debris.setImagesY(3);
	        Material debris_mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
	        debris_mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/Debris.png"));
	        debris.setMaterial(debris_mat);
	        explosionEffect.attachChild(debris);

	        shockwave = new ParticleEmitter("Shockwave", Type.Triangle, 1 * COUNT_FACTOR);
//	        shockwave.setRandomAngle(true);
	        shockwave.setFaceNormal(Vector3f.UNIT_Y);
	        shockwave.setStartColor(new ColorRGBA(.48f, 0.17f, 0.01f, (float) (.8f / COUNT_FACTOR_F)));
	        shockwave.setEndColor(new ColorRGBA(.48f, 0.17f, 0.01f, 0f));
	        shockwave.setStartSize(0f);
	        shockwave.setEndSize(7f);

	        shockwave.setParticlesPerSec(0);
	        shockwave.setGravity(0, 0, 0);
	        shockwave.setLowLife(0.5f);
	        shockwave.setHighLife(0.5f);
	        shockwave.setInitialVelocity(new Vector3f(0, 0, 0));
	        shockwave.setVelocityVariation(0f);
	        shockwave.setImagesX(1);
	        shockwave.setImagesY(1);
	        Material shockwave_mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
	        shockwave_mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/shockwave.png"));
	        shockwave.setMaterial(shockwave_mat);
	        explosionEffect.attachChild(shockwave);

	}
	
	private void Fire(Vector3f pt2){
		// http://hub.jmonkeyengine.org/wiki/doku.php/jme3:advanced:particle_emitters
		ParticleEmitter fire = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
	    Material mat_red = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
	    mat_red.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
	    fire.setMaterial(mat_red);
	    fire.setImagesX(2); 
	    fire.setImagesY(2); // 2x2 texture animation
	    fire.setEndColor(new ColorRGBA(1f, 0f, 0f, 1f));   // red
	    fire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
	    fire.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
	    fire.setStartSize(1.5f);
	    fire.setEndSize(0.1f);
	    fire.setGravity(0, 0, 0);
	    fire.setLowLife(1f);
	    fire.setHighLife(3f);
	    fire.getParticleInfluencer().setVelocityVariation(0.3f);
	    fire.setLocalTranslation(pt2);
	    rootNode.attachChild(fire);
	}
	
	private void materialInit(){
	    stone_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
	    TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
	    key2.setGenerateMips(true);
	    Texture tex2 = assetManager.loadTexture(key2);
	    stone_mat.setTexture("ColorMap", tex2);
	}
	
	private void Motions(){
		/* https://code.google.com/p/jmonkeyengine/source/browse/trunk/engine/src/test/jme3test/animation/TestMotionPath.java */
		path1 = new MotionPath();
		path1.setCycle(true);
		path1.addWayPoint(new Vector3f(-105,2,10));
		path1.addWayPoint(new Vector3f(-105, 4, 10));
//		path1.enableDebugShape(assetManager, rootNode);

		motionControl1 = new MotionEvent(blue, path1);
		motionControl1.setLoopMode(LoopMode.Loop);
		motionControl1.setDirectionType(MotionEvent.Direction.Path);
		motionControl1.setInitialDuration(10f);
		motionControl1.setSpeed(2f);

		path2 = new MotionPath();
		path2.setCycle(true);
		path2.addWayPoint(new Vector3f(-30,2,70));
		path2.addWayPoint(new Vector3f(-30, 4, 70));
//		path2.enableDebugShape(assetManager, rootNode);

		motionControl2 = new MotionEvent(red, path2);
		motionControl2.setLoopMode(LoopMode.Loop);
		motionControl2.setDirectionType(MotionEvent.Direction.Path);
		motionControl2.setInitialDuration(10f);
		motionControl2.setSpeed(2f);

		path3 = new MotionPath();
		path3.setCycle(true);
		path3.addWayPoint(new Vector3f(30,2, 110));
		path3.addWayPoint(new Vector3f(30, 4, 110));
//		path3.enableDebugShape(assetManager, rootNode);

		motionControl3 = new MotionEvent(green, path3);
		motionControl3.setLoopMode(LoopMode.Loop);
		motionControl3.setDirectionType(MotionEvent.Direction.Path);
		motionControl3.setInitialDuration(10f);
		motionControl3.setSpeed(2f);
		
		motionControl1.play();
		motionControl2.play();
		motionControl3.play();
	}
	
	private void Cubes(Node pivot){
		/* Cubes */
		Box box1 = new Box(1, 1, 1);
		blue = new Geometry("Box1", box1);
		blue.setLocalTranslation(new Vector3f(-105,2,10));
		Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		mat.setBoolean("UseMaterialColors", true);
//		mat.setColor("Color", new ColorRGBA(0, 0, 1, 0.25f));
		mat.setColor("GlowColor", ColorRGBA.Blue);
		blue.setMaterial(mat);
		blue.setShadowMode(ShadowMode.CastAndReceive);
		rootNode.attachChild(blue);
		
		///////////////////////////////////////
		
		Box box2 = new Box(1,1,1);      
		red = new Geometry("Box2", box2);
		red.setLocalTranslation(new Vector3f(-30,2,70));
		Material mat2 = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		//mat2.setColor("Color", ColorRGBA.Red);
		mat2.setBoolean("UseMaterialColors", true);
		mat2.setColor("Ambient", ColorRGBA.Red);
		mat2.setColor("Diffuse", ColorRGBA.Red);
		mat2.setColor("GlowColor", ColorRGBA.Red);
//		mat2.setFloat("Shininess", 128f);
		red.setMaterial(mat2);
		red.setShadowMode(ShadowMode.CastAndReceive);
		rootNode.attachChild(red);

		///////////////////////////////////////
		
		Box box3 = new Box(1,1,1);      
		green = new Geometry("Box3", box3);
		green.setLocalTranslation(new Vector3f(30,2, 110));		
		Material mat3 = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
//	    mat3.setColor("Color", ColorRGBA.Green);
	    mat3.setColor("GlowColor", ColorRGBA.Green);
	    green.setMaterial(mat3);
		green.setShadowMode(ShadowMode.CastAndReceive);
		rootNode.attachChild(green);
		
		///////////////////////////////////////
		
		Box box5 = new Box(1,1,1);      
		black = new Geometry("Box5", box5);
		black.setLocalTranslation(new Vector3f(0,1.5f,0));		
		Material mat5 = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
	    mat5.setColor("GlowColor", ColorRGBA.Black);
	    black.setMaterial(mat5);
	    black.setShadowMode(ShadowMode.CastAndReceive);
		rootNode.attachChild(black);

		pivot.attachChild(blue);
		pivot.attachChild(red);
		pivot.attachChild(green);
		pivot.attachChild(black);
	}
	
	private void initInputs() {
	    inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
	    inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
	    inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
	    inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
	    inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addMapping("Fire", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addMapping("Switch", new KeyTrigger(KeyInput.KEY_1));
		inputManager.addMapping("Gun",  new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
		inputManager.addListener(this, "Left", "Right", "Up", "Down", "Jump");
		inputManager.addListener(this, "Fire");
		inputManager.addListener(this, "Switch");
		inputManager.addListener(this, "Gun");
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
				pt = results.getCollision(i).getContactPoint();
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
				//rootNode.attachChild(mark);
				
				if(closest.getGeometry().getName() == "Box3" && (Math.abs(pt.x - player.getPhysicsLocation().x) < 15) && (Math.abs(pt.z - player.getPhysicsLocation().z)) < 15){
					shootable.detachChild(shootable.getChild(closest.getGeometry().getName()));
					haveKey = true;
					motionControl3.stop();
					Hud();
				}else if(closest.getGeometry().getName() == "Box2" && (Math.abs(pt.x - player.getPhysicsLocation().x) < 15) && (Math.abs(pt.z - player.getPhysicsLocation().z)) < 15){
					shootable.detachChild(shootable.getChild(closest.getGeometry().getName()));
					haveRemote = true;
					motionControl2.stop();
					Hud();
				}else if(closest.getGeometry().getName() == "Box1" && (Math.abs(pt.x - player.getPhysicsLocation().x) < 15) && (Math.abs(pt.z - player.getPhysicsLocation().z)) < 15){
					shootable.detachChild(shootable.getChild(closest.getGeometry().getName()));
					haveDuplicator = true;
					motionControl1.stop();
					Hud();
				}else if(closest.getGeometry().getName() == "Box5" && haveRemote){
		            flash.emitAllParticles();
		            spark.emitAllParticles();
		            smoketrail.emitAllParticles();
		            debris.emitAllParticles();
		            shockwave.emitAllParticles();
		            audioExplosion.playInstance();
					shootable.detachChild(shootable.getChild(closest.getGeometry().getName()));

					if((Math.abs(pt.x - player.getPhysicsLocation().x) < 50) && (Math.abs(pt.z - player.getPhysicsLocation().z)) < 50){
						dead = true;
						Hud();
					}
				}
			} 
			/*else {
				// No hits? Then remove the red mark.
				rootNode.detachChild(mark);
			}
			*/
			
			if(haveRemote && changeCam == true){
				Fire(pt);
				audio_gun.playInstance();
			}
		}
		
		if (name.equals("Switch") && keyPressed){
			if(haveKey){
				if(changeCam){
					changeCam = false;
					flyCam.setEnabled(false);

					Box box4 = new Box(1, 1, 1);
					yellow = new Geometry("Box4", box4);
					yellow.setLocalTranslation(player.getPhysicsLocation());
					Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
					mat.setColor("Color", ColorRGBA.Yellow);
					yellow.setMaterial(mat);
					rootNode.attachChild(yellow);

					System.out.println(cam.getDirection());
					PlayerLook = cam.getDirection();
					System.out.println("Where the player is looking: " + PlayerLook);

					guiNode.detachChild(ch);
					cam.setLocation(new Vector3f(145,425,0));
					cam.lookAt(new Vector3f(145,0,0), Vector3f.UNIT_Y);
				}else{
					changeCam = true;
					flyCam.setEnabled(true);
					inputManager.setCursorVisible(false);
					initCrossHairs();
					rootNode.detachChild(yellow);
					cam.setLocation(player.getPhysicsLocation());
					cam.lookAtDirection(PlayerLook, Vector3f.UNIT_Y);
				}
			}
		}
		if(haveDuplicator && changeCam == true){
			if(name.equals("Gun") && keyPressed){
				makeCannonBall();
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
        
		/*
        time += tpf / speed;
        if (time > 1f && state == 0){
            flash.emitAllParticles();
            spark.emitAllParticles();
            smoketrail.emitAllParticles();
            debris.emitAllParticles();
            shockwave.emitAllParticles();
            state++;
        }
        if (time > 5 / speed){
            state = 0;
            time = 0;
            flash.killAllParticles();
            spark.killAllParticles();
            smoketrail.killAllParticles();
            debris.killAllParticles();
            roundspark.killAllParticles();
            shockwave.killAllParticles();
        }
         */
		
		hudText.setText("Score: " + health);
		
		if(changeCam){
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
	}
	
	private void initMark() {
		Sphere sphere = new Sphere(30, 30, 0.2f);
		mark = new Geometry("BOOM!", sphere);
		Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mark_mat.setColor("Color", ColorRGBA.Red);
		mark.setMaterial(mark_mat);
	}
	
	private void initCrossHairs() {
		setDisplayStatView(false);
		guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
		ch = new BitmapText(guiFont, false);
		ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
		ch.setText("+");
		ch.setLocalTranslation(settings.getWidth() / 2 - ch.getLineWidth()/2, settings.getHeight() / 2 + ch.getLineHeight()/2, 0); // center
		guiNode.attachChild(ch);
	}
	
	private void Hud(){
		// http://hub.jmonkeyengine.org/wiki/doku.php/jme3:advanced:hud
		if(health_init == false){
			health_init = true;
		    hudText = new BitmapText(guiFont, false);
			hudText.setSize(guiFont.getCharSet().getRenderedSize());      // font size
			hudText.setColor(ColorRGBA.Red);                             // font color
			hudText.setText("Health: " + health);            			 // the text
			hudText.setLocalTranslation(settings.getWidth() / 10, settings.getHeight()/2 + hudText.getLineHeight()/2, 0); // position
			guiNode.attachChild(hudText);
		}
		
		if(haveRemote){
			BitmapText hud = new BitmapText(guiFont, false);
			hud.setSize(guiFont.getCharSet().getRenderedSize() * 2);
			hud.setText("Fire Blast");
			hud.setLocalTranslation(settings.getWidth() / 10, settings.getHeight() / 5 + hud.getLineHeight() / 5 , 0); //  - hud.getLineWidth() / 10
			guiNode.attachChild(hud);	
		}
		if(haveKey){
			BitmapText hud1 = new BitmapText(guiFont, false);
			hud1.setSize(guiFont.getCharSet().getRenderedSize() * 2);
			hud1.setText("Satellite View");
			hud1.setLocalTranslation(settings.getWidth() / 10, settings.getHeight() / 4 + hud1.getLineHeight() / 4 , 0); // - hud1.getLineWidth() / 2.5f
			guiNode.attachChild(hud1);
		}
		if(haveDuplicator){
			BitmapText hud2 = new BitmapText(guiFont, false);
			hud2.setSize(guiFont.getCharSet().getRenderedSize() * 2);
			hud2.setText("Cannon");
			hud2.setLocalTranslation(settings.getWidth() / 10, settings.getHeight() / 3.25f + hud2.getLineHeight() / 3.25f , 0); //  - hud2.getLineWidth() / 7
			guiNode.attachChild(hud2);
		}
		if(dead){
			BitmapText hud3 = new BitmapText(guiFont, false);
			hud3.setSize(guiFont.getCharSet().getRenderedSize() * 2);
			hud3.setText("You died!");
			hud3.setLocalTranslation(settings.getWidth() / 2 - hud3.getLineWidth(), settings.getHeight() / 2, 0);
			
			guiNode.detachAllChildren();
			guiNode.attachChild(hud3);
			flyCam.setEnabled(false);
			changeCam = false;
			haveRemote = false;
			haveKey = false;
			haveDuplicator = false;
			
		}
	}
}
