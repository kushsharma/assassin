package utils;

import objects.Friend;
import objects.Ghost;
import objects.Player;
import Screens.GameScreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.softnuke.epic.MyGame;

public class MyContactListener implements ContactListener{
	protected Vector2[] points;
	protected int numContactPoints;

	@Override
	public void beginContact(Contact contact) {
		
		Body A = contact.getFixtureA().getBody();
		Body B = contact.getFixtureB().getBody();
		
		Player pl = Player.getInstance();
		
		if((A.getUserData().equals("platform") == true && contact.getFixtureB() == pl.getSensorFixture()) || (contact.getFixtureA() == pl.getSensorFixture() && B.getUserData().equals("platform") == true))
		{//handle player and platform collision
			pl.CAN_JUMP = true;
			pl.startJumpEffect();
			
			return;
		}
		
		if(GameScreen.MULTIPLAYER == true){
		//if its multiplayer mode	
			Friend fr = Friend.getInstance();
			if((A.getUserData().equals("platform") == true && contact.getFixtureB() == fr.getSensorFixture()) || (contact.getFixtureA() == fr.getSensorFixture() && B.getUserData().equals("platform") == true))
			{//handle friend and platform collision
				//fr.CAN_JUMP = true;
				fr.startJumpEffect();
				
			}
		}
		
		if((A.getUserData().equals("spikes") == true && contact.getFixtureB().equals(pl.getBodyFixture()) == true) || (contact.getFixtureA().equals(pl.getBodyFixture()) == true && B.getUserData().equals("spikes") == true))
		{//handle spikes kills
			
			//start player anime
			pl.setDeath();
			return;
		}
		
		if((A.getUserData().equals("laser") == true && contact.getFixtureB().equals(pl.getBodyFixture()) == true) || (contact.getFixtureA().equals(pl.getBodyFixture()) == true && B.getUserData().equals("laser") == true))
		{//handle laser kills
			
			if(A.getUserData().equals("player") == true)
				LevelGenerate.getInstance().laserPlayerCollide(contact.getFixtureB(), contact.getFixtureA());
			else
				LevelGenerate.getInstance().laserPlayerCollide(contact.getFixtureA(), contact.getFixtureB());
			
			return;
		}
		
		if((A.getUserData().equals("enemy") == true && B.getUserData().equals("player") == true) || (A.getUserData().equals("player") == true && B.getUserData().equals("enemy") == true))
		{//handle badboy kills
			
			if(A.getUserData().equals("player") == true)
				LevelGenerate.getInstance().enemyPlayerCollide(contact.getFixtureB(), contact.getFixtureA());
			else
				LevelGenerate.getInstance().enemyPlayerCollide(contact.getFixtureA(), contact.getFixtureB());
			
		}
		
		if(!GameScreen.MULTIPLAYER)			
		if((A.getUserData().equals("enemy") == true && B.getUserData().equals("ghost") == true) || (A.getUserData().equals("ghost") == true && B.getUserData().equals("enemy") == true))
		{//handle enemy with ghost image kills
			
			if(A.getUserData().equals("ghost") == true)
				LevelGenerate.getInstance().enemyGhostCollide(contact.getFixtureB(), contact.getFixtureA());
			else
				LevelGenerate.getInstance().enemyGhostCollide(contact.getFixtureA(), contact.getFixtureB());

		}
		
		if((A.getUserData().equals("enemy") == true && B.getUserData().equals("platform") == true) || (A.getUserData().equals("platform") == true && B.getUserData().equals("enemy") == true))
		{//handle enemy and platform collision
			if(A.getUserData().equals("platform") == true)
				LevelGenerate.getInstance().enemyWallBounce(contact.getFixtureB());
			else
				LevelGenerate.getInstance().enemyWallBounce(contact.getFixtureA());
			
			return;
		}
		
		//handle bullet and enemy
		if((A.getUserData().equals("enemy") == true && B.getUserData().equals("bullet") == true) || (A.getUserData().equals("bullet") == true && B.getUserData().equals("enemy") == true))
		{
//			boolean left_direction = false;
//			
//			if(Player.getInstance().getBody().equals(A) || Player.getInstance().getBody().equals(B))
//				left_direction = Player.getInstance().LEFT_DIRECTION;
//			else if(!GameScreen.MULTIPLAYER && (Ghost.getInstance().getBody().equals(A) || Ghost.getInstance().getBody().equals(B)))
//				left_direction = Ghost.getInstance().LEFT_DIRECTION;
//			
						
//			if(A.getUserData().equals("enemy") == true)
//				LevelGenerate.getInstance().enemyBulletCollide(contact.getFixtureA(), contact.getFixtureB(), left_direction);
//			else
//				LevelGenerate.getInstance().enemyBulletCollide(contact.getFixtureB(), contact.getFixtureA(), left_direction);
			
			LevelGenerate.getInstance().enemyBulletCollide(contact);
			
			return;
		}
		
		//handle weapon and enemy of player
		//if((A.getUserData().equals("enemy") == true && B.getUserData().equals("weapon") == true) || (A.getUserData().equals("weapon") == true && B.getUserData().equals("enemy") == true))
		if((A.getUserData().equals("enemy") == true && (contact.getFixtureB().equals(pl.getWeaponFixture()) == true)) || (contact.getFixtureA().equals(pl.getWeaponFixture()) == true && B.getUserData().equals("enemy") == true))
		{			
			/*
			boolean left_direction = false;
			
			//player hit direction for jerk effect
			if(Player.getInstance().getBody().equals(A) || Player.getInstance().getBody().equals(B))
				left_direction = Player.getInstance().LEFT_DIRECTION;
			else if(!GameScreen.MULTIPLAYER && (Ghost.getInstance().getBody().equals(A) || Ghost.getInstance().getBody().equals(B)))
				left_direction = Ghost.getInstance().LEFT_DIRECTION;
			*/
						
			if(A.getUserData().equals("enemy") == true)
				LevelGenerate.getInstance().enemyWeaponCollide(contact.getFixtureA(), contact.getFixtureB(), true);
			else
				LevelGenerate.getInstance().enemyWeaponCollide(contact.getFixtureB(), contact.getFixtureA(), true);
			
		}
		
		//handle weapon and enemy of player
		if(!GameScreen.MULTIPLAYER)
		if((A.getUserData().equals("enemy") == true && (contact.getFixtureB().equals(Ghost.getInstance().getWeaponFixture()) == true)) || (contact.getFixtureA().equals(Ghost.getInstance().getWeaponFixture()) == true && B.getUserData().equals("enemy") == true))
		{
			if(A.getUserData().equals("enemy") == true)
				LevelGenerate.getInstance().enemyWeaponCollide(contact.getFixtureA(), contact.getFixtureB(), true);
			else
				LevelGenerate.getInstance().enemyWeaponCollide(contact.getFixtureB(), contact.getFixtureA(), true);
			
		}
		
		//handle bullet and (platfrom/portal)
		if((((A.getUserData().equals("platform") == true || A.getUserData().equals("portal") == true) && B.getUserData().equals("bullet") == true)) || (A.getUserData().equals("bullet") == true && (B.getUserData().equals("platform") || B.getUserData().equals("portal") == true) == true))
		{
			if(A.getUserData().equals("platform") == true || A.getUserData().equals("portal") == true)
				LevelGenerate.getInstance().bulletPlatformCollide(contact.getFixtureB());
			else
				LevelGenerate.getInstance().bulletPlatformCollide(contact.getFixtureA());
			
			return;
		}
		
		//handle powerups
//		if((A.getUserData().equals("player") == true && B.getUserData().equals("powerUp") == true) || (A.getUserData().equals("powerUp") == true && B.getUserData().equals("player") == true))
//		{
//			if(A.getUserData().equals("player") == true)
//				LevelGenerate.getInstance().powerUpPlayer(contact.getFixtureB());
//			else
//				LevelGenerate.getInstance().powerUpPlayer(contact.getFixtureA());
//			
//		}
		
		//handle portals
		if((A.getUserData().equals("player") == true && B.getUserData().equals("portal") == true) || (A.getUserData().equals("portal") == true && B.getUserData().equals("player") == true))
		{
			//user can jump on portals too
			pl.CAN_JUMP = true;

			if(A.getUserData().equals("player") == true)
					LevelGenerate.getInstance().levelClearPortal(contact.getFixtureB());
			else
				LevelGenerate.getInstance().levelClearPortal(contact.getFixtureA());				
						
		}
		
		//handle moving platforms, a.k.a. movers
		if((A.getUserData().equals("player") == true && B.getUserData().equals("mover") == true) || (A.getUserData().equals("mover") == true && B.getUserData().equals("player") == true))
		{
			//user can jump on movers too
			pl.CAN_JUMP = true;
			
		}
		
		//handle coin collection
		if((A.getUserData().equals("player") == true && B.getUserData().equals("coin") == true) || (A.getUserData().equals("coin") == true && B.getUserData().equals("player") == true))
		{
			if(A.getUserData().equals("player") == true)
				LevelGenerate.getInstance().coinPlayerCollide(contact.getFixtureB());
			else
				LevelGenerate.getInstance().coinPlayerCollide(contact.getFixtureA());
			
		}
		
		//handle switch for enabling level exit portal
		if((contact.getFixtureA().equals(pl.getBodyFixture()) == true && B.getUserData().equals("switch") == true) || (A.getUserData().equals("switch") == true && contact.getFixtureB().equals(pl.getBodyFixture()) == true ))
		{
			if(contact.getFixtureA().equals(pl.getBodyFixture()) == true)
				LevelGenerate.getInstance().switchPlayerCollide(contact.getFixtureB(), true);
			else
				LevelGenerate.getInstance().switchPlayerCollide(contact.getFixtureA(), true);			
			
		}
		
		//handle switch for enabling level exit portal with ghost!!
		if(!GameScreen.MULTIPLAYER)
		if((contact.getFixtureA().equals(Ghost.getInstance().getBodyFixture()) == true && B.getUserData().equals("switch") == true) || (A.getUserData().equals("switch") == true && contact.getFixtureB().equals(Ghost.getInstance().getBodyFixture()) == true ))
		{
			if(contact.getFixtureA().equals(pl.getBodyFixture()) == true)
				LevelGenerate.getInstance().switchGhostCollide(contact.getFixtureB(), true);
			else
				LevelGenerate.getInstance().switchGhostCollide(contact.getFixtureA(), true);
			
		}
		
	}

	@Override
	public void endContact(Contact contact) {
		
		Body A = contact.getFixtureA().getBody();
		Body B = contact.getFixtureB().getBody();
		
		Player pl = Player.getInstance();
		
		if((A.getUserData().equals("enemy") == true && B.getUserData().equals("platform") == true) || (A.getUserData().equals("platform") == true && B.getUserData().equals("enemy") == true))
		{//handle enemy and platform collision
			
			if(A.getUserData().equals("platform") == true)
				LevelGenerate.getInstance().enemyFlying(contact.getFixtureB());
			else
				LevelGenerate.getInstance().enemyFlying(contact.getFixtureA());
			
			return;
		}
		
		//enemy escapes from weapon range of player
		if((A.getUserData().equals("enemy") == true && contact.getFixtureB().equals(pl.getWeaponFixture()) == true) || (contact.getFixtureA().equals(pl.getWeaponFixture()) == true && B.getUserData().equals("enemy") == true))
		{							
			if(A.getUserData().equals("enemy") == true)
				LevelGenerate.getInstance().enemyWeaponCollide(contact.getFixtureA(), contact.getFixtureB(), false);
			else
				LevelGenerate.getInstance().enemyWeaponCollide(contact.getFixtureB(), contact.getFixtureA(), false);
			
			return;
		}
		
		//handle switch for enabling level exit portal
		if((contact.getFixtureA().equals(pl.getBodyFixture()) == true && B.getUserData().equals("switch") == true) || (A.getUserData().equals("switch") == true && contact.getFixtureB().equals(pl.getBodyFixture()) == true ))
		{
			if(contact.getFixtureA().equals(pl.getBodyFixture()) == true)
				LevelGenerate.getInstance().switchPlayerCollide(contact.getFixtureB(), false);
			else
				LevelGenerate.getInstance().switchPlayerCollide(contact.getFixtureA(), false);			
			
		}
		
		//handle switch for enabling level exit portal with ghost!!
		if(!GameScreen.MULTIPLAYER)
		if((contact.getFixtureA().equals(Ghost.getInstance().getBodyFixture()) == true && B.getUserData().equals("switch") == true) || (A.getUserData().equals("switch") == true && contact.getFixtureB().equals(Ghost.getInstance().getBodyFixture()) == true ))
		{
			if(contact.getFixtureA().equals(pl.getBodyFixture()) == true)
				LevelGenerate.getInstance().switchGhostCollide(contact.getFixtureB(), false);
			else
				LevelGenerate.getInstance().switchGhostCollide(contact.getFixtureA(), false);
			
		}
		
		//enemy escapes from weapon range of ghost
		if(!GameScreen.MULTIPLAYER)
		if((A.getUserData().equals("enemy") == true && contact.getFixtureB().equals(Ghost.getInstance().getWeaponFixture()) == true) || (contact.getFixtureA().equals(Ghost.getInstance().getWeaponFixture()) == true && B.getUserData().equals("enemy") == true))
		{							
			if(A.getUserData().equals("enemy") == true)
				LevelGenerate.getInstance().enemyWeaponCollide(contact.getFixtureA(), contact.getFixtureB(), false);
			else
				LevelGenerate.getInstance().enemyWeaponCollide(contact.getFixtureB(), contact.getFixtureA(), false);
			
			return;
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		Body A = contact.getFixtureA().getBody();
		Body B = contact.getFixtureB().getBody();		
	
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub
		
	}

}
