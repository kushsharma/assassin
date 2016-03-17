package objects;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.Array;
import com.softnuke.epic.MyGame;

import Screens.GameScreen;
import utils.AssetLord;
import utils.LevelGenerate;
import utils.MyInputProcessor;
import utils.MyInputProcessor.CONTROL;

public class Player {

	public static Player _player = null;
	private boolean IMMORTAL = false;
	private boolean PLAYER_DRAW = true;
	
	private float height = 1.0f * 0.9f, width = 0.5f * 0.9f;
	private Vector2 position = new Vector2();
	Vector2 startPos = new Vector2();
	public static float Speed = 4.6f * 0.8f; // 4.6f
	public static float Jump = 1.40f;
	private float gravityScale = 0f;
	
	public boolean GLOWING = false;
	private boolean FLYING = false; // disable player gravity effect
	private float flightTime = 0f;
	private final float FLYING_TIME = 0.5f;
	
	private float fire_hold = 0;
	public static final float BULLET_HOLD_TIME = 1.5f; //time to hold fire button for bullets
	public static boolean FIRING = false;
	public static final int TOTAL_BULLETS = 4;
	private int bullets_fired = 0;
	private float last_bullet_time = 0;
	
	
	private boolean visible = true;
	public boolean Revived = false;
	public int JUMP_DAMAGE = 2;
	public static int WEAPON_DAMAGE = 5;
	public static int DASH_DAMAGE = 20;

	//player dash
	public static final float DASH_FORCE = Speed; //dash force
	public static final float DASH_DURATION = 0.5f; //time it takes for dash
	public static final float DASH_BUTTON_SPEED = 0.5f; // time it needs by player to make dash
	public boolean DASHING = false; //if its dashing right now
	public int DASH_BUTTON_COUNT = 0;
	public float dashing_time = 0; //for how long we are dashing
	private float dashButtonCooler = DASH_BUTTON_SPEED; // time last time dash button is pressed [left/right button]
	private boolean lastDashDirectionLeft = false; //direction of last dash
	
	//player swing combos
	public boolean SWINGING = false; //if player is swinging right now
	public boolean SWING_COMBO = false; //if player swinged twice
	public boolean SWING_STORED = false; //player is swinging and haven't hurt anyone till now
	private float swing_time = 0;
	public static final float TOTAL_SWING_TIME = 0.5f;	//time in swing motion
	private Array<Enemy> enemyNearPool = new Array<Enemy>();

	private boolean DEAD = false; //reset game
	public boolean CONTROLS = true; //player can/can't be controlled
	public boolean GOT_HIT = false;
	public boolean TELEPORTING_OUT = false; //going out of stage
	public boolean TELEPORTING_IN = true; //coming in stage
	public static final float WIN_CAM = 0.7f;	//time before teleport
	public static final float DEATH_CAM = 0.4f;	//time before final death
	
	public boolean CAN_JUMP = true;
	public boolean CAN_FIRE = true;
	
	public boolean LEFT_DIRECTION = false;
	
	GameScreen gameScreen = GameScreen.getInstance();
		
	Body body;
	//Body weapon;
	//Joint weaponJoint;
	//RevoluteJointDef jointDef;
	World world;
	PolygonShape shape;
	BodyDef bodyDef;
	Fixture bodyFixture, sensorFixture, weaponFixture;
	FixtureDef weaponDef;
	
	TextureRegion playerTexR;
	Texture playerTex;
	Sprite playerSprite, hitSprite;
	Sprite glow;
	ParticleEffect jumpParticle;
	
	private Vector2 WorldGravityNegative = new Vector2(GameScreen.WorldGravity.x *-1, 0);
	
	boolean ASIDE = true;//player on Wall A or B, true means A 
	
	float bHEIGHT = MyGame.bHEIGHT;
	float bWIDTH = MyGame.bWIDTH;
	
	float time = 0f;
	float lastJumpTime = 0;
	float deathClock = 0;
	float winClock = 0;
	Animation idleAnime, moveAnime, jumpAnime, swingAnime, swingComboAnime, fireIdleAnime, fireMoveAnime, megaIdleAnime, megaMoveAnime, hitAnime, teleportOut, teleportIn;

	AssetLord Assets = GameScreen.getInstance().getAssetLord();
	TextureAtlas gameAtlas;
	public HashMap<CONTROL, Boolean> pKeys = null;
	
	public Player(World w){
		_player = this;
		world = w;		
		
		position.x = startPos.x = bWIDTH/4 ;
		position.y = startPos.y = bHEIGHT/2  - height/2;
		init();
	}
	
	public void init(){
		bodyDef =  new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		
		bodyDef.position.x = position.x;
		bodyDef.position.y = position.y;
		
		shape = new PolygonShape();
		shape.setAsBox((width*0.75f)/2, (height*0.9f)/2); // body
				
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density =  0.6f;
		fixtureDef.friction = 0.5f; // experimental, check if its works fine
		fixtureDef.restitution = 0;		
		fixtureDef.isSensor = false;
		
		fixtureDef.filter.categoryBits = LevelGenerate.CATEGORY_PLAYER;
		fixtureDef.filter.maskBits = (short) (LevelGenerate.CATEGORY_WALL | LevelGenerate.CATEGORY_POWERUP | LevelGenerate.CATEGORY_BADBOY);
		
		CircleShape cs = new CircleShape();
		cs.setRadius((width*0.7f)/2);
		cs.setPosition(new Vector2(0, -height/3)); // legs

		FixtureDef sensorDef = new FixtureDef();
		sensorDef.shape = cs;
		sensorDef.isSensor = true;
		sensorDef.filter.categoryBits = LevelGenerate.CATEGORY_PLAYER;
		sensorDef.filter.maskBits = (short) (LevelGenerate.CATEGORY_WALL | LevelGenerate.CATEGORY_BADBOY);
		
		PolygonShape handShape = new PolygonShape(); //side bars
        float[] vertices = new float[8];
        vertices[0] = -(width*0.75f)/2f;
        vertices[1] = -(height*0.85f)/2f;
        vertices[2] = -(width*0.75f)/2f;
        vertices[3] = (height*0.85f)/2f;
        vertices[4] = (width*0.75f)/2f + 0.01f;
        vertices[5] = (height*0.75f)/2f;
        vertices[6] = (width*0.75f)/2f + 0.01f;
        vertices[7] = -(height*0.75f)/2f;
		handShape.set(vertices);
		//handShape.setAsBox(0.01f, (height*0.85f)/2, new Vector2((width*0.75f)/2, 0), 0);
		
		FixtureDef sideBodyDef = new FixtureDef();
		sideBodyDef.isSensor = false;
		sideBodyDef.shape = handShape;
		sideBodyDef.friction = 0;
		sideBodyDef.density  = 0.02f;
		sideBodyDef.restitution = 0;
		sideBodyDef.filter.categoryBits = LevelGenerate.CATEGORY_PLAYER;
		sideBodyDef.filter.maskBits = (short) (LevelGenerate.CATEGORY_WALL);
		
		body = world.createBody(bodyDef);
		bodyFixture = body.createFixture(fixtureDef);
		sensorFixture = body.createFixture(sensorDef);
		body.createFixture(sideBodyDef);
		
		handShape.setAsBox(0.01f, (height*0.85f)/2, new Vector2(-(width*0.75f)/2, 0), 0);
		sideBodyDef.shape = handShape;
		body.createFixture(sideBodyDef);
		
		/*
		//sword
		FixtureDef weaponDef = new FixtureDef();
		weaponDef.density = 0.1f;
		weaponDef.isSensor = true;
		weaponDef.filter.categoryBits = LevelGenerate.CATEGORY_BULLET;
		weaponDef.filter.maskBits = LevelGenerate.CATEGORY_BADBOY;
		
		handShape.setAsBox(width*1.2f, width/7);
		weaponDef.shape = handShape;
		
		weapon = world.createBody(bodyDef);
		weapon.createFixture(weaponDef);
		
		jointDef = new RevoluteJointDef();
		jointDef.bodyA = body;
		jointDef.bodyB = weapon;
		jointDef.localAnchorA.set(0, 0);
		jointDef.localAnchorB.set(width/2, 0);
		jointDef.enableLimit = true;
		jointDef.lowerAngle = 110 * MathUtils.degreesToRadians;
		jointDef.upperAngle = 270 * MathUtils.degreesToRadians;
		
		jointDef.enableMotor = true;
		jointDef.maxMotorTorque = 0.1f;
		jointDef.motorSpeed = 5;
		
		weaponJoint = world.createJoint(jointDef);
		
		weapon.setUserData("weapon");
		weapon.setAngularDamping(0);
		weapon.setLinearDamping(0);
		weapon.setGravityScale(0);
		
		*/
		
		weaponDef = new FixtureDef();
		weaponDef.isSensor = true;
		weaponDef.filter.categoryBits = LevelGenerate.CATEGORY_BULLET;
		weaponDef.filter.maskBits = LevelGenerate.CATEGORY_BADBOY;
		
		cs.setRadius(width*1.5f);
		cs.setPosition(new Vector2(0, 0));
		weaponDef.shape = cs;		
		weaponFixture = body.createFixture(weaponDef);
		
		
		body.setLinearVelocity(0, 0);
		body.setFixedRotation(true);
		body.setUserData("player");
		gravityScale = body.getGravityScale();
		
		handShape.dispose();
		cs.dispose();
		shape.dispose();
		
		
		getSkinTexture();
	}
	
	//set the starting point of player according to entry portal
	public void setStartingPoint(float x, float y){
		startPos.set(x, y);
		position.set(x, y);
		body.setTransform(x, y, 0);
	}
	
	private void getSkinTexture(){
		
		//textures
		gameAtlas = Assets.manager.get(AssetLord.game_atlas, TextureAtlas.class);

		//Texture tex = new Texture("level/player-idle-1.png");
		playerSprite = new Sprite(gameAtlas.findRegion("player-idle-1"));
		//playerSprite.setSize(height*1.2f * playerSprite.getWidth()/playerSprite.getHeight(), height*1.2f);
		playerSprite.setSize(width*3f, width * 3f * playerSprite.getHeight()/playerSprite.getWidth());
		playerSprite.setOrigin(playerSprite.getWidth()/2, playerSprite.getHeight()/2);
		
		TextureRegion idleSheet[] = new TextureRegion[2];
		
		idleSheet[0] = gameAtlas.findRegion("player-idle-1");
		idleSheet[1] = gameAtlas.findRegion("player-idle-2");
		
		idleAnime = new Animation(0.2f, idleSheet);
		idleAnime.setPlayMode(PlayMode.LOOP);
		
		TextureRegion playerMoveSheet[] = new TextureRegion[8];		
		playerMoveSheet[0] = gameAtlas.findRegion("player-walk-1");
		playerMoveSheet[1] = gameAtlas.findRegion("player-walk-2");
		playerMoveSheet[2] = gameAtlas.findRegion("player-walk-3");
		playerMoveSheet[3] = gameAtlas.findRegion("player-walk-4");
		playerMoveSheet[4] = gameAtlas.findRegion("player-walk-5");
		playerMoveSheet[5] = gameAtlas.findRegion("player-walk-6");
		playerMoveSheet[6] = gameAtlas.findRegion("player-walk-7");
		playerMoveSheet[7] = gameAtlas.findRegion("player-walk-8");
		
		moveAnime = new Animation(0.05f, playerMoveSheet);
		moveAnime.setPlayMode(PlayMode.LOOP);		
		
		
		TextureRegion playerSwingSheet[] = new TextureRegion[7];		
		playerSwingSheet[0] = gameAtlas.findRegion("player-swing-1");
		playerSwingSheet[1] = gameAtlas.findRegion("player-swing-2");
		playerSwingSheet[2] = gameAtlas.findRegion("player-swing-3");
		playerSwingSheet[3] = gameAtlas.findRegion("player-swing-4");
		playerSwingSheet[4] = gameAtlas.findRegion("player-swing-5");
		playerSwingSheet[5] = gameAtlas.findRegion("player-swing-6");
		playerSwingSheet[6] = gameAtlas.findRegion("player-swing-6");
		
		swingAnime = new Animation(0.05f, playerSwingSheet);
		swingAnime.setPlayMode(PlayMode.NORMAL);
		
		
		TextureRegion playerSwingComboSheet[] = new TextureRegion[4];		
		playerSwingComboSheet[0] = gameAtlas.findRegion("player-swing-7");
		playerSwingComboSheet[1] = gameAtlas.findRegion("player-swing-8");
		playerSwingComboSheet[2] = gameAtlas.findRegion("player-swing-9");
		playerSwingComboSheet[3] = gameAtlas.findRegion("player-swing-10");
		//playerSwingComboSheet[4] = gameAtlas.findRegion("player-swing-10");
		swingComboAnime = new Animation(0.1f, playerSwingComboSheet);
		swingComboAnime.setPlayMode(PlayMode.NORMAL);
		
		
		TextureRegion playerHitSheet[] = new TextureRegion[4];
		playerHitSheet[0] = gameAtlas.findRegion("hit-anime-player-1");//new TextureRegion(tHit1);
		playerHitSheet[1] = gameAtlas.findRegion("hit-anime-2");//new TextureRegion(tHit2);
		playerHitSheet[2] = gameAtlas.findRegion("hit-anime-3");//new TextureRegion(tHit3);
		playerHitSheet[3] = gameAtlas.findRegion("hit-anime-4");//new TextureRegion(tHit4);
		
		hitAnime = new Animation(0.1f, playerHitSheet);
		hitAnime.setPlayMode(PlayMode.NORMAL);
		
		
		glow = new Sprite(gameAtlas.findRegion("playerglow"));
		glow.setSize(width*8, width*8 * glow.getHeight()/glow.getWidth());
		glow.setPosition(body.getPosition().x - glow.getWidth()/2, body.getPosition().y - glow.getHeight()/2);
		glow.setColor(1f,1f,1f, 0.5f);
		glow.setOrigin(glow.getWidth()/2, glow.getHeight()/2);
		
		
		hitSprite = new Sprite();
		hitSprite.setSize(width*3f, height * 1.2f); //.setSize(width*1.5f, width*1.5f);
		hitSprite.setOrigin(hitSprite.getWidth()/2, hitSprite.getHeight()/2);
		
		//particle
		jumpParticle = gameScreen.getAssetLord().manager.get(AssetLord.player_jump_particle,ParticleEffect.class);
		jumpParticle.setEmittersCleanUpBlendFunction(false);
	}
	
	public void render(ShapeRenderer canvas){
		//update(delta);
		if(!GameScreen.DEBUG) return;
		
        canvas.setColor(0.1f, 0.1f, 0.1f, 1.0f);
		canvas.rect(position.x-width/2, position.y-height/2, width/2 , height/2, width, height, 1, 1, body.getAngle());
		
	}
	
	public void render(SpriteBatch batch){
		if(!visible) return;
		
		playerSprite.setPosition(position.x - playerSprite.getWidth()/2, position.y - playerSprite.getHeight()*0.32f);

		//make em flyy!!!
		if(FLYING){
			glow.setPosition(body.getPosition().x - glow.getWidth()/2, body.getPosition().y - glow.getHeight()/2);
			glow.draw(batch);			
		}
		
		if(pKeys != null){
			if((pKeys.get(MyInputProcessor.CONTROL.LEFT) == true || pKeys.get(MyInputProcessor.CONTROL.RIGHT) == true) && CAN_JUMP == true)
					playerSprite.setRegion(moveAnime.getKeyFrame(time, true));
			else if(pKeys.get(MyInputProcessor.CONTROL.UP) == true )
				//playerSprite.setRegion(jumpAnime.getKeyFrame(time, true));
				playerSprite.setRegion(moveAnime.getKeyFrames()[5]);
			else
				playerSprite.setRegion(idleAnime.getKeyFrame(time, true));
		}
		
		if(SWINGING){
			if(SWING_COMBO){
				playerSprite.setRegion(swingComboAnime.getKeyFrame(swing_time));
				
				//disable on last frame
				if(swingComboAnime.getKeyFrame(swing_time) == swingComboAnime.getKeyFrames()[swingComboAnime.getKeyFrames().length-1])
				{
					SWINGING = false;
					SWING_COMBO = false;
				}
			}
			else{
				playerSprite.setRegion(swingAnime.getKeyFrame(swing_time));
				
				//disable on last frame
				if(swingAnime.getKeyFrame(swing_time) == swingAnime.getKeyFrames()[swingAnime.getKeyFrames().length-1])
					SWINGING = false;				
			}
		}
		
		//fire_hold < BULLET_HOLD_TIME/4
		if(fire_hold > BULLET_HOLD_TIME/10f){
			playerSprite.setRegion(gameAtlas.findRegion("player-hand-1"));
		}
		
		//for hit animation
		hitSprite.setRegion(hitAnime.getKeyFrame(deathClock));
		if(GOT_HIT)
		{
			if(!LEFT_DIRECTION)	
				hitSprite.setFlip(true, false);
			
			//if(LevelGenerate.WORLD_FLIPPED)
			//	hitSprite.setFlip(playerSprite.isFlipX(), true);
						
			if(hitAnime.getKeyFrameIndex(deathClock) == 0)
			{
				hitSprite.setRotation(0);
				hitSprite.setSize(width*3.4f, width*2.5f);
				hitSprite.setOrigin(hitSprite.getWidth()/2, hitSprite.getHeight()/2);
				hitSprite.setPosition(position.x - hitSprite.getWidth()*0.55f, position.y - hitSprite.getHeight()*0.5f);
				
			}
			else				
			{
				if(hitAnime.getKeyFrameIndex(deathClock) == 3)
					hitSprite.setRotation(45);
				else
					hitSprite.setRotation((deathClock * 1000)%360);
				
				hitSprite.setSize(width*2.5f, width * 2.5f);
				hitSprite.setOrigin(hitSprite.getWidth()/2, hitSprite.getHeight()/2);
				hitSprite.setPosition(position.x - hitSprite.getWidth()/2, position.y - hitSprite.getHeight()/2);

			}
			
			if(!hitAnime.isAnimationFinished(deathClock))
				hitSprite.draw(batch);
		}
		
		if(LEFT_DIRECTION)	
			playerSprite.setFlip(true, false);
		
		//if(PLAYER_DRAW && !GOT_HIT && !DEAD)
		if(!SWING_STORED)
			playerSprite.draw(batch);
		else
			playerSprite.draw(batch,0.6f);

	}
	
	public void renderParticles(SpriteBatch batch){

		if(GameScreen.PLAYER_PARTICLES)
			jumpParticle.draw(batch);
		
	}
	
	public void update(float delta){
		time+= delta;
		
		if(GOT_HIT && !DEAD)
		{//make him die after half a sec

			deathClock += delta;
			if(deathClock > DEATH_CAM){
				DEAD = true;
			}
		}
		
		if((TELEPORTING_IN || TELEPORTING_OUT) && !DEAD)
		{//make teleportation animation

			//give entering more time then going out
			winClock += (TELEPORTING_IN) ? delta*0.8f : delta;
			
			if(winClock > WIN_CAM){
				TELEPORTING_OUT = TELEPORTING_IN = false;
				winClock = 0;				
			}
		}
		
		if(SWINGING){
			//check if swing otion is active
			swing_time += delta;
			
			if(swing_time > TOTAL_SWING_TIME){
				SWINGING = SWING_COMBO = false;
				swing_time = 0;
			}
			
		}
		else
			SWING_STORED = false;
		
		if(FIRING){
			//fire bullets when fire is hold, with a slight pause
			last_bullet_time += delta;
			
			if(last_bullet_time > 0.05f){
				LevelGenerate.getInstance().fireBullet(0);
				bullets_fired++;
				last_bullet_time = 0;
			}
			
			if(bullets_fired >= TOTAL_BULLETS)
			{
				FIRING = false;
				bullets_fired = 0;
			}
		}
		
		//dashing		
		if(DASHING){
			dashing_time += delta;
			if(dashing_time > DASH_DURATION/5f)
				body.setLinearVelocity(0, body.getLinearVelocity().y);
			
			if(dashing_time > DASH_DURATION){
				dashing_time = 0;
				DASHING = false;
				DASH_BUTTON_COUNT = 0;
			}
			
		}
		
		if((SWINGING || DASHING) && !DEAD){
			
			//one swing, give one set of damage
			if((SWINGING && SWING_STORED) || DASHING)
			for(Enemy e: enemyNearPool){
				SWING_STORED = false;
				LevelGenerate.getInstance().enemyGotHit(e, getWeaponDamage(), LEFT_DIRECTION);
				
				if(e.DEAD)
					enemyNearPool.removeValue(e, false);
			}
		}
		
		
		//update player movement based on keys pressed right now
		if(pKeys != null && CONTROLS)
			updateMove(delta);
		
		position = body.getPosition();
		
		if(checkDeath())
		{
			DEAD = true;
			CONTROLS = false;
		}
		
		if(GameScreen.PLAYER_PARTICLES)
			jumpParticle.update(delta);
				
		if(FLYING)
		{
			flightTime += delta;
			
			if(flightTime > FLYING_TIME)
				disableFlying();
			
			if(GLOWING){
				gameScreen.shakeThatAss(true);	
			}
			
			glow.setRotation((time*100)%360);

		}
	}
	
	public void reset(){
		MyGame.sop("RESETTT");
		position.set(startPos);
		GameScreen.SLOW_MOTION = false;

		body.setTransform(position, 0);
		body.setLinearVelocity(0, 0);
		
		/*
		weapon.setLinearVelocity(0, 0);
		weapon.setAngularVelocity(0);
		*/	
		
		GOT_HIT = LEFT_DIRECTION = DEAD = TELEPORTING_OUT = SWINGING = SWING_COMBO = SWING_STORED = FIRING = DASHING = false;
		dashButtonCooler = DASH_BUTTON_SPEED;
		DASH_BUTTON_COUNT = 0;
		
		TELEPORTING_IN = true;
		
		CONTROLS = CAN_FIRE = true;
		deathClock = 0;
		time = swing_time = fire_hold = dashing_time = 0;
		visible = true;
		winClock = 0;
		CAN_FIRE = true;
		
		fixBodyAngle();
		enemyNearPool.clear();

	}
	
	public void fireBullets(){
		//fire all four bullets with a pause
		
		FIRING = true;
		bullets_fired = 0;
		last_bullet_time = 0;
	}
	
	public void swingWeapon(){
		if(!CAN_FIRE) return;
		
		if(SWINGING){
			SWING_COMBO = !SWING_COMBO;
			
			if(SWING_COMBO)
				body.applyLinearImpulse(((LEFT_DIRECTION) ? -1: 1) * 0.4f, 0, 0, 0, true);
		}
		
		SWINGING = true;
		swing_time = 0;
		SWING_STORED = true;
		
		/*
		if(LEFT_DIRECTION)
			weapon.applyTorque(3, true);
		else
			weapon.applyTorque(-3, true);
		*/
		
	}
	
	/** disable gravity for body
	 * @param time negative values extends flight duration
	 * **/
	public void enableFlying(float time){
		FLYING = true;
		flightTime = time;
		body.setGravityScale(0);
	}
	
	public void disableFlying(){		
		FLYING = false;
		GLOWING = false;
		body.setGravityScale(gravityScale);
		body.setLinearVelocity(0, 0);

		gameScreen.showControls();
	}
	
	/** starts an cinematic sequence of player flying  **/
	public void makeItGlow(){
		enableFlying(-2);
		body.setLinearVelocity(0, 0.2f);
		
		gameScreen.hideControls();
		GLOWING = true;		
	}
	
	public void flipBodyAngle(){
		body.setTransform(position, 180*MathUtils.degRad);
	}
	
	public void fixBodyAngle(){
		body.setTransform(position, 0);
	}
	
	public boolean checkDeath(){
		boolean dead = false;
		
		if(IMMORTAL) return false;
		
		if(DEAD || position.y < 0) //|| position.y > bHEIGHT + bHEIGHT/4
			dead = true;
		
		return dead;		
	}
	
	public static Player getInstance(){
		return _player;
	}


	public void setDeath() {
		//check time to avoid instant death after respawning
		if(GOT_HIT || DEAD || time < 0.5f || DASHING) return;
				
		CONTROLS = CAN_FIRE = false;
		GOT_HIT = true;
		
		GameScreen.SLOW_MOTION = true;
		
		if(GameScreen.BACKGROUND_MUSIC)
			Gdx.input.vibrate(50);
		//LevelGenerate.getInstance().playEnemyHitSound();

				
		int sign = (LEFT_DIRECTION) ? 1: -1;
		int grav =  1;
		
		body.setLinearVelocity(0, 0);		
		body.applyLinearImpulse(sign * 1.8f, grav * 1f, body.getWorldCenter().x, body.getWorldCenter().y, false);
		
//		Timer.schedule(new Task(){
//		    @Override
//		    public void run() {
//		    	DEAD = true;
//		    }
//		}, 0.5f);
	
	}
		
	public Body getBody(){
		return body;
	}
	
	/*
	public Body getWeapon(){
		return weapon;
	}
	*/
	
	public float getWidth(){
		return width;
	}
	
	public float getHeight(){
		return height;
	}
	
	public Fixture getWeaponFixture(){
		return weaponFixture;
	}
	
	public Fixture getBodyFixture(){
		return bodyFixture;
	}
	
	public Fixture getSensorFixture(){
		return sensorFixture;
	}
	
	/** Starts jumping effect**/
	public void startJumpEffect(){
		jumpParticle.setPosition(position.x, position.y - height/2);
		jumpParticle.start();
	}	
	
	public void makeJump() {
		//maybe add a variable jump, like longer you hold, more it will jump
		if(CAN_JUMP && CONTROLS)
		{
			
			body.applyLinearImpulse(0, Jump, body.getWorldCenter().x, body.getWorldCenter().y, true);
			
			if(!IMMORTAL)
				CAN_JUMP = false;
		}
	}
	
	public void makeMiniJump() {
		body.setLinearVelocity(body.getLinearVelocity().x, 0);
		
		body.applyLinearImpulse(0, Jump*0.7f, body.getWorldCenter().x, body.getWorldCenter().y, true);
		
	}

	public void moveLeft() {
		if(body.getLinearVelocity().x > -Speed)
		body.applyLinearImpulse(-0.1f, 0, body.getWorldCenter().x, body.getWorldCenter().y, true);
		//body.setLinearVelocity(-Speed, body.getLinearVelocity().y);
	}

	public void moveRight() {
		if(body.getLinearVelocity().x < Speed)
		body.applyLinearImpulse(0.1f, 0, body.getWorldCenter().x, body.getWorldCenter().y, true);

		//body.setLinearVelocity(Speed, body.getLinearVelocity().y);
	}

	public void stopMove() {
		//fix this somehow. i don;t know - maybe fixed
				
		if(CONTROLS)
			body.setLinearVelocity(0, body.getLinearVelocity().y);
		
				
	}

	/** update player temporary key cache 
	 * this is called once
	 * */
	public void updateKeys(HashMap<MyInputProcessor.CONTROL, Boolean> keys){
		pKeys = keys;
		
		//custom call for update
		updateMove(0);
				
		//dashing
		//button count == Need taps - 1 
		if(pKeys.get(MyInputProcessor.CONTROL.LEFT) == true || pKeys.get(MyInputProcessor.CONTROL.RIGHT) == true)
		{
			if(!DEAD && DASH_BUTTON_COUNT == 1 && dashButtonCooler > 0 && !DASHING && lastDashDirectionLeft == LEFT_DIRECTION){
		
			DASHING = true;
			MyGame.sop("DASHED...->>>");
					
			body.applyLinearImpulse(((LEFT_DIRECTION)?-1 : 1) * DASH_FORCE, 0, 0, 0, true);
			}
			else{
				dashButtonCooler = DASH_BUTTON_SPEED;
				//if not jumping
				//if(pKeys.get(MyInputProcessor.CONTROL.UP) == false && pKeys.get(MyInputProcessor.CONTROL.FIRE) == false)
					DASH_BUTTON_COUNT ++;
			}
			
		}
		lastDashDirectionLeft = LEFT_DIRECTION;

		

	}
	
	/** check for any update due to key input */
	public void updateMove(float delta) {		
		if(!CONTROLS)
			return;

		if(!FIRING){
			//if fire key hold, shoot bullets
			if(pKeys.get(MyInputProcessor.CONTROL.FIRE) == true){ //error here
				fire_hold += delta;
				//MyGame.sop(fire_hold);
			}
			else
				fire_hold = 0;
			
			if(fire_hold > BULLET_HOLD_TIME){
				fireBullets();
				fire_hold = 0;
				
				//MyGame.sop("Bullet fired");
			}
		}
		
		if(dashButtonCooler > 0){
			if((lastDashDirectionLeft == LEFT_DIRECTION))
			{
				dashButtonCooler -= delta;
			}
			else
				DASH_BUTTON_COUNT = 0;
		}
		else
			DASH_BUTTON_COUNT = 0;
		
		//if jump or fired reset dash count
		if(pKeys.get(MyInputProcessor.CONTROL.UP) == true || pKeys.get(MyInputProcessor.CONTROL.FIRE) == true)
			DASH_BUTTON_COUNT = 0;
		
		if(pKeys.get(MyInputProcessor.CONTROL.LEFT) == true && fire_hold == 0){
			
			moveLeft();
			if(LEFT_DIRECTION != true){
				LEFT_DIRECTION = true;
				
				//body.destroyFixture(weaponFixture);
				
				
				//weaponFixture = body.createFixture(weaponDef);
								
				/*
				world.destroyJoint(weaponJoint);
				
				//fix weapon position
				jointDef.bodyA = body;
				jointDef.bodyB = weapon;
				jointDef.localAnchorA.set(0, 0);
				jointDef.localAnchorB.set(-width/2, 0);
				jointDef.enableLimit = true;
				jointDef.lowerAngle = 90 * MathUtils.degreesToRadians;
				jointDef.upperAngle = 230 * MathUtils.degreesToRadians;
				
				jointDef.enableMotor = true;
				jointDef.maxMotorTorque = 0.1f;
				jointDef.motorSpeed = -5;
				
				weaponJoint = world.createJoint(jointDef);
				*/
			}
									
		}
		else if(pKeys.get(MyInputProcessor.CONTROL.RIGHT) == true && fire_hold == 0){
			moveRight();
			if(LEFT_DIRECTION != false){
				LEFT_DIRECTION = false;
				
				/*
				world.destroyJoint(weaponJoint);
				
				//fix weapon position
				jointDef.bodyA = body;
				jointDef.bodyB = weapon;
				jointDef.localAnchorA.set(0, 0);
				jointDef.localAnchorB.set(width/2, 0);
				jointDef.enableLimit = true;
				jointDef.lowerAngle = 110 * MathUtils.degreesToRadians;
				jointDef.upperAngle = 270 * MathUtils.degreesToRadians;
				
				jointDef.enableMotor = true;
				jointDef.maxMotorTorque = 0.1f;
				jointDef.motorSpeed = 5;
				
				weaponJoint = world.createJoint(jointDef);
				*/
			}
			
		}		
		
		
		
		//else if(pKeys.get(MyInputProcessor.CONTROL.LEFT) == false && keys.get(MyInputProcessor.CONTROL.RIGHT) == false)
		//	stopMove();
		
		
	}

	public Vector2 getPosition() {
		return position;
	}

	public Vector2 startPoint() {
		return startPos;
	}

	public boolean isDead() {
		return DEAD;
	}

	public void dispose() {
		//world.destroyBody(body);
		DEAD = true;
		
		enemyNearPool.clear();
	}

	public int getWeaponDamage() {
		if(GOT_HIT)
			return 0;
		else if(DASHING)
			return DASH_DAMAGE;
		else
			return WEAPON_DAMAGE;
	}

	/**Enemy is in/out of range of weapon */
	public void enemyUpdate(Enemy e, boolean iN_RANGE) {
		if(iN_RANGE && !enemyNearPool.contains(e, false)){
			MyGame.sop("Enter");
			enemyNearPool.add(e);
		}
		else{
			MyGame.sop("Exit");
			enemyNearPool.removeValue(e, false);
		}
		

	}

}
