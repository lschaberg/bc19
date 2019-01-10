package bc19;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;


public class MovingRobot extends AbstractRobot{	
	protected Deque<Pair> path;
	protected Pair target;
	int moveSpeed;

	public MovingRobot(MyRobot r){
		this.r = r;
		moveSpeed = 4;
		if(r.me.unit == r.SPECS.CRUSADER){
			moveSpeed = 9;
		}
	}
	
	private Deque<Pair> reconstructPath(HashMap<Pair, Pair> cameFrom, Pair current){
		Deque<Pair> totalPath = new LinkedList<Pair>();
		
		while(cameFrom.containsKey(current)){
			current = cameFrom.get(current);
			totalPath.add(current);
		}
		
		return totalPath;
	}

	private double getHeuristicValue(Pair curr, Pair target){
		return Math.sqrt(Math.pow(target.x - curr.x, 2) + Math.pow(target.y - curr.y, 2));
	}

	private ArrayList<Pair> getPassableNeighbors(Pair curr, Pair[][] passableMap){
		ArrayList<Pair> neighbors = new ArrayList<Pair>(24);
		for(int i = -moveSpeed; i <= moveSpeed; i++){
			for(int j = -moveSpeed; j <= moveSpeed; j++){
				if(curr.x + i < passableMap.length && curr.y + j < passableMap[curr.x].length && curr.x + i >= 0 && curr.y + j >= 0 && passableMap[curr.x + i][curr.y + j] != null && i*i +j*j <= moveSpeed)
					neighbors.add(passableMap[curr.x + i][curr.y + j]);
			}
		}
		return neighbors;
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
		fScore.put(passableMap[r.me.x][r.me.y], getHeuristicValue(new Pair(r.me.x, r.me.y), target));		
		
		class PairComparator<Pair> implements Comparator<Pair>{
			public int compare(Pair arg0, Pair arg1) {
				//System.out.println("comparator output: " + fScore.get(arg0) + " compared to " + fScore.get(arg1) + " gets " + (int) Math.signum(fScore.get(arg0) - fScore.get(arg1)));
				return (int) Math.signum(fScore.get(arg0) - fScore.get(arg1));
			}
		}
				
		HashSet<Pair> closedSet = new HashSet<Pair>(4096);  //potential overuse of memory
		PriorityQueue<Pair> openSet = new PriorityQueue<Pair>(4096, new PairComparator<Pair>());
		openSet.add(passableMap[r.me.x][r.me.y]);
		
		Pair current;
		while(!openSet.isEmpty()){
			current = openSet.poll();
			if(current.equals(target)){
				path = reconstructPath(cameFrom, current);
				return path;
			}
			
			openSet.remove(current);
			closedSet.add(current);
			
			for(Pair neighbor : getPassableNeighbors(current, passableMap)){
				//System.out.println("checking neighbor: " + neighbor.x + ", " + neighbor.y);
				if(closedSet.contains(neighbor))
					continue;
				
				int tentativeGScore = gScore.get(current) + 1;
				
				boolean inOpen = openSet.contains(neighbor);
				if(inOpen && tentativeGScore >= gScore.get(neighbor))
					continue;
				
				cameFrom.put(neighbor, current);
				gScore.put(neighbor, tentativeGScore);
				fScore.put(neighbor, tentativeGScore + getHeuristicValue(neighbor, target));
				
				if(!inOpen)
					openSet.add(neighbor);
			}
		}
		
		return null;
	}
	
	public Action followPath(){
		if(r.me.x == target.x && r.me.y == target.y)
			return null;
		Pair nextStep = path.poll();
		if(r.getVisibleRobotMap()[nextStep.y][nextStep.x] > 0){
			if(r.getVisibleRobotMap()[target.x][target.y] > 0){
				return targetBlocked();
			}
			else{
				getPathTo(target);
				followPath();
			}
		}
		return r.move(target.x - r.me.x, target.y - r.me.y);
	}
	
	public Action targetBlocked(){
		return null;
	}
	
	protected Pair search(Pair origin, boolean[][] map){ //Dijkstras that somehow combines passableMap and the map being searched to get the quickest movement there
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
		fScore.put(passableMap[r.me.x][r.me.y], getHeuristicValue(new Pair(r.me.x, r.me.y), target));		
		
		class PairComparator<Pair> implements Comparator<Pair>{
			public int compare(Pair arg0, Pair arg1) {
				//System.out.println("comparator output: " + fScore.get(arg0) + " compared to " + fScore.get(arg1) + " gets " + (int) Math.signum(fScore.get(arg0) - fScore.get(arg1)));
				return (int) Math.signum(fScore.get(arg0) - fScore.get(arg1));
			}
		}
				
		HashSet<Pair> closedSet = new HashSet<Pair>(4096);  //potential overuse of memory
		PriorityQueue<Pair> openSet = new PriorityQueue<Pair>(4096, new PairComparator<Pair>());
		openSet.add(passableMap[r.me.x][r.me.y]);
		
		Pair current;
		while(!openSet.isEmpty()){
			current = openSet.poll();
			if(current.equals(target)){
				path = reconstructPath(cameFrom, current);
				return null;
			}
			
			openSet.remove(current);
			closedSet.add(current);
			
			for(Pair neighbor : getPassableNeighbors(current, passableMap)){
				//System.out.println("checking neighbor: " + neighbor.x + ", " + neighbor.y);
				if(closedSet.contains(neighbor))
					continue;
				
				int tentativeGScore = gScore.get(current) + 1;
				
				boolean inOpen = openSet.contains(neighbor);
				if(inOpen && tentativeGScore >= gScore.get(neighbor))
					continue;
				
				cameFrom.put(neighbor, current);
				gScore.put(neighbor, tentativeGScore);
				fScore.put(neighbor, tentativeGScore + getHeuristicValue(neighbor, target));
				
				if(!inOpen)
					openSet.add(neighbor);
			}
		}
		
		return null;
	}
}
