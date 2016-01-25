package com.softnuke.epic;

import Screens.GameScreen;
import Screens.SplashScreen;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import utils.AssetLord;

public class MyGame extends Game {
	public static PlatformSpecific platform = null;


	public static int HEIGHT, WIDTH;
	public static int bHEIGHT, bWIDTH;

	public static float ASPECT_RATIO;
	public static float  PTP = 0.015f;//pixel to point

	AssetLord Assets = new AssetLord();

	public void MyGame(){
		platform = null;
	}

	public void MyGame(PlatformSpecific pl){
		platform = pl;
	}

	@Override
	public void create () {

		WIDTH = Gdx.graphics.getWidth();
		HEIGHT = Gdx.graphics.getHeight();

		ASPECT_RATIO =  ((float) WIDTH / (float) HEIGHT);

		bWIDTH = 20;
		bHEIGHT = (int) (bWIDTH * 1/ASPECT_RATIO);
		//PTP = (float)bWIDTH/(float)WIDTH;

		setScreen(new SplashScreen(this, Assets));

	}

	public static PlatformSpecific getPlatformResolver() {
		return platform;
	}

	public static void setPlatformSpecific(PlatformSpecific platformResolver) {
		platform = platformResolver;
	}

	@Override
	public void render () {
		super.render();
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void resume() {
		super.resume();

		Assets.finishLoading();
		Texture.setAssetManager(Assets.manager);
		//Gdx.app.log("JUMPDEBUG RESU","W:"+Gdx.graphics.getWidth()+", H:"+Gdx.graphics.getHeight());

	}

	@Override
	public void dispose() {
		super.dispose();

		//dispose all the textures and assets
		Assets.dispose();
	}

	@Override
	public void resize(int width, int height){
		super.resize(width, height);

		MyGame.HEIGHT = height;
		MyGame.WIDTH = width;

		MyGame.ASPECT_RATIO =  ((float) MyGame.WIDTH / (float) MyGame.HEIGHT);

		MyGame.bWIDTH = 20;
		MyGame.bHEIGHT = (int) (bWIDTH * 1/MyGame.ASPECT_RATIO);

		//PTP = (float)bWIDTH/(float)WIDTH;

		//Gdx.app.log("JUMPDEBUG RESI","W:"+Gdx.graphics.getWidth()+", H:"+Gdx.graphics.getHeight());
		//Gdx.app.log("JUMPDEBUG RESI G","W:"+width+", H:"+height);

	}

	//for debug
	public static void sop(String s){
		if(GameScreen.SOFT_DEBUG)
			System.out.println(s);
	}
	//for debug
	public static void sop(float f){
		if(GameScreen.SOFT_DEBUG)
			System.out.println(String.valueOf(f));
	}
}
