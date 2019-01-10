package bc19;

public class Pilgrim extends MovingRobot{
	public Pilgrim(MyRobot r){
		super(r);
		
		//getPathTo  search for fuel/karbonite
		state = State.TRAVELING;
	}
	
	private State state;
	
	public Action turn(){
		
		return null;
	}
	
	private enum State {
		TRAVELING, FARMINGFUEL, FARMINGKARBONITE;
	}
}
