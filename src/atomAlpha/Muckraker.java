package atomAlpha;

import battlecode.common.*;

public class Muckraker {
    public static void run(RobotController rc, int turnCount) throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    // System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return;
                }
            }
        }
        if (Helper.tryMove(rc, Helper.randomDirection())) {

        }
        // System.out.println("I moved!");
    }
}
