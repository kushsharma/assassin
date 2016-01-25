package utils;

import Screens.GameScreen;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class CameraShake {
	
	/*
	 * Camera shaker with fixed camera position.
	 * 
	 * USES:
	 * initialize than add "update" to render loop
	 * then call shakeThatAss whenever shake is required.
	 * 
	 * */
	
	OrthographicCamera camera;
	float shakedTime = 0;
	float shakeDuration = 0.1f;
	float nH,nW;
	boolean Shake = false;
	public static final float DEFAULT_SHAKE_POWER = 0.15f;
	float shakePower = 0.15f;
	
	float originalX, originalY;
	
	public CameraShake(OrthographicCamera cam){
		camera = cam;		
			
		//camera position
		originalX = camera.position.x;
		originalY = camera.position.y;
	}
	
	public void shakeThatAss(boolean sh, boolean light){
		//enable disable shakes sequence
		Shake = sh;
		originalX = camera.position.x;
		originalY = camera.position.y;
		
		if(light){
			shakePower = DEFAULT_SHAKE_POWER * 0.3f;
		}
		else
		{
			shakePower = DEFAULT_SHAKE_POWER;
		}
		
		if(sh)	shakedTime = 0;
	}
	
	public void shakeLight(boolean sh){
		shakeThatAss(sh, true);
	}
	
	public void update(float delta){
		//camera = cam;
				
		if(Shake == true)
		{
			shakedTime += delta;
			
			if(shakedTime < shakeDuration)
			{
				camera.position.x = MathUtils.random(originalX, originalX + shakePower);
				camera.position.y = MathUtils.random(originalY, originalY + shakePower);
			}
			else
			{
				camera.position.x = originalX;				
				camera.position.y = originalY;
				
				Shake = false;
				shakedTime = 0;
			}
			camera.update();
		}
		
		
	}
	
	public boolean getShake(){
		return Shake;
	}
	
	
}
