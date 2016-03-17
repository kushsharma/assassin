package utils;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader.ParticleEffectParameter;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.assets.loaders.SkinLoader.SkinParameter;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader.FreeTypeFontLoaderParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ObjectMap;
import com.softnuke.epic.MyGame;

public class AssetLord {


	public AssetManager manager = new AssetManager();
	
	public static final String bullet_tex = "level/bullet.png";
	public static final String bullet_power_tex = "level/gun-power.png";
	public static final String button_left_tex = "button-left.png";
	public static final String button_right_tex = "button-right.png";
	public static final String button_up_tex = "button-up.png";
	public static final String button_fire_tex = "button-fire.png";
	public static final String light_tex = "level/light.png";
	public static final String pause_back_tex = "level/pause_back_dark.png";
	public static final String menu_back_tex = "level/sword-background.png";
	public static final String scan_line_tex = "level/Scanlines.png";

	public static final String player_jump_particle = "particles/player_jump.p";
	public static final String enemy_kill_particle = "particles/enemy_kill.p";
	public static final String portal_particle = "particles/portal_rays.p";
	public static final String gravity_rev_particle = "particles/gravity_reverser.p";
	public static final String beamspot_particle = "particles/beamSpot.p";
	public static final String intro_somke_particle = "particles/intro_smoke.p";
	public static final String power_charge_particle = "particles/power_charge.p";
	public static final String dashed_particle = "particles/dashed.p";
	public static final String dashed_left_particle = "particles/dashed_flip.p";

//	public static final String background_shader_tex = "final/grid.png";//tiles_back2
//	public static final String lazer_music = "lazer.mp3";
//	public static final String slowdown_sound = "slowdown.mp3";
//	public static final String left_spark_particle = "particles/leftPortal.p";
//	public static final String right_spark_particle = "particles/rightPortal.p";
	public static final String square_floor_particle = "particles/square_floor.p";
//	public static final String powerup_particle = "particles/powerup.p";
//	public static final String meteor_particle = "particles/meteor.p";
//	
//	public static final String player_explode_particle = "particles/explode.p";

	public static final String gothic_font = "fonts/GOTHIC.TTF";
	public static final String mecha_font = "fonts/Mecha.ttf";
	public static final String joystix_font = "fonts/joystixmonospace.ttf";
	public static final String tiny_font = "size6.ttf";
	public static final String small_font = "size12.ttf";
	public static final String medium_font = "size20.ttf";
	public static final String large_font = "size30.ttf";
	
//	public static final String ui_atlas = "level/atlas.pack";
	//public static final String level_atlas = "level/pack/jump.pack";
//	public static final String ui_skin = "level/menu_skin.json";
	//public static final String pause_back_tex = "level/pause_back.png";
//	public static final String wall_tex = "final/wall-rgb-256.png";
//	public static final String menu_back_tex = "final/gradient-abstract-hd.jpg";
	
	public static final String menu_atlas = "atlas/menu.pack";
	public static final String menu_skin = "atlas/uiskin.json";
	public static final String game_atlas = "atlas/game.pack";
//	public static final String tutorial1_tex = "final/tutorial/tutorial-1.png";
//	public static final String tutorial2_tex = "final/tutorial/tutorial-2.png";
//	public static final String tutorial3_tex = "final/tutorial/tutorial-3.png";
//	public static final String tutorial4_tex = "final/tutorial/tutorial-4.png";
	
	public static final String fire_sound = "sound/Laser_Shoot.mp3";
	public static final String finish_sound = "sound/lose_0.mp3";
	public static final String epicLevelup_sound = "sound/epic-levelup.mp3";
	public static final String coin_sound = "sound/Pickup_Coin.mp3";
	public static final String player_hurt_sound = "sound/Player_Hit_Hurt.mp3";
	public static final String enemy_hurt_sound = "sound/Enemy_Hit_Hurt.mp3";
	public static final String levelup_sound = "sound/Level_Up.mp3";
	public static final String portal_sound = "sound/Portal.wav";
	public static final String game_music = "sound/rinse.mp3";
	//public static final String menu_music = "sound/EvilMenu.mp3";

	private boolean resized = false;
	
	public void load(){
		
		//textures
//		manager.load(background_shader_tex, Texture.class);
		manager.load(light_tex, Texture.class);
		manager.load(pause_back_tex, Texture.class);
		manager.load(menu_back_tex, Texture.class);
		manager.load(scan_line_tex, Texture.class);


		//manager.load(button_left_tex, Texture.class);


		
		//music
		manager.load(game_music, Music.class);		
		
		//sound
		manager.load(fire_sound, Sound.class);	
		manager.load(finish_sound, Sound.class);	
		manager.load(epicLevelup_sound, Sound.class);	
		manager.load(coin_sound, Sound.class);	
		manager.load(player_hurt_sound, Sound.class);	
		manager.load(enemy_hurt_sound, Sound.class);	
		manager.load(levelup_sound, Sound.class);	
		manager.load(portal_sound, Sound.class);	

		
		//fonts

		// set the loaders for the generator and the fonts themselves
		
		FileHandleResolver resolver = new InternalFileHandleResolver();
		manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

		// load to fonts via the generator (implicitely done by the FreetypeFontLoader).
		// Note: you MUST specify a FreetypeFontGenerator defining the ttf font file name and the size
		// of the font to be generated. The names of the fonts are arbitrary and are not pointing
		// to a file on disk!
		
		float density = Math.min(1.8f, Gdx.graphics.getDensity());
		density = Math.max(density, 1.2f);
		if(MyGame.WIDTH <= 900)
			density = 1.2f;
		
		//density = Gdx.graphics.getDensity();
		//density = (float) 1280 / (float)MyGame.WIDTH;
		//Gdx.app.log(density+","+MyGame.WIDTH, "dens");
		
		FreeTypeFontLoaderParameter size1Params = new FreeTypeFontLoaderParameter();
		size1Params.fontFileName = joystix_font;
		//size1Params.fontParameters.size = (int) (16 * density);
		size1Params.fontParameters.size = (int) (MyGame.HEIGHT/32f);
		manager.load(small_font, BitmapFont.class, size1Params);
		
		FreeTypeFontLoaderParameter size4Params = new FreeTypeFontLoaderParameter();
		size4Params.fontFileName = joystix_font;
		//size1Params.fontParameters.size = (int) (16 * density);
		size4Params.fontParameters.size = (int) (MyGame.HEIGHT/64f);
		manager.load(tiny_font, BitmapFont.class, size4Params);

		FreeTypeFontLoaderParameter size2Params = new FreeTypeFontLoaderParameter();
		size2Params.fontFileName = mecha_font;
		//size2Params.fontParameters.size = (int) (32 * density);
		size2Params.fontParameters.size = (int) (MyGame.HEIGHT/16f);
		manager.load(medium_font, BitmapFont.class, size2Params);
		
		FreeTypeFontLoaderParameter size3Params = new FreeTypeFontLoaderParameter();
		size3Params.fontFileName = gothic_font;
		//size3Params.fontParameters.size = (int) (80 * density);
		size3Params.fontParameters.size = (int) (MyGame.HEIGHT/8f);
		manager.load(large_font, BitmapFont.class, size3Params);



		//atlas

		manager.load(menu_atlas, TextureAtlas.class);
		manager.load(game_atlas, TextureAtlas.class);		
		
		//particles
		ParticleEffectParameter pep = new ParticleEffectParameter();
		//pep.imagesDir = Gdx.files.internal("particles/");
		pep.atlasFile = game_atlas;
		manager.load(player_jump_particle, ParticleEffect.class, pep);
		manager.load(enemy_kill_particle, ParticleEffect.class, pep);
		manager.load(power_charge_particle, ParticleEffect.class, pep);
		
		//TODO: fix this to atlas
		//ParticleEffectParameter pepAAA = new ParticleEffectParameter();
		//pepAAA.imagesDir = Gdx.files.internal("particles/");
		manager.load(dashed_particle, ParticleEffect.class, pep);
		manager.load(dashed_left_particle, ParticleEffect.class, pep);

		
		//TODO:make them fetch from different atlas
//		manager.load(square_floor_particle, ParticleEffect.class, pep);

		
		ParticleEffectParameter pepG = new ParticleEffectParameter();
		//pepG.imagesDir = Gdx.files.internal("particles/");
		pepG.atlasFile = menu_atlas;
		manager.load(square_floor_particle, ParticleEffect.class, pep);
//		manager.load(left_spark_particle, ParticleEffect.class, pepG);

				
		

		//manager.load(portal_sound, Sound.class);
		
		
//		manager.load(menu_music, Music.class);

	}
	
	public void finishLoading(){
		manager.finishLoading();
		
		manager.get(tiny_font, BitmapFont.class).setUseIntegerPositions(true);
		manager.get(small_font, BitmapFont.class).setUseIntegerPositions(true);
		manager.get(medium_font, BitmapFont.class).setUseIntegerPositions(true);
		manager.get(large_font, BitmapFont.class).setUseIntegerPositions(true);
		
		//manager.get(menu_music, Music.class).setVolume(0.9f);
		//manager.get(menu_music, Music.class).setLooping(true);

		
		if(resized) return;
		manager.get(player_jump_particle, ParticleEffect.class).scaleEffect(MyGame.PTP*0.7f);
		manager.get(enemy_kill_particle, ParticleEffect.class).scaleEffect(MyGame.PTP);
		manager.get(power_charge_particle, ParticleEffect.class).scaleEffect(MyGame.PTP*0.6f);
		manager.get(dashed_particle, ParticleEffect.class).scaleEffect(MyGame.PTP*0.3f);
		manager.get(dashed_left_particle, ParticleEffect.class).scaleEffect(MyGame.PTP*0.3f);

		//manager.get(gravity_rev_particle, ParticleEffect.class).scaleEffect(MyGame.PTP * 0.75f);
		//manager.get(beamspot_particle, ParticleEffect.class).scaleEffect(MyGame.PTP);

//		manager.get(right_spark_particle, ParticleEffect.class).scaleEffect(1 * MyGame.PTP * 0.5f);

//		manager.get(back_stars_particle, ParticleEffect.class).scaleEffect(MyGame.PTP * 2f);
		manager.get(square_floor_particle, ParticleEffect.class).scaleEffect(2);

		
		resized = true;
	}
	
	public void dispose(){
		manager.dispose();
		manager = null;
	}
}
