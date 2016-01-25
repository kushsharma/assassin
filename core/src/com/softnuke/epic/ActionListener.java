package com.softnuke.epic;

public interface ActionListener {
	
	public static final int SIGN_IN = 0;
    public static final int SIGN_OUT = 1;
    public static final int SIGN_IN_FAILED = 2;
    public static final int PREMIUM_ENABLED = 3;
    public static final int PREMIUM_DISABLED = 4;
    public static final int TOP_SCORE_UPDATED = 5;
    public static final int ACHIEVEMENTS_LOADED = 6;
    public static final int GAMES_READY = 7;
    public static final int APP_INVITE = 8;
    public static final int SCORE_SHARED = 9;

    public void handleEvent(int id, Object data);
    
}
