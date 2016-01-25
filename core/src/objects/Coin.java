package objects;

import Screens.GameScreen;
import utils.AssetLord;
import utils.LevelGenerate;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.softnuke.epic.MyGame;

public class Coin {

	//variables for Tiled maps
	// type : 1/2
	
	float width, height;
	public float Speed = 0.4f;
	Vector2 position;
	float flyDistance = 0.3f;
	private Vector2 fixed;

	Body body;
	World world;
	PolygonShape shape;
	BodyDef bodyDef;
	FixtureDef fixtureDef;
	Fixture bodyFixture;
	TextureRegion texRegion;
	static final Vector2 Center = new Vector2(0,0);

	float bHEIGHT = MyGame.bHEIGHT;
	float bWIDTH = MyGame.bWIDTH;
	
	ParticleEffect effect;
	Light light;
	Animation coinAnime;
	Sprite coinSprite;
	
	float time = 0;
	
	//false means not visible
	public boolean visible = false;
	public boolean consumed = false;

	//types of power
	//0 - lowest and 2 - highest
	public static int NORMAL = 1; // default
	public static int MEGA = 2;
	public static int HIDDEN = 3;
	
	//current type
	public int COIN_TYPE = NORMAL;	
	
	public Coin(World w, Vector2 pos, int t, Light l){
		COIN_TYPE = t;
		world = w;
		position = pos;
		light = l;
		
		init(w);		
	}
	
	private void init(World w){
		world = w;
		width = 0.4f;
		height = 0.4f;
		fixed = position.cpy();
		
		consumed = false;
		visible = true;
		
		//Texture tex = new Texture("level/milk-power.png");
		//tex.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		
		if(COIN_TYPE == NORMAL)
			texRegion = GameScreen.getInstance().getAssetLord().manager.get(AssetLord.game_atlas, TextureAtlas.class).findRegion("milk-power");
			//texRegion = atlas.findRegion("level1-power");
		else if(COIN_TYPE == MEGA)
			texRegion = GameScreen.getInstance().getAssetLord().manager.get(AssetLord.game_atlas, TextureAtlas.class).findRegion("milk-power");
		
		TextureRegion[] coinSheet = new TextureRegion[1];
		coinSheet[0] = texRegion;
		
		coinAnime = new Animation(0.1f, coinSheet);
		coinAnime.setPlayMode(PlayMode.LOOP);
		
		coinSprite = new Sprite();
		coinSprite.setSize(width, height);
		coinSprite.setPosition(position.x - width/2, position.y - height/2);
		
		
		if(GameScreen.PLAYER_PARTICLES){
			//change this later
			effect = new ParticleEffect(GameScreen.getInstance().getAssetLord().manager.get(AssetLord.enemy_kill_particle, ParticleEffect.class));
			//effect.scaleEffect(MyGame.PTP);
			effect.setPosition(position.x, position.y);
		
			effect.setEmittersCleanUpBlendFunction(false);

		}
		
		create();
	}
	
	private void create(){
		bodyDef = new BodyDef();
		bodyDef.type = BodyType.KinematicBody;
		
		bodyDef.position.set(position);
		
		shape = new PolygonShape();
		shape.setAsBox(width/2, height/2, Center, 0);
		
		fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.restitution = 0;		
		fixtureDef.isSensor = true;
		
		fixtureDef.filter.categoryBits = LevelGenerate.CATEGORY_POWERUP;
		fixtureDef.filter.maskBits = (short) (LevelGenerate.CATEGORY_PLAYER);
		
		body = world.createBody(bodyDef);		
		bodyFixture = body.createFixture(fixtureDef);
		
		body.setLinearVelocity(body.getLinearVelocity().x, Speed);
		
		body.setUserData("coin");
		
		shape.dispose();
		
	}
	
	public void render(SpriteBatch batch){		
		//posx = body.getPosition().x;
		//posy = body.getPosition().y;		

		if(!visible || COIN_TYPE == HIDDEN)
			return;
		
		//check if it is gone off the screen without user consumption
		//if(body.getPosition().y+height < -bWIDTH)
		//	setOffScreen(false);
		
		//batch.draw(texRegion, position.x-width/2, position.y-height/2, 0, 0, width, height, 1f, 1f, 0);
		
		coinSprite.setPosition(position.x - width/2, position.y - height/2);
		coinSprite.setRegion(coinAnime.getKeyFrame(time));
		
		coinSprite.draw(batch);
	}
	
	public void renderParticles(SpriteBatch batch){
		effect.draw(batch);

	}
	
	public void update(float delta, float viewportWidth){
		time += delta;
		
		position = body.getPosition();
		
		if(!consumed && position.x > viewportWidth-bWIDTH*0.8 && position.x < viewportWidth+bWIDTH*0.8)
			visible = true;
		else
			visible = false;
		
		//make it hover		
		if((fixed.y - height/2 + flyDistance) - body.getPosition().y < 0.00002f)
			body.setLinearVelocity( body.getLinearVelocity().x , -Speed);
		else if(body.getPosition().y - (fixed.y + height/2 - flyDistance) < 0.00002f)
			body.setLinearVelocity( body.getLinearVelocity().x , Speed);
		
		if(GameScreen.PLAYER_PARTICLES)
		effect.update(delta);
	}
	
	public void reset(){
	
		visible = true;
		consumed = false;

	}
	
	/**consume power and return its type**/
	public int consume(){
		if(!consumed)
		{
			visible = false;
			consumed = true;
			if(light != null)
			light.disable();
			
			if(GameScreen.PLAYER_PARTICLES)
			effect.start();
			
			LevelGenerate.getInstance().playCoinSound();
			
			return COIN_TYPE * 20;
		}
		else
			return 0;
	}
		
	public Fixture getFixture(){
		return bodyFixture;
	}
		
	public void setOffScreen(boolean collision){
		//hide bodies
		
		visible = false;
		
		if(GameScreen.PLAYER_PARTICLES){
		effect.allowCompletion();
		}
		

	}
	
//	public void updatePreviousPos() {
//		previous = body.getPosition();
//	}
//	
//	public void interpolate(float alpha, float invAlpha){
//		Vector2 pos = body.getPosition();
//		posx = pos.x * alpha + previous.x * invAlpha;
//		posy = pos.y * alpha + previous.y * invAlpha;	
//	}
	
	public float getY(){
		return position.y;
	}
	
	public float getX(){
		return position.x;
	}
	
	public float getWidth(){
		return width;
	}
	
	public float getHeight(){
		return height;
	}
	
	public float getflyDistance(){
		return flyDistance;
	}
	
	public void dispose(){
		world.destroyBody(body);
		
		if(GameScreen.PLAYER_PARTICLES){
			effect.dispose();
		}
	}
}
