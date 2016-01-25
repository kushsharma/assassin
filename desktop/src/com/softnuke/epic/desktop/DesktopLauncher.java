package com.softnuke.epic.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.softnuke.epic.MyGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Epic";
		//config.useGL30 = true;
		config.width = 840;
		config.height = 480;
		
		//Typical
		//config.height = 768;
		//config.width = 1024;
		
		//HD
		config.width = 1280;
		config.height = 720;
		
		//FULLHD
		//config.width = 1920;
		//config.height = 1080;
		//config.fullscreen = true;
		
		MyGame.setPlatformSpecific(new DesktopSpecific());
		
		new LwjglApplication(new MyGame(), config);
	}
}
