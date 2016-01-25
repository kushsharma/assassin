package objects;

import Screens.GameScreen;
import utils.AssetLord;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.softnuke.epic.MyGame;

public class Light {
	//variables in Tiled
	//color : 1-7
	
	float height, width, size;
	Color color;
	Vector2 position;
	public boolean visible = true;
	public boolean enabled = true;
	public boolean oscillate = false;
	
	public float OSCILLATION_FORCE = 0.5f;
	private float time = 0;
	Sprite light;
	
	public static int WHITE_COLOR = 1;
	public static int BLACK_COLOR = 2;
	public static int RED_COLOR = 3;
	public static int YELLOW_COLOR = 4;
	public static int GREEN_COLOR = 5;
	public static int BLUE_COLOR = 6;
	public static int GREY_COLOR = 7;

	
	public Light(Vector2 pos){
		position = pos;
		color = new Color(1f, 1f, 1f, 0.8f);
		size = 5f;
		
		init();
	}

	public Light(Vector2 pos, float s, Color col){
		position = pos;
		color = col;
		size = s;
		
		init();
	}
	
	public Light(Vector2 pos, float s, int col){
		position = pos;
		size = s * 6f;
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
	
	private void init() {
		
		
		//GameScreen.getInstance().getAssetLord().manager.get(AssetLord.light_tex, Texture.class).setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		light = new Sprite(GameScreen.getInstance().getAssetLord().manager.get(AssetLord.light_tex, Texture.class));
		light.setPosition(position.x - size/2, position.y - size/2);
		light.setSize(size, size);
		light.setColor(color);
	}
	
	public void render(SpriteBatch batch){
		if(!visible) return;
		
		//batch.setColor(color);
		light.draw(batch, 1f);
		
		//batch.setColor(1f,1f,1f,1f);
	}
	
	public void update(float delta, float viewport){
		time += delta;
		
		if(enabled && position.x > viewport - MyGame.bWIDTH - size && position.x < viewport + MyGame.bWIDTH + size)
			visible = true;
		else
			visible = false;
		
		if(oscillate){
			float lightSize = size * 0.90f + (size * 0.05f) * (float)Math.sin(delta) + (OSCILLATION_FORCE)* MathUtils.random();
			light.setSize(lightSize, lightSize);
			light.setPosition(position.x - lightSize/2, position.y - lightSize/2);
		}

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
	
	public void dispose(){
		
	}
}
