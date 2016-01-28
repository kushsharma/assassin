package objects;

import Screens.GameScreen;
import utils.AssetLord;
import utils.LevelGenerate;
import box2dLight.ConeLight;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.softnuke.epic.MyGame;

public class Portal {
	//variables in Tiled
	//place : entry/exit
	//type : body/sensor 
	
	float width, height;
	private Vector2 previous = new Vector2(0,0);
	private Vector2 position;
	
	Body body;
	World world;
	PolygonShape shape;
	Fixture bodyFixture, sensorFixture;
	
	Sprite rays;
	static final Vector2 Center = new Vector2(0,0);

	float bHEIGHT = MyGame.bHEIGHT;
	float bWIDTH = MyGame.bWIDTH;
	
	ConeLight cLight;
	ParticleEffect effect;
	//Light light;
	//false means not visible
	public boolean visible = false;
	public boolean ENABLED = true;
	
	//types of portal
	//0 - lowest and 2 - highest
	public static int ENTRY = 0; // default
	public static int EXIT = 1;
	
	//current type
	public int PORTAL_TYPE = ENTRY;
	
	
	public Portal(World wor, int pt, Vector2 pos, float w, float h, PolygonMapObject pmo){
		position = pos;
		height = h;
		width = w;
		PORTAL_TYPE = pt;
		
		float center = position.x + width/2;
		
		shape = new PolygonShape();
        float[] vertices = pmo.getPolygon().getTransformedVertices();

        float[] worldVertices = new float[vertices.length];
        for (int i = 0; i < vertices.length; ++i) {
        	if(i%2 == 0)
        		center = position.x;
        	else
        		center = position.y;
        		
            worldVertices[i] = (vertices[i] * LevelGenerate.PTP) - center;
        }

        shape.set(worldVertices);
		
        //check if there is any switch in this level
		if(LevelGenerate.getInstance().getSwitchCount() == 0 || PORTAL_TYPE == ENTRY)
			ENABLED = true;
		else
			ENABLED = false;
		
		init(wor);		
	}
	
	private void init(World w){
		world = w;
		//width = 1f;
		//height = 1f;
		previous.set(position.x, position.y);
		
		visible = true;
//			if(POWER_TYPE == SCORE_BONUS)
//				texRegion = new TextureRegion(GameScreen.getInstance().getAssetLord().manager.get(AssetLord.color_blocks_texture, Texture.class), 32*3, 32*1, 32, 32);
//			else if(POWER_TYPE == SHIELD)
//				texRegion = new TextureRegion(GameScreen.getInstance().getAssetLord().manager.get(AssetLord.color_blocks_texture, Texture.class), 32*2, 32*1, 32, 32);
		
		//TextureAtlas atlas = GameScreen.getInstance().getAssetLord().manager.get(AssetLord.game_atlas, TextureAtlas.class);
		
				
		if(GameScreen.PLAYER_PARTICLES){
			//change this later
			//effect = new ParticleEffect(GameScreen.getInstance().getAssetLord().manager.get(AssetLord.portal_particle, ParticleEffect.class));
			//effect.setPosition(position.x - width/4, position.y + height/2);
			//effect.start();
			//effect.setEmittersCleanUpBlendFunction(false);

		}
		
		rays = new Sprite(GameScreen.getInstance().getAssetLord().manager.get(AssetLord.game_atlas, TextureAtlas.class).findRegion("portal-rays"));
		rays.setSize(width * 3f, height*10);
		rays.setPosition(position.x - rays.getWidth()*0.52f, position.y - height/4);
		rays.setColor(0.5f, 0.5f, 0.3f,0.5f);
		create();
		
		float lx = position.x;
		float ly = position.y;
		if(PORTAL_TYPE == ENTRY)
			cLight = new ConeLight(LevelGenerate.getInstance().rayHandler, 64, Color.GOLD, 5, lx - width/2 , ly + height/4, 0, 180);
		else
			cLight = new ConeLight(LevelGenerate.getInstance().rayHandler, 64, Color.GREEN, 7, lx, ly - height/4, 90, 90);
		
		cLight.setSoft(true);
		
	}
	
	private void create(){
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.StaticBody;		
		bodyDef.position.set(position);
		
		//PolygonShape PS = (PolygonShape) shape;
		//shape.setAsBox(width/2, height/2);
		//PS.setAsBox(width/2, height/2, Center, 0);
		
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density =  0.0f;
		fixtureDef.friction = 0.2f;
		fixtureDef.restitution = 0;		
		fixtureDef.isSensor = false;
		
		fixtureDef.filter.categoryBits = LevelGenerate.CATEGORY_WALL;
		fixtureDef.filter.maskBits = (short) (LevelGenerate.CATEGORY_PLAYER | LevelGenerate.CATEGORY_BULLET);
		
		body = world.createBody(bodyDef);		
		bodyFixture = body.createFixture(fixtureDef);
		
		PolygonShape PS = new PolygonShape();
		PS.setAsBox(width/2, height/2, Enemy.CENTER_VECTOR, 0);
		
		fixtureDef.shape = PS;
		fixtureDef.isSensor = true;
		fixtureDef.filter.categoryBits = LevelGenerate.CATEGORY_WALL;
		fixtureDef.filter.maskBits = LevelGenerate.CATEGORY_PLAYER;		
		sensorFixture = body.createFixture(fixtureDef);
		
		body.setUserData("portal");		
		shape.dispose();
		PS.dispose();
		
	}
	
	public void render(SpriteBatch batch){	
		
		if(!visible)
			return;
		
		
		//check if it is gone off the screen without user consumption
		//if(body.getPosition().y+height < -bWIDTH)
		//	setOffScreen(false);
		
		//batch.draw(texRegion, position.x-width/2, position.y-height/2, 0, 0, width, height, 1f, 1f, 0);
		

	}
	
	public void renderParticles(SpriteBatch batch){
		if(!visible) return;
		
		if(ENABLED)
		{
			//effect.draw(batch);
			//rays.draw(batch, 0.6f);
		}
		
	}
	
	public void update(float delta, float viewportWidth){
		position = body.getPosition();
		
		if(position.x > viewportWidth-bWIDTH*0.8 && position.x < viewportWidth+bWIDTH*0.8)
			visible = true;
		else
			visible = false;
		
		if(ENABLED)
			cLight.setColor(Color.LIGHT_GRAY);
		else
			cLight.setColor(Color.RED);
		//if(ENABLED && GameScreen.PLAYER_PARTICLES)		
		//	effect.update(delta);
	}
	
	public void reset(){
	
		visible = true;
		effect.start();
		
		if(LevelGenerate.getInstance().getLevelSwitch() != null)
			ENABLED = false;
		else
			ENABLED = true;
		
		if(true)
		return;
		
		
	}
	
	public Fixture getSensorFixture() {
		return sensorFixture;
	}
	
	public Fixture getFixture(){
		return bodyFixture;
	}
			
	public void setOffScreen(boolean collision){
		//hide bodies
		
		visible = false;
		
				
		if(GameScreen.PLAYER_PARTICLES){
			//effect.allowCompletion();
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
	
	public void dispose(){
		world.destroyBody(body);
		
		if(GameScreen.PLAYER_PARTICLES){
			//effect.dispose();
		}
	}
}