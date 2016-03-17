package utils;

import java.util.HashMap;

import objects.Player;
import Screens.GameScreen;
import Screens.MainMenuScreen;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.softnuke.epic.MyGame;

public class MyInputProcessor implements InputProcessor{
	
	GameScreen game = GameScreen.getInstance();
	LevelGenerate level = LevelGenerate.getInstance();
	
	private Vector3 tempTouchVec = new Vector3();
	public static enum CONTROL{
		LEFT,
		RIGHT,
		UP,
		FIRE
	}
	public HashMap<CONTROL, Boolean> PLAYER_KEYS;
	
	public MyInputProcessor(){
		
		//keys that can be active
		PLAYER_KEYS = new HashMap<CONTROL, Boolean>();
		PLAYER_KEYS.put(CONTROL.LEFT, false);
		PLAYER_KEYS.put(CONTROL.RIGHT, false);
		PLAYER_KEYS.put(CONTROL.UP, false);
		PLAYER_KEYS.put(CONTROL.FIRE, false);
		
	}
	
	public void resetKeys(){
		PLAYER_KEYS.put(CONTROL.LEFT, false);
		PLAYER_KEYS.put(CONTROL.RIGHT, false);
		PLAYER_KEYS.put(CONTROL.UP, false);
		PLAYER_KEYS.put(CONTROL.FIRE, false);
	}
	
	@Override
	public boolean keyDown(int keycode) {
		
		
		Player pl = Player.getInstance();	
		
		if(keycode == Keys.UP || keycode == Keys.Z){
			makeActionUp();
			//pl.makeJump();
			//PLAYER_KEYS.put(CONTROL.UP, true);
		}
		if(keycode == Keys.LEFT){
			makeActionLeft();
		}
		if(keycode == Keys.RIGHT){
			makeActionRight();
		}
		if(keycode == Keys.SPACE || keycode == Keys.X){
			makeActionFire();			
		}
		//pl.updateKeys(PLAYER_KEYS);		
		LevelGenerate.getInstance().updateMove(PLAYER_KEYS);

		
		if(GameScreen.SOFT_DEBUG)
		{	
			//camera controls
			if(keycode == Keys.MINUS)
				game.getCamera().zoom += (game.getCamera().zoom > 2) ? 0.5f : 0.05f;
			if(keycode == Keys.PLUS)
				game.getCamera().zoom -= (game.getCamera().zoom > 2) ? 0.5f : 0.05f;
			if(keycode == Keys.SLASH)
				game.getCamera().position.x-= 3;
			if(keycode == Keys.STAR)
				game.getCamera().position.x+= 3;
			if(keycode == Keys.PAGE_UP)
				game.getCamera().position.y+= 3;
			if(keycode == Keys.PAGE_DOWN)
				game.getCamera().position.y-= 3;			
			
			if(keycode == Keys.D || keycode == Keys.CAMERA)
			{				
				LevelGenerate.getInstance().test();					
			}
			
			if(keycode == Keys.VOLUME_UP || keycode== Keys.S){
				if(game.networkManager != null)
					game.networkManager.makeMeServer();
			}
			if(keycode == Keys.VOLUME_DOWN || keycode== Keys.C){
				if(game.networkManager != null)
					game.networkManager.makeMeClient();
			}
			
			game.getCamera().update();
		
			return true;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		GameScreen gs = GameScreen.getInstance();
		
		Player pl = Player.getInstance();
		
		if(keycode == Keys.UP || keycode == Keys.Z){
			PLAYER_KEYS.put(CONTROL.UP, false);
		}
		if(keycode == Keys.LEFT){
			leaveActionLeft();

			//PLAYER_KEYS.put(CONTROL.LEFT, false);
		}
		if(keycode == Keys.RIGHT){
			leaveActionRight();
		}
		if(keycode == Keys.SPACE || keycode == Keys.X){
			//if(LevelGenerate.MACHINE_GUN)
				leaveActionFire();
		}
		//pl.updateKeys(PLAYER_KEYS);
		LevelGenerate.getInstance().updateMove(PLAYER_KEYS);

		
		if(keycode == Keys.R)
		{
			gs.reset(false);
			//gs.CURRENT_STATE = GameState.STOPPED;
			return true;
		}
		
		if(keycode == Keys.ENTER){
			//if(LevelGenerate.CURRENT_LEVEL_CLEARED == true)
			//	gs.startloadingNextLevel();
		}
		
		if(keycode == Keys.BACK || keycode == Keys.ESCAPE){
			
			if(GameScreen.CURRENT_STATE == GameState.RUNNING)// && !GameScreen.getInstance().levelClearScreen.isVisible())
				gs.pauseGame();
			else if(GameScreen.CURRENT_STATE == GameState.PAUSED)
			{
				if(GameScreen.SOFT_DEBUG || GameScreen.DEBUG)
					gs.returnToMainMenu();
				else
					gs.resumeGame();
			}
			else
				gs.returnToMainMenu();


			return true;
		}
		
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		GameScreen gs = GameScreen.getInstance();
		tempTouchVec.set(screenX, screenY, 0);
		
		Vector3 point = gs.getCamera().unproject(tempTouchVec);
		
		if(point.x < MyGame.WIDTH && point.y < MyGame.HEIGHT){
			if(GameScreen.CURRENT_STATE == GameState.RUNNING){
				Player pl = Player.getInstance();
				
				return true;
			}
			
		}
		
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		//if(keycode == Keys.SPACE)		
			
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		if(GameScreen.SOFT_DEBUG){
			if(amount > 0)
			{
				game.getCamera().zoom += (game.getCamera().zoom > 2) ? 0.5f : 0.05f;
			}
			if(amount < 0){
				game.getCamera().zoom -= (game.getCamera().zoom > 2) ? 0.5f : 0.05f;
			}
			game.getCamera().update();
		}
		return false;
	}
	
	//buttons for handling touch events from stage
	public void makeActionLeft(){
		PLAYER_KEYS.put(CONTROL.LEFT, true);
		PLAYER_KEYS.put(CONTROL.RIGHT, false);
		Player pl = Player.getInstance();	

		pl.updateKeys(PLAYER_KEYS);	
		//pl.switchWeaponSide();
		
		game.sendNetworkUpdate(NetworkManager.REQUEST_MOVE_LEFT);
		if(!GameScreen.MULTIPLAYER)
			game.recorder.addFrame(Recorder.ACTION_LEFT);
	}
	
	public void makeActionRight(){
		PLAYER_KEYS.put(CONTROL.LEFT, false);
		PLAYER_KEYS.put(CONTROL.RIGHT, true);	
		
		Player pl = Player.getInstance();	

		pl.updateKeys(PLAYER_KEYS);		
		
		game.sendNetworkUpdate(NetworkManager.REQUEST_MOVE_RIGHT);
		
		if(!GameScreen.MULTIPLAYER)
			game.recorder.addFrame(Recorder.ACTION_RIGHT);

	}
	
	public void makeActionUp(){
		Player pl = Player.getInstance();	
		pl.makeJump();
		PLAYER_KEYS.put(CONTROL.UP, true);

		pl.updateKeys(PLAYER_KEYS);		
		
		game.sendNetworkUpdate(NetworkManager.REQUEST_JUMP);
		
		if(!GameScreen.MULTIPLAYER)
			game.recorder.addFrame(Recorder.ACTION_JUMP);

	}

	public void leaveActionLeft() {
		Player pl = Player.getInstance();	
		PLAYER_KEYS.put(CONTROL.LEFT, false);

		pl.updateKeys(PLAYER_KEYS);		

		game.sendNetworkUpdate(NetworkManager.REQUEST_STOP_LEFT);
		if(!GameScreen.MULTIPLAYER)
			game.recorder.addFrame(Recorder.ACTION_LEFT_STOP);

		//stop movement if not moving anywhere
		if(PLAYER_KEYS.get(CONTROL.RIGHT) == false)
			pl.stopMove();
	}
	
	public void leaveActionRight() {
		Player pl = Player.getInstance();	
		PLAYER_KEYS.put(CONTROL.RIGHT, false);

		pl.updateKeys(PLAYER_KEYS);	
		
		game.sendNetworkUpdate(NetworkManager.REQUEST_STOP_RIGHT);
		if(!GameScreen.MULTIPLAYER)
			game.recorder.addFrame(Recorder.ACTION_RIGHT_STOP);

		//stop movement if not moving anywhere
		if(PLAYER_KEYS.get(CONTROL.LEFT) == false)
			pl.stopMove();

	}
	
	public void leaveActionUp() {
		Player pl = Player.getInstance();	
		PLAYER_KEYS.put(CONTROL.UP, false);

		pl.updateKeys(PLAYER_KEYS);		
		if(!GameScreen.MULTIPLAYER)
			game.recorder.addFrame(Recorder.ACTION_JUMP_STOP);

	}

	public void makeActionFire() {

		if(!LevelGenerate.MACHINE_GUN)
		{
			LevelGenerate.getInstance().swingSword(true);
			//LevelGenerate.getInstance().fireBullet();
			Player.getInstance().CAN_FIRE = false;
		}
		
		PLAYER_KEYS.put(CONTROL.FIRE, true);
		LevelGenerate.getInstance().updateMove(PLAYER_KEYS);
		
		game.sendNetworkUpdate(NetworkManager.REQUEST_FIRE);
		if(!GameScreen.MULTIPLAYER)
			game.recorder.addFrame(Recorder.ACTION_FIRE);

	}
	
	public void leaveActionFire() {	
		
		Player.getInstance().CAN_FIRE = true;
		
		PLAYER_KEYS.put(CONTROL.FIRE, false);
		LevelGenerate.getInstance().updateMove(PLAYER_KEYS);

		game.sendNetworkUpdate(NetworkManager.REQUEST_STOP_FIRE);
		if(!GameScreen.MULTIPLAYER)
			game.recorder.addFrame(Recorder.ACTION_FIRE_STOP);

	}
}
