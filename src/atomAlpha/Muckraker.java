package atomAlpha;

import battlecode.common.*;

public class Muckraker {
    public static String role = "";

    public static void run(RobotController rc, int turnCount) throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;

        if (rc.canSenseRadiusSquared(actionRadius)) {
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

            if (rc.canMove(targetDirection)) {
                rc.move(targetDirection);
            }
        }
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
