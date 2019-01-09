package bc19;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;


public class MovingRobot extends AbstractRobot{
	protected Deque<Pair> path;
	
	private Deque<Pair> reconstructPath(HashMap<Pair, Pair> cameFrom, Pair current){
		Deque<Pair> totalPath = new LinkedList<Pair>();
		totalPath.add(current);
		
		while(cameFrom.containsKey(current)){
			current = cameFrom.get(current);
			totalPath.add(current);
		}
		
		return totalPath;
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
		fScore.put(passableMap[r.me.x][r.me.y], Math.sqrt(Math.pow(target.x - r.me.x, 2) + Math.pow(target.y - r.me.y, 2)));
		
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
