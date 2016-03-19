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
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.softnuke.epic.MyGame;

import Screens.GameScreen;
import utils.AssetLord;
import utils.LevelGenerate;
import utils.MyInputProcessor;
import utils.MyInputProcessor.CONTROL;

public class Friend {

	public static Friend _friend = null;
	private boolean IMMORTAL = false;
	private boolean PLAYER_DRAW = true;
	
	private float height = 1.0f * 0.9f, width = 0.5f * 0.9f;
	public Vector2 netPosition = new Vector2();
	private Vector2 position = new Vector2();
	Vector2 startPos = new Vector2();
	private float Speed = 4.6f * 0.8f; // 4.6f
	private float Jump = 1.40f;
	private float gravityScale = 0f;
	
	public boolean GLOWING = false;
	private boolean FLYING = false; // disable player gravity effect
	private float flightTime = 0f;
	private final float FLYING_TIME = 0.5f;

	private boolean visible = true;
	public boolean Revived = false;
	public int JUMP_DAMAGE = 2;
	
	private boolean DEAD = false; //reset game
	public boolean CONTROLS = true; //player can/can't be controlled
	public boolean GOT_HIT = false;
	public boolean TELEPORTING_OUT = false; //going out of stage
	public boolean TELEPORTING_IN = true; //coming in stage
	public static final float WIN_CAM = 0.8f;	//time before teleport
	public static final float DEATH_CAM = 0.4f;	//time before final death
	
	private boolean SWINGING = false; //if player is swinging right now
	private float swing_time = 0;
	public static final float SWING_TIME = 0.5f;	//time in swing motion
	
	public boolean CAN_JUMP = true;
	public boolean CAN_FIRE = true;
	public boolean QUEUE_STARTPOS = true;//to avoid worldstep

	public boolean LEFT_DIRECTION = false;
	
	GameScreen gameScreen = GameScreen.getInstance();
		
	Body body;
	World world;
	PolygonShape shape;
	BodyDef bodyDef;
	Fixture bodyFixture, sensorFixture;
	
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
	
	public Friend(World w){
		_friend = this;
		world = w;	
		
		//keys that can be active
		pKeys = new HashMap<CONTROL, Boolean>();
		pKeys.put(CONTROL.LEFT, false);
		pKeys.put(CONTROL.RIGHT, false);
		pKeys.put(CONTROL.UP, false);
		pKeys.put(CONTROL.FIRE, false);
		
		position.x = startPos.x = bWIDTH/4 ;
		position.y = startPos.y = bHEIGHT/2  - height/2;
		
		netPosition.x = position.x;
		netPosition.y = position.y;
		
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
		fixtureDef.filter.maskBits = (short) (LevelGenerate.CATEGORY_WALL ); //| LevelGenerate.CATEGORY_POWERUP | LevelGenerate.CATEGORY_BADBOY
		
		body = world.createBody(bodyDef);
		bodyFixture = body.createFixture(fixtureDef);

		body.setLinearVelocity(0, 0);
		body.setFixedRotation(true);

		CircleShape cs = new CircleShape();
		cs.setRadius((width*0.7f)/2);
		cs.setPosition(new Vector2(0, -height/3)); // legs

		FixtureDef sensorDef = new FixtureDef();
		sensorDef.shape = cs;
		sensorDef.isSensor = true;
		sensorDef.filter.categoryBits = LevelGenerate.CATEGORY_PLAYER;
		sensorDef.filter.maskBits = (short) (LevelGenerate.CATEGORY_WALL ); //| LevelGenerate.CATEGORY_BADBOY
		
		sensorFixture = body.createFixture(sensorDef);

		
		
		body.setUserData("friend");
		gravityScale = body.getGravityScale();
		
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
				
        canvas.setColor(0.1f, 0.6f, 0.1f, 1.0f);
		canvas.rect(position.x-width/2, position.y-height/2, width/2 , height/2, width, height, 1, 1, body.getAngle());
		
		//canvas.setColor(0.6f, 0.1f, 0.1f, 0.2f);
		//canvas.rect(netPosition.x-width/2, netPosition.y-height/2, width/2 , height/2, width, height, 1, 1, body.getAngle());
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
			playerSprite.setRegion(swingAnime.getKeyFrame(swing_time));
			
			//disable on last frame
			if(swingAnime.getKeyFrame(swing_time) == swingAnime.getKeyFrames()[swingAnime.getKeyFrames().length-1])
				SWINGING = false;
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
		
		
		position.set(body.getPosition());
		
		if(UPDATE_POS){
			body.setTransform(netPosition.x, netPosition.y, body.getAngle());
			UPDATE_POS = false;
		}
		else{
			float x = Interpolation.linear.apply(position.x, netPosition.x, delta*2);

			//don't interpolate y-axis if too far
			if(Math.abs(position.y - netPosition.y) < 0.2f)
			{
				float y = Interpolation.linear.apply(position.y, netPosition.y, delta*2);
				body.setTransform(x, y, body.getAngle());			
			}
			else{
				body.setTransform(x, position.y, body.getAngle());
			}
		}
		
		//update player movement based on keys pressed right now
		if(pKeys != null && CONTROLS)
			updateMove();
		
		
		if(checkDeath())
		{
			DEAD = true;
			CONTROLS = false;
		}
				

		if(SWINGING){
			//check if swing otion is active
			swing_time += delta;
			
			if(swing_time > SWING_TIME){
				SWINGING = false;
				swing_time = 0;
			}
		}
		
		
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
		
		GOT_HIT = LEFT_DIRECTION = DEAD = TELEPORTING_OUT = SWINGING = false;
		TELEPORTING_IN = true;
		
		CONTROLS = true;
		deathClock = 0;
		time = swing_time = 0;
		visible = true;
		winClock = 0;
		CAN_FIRE = true;
		
		fixBodyAngle();
		
		pKeys.put(CONTROL.LEFT, false);
		pKeys.put(CONTROL.RIGHT, false);
		pKeys.put(CONTROL.UP, false);
		pKeys.put(CONTROL.FIRE, false);
		
		fixBodyAngle();
	}
	
	public void swingWeapon(){
		//if(!CAN_FIRE) return;
		
		SWINGING = true;
		swing_time = 0;
		
		//if(LEFT_DIRECTION)
		//	weapon.applyTorque(3, true);
		//else
		//	weapon.applyTorque(-3, true);
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
	
	public static Friend getInstance(){
		return _friend;
	}


	public void setDeath() {
		//check time to avoid instant death after respawning
		if(GOT_HIT || DEAD || time < 0.5f) return;
				
		CONTROLS = false;
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

	public void updateMove(){
		if(!CONTROLS)
			return;
		
		if(pKeys.get(MyInputProcessor.CONTROL.LEFT) == true){
			moveLeft();
			LEFT_DIRECTION = true;
		}
		else if(pKeys.get(MyInputProcessor.CONTROL.RIGHT) == true){
			moveRight();
			LEFT_DIRECTION = false;
		}
		else if(pKeys.get(MyInputProcessor.CONTROL.FIRE) == true){
			swingWeapon();			
		}
		
		if(pKeys.get(MyInputProcessor.CONTROL.LEFT) == false && pKeys.get(MyInputProcessor.CONTROL.RIGHT) == false)
			stopMove();
		
	}
	public void updateMove(MyInputProcessor.CONTROL control, boolean state) {		
		if(!CONTROLS)
			return;
		
		pKeys.put(control, state);
		
		updateMove();
		//else if(keys.get(MyInputProcessor.CONTROL.LEFT) == false && keys.get(MyInputProcessor.CONTROL.RIGHT) == false)
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

	/** interpolate slowly **/
	public void interpolateTo(float x, float y) {
		netPosition.set(x, y);
	}

	boolean UPDATE_POS = false;
	/** instant jump **/
	public void jumpTo(float x, float y) {
		netPosition.set(x, y);
		UPDATE_POS = true;
	}

	public float getWidth() {
		return width;
	}

}
