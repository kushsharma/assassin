package com.softnuke.epic.android;

import Screens.GameScreen;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.provider.Settings;
import android.widget.Toast;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.utils.Array;
import com.softnuke.epic.ActionListener;
import com.softnuke.epic.MyGame;
import com.softnuke.epic.PlatformSpecific;

public class AndroidLauncher extends AndroidApplication implements PlatformSpecific {

	AlertDialog.Builder alertBuilder;

	//send event to all those who are waiting to listen
	private Array<ActionListener> listeners;


	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// fix for launcher icon starting new activity on top of old one
		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
			finish();
			return;
		}

		listeners = new Array<ActionListener>();


		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useAccelerometer = false;
		config.useCompass = false;
		config.hideStatusBar = true;
		//config.useImmersiveMode = true;
		config.useAccelerometer = false;

		if(GameScreen.SOFT_DEBUG)
			config.useWakelock = true;

		MyGame.setPlatformSpecific(this);

		initialize(new MyGame(), config);



		//create app exit
		alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setTitle("Exit");
		alertBuilder.setMessage("Do you really want to exit?");
		alertBuilder.setCancelable(true);
		alertBuilder.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Gdx.app.exit();
					}
				});
		alertBuilder.setNegativeButton("No",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
	}

	@Override
	public String getID() {
		String androidId = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);

		//TelephonyManager tManager = (TelephonyManager)myActivity.getSystemService(Context.TELEPHONY_SERVICE);
		//String uid = tManager.getDeviceId();

		return androidId;
	}

	@Override
	public void signIn() {
		// TODO Auto-generated method stub

	}

	@Override
	public void signOut() {
		// TODO Auto-generated method stub

	}

	@Override
	public void rateGame() {
		// Replace the end of the URL with the package of your game
		//String str ="https://play.google.com/store/apps/details?id=com.softnuke.jumper";
		//startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(str)));

		Uri uri = Uri.parse("market://details?id=" + getPackageName());
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
		goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		try {
			startActivity(goToMarket);
		} catch (ActivityNotFoundException e) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
		}

		//unlock the doer achievement
		//unlockAchievementGPGS(Constants.ACHIEVEMENT_DOER);
	}

	@Override
	public void shareScore(int score){
		//TODO:add a screenshot too

		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

		String shareBody = "OMG! I scored "+score+" points. You have to give this a try - https://play.google.com/store/apps/details?id="+getPackageName();
		if(score < 0)
			shareBody = "You have to at least try this game once! Its insanely awesome - https://play.google.com/store/apps/details?id="+getPackageName();

		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.app_name);
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		startActivity(Intent.createChooser(sharingIntent, "Share via"));

		sendEvent(ActionListener.SCORE_SHARED);

		//unlock achievement show off
		//unlockAchievementGPGS(Constants.ACHIEVEMENT_SHOWOFF);
	}

	@Override
	public void submitLeaderboard(long score) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showLeaderboard() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSignedIn() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void getAchievementsGPGS() {
		// TODO Auto-generated method stub

	}

	@Override
	public void unlockAchievementGPGS(String achievementId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadAd() {
		// TODO Auto-generated method stub

	}

	@Override
	public void showAd() {
		// TODO Auto-generated method stub

	}

	@Override
	public void appInvite() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTrackerScreen(String screen) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAds() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isAdsRemoved() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void showToast(String text, boolean shortTime) {
		final String  message = text;
		final int duration = (shortTime) ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG;

		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				Toast.makeText(getApplicationContext(), message, duration).show();
			}
		});
	}

	public void sendEvent(int id){
		sendEvent(id, null);
	}

	public void sendEvent(int id, Object data){
		for (ActionListener listener: listeners){
			listener.handleEvent(id, data);
		}
	}

	@Override
	public void registerActionListener(ActionListener listener) {
		if (!listeners.contains(listener, true)){
			listeners.add(listener);
		}
	}

	@Override
	public void unRegisterActionListener(ActionListener listener) {
		listeners.removeValue(listener, true);
	}

	@Override
	public void exitApp() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertDialog exitAlert = alertBuilder.create();
				exitAlert.show();
			}
		});

	}

	@Override
	public void openTwitter() {
		//TODO: Add @thekushsharma

		try {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/thekushsharma")));
		} catch (ActivityNotFoundException e) {
		}
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
	public void isOnline() {
		// TODO Auto-generated method stub

	}
}
