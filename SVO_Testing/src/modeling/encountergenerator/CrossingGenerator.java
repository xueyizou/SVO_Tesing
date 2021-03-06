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
package modeling.encountergenerator;

import sim.util.Double2D;
import tools.CONFIGURATION;
import modeling.SAAModel;
import modeling.env.Destination;
import modeling.saa.collsionavoidance.AVO;
import modeling.saa.collsionavoidance.CollisionAvoidanceAlgorithm;
import modeling.saa.collsionavoidance.CollisionAvoidanceAlgorithmAdapter;
import modeling.saa.collsionavoidance.SVO;
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


/**
 * @author Xueyi
 *
 */
public class CrossingGenerator extends EncounterGenerator 
{

	/**
	 * 
	 */
	private SAAModel state;
	private UAS self;
	private double sideFactor;
	private double encounterAngle;
	double intruderSpeed;
	/************
	 * 
	 * @param state
	 * @param uas
	 * @param distance
	 * @param encounterAngle It's ABSOLUTE value is supposed to belong to (0,180). 0 -- tail approach, 180 -- head on
	 * @param intruderSpeed
	 */
	public CrossingGenerator(SAAModel state, UAS uas, double encounterAngle,boolean isRightSide, double intruderSpeed) 
	{
		this.state=state;
		this.self=uas;
		this.sideFactor = isRightSide ? +1:-1; 
		this.encounterAngle = encounterAngle;
		this.intruderSpeed = intruderSpeed;
	}
	
	/*********
	 * self and intruder will encounter at the middle point of each's journey
	 */
	public void execute()
	{
		
		Double2D selfMiddle = self.getLocation().add(self.getDestination().getLocation()).multiply(0.5);
		Double2D selfVector = self.getDestination().getLocation().subtract(self.getLocation());
		Double2D intruderVector = selfVector.rotate(-sideFactor*encounterAngle).multiply(intruderSpeed/self.getSpeed());
		Double2D intruderMiddle = selfMiddle;
				
		Double2D intruderLocation = intruderMiddle.subtract(intruderVector.multiply(0.5));
		Double2D intruderDestinationLoc = intruderMiddle.add(intruderVector.multiply(0.5));
		Destination intruderDestination = new Destination(state.getNewID(), null);
		intruderDestination.setLocation(intruderDestinationLoc);
		UASVelocity intruderVelocity = new UASVelocity(intruderDestination.getLocation().subtract(intruderLocation).normalize().multiply(intruderSpeed));
		UASPerformance intruderPerformance = new UASPerformance(CONFIGURATION.crossingMaxSpeed, CONFIGURATION.crossingMinSpeed, intruderSpeed, CONFIGURATION.crossingMaxClimb, CONFIGURATION.crossingMaxDescent,CONFIGURATION.crossingMaxTurning, CONFIGURATION.crossingMaxAcceleration, CONFIGURATION.crossingMaxDeceleration);
		SenseParas intruderSenseParas = new SenseParas(CONFIGURATION.crossingViewingRange,CONFIGURATION.crossingViewingAngle, CONFIGURATION.crossingSensitivityForCollisions);
		AvoidParas intruderAvoidParas = new AvoidParas(CONFIGURATION.crossingAlpha);
		
		UAS intruder = new UAS(state.getNewID(),CONFIGURATION.crossingSafetyRadius,intruderLocation, intruderDestination, intruderVelocity,intruderPerformance, intruderSenseParas,intruderAvoidParas);
		intruder.setSource(intruderLocation);
		
		Sensor sensor = new SimpleSensor();

		SelfSeparationAlgorithm ssa; 
		switch (CONFIGURATION.crossingSelfSeparationAlgorithmSelection)
		{
			case "SVOAvoidanceAlgorithm":
				ssa= new SVOSep(state, intruder);
				break;
			case "None":
				ssa= new SelfSeparationAlgorithmAdapter(state, intruder);
				break;
			default:
				ssa= new SelfSeparationAlgorithmAdapter(state, intruder);
		
		}
		
		CollisionAvoidanceAlgorithm caa;
		switch(CONFIGURATION.crossingCollisionAvoidanceAlgorithmSelection)
		{
			case "AVOAvoidanceAlgorithm":
				caa= new AVO(state, intruder);
				break;
			case "SVOAvoidanceAlgorithm":
				caa= new SVO(state, intruder);
				break;
			case "None":
				caa= new CollisionAvoidanceAlgorithmAdapter(state, intruder);
				break;
			default:
				caa= new CollisionAvoidanceAlgorithmAdapter(state, intruder);
		}

		intruder.init(sensor, ssa,caa);
		
		
		state.uasBag.add(intruder);
		state.obstacles.add(intruder);			
		state.allEntities.add(intruderDestination);
		state.allEntities.add(intruder);
		intruder.setSchedulable(true);
		state.toSchedule.add(intruder);
	
	}

}
