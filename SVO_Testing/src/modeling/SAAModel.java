/* *************************************************************************************
 * Copyright (C) Xueyi Zou - All Rights Reserved
 * Written by Xueyi Zou <xz972@york.ac.uk>, 2015
 * You are free to use/modify/distribute this file for whatever purpose!
 -----------------------------------------------------------------------
 |THIS FILE IS DISTRIBUTED "AS IS", WITHOUT ANY EXPRESS OR IMPLIED
 |WARRANTY. THE USER WILL USE IT AT HIS/HER OWN RISK. THE ORIGINAL
 |AUTHORS AND COPPELIA ROBOTICS GMBH WILL NOT BE LIABLE FOR DATA LOSS,
 |DAMAGES, LOSS OF PROFITS OR ANY OTHER KIND OF LOSS WHILE USING OR
 |MISUSING THIS SOFTWARE.
 ------------------------------------------------------------------------
 **************************************************************************************/
package modeling;

import modeling.env.Entity;
import modeling.env.Obstacle;
import modeling.env.Waypoint;
import modeling.observer.AccidentDetector;
import modeling.observer.OscillationCalculator;
import modeling.observer.OscillationCounter;
import modeling.observer.ProximityMeasurer;
import modeling.uas.UAS;
import sim.util.*;
import sim.field.continuous.*;
import sim.engine.*;
import sim.field.network.Network;

public class SAAModel extends SimState
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public boolean runningWithUI = false; 
	
	public Bag allEntities = new Bag(); // entities to load into the environment, important
	public Bag toSchedule = new Bag(); // entities to schedule, important	
	public Bag uasBag = new Bag();
	public Bag obstacles= new Bag();	
	
    private int newID = 0;	

    public Continuous2D environment;
    public Network voField;	
	
    public AccidentDetector aDetector= new AccidentDetector();
    public ProximityMeasurer pMeasurer= new ProximityMeasurer();
    public OscillationCalculator oCalculator= new OscillationCalculator();
    public OscillationCounter oCounter= new OscillationCounter();
    	
	/**
	 * Constructor used for setting up a simulation from the COModelBuilder object.
	 * 
	 * @param seed for random number generator
	 * @param x the width of the simulation environment
	 * @param y the height of the simulation environment
	 * @param UI pass true if the simulation is being ran with a UI false if it is not.
	 */
	public SAAModel(long seed, double x, double y, boolean UI)
    {
		super(seed);
		environment = new Continuous2D(1.0, x, y);
		voField = new Network(false);
		runningWithUI = UI;
//		System.out.println("COModel(long seed, double x, double y, boolean UI) is being called!!!!!!!!!!!! the simstate is :" + this.toString());

	}    
		
	
	public void start()
	{
		super.start();	
		environment.clear();
		voField.clear();
		
		loadEntities();
		scheduleEntities();			
	}
		

	/**
	 * A method which resets the variables for the COModel and also clears
	 * the schedule and environment of any entities, to be called between simulations.
	 * 
	 * This method resets the newID counter so should NOT be called during a run.
	 */
	public void reset()
	{
		newID = 0;
		obstacles.clear();
		uasBag.clear();
		toSchedule.clear();
		allEntities.clear();
		
		environment.clear(); //clear the environment
		voField.clear();

	}
	
	public void finish()
	{
		super.finish();		
		OscillationCounter oCounter = new OscillationCounter();
		oCounter.step(this);
	}
	
	
	/**
	 * A method which provides a different number each time it is called, this is
	 * used to ensure that different entities are given different IDs
	 * 
	 * @return a unique ID number
	 */
	public int getNewID()
	{
		int t = newID;
		newID++;
		return t;
	}
	
	
	/**
	 * A method which adds all of the entities to the simulations environment.
	 */
	public void loadEntities()
	{
		for(int i = 0; i < allEntities.size(); i++)
		{
			environment.setObjectLocation((Entity) allEntities.get(i), ((Entity) allEntities.get(i)).getLocation());
		
		}
		
	}
	
	
	/**
	 * A method which adds all the entities marked as requiring scheduling to the
	 * schedule for the simulation
	 */
	public void scheduleEntities()
	{
		//loop across all items in toSchedule and add them all to the schedule
		int i ;
		for(i = 0; i < toSchedule.size(); i++)
		{
			schedule.scheduleRepeating(((UAS)toSchedule.get(i)).getCaa(), i, 1.0);
		}	
		int k=i;
		for(int j=0; j < toSchedule.size(); j++,i++)
		{
			schedule.scheduleRepeating((Entity) toSchedule.get(j), k+j, 1.0);
		}	
		schedule.scheduleRepeating(pMeasurer,i++, 1.0);
		schedule.scheduleRepeating(oCalculator,i++, 1.0);	
		schedule.scheduleRepeating(aDetector,i++, 1.0);	
			
	}

	/**
	 * A method which returns a true or false value depending on if an obstacle is
	 * at the coordinate provided
	 * 
	 * @param coord the coordinate to check
	 * @param obstacles the obstacles to be checked
	 * @return 
	 */
	public boolean obstacleAtPoint(Double2D coord, Bag obstacles)
	{
		for (int i = 0; i < obstacles.size(); i++)
		{
			//for all of the obstacles check if the provided point is in it
			if (((Obstacle) (obstacles.get(i))).pointInShape(coord)) 
			{
				return true;
			}
		}
		//at this point no cross over has been detected so false should be returned
		return false;
	}

	
    public void dealWithTermination()
	{
    	int noActiveAgents =0;
    	//System.out.println(COModel.toSchedule.size());
    	for(Object o: this.toSchedule)
    	{
    		if(((UAS)o).isActive)
    		{
    			noActiveAgents++;
    		}
    		
    	}
//    	System.out.println("NO. of active uasBag is: "+ noActiveAgents);
    	
		if(noActiveAgents < 1)
		{
			this.schedule.clear();
			//System.out.println("NO. of active uasBag is: "+ noActiveAgents+". Game Over!");
			//System.out.println(this.schedule.scheduleComplete());
			this.kill();
		}
	 }
    

}
