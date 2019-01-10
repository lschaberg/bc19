package bc19;

public class Pilgrim extends MovingRobot{
	public Pilgrim(MyRobot r){
		super(r);
		//getPathTo  search for fuel/karbonite
		state = State.TRAVELING;
	}
	
	private enum State {
		TRAVELING, FARMINGFUEL, DEPOSITINGFUEL, FARMINGKARBONITE, DEPOSITINGKARBONITE;
	}
	private State state;
	
	private Pair resourcePatch; //durable record of where this pilgrim is farming, only set when it has been reached and claimed
	
	public Action turn(){
		switch(state){
		case TRAVELING: return traveling();
		case FARMINGFUEL: return farmingFuel();
		case DEPOSITINGFUEL: return depositingFuel();
		case FARMINGKARBONITE: return farmingKarbonite();
		case DEPOSITINGKARBONITE: return depositingKarbonite();
		}
		
		return null;
	}
	
	public Action traveling(){
		//if blocked change target to next closest resource of type target
		//if target reached set resourcePatch to target and state to farming type resource
		//followPath to target
		return null;
	}
	
	public Action farmingFuel(){
		//if full change target to nearest castle/church and state to depositing
		//farm
		return null;
	}
	
	public Action depositingFuel(){
		//if adjacent to target then deposit and change target to resourcePatch and change state to traveling
		//followPath to target
		return null;
	}
	
	public Action farmingKarbonite(){
		//if full change target to nearest castle/church and state to depositing
		//farm
		return null;
	}
	
	public Action depositingKarbonite(){
		//if adjacent to target then deposit and change target to resourcePatch and change state to traveling
		//followPath to target
		return null;
	}
}
