package atomAlpha;

import battlecode.common.*;

public class Politician {
    public static String role = "";

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
        if (rc.canSenseRadiusSquared(3) && rc.senseNearbyRobots(3, enemy) != null && rc.canEmpower(3) && role.equals("111")) {
            rc.empower(3);
        }
        // System.out.println("I moved!");
    }

    public static void getRole(RobotController rc) throws GameActionException {
        if (rc.canSenseRadiusSquared(1)) {
            for (RobotInfo robot : rc.senseNearbyRobots(1, rc.getTeam())) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    if (rc.canGetFlag(robot.getID())) {
                        if (rc.canSetFlag(rc.getFlag(robot.getID()))) {
                            role = Integer.toString(rc.getFlag(robot.getID()));
                        }
                    }
                }
            }
        }
    }
}
