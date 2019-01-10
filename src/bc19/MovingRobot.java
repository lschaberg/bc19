package bc19;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;


public class MovingRobot extends AbstractRobot{
	protected Deque<Pair> path;
	int moveSpeed;

	public MovingRobot(MyRobot r){
		super(r);
		moveSpeed = 4;
		if(r.unit == SPECS.CRUSADER){
			movespeed = 9;
		}
	}
	
	private Deque<Pair> reconstructPath(HashMap<Pair, Pair> cameFrom, Pair current){
		Deque<Pair> totalPath = new LinkedList<Pair>();
		totalPath.add(current);
		
		while(cameFrom.containsKey(current)){
			current = cameFrom.get(current);
			totalPath.add(current);
		}
		
		return totalPath;
	}

	private double getHeuristicValue(Pair curr, Pair target){
		return Math.sqrt(Math.pow(target.x - curr.x, 2) + Math.pow(target.y - curr.y, 2)
	}

	private Pair[] getNeighbors(Pair curr, Pair[][] passableMap){


		Pair[] neighbors = new Pair[(Math.sqrt(moveSpeed)-1)*12]

		for(int i = -moveSpeed; i < moveSpeed; i++){
			for(int j = -(moveSpeed - Math.abs(i)); j < moveSpeed - Math.abs(i); j++){

			}
		}
	}
	
	
	protected Deque<Pair> getPathTo(Pair target){
		Pair[][] passableMap = new Pair[r.getPassableMap()[0].length][r.getPassableMap().length];
		for(int i = 0; i < passableMap.length; i++){
			for(int j = 0; j < passableMap[i].length; j++){
				if(r.getPassableMap()[j][i] && r.getVisibleRobotMap()[j][i] < 1){
					passableMap[i][j] = new Pair(i, j);
				}
			}
		}
		passableMap[target.x][target.y] = target;
		
		HashMap<Pair, Pair> cameFrom = new HashMap<Pair, Pair>(4096);
		
		HashMap<Pair, Integer> gScore = new HashMap<Pair, Integer>(4096);
		HashMap<Pair, Double> fScore = new HashMap<Pair, Double>(4096);
		for(int i = 0; i < passableMap.length; i++){
			for(int j = 0; j < passableMap[i].length; j++){
				if(passableMap[i][j] != null){
					gScore.put(passableMap[i][j], Integer.MAX_VALUE);
					fScore.put(passableMap[i][j], Double.MAX_VALUE);
				}
			}
		}
		gScore.put(passableMap[r.me.x][r.me.y], 0);
		fScore.put(passableMap[r.me.x][r.me.y], getHeuristicValue(new Node(r.me.x, r.me.y), target);
		
		class PairComparator<Pair> implements Comparator<Pair>{
			public int compare(Pair arg0, Pair arg1) {
				return (int) Math.signum(fScore.get(arg0) - fScore.get(arg1));
			}
			
		}
		
		ArrayList<Pair> closedSet = new ArrayList<Pair>(4096);  //potential overuse of memory
		PriorityQueue<Pair> openSet = new PriorityQueue<Pair>(4096, new PairComparator<Pair>());
		
		Pair current;
		while(!openSet.isEmpty()){
			current = openSet.poll();
			if(current == target){
				return reconstructPath(cameFrom, current);
			}
			
			openSet.remove(current);
			closedSet.add(current);
			
			//for each neighbor of current
		}
		
		return null;
	}
}
