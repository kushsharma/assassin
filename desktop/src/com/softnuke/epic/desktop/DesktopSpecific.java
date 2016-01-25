package com.softnuke.epic.desktop;

import com.badlogic.gdx.Gdx;
import com.softnuke.epic.ActionListener;
import com.softnuke.epic.PlatformSpecific;

public class DesktopSpecific implements PlatformSpecific{
	boolean signedInStateGPGS = false;

	@Override
	public String getID() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public void signIn() {
		System.out.println("loginGPGS");
		signedInStateGPGS = true;
	}

	@Override
	public void signOut() {
		System.out.println("signout");
		signedInStateGPGS = false;
	}

	@Override
	public void rateGame() {
		System.out.println("rate");
		
	}

	@Override
	public void submitLeaderboard(long score) {
		System.out.println("submitScoreGPGS " + score);
		
	}

	@Override
	public void showLeaderboard() {
		System.out.println("show score");
		
	}

	@Override
	public boolean isSignedIn() {
		// TODO Auto-generated method stub
		return signedInStateGPGS;
	}

	@Override
	public void getAchievementsGPGS() {
		System.out.println("achievements");
		
	}

	@Override
	public void unlockAchievementGPGS(String achievementId) {
		System.out.println("unlock achievements");
		
	}

	@Override
	public void shareScore(int score) {
		System.out.println("Score shared:"+ score);
		
	}

	@Override
	public void showAd() {
		System.out.println("Ad shown");
		
	}

	@Override
	public void loadAd() {
		System.out.println("Ad loading");
		
	}

	@Override
	public void appInvite() {
		System.out.println("App invited.");
		
	}

	@Override
	public void setTrackerScreen(String screen) {
		System.out.println(screen+": Screen Analytics.");
	}

	@Override
	public void removeAds() {
		System.out.println("remove ads");
		
	}

	@Override
	public boolean isAdsRemoved() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void registerActionListener(ActionListener listener) {
		System.out.println("Listener Registered.");
		
	}

	@Override
	public void unRegisterActionListener(ActionListener listener) {
		System.out.println("Listener unregistered.");
		
	}

	@Override
	public void exitApp() {
		Gdx.app.exit();
	}

	@Override
	public void openTwitter() {
		System.out.println("Twitter @thekushsharma");
	}

	@Override
	public void showVideoAd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendStats(int jump, int slomo, int shield, int deaths) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showToast(String text, boolean shortTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void isOnline() {
		// TODO Auto-generated method stub
		
	}

}
