package objects;

import java.util.HashMap;

import Screens.GameScreen;
import utils.AssetLord;
import utils.LevelGenerate;
import utils.MyInputProcessor;
import utils.MyInputProcessor.CONTROL;
import utils.NetworkManager;

import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.softnuke.epic.MyGame;


public class Enemy {

	public static Enemy _enemy = null;
	private boolean IMMORTAL = false;
	
	
	private float height = 0.8f * 0.8f, width = 0.8f * 0.8f;
	private int health = 0;
	private Vector2 position;
	Vector2 startPos;
	
	private float Speed = 3f * 0.6f;
	private float Jump = 1;
	private int HEALTH = 8;
	private float gravityScale = 0f;
	public static final Vector2 CENTER_VECTOR = new Vector2(0,0);
	public static final int SCORE_VALUE = 50;
	
	private float flightTime = 0f;
	public boolean DEAD = false;
	private boolean visible = true;
	public boolean Revived = false;
	public boolean LEFT_DIRECTION = true;
	public boolean RUNNING = true;
	public Vector2 netPosition = new Vector2();

	GameScreen gameScreen = GameScreen.getInstance();
		
	Body body;
	World world;
	BodyDef bodyDef;
	FixtureDef fixtureDef;
	//body is for whole body square
	//sensor for hands, revert movement direction
	//head for player kill by jumping on it
	//leg for platform sensor
	Fixture sensorFixture, bodyFixture, headFixture, legFixture;
	
	TextureRegion playerTexR;
	Texture playerTex;
	Sprite playerSprite;
	Sprite glow;
	ParticleEffect killParticle;
	
	private Vector2 WorldGravityNegative = new Vector2(GameScreen.WorldGravity.x *-1, 0);
	
	boolean ASIDE = true;//player on Wall A or B, true means A 
	
	float bHEIGHT = MyGame.bHEIGHT;
	float bWIDTH = MyGame.bWIDTH;
	
	float time = 0f;
	float lastJumpTime = 0;
	Animation idleAnime, moveAnime, jumpAnime, fireAnime;

	public HashMap<CONTROL, Boolean> pKeys = null;
	TextureAtlas atlas = GameScreen.getInstance().getAssetLord().manager.get(AssetLord.game_atlas, TextureAtlas.class);

	public Enemy(World w){
		_enemy = this;
		world = w;		
		position  = new Vector2();
		startPos =  new Vector2();
		
		init();
	}
	
	public Enemy(World w, Vector2 p){
		_enemy = this;
		world = w;		
		position = p;
		startPos = p.cpy();
		
		init();
	}
	
	public void init(){
		//to avoid grouping
		//Speed = (MathUtils.random(Speed-0.5f, Speed+0.5f));
		
		netPosition.set(position.x, position.y);
		health = HEALTH;
		
		bodyDef =  new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		
		//shape = new PolygonShape();
		//shape.setAsBox(width/2, height/2);
		bodyDef.position.set(position);
		body = world.createBody(bodyDef);

		PolygonShape ps = new PolygonShape();
		ps.setAsBox((width*0.7f)/2, (height*0.9f)/2, CENTER_VECTOR, 0);
		
		fixtureDef = new FixtureDef();
		fixtureDef.shape = ps;
		fixtureDef.density =  0.5f;
		fixtureDef.friction = 0.2f;
		fixtureDef.restitution = 0.05f;		
		fixtureDef.isSensor = false;
		fixtureDef.filter.categoryBits = LevelGenerate.CATEGORY_BADBOY;
		fixtureDef.filter.maskBits = (short) (LevelGenerate.CATEGORY_WALL | LevelGenerate.CATEGORY_PLAYER | LevelGenerate.CATEGORY_BULLET);
		bodyFixture = body.createFixture(fixtureDef);
		
		PolygonShape sensorS = new PolygonShape();
		sensorS.setAsBox(width * 0.5f, height/4, CENTER_VECTOR, 0);
		FixtureDef sensorDef = new FixtureDef();
		sensorDef.shape = sensorS;
		sensorDef.isSensor = true;
		sensorDef.filter.categoryBits = LevelGenerate.CATEGORY_BADBOY;
		sensorDef.filter.maskBits = (short) (LevelGenerate.CATEGORY_WALL | LevelGenerate.CATEGORY_PLAYER);		
		sensorFixture = body.createFixture(sensorDef);
		
		CircleShape cs = new CircleShape();
		cs.setRadius((width * 0.7f)/2);
		cs.setPosition(new Vector2(0, height/3));
		sensorDef.shape = cs;
		sensorDef.filter.maskBits = (short) (LevelGenerate.CATEGORY_WALL | LevelGenerate.CATEGORY_PLAYER);
		headFixture = body.createFixture(sensorDef);
		
		cs.setRadius((width * 0.6f)/2);
		cs.setPosition(new Vector2(0, -height/2));
		sensorDef.shape = cs;
		legFixture = body.createFixture(sensorDef);
		
		
		body.setLinearVelocity(0, 0);
		body.setFixedRotation(true);
		//body.setTransform(position.x,position.y, 0);
		
		body.setUserData("enemy");
		//System.out.println(sensorFixture.getBody().getUserData());

		gravityScale = body.getGravityScale();
		
		cs.dispose();
		sensorS.dispose();
		ps.dispose();
		
		//position.x = startPos.x = bWIDTH/4 ;
		//position.y = startPos.y = bHEIGHT/2 - bHEIGHT/6 - height/2;
		
		//texture
		//Texture tex = new Texture("level/enemy/enemy-move-1.png");
		
		playerSprite = new Sprite(atlas.findRegion("enemy-walk-1"));
		getSkinTexture();		
		playerSprite.setSize(width*2, height*2);
		/*
		//particle
		killParticle = new ParticleEffect(gameScreen.getAssetLord().manager.get(AssetLord.enemy_kill_particle,ParticleEffect.class));
		killParticle.setEmittersCleanUpBlendFunction(false);
		*/
	}
	
	private void getSkinTexture(){
		TextureRegion playerSheet[] = new TextureRegion[1];		
		playerSheet[0] = atlas.findRegion("enemy-move-1");		
		idleAnime = new Animation(0.2f, playerSheet);
		idleAnime.setPlayMode(PlayMode.LOOP_PINGPONG);
		
		TextureRegion playerMoveSheet[] = new TextureRegion[4];	
		
		playerMoveSheet[0] = atlas.findRegion("enemy-walk-1"); //new TextureRegion(new Texture("level/enemy/enemy-move-1.png"));
		playerMoveSheet[1] = atlas.findRegion("enemy-walk-2"); //new TextureRegion(new Texture("level/enemy/enemy-move-2.png"));
		playerMoveSheet[2] = atlas.findRegion("enemy-walk-3"); //new TextureRegion(new Texture("level/enemy/enemy-move-3.png"));
		playerMoveSheet[3] = atlas.findRegion("enemy-walk-4"); //new TextureRegion(new Texture("level/enemy/enemy-move-4.png"));
		//playerMoveSheet[4] = atlas.findRegion("enemy-move-5"); //new TextureRegion(new Texture("level/enemy/enemy-move-5.png"));
		//playerMoveSheet[5] = atlas.findRegion("enemy-move-6"); //new TextureRegion(new Texture("level/enemy/enemy-move-6.png"));
		
		moveAnime = new Animation(0.1f, playerMoveSheet);
		moveAnime.setPlayMode(PlayMode.LOOP);
		
//		TextureRegion playerJumpSheet[] = new TextureRegion[1];		
//		playerJumpSheet[0] = new TextureRegion(new Texture("level/player-jump-1.png"));
//				
//		jumpAnime = new Animation(0.2f, playerJumpSheet);
//		jumpAnime.setPlayMode(PlayMode.LOOP_PINGPONG);
	}
	
	public void render(ShapeRenderer canvas){
	
        canvas.setColor(1f, 1f, 0f, 1.0f);	
		canvas.rect(position.x-width/2, position.y-height/2, width/2 , height/2, width, height, 1, 1, body.getAngle());
		
		//MyGame.sop("Can jump:"+CAN_JUMP);
	}
	
	public void render(SpriteBatch batch){
		
		if(!visible || DEAD) return;
		
		
		//playerSprite.setPosition(position.x - playerSprite.getWidth()/2, position.y- playerSprite.getHeight()/2);
		playerSprite.setPosition(body.getPosition().x - playerSprite.getWidth()/2, body.getPosition().y - playerSprite.getHeight()*0.25f);
		
		if(RUNNING)
			playerSprite.setRegion(moveAnime.getKeyFrame(time, true));
		//else
		//	playerSprite.setRegion(idleAnime.getKeyFrame(time, true));

		
		if(!LEFT_DIRECTION)
			playerSprite.flip(true, false);
		
		if(LevelGenerate.WORLD_FLIPPED)
			playerSprite.flip(playerSprite.isFlipX(), true);
		
		playerSprite.draw(batch);
		 
		
	}
	
	public void renderParticles(SpriteBatch batch){
		killParticle.setPosition(position.x, position.y);
		if(GameScreen.PLAYER_PARTICLES)
			killParticle.draw(batch);		
		
	}
	
	public void update(float delta, float viewportWidth){
		time+= delta;
		
		position = body.getPosition();
		if(!DEAD && GameScreen.MULTIPLAYER){
			if(UPDATE_POS){
				UPDATE_POS = false;
				body.setTransform(netPosition.x, netPosition.y, body.getAngle());
			}
			//else if(GameScreen.getInstance().networkManager.STATE == NetworkManager.STATES.CLIENT){
			//	float x = Interpolation.linear.apply(position.x, netPosition.x, delta);
				//float y = Interpolation.linear.apply(position.y, netPosition.y, delta);
			//	body.setTransform(x, position.y, body.getAngle());			
			//}			
		}
		
		if((position.x > viewportWidth-bWIDTH*0.5 && position.x < viewportWidth+bWIDTH*0.8f))
			visible = true;
		else
			visible = false;
		
		if(checkDeath())
		{
			//player is dead
			setDeath();
		}
		
		//if(Math.abs(Player.getInstance().getPosition().x - position.x) < bWIDTH)
		//{//player close, make him alive
			if(LEFT_DIRECTION)
				moveLeft();
			else
				moveRight();
			
		//}
		
		//if(GameScreen.PLAYER_PARTICLES)
		//	killParticle.update(delta);
	}
	
	public static Enemy getInstance(){
		return _enemy;
	}

	public boolean checkDeath(){
		boolean dead = false;
		
		if(IMMORTAL) return false;
		
		if(DEAD || position.y < 0)
			dead = true;
		
		return dead;		
	}

	public void flipBodyAngle(){
		body.setTransform(position, 180*MathUtils.degRad);
	}
	
	public void fixBodyAngle(){
		body.setTransform(position, 0);
	}
	
	public void setDeath() {
		if(DEAD) return;
		
		DEAD = true;
		
		//give score
		GameScreen.getInstance().increaseScore(SCORE_VALUE);
	}
	
	public void setOffScreen(){
		if(!DEAD) return;
		
		//if(GameScreen.PLAYER_PARTICLES)
		//	killParticle.start();

		visible = false;
		body.setTransform(-bWIDTH, -bHEIGHT, 0);
		body.setLinearVelocity(0, 0);
	}
		
	public Body getBody(){
		return body;
	}
	
	public Fixture getSensorFixture(){
		return sensorFixture;
	}
	
	public Fixture getLegFixture(){
		return legFixture;
	}
	
	public Fixture getHeadFixture(){
		return headFixture;
	}
	
	public Fixture getBodyFixture(){
		return bodyFixture;
	}
	
	public void makeJump() {
		body.applyLinearImpulse(0, Jump, body.getWorldCenter().x, body.getWorldCenter().y, true);
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
		body.setLinearVelocity(0, body.getLinearVelocity().y);
	}


	public void dispose() {
		world.destroyBody(body);
		
		if(GameScreen.PLAYER_PARTICLES){
			//killParticle.dispose();
		}
	}

	public void reset() {
		position.x = startPos.x;
		position.y = startPos.y;
		
		health = HEALTH;
		
		body.setActive(true);
		DEAD = false;
		body.setTransform(position, 0);
		body.setLinearVelocity(0, 0);
		LEFT_DIRECTION = true;
		
		fixBodyAngle();
		
	}

	public void hitBullet(int damage, boolean left_dir) {
		health -= damage;
		
		
		//apply negative impulse for jerk
		int sign = (left_dir) ? -1 : 1;
		float power = (left_dir == LEFT_DIRECTION) ? 0.5f : 1.5f;
		
		if(!LEFT_DIRECTION)
		{			
			body.applyLinearImpulse(sign * power, 0.5f, body.getWorldCenter().x, body.getWorldCenter().y, true);
		}
		else{
			//apply small impulse if on same direction
			body.applyLinearImpulse(sign * power, 0.5f, body.getWorldCenter().x, body.getWorldCenter().y, true);
		}
		//MyGame.sop("Yup HIT");
		if(health <= 0)
		{
			setDeath();
			LevelGenerate.getInstance().splatterBlood(position.x, position.y);
		}
		else{
			LevelGenerate.getInstance().splatterBlood(position.x, position.y, 5);
		}
		
		LevelGenerate.getInstance().playEnemyHitSound();
	}
	
	/** interpolate slowly **/
	public void interpolateTo(float x, float y) {
		netPosition.set(x, y);
	}

	boolean UPDATE_POS = false;
	public void jumpTo(float x, float y) {
		netPosition.set(x, y);
		UPDATE_POS = true;			
		
	}
	
	public void updateStats(float x, float y, float vx, float vy){
		//if(Math.abs(position.x - x) > 0.5f || Math.abs(position.y - y) > 0.2f)
		{
			jumpTo(x,y);			
		}
		//else{
		//	interpolateTo(x,y);
		//}
		
		body.setLinearVelocity(vx, vy);
	}
	
}
