package org.usfirst.frc.team4028.robot.commands;

import org.usfirst.frc.team4028.robot.subsystems.Chassis;

import edu.wpi.first.wpilibj.command.Command;

public class Auton_WaitUntilRemaingDistance extends Command {
	double _targetDistance;
	
	public Auton_WaitUntilRemaingDistance(double distance) {
		_targetDistance = distance;
	}
	
	@Override
	public void initialize() {}

	@Override
	public void execute() {}


	@Override
	public boolean isFinished() {
		return (Chassis.getInstance().getRemainingPathDistance() < _targetDistance) || Chassis.getInstance().isDoneWithPath();
	}
}