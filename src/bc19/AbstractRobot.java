package bc19;

public abstract class AbstractRobot {
	MyRobot r;

	public Action turn;
	
	protected class Pair{
		int x;
		int y;
		
		public Pair(int x, int y){
			this.x = x;
			this.y = y;
		}
	}
}
