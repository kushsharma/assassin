package utils;

import java.awt.EventQueue;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.TimerTask;

import objects.Enemy;
import objects.Friend;
import objects.Player;
import Screens.GameScreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.ClientDiscoveryHandler;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Serialization;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.kryonet.ServerDiscoveryHandler;
import com.softnuke.epic.MyGame;

public class NetworkManager {

	GameScreen game = GameScreen.getInstance();
	public STATES STATE = STATES.NONE;
	public NET_STATES NET_STATE = NET_STATES.DISCONNECTED;
	public static final int TCP_PORT = 19211;
	public static final int UDP_PORT = 19212;
	public static final float PLAYER_SNAP_TIME = 0.1f;
	public static final float ENEMY_SNAP_TIME = 5f;

	public static final int REQUEST_ACK = 0;
	public static final int REQUEST_FIRE = 1;
	public static final int REQUEST_STOP_FIRE = 2;
	public static final int REQUEST_JUMP = 3;
	public static final int REQUEST_MOVE_LEFT = 4;
	public static final int REQUEST_STOP_LEFT = 5;
	public static final int REQUEST_MOVE_RIGHT = 6;
	public static final int REQUEST_STOP_RIGHT = 7;
	public static final int REQUEST_DIE = 8;
	public static final int REQUEST_EMEMY_DIE = 9;
	public static final int REQUEST_GAME_STATE = 10;

	//public static final int REQUEST_SNAP_CORRECT = 9;

	float time = 0;
	float last_player_state = 0;
	float last_game_state = 0;
	
	Server server = null;
	Client client = null;
	
	Player player = null;
	Friend friend = null;
	Connection connection = null;
	
	public static enum STATES{
		NONE, SERVER, CLIENT
	}
	
	public static enum NET_STATES{
		CONNECTING, CONNECTED, DISCONNECTED
	}
	
	public NetworkManager(){
		friend = Friend.getInstance();
		player = Player.getInstance();
		
		NET_STATE = NET_STATES.DISCONNECTED;
		STATE = STATES.NONE;
		
	}
	
	public void sendGameInit(Connection con){
		Array<Enemy> enemyPool = LevelGenerate.getInstance().enemyPool;
		int i=0;
		currentGameInit.enemyCount = 0;
		for(Enemy e:enemyPool){
			//if(e.DEAD) continue;
			
			currentGameInit.enemyCount++;
			currentGameInit.enemyx[i] = e.getBody().getPosition().x;
			currentGameInit.enemyy[i] = e.getBody().getPosition().y;
			
			currentGameInit.enemyvx[i] = e.getBody().getLinearVelocity().x;
			currentGameInit.enemyvy[i] = e.getBody().getLinearVelocity().y;
			
			currentGameInit.enemyDead[i] = e.DEAD;
			currentGameInit.enemyDirection[i] = e.LEFT_DIRECTION;
					
			i++;
		}
		
		if(con != null){
			if(con.isConnected())
				con.sendTCP(currentGameInit);			
		}
		else{
			//i am client
			client.sendTCP(currentGameInit);
		}
		
		MyGame.sop("Sending Game State");
	}
	
	NetworkGameInit currentGameInit = new NetworkGameInit();
	public void makeMeServer(){
		STATE = STATES.SERVER;
		
		server = new Server();
		server.setDiscoveryHandler(new MyServerDiscoveryHandler());
		
		new Thread("Connect") {
				public void run () {
					server.start();
				}
		}.start();
		
		Kryo kryo = server.getKryo();
		kryo.register(NetworkRequest.class);
		kryo.register(NetworkMessage.class);
		kryo.register(NetworkPlayerState.class);
		kryo.register(NetworkGameInit.class);
		kryo.register(DiscoveryResponsePacket.class);
		kryo.register(int[].class);
		kryo.register(float[].class);
		kryo.register(boolean[].class);
		
		try {
			server.bind(TCP_PORT, UDP_PORT);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		Gdx.app.log("Net", server.getConnections().length+" connections");
		
		server.addListener(new Listener(){

			@Override
			public void connected(Connection con) {
				super.connected(con);
				
				connection = con;
				NET_STATE = NET_STATES.CONNECTED;
				//sendGameInit(connection);
				MyGame.sop("Connected to client");
			}

			@Override
			public void disconnected(Connection connection) {
				super.disconnected(connection);
				
				connection = null;
				//server.stop();
				NET_STATE = NET_STATES.DISCONNECTED;

			}
			
	        public void received (Connection connection, Object object) {
	        	super.received(connection, object);
	        	
	           if (object instanceof NetworkMessage) {
	        	   NetworkMessage response = (NetworkMessage)object;
	        	   Gdx.app.log("Net", response.text);
	           }
	           
	           else if(object instanceof NetworkPlayerState){
	        	   NetworkPlayerState ps = (NetworkPlayerState) object;
	        	   
	        	   friend.LEFT_DIRECTION = ps.side;
	        	   if(Math.abs(friend.getPosition().x - ps.x) > 1f || Math.abs(friend.getPosition().y - ps.y) > 1f){
	        		   //too large gap
	        		   friend.jumpTo(ps.x, ps.y);
	        	   }
	        	   else{
	        		   //not much distance, interpolate
	        		   friend.interpolateTo(ps.x, ps.y);
	        	   }
	        	       	   
	           }
	           
	           else if(object instanceof NetworkGameInit){
	        	   final NetworkGameInit gi = (NetworkGameInit) object;	        	   
	        	   //update enemies

	        	   EventQueue.invokeLater(new Runnable() {
						public void run () {
							//client killed someone
							MyGame.sop("Updating Enemies");
							LevelGenerate.getInstance().updateEnemyPos(gi);
						}
					});
	        	   
	           }
	           
	           else if(object instanceof NetworkRequest){
					NetworkRequest f = (NetworkRequest) object;
					switch(f.TYPE){
					case NetworkManager.REQUEST_JUMP:{
						friend.makeJump();
						//friend.getBody().applyLinearImpulse(0, 2, 0, 0, true);
						break;
					}
					case NetworkManager.REQUEST_MOVE_LEFT:{
						friend.updateMove(MyInputProcessor.CONTROL.LEFT, true);						
						break;
					}
					case NetworkManager.REQUEST_MOVE_RIGHT:{
						friend.updateMove(MyInputProcessor.CONTROL.RIGHT, true);
						break;
					}
					case NetworkManager.REQUEST_STOP_LEFT:{
						friend.updateMove(MyInputProcessor.CONTROL.LEFT, false);
						break;
					}
					case NetworkManager.REQUEST_STOP_RIGHT:{
						friend.updateMove(MyInputProcessor.CONTROL.RIGHT, false);
						break;
					}
					case NetworkManager.REQUEST_FIRE:{
						friend.updateMove(MyInputProcessor.CONTROL.FIRE, true);
						break;
					}
					case NetworkManager.REQUEST_STOP_FIRE:{
						friend.updateMove(MyInputProcessor.CONTROL.FIRE, false);
						break;
					}
					case NetworkManager.REQUEST_GAME_STATE:{

						sendGameInit(connection);						
						break;
					}
					}
	           }
	        }

			@Override
			public void idle(Connection connection) {
				super.idle(connection);
			}
			
		});
		
		init();
	}
	
	InetAddress clientConnectAddress = null;
	
	public void connectToServer(){
		if(clientConnectAddress == null){
			Gdx.app.log("Connecting to:", "None");
			return;
		}
		Gdx.app.log("Connecting to:", clientConnectAddress.getHostAddress());

		//MyGame.sop(""+clientConnectAddress.getHostAddress());
		new Thread("Connect") {
			public void run () {
				try {
					client.connect(5000, clientConnectAddress, TCP_PORT, UDP_PORT);
					// Server communication after connection can go here, or in Listener#connected().
					
				    Gdx.app.log("Net", "connected to server:" +client.getRemoteAddressTCP());   

				} catch (IOException ex) {
					ex.printStackTrace();
					//System.exit(1);
				}
			}
		}.start();
	}
	
	public void makeMeClient(){
		STATE = STATES.CLIENT;
		
		client = new Client();
		client.setDiscoveryHandler(new MyClientDiscoveryHandler(this));
		client.start();
		
		NET_STATE = NET_STATES.CONNECTING;
		
		Kryo kryo = client.getKryo();
		kryo.register(NetworkRequest.class);
		kryo.register(NetworkMessage.class);
		kryo.register(NetworkPlayerState.class);
		kryo.register(NetworkGameInit.class);
		kryo.register(DiscoveryResponsePacket.class);
		kryo.register(int[].class);
		kryo.register(float[].class);
		kryo.register(boolean[].class);
		
//		InetAddress address = client.discoverHost(UDP_PORT, 5000);
//	    if(address == null)
//	    {
//	    	Gdx.app.log("Net", "No server");
//	    	STATE = STATES.NONE;
//			NET_STATE = NET_STATES.DISCONNECTED;
//	    	return;
//	    }
//	    Gdx.app.log("Net", address.getHostAddress());
	    
	    new Thread("Search") {
			public void run () {
				clientConnectAddress = client.discoverHost(UDP_PORT, 5000);
				if(clientConnectAddress == null)
				{
					Gdx.app.log("Net", "No server");
					STATE = STATES.NONE;
					NET_STATE = NET_STATES.DISCONNECTED;
					return;
				}
				
			    Gdx.app.log("Net", "Found server:"+clientConnectAddress.getHostAddress());

			}
		}.start();
			
		//NET_STATE = NET_STATES.CONNECTED;

	    client.addListener(new Listener() {
	    	@Override
			public void connected(Connection connection) {
				super.connected(connection);
				
				NET_STATE = NET_STATES.CONNECTED;
				
				NetworkRequest request = new NetworkRequest();
				request.TYPE = NetworkManager.REQUEST_GAME_STATE;
				connection.sendTCP(request);
				
				MyGame.sop("Requesting State");
			}

			@Override
			public void disconnected(Connection connection) {
				super.disconnected(connection);
				//client.stop();
				NET_STATE = NET_STATES.DISCONNECTED;

			}
			
	        public void received (Connection connection, Object object) {
	        	super.received(connection, object);
	        	
	           if (object instanceof NetworkMessage) {
	        	   NetworkMessage response = (NetworkMessage)object;
	        	   Gdx.app.log("Net", response.text);
	           }
	           
	           else if(object instanceof NetworkPlayerState){
	        	   NetworkPlayerState ps = (NetworkPlayerState) object;
	        	   
	        	   friend.LEFT_DIRECTION = ps.side;
	        	   if(Math.abs(friend.getPosition().x - ps.x) > 1f || Math.abs(friend.getPosition().y - ps.y) > 0.5f){
	        		   //too large gap
	        		   friend.jumpTo(ps.x, ps.y);
	        	   }
	        	   else{
	        		   //not much distance, interpolate
	        		   friend.interpolateTo(ps.x, ps.y);
	        	   }
	        	   
	   			
	           }
	           
	           else if(object instanceof NetworkGameInit){
	        	   final NetworkGameInit gi = (NetworkGameInit) object;	        	   
	        	   //update enemies
	        	   
//	        	   Timer.schedule(new Task(){
//	   			    @Override
//	   			    public void run() {
//	   			    	MyGame.sop("Updating Enemies");
//						LevelGenerate.getInstance().updateEnemyPos(gi);
//	   			    }
//	   				}, 0);
	        	   EventQueue.invokeLater(new Runnable() {
						public void run () {
							
							MyGame.sop("Updating Enemies");
							LevelGenerate.getInstance().updateEnemyPos(gi);
							// Closing the frame calls the close listener which will stop the client's update thread.
						}
					});
	        	   
	           }
	           
	           else if(object instanceof NetworkRequest){
					NetworkRequest f = (NetworkRequest) object;
					switch(f.TYPE){
					case NetworkManager.REQUEST_JUMP:{
						friend.makeJump();
						//friend.getBody().applyLinearImpulse(0, 2, 0, 0, true);
						break;
					}
					case NetworkManager.REQUEST_MOVE_LEFT:{
						friend.updateMove(MyInputProcessor.CONTROL.LEFT, true);						
						break;
					}
					case NetworkManager.REQUEST_MOVE_RIGHT:{
						friend.updateMove(MyInputProcessor.CONTROL.RIGHT, true);
						break;
					}
					case NetworkManager.REQUEST_STOP_LEFT:{
						friend.updateMove(MyInputProcessor.CONTROL.LEFT, false);
						break;
					}
					case NetworkManager.REQUEST_STOP_RIGHT:{
						friend.updateMove(MyInputProcessor.CONTROL.RIGHT, false);
						break;
					}
					case NetworkManager.REQUEST_FIRE:{
						friend.updateMove(MyInputProcessor.CONTROL.FIRE, true);
						break;
					}
					case NetworkManager.REQUEST_STOP_FIRE:{
						friend.updateMove(MyInputProcessor.CONTROL.FIRE, false);
						break;
					}
					}
	           }
	        }
	        
	     });
	    
		init();
	}
	
	public void init(){
		
		
	}
	
	NetworkPlayerState currentState = new NetworkPlayerState();
	public void update(float delta){
		time += delta;
		
		if(STATE != STATES.NONE)
		{
			last_player_state += delta;
		}
		
		//if i am server, keep sending game states time to time
		if(STATE == STATES.SERVER)
			last_game_state += delta;
		
		if(last_game_state > ENEMY_SNAP_TIME){
			last_game_state = 0;
			
			if(STATE == STATES.SERVER){
				//check for connected connections
				if(connection != null && connection.isConnected()){					
					sendGameInit(connection);
				}				
			}
		}
		
		if(last_player_state > PLAYER_SNAP_TIME)
		{
			last_player_state = 0;
			currentState.x = Player.getInstance().getPosition().x;
			currentState.y = Player.getInstance().getPosition().y;
			currentState.side = Player.getInstance().LEFT_DIRECTION;
			
			
			sendPlayerState(currentState);
		}
		
		
	}
	
	private void sendPlayerState(NetworkPlayerState request) {
		if(STATE == STATES.NONE) return;
		
		if(STATE == STATES.SERVER){
			//check for connected connections
			if(connection == null || !connection.isConnected())
				return;
			
			connection.sendUDP(request);
		}
		else{
			//check if client is connected or not
			if(!client.isConnected())
				return;
			
			client.sendUDP(request);
		}
	}

	public void sendUpdate(int type){
		//MyGame.sop("aaa");

		//if not connected to anyone, just return
		if(STATE == STATES.NONE) return;
		
		NetworkRequest request = new NetworkRequest();
		request.TYPE = type;
		
		Gdx.app.log("Net", "Sending type:"+type);
		
		if(STATE == STATES.SERVER){
			//i am server
			
			//check for connected connections
			if(connection == null ||!connection.isConnected())
				return;
			
			connection.sendTCP(request);
			
		}
		else{
			//i am client
			
			//check if client is connected or not
			if(!client.isConnected())
				return;
			
			client.sendTCP(request);
		}
		
	}

	/** Request new enemy updates */
	public void sendEnemyKillUpdate() {
//		if(STATE == STATE.SERVER)
//			last_game_state += ENEMY_SNAP_TIME;
//		else
//		{
//			//i am client
//			sendGameInit(null);
//			
//		}
		//TODO: test this - tested and works
		if(STATE == STATES.SERVER){
			//check for connected connections
			if(connection != null && connection.isConnected()){					
				sendGameInit(connection);
			}				
		}
		else{
			//i am client
			sendGameInit(null);
		}
	}
	
	Timer timer = new Timer();
	public void dispose(){
		if(STATE == STATES.CLIENT)
		{
						
			new Thread("Stop") {
				public void run () {
					try {
						client.stop();
						client.dispose();
					} catch (IOException e) {
						e.printStackTrace();
					};
				}
			}.start();
			
			
			
		}
		else if(STATE == STATES.SERVER)
		{
//			Timer.schedule(new Task(){
//			    @Override
//			    public void run() {
//			    }
//			}, 0);
			
			
			new Thread("Stop") {
				public void run () {
					try {
						server.stop();
						server.dispose();
					} catch (IOException e) {
						e.printStackTrace();
					};
				}
			}.start();
			
//			timer.scheduleTask(new Task() {
//				public void run () {
//					
//				}
//			}, 0);
			
			
		}
		
		connection = null;
		STATE = STATES.NONE;
		NET_STATE = NET_STATES.DISCONNECTED;
	}


}
	class NetworkPlayerPosition{
		
		public NetworkPlayerPosition(){}
		
	}
	
	class NetworkPlayerState{
		
		public NetworkPlayerState(){}
		
		int id = 0;
		float x = 0;
		float y = 0;
		boolean side = false;
		
		//int enemyCount = 0;
		//float enemyx[] = new float[30];
		//float enemyy[] = new float[30];
		//boolean enemyAlive[] = new boolean[30];
		//boolean enemyDirection[] = new boolean[30];
		//float vx = 0;
		//float vy = 0;
	}
	
	class NetworkGameInit{
		int enemyCount = 0;
		float enemyx[] = new float[30];
		float enemyy[] = new float[30];
		float enemyvx[] = new float[30];
		float enemyvy[] = new float[30];
		boolean enemyDead[] = new boolean[30];
		boolean enemyDirection[] = new boolean[30];
		
		public NetworkGameInit(){}
	}
	
	class NetworkRequest{
		int id = 0;
		int TYPE = NetworkManager.REQUEST_ACK;
		int data = 0;
		
		public NetworkRequest(){}
	}
	
	class NetworkMessage{
		int id = 0;
		String text = null;
		public NetworkMessage(){}
	}
	
	class MyClientDiscoveryHandler implements ClientDiscoveryHandler{
		private Input input = null;

		NetworkManager manager;
		
		public MyClientDiscoveryHandler(NetworkManager man) {
			manager = man;
		}
		
		@Override
		public DatagramPacket onRequestNewDatagramPacket() {
			byte[] buffer = new byte[1024];
			input = new Input(buffer);
			return new DatagramPacket(buffer, buffer.length);
		}
	
		@Override
		public void onDiscoveredHost(DatagramPacket datagramPacket, Kryo kryo) {
			if (input != null) {
				DiscoveryResponsePacket packet = null;
				
				try{
					packet = (DiscoveryResponsePacket) kryo.readClassAndObject(input);					
				}catch(Exception e){
					e.printStackTrace();
				}

				if(packet != null){
					//updating visual list of servers
					GameScreen.getInstance().setServerList(packet.gameName+" : "+packet.playerCount);
					MyGame.sop("Server:"+ packet.gameName + ", connected:"+packet.playerCount);	
					
					manager.clientConnectAddress = datagramPacket.getAddress();
				}
				else{
					GameScreen.getInstance().setServerList("No server found, please retry.");
				}
			}
			
		}
	
		@Override
		public void onFinally() {
			if (input != null) {
				input.close();
			}
		}
		
	}
	
	class MyServerDiscoveryHandler implements ServerDiscoveryHandler{
	
		//private ByteBuffer dataBuffer = ByteBuffer.allocate(16);
	
		public boolean onDiscoverHost (DatagramChannel datagramChannel, InetSocketAddress fromAddress, Serialization serialization)
			throws IOException {
			
			DiscoveryResponsePacket packet = new DiscoveryResponsePacket();
			packet.id = 420;
			packet.gameName = "EpicSaga";
			packet.playerCount = 1;

			ByteBuffer buffer = ByteBuffer.allocate(256);
			serialization.write(null, buffer, packet);
			buffer.flip();

			datagramChannel.send(buffer, fromAddress);

			return true;
		}

		
	}
	
	class DiscoveryResponsePacket {
	
		public DiscoveryResponsePacket () {
			//
		}
	
		public int id;
		public int playerCount;
		public String gameName;
	}




