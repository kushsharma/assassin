package utils;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.softnuke.epic.MyGame;

import objects.Ghost;
import objects.Player;

public class Recorder {

	public static final short ACTION_NONE = 0;
	public static final short ACTION_LEFT = 1;
	public static final short ACTION_LEFT_STOP = 2;
	public static final short ACTION_RIGHT = 3;
	public static final short ACTION_RIGHT_STOP = 4;
	public static final short ACTION_JUMP = 5;
	public static final short ACTION_JUMP_STOP = 6;
	public static final short ACTION_FIRE = 7;
	public static final short ACTION_FIRE_STOP = 8;
	public static final short ACTION_STAY = 9;
	
	public static boolean RECORDING = false;
	public static boolean PLAYING = false;
	public static final float FRAME_RECORD_DELAY = 0.1f;
	
	Player player;
	int records[] = new int[5];
	Array<RecordFrame> recordTape;
	Array<RecordFrame> last_recordTape;

	Ghost ghost;
	World world;
	
	float time = 0;
	int frame_index = 0;
	
	float last_frame_record_time = 0;
	
	public Recorder(World w){
		world = w;
		
		ghost = new Ghost(world);
		player = Player.getInstance();
		recordTape = new Array<RecordFrame>();
		last_recordTape = new Array<RecordFrame>();
		
		//acquiring memory for recording
		for(int i=0;i<200;i++){
			recordTape.add(new RecordFrame());
			last_recordTape.add(new RecordFrame());
		}
		
	}
	
	public void render(SpriteBatch batch){
		ghost.render(batch);
	}
	
	public void update(float delta){
		time += delta;
		frame_index ++;
		
		ghost.update(delta);

				
		if(RECORDING){
			last_frame_record_time+= delta;
			
			if(last_frame_record_time > FRAME_RECORD_DELAY){
				//record new frame
				last_frame_record_time = 0;
				
				if(Player.getInstance().isDead())
				{
					RECORDING = false;
				}
				else
				if(Player.getInstance().GOT_HIT)
					addFrame(ACTION_STAY);//stop ghost
				else
					addFrame((short) -1);//update position
				
			}
		}
		
		if(PLAYING){
			//send events to ghost
			for(RecordFrame rf:last_recordTape){
				
				if(rf.frame_number == -1){
					//check if recorded tape is finished by checking actual last frame index
					int index = last_recordTape.indexOf(rf, false);
					if(last_recordTape.get(index-1).frame_number < frame_index)
						PLAYING = false;
					break;
				}
				else if(frame_index == rf.frame_number){
					ghost.updateKeys(rf);	
					//break;
					//MyGame.sop("Ghost Move played:"+rf.move);
				}
				

				//MyGame.sop("Ghost Moves:"+rf.move+", frame:"+rf.frame_number+", current frame:"+frame_index);
			}
			
			//check if recorded tape is finished
			//if(last_recordTape.get(last_recordTape.size-1).frame_number < frame_index)
			//	PLAYING = false;
		}
		
		//MyGame.sop(RECORDING +" " +PLAYING);
	}

	
	public void addFrame(short move){
		if(!RECORDING) return;
		
		boolean empty_found = false;
		int index = 0;
		RecordFrame frame = null;
		//get empty frame
		for(RecordFrame rf:recordTape){
			if(rf.frame_number == -1)
			{
				empty_found = true;
				frame = rf;
				break;
			}
			index++;
		}
		
		//MyGame.sop("New move to"+move);
		if(frame == null)
		{
			frame = new RecordFrame((short)frame_index, move);
			//MyGame.sop("New for tape");
		}
		else
		{
			//MyGame.sop("Existing tape at"+index);
			frame.frame_number = (short) frame_index;
			frame.move = move;
		}
		
		frame.posx = Player.getInstance().getPosition().x;
		frame.posy = Player.getInstance().getPosition().y;
		
		frame.vx = Player.getInstance().getBody().getLinearVelocity().x;
		frame.vy = Player.getInstance().getBody().getLinearVelocity().y;			
		
		//frame.weaponAngle = Player.getInstance().getWeapon().getAngle();
		//frame.weaponAngle = Player.getInstance().getWeapon().getAngularVelocity();
		
		//MyGame.sop(" "+recordTape.get(index).frame_number);
		
		//if new frame created, add to pool
		if(!empty_found)
			recordTape.add(frame);
		
	}
	
	public void record(){
		frame_index = 0;
		RECORDING = true;
		MyGame.sop("RECORDING");
		
		if(recordTape.size == 0 || recordTape.get(0).frame_number == -1)
		{
			//recording first time
			ghost.hide();
		}
		
		for(RecordFrame rf:recordTape){
			rf.reset();
		}
		//recordTape.clear();
	}
	
	public void play(){
		//MyGame.sop("record tape"+recordTape.size);
		//MyGame.sop("last record tape"+last_recordTape.size);
		
		if(recordTape.size == 0 || recordTape.get(0).frame_number == -1)
		{
			//no recording to play
			MyGame.sop("No recording to play");
			record();
			return;
			
		}
		
		//last_recordTape.clear();
		//last_recordTape.addAll(recordTape);
		//MyGame.sop("last pool reset"+PLAYING);
		for(RecordFrame rf:last_recordTape){
			rf.reset();
		}
		//copy record tape to lastrecordtape
		int maxi = last_recordTape.size;
		for(int i=0; i< maxi;i++){
		
			//empty now
			if(recordTape.get(i).frame_number == -1)
				break;
			
			last_recordTape.get(i).set(recordTape.get(i));
			
			//MyGame.sop("last pool "+i);
		}
		
		//size small gather more frames
		if(maxi < recordTape.size)
		{	//MyGame.sop("Size small of last pool");
			for(int i = maxi; i < recordTape.size; i++){
				
				//empty now
				if(recordTape.get(i).frame_number == -1)
					break;
				
				//MyGame.sop("New for last");
				RecordFrame frame = new RecordFrame();
				frame.set(recordTape.get(i));
				
				last_recordTape.add(frame);
			}
		}
		
		
		
		ghost.reset();
		
		MyGame.sop("PLAYING");
		
		frame_index = 0;
		PLAYING = true;
		
		
		//also start recording new instance
		record();
	}
	
	public void dispose(){
		PLAYING = false;
		RECORDING = false;
		
		last_recordTape.clear();
		recordTape.clear();
		frame_index = 0;
		
	}

	public void reset() {
		ghost.reset();
		for(RecordFrame rf:recordTape){
			//rf.reset();
		}
		RECORDING = false;
		PLAYING = false;
	}
	
	public void resetTape(){
		reset();
		
		for(RecordFrame rf:recordTape){
			rf.reset();
		}
		for(RecordFrame rf:last_recordTape){
			rf.reset();
		}
		frame_index = 0;
	}
	
	public class RecordFrame{
		
		public short frame_number = -1;
		public short move = 0;
		public float vx = 0;
		public float vy = 0;
		public float posx = 0;
		public float posy = 0;
		
		//public float weaponAngle = 0;
		//public float weaponAngleV = 0;

		public RecordFrame(){}
		
		public RecordFrame(short f, short m){
			frame_number = f;
			move = m;
		}
		
		public void reset(){
			frame_number = -1;
			move = 0;
			
			vx = 0;
			vy = 0;
			posx = 0;
			posy = 0;
		}
		
		public void set(RecordFrame rF){
			if(rF.frame_number == -1)
				return;
			
			frame_number = rF.frame_number;
			move = rF.move;
			
			posx = rF.posx;
			posy = rF.posy;
			
			vx = rF.vx;
			vy = rF.vy;
			
		}
	}
}


