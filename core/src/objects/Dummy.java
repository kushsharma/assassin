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

public class Dummy {

	public static Dummy _player = null;
	private boolean IMMORTAL = false;
	private boolean PLAYER_DRAW = true;
	
	private float height = 1.0f, width = 0.5f;
	private Vector2 position = new Vector2();
	Vector2 startPos = new Vector2();
	private float Speed = 4.6f; // 4.6f
	private float Jump = 1.85f;
	private float gravityScale = 0f;
	
	public boolean GLOWING = false;
	private boolean FLYING = false; // disable player gravity effect
	private float flightTime = 0f;
	private final float FLYING_TIME = 0.5f;

	private boolean visible = true;
	public boolean Revived = false;
	public int JUMP_DAMAGE = 2;
	public static int WEAPON_DAMAGE = 10;
	
	private boolean DEAD = false; //reset game
	public boolean CONTROLS = true; //player can/can't be controlled
	public boolean GOT_HIT = false;
	public boolean TELEPORTING_OUT = false; //going out of stage
	public boolean TELEPORTING_IN = true; //coming in stage
	public static final float WIN_CAM = 0.8f;	//time before teleport
	public static final float DEATH_CAM = 0.4f;	//time before final death
	
	public boolean CAN_JUMP = true;
	public boolean CAN_FIRE = true;
	
	public boolean LEFT_DIRECTION = false;
	
	GameScreen gameScreen = GameScreen.getInstance();
		
	Body body, weapon;
	Joint weaponJoint;
	RevoluteJointDef jointDef;
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
	Animation idleAnime, moveAnime, jumpAnime, fireAnime, fireIdleAnime, fireMoveAnime, megaIdleAnime, megaMoveAnime, hitAnime, teleportOut, teleportIn;

	AssetLord Assets = GameScreen.getInstance().getAssetLord();
	TextureAtlas gameAtlas;
	public HashMap<CONTROL, Boolean> pKeys = null;
	
	public Dummy(World w){
		_player = this;
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
		
		//sword
		FixtureDef weaponDef = new FixtureDef();
		weaponDef.density = 0.1f;
		weaponDef.isSensor = true;
		weaponDef.filter.categoryBits = LevelGenerate.CATEGORY_BULLET;
		weaponDef.filter.maskBits = LevelGenerate.CATEGORY_BADBOY;
		
		handShape.setAsBox(width*0.9f, width/10);
		weaponDef.shape = handShape;
		
		//bodyDef.fixedRotation = false;
		//bodyDef.type = BodyDef.BodyType.DynamicBody;
		//bodyDef.position.set(position.x, position.y);
		
		weapon = world.createBody(bodyDef);
		weaponFixture = weapon.createFixture(weaponDef);
		
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
		jointDef.motorSpeed = 10;
		
		weaponJoint = world.createJoint(jointDef);
		
		body.setLinearVelocity(0, 0);
		body.setFixedRotation(true);
		
		weapon.setUserData("weapon");
		body.setUserData("ghost");
		
		gravityScale = body.getGravityScale();
		
		handShape.dispose();
		cs.dispose();
		shape.dispose();
		
	
	}
	
	//set the starting point of player according to entry portal
	public void setStartingPoint(float x, float y){
		startPos.set(x, y);
		position.set(x, y);
		body.setTransform(x, y, 0);
	}
	
	private void getSkinTexture(){
		
	}
	
	public void render(ShapeRenderer canvas){
		//update(delta);
				
        canvas.setColor(0.1f, 0.1f, 0.1f, 1.0f);
		canvas.rect(position.x-width/2, position.y-height/2, width/2 , height/2, width, height, 1, 1, body.getAngle());
		
	}
	
	public void render(SpriteBatch batch){
		if(!visible) return;
		
		

	}
	
	public void renderParticles(SpriteBatch batch){

		//if(GameScreen.PLAYER_PARTICLES)
		//	jumpParticle.draw(batch);
		
	}
	
	public void update(float delta){
		time+= delta;
		
		MyGame.sop(weapon.getAngularVelocity());
		
		//update player movement based on keys pressed right now
		if(pKeys != null  && CONTROLS)
			updateMove();
		
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
		GameScreen.SLOW_MOTION = false;

		body.setActive(true);
		weapon.setActive(true);
		
		body.setTransform(position, 0);
		body.setLinearVelocity(0, 0);
		
		GOT_HIT = LEFT_DIRECTION = DEAD = TELEPORTING_OUT = false;
		TELEPORTING_IN = true;
		
		CONTROLS = true;
		deathClock = 0;
		time = 0;
		visible = true;
		winClock = 0;
		
		fixBodyAngle();
		
		weapon.setAngularVelocity(0);
		weapon.setLinearVelocity(0, 0);
		
		
		pKeys.put(CONTROL.LEFT, false);
		pKeys.put(CONTROL.RIGHT, false);
		pKeys.put(CONTROL.UP, false);
		pKeys.put(CONTROL.FIRE, false);
	}
	
	public void swingWeapon(){
		if(!CAN_FIRE) return;
		
		MyGame.sop("Swing baby swing");
		
		if(LEFT_DIRECTION)
			weapon.applyTorque(3, true);
		else
			weapon.applyTorque(-3, true);
		//weapon.applyAngularImpulse(4, true);
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
	
	public static Dummy getInstance(){
		return _player;
	}


	public void setDeath() {
		//check time to avoid instant death after respawning
		if(GOT_HIT || DEAD || time < 0.5f) return;
				
		//CONTROLS = false;
		GOT_HIT = true;
		
		//GameScreen.SLOW_MOTION = true;
		
		if(GameScreen.BACKGROUND_MUSIC)
			Gdx.input.vibrate(50);
		//LevelGenerate.getInstance().playEnemyHitSound();

				
		int sign = (LEFT_DIRECTION) ? 1: -1;
		int grav =  1;
		
		body.setLinearVelocity(0, 0);		
		body.applyLinearImpulse(sign * 2.8f, grav * 1f, body.getWorldCenter().x, body.getWorldCenter().y, false);
		
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
	public void updateKeys(int move){
		//pKeys = keys;
		
		switch (move){
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
			pKeys.put(MyInputProcessor.CONTROL.UP, true);
			makeJump();
			break;
		}
		case Recorder.ACTION_JUMP_STOP:{
			pKeys.put(MyInputProcessor.CONTROL.UP, false);
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
		}
	}
	
	/** check for any update due to key input */
	public void updateMove() {		
		if(!CONTROLS)
			return;
		
		if(pKeys.get(MyInputProcessor.CONTROL.LEFT) == true){
			moveLeft();
			
			if(LEFT_DIRECTION != true){
				LEFT_DIRECTION = true;
				
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
				jointDef.motorSpeed = -10;
				
				weaponJoint = world.createJoint(jointDef);
				
			}
		}
		else if(pKeys.get(MyInputProcessor.CONTROL.RIGHT) == true){
			moveRight();
			if(LEFT_DIRECTION != false){
				LEFT_DIRECTION = false;
				
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
				jointDef.motorSpeed = 10;
				
				weaponJoint = world.createJoint(jointDef);
				
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

	public boolean isDead() {
		return DEAD;
	}

	public void dispose() {
		//world.destroyBody(body);
	}

	public int getWeaponDamage() {
		return WEAPON_DAMAGE;
	}

	public void hide() {
		//body.setTransform(-bWIDTH, -bHEIGHT, 0);
		
		body.setActive(false);
		weapon.setActive(false);
		
		visible = false;
		DEAD = true;
		GOT_HIT = true;
		CONTROLS = false;
	}

}
