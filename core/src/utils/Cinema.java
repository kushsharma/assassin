package utils;

import objects.Player;
import Screens.GameScreen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;
import com.softnuke.epic.MyGame;

public class Cinema {

	//will handle first story intro and in between messages
	Stage stage;
	OrthographicCamera camera;
	
	float bWIDTH = MyGame.bWIDTH;
	float bHEIGHT = MyGame.bHEIGHT;
	float WIDTH = MyGame.WIDTH;
	float HEIGHT = MyGame.HEIGHT;
	
	float time = 0;
	float anime_time = 0;
	float anime_delay = 2f;
	int MOV_STAGE = 0;
	boolean CAN_EXECUTE = true;
	
	public static final int MOV_NONE = 0;
	public static final int MOV_INTRO = 1;
	public static final int MOV_INTRO_LEVEL = 2;
	public static final int MOV_TUT1 = 3;
	
	public static final int MOV_TUT_LEVEL_2 = 4;
	public static final int MOV_TUT_LEVEL_3 = 5;
	public static final int MOV_TUT_LEVEL_4 = 6;
	public static final int MOV_TUT_LEVEL_5 = 7;
	public static final int MOV_TUT_LEVEL_6 = 8;
	public static final int MOV_TUT_LEVEL_7 = 9;
	public static final int MOV_TUT_LEVEL_8 = 10;
	public static final int MOV_TUT_LEVEL_9 = 11;
	public static final int MOV_TUT_LEVEL_10 = 12;
	public static final int MOV_TUT_LEVEL_11 = 13;
	public static final int MOV_TUT_LEVEL_12 = 14;
	public static final int MOV_TUT_LEVEL_13 = 15;
	public static final int MOV_TUT_LEVEL_14 = 16;
	public static final int MOV_TUT_LEVEL_15 = 17;
	
	public static final int MOV_TUT_LEVEL_420 = 420;
	public static final int MOV_TUT_FIRST_EVOL = 9211;
	public static final int MOV_TUT_SECOND_EVOL = 9212;
	
	int CINEMA_TYPE = 0;
	
	Image intro1, introAlien, introPlayerSleep, introPlayerFly, introStar, introAlienRays, introMillFan, introHorlicks;
	Sprite introBoss;
	Animation bossAnime;
	
	ParticleEffect introSmokeEffect;
	Texture intro1T = null;
	
	BitmapFont fontTiny;
	BitmapFont fontMedium;
	Skin skin;
	TextButton dialogText, toolTipText;
	TextureAtlas atlas = GameScreen.getInstance().getAssetLord().manager.get(AssetLord.game_atlas, TextureAtlas.class);
	
	public Cinema(Stage stg, OrthographicCamera cam){
		stage = stg;
		camera = cam;
	
		init();
	}
	
	private void init(){
		fontMedium = GameScreen.getInstance().getAssetLord().manager.get(AssetLord.small_font, BitmapFont.class);
		fontTiny = GameScreen.getInstance().getAssetLord().manager.get(AssetLord.tiny_font, BitmapFont.class);
		
		skin = new Skin();
		skin.addRegions(atlas);
				
		TextButtonStyle tStyle = new TextButtonStyle();
		tStyle.font = fontTiny;
		tStyle.fontColor = Color.WHITE;
		tStyle.up = skin.getDrawable("black");
		
		toolTipText = new TextButton("", tStyle);
		toolTipText.setSize(WIDTH*0.2f, HEIGHT/16);
		toolTipText.align(Align.center);		
		toolTipText.setPosition(-WIDTH, HEIGHT/2 - toolTipText.getHeight()/2);
		toolTipText.setVisible(false);
		toolTipText.setColor(1f,1f,1f, 0.8f);
		toolTipText.addListener(new InputListener(){
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				
				hideNotify();
				
		 		return true;
		 	}

		});
		stage.addActor(toolTipText);

		if(LevelGenerate.CURRENT_LEVEL < 2){
			//only load if tutorial on
			if(GameScreen.getInstance().getScoreManager().TUTORIAL_LEVEL == 0)
			{
				//loadIntroMovie();
			}
			
			loadIntroLevel();			
		}
		
		tStyle.font = fontMedium;
		dialogText = new TextButton("",tStyle);
		dialogText.pad(HEIGHT/20);
		dialogText.setSize(WIDTH, HEIGHT/10);
		dialogText.align(Align.center);
		dialogText.addListener(new InputListener(){
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				
				MOV_STAGE ++;
				anime_time = 0;
				CAN_EXECUTE = true;
				
		 		return true;
		 	}

		});
		
		dialogText.setPosition(WIDTH/2 - dialogText.getWidth()/2, HEIGHT*1.5f);
		dialogText.setVisible(false);
		dialogText.setColor(1f, 1f, 1f, 0.8f);		
		//add dialogue after score to avoide overlap
		
	}
		
	private void loadIntroLevel(){
		
		TextureRegion[] bossSheet = new TextureRegion[2];
		bossSheet[0] = atlas.findRegion("elder");
		bossSheet[1] = atlas.findRegion("elder");
		bossAnime = new Animation(0.5f, bossSheet);
		bossAnime.setPlayMode(PlayMode.LOOP);
		
//		introScientist = new Sprite(profSheet[0]);
//		introScientist.setPosition(camera.position.x + bWIDTH/5, 4);
//		introScientist.setSize(0.7f, 0.7f * introScientist.getHeight()/introScientist.getWidth());
//		introScientist.setFlip(true, false);
				
	}

	public void start(int t){
		MOV_STAGE = 0;
		CINEMA_TYPE = t;
		
		switch(CINEMA_TYPE){
			case MOV_INTRO:{
				anime_delay = 2f;
				LevelGenerate.getInstance().playMenuMusic();
				
				introSmokeEffect.start();
				intro1.setVisible(true);
				introPlayerSleep.setVisible(true);
				introMillFan.setVisible(true);
				
				GameScreen.getInstance().hideControls();
				
				break;
			}
			case MOV_INTRO_LEVEL:{
				dialogText.clearActions();
				LevelGenerate.getInstance().playMenuMusic();
				
				//no tutorial for you
				if(GameScreen.getInstance().getScoreManager().TUTORIAL_LEVEL != 0){
					GameScreen.CURRENT_STATE = GameState.RUNNING;
					GameScreen.getInstance().showControls();					
				}
				
					anime_delay = 6f;
				break;
			}
			case MOV_TUT_LEVEL_3:{
				dialogText.clearActions();
				anime_delay = 8f;
			break;
			}
			case MOV_TUT_LEVEL_4:{
				dialogText.clearActions();
				anime_delay = 8f;
			break;
			}
			case MOV_TUT_LEVEL_5:{
				dialogText.clearActions();
				anime_delay = 6f;				
			break;
			}
			case MOV_TUT_LEVEL_6:{
				dialogText.clearActions();
				anime_delay = 6f;				
			break;
			}
			case MOV_TUT_LEVEL_7:{
				dialogText.clearActions();
				anime_delay = 8f;				
			break;
			}
			case MOV_TUT_LEVEL_8:{
				dialogText.clearActions();
				anime_delay = 8f;				
			break;
			}
			case MOV_TUT_LEVEL_9:{
				dialogText.clearActions();
				anime_delay = 8f;				
			break;
			}
			case MOV_TUT_LEVEL_10:{
				dialogText.clearActions();
				anime_delay = 6f;				
			break;
			}
			case MOV_TUT_LEVEL_11:{
				dialogText.clearActions();
				anime_delay = 10f;				
			break;
			}
			case MOV_TUT_LEVEL_12:{
				dialogText.clearActions();
				anime_delay = 6f;				
			break;
			}
			case MOV_TUT_LEVEL_13:{
				dialogText.clearActions();
				anime_delay = 8f;				
			break;
			}
			case MOV_TUT_LEVEL_14:{
				dialogText.clearActions();
				anime_delay = 5f;				
			break;
			}
			case MOV_TUT_LEVEL_15:{
				dialogText.clearActions();
				anime_delay = 5f;				
			break;
			}
			
			
			
			case MOV_TUT_LEVEL_420:{
				anime_delay = 5f;				
			break;
			}
			case MOV_TUT_FIRST_EVOL:{
				hideDialogue();
				
				Player.getInstance().enableFlying(1);
				Player.getInstance().makeItGlow();
				LevelGenerate.getInstance().playEpicLevelSound();
				
				//anime_delay = 5f;				
			break;
			}
			case MOV_TUT_SECOND_EVOL:{
				hideDialogue();
				
				Player.getInstance().enableFlying(1);
				Player.getInstance().makeItGlow();
				LevelGenerate.getInstance().playEpicLevelSound();

				//anime_delay = 5f;				
			break;
			}
		}
	}
	
	public void render(SpriteBatch batch){
		switch(CINEMA_TYPE){
			case MOV_INTRO_LEVEL:{
				//introLevelPods.draw(batch);
				//introScientist.draw(batch);
				
				break;
			}			
		}
	}
	
	public void renderUI(SpriteBatch batch){
		switch(CINEMA_TYPE){
			case MOV_INTRO:{
				introSmokeEffect.draw(batch);
			break;
			}
			
		}
	}
	
	public void update(float delta){
		time += delta;
		
		if(CINEMA_TYPE != MOV_NONE){
			anime_time += delta;
			
			if(anime_time > anime_delay)
			{
				anime_time = 0;
				MOV_STAGE++;
				CAN_EXECUTE = true;
			}
		}
		
		//needs continuous  execution
		switch(CINEMA_TYPE){
			case MOV_INTRO:{
				introSmokeEffect.update(delta);
				introMillFan.setRotation(360 - (time*50)%360);

				break;
			}
			case MOV_INTRO_LEVEL:{
//				introScientist.setRegion(profkAnime.getKeyFrame(time));
//				
//				if(Player.getInstance().getPosition().x > introScientist.getX())
//					introScientist.setFlip(false, false);
//				else
//					introScientist.setFlip(true, false);
			}
		}
		
		//one time execution
		if(CAN_EXECUTE)
		{
			switch(CINEMA_TYPE){
				case MOV_INTRO:{
					//introSmokeEffect.update(delta);
					
					//handleMovIntro();
					
					break;
				}
				case MOV_INTRO_LEVEL:{
					
					//handleIntroLevel();
					
					break;
				}
				case MOV_TUT_LEVEL_3:{
					switch(MOV_STAGE){
					case 1:{
						showDialogue("You have been assigned to kill lord Synth.");
					}break;
					case 2:{
						showDialogue("Find him and make him pay for his sins.");
					}break;
					case 3:{
						showDialogue("Don't forget, you are not alone in this fight.");
					}break;
					case 4:{
						hideDialogue();
						
						MOV_STAGE = 0;
						CINEMA_TYPE = MOV_NONE;
					}		
					}
					
					break;
				}
				case MOV_TUT_LEVEL_4:{
					switch(MOV_STAGE){
					case 1:{
						showDialogue("Every time you die\nyou will get a chance to relive once more.");
					}break;
					case 2:{
						showDialogue("You will behave exactly before\nbut the world may not.");
						showNotify("Try holding fire button");
					}break;
					case 3:{
						showDialogue("You are the choosen one!");
					}break;
					case 4:{
						hideDialogue();
						hideNotify();
						
						MOV_STAGE = 0;
						CINEMA_TYPE = MOV_NONE;
					}break;
					}
					break;
				}
				case MOV_TUT_LEVEL_5:{
					switch(MOV_STAGE){
					case 1:{
						showDialogue("Don't be scared, I am your inner voice.");
						break;
					}
					case 2:{
						showDialogue("Have you tried jumping on there heads?\nYou are safe there.");
						break;
					}
					case 3:{
						showDialogue("Ask you master to rate this game well.\nThis is the last stage!");
						break;
					}
					case 4:{
						hideDialogue();
						
						MOV_STAGE = 0;
						CINEMA_TYPE = MOV_NONE;
						break;
					}		
					}
					break;
				}
				case MOV_TUT_LEVEL_6:{
					switch(MOV_STAGE){
					case 1:{
						showDialogue("Ask you master to rate this game well.\nThis is the last stage!");
						break;
					}
					case 2:{
						hideDialogue();
						
						MOV_STAGE = 0;
						CINEMA_TYPE = MOV_NONE;
						break;
					}	
					}
					break;
				}
				case MOV_TUT_LEVEL_7:{
					switch(MOV_STAGE){
					case 1:{
						showDialogue(".");
						break;
					}
					case 2:{
						showDialogue("!");
						break;
					}
					case 3:{
						hideDialogue();
						
						MOV_STAGE = 0;
						CINEMA_TYPE = MOV_NONE;
						break;
					}		
					}
					break;
				}
				case MOV_TUT_LEVEL_8:{
					switch(MOV_STAGE){
					case 1:{
						showDialogue("I don't know why but i just love\nseeing you try so hard...");
						break;
					}
					case 2:{
						showDialogue("By the way, here is a piece of advice...");
					}break;
					case 3:{
						showDialogue("Once you get out of here, buy some clothes.\nClowns look better than you.");
					}break;
					case 4:{
						hideDialogue();
						
						MOV_STAGE = 0;
						CINEMA_TYPE = MOV_NONE;
						break;
					}		
					}
					break;
				}
				case MOV_TUT_LEVEL_9:{
					switch(MOV_STAGE){
					case 1:{
						showDialogue("Some levels may need you to activate\nthe switch to powerup portal...");
						break;
					}
					case 2:{
						showDialogue("Your gun has recoil!\nUse it smart for speed.");
						showNotify("Fire Gun for speed");
						break;
					}
					case 3:{
						hideDialogue();
						hideNotify();
						
						MOV_STAGE = 0;
						CINEMA_TYPE = MOV_NONE;
						break;
					}		
					}
					break;
				}
				case MOV_TUT_LEVEL_10:{
					switch(MOV_STAGE){
					case 1:{
						showDialogue("You may find here enough milk to transform\neven more powerful...");
						break;
					}
					case 2:{
						showDialogue("But remember to use that power wisely.");
						break;
					}
					case 3:{
						hideDialogue();
						
						MOV_STAGE = 0;
						CINEMA_TYPE = MOV_NONE;
						break;
					}		
					}
					break;
				}
				case MOV_TUT_LEVEL_11:{
					switch(MOV_STAGE){
					case 1:{
						showDialogue("You are really enjoying the power, don't you?");
						break;
					}
					case 2:{
						hideDialogue();
						
						MOV_STAGE = 0;
						CINEMA_TYPE = MOV_NONE;
						break;
					}		
					}
					break;
				}
				case MOV_TUT_LEVEL_12:{
					switch(MOV_STAGE){
					case 1:{
						showDialogue("Gravity is a great force to master...");
						break;
					}
					case 2:{
						showDialogue("I realized this by falling from stairs,\ntrust me worth it! Ouch...");
						break;
					}
					case 3:{
						showDialogue("I hope you enjoyed talking to me,\nask your master to rate this world well.");
						break;
					}
					case 4:{
						hideDialogue();
						
						MOV_STAGE = 0;
						CINEMA_TYPE = MOV_NONE;
						break;
					}		
					}
					break;
				}
				case MOV_TUT_LEVEL_13:{
					switch(MOV_STAGE){
					case 1:{
						showDialogue("Finally the boss faceoff!\nLets see how well you have mastered Horlicks.");//\nJust don't cry like a baby when it appears.");
						break;
					}
					case 2:{
						showDialogue("Unknown: You filthy human.\nIt's going to be fun killing you...");
						break;
					}
					case 3:{
						showDialogue("Unknown: Hahahahahah!!! *cough* Haha *cough*...");
						break;
					}
					case 4:{
						showDialogue("Unknown: I am getting too old for these things.");
						break;
					}
					case 5:{
						hideDialogue();
						
						MOV_STAGE = 0;
						CINEMA_TYPE = MOV_NONE;
						break;
					}		
					}
					break;
				}
				case MOV_TUT_LEVEL_420:{
					switch(MOV_STAGE){
					case 1:{
						showDialogue("How could you find this private area you stupid kid!\nIt's not ready yet.");
						break;
					}
					case 2:{
						hideDialogue();
						
						MOV_STAGE = 0;
						CINEMA_TYPE = MOV_NONE;
						break;
					}		
					}
					break;
				}

				case MOV_TUT_FIRST_EVOL:{
					switch(MOV_STAGE){
					case 1:{
						showNotify("Evolved to Super");
						
						break;
					}
					case 2:{
						showNotify("Gun Enabled");
						
						break;
					}
					case 3:{
						hideNotify();
						hideDialogue();
						
						MOV_STAGE = 0;
						CINEMA_TYPE = MOV_NONE;
						break;
					}		
					}
				break;				
				}
				case MOV_TUT_SECOND_EVOL:{
					switch(MOV_STAGE){
					case 1:{
						showNotify("Evolved to Mega");
						
						break;
					}
					case 2:{
						showDialogue("Try holding fire key.");
						showNotify("Dope Enabled");
						
						break;
					}
					case 3:{
						hideNotify();
						hideDialogue();
						
						MOV_STAGE = 0;
						CINEMA_TYPE = MOV_NONE;
						break;
					}		
					}
				break;
				}
			}
			
			
			//executed once already
			CAN_EXECUTE = false;
			
		}
	}
	
	private void handleIntroLevel(){		
		
		switch(MOV_STAGE){
		case 1:{
			showDialogue("You: What just happened?");			
		}break;
		case 2:{
			showDialogue("Calm down! You were abducted by aliens...");
		}break;
		case 3:{
			showDialogue("Well, it's normal to freak out actually.\n But before that...");
		}break;
		case 4:{
			showDialogue("I don't have much time,\n so listen closely...");
		}break;
		case 5:{
			showDialogue("The race that rules here is similar to humans,\nexcept that they are evil and love milk too much...");
		}break;
		case 6:{
			showDialogue("You have to fight back to survive.\nLuckily I have been working on a compound...");
		}break;
		case 7:{
			showDialogue("This compound can enhance human ability by \ngenetically modifying ordinary milk...");
		}break;
		case 8:{
			showDialogue("And this planet has more than enough.\nHere take this, I named it 'Horlicks'...");
			showNotify("Horlicks Acquired");			
			
		}break;
		case 9:{
			showDialogue("Just remember, drink as much milk as you can!\n You should go now.");
			hideNotify();

			
			if(GameScreen.getInstance().getScoreManager().TUTORIAL_LEVEL == 0)
				GameScreen.getInstance().showControls();
		}break;
		case 10:{
			hideDialogue();
			
		}break;
		}
	}
	
	private void handleMovIntro() {
		switch(MOV_STAGE){
		case 1:{
			showDialogue("Honey! Dinner is ready, come inside.");
			
			introStar.setVisible(true);
		}break;
		case 2:{
			introStar.setVisible(false);
			introAlien.setVisible(true);
			introAlien.addAction(Actions.moveTo(WIDTH - WIDTH*0.3f, HEIGHT/9 + introAlienRays.getHeight(), 1f, Interpolation.sineOut));
		}break;
		case 3:{
			hideDialogue();
			
		}break;
		case 4:{
			introAlien.setPosition(WIDTH - WIDTH*0.3f, HEIGHT/9 + introAlienRays.getHeight());
			introAlien.clearActions();
			introAlienRays.setVisible(true);
		}break;
		case 5:{
			introPlayerFly.setVisible(true);
			introPlayerSleep.setVisible(false);
		}break;
		case 6:{
			showDialogue("Honey??");

			introPlayerFly.setVisible(false);
		}break;
		case 7:{

			introAlienRays.setVisible(false);
			introAlien.addAction(Actions.moveTo(WIDTH - WIDTH*0.3f, 2*HEIGHT, 2f, Interpolation.sineOut));
		}break;
		case 8:{
			hideDialogue();

			introAlien.setVisible(false);
			introStar.setVisible(true);
		}break;
		case 9:{			
			intro1.addAction(Actions.sequence(Actions.after(Actions.fadeOut(0.5f)), Actions.run(new Runnable(){
				@Override
				public void run() {
					intro1.setVisible(false);					
				}
			})));
			
			introAlien.setVisible(false);
			introAlienRays.setVisible(false);
			introStar.setVisible(false);
			introSmokeEffect.allowCompletion();
			introMillFan.setVisible(false);
			
			GameScreen.CURRENT_STATE = GameState.RUNNING;
			
			if(GameScreen.getInstance().getScoreManager().TUTORIAL_LEVEL != 0)
				GameScreen.getInstance().showControls();

			
			MOV_STAGE = 0;
			CINEMA_TYPE = MOV_NONE;
			
			start(MOV_INTRO_LEVEL);
		}break;
		case 10:{
			
		}break;
	}
		
	}

	private void startLevel1() {

	}
	
	private void showDialogue(String text){
		dialogText.setText(text);
		dialogText.setVisible(true);
		dialogText.addAction(Actions.moveTo(dialogText.getX(), HEIGHT - dialogText.getHeight(), 1f));
	}
	
	private void hideDialogue(){
		dialogText.addAction(Actions.sequence(Actions.after(Actions.moveTo(dialogText.getX(), HEIGHT*1.5f, 1f)),
				Actions.run( new Runnable() {
					public void run() {
						dialogText.setVisible(false);
					}
				})
			));
			
		//dialogText.addAction(Actions.fadeOut(1f));
		
		
	}

	private void showNotify(String text){
		toolTipText.setText(text);
		toolTipText.setVisible(true);
		toolTipText.addAction(Actions.moveTo(0, toolTipText.getY(), 1f));
		
		float toolCenter = toolTipText.getWidth()/2;
		if(LevelGenerate.CURRENT_LEVEL<2){
			//TODO:
			//introHorlicks.setVisible(true);
			introHorlicks.addAction(Actions.moveTo(toolCenter - introHorlicks.getWidth()/2, introHorlicks.getY(), 1f));
		}
	}
	
	private void hideNotify(){
		toolTipText.addAction(Actions.moveTo(-WIDTH, toolTipText.getY(), 5f));
		toolTipText.addAction(Actions.fadeOut(0.5f));

		if(LevelGenerate.CURRENT_LEVEL<2){
			introHorlicks.addAction(Actions.moveTo(-WIDTH, introHorlicks.getY(), 5f));
			introHorlicks.addAction(Actions.fadeOut(0.5f));

			//introHorlicks.setVisible(false);
		}
	}
	
	public void dispose(){
		if(intro1!=null)
			intro1T.dispose();
		
	}

	public void clearCinema() {
		dialogText.setVisible(false);
		CINEMA_TYPE = MOV_NONE;
		hideDialogue();
		hideNotify();
	}

	/** notification if there is anything that needs to be done in this new level**/
	public void levelUpdate(int level) {
		switch(level){
		case 1:{
			start(MOV_TUT_LEVEL_3);
			break;
		}
		case 2:{
			start(MOV_TUT_LEVEL_4);
			break;
		}
		case 3:{
			start(MOV_TUT_LEVEL_5);
			break;
		}
		case 4:{
			start(MOV_TUT_LEVEL_6);
			break;
		}
		case 7:{
			start(MOV_TUT_LEVEL_7);
			break;
		}
		case 8:{
			start(MOV_TUT_LEVEL_8);
			break;
		}
		case 9:{
			start(MOV_TUT_LEVEL_9);
			break;
		}
		case 10:{
			start(MOV_TUT_LEVEL_10);
			break;
		}
		case 11:{
			start(MOV_TUT_LEVEL_11);
			break;
		}
		case 12:{
			start(MOV_TUT_LEVEL_12);
			break;
		}
		case 13:{
			start(MOV_TUT_LEVEL_13);
			break;
		}
		case 14:{
			start(MOV_TUT_LEVEL_14);
			break;
		}
		case 15:{
			start(MOV_TUT_LEVEL_15);
			break;
		}
		
		case 420:{
			start(MOV_TUT_LEVEL_420);
			break;
		}
		}
	}

	/** to set its view priority over score text **/
	public void addDialogue() {
		stage.addActor(dialogText);
	}
	
}
