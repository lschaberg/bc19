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
    		
    		public String toString(){
    			return "(" + x + ", " + y + ")";
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
    			karbCost = 15;
    		else if(u == 4)
    			karbCost = 25;
    		else if(u == 5)
    			karbCost = 30;
    		
    		return karbCost <= r.karbonite && fuelCost <= r.fuel;
    	}
    	
    	public Action tryBuild(int u){
			for(int i = -1; i <= 1; i++){
				for(int j = -1; j <= 1; j++){
					if(inMap(r.me.x + j, r.me.y + i) && r.getVisibleRobotMap()[r.me.y + i][r.me.x + j] <= 0 && r.getPassableMap()[r.me.y + i][r.me.x + j] == true && isAffordable(u)){
						return r.buildUnit(u, j, i);
					}
				}
			}
    	}
    	
    	public int getDistance(Robot bot){
    		return (r.me.x - bot.x)*(r.me.x - bot.x) + (r.me.y - bot.y)*(r.me.y - bot.y);
    	}
    	
    	public int getDistance(int x, int y){
    		return (r.me.x - x)*(r.me.x - x) + (r.me.y - y)*(r.me.y - y);
    	}
    	
    	public int getDistance(int x, int y, int x2, int y2){
    		return (x2 - x)*(x2 - x) + (y2 - y)*(y2 - y);
    	}
    	
    	public int attackRange(Robot bot){
    		if(bot.unit == 0)
    			return 100;
    		if(bot.unit == 3)
    			return 16;
    		if(bot.unit == 4)
    			return 64;
    		if(bot.unit == 5)
    			return 16;
    	}
    	
    	public Action tryMove(Pair... moves){
    		for(Pair move : moves){
    			if(inMap(r.me.x + move.x, r.me.y + move.y) && r.getPassableMap()[r.me.y + move.y][r.me.x + move.x] && r.getVisibleRobotMap()[r.me.y + move.y][r.me.x + move.x] <= 0){
    				return r.move(move.x, move.y);
    			}
    		}
    		return null;
    	}
    	
    	public boolean passable(int y, int x){
    		return inMap(x, y) && r.getPassableMap()[y][x];
    	}
    }

    public class Castle extends AbstractRobot{
    	int turnCount = 0;
    	ArrayList<Robot> otherCastles = new ArrayList<Robot>(2);
    	ArrayList<Integer> compressedCastles = new ArrayList<Integer>(2);
    	int nearbyResources = 0;
    	int assignedResources = 0;
    	int nearbyKarbonite = 0;
    	boolean verticallySymmetric = false;
    	boolean topOrLeft = false;
		int magicCompressedNumber = 0;
		boolean closestCastle = true;
		int karboniteReserve = 60;
		int phase = 2;
		int pilgrimsBuilt = 0;
		int fuelReserve = 200;
    	
    	public Castle(MyRobot r){
    		this.r = r;
    	}
    	
    	public Action robotTurn(){
    		r.log("Turn #" + turnCount);
    		if(turnCount == 0){
    			for(int i = 0; i < r.getPassableMap().length/2; i++){
    				for(int j = 0; j < r.getPassableMap()[0].length/2; j++){
    					if(r.getPassableMap()[i][j] != r.getPassableMap()[r.getPassableMap().length -1 - i][j]){
    						verticallySymmetric = true;
    					}
    				}
    			}
    			if(verticallySymmetric){
    				topOrLeft = r.getKarboniteMap()[0].length - r.me.x * 2 > 0;
    			}
    			else{
    				topOrLeft = r.getKarboniteMap().length - r.me.y * 2 > 0;
    			}
				if(verticallySymmetric){
					if(topOrLeft){
        				magicCompressedNumber = (r.me.y/4 << 3)  + r.me.x/4;
					}
					else{
        				magicCompressedNumber = (r.me.y/4 << 3)  + (r.getKarboniteMap()[0].length - 1 - r.me.x)/4;
					}
				}
				else{
					if(topOrLeft){
        				magicCompressedNumber = (r.me.x/4 << 3)  + r.me.y/4;
					}
					else{
        				magicCompressedNumber = (r.me.x/4 << 3)  + (r.getKarboniteMap().length - 1 - r.me.y)/4;
					}
				}
				String binaryString = "";
				int magicCopy = magicCompressedNumber;
				for(int i = 0; i < 9; i++){
					binaryString += magicCopy % 2;
					magicCopy = magicCopy/2;
				}
				r.castleTalk(128 + magicCompressedNumber);
				turnCount++;
    			for(Robot bot : r.getVisibleRobots()){
    				if(bot.team == r.me.team && bot.id != r.me.id && bot.castle_talk != 0){
    					otherCastles.add(bot);
    					compressedCastles.add(bot.castle_talk - 128);
    				}
    			}
    			pilgrimsBuilt++;
    			return tryBuild(2);
    		}
    		if(turnCount == 1){
				r.castleTalk(128 + magicCompressedNumber);
    			for(Robot bot : r.getVisibleRobots()){
    				if(bot.team == r.me.team && bot.id != r.me.id && bot.castle_talk != 0){
    					otherCastles.add(bot);
    					compressedCastles.add(bot.castle_talk - 128);
    				}
    			}
    			for(int m : compressedCastles){ //the castle with the largest short is the closest to its opposite, if 2 are equal then at least for now treat them both as closest
    				if(m % 8 > magicCompressedNumber % 8){
    					closestCastle = false;
    					break;
    				}
    			}
    			if(closestCastle && magicCompressedNumber % 8 == compressedCastles.get(0) % 8){
    				if(verticallySymmetric){
    					closestCastle = Math.abs((magicCompressedNumber >> 3) % 16 - r.getKarboniteMap().length/4) > Math.abs((compressedCastles.get(0) >> 3) % 16 - r.getKarboniteMap().length/4);
    				}
    				else{
    					closestCastle = Math.abs((magicCompressedNumber >> 3) % 16 - r.getKarboniteMap()[0].length/4) > Math.abs((compressedCastles.get(0) >> 3) % 16 - r.getKarboniteMap()[0].length/4);
    				}
    			}
    			if(closestCastle && magicCompressedNumber % 8 == compressedCastles.get(1) % 8){
    				if(verticallySymmetric){
    					closestCastle = Math.abs((magicCompressedNumber >> 3) % 16 - r.getKarboniteMap().length/4) > Math.abs((compressedCastles.get(1) >> 3) % 16 - r.getKarboniteMap().length/4);
    				}
    				else{
    					closestCastle = Math.abs((magicCompressedNumber >> 3) % 16 - r.getKarboniteMap()[0].length/4) > Math.abs((compressedCastles.get(1) >> 3) % 16 - r.getKarboniteMap()[0].length/4);
    				}
    			}
    			
    			if(verticallySymmetric){
    				if(topOrLeft){
    					for(int i = 0; i <= r.getPassableMap().length; i++){
    						for(int j = 0; j < r.getPassableMap()[0].length/2 - 2; j++){
    							if(inMap(j, i) && (r.karboniteMap[i][j] || r.fuelMap[i][j])){
    								boolean nearMe = true;
    								for(Integer c : compressedCastles){
    									if(getDistance(j, i, r.me.x/4 *4, r.me.y/4 *4) > getDistance(j, i, (c % 8) * 4, ((c >> 3) % 16) * 4)){
    										nearMe = false;
    									}
    								}
    								if(nearMe){
    									assignedResources++;
    									if(getDistance(j, i) < 25){
    										nearbyResources++;
    									}
    								}
    							}
    						}
    					}
    				}
    				else{
    					for(int i = 0; i <= r.getPassableMap().length; i++){
    						for(int j = r.getPassableMap()[0].length/2 + 2; j < r.getPassableMap()[0].length; j++){
    							if(inMap(j, i) && (r.karboniteMap[i][j] || r.fuelMap[i][j])){
    								boolean nearMe = true;
    								for(Integer c : compressedCastles){
    									if(getDistance(j, i, (r.getPassableMap()[0].length - r.me.x)/4 *4, r.me.y/4 *4) > getDistance(j, i, c % 8, (c >> 3) % 16)){
    										nearMe = false;
    									}
    								}
    								if(nearMe){
    									assignedResources++;
    									if(getDistance(j, i) < 25){
    										nearbyResources++;
    									}
    								}
    							}
    						}
    					}
    				}
    			}
    			else{
    				if(topOrLeft){
    					for(int i = 0; i <= r.getPassableMap()[0].length; i++){
    						for(int j = 0; j < r.getPassableMap().length/2 - 2; j++){
    							if(inMap(i, j) && (r.karboniteMap[j][i] || r.fuelMap[j][i])){
    								boolean nearMe = true;
    								for(Integer c : compressedCastles){
    									if(getDistance(i, j, r.me.x/4 *4, r.me.y/4 *4) > getDistance(i, j, ((c >> 3) % 16) * 4, (c % 8) * 4)){
    										nearMe = false;
    									}
    								}
    								if(nearMe){
    									assignedResources++;
    									if(getDistance(i, j) < 25){
    										nearbyResources++;
    									}
    								}
    							}
    						}
    					}
    				}
    				else{
    					for(int i = 0; i <= r.getPassableMap()[0].length; i++){
    						for(int j = r.getPassableMap().length/2 + 2; j < r.getPassableMap().length; j++){
    							if(inMap(i, j) && (r.karboniteMap[j][i] || r.fuelMap[j][i])){
    								boolean nearMe = true;
    								for(Integer c : compressedCastles){
    									if(getDistance(i, j, r.me.x/4 *4, (r.getPassableMap().length - r.me.y)/4 *4) > getDistance(i, j, ((c >> 3) % 16) * 4, (c % 8) * 4)){
    										nearMe = false;
    									}
    								}
    								if(nearMe){
    									assignedResources++;
    									if(getDistance(i, j) < 25){
    										nearbyResources++;
    									}
    								}
    							}
    						}
    					}
    				}
    			}
    		}
    		
    		turnCount++;
    		
    		int enemyCrusaders = 0;
    		int enemyRangers = 0;
    		int enemyMages = 0;
    		int ourCrusaders = 0;
    		int ourRangers = 0;
    		int ourMages = 0;
    		int ourPilgrims = 0;
    		Robot closestEnemy = null;
    		boolean lineFull = true;
    		for(Robot bot : r.getVisibleRobots()){
    			if(bot.castle_talk == 99 || turnCount < 100){
    				lineFull = false;
    			}
    			if(bot.team != r.me.team){
        			if(closestEnemy == null || getDistance(bot) < getDistance(closestEnemy))
        				closestEnemy = bot;
    				if(bot.unit == 3){
    					enemyCrusaders++;
    				}
    				else if(bot.unit == 4){
    					enemyRangers++;
    				}
    				else if(bot.unit == 5){
    					enemyMages++;
    				}
    			}
    			else if(getDistance(bot) <= 100){
    				if(bot.unit == 2){
    					ourPilgrims++;
    				}
    				if(bot.unit == 3){
    					ourCrusaders++;
    				}
    				else if(bot.unit == 4){
    					ourRangers++;
    				}
    				else if(bot.unit == 5){
    					ourMages++;
    				}
    			}
    		}
    		if((lineFull && phase == 2) || turnCount == 319){
    			r.signal(1452, 4096);
    			phase = 3;
    		}
    		    		
    		if(isAffordable(5) && enemyMages  > ourMages){ //change to preachers if rangers have problems
    			return tryBuild(5);
    		}
    		if(isAffordable(5) && enemyCrusaders > ourMages){
    			return tryBuild(5);
    		}
    		if(isAffordable(4) && enemyRangers > ourRangers){
    			return tryBuild(4);
    		}
    		if(closestEnemy != null){
    			return r.attack(closestEnemy.x - r.me.x, closestEnemy.y - r.me.y);
    		}
    		if(pilgrimsBuilt < assignedResources && r.karbonite >= 40 && isAffordable(2)){
    			pilgrimsBuilt++;
    			return tryBuild(2);
    		}
			if(isAffordable(4) && r.karbonite >= karboniteReserve + 50 - ((magicCompressedNumber % 8) * 5) && r.fuel >= fuelReserve + 50){
				return tryBuild(4);
			}
    		return null;
    	}
    }
    
    private class Church extends AbstractRobot{
    	public Church(MyRobot r){
    		this.r = r;
    	}
    	
		int fuelReserve = 200;
		int karboniteReserve = 60;
		int magicCompressedNumber;
		boolean verticallySymmetric = false;
		boolean topOrLeft = false;
		boolean firstTurn = true;
    	
    	public Action robotTurn(){
    		if(firstTurn){
    			for(int i = 0; i < r.getPassableMap().length/2; i++){
    				for(int j = 0; j < r.getPassableMap()[0].length/2; j++){
    					if(r.getPassableMap()[i][j] != r.getPassableMap()[r.getPassableMap().length -1 - i][j]){
    						verticallySymmetric = true;
    					}
    				}
    			}
    			if(verticallySymmetric){
    				topOrLeft = r.getKarboniteMap()[0].length - r.me.x * 2 > 0;
    			}
    			else{
    				topOrLeft = r.getKarboniteMap().length - r.me.y * 2 > 0;
    			}
				if(verticallySymmetric){
					if(topOrLeft){
        				magicCompressedNumber = (r.me.y/4 << 3)  + r.me.x/4;
					}
					else{
        				magicCompressedNumber = (r.me.y/4 << 3)  + (r.getKarboniteMap()[0].length - 1 - r.me.x)/4;
					}
				}
				else{
					if(topOrLeft){
        				magicCompressedNumber = (r.me.x/4 << 3)  + r.me.y/4;
					}
					else{
        				magicCompressedNumber = (r.me.x/4 << 3)  + (r.getKarboniteMap().length - 1 - r.me.y)/4;
					}
				}
				firstTurn = false;
    		}
    		
    		int enemyCrusaders = 0;
    		int enemyRangers = 0;
    		int enemyMages = 0;
    		int enemyEcon = 0;
    		int ourCrusaders = 0;
    		int ourRangers = 0;
    		int ourMages = 0;
    		int ourPilgrims = 0;
    		Robot closestEnemy = null;
    		for(Robot bot : r.getVisibleRobots()){
    			if(bot.team != r.me.team && getDistance(bot) <= 100){
        			if(closestEnemy == null || getDistance(bot) < getDistance(closestEnemy))
        				closestEnemy = bot;
        			if(bot.unit == 1 || bot.unit == 2){
        				enemyEcon++;
        			}
    				if(bot.unit == 3){
    					enemyCrusaders++;
    				}
    				else if(bot.unit == 4){
    					enemyRangers++;
    				}
    				else if(bot.unit == 5){
    					enemyMages++;
    				}
    			}
    			else if(getDistance(bot) <= 100){
    				if(bot.unit == 2){
    					ourPilgrims++;
    				}
    				if(bot.unit == 3){
    					ourCrusaders++;
    				}
    				else if(bot.unit == 4){
    					ourRangers++;
    				}
    				else if(bot.unit == 5){
    					ourMages++;
    				}
    			}
    		}
    		    		
    		if(isAffordable(5) && enemyMages + enemyCrusaders > ourMages){ //change to preachers if rangers have problems
    			return tryBuild(5);
    		}
    		if(isAffordable(4) && enemyRangers + enemyEcon > ourRangers){
    			return tryBuild(4);
    		}
    		if(isAffordable(4) && r.karbonite >= karboniteReserve + 50 - ((magicCompressedNumber % 8) * 5) && r.fuel >= fuelReserve + 50){
    			return tryBuild(4);
    		}
    		return null;
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
    	
    	private Deque<Pair> reconstructPath(Pair[][] cameFrom, Pair current){
    		Deque<Pair> totalPath = new LinkedList<Pair>();
    		totalPath.add(current);
    		
    		while(cameFrom[current.x][current.y] != null){
    			current = cameFrom[current.x][current.y];
    			totalPath.addFirst(current);
    		}
    		
    		totalPath.pollFirst();
    		return totalPath;
    	}

    	private short ourRoot(short num){
    	    short res = 0;
    	    short bit = 1 << 14; // The second-to-top bit is set: 1 << 30 for 32 bits
    	 
    	    // "bit" starts at the highest power of four <= the argument.
    	    while (bit > num)
    	        bit >>= 2;
    	        
    	    while (bit != 0) {
    	        if (num >= res + bit) {
    	            num -= res + bit;
    	            res += bit << 1;
    	        }
    	        
    	        res >>= 1;
    	        bit >>= 2;
    	    }
    	    return res;
    	}
    	
    	private double getHeuristicValue(Pair curr, Pair target){
    		//return Math.abs(Math.abs(curr.x - target.x) - Math.abs(curr.y - target.y))/2 + Math.min(Math.abs(curr.x - target.x), Math.abs(curr.y - target.y));
    		return Math.sqrt((Math.pow(target.x - curr.x, 2) + Math.pow(target.y - curr.y, 2)));
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
    		r.log("Entering A*");
    		int nodesChecked = 0;
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
    		
    		Pair[][] cameFrom = new Pair[r.getPassableMap()[0].length][r.getPassableMap().length];
    		int[][] gScore = new int[r.getPassableMap()[0].length][r.getPassableMap().length];
    		double[][] fScore = new double[r.getPassableMap()[0].length][r.getPassableMap().length];

    		for(int i = 0; i < passableMap.length; i++){
    			for(int j = 0; j < passableMap[i].length; j++){
    				if(passableMap[i][j] != null){
    					gScore[i][j] = Integer.MAX_VALUE;
    					fScore[i][j] = Double.MAX_VALUE;
    				}
    			}
    		}
    		gScore[r.me.x][r.me.y] = 0;
    		fScore[r.me.x][r.me.y] = getHeuristicValue(new Pair(r.me.x, r.me.y), target);		
     				
    		boolean[][] closedSet = new boolean[r.getPassableMap()[0].length][r.getPassableMap().length];
    		ArrayList<Pair> openSet = new ArrayList<Pair>(r.getPassableMap().length * r.getPassableMap()[0].length);
    		openSet.add(passableMap[r.me.x][r.me.y]);
    		boolean[][] openMap = new boolean[r.getPassableMap()[0].length][r.getPassableMap().length];
    		openMap[r.me.x][r.me.y] = true;
    		
    		Pair current;
    		while(!openSet.isEmpty()){
    			current = null;
    			nodesChecked++;
    			//if(nodesChecked % 10 == 0)
    				//r.log("Nodes checked: " + nodesChecked);
    			for(Pair p : openSet){
    				if(current == null)
    					current = p;
    				if(fScore[p.x][p.y] < fScore[current.x][current.y])
    					current = p;
    			}

    			if(current.equals(target)){
        			r.log("Nodes checked: " + nodesChecked);
    				path = reconstructPath(cameFrom, current);
    				return path;
    			}
    			
    			openSet.remove(current);
    			openMap[current.x][current.y] = false;
    			closedSet[current.x][current.y] = true;
    			
    			for(Pair neighbor : getPassableNeighbors(current, passableMap)){
    				if(closedSet[neighbor.x][neighbor.y])
    					continue;
    				
    				int tentativeGScore = gScore[current.x][current.y] + 1;
    				
    				boolean inOpen = openMap[neighbor.x][neighbor.y];
    				if(inOpen && tentativeGScore >= gScore[neighbor.x][neighbor.y])
    					continue;
    				
    				cameFrom[neighbor.x][neighbor.y] = current;
    				gScore[neighbor.x][neighbor.y] = tentativeGScore;
    				fScore[neighbor.x][neighbor.y] = tentativeGScore + getHeuristicValue(neighbor, target);
    				
    				if(!inOpen){
    					openSet.add(neighbor);
    					openMap[neighbor.x][neighbor.y] = true;
    				}
    			}
    		}
    		
    		return null;
    	}
    	
    	public Action followPath(){
    		if(path.isEmpty() || path == null)
    			return null;
    		Pair nextStep = path.poll();
    		if(((nextStep.x - r.me.x)*(nextStep.x - r.me.x) + (nextStep.y - r.me.y)*(nextStep.y - r.me.y)) *  Math.max(1, r.me.unit - 2) > r.fuel){
    			path.addFirst(nextStep);
    			return null;
    		}
			if(r.getVisibleRobotMap()[target.y][target.x] > 0){
				path.addFirst(nextStep);
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
    		r.log("Entering BFS");
    		int nodesChecked = 0;
    		Pair[][] passableMap = new Pair[r.getPassableMap()[0].length][r.getPassableMap().length];
    		for(int i = 0; i < passableMap.length; i++){
    			for(int j = 0; j < passableMap[i].length; j++){
    				if(r.getPassableMap()[j][i] && r.getVisibleRobotMap()[j][i] < 1){
    					passableMap[i][j] = new Pair(i, j);
    				}
    			}
    		}
    		passableMap[r.me.x][r.me.y] = new Pair(r.me.x, r.me.y);
    		
       		Pair[][] cameFrom = new Pair[r.getPassableMap()[0].length][r.getPassableMap().length];
    		int[][] gScore = new int[r.getPassableMap()[0].length][r.getPassableMap().length];

    		for(int i = 0; i < passableMap.length; i++){
    			for(int j = 0; j < passableMap[i].length; j++){
    				if(passableMap[i][j] != null){
    					gScore[i][j] = Integer.MAX_VALUE;
    				}
    			}
    		}
    		gScore[r.me.x][r.me.y] = 0;
    		
    		boolean[][] closedSet = new boolean[r.getPassableMap()[0].length][r.getPassableMap().length];
    		Queue<Pair> openSet = new LinkedList<Pair>();
    		openSet.add(passableMap[r.me.x][r.me.y]);
    		boolean[][] openMap = new boolean[r.getPassableMap()[0].length][r.getPassableMap().length];
    		openMap[r.me.x][r.me.y] = true;
    		
    		Pair current;
    		while(!openSet.isEmpty()){
    			nodesChecked++;
    			//if(nodesChecked % 25 == 0)
    				//r.log("BFS Nodes checked: " + nodesChecked);
    			current = openSet.poll();
    			openMap[current.x][current.y] = false;
    			if(map[current.y][current.x]){
    				r.log("BFS Nodes checked: " + nodesChecked);
    				return reconstructPath(cameFrom, current);
    			}
    			
    			closedSet[current.x][current.y] = true;
    			
    			for(Pair neighbor : getPassableNeighbors(current, passableMap)){
    				if(closedSet[neighbor.x][neighbor.y])
    					continue;
    				
    				int tentativeGScore = gScore[current.x][current.y] + 1;
    				
    				boolean inOpen = openMap[neighbor.x][neighbor.y];
    				if(inOpen && tentativeGScore >= gScore[neighbor.x][neighbor.y])
    					continue;
    				
    				cameFrom[neighbor.x][neighbor.y] = current;
    				gScore[neighbor.x][neighbor.y] = tentativeGScore;
    				
    				if(!inOpen){
    					openSet.add(neighbor);
    					openMap[neighbor.x][neighbor.y] = true;
    				}
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
    	boolean panicMode = false;
    	int phase = 1;
    	boolean verticallySymmetric;
    	boolean topOrLeft;

    	private static final int TRAVELING = 0;
    	private static final int FARMING = 1;
    	private static final int DEPOSITING = 2;
    	private Integer state;
    	
    	private Pair resourcePatch; //durable record of where this pilgrim is farming, only set when it has been reached and claimed
    	
    	private boolean[][] availableResources;
    	private int[][] previousRobotMap;
    	
    	public Action robotTurn(){
    		if(turnCount == 0){
    			previousRobotMap = new int[r.getFuelMap().length][r.getFuelMap()[0].length];
    			for(int i = 0; i < r.getPassableMap().length/2; i++){
    				for(int j = 0; j < r.getPassableMap()[0].length/2; j++){
    					if(r.getPassableMap()[i][j] != r.getPassableMap()[r.getPassableMap().length -1 - i][j]){
    						verticallySymmetric = true;
    					}
    				}
    			}
    			if(verticallySymmetric){
    				topOrLeft = r.getKarboniteMap()[0].length - r.me.x * 2 > 0;
    			}
    			else{
    				topOrLeft = r.getKarboniteMap().length - r.me.y * 2 > 0;
    			}

	    		availableResources = new boolean[r.getFuelMap().length][r.getFuelMap()[0].length];
    			if(verticallySymmetric){
    				if(topOrLeft){
    					for(int i = 0; i < availableResources[0].length; i++){
    						for(int j = 0; j < availableResources.length; j++){
    							if(i < availableResources[0].length/2 - 2){
    								availableResources[j][i] = r.getFuelMap()[j][i] || r.getKarboniteMap()[j][i];
    							}
    							else{
    								availableResources[j][i] = false;
    							}
    						}
    					}
    				}
    				else{
    					for(int i = 0; i < availableResources[0].length; i++){
    						for(int j = 0; j < availableResources.length; j++){
    							if(i >= availableResources[0].length/2 + 2){
    								availableResources[j][i] = r.getFuelMap()[j][i] || r.getKarboniteMap()[j][i];
    							}
    							else{
    								availableResources[j][i] = false;
    							}
    						}
    					}
    				}
    			}
    			else{
    				if(topOrLeft){
    					for(int i = 0; i < availableResources.length; i++){
    						for(int j = 0; j < availableResources[0].length; j++){
    							if(i < availableResources.length/2 - 2){
    								availableResources[i][j] = r.getFuelMap()[i][j] || r.getKarboniteMap()[i][j];
    							}
    							else{
    								availableResources[i][j] = false;
    							}
    						}
    					}
    				}
    				else{
    					for(int i = 0; i < availableResources.length; i++){
    						for(int j = 0; j < availableResources[0].length; j++){
    							if(i >= availableResources.length/2 + 2){
    								availableResources[i][j] = r.getFuelMap()[i][j] || r.getKarboniteMap()[i][j];
    							}
    							else{
    								availableResources[i][j] = false;
    							}
    						}
    					}
    				}
    			}
	    		/*if(phase == 1){
		    		boolean alone = true;
		    		for(Robot bot : r.getVisibleRobots()){
		    			if(bot.unit == 2 && bot.team == r.me.team && bot.id != r.me.id){
		    				alone = false;
		    				break;
		    			}
		    		}
		    		if(alone){
		    			path = search(r.getKarboniteMap());
		    		}
		    		else{
		    			path = search(availableResources);
		    		}
	    		}
	    		else if(phase == 2){*/
	    			path = search(availableResources);
	    		//}
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
    		
    		Robot closestEnemy = null;
    		for(Robot bot : r.getVisibleRobots()){
    			if(bot.team != r.me.team && (closestEnemy == null || getDistance(bot) < getDistance(closestEnemy)) && bot.unit > 2)
    				closestEnemy = bot;
    		}
    		Action move = null;
    		if(closestEnemy != null && getDistance(closestEnemy) <= 100){
    			if(closestEnemy.x > r.me.x){
    				if(closestEnemy.y > r.me.y){
    					move = tryMove(new Pair(-1, -1), new Pair(-2, 0), new Pair(0, -2), new Pair(-1, 0), new Pair(0, -1));
    				}
    				else if(r.me.y > closestEnemy.y){
    					move = tryMove(new Pair(-1, 1), new Pair(-2, 0), new Pair(0, 2), new Pair(-1, 0), new Pair(0, 1));
    				}
    				else{
    					move = tryMove(new Pair(-2, 0), new Pair(-1, 1), new Pair(-1, -1), new Pair(-1, 0));
    				}
    			}
    			else if(r.me.x > closestEnemy.x){
    				if(closestEnemy.y > r.me.y){
    					move = tryMove(new Pair(1, -1), new Pair(2, 0), new Pair(0, -2), new Pair(1, 0), new Pair(0, -1));
    				}
    				else if(r.me.y > closestEnemy.y){
    					move = tryMove(new Pair(1, 1), new Pair(2, 0), new Pair(0, 2), new Pair(1, 0), new Pair(0, 1));
    				}
    				else{
    					move = tryMove(new Pair(2, 0), new Pair(1, 1), new Pair(1, -1), new Pair(-1, 0));
    				}
    			}
    			else{
    				if(closestEnemy.y > r.me.y){
    					move = tryMove(new Pair(0, -2), new Pair(1, -1), new Pair(-1, -1), new Pair(0, -1));
    				}
    				else if(r.me.y > closestEnemy.y){
    					move = tryMove(new Pair(0, 2), new Pair(1, 1), new Pair(-1, 1), new Pair(0, 1));
    				}
    			}
    		}
    		if(move != null){
    			panicMode = true;
    			return move;
    		}
    		if(panicMode){
    			panicMode = false;
    			if(state == FARMING){
    				state = TRAVELING;
    				target = resourcePatch;
    			}
    			path = getPathTo(target);
    		}
    		
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
    			boolean hasNearbyBuilding = false;
    			boolean[][] nearbyBuildings = new boolean[availableResources.length][availableResources[0].length];
    			Robot[] visibleRobots = r.getVisibleRobots();
    			for(int i = 0; i < visibleRobots.length; i++){
					if(visibleRobots[i].unit <= 1 && visibleRobots[i].team == r.me.team){
						for(int j = -1; j <= 1; j++){
							for(int k = -1; k <= 1; k++){
								if(inMap(visibleRobots[i].x + j, visibleRobots[i].y + k) && r.getPassableMap()[visibleRobots[i].y + k][visibleRobots[i].x + j] && (r.getVisibleRobotMap()[visibleRobots[i].y + k][visibleRobots[i].x + j] <= 0 || r.getVisibleRobotMap()[visibleRobots[i].y + k][visibleRobots[i].x + j] == r.me.id) && getDistance(visibleRobots[i].x + j, visibleRobots[i].y + k) < 25){
									nearbyBuildings[visibleRobots[i].y + k][visibleRobots[i].x + j] = true;
									hasNearbyBuilding = true;
								}
							}
						}
					}
    			}
    			if(!hasNearbyBuilding){
	    			for(int i = -1; i <= 1; i++){
	    				for(int j = -1; j <= 1; j++){
	    					if(inMap(r.me.x + j, r.me.y + i) && isAffordable(1) && r.getVisibleRobotMap()[r.me.y + i][r.me.x + j] <= 0 && r.getPassableMap()[r.me.y + i][r.me.x + j] == true && r.getKarboniteMap()[r.me.y + i][r.me.x + j] == false && r.getFuelMap()[r.me.y + i][r.me.x + j] == false){
	    						return r.buildUnit(1, j, i);
	    					}
	    				}
	    			}
	    			for(int i = -1; i <= 1; i++){
	    				for(int j = -1; j <= 1; j++){
	    					if(inMap(r.me.x + j, r.me.y + i) && isAffordable(1) && r.getVisibleRobotMap()[r.me.y + i][r.me.x + j] <= 0 && r.getPassableMap()[r.me.y + i][r.me.x + j] == true){
	    						return r.buildUnit(1, j, i);
	    					}
	    				}
	    			}
	    			return null;
    			}
    			
    			resourcePatch = target;
    			state = FARMING;
    			
				path = search(nearbyBuildings);
				if(!path.isEmpty()){
        			target = path.pollLast();
        			path.add(target);
				}
				
    			return farming();
    		}
    		
    		for(int i = -10; i <= 10; i++){
    			for(int j = -10; j <= 10; j++){
    				if(inMap(r.me.x + i, r.me.y + j) && getDistance(r.me.x + i, r.me.y + j) <= 100){
    					if(r.getVisibleRobotMap()[r.me.y + j][r.me.x + i] > 0 && (r.getRobot(r.getVisibleRobotMap()[r.me.y + j][r.me.x + i]).unit > 2 || r.getVisibleRobotMap()[r.me.y + j][r.me.x + i] == r.me.id)){
    						continue;
    					}
    					if(r.getVisibleRobotMap()[r.me.y + j][r.me.x + i] > 0 && r.getVisibleRobotMap()[r.me.y + j][r.me.x + i] == previousRobotMap[r.me.y + j][r.me.x + i]){
        					availableResources[r.me.y + j][r.me.x + i] = false;
    					}
    				}
    			}
    		}
    		
    		for(int i = 0; i < previousRobotMap.length; i++){
    			previousRobotMap[i] = r.getVisibleRobotMap()[i].clone();
    		}
    		
    		return followPath();
    	}
    	
    	public Action farming(){
    		//if full change target to nearest castle/church and state to depositing
    		if(r.me.fuel > 95 || r.me.karbonite >= 20){
    			state = DEPOSITING;
				return depositing();
			}
    		if(!r.getFuelMap()[r.me.y][r.me.x] && !r.getKarboniteMap()[r.me.y][r.me.x]){
    			path = search(availableResources);
    			if(path == null){
    				return null;
    			}
    			target = path.pollLast();
    			path.add(target);
    		}
    		//farm
    		return r.mine();
    	}
    	
    	public Action depositing(){
    		//if adjacent to target then deposit and change target to resourcePatch and change state to traveling
    		if(path.isEmpty()){
    			for(int i = -1; i <= 1; i++){
    				for(int j = -1; j <= 1; j++){
    					if(inMap(r.me.y + i, r.me.x + j) && r.getVisibleRobotMap()[r.me.y + i][r.me.x + j] > 0 && r.getRobot(r.getVisibleRobotMap()[r.me.y + i][r.me.x + j]).unit <= 1 && r.getRobot(r.getVisibleRobotMap()[r.me.y + i][r.me.x + j]).team == r.me.team){
    						path = getPathTo(resourcePatch);
    						target = resourcePatch;
    						state = TRAVELING;
    						return r.give(j, i, r.me.karbonite, r.me.fuel);
    					}
    				}
    			}
    		}
    		//followPath to target
    		return followPath();
    	}
    	
    	public Action targetBlocked(){
    		if(r.getRobot(r.getVisibleRobotMap()[target.y][target.x]).unit > 2)
    			return null;
    		//r.log("Pilgrim's target is blocked");
    		if(state == TRAVELING){
    			if(availableResources[target.y][target.x]){
    				return null;
    			}
    			path = search(availableResources);
    			if(path == null){
    				//r.log("Seen everything");
    				return null;
    			}
    			if(!path.isEmpty()){
	    			target = path.pollLast();
	    			path.add(target);
    			}
    			else{
    				target = new Pair(r.me.x, r.me.y);
    			}
    			return followPath();
    		}
    		if(state == DEPOSITING){
    			boolean hasNearbyBuilding = false;
    			boolean[][] nearbyBuildings = new boolean[availableResources.length][availableResources[0].length];
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
    			if(!hasNearbyBuilding){
	    			for(int i = -1; i <= 1; i++){
	    				for(int j = -1; j <= 1; j++){
	    					if(inMap(r.me.x + j, r.me.y + i) && isAffordable(1) && r.getVisibleRobotMap()[r.me.y + i][r.me.x + j] <= 0 && r.getPassableMap()[r.me.y + i][r.me.x + j] == true && r.getKarboniteMap()[r.me.y + i][r.me.x + j] == false && r.getFuelMap()[r.me.y + i][r.me.x + j] == false){
	    						return r.buildUnit(1, j, i);
	    					}
	    				}
	    			}
	    			for(int i = -1; i <= 1; i++){
	    				for(int j = -1; j <= 1; j++){
	    					if(inMap(r.me.x + j, r.me.y + i) && isAffordable(1) && r.getVisibleRobotMap()[r.me.y + i][r.me.x + j] <= 0 && r.getPassableMap()[r.me.y + i][r.me.x + j] == true){
	    						return r.buildUnit(1, j, i);
	    					}
	    				}
	    			}
	    			return null;
    			}
    			
				path = search(nearbyBuildings);
				if(!path.isEmpty()){
        			target = path.pollLast();
        			path.add(target);
				}
				return depositing();
    		}
    		return null;
    	}
    }
    
    private class Preacher extends CombatRobot{
    	public Preacher(MyRobot r){
    		super(r);
    	}
    	
    	int turnCount = 0;
    	boolean verticallySymmetric;
    	boolean topOrLeft;
    	boolean castle2;
    	boolean castle3;
    	ArrayList<Pair> enemyCastles = new ArrayList<Pair>(3);
    	boolean[][] enemyCastlesMap;
    	    	
    	public Action robotTurn(){
    		if(turnCount == 0){
    			for(int i = 0; i < r.getPassableMap().length/2; i++){
    				for(int j = 0; j < r.getPassableMap()[0].length/2; j++){
    					if(r.getPassableMap()[i][j] != r.getPassableMap()[r.getPassableMap().length -1 - i][j]){
    						verticallySymmetric = true;
    					}
    				}
    			}
    			if(verticallySymmetric){
    				topOrLeft = r.getKarboniteMap()[0].length - r.me.x * 2 > 0;
    			}
    			else{
    				topOrLeft = r.getKarboniteMap().length - r.me.y * 2 > 0;
    			}
    			
    			enemyCastlesMap = new boolean[r.getFuelMap().length][r.getFuelMap()[0].length];
    			for(Robot bot : r.getVisibleRobots()){
    				if(bot.team == r.me.team && r.isRadioing(bot) && bot.unit == 0){
    					castle2 = (bot.signal >> 14) > 1;
    					castle3 = (bot.signal >> 14) > 2;
    					int castle2long = ((int) ((bot.signal >> 10) % 16)) * 4;
	        			int castle2short = ((int) ((bot.signal >> 7) % 8)) * 4;
    					int castle3long = ((int) ((bot.signal >> 3) % 16)) * 4;
	        			int castle3short = ((int) (bot.signal % 8)) * 4;
    					if(verticallySymmetric){
    						if(topOrLeft){
    							if(castle2){
    								for(int i = 0; i < 4; i++){
    									for(int j = 0; j < 4; j++){
    										enemyCastlesMap[castle2long + i][enemyCastlesMap[0].length - 1 - castle2short - j] = true;
    									}
    								}
    							}	
    							if(castle3){
    								for(int i = 0; i < 4; i++){
    									for(int j = 0; j < 4; j++){
    										enemyCastlesMap[castle3long + i][enemyCastlesMap[0].length - 1 - castle3short - j] = true;
    									}
    								}
    							}
    						}
    						else{
    							if(castle2){
    								for(int i = 0; i < 4; i++){
    									for(int j = 0; j < 4; j++){
    										enemyCastlesMap[castle2long + i][castle2short + j] = true;
    									}
    								}
    							}
    							if(castle3){
    								for(int i = 0; i < 4; i++){
    									for(int j = 0; j < 4; j++){
    										enemyCastlesMap[castle3long + i][castle3short + j] = true;
    									}
    								}
    							}
    						}
    					}
    					else{
    						if(topOrLeft){
    							if(castle2){
    								for(int i = 0; i < 4; i++){
    									for(int j = 0; j < 4; j++){
    										enemyCastlesMap[enemyCastlesMap.length - 1 - castle2short - j][castle2long + i] = true;
    									}
    								}
    							}	
    							if(castle3){
    								for(int i = 0; i < 4; i++){
    									for(int j = 0; j < 4; j++){
    										enemyCastlesMap[enemyCastlesMap.length - 1 - castle3short - j][castle3long + i] = true;
    									}
    								}
    							}
    						}
    						else{
    							if(castle2){
    								for(int i = 0; i < 4; i++){
    									for(int j = 0; j < 4; j++){
    										enemyCastlesMap[castle2short + j][castle2long + i] = true;
    									}
    								}
    							}	
    							if(castle3){
    								for(int i = 0; i < 4; i++){
    									for(int j = 0; j < 4; j++){
    										enemyCastlesMap[castle3short + j][castle3long + i] = true;
    									}
    								}
    							}
    						}
    					}
    				}
    			}
    			if(verticallySymmetric){
    				enemyCastles.add(new Pair(enemyCastlesMap[0].length - 1 - r.me.x, r.me.y));
    			}
    			else{
    				enemyCastles.add(new Pair(r.me.x, enemyCastlesMap.length - 1 - r.me.y));
    			}
    			/*if(verticallySymmetric){
    				for(int i = -1; i < 1; i++){
    					for(int j = -1; j < 1; j++){
    						if(inMap(enemyCastlesMap[0].length - 1 - r.me.x, r.me.y))
    							enemyCastlesMap[r.me.y][enemyCastlesMap[0].length - 1 - r.me.x] = true;
    					}
    				}
    			}
    			else{
    				for(int i = -1; i < 1; i++){
    					for(int j = -1; j < 1; j++){
    						if(inMap(r.me.x, enemyCastlesMap.length - 1 - r.me.y))
    							enemyCastlesMap[enemyCastlesMap.length - 1 - r.me.y][r.me.x] = true;
    					}
    				}
    			}*/
				path = getPathTo(enemyCastles.get(0));
				//r.log("Initial path: " + path);
    			target = path.pollLast();
    			path.add(target);
    		}
    		
			for(int i = -4; i <= 4; i++){
				for(int j = -4; j <= 4; j++){
					if(inMap(r.me.x + i, r.me.y + j) && i*i + j*j <= 16){
						enemyCastlesMap[r.me.y + j][r.me.x + i] = r.getVisibleRobotMap()[r.me.y + j][r.me.x + i] != 0 && r.getRobot(r.getVisibleRobotMap()[r.me.y + j][r.me.x + i]).unit == 0 && r.getRobot(r.getVisibleRobotMap()[r.me.y + j][r.me.x + i]).team != r.me.team;
					}
				}
			}
			for(Robot bot : r.getVisibleRobots()){
				if(bot.unit == 0 && bot.team != r.me.team && enemyCastlesMap[bot.y][bot.x] == false){
					enemyCastlesMap[bot.y][bot.x] = true;
				}
			}
			
    		if(path.isEmpty()){
				path = search(enemyCastlesMap);
				//r.log("Path is empty, new path: " + path);
    			target = path.pollLast();
    			path.add(target);
    		}
    		
    		turnCount++;
    		
    		Robot closestEnemy = null;
    		for(Robot bot : r.getVisibleRobots()){
    			if(bot.team != r.me.team && (closestEnemy == null || getDistance(bot) < getDistance(closestEnemy)))
    				closestEnemy = bot;
    		}
    		if(closestEnemy != null)
    			return r.attack(closestEnemy.x - r.me.x, closestEnemy.y - r.me.y);
    		return followPath();
    	}
    	
    	public Action targetBlocked(){
			path = search(enemyCastlesMap);
			target = path.pollLast();
			path.add(target);
    		return followPath();
    	}
    }

    private class Prophet extends CombatRobot{
    	public Prophet(MyRobot r){
    		super(r);
    	}
    	
    	int turnCount = 0;
    	boolean verticallySymmetric;
    	boolean topOrLeft;
    	boolean castle2;
    	boolean castle3;
    	ArrayList<Pair> enemyCastles = new ArrayList<Pair>(3);
    	int stepsTaken = 0;
		boolean closestCastle = true;
		boolean[][] frontLineMap;
		boolean[][] backLineMap;
		Robot myCastle;
		int phase = 2;
		int castle1short = 0;
		int castle1long = 0;
		int castle2short = 0;
		int castle2long = 0;
		int castle3short = 0;
		int castle3long = 0;
    	boolean panicMode = false;
    	int lineFront;

    	
    	
    	public Action robotTurn(){
    		if(turnCount == 0){
    			backLineMap = new boolean[r.getFuelMap().length][r.getFuelMap()[0].length];
    			for(int i = 0; i < r.getPassableMap().length/2; i++){
    				for(int j = 0; j < r.getPassableMap()[0].length/2; j++){
    					if(r.getPassableMap()[i][j] != r.getPassableMap()[r.getPassableMap().length -1 - i][j]){
    						verticallySymmetric = true;
    					}
    				}
    			}
    			
    			if(verticallySymmetric){
    				topOrLeft = r.getKarboniteMap()[0].length - r.me.x * 2 > 0;
    				lineFront = r.getKarboniteMap()[0].length/2 - 1;
    			}
    			else{
    				topOrLeft = r.getKarboniteMap().length - r.me.y * 2 > 0;
    				lineFront = r.getKarboniteMap().length/2 - 1;
    			}
    			
    			for(Robot bot : r.getVisibleRobots()){
    				if(bot.team == r.me.team && bot.unit == 0){
    					myCastle = bot;
    				}

    				if(bot.team == r.me.team && r.isRadioing(bot) && bot.unit == 0){
    					castle2 = (bot.signal >> 14) > 1;
    					castle3 = (bot.signal >> 14) > 2;
	        			castle2short = ((int) ((bot.signal >> 3) % 8));
	        			castle3short = ((int) (bot.signal % 8));
	        			phase = ((bot.signal >> 6) % 4) + 1;
    				}
    			}
    			if(verticallySymmetric){
    				enemyCastles.add(new Pair(backLineMap[0].length - 1 - r.me.x, r.me.y));
    			}
    			else{
    				enemyCastles.add(new Pair(r.me.x, backLineMap.length - 1 - r.me.y));
    			}

	    		for(Robot bot : r.getVisibleRobots()){
	    			if(bot.unit == 0 && bot.team == r.me.team){
	        			if(verticallySymmetric){
	        				if(topOrLeft){
	        					castle1short = bot.x/4;
	        				}
	        				else{
	        					castle1short = (backLineMap[0].length - 1 - bot.x)/4;
	        				}
        					castle1long = bot.y/4;
	        			}
	        			else{
	        				if(topOrLeft){
	        					castle1short = bot.y/4;
	        				}
	        				else{
	        					castle1short = (backLineMap.length - 1 - bot.y)/4;
	        				}
        					castle1long = bot.x/4;
	        			}
	        			if(castle2short > castle1short || castle3short > castle1short){
	        				closestCastle = false;
	        			}
	        		}
	    		}
	    			    		
				if(verticallySymmetric){
					for(int i = 0; i < backLineMap.length; i++){
    					if(topOrLeft){
    						backLineMap[i][lineFront - 1] = true;
    					}
    					else{
    						backLineMap[i][backLineMap[0].length - lineFront] = true;
    					}
					}
				}
				else{
					for(int i = 0; i < backLineMap[0].length; i++){
    					if(topOrLeft){
    						backLineMap[lineFront - 1][i] = true;
    					}
    					else{
    						backLineMap[backLineMap.length - lineFront][i] = true;
    					}
					}
				}
				path = search(backLineMap);
				target = path.pollLast();
				path.add(target);
	    	}
    		
    		turnCount++;
    		    		
    		Robot closestEnemy = null;
    		for(Robot bot : r.getVisibleRobots()){
    			if(phase == 2 && bot.signal == 1452){
    				r.log("phase change acknowledged");
    				phase = 3;
    				if(verticallySymmetric){
    					if(topOrLeft){
    						target.x = backLineMap[0].length - 1;
    						target.y = r.me.y;
    						while(!passable(target.y, target.x)){
    							target.x--;
    						}
    						path = getPathTo(target);
    					}
    					else{
    						target.x = 0;
    						target.y = r.me.y;
    						while(!passable(target.y, target.x)){
    							target.x++;
    						}
    						path = getPathTo(target);
    					}
    				}
    				else{
    					if(topOrLeft){
    						target.y = backLineMap.length - 1;
    						target.x = r.me.x;
    						while(!passable(target.y, target.x)){
    							target.y--;
    						}
    						path = getPathTo(target);
    					}
    					else{
    						target.y = 0;
    						target.x = r.me.x;
    						while(!passable(target.y, target.x)){
    							target.y++;
    						}
    						path = getPathTo(target);
    					}
    				}
    			}
    			if(r.isVisible(bot) && bot.team != r.me.team && (closestEnemy == null || getDistance(bot) < getDistance(closestEnemy)))
    				closestEnemy = bot;
    		}
			
    		Action move = null;
    		if(closestEnemy != null && getDistance(closestEnemy) <= 16){
    			if(closestEnemy.x > r.me.x){
    				if(closestEnemy.y > r.me.y){
    					move = tryMove(new Pair(-1, -1), new Pair(-2, 0), new Pair(0, -2), new Pair(-1, 0), new Pair(0, -1));
    				}
    				else if(r.me.y > closestEnemy.y){
    					move = tryMove(new Pair(-1, 1), new Pair(-2, 0), new Pair(0, 2), new Pair(-1, 0), new Pair(0, 1));
    				}
    				else{
    					move = tryMove(new Pair(-2, 0), new Pair(-1, 1), new Pair(-1, -1), new Pair(-1, 0));
    				}
    			}
    			else if(r.me.x > closestEnemy.x){
    				if(closestEnemy.y > r.me.y){
    					move = tryMove(new Pair(1, -1), new Pair(2, 0), new Pair(0, -2), new Pair(1, 0), new Pair(0, -1));
    				}
    				else if(r.me.y > closestEnemy.y){
    					move = tryMove(new Pair(1, 1), new Pair(2, 0), new Pair(0, 2), new Pair(1, 0), new Pair(0, 1));
    				}
    				else{
    					move = tryMove(new Pair(2, 0), new Pair(1, 1), new Pair(1, -1), new Pair(-1, 0));
    				}
    			}
    			else{
    				if(closestEnemy.y > r.me.y){
    					move = tryMove(new Pair(0, -2), new Pair(1, -1), new Pair(-1, -1), new Pair(0, -1));
    				}
    				else if(r.me.y > closestEnemy.y){
    					move = tryMove(new Pair(0, 2), new Pair(1, 1), new Pair(-1, 1), new Pair(0, 1));
    				}
    			}
    		}
    		if(move != null){
    			panicMode = true;
    			return move;
    		}
    		if(closestEnemy != null)
    			return r.attack(closestEnemy.x - r.me.x, closestEnemy.y - r.me.y);
    		
			if(verticallySymmetric){
				for(int i = 0; i < backLineMap.length; i++){
					if(topOrLeft){
						if(r.getVisibleRobotMap()[i][lineFront - 1] == 0){
							backLineMap[i][lineFront - 1] = true;
						}
						else if(r.getVisibleRobotMap()[i][lineFront - 1] > 0){
							backLineMap[i][lineFront - 1] = false;
						}
						if(r.me.x == lineFront - 1 && r.me.y == i)
							backLineMap[r.me.y][r.me.x] = true;
					}
					else{
						if(r.getVisibleRobotMap()[i][backLineMap[0].length - lineFront] == 0){
							backLineMap[i][backLineMap[0].length - lineFront] = true;
						}
						else if(r.getVisibleRobotMap()[i][backLineMap[0].length - lineFront] > 0){
							backLineMap[i][backLineMap[0].length - lineFront] = false;
						}
						if(r.me.x == backLineMap[0].length - lineFront && r.me.y == i)
							backLineMap[r.me.y][r.me.x] = true;
					}
				}
			}
			else{
				for(int i = 0; i < backLineMap[0].length; i++){
					if(topOrLeft){
						if(r.getVisibleRobotMap()[lineFront - 1][i] == 0){
							backLineMap[lineFront - 1][i] = true;
						}
						else if(r.getVisibleRobotMap()[lineFront - 1][i] > 0){
							backLineMap[lineFront - 1][i] = false;
						}
						if(r.me.x == i && r.me.y == lineFront - 1)
							backLineMap[r.me.y][r.me.x] = true;
					}
					else{
						if(r.getVisibleRobotMap()[backLineMap.length - lineFront][i] == 0){
							backLineMap[backLineMap.length - lineFront][i] = true;
						}
						else if(r.getVisibleRobotMap()[backLineMap.length - lineFront][i] > 0){
							backLineMap[backLineMap.length - lineFront][i] = false;
						}
						if(r.me.x == i && r.me.y == backLineMap.length - lineFront)
							backLineMap[r.me.y][r.me.x] = true;
					}
				}
			}
			if(panicMode){
				panicMode = false;
				if(phase == 2){
					path = search(backLineMap);
					if(!path.isEmpty()){
						target = path.pollLast();
						path.add(target);
					}
				}
				else{
					path = getPathTo(target);
				}
			}
			if(path.isEmpty() && backLineMap[r.me.y][r.me.x] && phase == 2){
    			frontLineMap = new boolean[r.getFuelMap().length][r.getFuelMap()[0].length];
    			boolean frontLineOpen = false;
    			boolean adjacentBackImpassable = false;
				if(verticallySymmetric){
					for(int i = 0; i < backLineMap.length; i++){
    					if(topOrLeft){
    						if(r.getVisibleRobotMap()[i][lineFront] == 0 && r.getPassableMap()[i][lineFront] && !(passable(i, lineFront - 2) && (!passable(i - 1, lineFront - 1) || !passable(i - 1, lineFront - 2) || !passable(i + 1, lineFront - 1) || !passable(i + 1, lineFront - 2)))){
    							frontLineOpen = true;
        						frontLineMap[i][lineFront] = true;
    						}
    					}
    					else{
    						if(r.getVisibleRobotMap()[i][backLineMap[0].length - lineFront - 1] == 0 && r.getPassableMap()[i][backLineMap[0].length - lineFront - 1] && !(passable(i, backLineMap[0].length - lineFront - 3) && (!passable(i - 1, backLineMap[0].length - lineFront - 2) || !passable(i - 1, backLineMap[0].length - lineFront - 3) || !passable(i + 1, backLineMap[0].length - lineFront - 2) || !passable(i + 1, backLineMap[0].length - lineFront - 3)))){
    							frontLineOpen = true;
        						frontLineMap[i][backLineMap[0].length - lineFront - 1] = true;
    						}
    					}
					}
				}
				else{
					for(int i = 0; i < backLineMap[0].length; i++){
						if(topOrLeft){
							if(r.getVisibleRobotMap()[lineFront][i] == 0 && r.getPassableMap()[lineFront][i] && !(passable(lineFront - 2, i) && (!passable(lineFront - 1, i - 1) || !passable(lineFront - 2, i - 1) || !passable(lineFront - 1, i + 1) || !passable(lineFront - 2, i + 1)))){
    							frontLineOpen = true;
        						frontLineMap[lineFront][i] = true;
							}
						}
    					else{
    						if(r.getVisibleRobotMap()[backLineMap[0].length - lineFront - 1][i] == 0 && r.getPassableMap()[backLineMap[0].length - lineFront - 1][i] && !(passable(backLineMap[0].length - lineFront - 3, i) && (!passable(backLineMap[0].length - lineFront - 2, i - 1) || !passable(backLineMap[0].length - lineFront - 3, i - 1) || !passable(backLineMap[0].length - lineFront - 2, i + 1) || !passable(backLineMap[0].length - lineFront - 3, i + 1)))){
    							frontLineOpen = true;
        						frontLineMap[backLineMap[0].length - lineFront - 1][i] = true;
    						}
    					}
					}
				}
				if(frontLineOpen){
					path = search(frontLineMap);
					target = path.pollLast();
					path.add(target);
				}
				else{
					if(verticallySymmetric){
						for(int i = r.me.y - 8; i <= r.me.y + 8; i++){
							if(passable(i, r.me.x) && r.getVisibleRobotMap()[i][r.me.x] == 0){
								r.castleTalk(99);
								break;
							}
						}
					}
					else{
						for(int i = r.me.x - 8; i <= r.me.x + 8; i++){
							if(passable(r.me.y, i) && r.getVisibleRobotMap()[r.me.y][i] == 0){
								r.castleTalk(99);
								break;
							}
						}
					}
				}
			}
			return followPath();
  
			//attack if possible
    		//else SMOrc
    		//needs castles to solve symmetry and broadcast it in order to find enemy castles
    	}
    	
    	public Action targetBlocked(){
    		if(phase == 2){
				path = search(backLineMap);
				if(!path.isEmpty()){
					target = path.pollLast();
					path.add(target);
				}
    		}
    		else{
    			return null;
    		}
    		return followPath();
    	}
    }
}
