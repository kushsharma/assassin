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
import com.badlogic.gdx.physics.box2d.Filter;
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
import utils.Recorder;

public class Ghost {

	public static Ghost _ghost = null;
	private boolean IMMORTAL = false;
	private boolean PLAYER_DRAW = true;
	
	private float height = 1.0f * 0.9f, width = 0.5f * 0.9f;
	private Vector2 position = new Vector2();
	Vector2 startPos = new Vector2();
	private float Speed = 4.6f * 0.8f; // 4.6f
	private float Jump = 1.45f;
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
	
	public boolean SWINGING = false; //if player is swinging right now
	public boolean SWING_STORED = false; //player is swinging and haven't hurt anyone till now
	private float swing_time = 0;
	public static final float TOTAL_SWING_TIME = Player.TOTAL_SWING_TIME;	//time in swing motion
	private Array<Enemy> enemyNearPool = new Array<Enemy>();

	private boolean visible = true;
	public boolean Revived = false;
	public int JUMP_DAMAGE = 2;
	public static int WEAPON_DAMAGE = Player.WEAPON_DAMAGE;
	
	private boolean DEAD = false; //reset game
	public boolean CONTROLS = true; //player can/can't be controlled
	public boolean GOT_HIT = false;
	public boolean TELEPORTING_OUT = false; //going out of stage
	public boolean TELEPORTING_IN = true; //coming in stage
	public static final float WIN_CAM = 0.8f;	//time before teleport
	public static final float DEATH_CAM = 0.1f;	//time before final death
	
	public boolean CAN_JUMP = true;
	public boolean CAN_FIRE = true;
	public boolean QUEUE_STARTPOS = true;//to avoid worldstep
	
	public boolean LEFT_DIRECTION = false;
	
	GameScreen gameScreen = GameScreen.getInstance();
		
	Body body;
	//Body weapon;
	//Joint weaponJoint;
	//RevoluteJointDef jointDef;
	World world;
	PolygonShape shape;
	BodyDef bodyDef;
	Fixture bodyFixture, sensorFixture;
	Fixture weaponFixture;
	
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
	Animation idleAnime, moveAnime, jumpAnime, swingAnime, fireIdleAnime, fireMoveAnime, megaIdleAnime, megaMoveAnime, hitAnime, teleportOut, teleportIn;

	AssetLord Assets = GameScreen.getInstance().getAssetLord();
	TextureAtlas gameAtlas;
	public HashMap<CONTROL, Boolean> pKeys = null;
	
	public Ghost(World w){
		_ghost = this;
		world = w;		
		
		//keys that can be active
		pKeys = new HashMap<CONTROL, Boolean>();
		pKeys.put(CONTROL.LEFT, false);
		pKeys.put(CONTROL.RIGHT, false);
		pKeys.put(CONTROL.UP, false);
		pKeys.put(CONTROL.FIRE, false);
		
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
		fixtureDef.filter.maskBits = (short) (LevelGenerate.CATEGORY_WALL | LevelGenerate.CATEGORY_POWERUP ); //| LevelGenerate.CATEGORY_BADBOY
		
		CircleShape cs = new CircleShape();
		cs.setRadius((width*0.7f)/2);
		cs.setPosition(new Vector2(0, -height/3)); // legs

		FixtureDef sensorDef = new FixtureDef();
		sensorDef.shape = cs;
		sensorDef.isSensor = true;
		sensorDef.filter.categoryBits = LevelGenerate.CATEGORY_PLAYER;
		sensorDef.filter.maskBits = (short) (LevelGenerate.CATEGORY_WALL ); //| LevelGenerate.CATEGORY_BADBOY
		
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
		
		FixtureDef weaponDef = new FixtureDef();
		weaponDef.isSensor = true;
		weaponDef.filter.categoryBits = LevelGenerate.CATEGORY_BULLET;
		weaponDef.filter.maskBits = LevelGenerate.CATEGORY_BADBOY;
		
		cs.setRadius(width*1.5f);
		cs.setPosition(new Vector2(0, 0));
		weaponDef.shape = cs;		
		weaponFixture = body.createFixture(weaponDef);
		
		
		body.setLinearVelocity(0, 0);
		body.setFixedRotation(true);
		body.setUserData("ghost");
		gravityScale = body.getGravityScale();
		
		handShape.dispose();
		cs.dispose();
		shape.dispose();
		
		
		getSkinTexture();
	}
	
	//set the starting point of player according to entry portal
	//will update on next frame
	public void setStartingPoint(float x, float y){
		startPos.set(x, y);
		position.set(x, y);
		QUEUE_STARTPOS = true;		
	}
	
	private void getSkinTexture(){
		
		//textures
		gameAtlas = Assets.manager.get(AssetLord.game_atlas, TextureAtlas.class);

		//Texture tex = new Texture("level/player-idle-1.png");
		playerSprite = new Sprite(gameAtlas.findRegion("player-idle-1"));
		//playerSprite.setSize(height*1.2f * playerSprite.getWidth()/playerSprite.getHeight(), height*1.2f);
		playerSprite.setSize(width*3f, width * 3f * playerSprite.getHeight()/playerSprite.getWidth());
		playerSprite.setOrigin(playerSprite.getWidth()/2, playerSprite.getHeight()/2);
		
		//adding color tint
		playerSprite.setAlpha(0.6f);
		
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
		swingAnime.setPlayMode(PlayMode.LOOP);		
		
		
		
		glow = new Sprite(gameAtlas.findRegion("playerglow"));
		glow.setSize(width*8, width*8 * glow.getHeight()/glow.getWidth());
		glow.setPosition(body.getPosition().x - glow.getWidth()/2, body.getPosition().y - glow.getHeight()/2);
		glow.setColor(1f,1f,1f, 0.5f);
		glow.setOrigin(glow.getWidth()/2, glow.getHeight()/2);
	}
	
	public void render(ShapeRenderer canvas){
		//update(delta);
				
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
			if((pKeys.get(MyInputProcessor.CONTROL.LEFT) == true || pKeys.get(MyInputProcessor.CONTROL.RIGHT) == true) && pKeys.get(MyInputProcessor.CONTROL.UP) == false)
					playerSprite.setRegion(moveAnime.getKeyFrame(time, true));
			else if(pKeys.get(MyInputProcessor.CONTROL.UP) == true )
				//playerSprite.setRegion(jumpAnime.getKeyFrame(time, true));
				playerSprite.setRegion(moveAnime.getKeyFrames()[5]);
			else
				playerSprite.setRegion(idleAnime.getKeyFrame(time, true));
		}
		
		if(SWINGING){
			playerSprite.setRegion(swingAnime.getKeyFrame(swing_time));
			
			//disable on last frame
			if(swingAnime.getKeyFrame(swing_time) == swingAnime.getKeyFrames()[swingAnime.getKeyFrames().length-1])
				SWINGING = false;
		}
		
		if(fire_hold > BULLET_HOLD_TIME/10f){
			playerSprite.setRegion(gameAtlas.findRegion("player-hand-1"));
		}
		
		if(LEFT_DIRECTION)	
			playerSprite.setFlip(true, false);
		
		if(PLAYER_DRAW && !GOT_HIT && !DEAD)
			playerSprite.draw(batch);

	}
	
	public void renderParticles(SpriteBatch batch){

		//if(GameScreen.PLAYER_PARTICLES)
		//	jumpParticle.draw(batch);
		
	}
	
	public void update(float delta){
		time+= delta;
		
		if(QUEUE_STARTPOS){
			//helpful in avoiding worldstep fail
			QUEUE_STARTPOS = false;
			body.setTransform(startPos.x, startPos.y, 0);
		}

		
		if(GOT_HIT && !DEAD)
		{//make him die after half a sec

			deathClock += delta;
			if(deathClock > DEATH_CAM){
				DEAD = true;
				
				body.setActive(false);
				//weapon.setActive(false);
				
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
				SWINGING = SWING_STORED = false;
				swing_time = 0;
			}
			

			//one swing, give one set of damage
			if(SWING_STORED)
			for(Enemy e: enemyNearPool){
				SWING_STORED = false;
				LevelGenerate.getInstance().enemyGotHit(e, getWeaponDamage(), LEFT_DIRECTION);
			}
		}
		else
			SWING_STORED = false;
		
		if(FIRING){
			//fire bullets when fire is hold, with a slight pause
			last_bullet_time += delta;
			
			if(last_bullet_time > 0.05f){
				LevelGenerate.getInstance().fireBullet(1);
				bullets_fired++;
				last_bullet_time = 0;
			}
			
			if(bullets_fired >= TOTAL_BULLETS)
			{
				FIRING = false;
				bullets_fired = 0;
			}
		}
		
		//update player movement based on keys pressed right now
		if(pKeys != null  && CONTROLS)
			updateMove(delta);
		
		position = body.getPosition();
		
		if(checkDeath())
		{
			DEAD = true;
			CONTROLS = false;
		}
		
		//if(GameScreen.PLAYER_PARTICLES)
		//	jumpParticle.update(delta);
		
		
		
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
		MyGame.sop("Ghost Reset");
		position.set(startPos);
		//GameScreen.SLOW_MOTION = false;

		body.setActive(true);
		//weapon.setActive(true);
		
		body.setTransform(position, 0);
		body.setLinearVelocity(0, 0);
		
		GOT_HIT = LEFT_DIRECTION = DEAD = TELEPORTING_OUT = SWINGING = SWING_STORED = FIRING = false;
		TELEPORTING_IN = true;
		CAN_FIRE = true;

		CONTROLS = true;
		deathClock = 0;
		time = swing_time = fire_hold = 0;
		visible = true;
		winClock = 0;
		
		fixBodyAngle();
		
		enemyNearPool.clear();
		
		/*
		weapon.setAngularVelocity(0);
		weapon.setLinearVelocity(0, 0);
		*/
		
		pKeys.put(CONTROL.LEFT, false);
		pKeys.put(CONTROL.RIGHT, false);
		pKeys.put(CONTROL.UP, false);
		pKeys.put(CONTROL.FIRE, false);
	}

	public void fireBullets(){
		//fire all four bullets with a pause
		
		FIRING = true;
		bullets_fired = 0;
		last_bullet_time = 0;
	}
	
	public void swingWeapon(){
		if(!CAN_FIRE) return;

		SWINGING = true;
		swing_time = 0;
		SWING_STORED = true;
		
		//MyGame.sop("Swing baby swing");
		
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
	
	public static Ghost getInstance(){
		return _ghost;
	}


	public void setDeath() {
		//check time to avoid instant death after respawning
		if(GOT_HIT || DEAD || time < 0.5f) return;
				
		//CONTROLS = false;
		GOT_HIT = true;
		//body.setActive(false);
		//weapon.setActive(false);
		//MyGame.sop("zzzzzzzzzzzzzzzz");
		
		//GameScreen.SLOW_MOTION = true;
		
		//if(GameScreen.BACKGROUND_MUSIC)
		//	Gdx.input.vibrate(50);
		//LevelGenerate.getInstance().playEnemyHitSound();

				
		int sign = (LEFT_DIRECTION) ? 1: -1;
		int grav =  1;
		
		body.setLinearVelocity(0, 0);
		//weapon.setLinearVelocity(0, 0);
		
		
		//body.applyLinearImpulse(sign * 2.8f, grav * 1f, body.getWorldCenter().x, body.getWorldCenter().y, false);
		
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
	
	public Fixture getBodyFixture(){
		return bodyFixture;
	}
	
	public Fixture getSensorFixture(){
		return sensorFixture;
	}
	
	/** Starts jumping effect**/
	public void startJumpEffect(){
		//jumpParticle.setPosition(position.x, position.y - height/2);
		//jumpParticle.start();
	}	
	
	public void makeJump() {
		//maybe add a variable jump, like longer you hold, more it will jump
		if(CAN_JUMP && CONTROLS)
		{
			
			body.applyLinearImpulse(0, Jump, body.getWorldCenter().x, body.getWorldCenter().y, true);
			
			//CAN_JUMP = false;
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

	/** update player temporary key cache */
	public void updateKeys(Recorder.RecordFrame frame){
		//pKeys = keys;
		if(GOT_HIT) return;
		
			switch (frame.move){
			case Recorder.ACTION_LEFT:{
				
				pKeys.put(MyInputProcessor.CONTROL.LEFT, true);
				break;
			}
			case Recorder.ACTION_LEFT_STOP:{
				pKeys.put(MyInputProcessor.CONTROL.LEFT, false);
				
				if(pKeys.get(MyInputProcessor.CONTROL.RIGHT) == false)
					stopMove();
					
				break;
			}
			case Recorder.ACTION_RIGHT:{
				pKeys.put(MyInputProcessor.CONTROL.RIGHT, true);
				break;
			}
			case Recorder.ACTION_RIGHT_STOP:{
				pKeys.put(MyInputProcessor.CONTROL.RIGHT, false);
				
				if(pKeys.get(MyInputProcessor.CONTROL.LEFT) == false)
					stopMove();
				break;
			}
			case Recorder.ACTION_JUMP:{
				//pKeys.put(MyInputProcessor.CONTROL.UP, true);
				makeJump();
				break;
			}
			case Recorder.ACTION_JUMP_STOP:{
				//pKeys.put(MyInputProcessor.CONTROL.UP, false);
				break;
			}
			case Recorder.ACTION_FIRE:{
				pKeys.put(MyInputProcessor.CONTROL.FIRE, true);
				swingWeapon();
				CAN_FIRE = false;
				break;
			}
			case Recorder.ACTION_FIRE_STOP:{
				pKeys.put(MyInputProcessor.CONTROL.FIRE, false);
				CAN_FIRE = true;
				break;
			}
			case Recorder.ACTION_STAY:{
				pKeys.put(MyInputProcessor.CONTROL.LEFT, false);
				pKeys.put(MyInputProcessor.CONTROL.RIGHT, false);
				setDeath();
				body.setLinearVelocity(0, 0);
				break;
			}
			
		}
		
		if(!GOT_HIT && CONTROLS && !DEAD && frame.move != Recorder.ACTION_STAY){
			
			if(!GameScreen.SLOW_MOTION)
			body.setTransform(frame.posx, frame.posy, 0);
			
			body.setLinearVelocity(frame.vx, frame.vy);
			
		}
		
		//weapon.setTransform(weapon.getPosition(), frame.weaponAngle);
		//weapon.setAngularVelocity(frame.weaponAngleV);
	}
	
	/** check for any update due to key input */
	public void updateMove(float delta) {		
		if(!CONTROLS || GOT_HIT)
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
		
		if(pKeys.get(MyInputProcessor.CONTROL.LEFT) == true){
			moveLeft();
			
			if(LEFT_DIRECTION != true){
				LEFT_DIRECTION = true;
				
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
		else if(pKeys.get(MyInputProcessor.CONTROL.RIGHT) == true){
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
		
		//if(pKeys.get(MyInputProcessor.CONTROL.LEFT) == false && pKeys.get(MyInputProcessor.CONTROL.RIGHT) == false)
		//	stopMove();
		
		
	}

	public Vector2 getPosition() {
		return position;
	}

	public Vector2 startPoint() {
		return startPos;
	}
	
	public float getWidth(){
		return width;
	}
	
	public float getHeight(){
		return height;
	}
	
	public boolean isDead() {
		return DEAD;
	}
	
	public Fixture getWeaponFixture(){
		return weaponFixture;
	}
	
	public void dispose() {
		//world.destroyBody(body);
		
		enemyNearPool.clear();
	}

	public int getWeaponDamage() {
		if(GOT_HIT)
			return 0;
		else
			return WEAPON_DAMAGE;
	}

	/**Enemy is in/out of range of weapon */
	public void enemyUpdate(Enemy e, boolean iN_RANGE) {
		if(iN_RANGE && !enemyNearPool.contains(e, false)){
			enemyNearPool.add(e);
		}
		else{
			enemyNearPool.removeValue(e, false);
		}
		

	}

	public void hide() {
		//body.setTransform(-bWIDTH, -bHEIGHT, 0);
		
		body.setActive(false);
		//weapon.setActive(false);
		
		visible = false;
		DEAD = true;
		GOT_HIT = true;
		CONTROLS = false;
	}

}
