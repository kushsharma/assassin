package utils;

import Screens.GameScreen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
import com.softnuke.epic.MyGame;

public class BloodManager {

	World world;
	Array<Blood> bloodActivePool;
	Array<Blood> bloodPool;
	static final int MAX_BLOOD = 50;
	static final int SINGLE_BLOOD_COUNT = 10;
	
	//used for queuing splatter
	boolean SPLATTER_QUEUED = false;
	float Qx, Qy;
	int Qcount;
	
	public BloodManager(World w){
		world = w;
		bloodActivePool = new Array<Blood>();
		bloodPool = new Array<Blood>();
		
		for(int i=0;i<30;i++){						
			bloodPool.add(new Blood(world));
		}
		
	}
	
	public void render(SpriteBatch batch){
		
		for(Blood b:bloodActivePool){
			b.render(batch);
		}
		
	}
	
	public void update(float delta){
		//update & check for dead blood and recycle
		int size = bloodActivePool.size;
		while(--size >= 0){
			Blood b = bloodActivePool.get(size);
			
			b.update(delta);
			
			if(b.DEAD == true)
			{
				bloodActivePool.removeIndex(size);
				bloodPool.add(b);
			}
		}
		
		if(SPLATTER_QUEUED){
			splatter(Qx, Qy, Qcount);
			SPLATTER_QUEUED = false;
		}
	}
	
	/** World.step safe :) */
	public void queueSplatter(float x, float y, int count){
		Qx = x;
		Qy = y;
		Qcount = count;
		
		SPLATTER_QUEUED = true;
	}
	
	/**clean up active pool*/
	public void reset(){
		//MyGame.sop("Reseting active blood");
		
		int size = bloodActivePool.size;
		while(--size >= 0){
			Blood b = bloodActivePool.get(size);
						
			b.setDead();
			bloodActivePool.removeIndex(size);
			bloodPool.add(b);
		}
		bloodActivePool.clear();
	}
	
	/** Cause blood explosion */
	public void splatter(float x, float y, int count){
		//MyGame.sop("Splatter at"+x+","+y);
		
		int bc = Math.min(count, SINGLE_BLOOD_COUNT);
		
		//10 particles for blood for splatter
		for(int i=0; i < bc;i++){
			
			if(bloodPool.size > 0)
			{//get from dead pool
				Blood b = bloodPool.get(0);
				bloodPool.removeIndex(0);
				
				b.reset(x,y);
				//b.LEFT_DIRECTION = Player.getInstance().LEFT_DIRECTION;
				
				bloodActivePool.add(b);			
			}
			else
			{
				
				Blood blood = null;
				
				//add more blood if its already not on max
				int bloods = bloodActivePool.size + bloodPool.size;
				if(bloods < MAX_BLOOD){
					//create more blood objects
					
					int mx = Math.min(MAX_BLOOD - bloods , 5);
					while(mx-- > 0){
						bloodPool.add(new Blood(world));
					}
					
					//create blood active
					blood = bloodPool.get(0);
					bloodPool.removeIndex(0);			
					
				}
				else{
					//get from old blood
					blood = bloodActivePool.get(0);
					bloodActivePool.removeIndex(0);					
				}
												
				blood.reset(x,y);				
				bloodActivePool.add(blood);	
			}
		}
		
		
	}
	
	public void dispose(){
		bloodPool.clear();
		bloodActivePool.clear();
	}
	
}

class Blood{
	
	float width = 0.2f;
	float height = 0.2f;
	boolean visible = true;
	boolean DEAD = false;
	
	Body body;
	Fixture fixture;
	Sprite bloodSprite;
	float timeLived = 0;
	static final float LIFE_DURATION = 3f;
	static final float IMPULSE_POWER = 0.015f;
	
	float impulseX, impulseY;
	World world;
	
	public Blood(World w){
		
		world = w;
		float ran = MathUtils.random(0, 0.1f);
		width -= ran;
		height -= ran;
		
		BodyDef bodyDef = new BodyDef();
		PolygonShape shape = new PolygonShape();
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;

		//blood
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(0, 0);
		bodyDef.allowSleep = true;
		bodyDef.fixedRotation = true;
		
		shape.setAsBox(width/2f, height/2f);		
		fixtureDef.restitution = 0;		
		fixtureDef.isSensor = false;
		fixtureDef.density = 0.2f;
		fixtureDef.friction = 0.8f;
		
		fixtureDef.filter.categoryBits = LevelGenerate.CATEGORY_UTILS;
		fixtureDef.filter.maskBits = LevelGenerate.CATEGORY_WALL;
		
		body = world.createBody(bodyDef);
		fixture = body.createFixture(fixtureDef);
					
		body.setActive(false);
		visible = false;
		DEAD = true;
		
		body.setUserData("blood");
		
		shape.dispose();
		
		bloodSprite = new Sprite(GameScreen.getInstance().getAssetLord().manager.get(AssetLord.game_atlas, TextureAtlas.class).findRegion("white"));
		bloodSprite.setSize(width, height);
	}
	
	Color color = new Color(0.4f, 0.5f, 0.5f, 1);
	
	public void render(SpriteBatch batch){
		if(!visible) return;
		
		bloodSprite.setColor(color);
		bloodSprite.setPosition(body.getPosition().x - width/2,  body.getPosition().y - height/2);
		bloodSprite.draw(batch);
		
	}
	
	public void update(float delta){
		if(DEAD) return;
		
		timeLived += delta;
		
		if(timeLived > LIFE_DURATION){
			setDead();
		}
		
	}
	
	/** Don't do this in middle of world step */
	public void reset(float x, float y){
		visible = true;
		timeLived = 0;
		DEAD = false;
		
		body.setActive(true);
		
		impulseX = MathUtils.random(-IMPULSE_POWER, IMPULSE_POWER);
		impulseY = MathUtils.random(0, IMPULSE_POWER);
		
		body.setTransform(x, y, 0);
		
		body.applyLinearImpulse(impulseX, impulseY, 0, 0, true);
	}
	
	/** Don't do this in middle of world step */
	public void setDead(){
		//queue this blood for cleanup
		visible = false;
		DEAD = true;
		cleanUp();
	}
	
	//will make this body inactive
	public void cleanUp(){
		body.setActive(false);
	}
	
}
