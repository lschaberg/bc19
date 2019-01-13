package bc19;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

//import util.Action;
//import util.BCAbstractRobot;

public class MyRobot extends BCAbstractRobot {
	private AbstractRobot self;
	private int myrTurnCount;
	
    public Action turn() {   
    	if(myrTurnCount == 0){
			if(me.unit == SPECS.CASTLE){
				self = new Castle(this);
			}
			if(me.unit == SPECS.CHURCH){
				self = new Church(this);
			}
			if(me.unit == SPECS.CRUSADER){
				self = new Crusader(this);
			}
			if(me.unit == SPECS.PILGRIM){
				self = new Pilgrim(this);
			}
			if(me.unit == SPECS.PREACHER){
				self = new Preacher(this);
			}
			if(me.unit == SPECS.PROPHET){
				self = new Prophet(this);
			}
    	}
    	myrTurnCount++;
    	return self.robotTurn();
	}
    
    public class AbstractRobot {
    	MyRobot r;

    	public AbstractRobot(){}
    	
    	public Action robotTurn(){
    		return null;
    	}
    	
    	protected class Pair{
    		int x;
    		int y;
    		
    		public Pair(int x, int y){
    			this.x = x;
    			this.y = y;
    		}
    	}
    	
    	public boolean inMap(int x, int y){
    		return x >= 0 && y >= 0 && x < r.getPassableMap()[0].length && y < r.getPassableMap().length;
    	}
    	
    	public boolean isAffordable(int u){
    		int karbCost = 10;
    		int fuelCost = 50;
    		if(u == 1){
    			karbCost = 50;
    			fuelCost = 200;
    		}
    		else if(u == 3)
    			karbCost = 20;
    		else if(u == 4)
    			karbCost = 25;
    		else if(u == 5)
    			karbCost = 30;
    		
    		return karbCost <= r.karbonite && fuelCost <= r.fuel;
    	}
      }

    public class Castle extends AbstractRobot{
    	int turnCount = 0;
    	
    	public Castle(MyRobot r){
    		this.r = r;
    	}
    	
    	public Action robotTurn(){
    		r.log("Turn #" + turnCount);
    		if(turnCount++ % 10 == 0){
    			for(int i = -1; i <= 1; i++){
    				for(int j = -1; j <= 1; j++){
    					if(inMap(r.me.x + j, r.me.y + i) && r.getVisibleRobotMap()[r.me.y + i][r.me.x + j] <= 0 && r.getPassableMap()[r.me.y + i][r.me.x + j] == true && isAffordable(2)){
    						r.log("Attempting to build Pilgrim");
    						return r.buildUnit(2, j, i);
    					}
    				}
    			}
    		}
    		return null;
    	}
    }
    
    private class Church extends AbstractRobot{
    	
    	public Church(MyRobot r){
    		this.r = r;
    	}
    }

    private class CombatRobot extends MovingRobot{

    	public CombatRobot(MyRobot r) {
    		super(r);
    		// TODO Auto-generated constructor stub
    	}
    }

    private class Crusader extends CombatRobot{
    	
    	public Crusader(MyRobot r){
    		super(r);
    	}
    }

    private class MovingRobot extends AbstractRobot{		
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
    		totalPath.add(current);
    		
    		while(cameFrom.containsKey(current)){
    			current = cameFrom.get(current);
    			totalPath.addFirst(current);
    		}
    		
    		totalPath.pollFirst();
    		for(Pair p : totalPath){
    			//r.log(p.x + ", " + p.y);
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
    		passableMap[r.me.x][r.me.y] = new Pair(r.me.x, r.me.y);
    		
    		
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
    		
    		/*class PairComparator<Pair> implements Comparator<Pair>{
    			public int compare(Pair arg0, Pair arg1) {
    				//System.out.println("comparator output: " + fScore.get(arg0) + " compared to " + fScore.get(arg1) + " gets " + (int) Math.signum(fScore.get(arg0) - fScore.get(arg1)));
    				return (int) Math.signum(fScore.get(arg0) - fScore.get(arg1));
    			}
    		}*/
    				
    		HashSet<Pair> closedSet = new HashSet<Pair>(4096);  //potential overuse of memory
    		//PriorityQueue<Pair> openSet = new PriorityQueue<Pair>(4096, new PairComparator<Pair>());
    		ArrayList<Pair> openSet = new ArrayList<Pair>(4096);
    		openSet.add(passableMap[r.me.x][r.me.y]);
    		
    		Pair current;
    		while(!openSet.isEmpty()){
    			//current = openSet.poll();
    			current = openSet.get(0);
    			for(Pair p : openSet){
    				if(fScore.get(p) < fScore.get(current))
    					current = p;
    			}

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
    		if(path.isEmpty())
    			return null;
    		Pair nextStep = path.poll();
    		//r.log("Move distance:" + ((nextStep.x - r.me.x)*(nextStep.x - r.me.x) + (nextStep.y - r.me.y)*(nextStep.y - r.me.y)));
    		//r.log("Fuel: " + r.fuel);
    		if(((nextStep.x - r.me.x)*(nextStep.x - r.me.x) + (nextStep.y - r.me.y)*(nextStep.y - r.me.y)) *  Math.max(1, r.me.unit - 2) > r.fuel){
    			path.addFirst(nextStep);
    			return null;
    		}
			if(r.getVisibleRobotMap()[target.y][target.x] > 0){
				return targetBlocked();
			}
    		if(r.getVisibleRobotMap()[nextStep.y][nextStep.x] > 0){
				getPathTo(target);
				return followPath();
    		}
    		return r.move(nextStep.x - r.me.x, nextStep.y - r.me.y);
    	}
    	
    	public Action targetBlocked(){
    		return null;
    	}
    	
    	protected Deque<Pair> search(boolean[][] map){ //Dijkstras that somehow combines passableMap and the map being searched to get the quickest movement there
    		Pair[][] passableMap = new Pair[r.getPassableMap()[0].length][r.getPassableMap().length];
    		for(int i = 0; i < passableMap.length; i++){
    			for(int j = 0; j < passableMap[i].length; j++){
    				if(r.getPassableMap()[j][i] && r.getVisibleRobotMap()[j][i] < 1){
    					passableMap[i][j] = new Pair(i, j);
    				}
    			}
    		}
    		passableMap[r.me.x][r.me.y] = new Pair(r.me.x, r.me.y);
    		
    		
    		HashMap<Pair, Pair> cameFrom = new HashMap<Pair, Pair>(4096);
    		
    		HashMap<Pair, Integer> gScore = new HashMap<Pair, Integer>(4096);
    		for(int i = 0; i < passableMap.length; i++){
    			for(int j = 0; j < passableMap[i].length; j++){
    				if(passableMap[i][j] != null){
    					gScore.put(passableMap[i][j], Integer.MAX_VALUE);
    				}
    			}
    		}
    		gScore.put(passableMap[r.me.x][r.me.y], 0);
    		
    		HashSet<Pair> closedSet = new HashSet<Pair>(4096);  //potential overuse of memory
    		Queue<Pair> openSet = new LinkedList<Pair>();
    		openSet.add(passableMap[r.me.x][r.me.y]);
    		
    		Pair current;
    		while(!openSet.isEmpty()){
    			current = openSet.poll();
    			if(map[current.y][current.x]){
    				return reconstructPath(cameFrom, current);
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
    				
    				if(!inOpen)
    					openSet.add(neighbor);
    			}
    		}
    		
    		return null;
    	}
    }

    private class Pilgrim extends MovingRobot{
    	public Pilgrim(MyRobot r){
    		super(r);
    	}
    	
    	int turnCount = 0;

    	private static final int TRAVELING = 0;
    	private static final int FARMING = 1;
    	private static final int DEPOSITING = 2;
    	private Integer state;
    	
    	private Pair resourcePatch; //durable record of where this pilgrim is farming, only set when it has been reached and claimed
    	
    	private boolean[][] availableFuel;
    	private boolean[][] availableKarbonite;
    	
    	public Action robotTurn(){
    		if(turnCount == 0){
	    		boolean[][] tempMap = r.getFuelMap();
	    		availableFuel = new boolean[tempMap.length][];
	    		for(int i = 0; i < availableFuel.length; i++){
	    			availableFuel[i] = tempMap[i].clone();
	    		}
	    		tempMap = r.getKarboniteMap();
	    		availableKarbonite = new boolean[tempMap.length][];
	    		for(int i = 0; i < availableKarbonite.length; i++){
	    			availableKarbonite[i] = tempMap[i].clone();
	    		}
    			path = search(availableFuel);
    			Deque<Pair> tempPath = search(availableKarbonite);
    			if(path.size() > tempPath.size())
    				path = tempPath;
    			if(!path.isEmpty()){
	    			target = path.pollLast();
	    			path.add(target);
    			}
    			else{
    				target = new Pair(r.me.x, r.me.y);
    			}
    			
	    		state = TRAVELING;
    		}
    		
    		turnCount++;
    		switch(state){
    		case TRAVELING: return traveling();
    		case FARMING: return farming();
    		case DEPOSITING: return depositing();
    		}
    		
    		return null;
    	}
    	
    	public Action traveling(){
    		//if target reached set resourcePatch to target and state to farming type resource
    		if(path.isEmpty()){
    			resourcePatch = target;
    			state = FARMING;
    			return farming();
    		}
			for(Robot bot : r.getVisibleRobots()){
				if(bot.id == r.me.id)
					continue;
				availableFuel[bot.y][bot.x] = false;
				availableKarbonite[bot.y][bot.x] = false;
			}
    		return followPath();
    	}
    	
    	public Action farming(){
    		//if full change target to nearest castle/church and state to depositing
    		if(r.me.fuel > 95 || r.me.karbonite >= 20){
    			boolean hasNearbyBuilding = false;
    			boolean[][] nearbyBuildings = new boolean[availableFuel.length][availableFuel[0].length];
    			Robot[] visibleRobots = r.getVisibleRobots();
    			for(int i = 0; i < visibleRobots.length; i++){
					if(visibleRobots[i].unit <= 1 && visibleRobots[i].team == r.me.team){
						for(int j = -1; j <= 1; j++){
							for(int k = -1; k <= 1; k++){
								if(inMap(visibleRobots[i].x + j, visibleRobots[i].y + k) && r.getPassableMap()[visibleRobots[i].y + k][visibleRobots[i].x + j] && (r.getVisibleRobotMap()[visibleRobots[i].y + k][visibleRobots[i].x + j] <= 0 || r.getVisibleRobotMap()[visibleRobots[i].y + k][visibleRobots[i].x + j] == r.me.id)){
									nearbyBuildings[visibleRobots[i].y + k][visibleRobots[i].x + j] = true;
									hasNearbyBuilding = true;
								}
							}
						}
					}
    			}
    			if(hasNearbyBuilding){
    				path = search(nearbyBuildings);
    				if(!path.isEmpty()){
	        			target = path.pollLast();
	        			path.add(target);
    				}
        			state = DEPOSITING;
    				return depositing();
    			}
    			r.log("No nearby buildings, attempting to build church");
    			for(int i = -1; i <= 1; i++){
    				for(int j = -1; j <= 1; j++){
    					if(r.getVisibleRobotMap()[r.me.y + i][r.me.x + j] <= 0 && r.getPassableMap()[r.me.y + i][r.me.x + j] == true && r.getKarboniteMap()[r.me.y + i][r.me.x + j] == false && r.getFuelMap()[r.me.y + i][r.me.x + j] == false){
    						return r.buildUnit(1, j, i);
    					}
    				}
    			}
    			for(int i = -1; i <= 1; i++){
    				for(int j = -1; j <= 1; j++){
    					if(r.getVisibleRobotMap()[r.me.y + i][r.me.x + j] <= 0 && r.getPassableMap()[r.me.y + i][r.me.x + j] == true){
    						return r.buildUnit(1, j, i);
    					}
    				}
    			}
    		}
    		//farm
    		return r.mine();
    	}
    	
    	public Action depositing(){
    		//if adjacent to target then deposit and change target to resourcePatch and change state to traveling
    		if(path.isEmpty()){
    			for(int i = -1; i <= 1; i++){
    				for(int j = -1; j <= 1; j++){
    					if(r.getVisibleRobotMap()[r.me.y + i][r.me.x + j] > 0 && r.getRobot(r.getVisibleRobotMap()[r.me.y + i][r.me.x + j]).unit <= 1 && r.getRobot(r.getVisibleRobotMap()[r.me.y + i][r.me.x + j]).team == r.me.team){
    						path = getPathTo(resourcePatch);
    						target = resourcePatch;
    						state = TRAVELING;
    						r.log("Giving: " + r.me.karbonite + " karbonite and " + r.me.fuel + " fuel to (" + j + ", " + i + ")");
    						return r.give(j, i, r.me.karbonite, r.me.fuel);
    					}
    				}
    			}
    		}
    		//followPath to target
    		return followPath();
    	}
    	
    	public Action targetBlocked(){
    		if(state == TRAVELING){
    			path = search(availableFuel);
    			Deque<Pair> tempPath = search(availableKarbonite);
    			if(path == null || (tempPath != null && path.size() > tempPath.size()))
    				if(path == null && tempPath == null)
    					return null; //party time
    				path = tempPath;
    			target = path.pollLast();
    			path.add(target);
    			return followPath();
    		}
    		if(state == DEPOSITING){
    			return farming();
    		}
    		return null;
    	}
    }
    
    private class Preacher extends CombatRobot{
    	public Preacher(MyRobot r){
    		super(r);
    	}
    }

    private class Prophet extends CombatRobot{
    	public Prophet(MyRobot r){
    		super(r);
    	}
    }
}
