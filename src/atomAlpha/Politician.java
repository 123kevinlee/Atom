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
        if (role.length() == 7) {
            System.out.println("I moved!");
            int[] coords = Communication.coordDecoder(role);
            MapLocation currentLocation = rc.getLocation();
            coords[0] += currentLocation.x;
            coords[1] += currentLocation.y;
            System.out.println("ENEMY TARGET: " + coords[0] + "," + coords[1]);

            MapLocation targetLocation = new MapLocation(coords[0], coords[1]);
            Direction targetDirection = currentLocation.directionTo(targetLocation);

            if (rc.canMove(Pathfinding.chooseBestNextStep(rc, targetDirection))) {
                rc.move(Pathfinding.chooseBestNextStep(rc, targetDirection));
            }
        }
        // create a locking mechanism and chasing mechanism
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
