package org.usfirst.frc.team4028.robot.auton.pathfollowing.control;

import org.usfirst.frc.team4028.robot.Constants;
import org.usfirst.frc.team4028.robot.auton.pathfollowing.motion.RigidTransform;
import org.usfirst.frc.team4028.robot.auton.pathfollowing.motion.Twist;
import org.usfirst.frc.team4028.robot.auton.pathfollowing.motionProfile.MotionProfileConstraints;
import org.usfirst.frc.team4028.robot.auton.pathfollowing.motionProfile.MotionProfileGoal;
import org.usfirst.frc.team4028.robot.auton.pathfollowing.motionProfile.MotionProfileGoal.CompletionBehavior;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.usfirst.frc.team4028.robot.auton.pathfollowing.motionProfile.MotionState;
import org.usfirst.frc.team4028.robot.auton.pathfollowing.motionProfile.ProfileFollower;

public class PathFollower {
    AdaptivePurePursuitController mSteeringController;
    Twist mLastSteeringDelta;
    ProfileFollower mVelocityController;
    boolean overrideFinished = false;
    boolean doneSteering = false;
    double maxAccel, maxDecel;
    double inertiaSteeringGain;
    double remainingPathLength;
    boolean hasAutoStopCounterStarted;
    double autoStopCounterInitTime;
    boolean checkedSteerQ = false;
    boolean checkedVeloFinishedQ = false;
    boolean checkedVeloOnTargetQ = false;

    /** Create a new PathFollower for a given path */
    public PathFollower(Path path, boolean reversed, double maxAccel, double maxDecel, double inertiaSteeringGain) {
        mSteeringController = new AdaptivePurePursuitController(path, reversed);
        mLastSteeringDelta = Twist.identity();
        mVelocityController = new ProfileFollower();
        this.maxAccel = maxAccel;
        this.maxDecel = maxDecel;
        this.inertiaSteeringGain = inertiaSteeringGain;
        mVelocityController.setConstraints(
                new MotionProfileConstraints(Constants.PATH_FOLLOWING_MAX_VEL, maxAccel, maxDecel));
        hasAutoStopCounterStarted = false;
    }

    /**
     * Get new velocity commands to follow the path.
     * 
     * @param t
     *            The current timestamp
     * @param pose
     *            The current robot pose
     * @param displacement
     *            The current robot displacement (total distance driven).
     * @param velocity
     *            The current robot velocity.
     * @return The velocity command to apply
     */
    public synchronized Twist update(double t, RigidTransform pose, double displacement, double velocity) {
        if (!mSteeringController.isFinished()) {
            final AdaptivePurePursuitController.Command steering_command = mSteeringController.update(pose);
            mLastSteeringDelta = steering_command.delta;
            mVelocityController.setGoalAndConstraints(
                    new MotionProfileGoal(displacement + steering_command.delta.dx,
                            Math.abs(steering_command.endVelocity), CompletionBehavior.VIOLATE_MAX_ACCEL,
                            Constants.PATH_FOLLOWING_GOAL_POS_TOLERANCE, Constants.PATH_FOLLOWING_GOAL_VEL_TOLERANCE),
                    new MotionProfileConstraints(Math.min(Constants.PATH_FOLLOWING_MAX_VEL, steering_command.maxVelocity),
                            maxAccel, maxDecel));

            if (steering_command.remainingPathLength < Constants.PATH_STOP_STEERING_DISTANCE) {
                doneSteering = true;
            }
            remainingPathLength = steering_command.remainingPathLength;
            
            if (!hasAutoStopCounterStarted && (steering_command.remainingPathLength < 25.0)) {
            	hasAutoStopCounterStarted = true;
            	autoStopCounterInitTime = Timer.getFPGATimestamp();
            }
            
            if (hasAutoStopCounterStarted && ((Timer.getFPGATimestamp() - autoStopCounterInitTime) > 2.0)) {
            	forceFinish();
            }
            
            SmartDashboard.putNumber("Remaining Path Length", remainingPathLength);
        }

        final double velocity_command = mVelocityController.update(new MotionState(t, displacement, velocity, 0.0), t);
        final double curvature = mLastSteeringDelta.dtheta / mLastSteeringDelta.dx;
        double dtheta = mLastSteeringDelta.dtheta;
        if (!Double.isNaN(curvature) && Math.abs(curvature) < Constants.BIG_NUMBER) {
            // Regenerate angular velocity command from adjusted curvature.
            final double abs_velocity_setpoint = Math.abs(mVelocityController.getSetpoint().vel());
            dtheta = mLastSteeringDelta.dx * curvature * (1.0 + inertiaSteeringGain * abs_velocity_setpoint);
        }
        double scale = velocity_command / mLastSteeringDelta.dx;
        final Twist rv = new Twist(mLastSteeringDelta.dx * scale, 0.0, -dtheta * scale);

        return rv;
    }
    
    public boolean isFinished() {
        /*checkedSteerQ = false;
        checkedVeloFinishedQ = false;
        checkedVeloOnTargetQ = false;
        if (mSteeringController.isFinished() && !checkedSteerQ){
            System.out.println("Steering Control Finished");
            checkedSteerQ = true;
        } if (mVelocityController.isFinishedProfile() && !checkedVeloFinishedQ){
            System.out.println("Velocity Control Finished");
            checkedVeloFinishedQ = true;
        } if (mVelocityController.onTarget() && !checkedVeloOnTargetQ){
            System.out.println("Velocity Control On Target");
            checkedVeloOnTargetQ = true;
        }
        */
        if ((mSteeringController.isFinished() && mVelocityController.isFinishedProfile()
        && mVelocityController.onTarget()) || overrideFinished){
            //System.out.println("Path Follower Finishing");
            return true;
        } else {
            return false;
        }
    }

    public void forceFinish() {
        overrideFinished = true;
    }
    
    public double remainingPathLength() {
    	return remainingPathLength;
    }
}