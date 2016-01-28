package Screens;

import objects.Background;
import objects.Friend;
import objects.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.softnuke.epic.MyGame;

import Screens.MainMenuScreen;
import utils.AssetLord;
import utils.CameraShake;
import utils.Cinema;
import utils.GameState;
import utils.LevelGenerate;
import utils.MyContactListener;
import utils.MyInputProcessor;
import utils.NetworkManager;
import utils.Recorder;
import utils.ScoreManager;

/**
 * Created by Kush.
 * 
 * TODO:
 * 
 * Make basic Main Menu
 * Create switchPool _ done
 * odd activities when weapon hit enemy
 * add intro
 * add tutorial
 * add lasers
 * add teleport sequence
 * make switch enable/disable lasers
 * change sword swing to a sensor shield 
 * add fire button hold make bullets animation
 * make multiplayer server selection screen
 * fix kryonet version changes
 * fix program connect/disconnect stuck bug
 * fix android client connect crash
 * unable to update enemy when client kill one- bug
 * update friend and ghost
 * create two different pools for bullets - maybe not
 * Blood explosion
 * better level complete portals, add lights in the tunnel
 * draw sword swing animation one more and play it if sword is already in motion _ done
 * bug: portal active even if first switch is active _ fixed
 * bug: recorder doesn't work, when last_rec == rec in count _ fixed
 * 
 */
public class GameScreen implements Screen {

    public static boolean DEBUG = false;
    public static boolean SOFT_DEBUG = false;
    public static boolean DISABLE_ADS = true;

    public static boolean RENDER_LIGHTS = true;
    public static boolean BACKGROUND_SHADER = false; //no use right now
    public static boolean BACKGROUND_PARALLAX = false;
    public static boolean BACKGROUND_PARTICLES = true;
    public static boolean PLAYER_PARTICLES = true;
    public static boolean BACKGROUND_MUSIC = true;
    
    public static boolean MULTIPLAYER = true;
    
    MyGame game;
    AssetLord Assets;
    public static GameScreen _gameScreen = null;
    int WIDTH = MyGame.WIDTH, HEIGHT = MyGame.HEIGHT;
    int bWIDTH = MyGame.bWIDTH, bHEIGHT = MyGame.bHEIGHT;
    float PTP = MyGame.PTP;//pixel to point

    public OrthographicCamera camera, cameraui;
    Viewport viewport;
    SpriteBatch batch;
    ShapeRenderer canvas;
    World world;
    Box2DDebugRenderer debugRenderer;

	public static GameState CURRENT_STATE = GameState.RUNNING;
	public static Vector2 WorldGravity = new Vector2(0,-19f); //-80,0
	public boolean StageVisible = true;
	public boolean UpdateOnceGameOver = true; //helps in doing things only once
	public static boolean SLOW_MOTION = false;
	
	private double stepAccumulator = 0;
    private double stepCurrentTime = 0;
    private float TIME_STEP = 1.0f / 60.0f;	
    
	boolean ScreenFinished = false;
	float Fade = 1f;
	Sprite blackFade;

	int lastScore = 0, levelScore = 0;

	public Recorder recorder;
	MyInputProcessor inputProcessor;
	CameraShake cameraShake;
	Player player;
	Friend friend = null;
	LevelGenerate level;
	public NetworkManager networkManager = null;
	ScoreManager scoreManager;
	Preferences prefs;
	Stage stage;
	BitmapFont fontSmall, fontMedium, fontLarge;
	Label scoreLabel;
	Group pauseScreen;
	VerticalGroup networkSelectGroup, serverSelectGroup;
	public Group levelClearScreen;
	TextButton nextLevelText, readyButt, scoreBoard, scoreTextButt, scoreHighTextButt, coinsCollectedCount, enemyKilledCount, serverList;
	Group controls;
	Image fireImage, pauseBack, coinStarImage, enemyStarImage;
	Background background;
	public Cinema cinema;

	private TextureAtlas gameAtlas;
	
	public GameScreen(MyGame g, AssetLord ass, int lno){
		
		MyGame.sop("level loading:"+lno);
		//TODO
		LevelGenerate.CURRENT_LEVEL = lno;
		if(lno < 0){				
			//negative values for multiplayer levels
			MULTIPLAYER = true;
		}
		else
		{
			MULTIPLAYER = false;
		}
	
        game = g;
        Assets = ass;
        _gameScreen = this;
        
		prefs = Gdx.app.getPreferences(MainMenuScreen.PreferenceName);
		/////////
		camera = new OrthographicCamera();
		camera.setToOrtho(false, bWIDTH, bHEIGHT);
		//camera.position.set(bWIDTH/2, bHEIGHT/2 + bHEIGHT/6, 0);
		//camera.update();
		
		cameraui = new OrthographicCamera();
		cameraui.setToOrtho(false, WIDTH, HEIGHT);
		cameraui.position.set(cameraui.viewportWidth/2, cameraui.viewportHeight/2, 0);
		cameraui.update();
		
		//viewport = new FitViewport(bWIDTH, bHEIGHT, bWIDTH*1.5f, bHEIGHT, camera);
		//viewport = new FitViewport(bWIDTH, bHEIGHT, camera);
		//viewport = new StretchViewport(bWIDTH, bHEIGHT,camera);
		//viewport.apply(true);
		camera.position.set(camera.viewportWidth/2, camera.viewportHeight/2, 0);
		camera.update();
		
		
		batch = new SpriteBatch();		
		
		if(DEBUG)
		canvas = new ShapeRenderer();

		//i have no idea how i initialized this stage
		stage = new Stage(new ScalingViewport(Scaling.stretch, WIDTH, HEIGHT, cameraui), batch);
		
		if(DEBUG) stage.setDebugAll(true);
			
		//handle user inputs
		InputMultiplexer multiplexer = new InputMultiplexer();
		inputProcessor = new MyInputProcessor();
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(inputProcessor);
		
		Gdx.input.setInputProcessor(multiplexer);
		Gdx.input.setCatchBackKey(true);		
				
		//-9.8f
		world = new World(WorldGravity, false);
		world.setAutoClearForces(false);
		if(DEBUG) debugRenderer = new Box2DDebugRenderer();
		
		//handle collision events
		MyContactListener contactListener = new MyContactListener();
		world.setContactListener(contactListener);
		
		gameAtlas = Assets.manager.get(AssetLord.game_atlas, TextureAtlas.class);
		
		fontMedium = Assets.manager.get(AssetLord.medium_font, BitmapFont.class);
		fontLarge = Assets.manager.get(AssetLord.large_font, BitmapFont.class);
		fontSmall = Assets.manager.get(AssetLord.small_font, BitmapFont.class);
		
		createObjects();
		createUI();
		
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    }
    
    public void createObjects(){
    	//create scoremanager before cinema
		scoreManager = new ScoreManager();
		
		//create cinema before level
		cinema = new Cinema(stage, camera);
		
		//create player before than level
		player = new Player(world);
		
		if(MULTIPLAYER)
		{
			friend = new Friend(world);
			networkManager = new NetworkManager();
		}
		
		cameraShake = new CameraShake(camera);
		
		level = new LevelGenerate(camera, world, batch);
		
		if(!MULTIPLAYER){
			recorder = new Recorder(world);
			recorder.record();
		}
		
		background = new Background(camera);

    }
    
    public void createUI(){
    	controls = new Group();
		
		final Image moveLeftImage = new Image(gameAtlas.findRegion("button-left"));
		moveLeftImage.setSize(HEIGHT*0.25f, HEIGHT*0.25f * moveLeftImage.getHeight()/moveLeftImage.getWidth());
		moveLeftImage.setPosition(WIDTH/35 , 0);
		moveLeftImage.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				inputProcessor.makeActionLeft();
				moveLeftImage.setColor(1f, 1f, 1f, 1f);
				return true;
			}
			
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {				
				moveLeftImage.setColor(1f, 1f, 1f, 0.6f);
				inputProcessor.leaveActionLeft();
			}
		});
		moveLeftImage.setColor(1f, 1f, 1f, 0.6f);
		
		final Image moveRightImage = new Image(gameAtlas.findRegion("button-right"));
		moveRightImage.setSize(HEIGHT*0.25f, HEIGHT*0.25f * moveRightImage.getHeight()/moveRightImage.getWidth());
		moveRightImage.setPosition(moveLeftImage.getWidth() + WIDTH/30, 0);
		moveRightImage.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				inputProcessor.makeActionRight();
				moveRightImage.setColor(1f, 1f, 1f, 1f);
				return true;
			}
			
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {				
				inputProcessor.leaveActionRight();
				moveRightImage.setColor(1f, 1f, 1f, 0.6f);

			}
		});
		moveRightImage.setColor(1f, 1f, 1f, 0.6f);
		
		final Image jumpImage = new Image(gameAtlas.findRegion("button-up"));		
		jumpImage.setSize(HEIGHT*0.25f, HEIGHT*0.25f  * jumpImage.getHeight()/jumpImage.getWidth());
		jumpImage.setPosition(WIDTH - WIDTH/20 - jumpImage.getWidth(), 0);
		jumpImage.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				inputProcessor.makeActionUp();		
				jumpImage.setColor(1f, 1f, 1f, 1f);
				return true;
			}
			
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {				
				inputProcessor.leaveActionUp();
				jumpImage.setColor(1f, 1f, 1f, 0.6f);
			}
		});
		jumpImage.setColor(1f, 1f, 1f, 0.6f);

		
		fireImage = new Image(gameAtlas.findRegion("button-fire"));
		fireImage.setSize(HEIGHT*0.25f, HEIGHT*0.25f  * fireImage.getHeight()/fireImage.getWidth());
		fireImage.setPosition(WIDTH - WIDTH/15 - fireImage.getWidth() - jumpImage.getWidth(), 0);
		fireImage.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				//level.fireBullet();
				
				//if(Player.getInstance().PLAYER_EVOLUTION == PowerUp.LEVEL_ONE)
				//	level.fireBullet();
				//else if(Player.getInstance().PLAYER_EVOLUTION == PowerUp.LEVEL_TWO)
					inputProcessor.makeActionFire();	
					
					fireImage.setColor(1f, 1f, 1f, 1f);

				return true;
			}
			
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {				
				//if(Player.getInstance().PLAYER_EVOLUTION == PowerUp.LEVEL_TWO)
					inputProcessor.leaveActionFire();
					
					fireImage.setColor(1f, 1f, 1f, 0.6f);
			}
		});
		fireImage.setColor(1f, 1f, 1f, 0.6f);
		
		
		controls.addActor(moveLeftImage);
		controls.addActor(moveRightImage);
		controls.addActor(fireImage);
		controls.addActor(jumpImage);
		
		stage.addActor(controls);
		
		Label.LabelStyle scoreStyle = new Label.LabelStyle();
		scoreStyle.fontColor = Color.WHITE;
		scoreStyle.font = fontSmall;
		
		scoreLabel = new Label("0000000", scoreStyle);
		scoreLabel.setWidth(WIDTH/9);
		scoreLabel.setPosition(scoreLabel.getWidth() * 1.2f, HEIGHT - scoreLabel.getHeight()*0.8f, Align.right);
		
		//added score label before cinema
		stage.addActor(scoreLabel);
		
		//used to prioritize its view over score
		cinema.addDialogue();
		
		createPauseScreen();
		
		createLevelClearScreen();
		
		createNetworkScreen();
		
		createServerSelectScreen();
	}
	
    /**Option to join one of the many servers*/
	private void createServerSelectScreen() {
		serverSelectGroup = new VerticalGroup();
		serverSelectGroup.setVisible(false);
		
		serverSelectGroup.setSize(WIDTH/2, HEIGHT/2);
		serverSelectGroup.setPosition(WIDTH/2 - serverSelectGroup.getWidth()/2, HEIGHT/2 - serverSelectGroup.getHeight()/2);
		
		TextButtonStyle smallStyle = new TextButtonStyle();
		smallStyle.font = fontMedium;
		
		serverList = new TextButton("Server", smallStyle);
		serverList.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if(networkManager != null)
					networkManager.connectToServer();
				return true;
			}
			
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				serverSelectGroup.setVisible(false);				
			}
		});
		
		serverSelectGroup.addActor(serverList);
		stage.addActor(serverSelectGroup);
	}
	
	public void setServerList(String text){
		serverList.setText(text);
	}

	/**Option to create server or join one*/
	private void createNetworkScreen() {
		networkSelectGroup = new VerticalGroup();
		if(MULTIPLAYER)
			networkSelectGroup.setVisible(true);
		else
			networkSelectGroup.setVisible(false);
		
		networkSelectGroup.setSize(WIDTH/2, HEIGHT/2);
		networkSelectGroup.setPosition(WIDTH/2 - networkSelectGroup.getWidth()/2, HEIGHT/2 - networkSelectGroup.getHeight()/2);
		
		TextButtonStyle smallStyle = new TextButtonStyle();
		smallStyle.font = fontMedium;
		
		TextButton beServer = new TextButton("Create server", smallStyle);
		beServer.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if(networkManager != null)
					networkManager.makeMeServer();
				return true;
			}
			
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				networkSelectGroup.setVisible(false);
				
			}
		});
		TextButton beClient = new TextButton("Join server", smallStyle);
		beClient.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if(networkManager != null)
					networkManager.makeMeClient();
				return true;
			}
			
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				networkSelectGroup.setVisible(false);
				serverSelectGroup.setVisible(true);
			}
		});
		
		networkSelectGroup.addActor(beServer);
		
		networkSelectGroup.addActor(beClient);

		
		smallStyle.font = fontSmall;
		TextButton notes = new TextButton("Connect both device to same\n wifi network or create a hotspot\n in one device and request another\n one to join.\n\nUnder Construction", smallStyle);
		//notes.setPosition(server, y, alignment);
		networkSelectGroup.addActor(notes);


		
		stage.addActor(networkSelectGroup);
	}

	private void createLevelClearScreen() {
		
		levelClearScreen = new Group();
		
		TextButtonStyle smallStyle = new TextButtonStyle();
		smallStyle.font = fontMedium;
		
		TextButton levelClearText = new TextButton("Level Cleared", smallStyle);
		levelClearText.setDisabled(true);
		levelClearText.setPosition(WIDTH/2 - levelClearText.getWidth()/2, HEIGHT - levelClearText.getHeight() * 1.2f);
		
		levelClearScreen.addActor(levelClearText);
		
		TextButtonStyle largeStyle = new TextButtonStyle();
		largeStyle.font = fontLarge;
		
		nextLevelText = new TextButton("Next Level", largeStyle);
		nextLevelText.setPosition(WIDTH/2 - nextLevelText.getWidth()/2, HEIGHT/2 - nextLevelText.getHeight()/2);
		nextLevelText.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				
				startloadingNextLevel();
				
				return true;
			}
		});		
		
		levelClearScreen.addActor(nextLevelText);
		
		TextButton menuText = new TextButton("MAIN MENU", smallStyle);
		//menuText.setPosition(WIDTH/2 - menuText.getWidth()/2, HEIGHT/2 - HEIGHT/4 - menuText.getHeight()/2);
		menuText.setPosition(WIDTH - menuText.getWidth() * 1.2f, menuText.getHeight()/2);
		//menuText.setRotation(90);
		
		menuText.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				
				returnToMainMenu();
				
				return true;
			}
		});
		
		levelClearScreen.addActor(menuText);		

		
		TextButton coinsCollected = new TextButton("COINS COLLECTED", smallStyle);
		coinsCollected.align(Align.left);
		coinsCollected.setPosition(WIDTH / 4, HEIGHT * 0.7f);
		
		levelClearScreen.addActor(coinsCollected);		

		TextButton enemyKilled = new TextButton("ENEMIE KILLS", smallStyle);
		enemyKilled.align(Align.left);
		enemyKilled.setPosition(WIDTH / 4, coinsCollected.getY() + coinsCollected.getHeight()*1.2f);
		
		levelClearScreen.addActor(enemyKilled);
		
		
		//Texture coinTexture = new Texture("level/star.png");
		//coinTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		coinStarImage = new Image(gameAtlas.findRegion("star"));
		coinStarImage.setSize(coinsCollected.getHeight(), coinsCollected.getHeight() * coinStarImage.getWidth()/coinStarImage.getHeight());
		coinStarImage.setPosition(coinsCollected.getX() - coinStarImage.getWidth()*1.2f, coinsCollected.getY());
		
		enemyStarImage = new Image(gameAtlas.findRegion("star"));
		enemyStarImage.setSize(enemyKilled.getHeight(), enemyKilled.getHeight() * enemyStarImage.getWidth()/enemyStarImage.getHeight());
		enemyStarImage.setPosition(enemyKilled.getX() - enemyStarImage.getWidth()*1.2f, enemyKilled.getY());
		
		levelClearScreen.addActor(coinStarImage);
		levelClearScreen.addActor(enemyStarImage);
		
		coinsCollectedCount = new TextButton("0/0", smallStyle);
		coinsCollectedCount.align(Align.right);
		coinsCollectedCount.setPosition(WIDTH - WIDTH / 4, HEIGHT * 0.7f);
		
		levelClearScreen.addActor(coinsCollectedCount);		

		enemyKilledCount = new TextButton("0/0", smallStyle);
		enemyKilledCount.align(Align.left);
		enemyKilledCount.setPosition(WIDTH - WIDTH / 4, coinsCollectedCount.getY() + coinsCollectedCount.getHeight()*1.2f);
		
		levelClearScreen.addActor(enemyKilledCount);
		
		
		levelClearScreen.setVisible(false);
		stage.addActor(levelClearScreen);
	}
	
	public void showLevelClear(){
		level.playFinishSound();

		//update max level score
		scoreManager.unlockLevel(LevelGenerate.CURRENT_LEVEL+1);
		if(scoreManager.updateHighScore())
		{
			//highScoreHighLight.setVisible(true);
			
			//submit to leaderboard
			//JumperGame.platform.submitLeaderboard(scoreManager.USER_SCORE);
		}
		else
			;//highScoreHighLight.setVisible(false);
		
		scoreManager.increaseDeath();
		scoreManager.updateTotalScore();
		
		//update screen stats
		coinsCollectedCount.setText(level.getCoinCollected() +"/"+ level.coinsPool.size);
		enemyKilledCount.setText(level.getEnemyKilled() +"/"+ level.enemyPool.size);
		if(level.getCoinCollected() == level.coinsPool.size)
			{//all coins collected
				coinStarImage.setVisible(true);
				//scoreManager.unlockStars(LevelGenerate.CURRENT_LEVEL, ScoreManager.STAR_MILK);
			}
		else
			coinStarImage.setVisible(false);
		if(level.getEnemyKilled() == level.enemyPool.size)
			{//all enemies killed
				enemyStarImage.setVisible(true);
				//scoreManager.unlockStars(LevelGenerate.CURRENT_LEVEL, ScoreManager.STAR_ENEMY);
			}
		else
			enemyStarImage.setVisible(false);
		
		pauseBack.setVisible(true);
		
		//don't go forward, its the end my friend
		if(LevelGenerate.CURRENT_LEVEL == LevelGenerate.MAX_LEVELS)
			nextLevelText.setVisible(false);
		else
			nextLevelText.setVisible(true);
		
		levelClearScreen.setVisible(true);
		
		//if you are stupid and want to play alone, just dont bother to create server then
		networkSelectGroup.setVisible(false);
		
		//save !!
		scoreManager.save(0);
	}
	
	public void startloadingNextLevel(){

		levelClearScreen.setVisible(false);
		pauseBack.setVisible(false);
		
		//change game state
		CURRENT_STATE = GameState.EVOLVING;
		
		level.loadNextLevel();
		
	}
	
	public void createPauseScreen(){
		//pause screen back background transparent
		Texture pauseBackTex = Assets.manager.get(AssetLord.pause_back_tex, Texture.class);
		pauseBackTex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		pauseBack = new Image(pauseBackTex);
		pauseBack.setSize(WIDTH, HEIGHT);
		pauseBack.setColor(1f, 1f, 1f ,0.95f);
		pauseBack.setPosition(0, 0);
		pauseBack.setVisible(false);
		stage.addActor(pauseBack);
		
		pauseScreen = new Group();
		
		TextButtonStyle smallStyle = new TextButtonStyle();
		smallStyle.font = fontMedium;
		
		TextButton pausedText = new TextButton("PAUSED", smallStyle);
		pausedText.setDisabled(true);
		pausedText.setPosition(WIDTH/2 - pausedText.getWidth()/2, HEIGHT/2 + HEIGHT/4 - pausedText.getHeight()/2);
		
		pauseScreen.addActor(pausedText);
		
		TextButtonStyle largeStyle = new TextButtonStyle();
		largeStyle.font = fontLarge;
		
		TextButton continueText = new TextButton("CONTINUE", largeStyle);
		continueText.setPosition(WIDTH/2 - continueText.getWidth()/2, HEIGHT/2 - continueText.getHeight()/2);
		continueText.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				
				resumeGame();
				
				return true;
			}
		});
		
		pauseScreen.addActor(continueText);
		
		TextButton menuText = new TextButton("MAIN MENU", smallStyle);
		//menuText.setPosition(WIDTH/2 - menuText.getWidth()/2, HEIGHT/2 - HEIGHT/4 - menuText.getHeight()/2);
		menuText.setPosition(WIDTH - menuText.getWidth() * 1.2f, menuText.getHeight()/2);
		//menuText.setRotation(90);
		
		menuText.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				
				returnToMainMenu();
				
				return true;
			}
		});
		
		pauseScreen.addActor(menuText);		
		
		hidePauseScreen();
		
		stage.addActor(pauseScreen);
	}
	
	public void showPauseScreen(){
		pauseScreen.setVisible(true);
		pauseBack.setVisible(true);
		level.pauseMusic();

	}
	
	public void hidePauseScreen(){
		pauseScreen.setVisible(false);
		pauseBack.setVisible(false);
		
	}
	
	public void resumeGame(){
		hidePauseScreen();
		GameScreen.CURRENT_STATE = GameState.RUNNING;
		level.resumeEverything();
	}

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
    	Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
    	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    	
    	if(CURRENT_STATE != GameState.STOPPED)
			update(delta);
		
		if(CURRENT_STATE != GameState.EVOLVING){
		//do if level is not processing
			
			if(CURRENT_STATE == GameState.RUNNING){
				//all the physics should step now
				doPhysics(delta);
				
			}
					
			camera.update();
			
			background.draw(batch);

			//pCounter.start();
			//resume developing this
			//background.draw(batch);
			
			//background.render();
			//pCounter.stop();
			
			
			level.render(batch, canvas);
			
			if(DEBUG)
			{
				canvas.setProjectionMatrix(camera.combined);
				canvas.begin(ShapeType.Filled);
				player.render(canvas);
				if(friend != null)
					friend.render(canvas);
				canvas.end();
	
				debugRenderer.render(world, camera.combined);
			}
			
			batch.setProjectionMatrix(camera.combined);
			batch.begin();
			cinema.render(batch);
			
			//blend function will become bad
			level.renderParticles(batch);
			player.renderParticles(batch);
			//reset blend function
			batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

			//single player
			if(recorder!= null)
				recorder.render(batch);
			
			//multiplayer
			if(friend != null)
				friend.render(batch);
			
			//self
			player.render(batch);
			
			//things that needs to be rendered over player
			level.renderOverlayed(batch);

			batch.end();
			
			if(MULTIPLAYER){
				//batch.setTransformMatrix(cameraui.combined);
				//batch.begin();
				//fontLarge.draw(batch, NetworkManager.STATE+","+NetworkManager.NET_STATE, WIDTH, HEIGHT - HEIGHT/15);			
				//batch.end();				
			}


			if(RENDER_LIGHTS)
				renderLights();
		}
		
		stage.draw();
		
		//cinema.render(batch);
		cinema.update(delta);
		
		batch.setProjectionMatrix(cameraui.combined);
		batch.begin();
		cinema.renderUI(batch);
		batch.end();
    }
    

    private void update(float delta) {
    	
    	//update score text
		if(lastScore < scoreManager.USER_SCORE)
		{
			if(scoreManager.USER_SCORE < 10)
				scoreLabel.setText("000000".concat(String.valueOf(scoreManager.USER_SCORE)));
			else if(scoreManager.USER_SCORE < 100)
				scoreLabel.setText("00000".concat(String.valueOf(scoreManager.USER_SCORE)));
			else if(scoreManager.USER_SCORE < 1000)
				scoreLabel.setText("0000".concat(String.valueOf(scoreManager.USER_SCORE)));
			else if(scoreManager.USER_SCORE < 10000)
				scoreLabel.setText("000".concat(String.valueOf(scoreManager.USER_SCORE)));
			else if(scoreManager.USER_SCORE < 100000)
				scoreLabel.setText("00".concat(String.valueOf(scoreManager.USER_SCORE)));
			else if(scoreManager.USER_SCORE < 1000000)
				scoreLabel.setText("0".concat(String.valueOf(scoreManager.USER_SCORE)));
			else if(scoreManager.USER_SCORE < 10000000)
				scoreLabel.setText(String.valueOf(scoreManager.USER_SCORE));
			
			lastScore = scoreManager.USER_SCORE;
		}
    			
		stage.act(delta);
		
		if(player.isDead()){// && !levelClearScreen.isVisible()){
			reset(false);
		}
		
		if(CURRENT_STATE == GameState.RUNNING)
		{
			cameraShake.update(delta);
			
			//update camera pos
			updateCameraMovement(delta);
			
			if(MULTIPLAYER)
			{
				networkManager.update(delta);
				if(DEBUG)
					scoreLabel.setText(networkManager.STATE +":"+networkManager.NET_STATE);
			}
			
			background.update(delta);

		}
		
	}
    
	public void reset(boolean newLevel) {
		if(!newLevel){
			//game is not closed
			GameScreen.CURRENT_STATE = GameState.RUNNING;
			//reset user score
			scoreManager.reset();
			lastScore = 0;	
			scoreLabel.setText("0000000");
			
		}
		SLOW_MOTION = false;
		
		level.reset();
		
		if(!MULTIPLAYER){
			recorder.reset();
			recorder.play();			
		}
		
		player.reset();
		
		if(friend!=null)
			friend.reset();
		
	}

    private void updateCameraMovement(float delta) {
    	float offsetX = 4	; //player and camera difference
    	//float offsetY = 1;
		float lerp = 5f;
		
		Vector3 position = this.getCamera().position;
		
		//max upper bound
		//fix this if you can
		//float uy = level.MAP_UPPER_BOUND/4f - offsetY;
		//float plY = Math.min(uy, player.getPosition().y);
		
		float plY = player.getPosition().y;
		//max lower bound
		float y = Interpolation.linear.apply(position.y, Math.max(plY, 6), delta*lerp);
		position.y = y;

		//max right bound			
		float plX = Math.min(level.MAP_RIGHT_BOUND - 14.7f, player.getPosition().x + offsetX);
		//max left bound
		float x = Interpolation.linear.apply(position.x, Math.max(plX, 10), delta*lerp);
		position.x = x;
		
		camera.update();
		
		//MyGame.sop("CAMY"+y+" UP"+level.MAP_UPPER_BOUND);
	}

	private void doPhysics(float delta) {
		
		if(!SLOW_MOTION)
			TIME_STEP = 1/60f;
		else
			TIME_STEP = delta = 1/180f;
		
		float frameTime = Math.min(delta, 0.25f);	    
	    stepAccumulator += frameTime;   
	    
	    while (stepAccumulator >= TIME_STEP) {
	    		    	
	    	player.update(TIME_STEP);
	    	if(friend != null) 	friend.update(TIME_STEP);
	    	
			level.update(TIME_STEP);
			
			if(!MULTIPLAYER) 
				recorder.update(TIME_STEP);
			
			//if(CURRENT_STATE == GameState.RUNNING)
			world.step(TIME_STEP, 6, 2); 
			
            stepAccumulator -= TIME_STEP;            

            //world needs cleanup 
            level.cleanUpLevel();
	    }
	    
		
	    world.clearForces();
	}

	private void renderLights() {
		// TODO Auto-generated method stub
		
	}
	
	public void sendNetworkUpdate(int code){
		if(networkManager == null || !MULTIPLAYER) return;
		
		networkManager.sendUpdate(code);
	}

	@Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
	public void resume() {
		
		//CURRENT_STATE = GameState.PAUSED;
		//showPauseScreen();
	}

	@Override
	public void hide() {
		//save everything first
		//scoreManager.save(0);
		dispose();

	}

	@Override
	public void dispose() {
		reset(false);
		
		batch.dispose();
		if(DEBUG) canvas.dispose();
		player.dispose();
		level.dispose(false);
		world.dispose();
		if(friend != null) friend.dispose();
		if(DEBUG) debugRenderer.dispose();
		
		if(networkManager != null) networkManager.dispose();
		
		if(recorder!= null)
			recorder.dispose();
		
		
		//lightBuffer.dispose();	
		background.dispose();
	}
	
	public static GameScreen getInstance(){
		return _gameScreen;
	}

	public void returnToMainMenu() {
		GameScreen.CURRENT_STATE = GameState.STOPPED;
		//Gdx.app.exit();
		game.setScreen(new MainMenuScreen(game, Assets));
	}

	public ScoreManager getScoreManager(){
		return scoreManager;
	}
	
	public OrthographicCamera getCamera() {
		return camera;
	}

	public AssetLord getAssetLord() {
		return Assets;
	}

	public void shakeThatAss(boolean b) {
		cameraShake.shakeLight(true);
	}

	public void hideControls(){
		controls.setVisible(false);
	}
	
	public void showControls(){
		controls.setVisible(true);
	}

	public void increaseScore(int val) {
		if(player.checkDeath()) return;
		
		//if(readyButt.isVisible() == false){
		scoreManager.increaseScore(val);	
	}
}
