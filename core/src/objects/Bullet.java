package objects;

import Screens.GameScreen;
import utils.AssetLord;
import utils.LevelGenerate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.softnuke.epic.MyGame;

public class Bullet {

	private static Bullet _bullet = null;
	public static float Speed = 1f;
	public static int DAMAGE = Player.WEAPON_DAMAGE/2;
	public static boolean BULLET_POWER = false; //makes bullet Awesome

	float width, height;
	private Vector2 position = new Vector2();
	
	public boolean LEFT_DIRECTION = false;
	//public static int COLOR = 0;
	private static Color color = new Color(0f, 0f, 0f, 1.0f);//0 - black, 1 - blue, 2 - purple
	
	Body body;
	World world;
	PolygonShape shape;
	BodyDef bodyDef;
	Fixture bodyFixture;
	FixtureDef fixtureDef;
	public static TextureRegion wallTex;
	Texture wallTexx;
	Sprite bulletSprite, bulletPowerSprite;
	static final Vector2 Center = new Vector2(0,0);
	
	float bHEIGHT = MyGame.bHEIGHT;
	float bWIDTH = MyGame.bWIDTH;
	
	ParticleEffect effect;
	
	public boolean visible = true;
	
	public Bullet(World w){
		_bullet = this;
		
		float x = Player.getInstance().getPosition().x;
		init(w, x);		
	}
	
	public Bullet(World w, boolean alive){
		_bullet = this;
		
		float x = Player.getInstance().getPosition().x;
		visible = alive;
		
		init(w,x);		
	}
	
	private void init(World w, float x){
		world = w;
		width = 0.3f;
		height = 0.1f;
		position.x = x;
		position.y = 0;
		//generateXY();
		
		//wallTexx = GameScreen.getInstance().getAssetLord().manager.get(AssetLord.grey_tile_tex, Texture.class);
		
		//wallTex = GameScreen.getInstance().getAssetLord().manager.get(AssetLord.game_atlas, TextureAtlas.class).findRegion("shield");
		
//		if(GameScreen.PLAYER_PARTICLES){
//			effect = new ParticleEffect(GameScreen.getInstance().getAssetLord().manager.get(AssetLord.meteor_particle, ParticleEffect.class));
//			//effect.scaleEffect(MyGame.PTP);
//			effect.setPosition(posx, posy);			
//			
//			effect.setEmittersCleanUpBlendFunction(false);
//		}

		bulletSprite = new Sprite(GameScreen.getInstance().getAssetLord().manager.get(AssetLord.game_atlas, TextureAtlas.class).findRegion("bullet"));
		bulletSprite.setSize(width, height);
		bulletSprite.setOrigin(bulletSprite.getWidth()/2, bulletSprite.getHeight()/2);

		//mega power
		bulletPowerSprite = new Sprite(GameScreen.getInstance().getAssetLord().manager.get(AssetLord.game_atlas, TextureAtlas.class).findRegion("gun-power"));
		bulletPowerSprite.setSize(width*5, height*8);
		bulletPowerSprite.setOrigin(bulletSprite.getWidth()/2, bulletSprite.getHeight()/2);
		

		create();
	}
	
	private void create(){
		bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		
		bodyDef.position.set(position);
		bodyDef.bullet = true;

		//bodyDef.linearVelocity.x = Speed;
		
		shape = new PolygonShape();
		//shape.setAsBox(width/2, height/2);
		shape.setAsBox(width/2, height/2, Center, 0);
		
		fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density =  0.4f;
		fixtureDef.friction = 0;
		fixtureDef.restitution = 0.4f;		
		fixtureDef.isSensor = false;
		fixtureDef.filter.categoryBits = LevelGenerate.CATEGORY_BULLET;
		fixtureDef.filter.maskBits = (short) (LevelGenerate.CATEGORY_BADBOY | LevelGenerate.CATEGORY_WALL);
		
		body = world.createBody(bodyDef);		
		body.setGravityScale(0);
		body.setBullet(true);

		bodyFixture = body.createFixture(fixtureDef);
		
		body.setUserData("bullet");
		shape.dispose();
		
		//hide
		if(!visible)
			setOffScreen();
	}
	
	public void render(SpriteBatch batch){		
		if(!visible) return;
		
		bulletPowerSprite.setRotation(body.getAngle() * MathUtils.radiansToDegrees);
		bulletPowerSprite.setPosition(position.x , position.y - bulletPowerSprite.getHeight()/2);
		
		bulletSprite.setRotation(body.getAngle() * MathUtils.radiansToDegrees);
		bulletSprite.setPosition(position.x -bulletSprite.getWidth()/2, position.y - bulletSprite.getHeight()/2);
		
		if(LEFT_DIRECTION)
		{
			bulletSprite.setFlip(true, false);
			bulletSprite.setPosition(position.x - bulletSprite.getWidth()/2, position.y - bulletSprite.getHeight()/2);
			
			bulletPowerSprite.setFlip(true, false);
			bulletPowerSprite.setPosition(position.x - bulletPowerSprite.getWidth(), position.y - bulletPowerSprite.getHeight()/2);

		}
		else
		{
			bulletSprite.setFlip(false, false);
			bulletPowerSprite.setFlip(false, false);
		}
		
		if(BULLET_POWER)
			bulletPowerSprite.draw(batch);
		else
			bulletSprite.draw(batch);
		//batch.draw(wallTex, position.x-width/2, position.y-height/2, width, height);
		
		
		
	}
	
	public void update(float delta, float viewPosition){
		position = body.getPosition();
		
		if(position.x > viewPosition + bWIDTH*0.8 || position.x < viewPosition - bWIDTH*0.8)
		{//hide bullets which are off screen
			setOffScreen();
		}
				
		if(visible)//only move if visible
		if(LEFT_DIRECTION){
			if(body.getLinearVelocity().x > -Speed)
				body.applyLinearImpulse(-0.2f, 0, body.getWorldCenter().x, body.getWorldCenter().y, true);
		}
		else
		{
			if(body.getLinearVelocity().x < Speed)
				body.applyLinearImpulse(0.2f, 0, body.getWorldCenter().x, body.getWorldCenter().y, true);
		}
		
		//decrease bullet size
		bulletPowerSprite.setSize(bulletPowerSprite.getWidth()*0.98f, bulletPowerSprite.getHeight()*0.98f);
		bulletPowerSprite.setOrigin(bulletPowerSprite.getWidth()/2, bulletPowerSprite.getHeight()/2);

	}
	
	public void renderParticles(SpriteBatch batch, float delta){
		if(!visible) return;
		
		//effect.setPosition(posx, posy);
			
		//effect.draw(batch, delta);
	}
	
	public void reset(int whoisfiring){
		//wallTexx = AssetLord.manager.get(AssetLord.grey_tile_tex, Texture.class);
		//effect = AssetLord.manager.get(AssetLord.meteor_particle, ParticleEffect.class);
		bulletPowerSprite.setSize(width*5, height*8);
		bulletPowerSprite.setOrigin(bulletSprite.getWidth()/2, bulletSprite.getHeight()/2);

		generateXY(whoisfiring);
				
		body.setActive(true);
		body.setTransform(position, 0);
		body.setLinearVelocity(0, 0);
		body.setAngularVelocity(0);
		
		visible = true;
		
//		if(GameScreen.PLAYER_PARTICLES){
//		effect.allowCompletion();
//		effect.start();
//		}
	}
	
	/** generate xy spawn position for bullets */
	private void generateXY(int whoisfiring){
		float y = 0;
		
		if(whoisfiring == 0){
			y = Player.getInstance().getPosition().y;		
			
			position.x = Player.getInstance().getPosition().x;
			
			if(Player.getInstance().LEFT_DIRECTION)
				position.x -= Player.getInstance().getWidth();
			else
				position.x += Player.getInstance().getWidth();			
		}
		else if(whoisfiring == 1){
			if(GameScreen.MULTIPLAYER){
				y = Friend.getInstance().getPosition().y;
				
				position.x = Friend.getInstance().getPosition().x;
				
				if(Friend.getInstance().LEFT_DIRECTION)
					position.x -= Friend.getInstance().getWidth();
				else
					position.x += Friend.getInstance().getWidth();
			}
			else{
				y = Ghost.getInstance().getPosition().y;
				
				position.x = Ghost.getInstance().getPosition().x;
				
				if(Ghost.getInstance().LEFT_DIRECTION)
					position.x -= Ghost.getInstance().getWidth();
				else
					position.x += Ghost.getInstance().getWidth();
			}
		}
		
		position.y = MathUtils.random(y - 0.2f, y+0.2f);
	}
	

	public void setOffScreen(){
		//hide bodies
		visible = false;
		body.setTransform(position.x, -bHEIGHT*3, 0);
		body.setLinearVelocity(0, 0);
		body.setAngularVelocity(0);
		body.setActive(false);
		
//		if(GameScreen.PLAYER_PARTICLES){
//			effect.allowCompletion();
//		}

	}
	
	public Vector2 getPosition(){
		return position;
	}
	
	public float getWidth(){
		return width;
	}
	
	public float getHeight(){
		return height;
	}
	
	public Fixture getBodyFixture() {
		return bodyFixture;
	}

	public void dispose(){
		world.destroyBody(body);	
		
		if(GameScreen.PLAYER_PARTICLES){
		//effect.dispose();
		}
	}

	public int getDamage() {
		//mega bullets do twice the damage
		if(BULLET_POWER)
			return DAMAGE*2;
		
		return DAMAGE;
		
	}

	public void makeDead() {
		visible = false;
	}


	
}
