package com.softnuke.epic;

public interface PlatformSpecific {
	
	public String getID();
	
	public void signIn();
	public void signOut();
	public void rateGame();
	public void shareScore(int score);
	public void submitLeaderboard(long score);
	public void showLeaderboard();
	public boolean isSignedIn();
	public void getAchievementsGPGS();
	public void unlockAchievementGPGS(String achievementId);

	public void loadAd();
	public void showAd();
	public void showVideoAd();
	
	public void appInvite();
	public void setTrackerScreen(String screen);
	public void sendStats(int jump, int slomo, int shield, int deaths);
	
	public void removeAds();
	public boolean isAdsRemoved();
	
	public void showToast(String text, boolean shortTime);
	public void exitApp();
	
    public void registerActionListener(ActionListener listener);
    public void unRegisterActionListener(ActionListener listener);
 
    public void openTwitter();
    public void isOnline();
}
