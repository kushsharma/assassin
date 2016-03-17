package utils;

import java.util.HashMap;

import objects.Bullet;
import objects.Coin;
import objects.Enemy;
import objects.Friend;
import objects.Ghost;
import objects.Laser;
import objects.Light;
import objects.Player;
import objects.Portal;
import objects.Switch;
import utils.MyInputProcessor.CONTROL;
import Screens.GameScreen;
import box2dLight.RayHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.softnuke.epic.MyGame;

public class LevelGenerate {
	public static LevelGenerate _level = null;
	int WIDTH = MyGame.WIDTH, HEIGHT = MyGame.HEIGHT;
	int bWIDTH = MyGame.bWIDTH, bHEIGHT = MyGame.bHEIGHT;
	public static float PTP = 0.15f;
	
	public static boolean MACHINE_GUN = false; //continues fire
	public static boolean WORLD_FLIPPED = false; //if gravity is flipped
	public static boolean LEVEL_LOADED = false;
	public static int CURRENT_LEVEL = 1;
	public static int MAX_LEVELS = 3;
	
	TiledMap tileMap;
	TmxMapLoader tileLoader;
	OrthogonalTiledMapRenderer tmRenderer;
	OrthographicCamera camera;
	World world;
	SpriteBatch batch;
	Viewport viewport;
	GameScreen gameScreen = GameScreen.getInstance();
	
//	public static short CATEGORY_NONE = 0x0001;
//	public static short CATEGORY_PLAYER = 0x0002;
//	public static short CATEGORY_BADBOY = 0x0004;
//	public static short CATEGORY_WALL = 0x0008;
//	public static short CATEGORY_POWERUP = 0x0010;
//	public static short CATEGORY_BULLET = 0x0020;
	//public static short CATEGORY_UTILS = 0x0040;
	
	public static short CATEGORY_NONE = 1<<0;
	public static short CATEGORY_PLAYER = 1<<1;
	public static short CATEGORY_BADBOY = 1<<2;
	public static short CATEGORY_WALL = 1<<3;
	public static short CATEGORY_POWERUP = 1<<4;
	public static short CATEGORY_BULLET = 1<<5;
	public static short CATEGORY_UTILS = 1<<6;
	
	public static boolean CURRENT_LEVEL_CLEARED = false;
	private float gametime = 0;
	private float last_bullet = 0; //time since last bullet was fired
	public float MAP_RIGHT_BOUND = 50;
	public float MAP_UPPER_BOUND = 20;

	public HashMap<CONTROL, Boolean> pKeys = null;
	TaskQueue taskQueue = new TaskQueue();
	AssetLord Assets = GameScreen.getInstance().getAssetLord();

	Array<Body> platformPool = new Array<Body>();
	public Array<Enemy> enemyPool = new Array<Enemy>();	
	Array<Bullet> bulletPool = new Array<Bullet>();
	Array<Bullet> activeBulletPool = new Array<Bullet>();
	Array<Light> lightPool = new Array<Light>();
	Array<Portal> portalPool = new Array<Portal>();
	Array<Body> spikesPool = new Array<Body>();
	Array<Switch> switchPool = new Array<Switch>();
	Array<Laser> laserPool = new Array<Laser>();

	//Array<Movers> moverPool = new Array<Movers>();
	public Array<Coin> coinsPool = new Array<Coin>();
	
	//Switch levelSwitch = null;
	BloodManager bloodManager;	
	public RayHandler rayHandler;
	
	Music gameMusic = null;
	Sound coinSound, fireSound, playerHurtSound, enemyHurtSound, levelUpSound, portalSound, epicLevelSound, finishSound;
	
	public LevelGenerate(OrthographicCamera cam, World w, SpriteBatch b){
		_level = this;
		camera = cam;
		world = w;
		batch = b;
		
		PTP = 1/70f;

		tileLoader = new TmxMapLoader();
		tmRenderer = new OrthogonalTiledMapRenderer(tileMap, PTP, batch);
		
		//prepare blood
		bloodManager = new BloodManager(world);
		
		//gameMusic = Gdx.audio.newMusic(Gdx.files.internal("rinse.mp3"));
		
		
		prepareBullets();
		
		levelTiled();

		coinSound = fireSound = playerHurtSound = enemyHurtSound = levelUpSound = portalSound = null;

	}
	
	public void levelTiled(){
			MyGame.sop("Generating Tiled Map of Level..."+CURRENT_LEVEL);
				
		try {
			switch(CURRENT_LEVEL){			
			case 1:
				tileMap = tileLoader.load("levels/level-1.tmx");
				break;
			case 2:
				tileMap = tileLoader.load("levels/level-2.tmx");
				break;
			case 3:
				tileMap = tileLoader.load("levels/level-3.tmx");
				break;
			case -1:
				//negative values for multiplayer levels
				tileMap = tileLoader.load("levels/level-net.tmx");
				break;
			default:
				tileMap = tileLoader.load("levels/level-zero.tmx");
				break;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			MyGame.sop("Cannot find file: map files .tmx");
			Gdx.app.exit();
		}		
		
		MapProperties prop = tileMap.getProperties();		
		MAP_RIGHT_BOUND = (float)prop.get("width", Integer.class);
		MAP_UPPER_BOUND = (float)prop.get("height", Integer.class);
		int tile_width = prop.get("tilewidth", Integer.class);
		//calculating actual map right bound
		MAP_RIGHT_BOUND = MAP_RIGHT_BOUND * tile_width * PTP;
		
		//MyGame.sop("Map Width:"+MAP_RIGHT_BOUND);
		
		rayHandler = new RayHandler(world, WIDTH/5, HEIGHT/5);
		rayHandler.setAmbientLight(0.95f);
		rayHandler.setBlur(false);

		buildShapes(tileMap, world);		
		tmRenderer.setMap(tileMap);
		
		GameScreen.getInstance().cinema.levelUpdate(CURRENT_LEVEL);
		
		LEVEL_LOADED = true;		
		GameScreen.CURRENT_STATE = GameState.RUNNING;
		
		//create Sounds
		loadSounds();
	}
			
	private void loadSounds() {
			
		coinSound = Assets.manager.get(AssetLord.coin_sound, Sound.class);
		fireSound = Assets.manager.get(AssetLord.fire_sound, Sound.class);
		finishSound = Assets.manager.get(AssetLord.finish_sound, Sound.class);
		epicLevelSound = Assets.manager.get(AssetLord.epicLevelup_sound, Sound.class);
		
		playerHurtSound = Assets.manager.get(AssetLord.player_hurt_sound, Sound.class);
		enemyHurtSound = Assets.manager.get(AssetLord.enemy_hurt_sound, Sound.class);
		levelUpSound = Assets.manager.get(AssetLord.levelup_sound, Sound.class);
		//portalSound = Assets.manager.get(AssetLord.portal_sound, Sound.class);
		
		
		gameMusic = Assets.manager.get(AssetLord.game_music, Music.class);
		gameMusic.setLooping(true);
		gameMusic.setVolume(0.5f);
		
		if(GameScreen.BACKGROUND_MUSIC){			
				gameMusic.stop();
				gameMusic.play();
		
		}
		
	}
	
	private void prepareBullets(){
		
		//8 bullets, 4 for player, 4 for ghost/friend
		for(int i=0;i<8;i++)
		{
			Bullet b = new Bullet(world, false);
			bulletPool.add(b);
		}
		
	}
	
	public void buildShapes(TiledMap map, World world) {
		
		MapObjects platforms = map.getLayers().get("PlatformOb").getObjects();
        for(MapObject object : platforms) {

            if (object instanceof TextureMapObject) {
                continue;
            }

            Shape shape;

            if (object instanceof RectangleMapObject) {
                shape = getRectangle((RectangleMapObject)object);
                
            }
            else if (object instanceof PolygonMapObject) {
                shape = getPolygon((PolygonMapObject)object);
            }
            else if (object instanceof CircleMapObject) {
                shape = getCircle((CircleMapObject)object);
            }
            else {
                continue;
            }

            
            BodyDef bd = new BodyDef();
            bd.type = BodyType.StaticBody;
            Body body = world.createBody(bd);
            
            FixtureDef fixD = new FixtureDef();
            fixD.density = 0;
            fixD.shape = shape;
            fixD.filter.categoryBits = (CATEGORY_WALL);
            fixD.filter.maskBits = (short) (CATEGORY_PLAYER | CATEGORY_BADBOY | CATEGORY_BULLET | CATEGORY_UTILS);
            
            body.createFixture(fixD);

            body.setUserData("platform");
            platformPool.add(body);

            shape.dispose();
        }
        
        if(map.getLayers().get("EnemyOb") != null){
	        MapObjects enemy = map.getLayers().get("EnemyOb").getObjects();
	        for(MapObject object : enemy) {
	
	            //generate enemy ai
	            Rectangle r = ((RectangleMapObject)object).getRectangle();
	            Vector2 pos = new Vector2((r.x + r.width * 0.5f) * PTP,
	                    (r.y + r.height * 0.5f ) * PTP);
	            Enemy en = new Enemy(world,  pos);
	            
	            enemyPool.add(en);
	        }
       	}
        
        //get level switch if any and clear old
        //levelSwitch = null;
        switchPool.clear();
        if(map.getLayers().get("SwitchOb") != null){
        	MapObjects switchs = map.getLayers().get("SwitchOb").getObjects();
        	for(MapObject object : switchs) {
        		
        		Rectangle r = ((RectangleMapObject)object).getRectangle();
        		Vector2 pos = new Vector2((r.x + r.width * 0.5f) * PTP,
        				(r.y + r.height * 0.5f ) * PTP);
        		
        		//calculating light size
        		float len1x = (r.x - r.width/2) * PTP;
        		float len1y = (r.y - r.height/2) * PTP;            
        		float len2x = (r.x + r.width/2) * PTP;
        		float len2y = (r.y + r.height/2) * PTP;            
        		double size = Math.pow(len1x - len2x, 2) +  Math.pow(len1y - len2y, 2);
        		size = Math.sqrt(size);
        		
        		Light lightE = new Light(pos, (float)size, Light.GREEN_COLOR);
        		pushLight(lightE);
        		
        		Light lightD = new Light(pos, (float)size, Light.RED_COLOR);
        		pushLight(lightD);
        		
        		Switch s = new Switch(world, pos, lightE, lightD);
        		
        		int auth = 0;
	            try{
	            	auth = Integer.parseInt(object.getProperties().get("id").toString());
	            }catch(Exception e){}
        		
	            s.auth_id = auth;
        		switchPool.add(s);     
        		
        	}        	
        }//end if
        
        if(map.getLayers().get("SpikesOb") != null){
	        MapObjects spikes = map.getLayers().get("SpikesOb").getObjects();
	        for(MapObject object : spikes) {
	
	            if (object instanceof TextureMapObject) {
	                continue;
	            }
	
	            Shape shape;
	
	            if (object instanceof RectangleMapObject) {
	                shape = getRectangle((RectangleMapObject)object);
	                
	            }
	            else {
	                continue;
	            }
	
	            
	            BodyDef bd = new BodyDef();
	            bd.type = BodyType.StaticBody;
	            Body body = world.createBody(bd);
	            
	            FixtureDef fixD = new FixtureDef();
	            fixD.density = 0;
	            fixD.shape = shape;
	            fixD.filter.categoryBits = (CATEGORY_BADBOY);
	            fixD.filter.maskBits = (short) (CATEGORY_PLAYER);
	            
	            body.createFixture(fixD);
	            //body.createFixture(shape, 0);
	
	            body.setUserData("spikes");
	            spikesPool.add(body);
	
	            shape.dispose();
	        }
       	}
        
      //create portals
       MapObjects portals = map.getLayers().get("PortalOb").getObjects();
       Array<RectangleMapObject> portalSensors = portals.getByType(RectangleMapObject.class);
       Array<PolygonMapObject> portalBodies = portals.getByType(PolygonMapObject.class);
        
       for(RectangleMapObject rmo:portalSensors){
        	//entry portal
        	if(rmo.getProperties().get("place").toString().equals("entry")){
        		for(PolygonMapObject mpo: portalBodies){
        			
                	if(mpo.getProperties().get("place").toString().equals("entry")){
                		//create new portal
                		
                		Rectangle r = ((RectangleMapObject)rmo).getRectangle();
                        Vector2 pos = new Vector2((r.x + r.width * 0.5f) * PTP,
                                 (r.y + r.height * 0.5f ) * PTP);
                         
                   
                		Portal p = new Portal(world, Portal.ENTRY, pos, r.width * PTP, r.height * PTP, (PolygonMapObject) mpo);
                		if(CURRENT_LEVEL == 0)
                			p.ENABLED = false;
                		
                		portalPool.add(p);
                		
                		//UPDATE PLAYER LOCATION
                		Player.getInstance().setStartingPoint(pos.x , pos.y + 1);
                		if(GameScreen.MULTIPLAYER){
                			//UPDATE PLAYER LOCATION
                			if(Friend.getInstance() != null)
                				Friend.getInstance().setStartingPoint(pos.x , pos.y + 1);                			
                		}
                		else{
                			if(Ghost.getInstance() != null)
                				Ghost.getInstance().setStartingPoint(pos.x , pos.y + 1);
                		}
                	}
                }
        	}
        	
        	//exit portal
        	if(rmo.getProperties().get("place").toString().equals("exit")){
        		for(PolygonMapObject mpo: portalBodies){
        			
                	if(mpo.getProperties().get("place").toString().equals("exit")){
                		//create new portal
                		
                		Rectangle r = ((RectangleMapObject)rmo).getRectangle();
                        Vector2 pos = new Vector2((r.x + r.width * 0.5f) * PTP,
                                 (r.y + r.height * 0.5f ) * PTP);
                         
                        
                		Portal p = new Portal(world, Portal.EXIT, pos, r.width * PTP, r.height * PTP, (PolygonMapObject) mpo);
                		portalPool.add(p);

                	}
                }
        	}
        }
       
       if(map.getLayers().get("CoinOb") != null){
	        MapObjects coins = map.getLayers().get("CoinOb").getObjects();
	        for(MapObject object : coins) {
	
	            if (object instanceof TextureMapObject) {
	                continue;
	            }
	
	
	            if (object instanceof RectangleMapObject) {
	                Rectangle rectangle = ((RectangleMapObject)object).getRectangle();
	                
	            }
	            else {
	                continue;
	            }
	            
	            Rectangle r = ((RectangleMapObject)object).getRectangle();
	            Vector2 pos = new Vector2((r.x + r.width * 0.5f) * PTP,
	                    (r.y + r.height * 0.5f ) * PTP);
	            
	            //calculating light size
	            float len1x = (r.x - r.width/2) * PTP;
	            float len1y = (r.y - r.height/2) * PTP;            
	            float len2x = (r.x + r.width/2) * PTP;
	            float len2y = (r.y + r.height/2) * PTP;            
	            double size = Math.pow(len1x - len2x, 2) +  Math.pow(len1y - len2y, 2);
	            size = Math.sqrt(size);
	            
	            
	            int level = Coin.NORMAL;
	            try{
	            	level = Integer.parseInt(object.getProperties().get("type").toString());
	            }catch(Exception e){level = Coin.NORMAL;}
	            
	            //only create light if its not hidden
	            Light light = null;
	            if(level != Coin.HIDDEN){
	            	light = new Light(pos, (float)size, Light.WHITE_COLOR);
	            	pushLight(light);	            	
	            }

	            Coin c = new Coin(world, pos, level, light);
	            coinsPool.add(c);
	        }
      	}

       if(map.getLayers().get("LaserOb") != null){
	        MapObjects coins = map.getLayers().get("LaserOb").getObjects();
	        for(MapObject object : coins) {
		            	            
	        	Rectangle r = ((RectangleMapObject)object).getRectangle();
	            	                       
	            int color = 1;
	            try{
	            	color = Integer.parseInt(object.getProperties().get("color").toString());
	            }catch(Exception e){
	            	//e.printStackTrace();
	            	//MyGame.sop("No color baby");
	            	color = 1;
	            }
	            
	            int laserid = 0;
	            try{
	            	laserid = Integer.parseInt(object.getProperties().get("id").toString());
	            }catch(Exception e){
	            	laserid = 0;
	            }
	            
	            
	            Laser l = new Laser(world, r, color);
	            l.enableOscillate();
	            l.laser_id = laserid;
	            
	            laserPool.add(l);
	        }
     	}
	    
	}
	
	public void loadNextLevel() {
		if(CURRENT_LEVEL < MAX_LEVELS)
		{
			LEVEL_LOADED = false;
			CURRENT_LEVEL++;
			
			clearOldLevel();
			levelTiled();
			GameScreen.getInstance().reset(true);
			
			CURRENT_LEVEL_CLEARED = false;
			
			//start music
			if(GameScreen.BACKGROUND_MUSIC){
				if(CURRENT_LEVEL > 0)
				{
					if(GameScreen.BACKGROUND_MUSIC && gameMusic != null)			
						gameMusic.play();
				}
			}
		}		
	}
	
	private void clearOldLevel() {
		//soft dispose only those who belong to this level
				
		dispose(true);
	}

	public static LevelGenerate getInstance(){
		return _level;
	}

	Array<TiledMapImageLayer> tileImageLayers = new Array<TiledMapImageLayer>();
	public void render(SpriteBatch batch, ShapeRenderer canvas){
		if(!LEVEL_LOADED) return;
		
		//resetting color changed by idiots
		batch.setColor(1f, 1f, 1f, 1f);		
		tmRenderer.setView(camera.combined, camera.position.x - bWIDTH/2 -bWIDTH/6 , camera.position.y - bHEIGHT/2 - bHEIGHT/6, bWIDTH*1.5f, bHEIGHT*1.5f);
		//tmRenderer.setView(camera);
		//batch.setProjectionMatrix(GameScreen.getInstance().cameraui.combined);
		batch.begin();
		tileMap.getLayers().getByType(TiledMapImageLayer.class, tileImageLayers);
		for(TiledMapImageLayer l:tileImageLayers)
			tmRenderer.renderImageLayer(l);
		
		//if(BOSS_LEVEL || Player.getInstance().GLOWING)
		//	batch.setColor(Color.BLACK);

		if(tileMap.getLayers().get("Platform") != null)
			tmRenderer.renderTileLayer((TiledMapTileLayer) tileMap.getLayers().get("Platform"));
		if(tileMap.getLayers().get("Spikes") != null)
			tmRenderer.renderTileLayer((TiledMapTileLayer) tileMap.getLayers().get("Spikes"));
		if(tileMap.getLayers().get("Portal") != null)
			tmRenderer.renderTileLayer((TiledMapTileLayer) tileMap.getLayers().get("Portal"));
		
		for(Portal p:portalPool)
			p.render(batch);
		for(Enemy e:enemyPool)
			e.render(batch);
		for(Bullet b:activeBulletPool)
			b.render(batch);
		for(Coin c:coinsPool)
			c.render(batch);
		for(Switch s:switchPool)
			s.render(batch);
		for(Laser l:laserPool)
			l.render(batch);
		
		bloodManager.render(batch);
		
		batch.end();
		
		rayHandler.setCombinedMatrix(camera.combined, camera.position.x, camera.position.y, camera.viewportWidth, camera.viewportHeight);
		rayHandler.updateAndRender();
	}
	
	public void renderOverlayed(SpriteBatch batch) {
		
		if(tileMap.getLayers().get("Overlay") != null)
			tmRenderer.renderTileLayer((TiledMapTileLayer) tileMap.getLayers().get("Overlay"));		
		
	}
	
	public void renderParticles(SpriteBatch batch) {
		if(!GameScreen.BACKGROUND_PARTICLES)
			return;
		
		//render particles
		for(Enemy e:enemyPool)
			e.renderParticles(batch);
		for(Portal p:portalPool)
			p.renderParticles(batch);
		for(Coin c:coinsPool)
			c.renderParticles(batch);
		for(Switch s:switchPool)
			s.renderParticles(batch);
	}
	
	public void update(float delta) {
		if(!LEVEL_LOADED) return;

		gametime  += delta;
		last_bullet += delta;
		
		bloodManager.update(delta);
		
		for(Enemy e:enemyPool)
			e.update(delta, camera.position.x);
		
		for(Bullet b:activeBulletPool){
			b.update(delta, camera.position.x);
		}
		
		for(Portal p: portalPool){
			p.update(delta, camera.position.x);
		}
		
//		for(Movers m:moverPool){
//			m.update(delta, camera.position.x);
//		}
		
		for(Light l: lightPool){
			l.update(delta, camera.position.x);
		}
		
		for(Laser l: laserPool){
			l.update(delta, camera.position.x);
		}
		
		//if(levelSwitch != null)
		//	levelSwitch.update(delta, camera.position.x);
		
		for(Switch s:switchPool){
			s.update(delta, camera.position.x);
		}
		
		for(Coin c:coinsPool){
			c.update(delta, camera.position.x);
		}
		
		//check for dead bullets and recycle
		int size = activeBulletPool.size;
		while(--size >= 0){
			Bullet b = activeBulletPool.get(size);
			if(b.visible == false)
			{
				activeBulletPool.removeIndex(size);
				bulletPool.add(b);
			}
		}
		
		//execute pending task if any
		taskQueue.execute();
		
		//update game according to moves
		if(pKeys != null)
			updateMove(pKeys);
	}

	/**used for cleaning up dead bodies**/
	public void cleanUpLevel(){
		for(Enemy e:enemyPool){
			if(e.DEAD)
				e.setOffScreen();
		}
		
		int size = activeBulletPool.size;
		while(--size >= 0){
			Bullet b = activeBulletPool.get(size);
			if(b.visible == false)
			{	
				activeBulletPool.removeIndex(size);
				b.setOffScreen();
				bulletPool.add(b);
			}
		}
		
//		if(BOSS_LEVEL && boss != null)
//		{
//			if(boss.DEAD)
//				boss.setOffScreen();
//		}
		
		//MyGame.sop("Bullets:"+activeBulletPool.size);
	}

	public void reset() {
		for(Enemy e:enemyPool)
			e.reset();
		
		//reset bullets
		int size = activeBulletPool.size;
		while(--size >= 0)
		{	Bullet b = activeBulletPool.get(size);
			b.setOffScreen();
			activeBulletPool.removeIndex(size);
			bulletPool.add(b);
		}
		activeBulletPool.clear();
	
		for(Coin c:coinsPool){
			c.reset();
		}

		bloodManager.reset();
		
//		for(Light l:lightPool){
//			l.enable();
//		}
		
//		if(levelSwitch != null)
//		{
//			levelSwitch.reset();
//			
//			for(Portal p:portalPool){
//				if(p.PORTAL_TYPE == Portal.EXIT)
//					p.ENABLED = false;
//			}
//		}
		
		if(switchPool.size > 0)
		{
			for(Switch s: switchPool)
				s.reset();
			
			for(Portal p:portalPool){
				if(p.PORTAL_TYPE == Portal.EXIT)
					p.ENABLED = false;
			}
		}
		
		for(Laser l:laserPool){
			l.reset();
		}
		

		
//		if(BOSS_LEVEL && boss!= null){
//			boss.reset();
//		}
			
		Bullet.BULLET_POWER = false;
		MACHINE_GUN = false;
		
	}
	
	/** Add manually created lights **/
	public void pushLight(Light l){
		lightPool.add(l);
	}
	
	public void pauseMusic() {
		if(GameScreen.BACKGROUND_MUSIC){

		if(gameMusic != null)
			gameMusic.pause();
//		if(menuMusic != null)
//			menuMusic.pause();
		
		}
	}
	
	public void pauseEverything() {
		pauseMusic();
		
	}

	public void resumeEverything() {
		if(GameScreen.BACKGROUND_MUSIC){
			if(gameMusic != null)
				gameMusic.play();
//			if(menuMusic != null)
//			{
//				if(CURRENT_LEVEL < 1)
//					menuMusic.play();
//			}			
		}
	}

	public void dimMusic() {
		
	}
	
	public void test() {
		// TODO Auto-generated method stub
		levelClear();
	}
	
	/**Ask blood manager to start the party*/
	public void splatterBlood(float x, float y, int count){
		bloodManager.queueSplatter(x, y, count);
	}
	
	public void splatterBlood(float x, float y){
		bloodManager.queueSplatter(x, y, BloodManager.SINGLE_BLOOD_COUNT);
	}
	
	public void enemyWallBounce(Fixture fixture) {
		//find enemy and change its direction
		for(Enemy e:enemyPool){
			if(e.getLegFixture().equals(fixture)){
				//set enemy running
				e.RUNNING = true;				
				break;
			}
			else if(e.getSensorFixture().equals(fixture)){
				//revert enemy direction
				e.LEFT_DIRECTION = !e.LEFT_DIRECTION;

				break;
			}
			
		}
		
	}
	
	/** increase score and collect coin**/
	public void coinPlayerCollide(Fixture coin) {
		
		for(Coin c:coinsPool)
		{
			if(c.getFixture().equals(coin)){
				int val = c.consume();
				//MyGame.sop("coin val"+val);
				GameScreen.getInstance().increaseScore(val);
				break;
			}
		}
	}	

	/**
	 * Fire bullets from hand
	 * @param whoisfiring 0 = player, 1 = ghost/friend
	 * **/
	public void fireBullet(int whoisfiring) {
		//get a bullet from pool and add to active bullets
		//if(!Player.getInstance().CAN_FIRE)
		//	return;
		
		//Player.getInstance().applyFireImpulse();
		
		//if(Player.getInstance().getEvolution() == PowerUp.LEVEL_ZERO)
		//	return;
		
		if(Bullet.BULLET_POWER == true)
			gameScreen.shakeThatAss(true);
		
		if(bulletPool.size > 0)
		{//get from dead pool
			Bullet b = bulletPool.get(0);
			bulletPool.removeIndex(0);
			
			b.reset(whoisfiring);
			b.LEFT_DIRECTION = Player.getInstance().LEFT_DIRECTION;
			
			activeBulletPool.add(b);			
		}
		else
		{//get from active pool
			Bullet b =activeBulletPool.get(0);
			activeBulletPool.removeIndex(0);
			b.reset(whoisfiring);
			b.LEFT_DIRECTION = Player.getInstance().LEFT_DIRECTION;
			
			activeBulletPool.add(b);	
		}
		
		
		playFireSound();
	}
	
	/**number of enemies killed**/
	public int getEnemyKilled(){
		int t = 0;
		for(Enemy e:enemyPool){
			if(e.DEAD == true)
				t++;
		}		
		return t;
	}
	
	public void enemyPlayerCollide(Fixture fixtureE, Fixture fixtureP) {
		//MyGame.sop("CHECK DEAD");

		for(Enemy e:enemyPool){
			/*if(false && e.getHeadFixture().equals(fixtureE)){
				
				//enemy died
				e.setDeath();
				
				
				//e.hitBullet(Player.getInstance().JUMP_DAMAGE);
				Player.getInstance().makeMiniJump();
				//MyGame.sop("ENEMY DEAD");
				
				break;
			} else */
				if(e.getSensorFixture().equals(fixtureE) && Player.getInstance().getSensorFixture().equals(fixtureP)){
				
				LevelGenerate.getInstance().playEnemyHitSound();

				//player died
				Player.getInstance().setDeath(Player.DEATH_BY.ENEMY);
				
				if(GameScreen.BACKGROUND_MUSIC)
					Gdx.input.vibrate(50);
				
				break;
			}
		}
		
		
	}
	
	/** Player collides with laser, see if laser can hurt */
	public void laserPlayerCollide(Fixture fixtureL, Fixture fixtureP) {
		MyGame.sop("laser Coll");
		for(Laser l:laserPool){
			if(l.getFixture().equals(fixtureL))
			{	
				if(l.CAN_HURT){
					//player died
					Player.getInstance().setDeath(Player.DEATH_BY.LASERS);					
				}
								
				break;
			}
		}
		
		
	}
	
	/** When ghost collides with enemy */
	public void enemyGhostCollide(Fixture fixtureE, Fixture fixtureP) {
		MyGame.sop("GHOST DEAD");

		if(true || Ghost.getInstance().getSensorFixture().equals(fixtureP)){
				
			//player died
			//Ghost.getInstance().setDeath();
			
			//if(GameScreen.BACKGROUND_MUSIC)
			//	Gdx.input.vibrate(50);
				
			
		}
		
		
	}
	
	/**find enemy and change set its not flying **/
	public void enemyFlying(Fixture fixture) {
		for(Enemy e:enemyPool){
			if(e.getLegFixture().equals(fixture)){

				//enemy not running
				e.RUNNING = false;

				break;
			}
		}
	}
	
	/** kill enemy and bullet **/
	/*
	public void enemyBulletCollide(Fixture enemy, Fixture bullet, boolean LEFT_DIR) {
		int damage = 0;
		
		for(Bullet b: activeBulletPool){
			if(b.getBodyFixture().equals(bullet)){
				//bullet got disappeared
				b.makeDead();
				
				damage = b.getDamage();
				break;
			}
		}
		
		for(Enemy e: enemyPool){
			if(e.DEAD == false && e.getBodyFixture().equals(enemy) == true)
			{//enemy got hit by bullet
				e.hitBullet(damage, LEFT_DIR);				
				break;
			}
		}
	}
	*/
	
	/** kill enemy and bullet **/
	public void enemyBulletCollide(Contact contact) {
		int damage = 0;
		int numContactPoints = contact.getWorldManifold().getNumberOfContactPoints();
		Vector2[] points = contact.getWorldManifold().getPoints();		
		
		Fixture enemy, bullet;
		if(contact.getFixtureA().getBody().getUserData().equals("enemy") == true)
		{
			enemy = contact.getFixtureA();
			bullet = contact.getFixtureB();
		}
		else{
			enemy = contact.getFixtureB();
			bullet = contact.getFixtureA();
		}
			
		for(Bullet b: activeBulletPool){
			if(b.getBodyFixture().equals(bullet)){
				//bullet got disappeared
				b.makeDead();
				
				damage = b.getDamage();
				break;
			}
		}
		
		for(Enemy e: enemyPool){
			if(e.DEAD == false && e.getBodyFixture().equals(enemy) == true)
			{//enemy got hit by bullet
				
				boolean LEFT_DIR = true;
				
				//check which side got hit with bullets
				for(int i=0;i<numContactPoints;i++){
					if(points[i].x < e.getBody().getPosition().x){
						LEFT_DIR = false;
					}
				}		
				
				e.hitBullet(damage, LEFT_DIR);				
				break;
			}
		}
		
		playEnemyHitSound();
	}
	
	/** kill enemy with weapon **/
	public void enemyWeaponCollide(Fixture enemy, Fixture weapon, boolean IN_RANGE) {
		
		playEnemyHitSound();
		
		for(Enemy e: enemyPool){
			if(e.DEAD == false && e.getBodyFixture().equals(enemy) == true)
			{//enemy got hit by bullet
				
				if(Player.getInstance().getWeaponFixture().equals(weapon))
					Player.getInstance().enemyUpdate(e, IN_RANGE);
				if(!gameScreen.MULTIPLAYER && Ghost.getInstance().getWeaponFixture().equals(weapon))
					Ghost.getInstance().enemyUpdate(e, IN_RANGE);
				
				break;
			}
		}
	}
	
	public void enemyGotHit(Enemy e, int damage, boolean LEFT_DIR){
		e.hitBullet(damage, LEFT_DIR);
		
		if(GameScreen.MULTIPLAYER && GameScreen.getInstance().networkManager.NET_STATE == NetworkManager.NET_STATES.CONNECTED)
			GameScreen.getInstance().networkManager.sendEnemyKillUpdate();
	}
	
	/**When in multiplayer, update enemy states*/
	public void updateEnemyPos(NetworkGameInit gi) {
		if(!GameScreen.MULTIPLAYER) return;
		
		int i=0;
		for(Enemy e: enemyPool){
			//MyGame.sop(gi.enemyx[i]+","+gi.enemyy[i]);
			
			if(!e.DEAD && gi.enemyDead[i])
				e.setDeath();
			else
				e.DEAD = gi.enemyDead[i];
			
			//e.interpolateTo(gi.enemyx[i], gi.enemyy[i]);
			
			//only update position if i am client & alive - wrong
			//if(!e.DEAD && GameScreen.getInstance().networkManager.STATE == NetworkManager.STATES.CLIENT){
			
			//update anywhere
			if(!e.DEAD){
				e.updateStats(gi.enemyx[i], gi.enemyy[i], gi.enemyvx[i], gi.enemyvy[i]);
				e.LEFT_DIRECTION = gi.enemyDirection[i];				
			}
			
			i++;
		}
	}

	
	/** remove bullet **/
	public void bulletPlatformCollide(Fixture bullet){
		for(Bullet b: activeBulletPool){
			if(b.getBodyFixture().equals(bullet)){
				//bullet got disappeared
				b.makeDead();
				
				break;
			}
		}
	}
	
	/** is level cleared ??? **/
	public void levelClearPortal(Fixture portal) {
		for(Portal p:portalPool){
			if(p.PORTAL_TYPE == Portal.EXIT && portal.equals(p.getSensorFixture())){
				//exit portal reached
				if(p.ENABLED)
				{
					//start animation of going out
					Player.getInstance().TELEPORTING_OUT = true;

					levelClear();
				}
				
				
				break;
			}
		}
		
	}
		
	/** Called when player reaches exit portal and its enabled */
	private void levelClear() {
		MyGame.sop("level cleared..");
		
		
		gameScreen.showLevelClear();
		
		CURRENT_LEVEL_CLEARED = true;

	}
	
	/** pass false to disable exit portal **/
	public void toggleExitPortalSwitch(boolean enable){
		for(Portal p :portalPool){
			if(p.PORTAL_TYPE == Portal.EXIT)
			{
				p.ENABLED = enable;				
			}
		}
	}
	
	/** When ghost tries to enable switch */
	public void switchGhostCollide(Fixture switchFix, boolean inRange){
		switchCollide(switchFix, inRange, false);
	}
	
	/** level portal switch **/
	public void switchPlayerCollide(Fixture lSwitch, boolean inRange) {
		switchCollide(lSwitch, inRange, true);		
	}
	
	public void switchCollide(Fixture lSwitch, boolean inRange, boolean isPlayer){
		//switch states
		boolean state = true;
		
		if(switchPool.size > 0){
			for(Switch s:switchPool){
				
				//right now just set the state of switch to be ready for enabled on sword swing
				if(lSwitch.equals(s.getFixture())){					
					s.toggleReady(inRange, isPlayer);	
					
				}
				
				state = state & s.STATE_ENABLED;
				
				for(Laser l: laserPool)
				{//switch toggled for laser
					if(s.auth_id == l.laser_id){
						l.CAN_HURT = !s.STATE_ENABLED;
					}
				}
			}
			
			//if all enabled
			if(state)
			{//enable portal
				toggleExitPortalSwitch(true);
			}
			else{
			//disable portal
				toggleExitPortalSwitch(false);
			}
		}
		
	}

	/** Player will swing sword to kill enemies 
	 * @param byPlayer see if player or ghost used this method call
	 * */
	public void swingSword(boolean byPlayer) {		
		Player.getInstance().swingWeapon();	
		
		if(!Player.getInstance().DASHING && !Player.getInstance().isDead())
			GameScreen.getInstance().shakeThatAss(true);
		
		checkSwitchToggle(byPlayer);
	}
	
	/**If any switch is ready, toggle it
	 * @param byPlayer see if player or ghost used this method call
	 * */
	public void checkSwitchToggle(boolean byPlayer){
		//check for switch toggling
		for(Switch s:switchPool){
			s.toggle(byPlayer);
		}
	}
	
	/** keep track of user inputs **/
	public void updateMove(HashMap<MyInputProcessor.CONTROL, Boolean> keys) {
		pKeys = keys;
		
		//automatic bullet fire
		if(pKeys.get(MyInputProcessor.CONTROL.FIRE) == true)
		{
			if(last_bullet > 0.1f)
			{
				swingSword(true); //true for player
				//fireBullet();
				last_bullet = 0;
			}
		}

	}
	
	public void dispose(boolean levelClear){
		if(!levelClear){
			//clear only when game ends
			
			tmRenderer.dispose();
			tileMap.dispose();			

			//clear bullets
			for(Bullet b:bulletPool)
				b.dispose();
			bulletPool.clear();
			for(Bullet b:activeBulletPool)
				b.dispose();
			activeBulletPool.clear();
			
			if(gameMusic != null && GameScreen.BACKGROUND_MUSIC)
				gameMusic.dispose();
		}		
		
		for(Body b: platformPool){
			world.destroyBody(b);
		}
		platformPool.clear();
		
		//clear enemies
		for(Enemy e:enemyPool)
			e.dispose();
		enemyPool.clear();
		
		//clear lights
		for(Light l:lightPool)
			l.dispose();
		lightPool.clear();
		
		for(Body b:spikesPool){
			world.destroyBody(b);
		}
		spikesPool.clear();
		
		for(Coin c:coinsPool){
			c.dispose();
		}
		coinsPool.clear();
		
		for(Portal p: portalPool)
			p.dispose();
		portalPool.clear();
		
		for(Switch s: switchPool)
			s.dispose();
		switchPool.clear();
		
		for(Laser l: laserPool)
			l.dispose();
		laserPool.clear();
		
		bloodManager.dispose();
		rayHandler.dispose();
		
		//clear image layers used in rendering
		//tileImageLayers.clear();
		
		//clear tasks
		taskQueue.clear();
		
		LEVEL_LOADED = false;
	}
	
	
	private static PolygonShape getRectangle(RectangleMapObject rectangleObject) {
        Rectangle rectangle = rectangleObject.getRectangle();
        PolygonShape polygon = new PolygonShape();
        Vector2 size = new Vector2((rectangle.x + rectangle.width * 0.5f) * PTP,
                                   (rectangle.y + rectangle.height * 0.5f ) * PTP);
        polygon.setAsBox(rectangle.width * 0.5f * PTP,
                         rectangle.height * 0.5f * PTP,
                         size,
                         0.0f);        
        
        return polygon;
    }

    private static CircleShape getCircle(CircleMapObject circleObject) {
        Circle circle = circleObject.getCircle();
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(circle.radius * PTP);
        circleShape.setPosition(new Vector2(circle.x * PTP, circle.y * PTP));
        return circleShape;
    }

    private static PolygonShape getPolygon(PolygonMapObject polygonObject) {
        PolygonShape polygon = new PolygonShape();
        float[] vertices = polygonObject.getPolygon().getTransformedVertices();

        float[] worldVertices = new float[vertices.length];

        for (int i = 0; i < vertices.length; ++i) {
            //MyGame.sop(vertices[i]* PTP);
            worldVertices[i] = vertices[i] * PTP;
        }

        polygon.set(worldVertices);
        return polygon;
    }
	
	//this class will enqueue task which needs to be executed outside world step
	class TaskQueue {

		public static final int GRAVITY_FLI = 0;
		public static final int GRAVITY_FIX = 1;
		public static final int CLEAR_LEVEL = 2;

		Array<Integer> tasks;
		
		public TaskQueue(){
			tasks= new Array<Integer>();
			
		}
		
		public void execute(){
			for(Integer i : tasks){
				switch(i){
				case GRAVITY_FLI: //flipGravity();
					break;
				case GRAVITY_FIX: //fixGravity();
					break;
				case CLEAR_LEVEL:{
					//change game state
					GameScreen.CURRENT_STATE = GameState.EVOLVING;
					
					//directly jump to next level if this is intro
					loadNextLevel();
					
					break;
				}				
				}
			}
			
			tasks.clear();
		}

		public void push(int t){
			tasks.add(new Integer(t));
		}
		
		public void clear(){
			tasks.clear();
		}
	}

	public Switch getLevelSwitch() {
		//return levelSwitch;
		return null;
	}
	
	public int getSwitchCount(){
		return switchPool.size;
	}

	/**number of coins collected**/
	public int getCoinCollected(){
		int t = 0;
		for(Coin c:coinsPool){
			if(c.consumed == true)
				t++;
		}
		
		return t;
	}

	public void playMenuMusic() {
		// TODO Auto-generated method stub
		
	}

	public void playCoinSound(){
		MyGame.sop("COIN SOUND");

		if(!GameScreen.BACKGROUND_MUSIC) return;
		
		if(coinSound != null)
			coinSound.play();
		
	}
	
	public void playFinishSound(){
		if(!GameScreen.BACKGROUND_MUSIC) return;

		if(finishSound != null)
			finishSound.play();
	}
	
	public void playEpicLevelSound(){
		if(!GameScreen.BACKGROUND_MUSIC) return;

		if(epicLevelSound != null)
			epicLevelSound.play();
	}
	
	public void playFireSound(){
		MyGame.sop("Bullet SOUND");

		if(!GameScreen.BACKGROUND_MUSIC) return;

		MyGame.sop("Bullet SOUND can");

		if(fireSound != null)
			fireSound.play();
	}
	
	public void playPlayerHitSound(){
		if(!GameScreen.BACKGROUND_MUSIC) return;

		if(playerHurtSound != null)
			playerHurtSound.play();
		
	}
	
	public void playEnemyHitSound(){
		if(!GameScreen.BACKGROUND_MUSIC) return;

		if(enemyHurtSound != null)
			enemyHurtSound.play();
		
	}
	
	public void playLevelUpSound(){
		if(!GameScreen.BACKGROUND_MUSIC) return;

		if(levelUpSound != null)
			levelUpSound.play();
	}
}
