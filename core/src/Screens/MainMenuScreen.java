package Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.softnuke.epic.ActionListener;
import com.softnuke.epic.MyGame;

import Screens.GameScreen;
import Screens.MainMenuScreen;
import utils.AssetLord;
import utils.ScoreManager;

/**
 * Created by Kush on 09-12-2015.
 */
public class MainMenuScreen implements Screen, ActionListener{

    MyGame game;
    AssetLord Assets;
    public static final String PreferenceName = "EpicDimension";

    public static boolean BACKGROUND_SOUND = true;

    private boolean DEBUG = GameScreen.DEBUG;
    private int CURRENT_VIEW = 0;//-1 = store, 0 = home, 1 = options, 2 = credits, 3 = level select
    private int LAST_VIEW = 0;//helpful in analytics

    private int HIGH_SCORE = 0;
    private int Easter_Count = 0;

    public static MainMenuScreen _menuScreen = null;
    int WIDTH = MyGame.WIDTH, HEIGHT = MyGame.HEIGHT;
    int bWIDTH = MyGame.bWIDTH, bHEIGHT = MyGame.bHEIGHT;
    float PTP = MyGame.PTP;//pixel to point

    OrthographicCamera camera;
    private Stage stage;
    float time;
    int levelNo = 1; //level to start from

    SpriteBatch batch;
	Preferences prefs;
	ScoreManager scoreManager;
	
	Color color;
	BitmapFont fontSmall,fontLarge, fontMedium;
	Label totalScoreLabel,totalTextScoreLabel;
	Label.LabelStyle tscoreStyle,tscoreTextStyle;
	ParticleEffect introSmokeEffect;
	NinePatch ninep;
	NinePatchDrawable ninePatchDrawable;
	TextureRegion topBack;
	Texture logoBack, intro1T;
	
	Image logobackI;
	TextButton visualButt, tutorialButt, musicButt, creditsButt, rabbitButton, squareButton, sensaiButton, coinsButton ;
	Table table, optionsTable, store, credits, levelTable;
	Group menu;
	TextureAtlas atlas,atlasui;
	Skin skin;
	ParticleEffect squareFloor;
	Dialog dialog; 
	
	
	boolean ScreenFinished = false;
	float Fade = 1f;
	Sprite blackFade;
	
    public MainMenuScreen(MyGame g, AssetLord ass){
    	game = g;
    	Assets = ass;
		_menuScreen = this;
		
		Assets.manager.finishLoading();
		
		//register listener
        MyGame.platform.registerActionListener(this);
        
        //send analytics
        MyGame.platform.setTrackerScreen("MainMenu");
        
		
		init();
    }

    private void init(){
		camera = new OrthographicCamera();
		camera.setToOrtho(false, WIDTH, HEIGHT);
		camera.position.set(WIDTH/2, HEIGHT/2, 0);
		camera.update();
		
		
		batch = new SpriteBatch();		
		
		stage = new Stage(new ScalingViewport(Scaling.stretch, WIDTH, HEIGHT, camera), batch);
		
		
		//handle user inputs
		InputMultiplexer multiplexer = new InputMultiplexer();
		MenuInputProcessor inputProcessor = new MenuInputProcessor();
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(inputProcessor);
		Gdx.input.setInputProcessor(multiplexer);
		Gdx.input.setCatchBackKey(true);
		
		
		prefs = Gdx.app.getPreferences(MainMenuScreen.PreferenceName);
		
		BACKGROUND_SOUND = prefs.getBoolean("music", true);
		
		
		time = MathUtils.random(0f , 100f);	

		atlas = Assets.manager.get(AssetLord.menu_atlas, TextureAtlas.class);		
		
		fontSmall = Assets.manager.get(AssetLord.small_font, BitmapFont.class);
		fontMedium= Assets.manager.get(AssetLord.medium_font, BitmapFont.class);
		fontLarge = Assets.manager.get(AssetLord.large_font, BitmapFont.class);
		
		
		//skin = new Skin(Gdx.files.internal("final/uiskin.json"), atlas);
		skin = new Skin();
		skin.add("default-font", fontSmall);
		skin.addRegions(atlas);
		skin.load(Gdx.files.internal("atlas/uiskin.json"));
		
		//background black fade
		TextureRegion tBlackFade = atlas.findRegion("black");
		blackFade = new Sprite(tBlackFade);
		blackFade.setColor(Color.BLACK);
		blackFade.setPosition(0, 0);
		blackFade.setSize(WIDTH, HEIGHT);
		
		//particles
		squareFloor = Assets.manager.get(AssetLord.square_floor_particle, ParticleEffect.class);
		squareFloor.setPosition(-WIDTH/4, 0);
		squareFloor.start();		
		
				
		ninep = atlas.createPatch("buttonhell");
		ninep.setColor(Color.BLACK);
		ninePatchDrawable = new NinePatchDrawable(ninep);
		
		
		Image white = new Image(Assets.manager.get(AssetLord.menu_back_tex, Texture.class));
		white.setColor(1f, 1f, 1f, 0.6f);
		white.setSize(WIDTH, HEIGHT);
		//white.setOrigin(white.getWidth()/2, white.getHeight()/2);
		//white.setPosition(WIDTH/2 - white.getWidth()/2, HEIGHT/2 - white.getHeight()/2);
		white.setPosition(0, 0);
		stage.addActor(white);
		
		
		topBack = atlas.findRegion("black");
		
		Image topbackI = new Image(topBack);
		topbackI.setColor(Color.BLACK);
		topbackI.setSize(WIDTH, HEIGHT/5);
		topbackI.setPosition(0, HEIGHT - HEIGHT/5);
		
		stage.addActor(topbackI);
		
		TextureRegion tLogoBack = atlas.findRegion("white");
		
		logobackI = new Image(tLogoBack);
		logobackI.setSize(HEIGHT/5 * logobackI.getWidth()/logobackI.getHeight(), HEIGHT/5);
		logobackI.setPosition(WIDTH/2 - logobackI.getWidth()/2, HEIGHT - HEIGHT/5);
		logobackI.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
				//easter egg
				Easter_Count++;				
				
				return true;
			}
			public void touchUp(InputEvent event, float x, float y, int pointer, int button){
				if(Easter_Count == 10)
					MyGame.platform.showToast("May the force be with you.", false);
				
				if(Easter_Count == 20)
					MyGame.platform.showToast("Fasten your seat belts, it's going to be a bumby night!", false);
				
				if(Easter_Count == 30)
					MyGame.platform.showToast("Do I look like I give a damn?", false);
				
				if(Easter_Count == 40)
					MyGame.platform.showToast("Just keep swimming.", false);
				
				if(Easter_Count == 50)
					MyGame.platform.showToast("Oh my dear God - are you one of those single tear people?", false);
				
				if(Easter_Count == 100)
				{
					//MyGame.platform.showToast("You! Really deserves a trophy! Here is a bonus secret stage.", false);
					
					//game.setScreen(new GameScreen(game, Assets, 420));
				}
			}
		});
		
		table = new Table(skin);
		menu = new Group();
		store = new Table(skin);
		optionsTable = new Table(skin);
		levelTable = new Table(skin);
		credits = new Table(skin);

		//skin.add("storebutt", new Texture("ui/button-hell.png"), Texture.class);
		//skin.add("storebutt-up", new Texture("ui/button-hell-up.png"), Texture.class);

		menu.setVisible(true);
		optionsTable.setVisible(false);
		levelTable.setVisible(false);
		store.setVisible(false);
		credits.setVisible(false);
		
		
		//create home
			
		
	}
    
	@Override
	public void show() {
		scoreManager = new ScoreManager();

		//if(DEBUG) GLProfiler.enable();
		
		HIGH_SCORE = scoreManager.USER_HIGH_SCORE;

		//prepare music
		//menuMusic = Assets.manager.get(AssetLord.menu_music, Music.class);
		if(BACKGROUND_SOUND)
		{
			//menuMusic.play();
		}
		
		createMenu();

		createOptions();
		
		createStore();
		
		createCredits();
		
		//keep logo above everything
		stage.addActor(logobackI);		
		
		createLevelSelect();
		
		
		if(DEBUG){
			Table.debugTableColor = Color.CYAN;
			
			table.debug();
			optionsTable.debug();
			store.debug();
		}
		
		if(DEBUG) stage.setDebugAll(true);		

		
		color = new Color(1f,1f,1f,1f);
		
		
		//if(JumpGame.platform.isSignedIn())
		//	JumpGame.platform.signOut();
		
		//load large ad
		//if(!MyGame.platform.isAdsRemoved() && !GameScreen.DISABLE_ADS)
		
		if(!MyGame.platform.isAdsRemoved())
			MyGame.platform.loadAd();
		
		//check user internet
		MyGame.platform.isOnline();
	}
	
	private float getImageRatio(Image i){
		return i.getHeight()/i.getWidth();
	}
	
	private void createMenu(){
		menu.setSize(WIDTH, HEIGHT);
		menu.setPosition(WIDTH/2 - menu.getWidth()/2, HEIGHT/2 - menu.getHeight()/2);
		
		if(DEBUG)menu.debugAll();
		stage.addActor(menu);

		//skin.add("playImage", new Texture("ui/directional sign10.png"), Texture.class);


		//ADD TOTAL HIGH SCORE
		tscoreStyle = new Label.LabelStyle();
		tscoreStyle.font = fontLarge;
		tscoreStyle.fontColor = Color.WHITE;
		

		tscoreTextStyle = new Label.LabelStyle();
		tscoreTextStyle.font = fontMedium;
		tscoreTextStyle.fontColor = Color.WHITE;
		
		totalScoreLabel = new Label(""+HIGH_SCORE, tscoreStyle);
		Label totalTextScoreLabel = new Label("High Score", tscoreTextStyle);
		
		totalScoreLabel.setAlignment(Align.right);
		totalTextScoreLabel.setAlignment(Align.right);
		
		totalTextScoreLabel.setPosition(WIDTH - totalTextScoreLabel.getWidth(), HEIGHT - totalTextScoreLabel.getHeight());
		totalScoreLabel.setPosition(WIDTH - totalScoreLabel.getWidth(), HEIGHT - totalScoreLabel.getHeight() - totalTextScoreLabel.getHeight());
		
		menu.addActor(totalScoreLabel);
		menu.addActor(totalTextScoreLabel);
		
		
		
		
		TextureRegion playTex = atlas.findRegion("play-image");
		
		Image playImage = new Image(playTex);
		playImage.setSize(WIDTH/6, WIDTH/6 * getImageRatio(playImage));
		playImage.setPosition(WIDTH/2 - playImage.getWidth()/2, HEIGHT/2 - playImage.getHeight()/2);
		
		playImage.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
				//call fade out effect
				//ScreenFinished = true;
				
				menu.addAction(Actions.moveTo(WIDTH/2 - menu.getWidth()/2, 0 - HEIGHT/2 - menu.getHeight()/2,
						0.5f, Interpolation.fade));				
				menu.addAction(Actions.fadeOut(0.5f));
				
				levelTable.addAction(Actions.moveTo(WIDTH/2 -levelTable.getWidth()/2 , HEIGHT/2 -levelTable.getHeight()/2,
						0.5f, Interpolation.fade));
				levelTable.addAction(Actions.fadeIn(0.5f));
				
				
				CURRENT_VIEW = 3;

				
				return true;
			}
			public void touchUp(InputEvent event, float x, float y, int pointer, int button){
				MyGame.sop("Play!!");
			}
		});
		
		menu.addActor(playImage);
		
		TextureRegion optTex = atlas.findRegion("settings");
		
		Image optionImage = new Image(optTex);
		optionImage.setSize(WIDTH/10, WIDTH/10 * getImageRatio(optionImage));
		optionImage.setPosition(WIDTH/2 + WIDTH/4 - optionImage.getWidth()/2, HEIGHT/2 - HEIGHT/4 - optionImage.getHeight()/2);
		
		optionImage.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){

				menu.addAction(Actions.moveTo(0 - WIDTH/2 - menu.getWidth()/2, HEIGHT/2 - menu.getHeight()/2,
						0.5f, Interpolation.fade));				
				menu.addAction(Actions.fadeOut(0.5f));
				
				optionsTable.addAction(Actions.moveTo(0, 0, 0.5f, Interpolation.fade));
				optionsTable.addAction(Actions.fadeIn(0.5f));
				
				CURRENT_VIEW = 1;
				
				return true;
			}
			public void touchUp(InputEvent event, float x, float y, int pointer, int button){
				MyGame.sop("Options!!");
			}
		});
		
		menu.addActor(optionImage);

		
		TextureRegion storeTex = atlas.findRegion("shoppingcart");
		Image storeImage = new Image(storeTex);
		storeImage.setSize(WIDTH/10, WIDTH/10 * getImageRatio(storeImage));
		storeImage.setPosition(WIDTH/2 - WIDTH/4 - storeImage.getWidth()/2, HEIGHT/2 - HEIGHT/4 - storeImage.getHeight()/2);
		
		storeImage.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){

				menu.addAction(Actions.moveTo(0 - WIDTH/2 - menu.getWidth()/2, HEIGHT/2 - menu.getHeight()/2,
						0.5f, Interpolation.fade));				
				menu.addAction(Actions.fadeOut(0.5f));
				
				store.addAction(Actions.moveTo(WIDTH/2 - store.getWidth()/2, 0, 0.5f, Interpolation.fade));
				store.addAction(Actions.fadeIn(0.5f));
				
				CURRENT_VIEW = -1;
				
				return true;
			}
			public void touchUp(InputEvent event, float x, float y, int pointer, int button){
				MyGame.sop("Options!!");
			}
		});
		
		menu.addActor(storeImage);
		
		
		//Texture rateTex = new Texture("ui/star207.png");
		//rateTex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		TextureRegion rateTex = atlas.findRegion("star");

		Image rateImage = new Image(rateTex);
		rateImage.setSize(WIDTH/17, WIDTH/17* getImageRatio(rateImage));
		//rateImage.setOrigin(rateImage.getWidth()/2, rateImage.getHeight()/2);
		rateImage.setPosition( rateImage.getWidth()/2, rateImage.getHeight()*1.5f);
		rateImage.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){

				MyGame.platform.rateGame();				
				
				return true;
			}
			public void touchUp(InputEvent event, float x, float y, int pointer, int button){
				MyGame.sop("rate!!");
			}
		});
		rateImage.setOrigin(rateImage.getWidth()/2, rateImage.getHeight()/2);
		rateImage.addAction(Actions.forever(Actions.sequence(Actions.scaleTo(1.1f, 1.1f, 1.5f), Actions.scaleTo(0.9f, 0.9f, 1.5f))));
		menu.addActor(rateImage);
		
		//Texture shareTex = new Texture("ui/facebook2.png");
		//shareTex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		TextureRegion shareTex = atlas.findRegion("friends");

		Image shareImage = new Image(shareTex);
		shareImage.setSize(WIDTH/17, WIDTH/17* getImageRatio(shareImage));
		shareImage.setPosition( shareImage.getWidth()/2, shareImage.getHeight()/4);
		shareImage.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){

				MyGame.platform.appInvite();
				//MyGame.platform.showAd();
				
				return true;
			}
		});		
		
		menu.addActor(shareImage);
		
		TextureRegion shareEasyTex = atlas.findRegion("share-menu");

		Image shareEasyImage = new Image(shareEasyTex);
		shareEasyImage.setSize(WIDTH/17, WIDTH/17* getImageRatio(shareEasyImage));
		shareEasyImage.setPosition( WIDTH - shareEasyImage.getWidth()*1.5f, shareEasyImage.getHeight()/4);
		shareEasyImage.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
								
				MyGame.platform.shareScore(-1);
				
				return true;
			}
		});		
		
		menu.addActor(shareEasyImage);
		
		//Texture playLoginTex = new Texture("ui/play-games-icon.png");
		//playLoginTex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		TextureRegion playLoginTex = atlas.findRegion("play-games-icon-128");

		Image playLoginImage = new Image(playLoginTex);
		playLoginImage.setSize(WIDTH/17, WIDTH/17* getImageRatio(playLoginImage));
		playLoginImage.setPosition( WIDTH - playLoginImage.getWidth()*1.5f, playLoginImage.getHeight()*2.2f);
		playLoginImage.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
								
				MyGame.platform.getAchievementsGPGS();
				
				return true;
			}
		});		
		
		menu.addActor(playLoginImage);
		
		
		
	}
	
	private void createLevelSelect() {
		
		
		levelTable.setSize(WIDTH, HEIGHT - HEIGHT/5);
		levelTable.setPosition(0, HEIGHT);		
		levelTable.defaults().uniformX().pad(3);
		//levelTable.defaults().width(WIDTH/2.5f);
		//optionsTable.bottom();
				
				
		Table levelContainer = new Table(skin);
		//levelContainer.pad(WIDTH/20);
		//levelContainer.setFillParent(true);
		//levelContainer.setWidth(WIDTH*0.8f);
		levelContainer.defaults().width(WIDTH/10);
		levelContainer.defaults().height(WIDTH/10);

		levelContainer.center();
		
		Label.LabelStyle worldLabelStyle = new Label.LabelStyle(fontMedium, Color.WHITE);
		worldLabelStyle.background = skin.getDrawable("black");
		
		Label worldLabel = new Label("World 1", worldLabelStyle);
		worldLabel.setAlignment(Align.center);
		worldLabel.setColor(1f, 1f, 1f, 0.8f);
		levelTable.add(worldLabel).height(HEIGHT/10).fill().row();
		
		//skin.add("level-button-unlocked", atlas.findRegion("level-unlocked.png"));
		//skin.add("level-button-locked", new Texture("level-select/level-locked.png"), Texture.class);

		TextButtonStyle buttStyleLocked = new TextButtonStyle();
		buttStyleLocked.font = fontMedium;
		buttStyleLocked.fontColor = Color.WHITE;
		buttStyleLocked.pressedOffsetX = 2;
		buttStyleLocked.pressedOffsetY = -2;
		buttStyleLocked.up = skin.getDrawable("level-locked"); 
		buttStyleLocked.down = skin.getDrawable("level-locked");	
		
		TextButtonStyle buttStyleUnlock = new TextButtonStyle();
		buttStyleUnlock.font = fontMedium;
		buttStyleUnlock.fontColor = Color.WHITE;
		buttStyleUnlock.pressedOffsetX = 2;
		buttStyleUnlock.pressedOffsetY = -2;
		buttStyleUnlock.up = skin.getDrawable("level-unlocked"); 
		buttStyleUnlock.down = skin.getDrawable("level-unlocked");
		
		TextButtonStyle buttStyle1Star = new TextButtonStyle();
		buttStyle1Star.font = fontMedium;
		buttStyle1Star.fontColor = Color.WHITE;
		buttStyle1Star.pressedOffsetX = 2;
		buttStyle1Star.pressedOffsetY = -2;
		buttStyle1Star.up = skin.getDrawable("level-1star"); 
		buttStyle1Star.down = skin.getDrawable("level-1star");
		
		TextButtonStyle buttStyle2Star = new TextButtonStyle();
		buttStyle2Star.font = fontMedium;
		buttStyle2Star.fontColor = Color.WHITE;
		buttStyle2Star.pressedOffsetX = 2;
		buttStyle2Star.pressedOffsetY = -2;
		buttStyle2Star.up = skin.getDrawable("level-2star"); 
		buttStyle2Star.down = skin.getDrawable("level-2star");
		
		TextButtonStyle buttStyle3Star = new TextButtonStyle();
		buttStyle3Star.font = fontMedium;
		buttStyle3Star.fontColor = Color.WHITE;
		buttStyle3Star.pressedOffsetX = 2;
		buttStyle3Star.pressedOffsetY = -2;
		buttStyle3Star.up = skin.getDrawable("level-3star"); 
		buttStyle3Star.down = skin.getDrawable("level-3star");
		
		int l = 1;
		for(int i=0;i<3;i++){
			for(int j=0;j<5;j++)
			{
				if(l > scoreManager.MAX_LEVELS)
					continue;
				
				final TextButton b = new TextButton(""+(l), buttStyleLocked);

				if(l <= scoreManager.MAX_LEVELS_UNLOCKED){
					b.setStyle(buttStyleUnlock);
					
					int stars = scoreManager.getStars(l);
					switch(stars){
						case 1:
							b.setStyle(buttStyle1Star);
							break;
						case 2:
							b.setStyle(buttStyle2Star);
							break;
						case 3:
							b.setStyle(buttStyle3Star);
							break;
					}
					
				}
				else
				{
					b.setText("");
					b.setStyle(buttStyleLocked);
				}
								
				b.padBottom(b.getHeight()*0.6f);
				
				//only add listener if there are enough levels
				if(l <= scoreManager.MAX_LEVELS_UNLOCKED){
					b.addListener(new InputListener(){
						public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
							levelNo = Integer.parseInt(b.getText().toString());
							
							//start screen fade effect to start game
							ScreenFinished = true;
							return true;
						}
					});					
				}
				
				levelContainer.add(b).center();				
				l++;
			}
			
			levelContainer.row();
		}
		
		levelTable.add(levelContainer).row();
		
		
		TextButtonStyle playStyle = new TextButtonStyle();	
		playStyle.font = fontMedium;
		playStyle.fontColor = Color.WHITE;
		playStyle.pressedOffsetX = 2;
		playStyle.pressedOffsetY = -2;
		
		playStyle.down = ninePatchDrawable;//skin.getDrawable("storebutt");
		playStyle.up = ninePatchDrawable;
		
		TextButton backButt = new TextButton("Back", playStyle);
		backButt.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
							
				menu.addAction(Actions.moveTo(WIDTH/2 - menu.getWidth()/2, HEIGHT/2 - menu.getHeight()/2,
						0.5f, Interpolation.fade));				
				menu.addAction(Actions.fadeIn(0.5f));
				
				levelTable.addAction(Actions.moveTo(0 , HEIGHT,	0.5f, Interpolation.fade));
				levelTable.addAction(Actions.fadeOut(0.5f));
								
				CURRENT_VIEW = 0;
				
				return true;
			}
			public void touchUp(InputEvent event, float x, float y, int pointer, int button){
				if(DEBUG)System.out.println("Back!");
			}
		});
		
		levelTable.add(backButt).fill().padTop(HEIGHT/20);
		
		if(GameScreen.DEBUG)
		levelTable.setDebug(true);
		
		stage.addActor(levelTable);

	}
	
	
	private void createOptions(){
		
		
		//optionsTable.setWidth(WIDTH/2);
		//optionsTable.setHeight(HEIGHT/2);
		//optionsTable.setFillParent(true);
		optionsTable.setSize(WIDTH, HEIGHT - HEIGHT/5);
		optionsTable.setPosition(-WIDTH, 0);		
		optionsTable.defaults().uniformX().pad(3);
		optionsTable.defaults().width(WIDTH/2.5f);
		//optionsTable.bottom();
				
		TextButtonStyle playStyle = new TextButtonStyle();
//		playStyle.font = fontLarge;
//		playStyle.fontColor = Color.BLACK;
//		playStyle.pressedOffsetX = 2;
//		playStyle.pressedOffsetY = -2;
		
		playStyle.font = fontMedium;
		playStyle.fontColor = Color.WHITE;
		playStyle.pressedOffsetX = 2;
		playStyle.pressedOffsetY = -2;
		
		playStyle.down = ninePatchDrawable;//skin.getDrawable("storebutt");
		playStyle.up = ninePatchDrawable;//skin.getDrawable("storebutt-up");
		
		String musicButtName = "MUSIC ON";
		if(!prefs.getBoolean("music", true))
			musicButtName = "music off";
		
		musicButt = new TextButton(musicButtName, playStyle);		
		musicButt.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
				//optionsTable.getChildren().first().setColor(0.3f, 0.4f, 0.1f, 1f);
				
				TextButton t = musicButt;
				
				if(t.getText().toString().equals("music off"))
				{
					t.setText("MUSIC ON");
					prefs.putBoolean("music", true);
					
					//menuMusic.play();
					GameScreen.BACKGROUND_MUSIC = true;
					MainMenuScreen.BACKGROUND_SOUND = true;
				}
				else
				{
					t.setText("music off");
					prefs.putBoolean("music", false);
					
					//menuMusic.stop();
					GameScreen.BACKGROUND_MUSIC = false;
					MainMenuScreen.BACKGROUND_SOUND = false;
				}
				
				musicButt.invalidate();
				
				return true;
			}
			public void touchUp(InputEvent event, float x, float y, int pointer, int button){
				if(DEBUG)System.out.println("Music!");
			}
		});
		musicButt.padLeft(5f);
		musicButt.padRight(5f);
		optionsTable.add(musicButt).fill();
		//optionsTable.row();
		
		String tutorialButtName = "TUTORIAL ON";
		if(4 == scoreManager.TUTORIAL_LEVEL)
			tutorialButtName = "tutorial off";
		
		tutorialButt = new TextButton(tutorialButtName, playStyle);		
		tutorialButt.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
				//optionsTable.getChildren().first().setColor(0.3f, 0.4f, 0.1f, 1f);
				
				TextButton t = tutorialButt;
				
				if(t.getText().toString().equals("tutorial off"))
				{
					t.setText("TUTORIAL ON");
					scoreManager.tutorialSeen(0);
				}
				else
				{
					t.setText("tutorial off");
					scoreManager.tutorialSeen(4);
				}				
				
				return true;
			}
			public void touchUp(InputEvent event, float x, float y, int pointer, int button){
				if(DEBUG)System.out.println("Tutoria!");
			}
		});
		optionsTable.add(tutorialButt).fill();
		optionsTable.row();
		
		String visualButtName = "VISUALS HIGH";
		if(prefs.getInteger("visuals", 1) == 1)
			visualButtName = "Visuals Medium";
		else if(prefs.getInteger("visuals", 1) == 0)
			visualButtName = "visuals low";
		
		visualButt = new TextButton(visualButtName, playStyle);		
		visualButt.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
				//optionsTable.getChildren().first().setColor(0.3f, 0.4f, 0.1f, 1f);
				
				TextButton t = visualButt;
				
				if(t.getText().toString().equals("VISUALS HIGH"))
				{
					t.setText("Visuals Medium");
					prefs.putInteger("visuals", 1);

				}
				else if(t.getText().toString().equals("Visuals Medium"))
				{
					t.setText("visuals low");
					prefs.putInteger("visuals", 0);

				}
				else if(t.getText().toString().equals("visuals low"))
				{
					t.setText("VISUALS HIGH");
					prefs.putInteger("visuals", 2);					

				}
				
				visualButt.invalidate();
				
				return true;
			}
			public void touchUp(InputEvent event, float x, float y, int pointer, int button){
				if(DEBUG)System.out.println("Visual!");
			}
		});
		
//		visualButt.padLeft(5f);
//		visualButt.padRight(5f);
		
		optionsTable.add(visualButt).fill();
		//optionsTable.row();
		
		creditsButt = new TextButton("Credits", playStyle);		
		creditsButt.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
				
				optionsTable.addAction(Actions.moveTo(0 - WIDTH/2 - optionsTable.getWidth()/2, 0 ,
						0.5f, Interpolation.fade));
				optionsTable.addAction(Actions.fadeOut(0.5f));
				
				
				credits.addAction(Actions.moveTo(0 , 0,
						0.5f, Interpolation.fade));
				credits.addAction(Actions.fadeIn(0.5f));
				
				
				CURRENT_VIEW = 2;
				
				//unlock curious gentleman
				//MyGame.platform.unlockAchievementGPGS(Constants.ACHIEVEMENT_GENTLEMAN);
								
				return true;
			}
			public void touchUp(InputEvent event, float x, float y, int pointer, int button){
			
				if(DEBUG)System.out.println("Credits");
			}
		});
		optionsTable.add(creditsButt).fill();
		optionsTable.row();

		TextButton backButt = new TextButton("Back", playStyle);
		backButt.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
									
				menu.addAction(Actions.moveTo(WIDTH/2 - menu.getWidth()/2, HEIGHT/2 - menu.getHeight()/2,
						0.5f, Interpolation.fade));
				menu.addAction(Actions.fadeIn(0.5f));
				
				optionsTable.addAction(Actions.moveTo(-WIDTH , 0,
						0.5f, Interpolation.fade));
				optionsTable.addAction(Actions.fadeOut(0.5f));
				
				prefs.flush();
				
				CURRENT_VIEW = 0;
				
				return true;
			}
			public void touchUp(InputEvent event, float x, float y, int pointer, int button){
				if(DEBUG)System.out.println("Back!");
			}
		});
		
		optionsTable.add(backButt).colspan(2).fill().padTop(HEIGHT/10);
		
		
			
		stage.addActor(optionsTable);
	}
	
	private void createStore(){
		
		//left offscreen
		store.setSize(WIDTH, HEIGHT - HEIGHT/4);
		store.setPosition(-WIDTH/2 - store.getWidth()/2, 0);
		store.defaults().uniformX().pad(3);
		store.align(Align.bottom);
		
		Table scrollStore = new Table(skin);
		//scrollStore.setSize(store.getWidth(), store.getHeight());
		scrollStore.pad(10).defaults().expandX().space(4);
		scrollStore.defaults().uniformY().padTop(4);
		scrollStore.defaults().uniformY().padBottom(4);
		
		

		final ScrollPane scroll = new ScrollPane(scrollStore, skin);
		scroll.setFadeScrollBars(false);
		
		store.add(scroll).expand().fill();
		store.row();
		
		TextButtonStyle buyStyle = new TextButtonStyle();
		buyStyle.font = fontMedium;
		buyStyle.fontColor = Color.WHITE;
		
		Label.LabelStyle labelStyle = new Label.LabelStyle();
		labelStyle.font = fontSmall;
		labelStyle.fontColor = Color.WHITE;
		Label.LabelStyle labelHeadStyle = new Label.LabelStyle();
		labelHeadStyle.font = fontMedium;
		labelHeadStyle.fontColor = Color.WHITE;

		
		TextureRegion coinsTex = atlas.findRegion("coins-128");

		//under construction here
		
		Image coinsImage = new Image(coinsTex);
		scrollStore.add(coinsImage).width(100).height(100);

		VerticalGroup vGroupCoins = new VerticalGroup();
		
		vGroupCoins.addActor(new Label("Coins - UNDER CONSTRUCTION", labelHeadStyle));
		vGroupCoins.addActor(new Label("Remove ads and get 5000 score points free.", labelStyle));
		vGroupCoins.align(Align.left);
		scrollStore.add(vGroupCoins).align(Align.left);
		
		VerticalGroup vGroupCoinsBuy = new VerticalGroup();	
		
		String buyText = "Buy";
		if(MyGame.platform.isAdsRemoved())
			buyText = "Purchased";
		
		coinsButton = new TextButton(buyText, buyStyle);
		coinsButton.addListener(new ClickListener() {
			public void clicked (InputEvent event, float x, float y) {
				
				if(!MyGame.platform.isAdsRemoved()){
					MyGame.platform.removeAds();					
				}
				else
				{
					MyGame.platform.showToast("Already purchased.", true);				
				}
				
				if(DEBUG)System.out.println("coins click " + x + ", " + y);
			}
		});					
		
		coinsButton.pad(4);
		
		vGroupCoinsBuy.addActor(coinsButton);
		if(MyGame.platform.isAdsRemoved())
			vGroupCoinsBuy.addActor(new Label("", labelStyle));
		else
			vGroupCoinsBuy.addActor(new Label("$ 0.99", labelStyle));

		
		scrollStore.add(vGroupCoinsBuy);		
		scrollStore.row();
		
		
	
		
		
		TextButtonStyle storeStyle = new TextButtonStyle();
		storeStyle.font = fontMedium;
		storeStyle.fontColor = Color.WHITE;
		storeStyle.pressedOffsetX = 2;
		storeStyle.pressedOffsetY = -2;
		storeStyle.up = ninePatchDrawable;// skin.getDrawable("storebutt");
/*		
		TextButton slomoIncrease = new TextButton("Slomo", storeStyle);
		
		slomoIncrease.setPosition(0, 0);
		
		//store.add(slomoIncrease);
*/		
		
		TextButton backButt = new TextButton("Back", storeStyle);
		backButt.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
				
				menu.addAction(Actions.moveTo(WIDTH/2 - menu.getWidth()/2, HEIGHT/2 - menu.getHeight()/2,
						0.5f, Interpolation.fade));
				menu.addAction(Actions.fadeIn(0.5f));
				
				
				store.addAction(Actions.moveTo(0 - WIDTH/2 - store.getWidth()/2, 0,
						0.5f, Interpolation.fade));
				store.addAction(Actions.fadeOut(0.5f));
				
				prefs.flush();
				CURRENT_VIEW = 0;

				return true;
			}
			public void touchUp(InputEvent event, float x, float y, int pointer, int button){
				if(DEBUG)System.out.println("Back!");
			}
		});
		
		store.add(backButt).align(Align.right).fill();
		
		
		stage.addActor(store);
		
	}
	
	private void createCredits(){
		//credits.setFillParent(true);
		credits.setSize(WIDTH, HEIGHT - HEIGHT/5);
		credits.setPosition(-WIDTH/2 - credits.getWidth()/2, 0);
		credits.defaults().uniformX().pad(3);
		
		TextButtonStyle storeStyle = new TextButtonStyle();
		storeStyle.font = fontMedium;
		storeStyle.fontColor = Color.WHITE;
		storeStyle.pressedOffsetX = 2;
		storeStyle.pressedOffsetY = -2;
		storeStyle.up = ninePatchDrawable;
		
		Label.LabelStyle labelStyle = new Label.LabelStyle();
		labelStyle.font = fontMedium;
		labelStyle.fontColor = Color.MAROON;
		
		Label.LabelStyle labelStyleSmall = new Label.LabelStyle();
		labelStyleSmall.font = fontSmall;
		labelStyleSmall.fontColor = Color.WHITE;
		
		Label.LabelStyle labelStyleSmallColor = new Label.LabelStyle();
		labelStyleSmallColor.font = fontSmall;
		labelStyleSmallColor.fontColor = Color.GRAY;
		
		Image twitterImage = new Image(atlas.findRegion("twitter-kush"));
		//twitterImage.setSize(WIDTH/17, WIDTH/17* getImageRatio(twitterImage));
		//twitterImage.setPosition( WIDTH - twitterImage.getWidth()*1.5f, twitterImage.getHeight()/4);
		twitterImage.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
								
				MyGame.platform.openTwitter();
				
				return true;
			}
		});	
		//creditGroup.addActor(twitterImage);
		
		credits.add(new Label("Developer", labelStyle)).colspan(3);
		//credits.add(twitterImage).size(WIDTH/22, WIDTH/22).align(Align.left);
		credits.row();
		credits.add(new Label("Kush Sharma", labelStyleSmall)).colspan(3);
		credits.row();		
		credits.add(twitterImage).colspan(3).size(WIDTH/25, WIDTH/25);
		credits.row();
		
		/*
		credits.add(new Label("Testers", labelStyle)).colspan(3);
		credits.row();
		credits.add(new Label("R", labelStyleSmall));

		credits.add(new Label("Y", labelStyleSmall));
		
		credits.add(new Label("V", labelStyleSmall));
		credits.row();
		*/
		
		credits.add(new Label("Music", labelStyle)).colspan(3);
		credits.row();
		credits.add(new Label("Trevor Lentz", labelStyleSmall)).colspan(3);
		credits.row();
		
		/*
		credits.add(new Label("And the libgdx community.", labelStyleSmallColor)).colspan(3);
		credits.row();
		*/
		
		TextButton backButt = new TextButton("Back", storeStyle);
		backButt.addListener(new InputListener(){
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
				
				optionsTable.addAction(Actions.moveTo(0, 0,
						0.5f, Interpolation.fade));
				optionsTable.addAction(Actions.fadeIn(0.5f));
				
				credits.addAction(Actions.moveTo(0 - WIDTH/2 - credits.getWidth()/2, 0,
						0.5f, Interpolation.fade));
				credits.addAction(Actions.fadeIn(0.5f));
								
				CURRENT_VIEW = 1;

				return true;
			}
			public void touchUp(InputEvent event, float x, float y, int pointer, int button){
				if(DEBUG)System.out.println("Back!");
			}
		});
		
		credits.add(backButt).colspan(3).fill();
		
		//creditGroup.addActor(credits);
		stage.addActor(credits);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(.833f, .825f, .794f, 1);
		//Gdx.gl.glClearColor(1/74f, 1/60f, 1/129f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		time += delta;		
		camera.update();
		
		update(delta);
		
		color.r = (float) (MathUtils.cos(time/10)*2);
		color.g = (float) ((MathUtils.sin(time/20)*0.5 + 0.5)*2);    
		color.b = (float) ((MathUtils.sin(time/10)*0.5 + 0.5)*2);
		
		//add this for color beats 
		//color.a = 1.0f - Math.min(0.1f, (float) (MathUtils.sin(time*8)));
		
		
		stage.act(delta);
	
		//Gdx.app.log(""+ Gdx.graphics.getDensity(), "z");


		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		squareFloor.draw(batch, delta);
		//topback.draw(batch);
		//logoback.draw(batch);
		
		//fontLarge.draw(stage.getBatch(), "Play", WIDTH/2 - fontLargeLayout.width/2, HEIGHT/2 + fontLargeLayout.height/2);

		//reset blend function
		//batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		batch.end();
		
		stage.draw();
		
		//notify analytics for changes
		checkForScreenChange();
		
		if (!ScreenFinished && Fade > 0) {
			//will fade in
			Fade = Math.max(Fade - delta / 2.f, 0);
			batch.begin();
			blackFade.setColor(1f, 1f, 1f, Fade);
			blackFade.draw(batch);
			batch.end();
		}

		if (ScreenFinished) {
			//will fade out
			Fade = Math.min(Fade + delta*5 / 2.f, 1);
			batch.begin();
			blackFade.setColor(1f, 1f, 1f, Fade);
			
			blackFade.draw(batch);
			batch.end();
			if (Fade >= 1) {
				game.setScreen(new GameScreen(game, Assets, levelNo));
			}
		}
		
		
		
		//System.out.println("DC"+GLProfiler.drawCalls+", TB"+GLProfiler.textureBindings);
		//Gdx.app.log("FPS", ""+Gdx.graphics.getFramesPerSecond());
		//if(DEBUG) GLProfiler.reset();
	}
	
	private void update(float delta){
		
		//add culling to tables
		if(menu.getX()< WIDTH && menu.getX()+menu.getWidth() > 0)
			menu.setVisible(true);
		else
			menu.setVisible(false);
		
		if(optionsTable.getX()< WIDTH && optionsTable.getX()+optionsTable.getWidth() > 0)
			optionsTable.setVisible(true);
		else
			optionsTable.setVisible(false);
		
		if(store.getX()< WIDTH && store.getX()+store.getWidth() > 0)
			store.setVisible(true);
		else
			store.setVisible(false);
		
		if(credits.getX()< WIDTH && credits.getX()+credits.getWidth() > 0)
			credits.setVisible(true);
		else
			credits.setVisible(false);
		
		if(levelTable.getY() < HEIGHT)
			levelTable.setVisible(true);
		else
			levelTable.setVisible(false);
	}
	
	private void checkForScreenChange(){
		
		if(LAST_VIEW != CURRENT_VIEW){
			LAST_VIEW = CURRENT_VIEW;

			String scr = "Menu";
			switch(CURRENT_VIEW){
				case -1 :{
					scr = "Store";
					break;
				}
				case 0 :{
					scr = "Menu";
					
					break;
				}
				case 1 :{
					scr = "Options";
					
					break;
				}
				case 2:{
					scr ="Credits";
					
				}break;
				case 3:{
					scr = "LevelSelect";
				}break;
			}
			MyGame.platform.setTrackerScreen(scr);
		}
	}

	public boolean isHome(){
		//check if view is on main menu
		if(CURRENT_VIEW == 0)
			return true;
		
		return false;
	}
	
	public void setHome(){
		//reset view to main menu
		
		menu.addAction(Actions.moveTo(WIDTH/2 - menu.getWidth()/2, HEIGHT/2 - menu.getHeight()/2, 0.5f, Interpolation.swingOut));					
		
		optionsTable.addAction(Actions.moveTo(WIDTH, 0, 0.5f, Interpolation.swingOut));

		store.addAction(Actions.moveTo(0 - WIDTH/2 - store.getWidth()/2, 0, 0.5f, Interpolation.swingOut));
		
		credits.addAction(Actions.moveTo(WIDTH*2 - WIDTH/2 - credits.getWidth()/2, 0, 0.5f, Interpolation.swingOut));
		
		levelTable.addAction(Actions.moveTo(0, HEIGHT, 0.5f, Interpolation.swingOut));

		CURRENT_VIEW = 0;
		
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		prefs.flush();		
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		dispose();
		
	}

	@Override
	public void dispose() {
		scoreManager.save(1);

		batch.dispose();
		stage.dispose();	
		
		//unregister listener
		MyGame.platform.unRegisterActionListener(this);
	}

	public static MainMenuScreen getInstance() {
		// TODO Auto-generated method stub
		return _menuScreen;
	}
	
	public OrthographicCamera getCamera(){
		return camera;
	}
	
	@Override
	public void handleEvent(int id, Object data) {
		// TODO Auto-generated method stub
		
	}
	
	class MenuInputProcessor implements InputProcessor{
		
		private Vector3 tempTouchVec = new Vector3();

		@Override
		public boolean keyDown(int keycode) {
			MainMenuScreen mms = MainMenuScreen.getInstance();
			//LevelGenerate level = LevelGenerate.getInstance();
			
			if(GameScreen.DEBUG){
				//camera controls
				if(keycode == Keys.MINUS || keycode == Keys.VOLUME_DOWN)
					mms.getCamera().zoom *= 1.1f;
				if(keycode == Keys.PLUS  || keycode == Keys.VOLUME_UP)
					mms.getCamera().zoom *= 0.9f;
				if(keycode == Keys.SLASH)
					mms.getCamera().position.x--;
				if(keycode == Keys.STAR)
					mms.getCamera().position.x++;
				if(keycode == Keys.PAGE_UP)
					mms.getCamera().position.y+= 10;
				if(keycode == Keys.PAGE_DOWN)
					mms.getCamera().position.y-= 10;
						
				mms.getCamera().update();
			}
			

			return false;	
		}

		@Override
		public boolean keyUp(int keycode) {
			
			if(keycode == Keys.BACK || keycode == Keys.ESCAPE)
			{
				MainMenuScreen mms = MainMenuScreen.getInstance();
				
				if(mms.isHome() == true)			
				{
					//exit game platform wise
					MyGame.platform.exitApp();
					
					//mms.showExitDialog();
				}
				else
					mms.setHome();
				
			}
			
			return false;
		}

		@Override
		public boolean keyTyped(char character) {
			return false;
		}

		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			
			
			return false;
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			//if(keycode == Keys.SPACE)		
				
			return false;
		}

		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			return false;
		}

		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			return false;
		}

		@Override
		public boolean scrolled(int amount) {
			return false;
		}

	}
}
