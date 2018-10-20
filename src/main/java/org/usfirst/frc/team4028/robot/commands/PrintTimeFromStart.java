package org.usfirst.frc.team4028.robot.commands;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;



import org.usfirst.frc.team4028.robot.subsystems.Chassis;

public class PrintTimeFromStart extends Command
{
    private Chassis _chassis = Chassis.getInstance();

    public PrintTimeFromStart(){
    }

    protected void initialize() {    	
    }

    
    protected void execute() {
    }

    protected boolean isFinished() {
        return true;
    }

    protected void end() {
        System.out.println(Timer.getFPGATimestamp() -_chassis._autonStartTime);
    }

    protected void interrupted() {
    }


}