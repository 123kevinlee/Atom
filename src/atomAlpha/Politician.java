package atomAlpha;

import battlecode.common.*;

public class Politician {
    public static void run(RobotController rc, int turnCount) throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            // System.out.println("empowering...");
            rc.empower(actionRadius);
            // System.out.println("empowered");
            return;
        }
        if (Helper.tryMove(rc, Helper.randomDirection())) {

        }
        // System.out.println("I moved!");
    }

    public static void getRole(RobotController rc) {

    }
}
