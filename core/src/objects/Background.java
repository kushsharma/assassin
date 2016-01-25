package objects;

import Screens.GameScreen;
import utils.AssetLord;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.softnuke.epic.MyGame;

public class Background {
//fix this shit
	//may contain bugs
	
	public static int TOTAL_SPRITES = 10;
	
	private float x;
	private float y;
	private float vx = -0.2f;
	private float vy;
	private float bWIDTH, bHEIGHT;
	private float WIDTH, HEIGHT;
	
	TextureRegion[] layers;
	ParallaxCamera paxCamera;
	OrthographicCamera mainCam;
	OrthographicCamera camLayer1, camLayer2;
	Sprite backSprite, buildLightSprite, cbuildLightSprite, buildDarkSprite, cbuildDarkSprite;
	
	Array<Sprite> paxLayer1 = new Array<Sprite>();
	Array<Sprite> paxLayer2 = new Array<Sprite>();

	Texture layer1,layer2,layer3;
		
	public Background(OrthographicCamera cam ){

		bHEIGHT = MyGame.bHEIGHT;
		bWIDTH = MyGame.bWIDTH;
		WIDTH = MyGame.WIDTH;
		HEIGHT = MyGame.HEIGHT;
		
		mainCam = cam;
		
		paxCamera = new ParallaxCamera(false, bWIDTH, bHEIGHT);
	
		paxCamera.position.set(paxCamera.viewportWidth/2, paxCamera.viewportHeight/2, 0);
		paxCamera.update();
		
		
		this.init();
	}
	
	public void init(){
		this.x = 0;
		this.y = 0;
		
		
		
		
		backSprite = new Sprite(GameScreen.getInstance().getAssetLord().manager.get(AssetLord.game_atlas, TextureAtlas.class).findRegion("pixel-back"));
		//backSprite.flip(false, true);
		backSprite.setPosition(0, 0);
		backSprite.setSize(WIDTH, HEIGHT);
		
		buildDarkSprite  = new Sprite(GameScreen.getInstance().getAssetLord().manager.get(AssetLord.game_atlas, TextureAtlas.class).findRegion("back-small-front"));
		buildDarkSprite.setPosition(-bWIDTH/2, -bHEIGHT*0.6f);
		buildDarkSprite.setSize(bHEIGHT * buildDarkSprite.getWidth()/buildDarkSprite.getHeight(), bHEIGHT);

		buildLightSprite = new Sprite(GameScreen.getInstance().getAssetLord().manager.get(AssetLord.game_atlas, TextureAtlas.class).findRegion("back-small-mid"));
		buildLightSprite.setPosition(-bWIDTH/2, -bHEIGHT*0.6f);
		buildLightSprite.setSize(bHEIGHT * buildLightSprite.getWidth()/buildLightSprite.getHeight(), bHEIGHT);
		
		
		
		for(int i = 0;i<TOTAL_SPRITES;i++){
			buildDarkSprite.setPosition(-bWIDTH + (i * buildDarkSprite.getWidth()), buildDarkSprite.getY());
			paxLayer1.add(new Sprite(buildDarkSprite));
		}
		
		for(int i = 0;i<TOTAL_SPRITES;i++){
			buildLightSprite.setPosition(-bWIDTH + (i * buildLightSprite.getWidth()), buildLightSprite.getY());
			paxLayer2.add(new Sprite(buildLightSprite));
		}
		
		
		
	}
	
	Vector3 temp = new Vector3(0,0,0);
	float time = 0;
	public void draw(SpriteBatch batch){
		
		batch.disableBlending();		
		batch.setProjectionMatrix(GameScreen.getInstance().cameraui.combined);
		batch.begin();
		backSprite.draw(batch);
		
		//TODO
		batch.end();
		batch.enableBlending();

		//batch.setColor(1f,1f,1f,1f);
		if(GameScreen.BACKGROUND_PARALLAX){
			batch.setProjectionMatrix(paxCamera.calculateParallaxMatrix(0.2f, 0f));
			batch.begin();
			
			for(Sprite s:paxLayer2)
			{
				if(s.getX() - bWIDTH/2 < paxCamera.position.x*0.2 && s.getX() + s.getWidth() + bWIDTH/2 > paxCamera.position.x*0.2)
					s.draw(batch);		
			}
			
			batch.setProjectionMatrix(paxCamera.calculateParallaxMatrix(0.4f, 0f));
			
			
			for(Sprite s:paxLayer1)
			{	
				
				if(s.getX() - bWIDTH/2 < paxCamera.position.x*0.4 && s.getX() + s.getWidth() + bWIDTH/2 > paxCamera.position.x*0.4)
					s.draw(batch);		
			}
			
			batch.end();
		}
		

	}
	
	public void update(float delta){
		
		paxCamera.position.set(mainCam.position.x, mainCam.position.y, 0);
		
		
	}
	
	public void dispose(){
		paxLayer1.clear();
		paxLayer2.clear();
				
	}
	
	class ParallaxCamera extends OrthographicCamera {
		Matrix4 parallaxView = new Matrix4();
		Matrix4 parallaxCombined = new Matrix4();
		Vector3 tmp = new Vector3();
		Vector3 tmp2 = new Vector3();

		public ParallaxCamera (float viewportWidth, float viewportHeight) {
			super(viewportWidth, viewportHeight);
		}
		
		public ParallaxCamera (boolean yDown, float viewportWidth, float viewportHeight) {
			super();
			this.setToOrtho(yDown, viewportWidth, viewportHeight);
		}

		public Matrix4 calculateParallaxMatrix (float parallaxX, float parallaxY) {
			update();
			tmp.set(position);
			tmp.x *= parallaxX;
			tmp.y *= parallaxY;

			parallaxView.setToLookAt(tmp, tmp2.set(tmp).add(direction), up);
			parallaxCombined.set(projection);
			
			Matrix4.mul(parallaxCombined.val, parallaxView.val);
			return parallaxCombined;
		}
	}
}
