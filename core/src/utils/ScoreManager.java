package utils;

import Screens.GameScreen;
import Screens.MainMenuScreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Base64Coder;
import com.softnuke.epic.MyGame;

public class ScoreManager {

	private Preferences prefs;
	
	protected static final String UTF8 = "utf-8";
    private static final char[] SEKRIT = "1sth@t@lly0uG0t?".toCharArray() ; 
     
    StringBuilder sb;
    
	public int USER_SCORE = 0;
	public int USER_HIGH_SCORE;
	public int USER_TOTAL_SCORE;
	public int USER_TOTAL_DEATHS;
	public int MAX_LEVELS_UNLOCKED = LevelGenerate.MAX_LEVELS;
	public int MAX_LEVELS = LevelGenerate.MAX_LEVELS;

	//public static final int STAR_MILK = 3;
	//public static final int STAR_ENEMY = 5;
	
//	public int SLOMO_DURATION;
//	public int SHIELD_DURATION;
	
//	public int TIMES_SLOMOED;
//	public int TIMES_SHIELDED;
	public int TIMES_JUMPED;	
	
	public int TUTORIAL_LEVEL;

//	public boolean RABBIT_OWNED;
//	public boolean SENSAI_OWNED;
//	public int PLAYER_COSTUME;
	
//	public final static int SLOMO_UPGRADE_COST = 700;
//	public final static int SHIELD_UPGRADE_COST = 800;
	public final static int REVIVE_COST = 1500;
	
//	public final static int SLOMO_USE_COST = 15;	
	
//	public final static int RABBIT_COST = 1500;
//	public final static int SENSAI_COST = 4000;

	//settings
	public static boolean FPS_CHECK = true;
	
	
	//analytics stats
	public int jumpedLastTime = 0;
	public int slomodLastTime = 0;
	public int shieldLastTime = 0;
	public int deathLastTime = 0;

	//public static boolean RESET_GAME = true;
	
	public ScoreManager(){		
		prefs = Gdx.app.getPreferences(MainMenuScreen.PreferenceName);	
		
		sb = new StringBuilder();

	
		USER_HIGH_SCORE =  getInteger("highScore", 0);
		USER_TOTAL_SCORE = getInteger("totalScore", 0);
		//MAX_LEVELS_UNLOCKED = getInteger("levelUnlocked", LevelGenerate.MAX_LEVEL);
		
		deathLastTime = USER_TOTAL_DEATHS = getInteger("totalDeath", 0);

//		SLOMO_DURATION = getInteger("slomoDuration", 2);
//		SHIELD_DURATION = getInteger("shieldDuration", 4);
		
//		slomodLastTime =  TIMES_SLOMOED = getInteger("timesSlomo", 0);
//		shieldLastTime = TIMES_SHIELDED = getInteger("timesShield", 0);
		jumpedLastTime = TIMES_JUMPED = getInteger("timesJump", 0);	
		
		TUTORIAL_LEVEL = getInteger("tutorialLevel", 0);		
//		PLAYER_COSTUME = getInteger("playerSkin", Player.SQUARE);
		
		
//		RABBIT_OWNED = getBoolean("playerRabbit", false);
//		SENSAI_OWNED = getBoolean("playerSensai", false);
		

		//non encrypted
		//FPS_CHECK = prefs.getBoolean("fpsCheck", true);
	}
	
	/**@param screen 0 for all, 1 for menu, 2 for game**/
	public void save(int screen){
		
		if(screen == 0 || screen == 2){
			//game
			prefs.putString("highScore", encInt(USER_HIGH_SCORE));
			prefs.putString("totalDeath", encInt(USER_TOTAL_DEATHS));
			prefs.putString("timesJump", encInt(TIMES_JUMPED));
			//prefs.putString("levelUnlocked", encInt(MAX_LEVELS_UNLOCKED));
//			prefs.putString("timesShield", encInt(TIMES_SHIELDED));
//			prefs.putString("timesSlomo", encInt(TIMES_SLOMOED));
			prefs.putString("tutorialLevel", encInt(TUTORIAL_LEVEL));			
		}

		if(screen ==0 || screen == 1){
			//main menu
//			prefs.putString("slomoDuration", encInt(SLOMO_DURATION));
//			prefs.putString("shieldDuration", encInt(SHIELD_DURATION));
//			prefs.putString("playerSensai", encBool(SENSAI_OWNED ));
//			prefs.putString("playerRabbit", encBool(RABBIT_OWNED ));			
//			prefs.putString("playerSkin", encInt(PLAYER_COSTUME));
		}
		
		prefs.putString("totalScore", encInt(USER_TOTAL_SCORE));

		prefs.flush();
		

	}
	
	private String xor (String input) {
		
		sb.setLength(0);
		for (int i = 0; i < input.length(); i++) {
			sb.append((char)(input.charAt(i) ^ SEKRIT[i % 13]));
		}
		return sb.toString();
	}
	
	public int getInteger(String name, int def){
		
		String result = prefs.getString(name, null);
		if(result == null)
			return def;
		try{
			
			result = xor(new String(Base64Coder.decodeString(result)));
		}catch(Exception e){
			result = "";
			
			if(GameScreen.DEBUG) e.printStackTrace();

		}
				
		int num = def;
		try{
			num = Integer.parseInt(result);
		}
		catch(Exception e){
			num = def;
			if(GameScreen.DEBUG) e.printStackTrace();

		}
		
		return num;
	}
	
	public boolean getBoolean(String name, boolean def){
		String result = prefs.getString(name, null);
		
		if(result == null)
			return def;
				
		try{			
			result = xor(new String(Base64Coder.decodeString(result)));
		}catch(Exception e){
			result = "";
			if(GameScreen.DEBUG) e.printStackTrace();

		}
		
		boolean num = def;
		try{
			num = Boolean.parseBoolean(result);
		}
		catch(Exception e){
			if(GameScreen.DEBUG) e.printStackTrace();

			num = def;
		}
		
		return num;
	}	
	
	private String encInt(int val){
		String r = "";
		try{
			r = Base64Coder.encodeString(xor(String.valueOf(val)));
		}
		catch(Exception e)
		{
			if(GameScreen.DEBUG) e.printStackTrace();
			r = "";
		}
		return r;
	}
	
	private String encBool(boolean val){
		String r = "";
		try{
			r = Base64Coder.encodeString(xor(String.valueOf(val)));
		}
		catch(Exception e)
		{
			if(GameScreen.DEBUG) e.printStackTrace();

			r = "";
		}
		return r;
	}
	
	public void increaseScore(int val){
		USER_SCORE+= Math.abs(val);
		
		//TODO:
		//MyGame.sop(USER_SCORE);
	}
	
	public void increaseDeath(){
		USER_TOTAL_DEATHS++;
	}
	
	public boolean updateHighScore(){
		if(USER_SCORE != 0)
		USER_HIGH_SCORE = Math.max(USER_HIGH_SCORE, USER_SCORE);	
		
		
		if(USER_SCORE == USER_HIGH_SCORE)
			return true;
		else
			return false;
	}
	
	public void updateTotalScore(){
		USER_TOTAL_SCORE += USER_SCORE;				

	}
	
//	public void fireTotalScore(){
//		USER_TOTAL_SCORE -= USER_SCORE;
//		
//		//save();
//	}
	
	public void reload(){
		if(prefs != null)
			prefs.flush();
		else
			prefs = Gdx.app.getPreferences(MainMenuScreen.PreferenceName);

	}
	
	public void reset(){
		USER_SCORE = 0;
	}
/*	
	public void applySlomo(){
		USER_SCORE -= SLOMO_USE_COST;
		
		increaseTimesSlowed();
	}
	
	public boolean canSlomo(){
		if(USER_SCORE < 15)
			return false;
		
		return true;
	}
*/	
	public void applyScorePower(){
		USER_SCORE += 20;
	}
/*	
	public boolean applyScoreRevive(){
		//apply revive using score
		if(USER_TOTAL_SCORE >= REVIVE_COST)
		{
			USER_TOTAL_SCORE -= REVIVE_COST;
			save(1);
			return true;
		}
		else
			return false;
	}

	public boolean canBadBoy() {
		if(USER_SCORE < 40)
			return false;
		
		return true;
	}
	
	public boolean upgradeSlomo(){
		if(USER_TOTAL_SCORE < SLOMO_UPGRADE_COST || SLOMO_DURATION == 5)
			return false;
		
		USER_TOTAL_SCORE -= SLOMO_UPGRADE_COST;
		
		SLOMO_DURATION += 1;
		SLOMO_DURATION = Math.min(SLOMO_DURATION , 5);

		save(1);
		
		return true;
	}
	
	public boolean upgradeShield(){
		if(USER_TOTAL_SCORE < SHIELD_UPGRADE_COST || SHIELD_DURATION == 7)
			return false;
		
		USER_TOTAL_SCORE -= SHIELD_UPGRADE_COST;
		
		SHIELD_DURATION += 1;
		SHIELD_DURATION = Math.min(SHIELD_DURATION , 7);
		

		save(1);
		
		return true;
	}

	public boolean canShield() {
		if(USER_SCORE < 80)
			return false;
		
		return true;
	}
	
	public boolean upgradePlayerCharacter(int player){
		
		if(player == Player.RABBIT){
			if(USER_TOTAL_SCORE < RABBIT_COST)
				return false;
			
			RABBIT_OWNED = true;
			
			USER_TOTAL_SCORE -= RABBIT_COST;
			
			save(1);
			return true;
		}		
		if(player == Player.SENSAI){
			if(USER_TOTAL_SCORE < SENSAI_COST)
				return false;
			
			SENSAI_OWNED = true;
			
			USER_TOTAL_SCORE -= SENSAI_COST;
			
			save(1);
			return true;
		}
		
		return false;
	}
	
	public void setPlayerSkin(int player){
		PLAYER_COSTUME = player;
		
		save(1);
	}
	
	public void increaseTimesSlowed(){
		TIMES_SLOMOED++;
	}

	public void increaseTimesShielded(){
		TIMES_SHIELDED++;
	}
*/	
	public void increaseTimesJumped(){
		TIMES_JUMPED++;
	}
	
	public void tutorialSeen(int level){
		TUTORIAL_LEVEL = level;
		
		save(0);
	}

	public void applyPremium() {
		USER_TOTAL_SCORE += 5000;
		
		save(0);
	}
	
	public void unlockLevel(int l){
		//if(l > LevelGenerate.MAX_LEVEL)
		//	return;
		
		//MAX_LEVELS_UNLOCKED = Math.max(l,MAX_LEVELS_UNLOCKED);
		
		save(0);
	}
	
	/**used for unlocking more stars in a level
	 * 
	 * @param levelno level number of game
	 * @param starno STAR_MILK, STAR_ENEMY [3, 5]
	 * **/
	public void unlockStars(int levelno, int starno){
		/* mechanism work as follows
		 * 
		 * by default it has 1 as value that mean no stars earned
		 * to unlock level one star, multiply by 2
		 * unlock level two star, multiply by 3
		 * unlock level three star, myltiply by 5
		 * 
		 */
		
		//give them one star by default for now
		int defaultStar = 2;
		
		int val = prefs.getInteger("level_stat_"+levelno, 1);
		
		if(val == 1)
			val *= defaultStar;
		
		if(val % starno != 0)
			val *= starno;
		
		prefs.putInteger("level_stat_"+levelno, val);
		
		//prefs.flush();
	}
	
	/**get number of stars of this level earned so far**/
	public int getStars(int levelno){
		/* mechanism work as follows
		 * 
		 * if 1 : no stars earned
		 * 2/3/5 : one star
		 * 6/15/10 : two stars
		 * 30 : three stars earned
		 * 
		 */
		
		int val = prefs.getInteger("level_stat_"+levelno, 1);
		
		if(val == 2 || val == 3 || val == 5)
			return 1;
		else if(val == 6 || val == 10 || val == 15)
			return 2;
		else if(val == 30)
			return 3;
		
		return 0;
	}
}
