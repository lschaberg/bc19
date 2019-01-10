package bc19;

public class MyRobot extends BCAbstractRobot {
	private AbstractRobot self;
	
	public MyRobot(){
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

    public Action turn() {    	
    	return self.turn;
	}
}
