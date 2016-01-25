package objects;

import utils.AssetLord;
import utils.LevelGenerate;
import Screens.GameScreen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.softnuke.epic.MyGame;

public class Laser {
	
	//TODO: laser rotation
	
	float height, width;
	Color color;
	Vector2 position;
	public boolean visible = true;
	public boolean enabled = true;
	public boolean oscillate = false;
	
	public float OSCILLATION_FORCE = 0.5f;
	private float time = 0;
	Sprite laserStart, laserStartGlow, laserMiddle, laserMiddleGlow, laserEnd, laserEndGlow;
	
	public static int WHITE_COLOR = 1;
	public static int BLACK_COLOR = 2;
	public static int RED_COLOR = 3;
	public static int YELLOW_COLOR = 4;
	public static int GREEN_COLOR = 5;
	public static int BLUE_COLOR = 6;
	public static int GREY_COLOR = 7;

	TextureAtlas game_atlas;
	
	public static final short HOR = 0;
	public static final short VER = 1;
	public static final Vector2 CenterV = new Vector2(0, 0.3f);
	public static final Vector2 CenterH = new Vector2(0.3f, 0f);

	public short TYPE = VER;
	public boolean CAN_HURT = true;
	
	//used to enable and disable laser from specific switch
	public int laser_id = 0;
	
	World world;
	Body body;
	Fixture bodyFixture;
	Rectangle rect;
	
	public Laser(World w, Rectangle r, int col){
		rect = r;
		world = w;		

		switch(col){
		case 1: color = new Color(0.9f,0.9f,0.9f, 0.8f);break;
		case 2:color = new Color(0.1f,0.1f,0.1f, 0.8f);break;
		case 3:color = new Color(1f, 0.1f,0.1f, 0.8f);break;
		case 4:color = new Color(0.8f,0.8f,0.1f, 0.8f);break;
		case 5:color = new Color(0.1f,1f,0.1f, 0.8f);break;
		case 6:color = new Color(0.1f, 0.1f, 0.6f, 0.8f);break;
		case 7:color = new Color(0.51f,0.51f,0.41f, 0.8f);break;
			default: color = new Color(1f,1f,1f, 0.8f);
		}
		
		init();
	}
	
	public Laser(World w, Rectangle r, Color col){
		rect = r;
		world = w;
		color = col;
		
		init();
	}
	
	private void init() {
		//calculating laser size	            
        short dir = (rect.height > rect.width) ? Laser.VER : Laser.HOR;
        float size = (rect.height > rect.width) ? rect.height * LevelGenerate.PTP : rect.width* LevelGenerate.PTP;
		TYPE = dir;

		width = 0.5f;
		height = rect.height*LevelGenerate.PTP - width * 2;
		
		position = new Vector2(rect.x * LevelGenerate.PTP, rect.y * LevelGenerate.PTP);
        
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.KinematicBody;
		
		bodyDef.position.set(position);
		
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(width/2, height/2 + width/2, CenterV, 0);
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.restitution = 0;		
		fixtureDef.isSensor = true;
		
		fixtureDef.filter.categoryBits = LevelGenerate.CATEGORY_BADBOY;
		fixtureDef.filter.maskBits = (short) (LevelGenerate.CATEGORY_PLAYER);
		
		body = world.createBody(bodyDef);		
		bodyFixture = body.createFixture(fixtureDef);
		
		//body.setLinearVelocity(body.getLinearVelocity().x, Speed);
		body.setTransform(position.x + width/2, position.y + height/2, 0);
		
		body.setUserData("laser");
		
		shape.dispose();
		
		
		game_atlas = GameScreen.getInstance().getAssetLord().manager.get(AssetLord.game_atlas);
		
		//GameScreen.getInstance().getAssetLord().manager.get(AssetLord.light_tex, Texture.class).setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		laserStart = new Sprite(game_atlas.findRegion("laser-start"));
		laserStart.setSize(width, width * laserStart.getHeight()/laserStart.getWidth());
		laserStart.setPosition(position.x, position.y);
		laserStart.setOriginCenter();
		
		laserStartGlow = new Sprite(game_atlas.findRegion("laser-start-glow"));
		laserStartGlow.setSize(width, width * laserStartGlow.getHeight()/laserStartGlow.getWidth());
		laserStartGlow.setPosition(position.x, position.y);
		laserStartGlow.setOriginCenter();
		laserStartGlow.setColor(color);

		laserMiddle = new Sprite(game_atlas.findRegion("laser-middle"));
		laserMiddle.setSize(width, height);
		laserMiddle.setPosition(3f, 2);
		laserMiddle.setOriginCenter();
		
		laserMiddleGlow = new Sprite(game_atlas.findRegion("laser-middle-glow"));
		laserMiddleGlow.setSize(width, height);
		laserMiddleGlow.setPosition(3f, 2);
		laserMiddleGlow.setOriginCenter();
		laserMiddleGlow.setColor(color);

		laserEnd = new Sprite(game_atlas.findRegion("laser-end"));
		laserEnd.setSize(width, width * laserEnd.getHeight()/laserEnd.getWidth());
		laserEnd.setPosition(3f, 2);
		laserEnd.setOriginCenter();
		
		laserEndGlow = new Sprite(game_atlas.findRegion("laser-end-glow"));
		laserEndGlow.setSize(width, width * laserEndGlow.getHeight()/laserEndGlow.getWidth());
		laserEndGlow.setPosition(3f, 2);
		laserEndGlow.setOriginCenter();
		laserEndGlow.setColor(color);

		//light.setSize(size, size);
	}
	
	public void render(SpriteBatch batch){
		if(!visible) return;
		
		//batch.setColor(color);
		laserStart.draw(batch);
		laserStartGlow.draw(batch);
		
		if(CAN_HURT)
		{
			laserMiddle.draw(batch);
			laserMiddleGlow.draw(batch);
		}
		else
		{
			laserMiddle.draw(batch, 0.3f);
			laserMiddleGlow.draw(batch, 0.3f);
		}
		
		laserEnd.draw(batch);
		laserEndGlow.draw(batch);
		
		//batch.setColor(1f,1f,1f,1f);
	}
	
	public void update(float delta, float viewport){
		time += delta;
		
		if(enabled && position.x > viewport - MyGame.bWIDTH && position.x < viewport + MyGame.bWIDTH)
			visible = true;
		else
			visible = false;
		
		if(oscillate){
			//float lightSize = size * 0.90f + (size * 0.05f) * (float)Math.sin(delta) + (OSCILLATION_FORCE)* MathUtils.random();
			//light.setSize(lightSize, lightSize);
			//light.setPosition(position.x - lightSize/2, position.y - lightSize/2);
		//}

			float value = MathUtils.random(0.0f, 2f);
			laserStartGlow.setAlpha(MathUtils.clamp(value, 0.6f, 1));
			laserMiddleGlow.setAlpha(MathUtils.clamp(value, 0.2f, 1));
			laserEndGlow.setAlpha(MathUtils.clamp(value, 0.6f, 1));
		}
		
		//laserMiddle.setRotation(laserStart.getRotation());
		//laserMiddleGlow.setRotation(laserStart.getRotation());
		laserMiddle.setPosition(laserStart.getX(), laserStart.getY() + laserStart.getHeight());
		laserMiddleGlow.setPosition(laserMiddle.getX(), laserMiddle.getY());
		
		
		//laserEnd.setRotation(laserMiddle.getRotation());
		//laserEndGlow.setRotation(laserMiddleGlow.getRotation());
		laserEnd.setPosition(laserMiddle.getX(), laserMiddle.getY() + laserMiddle.getHeight());
		laserEndGlow.setPosition(laserEnd.getX(), laserEnd.getY());
	}
	
	public void reset() {
		CAN_HURT = true;
	}
	
	public void setColor(Color c){
		color = c;
	}
	
	public Color getColor(){
		return color;
	}
	
	public void enable(){
		enabled = true;
	}
	
	public void disable(){
		enabled = false;
	}
	
	/** makes vibrating effect**/
	public void enableOscillate(){
		oscillate = true;
	}
	
	public void disbaleOscillate(){
		oscillate = false;
	}
	
	public void setPosition(float x, float y){
		position.set(x, y);
	}
	
	public Vector2 getPosition(){
		return position;
	}
	
	public Fixture getFixture(){
		return bodyFixture;
	}
	
	public void dispose(){
		//world.destroyBody(body);
	}
}
