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

import modeling.encountergenerator.CrossingGenerator;
import modeling.encountergenerator.HeadOnGenerator;
import modeling.encountergenerator.SelfGenerator;
import modeling.encountergenerator.TailApproachGenerator;
import modeling.env.Destination;
import modeling.saa.collsionavoidance.AVO;
import modeling.saa.collsionavoidance.CollisionAvoidanceAlgorithm;
import modeling.saa.collsionavoidance.CollisionAvoidanceAlgorithmAdapter;
import modeling.saa.selfseparation.SVOSep;
import modeling.saa.selfseparation.SelfSeparationAlgorithm;
import modeling.saa.selfseparation.SelfSeparationAlgorithmAdapter;
import modeling.saa.sense.Sensor;
import modeling.saa.sense.SimpleSensor;
import modeling.uas.AvoidParas;
import modeling.uas.SenseParas;
import modeling.uas.UAS;
import modeling.uas.UASPerformance;
import modeling.uas.UASVelocity;


import sim.util.*;
import tools.CONFIGURATION;
/**
 *
 * @author Robert Lee
 * This class is used to build/initiate the simulation.
 * Called for by simulationWithUI.class
 */
public class SAAModelBuilder
{
	public  SAAModel state;
	
	public static double fieldXVal = CONFIGURATION.fieldXVal; 
	public static double fieldYVal = CONFIGURATION.fieldYVal;

	
	public SAAModelBuilder(SAAModel simState)
	{
		state = simState;
//		System.out.println("COModelBuilder(COModel simState) is being called!!!!!!!!!! the simstate is：" + state.toString());
	}
	
	public  void generateSimulation()
	{	
		UAS self;
		if(CONFIGURATION.tailApproachSelected==1)
		{
			double uasX= fieldXVal/9;
	    	double uasY= fieldYVal/2;
	    	self = new SelfGenerator(state,uasX, uasY, CONFIGURATION.selfDestDist, CONFIGURATION.selfDestAngle).execute();
	    	
		}
		else
		{
			double uasX= fieldXVal/10;
	    	double uasY= fieldYVal/2;
	    	self = new SelfGenerator(state,uasX, uasY, CONFIGURATION.selfDestDist, CONFIGURATION.selfDestAngle).execute();
	    				
		}
	
	    if(CONFIGURATION.headOnSelected==1)
	    {
	    	double offset = CONFIGURATION.headOnOffset ;
	    	boolean isRightSide = (CONFIGURATION.headOnIsRightSide==1);
	    	double speed= CONFIGURATION.headOnPrefSpeed;
	    	
	    	if(state.runningWithUI)
	    	{
		    	for(int i=0; i<CONFIGURATION.headOnTimes; i++)
		    	{
		    		new HeadOnGenerator(state, self, offset, isRightSide, speed).execute();
		    		offset += 2;
		    		isRightSide = !isRightSide;
		    		speed += 0.2*speed;
		    	}
	    	}
	    	else
	    	{
	    	  	new HeadOnGenerator(state, self, offset, isRightSide, speed).execute();
		    		
	    	}
	    
	    }
	    
	    if(CONFIGURATION.crossingSelected==1)
	    {
    		double encounterAngle = CONFIGURATION.crossingEncounterAngle;
    		boolean isRightSide = (CONFIGURATION.crossingIsRightSide==1);
    		double speed= CONFIGURATION.crossingPrefSpeed;
	   
	       	if(state.runningWithUI)
	    	{
		    	for(int i=0; i<CONFIGURATION.crossingTimes; i++)
		    	{
		    		new CrossingGenerator(state, self, encounterAngle,isRightSide,speed).execute();
		    		isRightSide = !isRightSide;
		    		encounterAngle -= Math.toRadians(20);
		    		speed += 0.2*speed;
		    	}
	    	}
	    	else
	    	{
	    		new CrossingGenerator(state, self, encounterAngle,isRightSide,speed).execute();
	    	}
	    	
	    }
	    
	    if(CONFIGURATION.tailApproachSelected==1)
	    {
	    	double offset = CONFIGURATION.tailApproachOffset;
	    	boolean isRightSide = (CONFIGURATION.tailApproachIsRightSide==1);
	    	double speed= CONFIGURATION.tailApproachPrefSpeed;
	     	if(state.runningWithUI)
	    	{	     		
		    	for(int i=0; i<CONFIGURATION.tailApproachTimes; i++)
		    	{
		    		new TailApproachGenerator(state, self, offset, isRightSide, speed).execute();
		    		offset += 2;
		    		isRightSide = !isRightSide;
		    		speed *= 1.02;
		    	}
	    	}
	    	else
	    	{	    		
		    	new TailApproachGenerator(state, self, offset, isRightSide, speed).execute();
	    	}
	    	
	    }
	
	    for(Object o : state.uasBag)
	    {
	    	UAS uas = (UAS)o;
	    	uas.getCaa().init();	
	    	uas.getSsa().init();
	    }
				
		System.out.println("Simulation stepping begins!");
		System.out.println("====================================================================================================");
		
			
	}

	


	
}
