package org.usfirst.frc.team4028.robot.commands;

import org.usfirst.frc.team4028.robot.subsystems.Infeed;
import org.usfirst.frc.team4028.robot.subsystems.Infeed.INFEED_ARM_TARGET_POSITION;

import edu.wpi.first.wpilibj.command.Command;

/**
 * This command implements support for moving the infeed arms to a preset target position
 */
public class MoveInfeedArmsToPresetPosition extends Command 
{
	private Infeed _infeed = Infeed.getInstance();
	INFEED_ARM_TARGET_POSITION _presetPosition;

    public MoveInfeedArmsToPresetPosition(INFEED_ARM_TARGET_POSITION presetPosition) 
    {
        requires(_infeed);
        setInterruptible(true);
        _presetPosition = presetPosition;
    }

    // Called just before this Command runs the first time
    protected void initialize() 
    {
    	setTimeout(4);  // set 4 second timeout
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() 
    {
    	_infeed.MoveToPresetPosition(_presetPosition);    	
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() 
    {
    	 return _infeed.areArmsInPosition() || isTimedOut();
    }

    // Called once after isFinished returns true
    protected void end() {
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    }
}