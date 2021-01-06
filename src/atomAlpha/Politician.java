package atomAlpha;

import battlecode.common.*;

public class Politician {
    public static String role = "";

    public static void run(RobotController rc, int turnCount) throws GameActionException {

        Team enemy = rc.getTeam().opponent();

        MapLocation myLoc = rc.getLocation();

        int detectionRadiusSquared = 25;
        int actionRadiusSquared = 9;
        int chaseCount = 0;

        RobotInfo[] attackable = rc.senseNearbyRobots(myLoc, detectionRadiusSquared, enemy);

        /*
         * int thisId = rc.getID(); if (role.equals("")) { // this means it just
         * converted from slanderer if (rc.canGetFlag(thisId)) { role =
         * Integer.toString(rc.getFlag(thisId)); } // this flag isn't the target }
         */

        System.out.println(role);
        if (role.length() == 7) {

            // create a locking mechanism and chasing mechanism
            // marks the last known location of the closest enemy bot, no flags needed
            // moves in the direction of this location, if within sensor radius, blows up
            // if the enemy bot is already destroyed, the bot must resume trying to blow up
            // the enlightment center
            if (attackable.length != 0 && chaseCount != -1) {
                System.out.println("TRACKING");

                RobotInfo closeEnemy = attackable[0];
                MapLocation track = closeEnemy.getLocation();
                int[] tracked = new int[2];
                tracked[0] += track.x;
                tracked[1] += track.y;

                System.out.println("ENEMY ROBOT: " + tracked[0] + "," + tracked[1]);
                Direction toCloseEnemy = myLoc.directionTo(track);

                if (myLoc.distanceSquaredTo(track) <= actionRadiusSquared && rc.canEmpower(actionRadiusSquared)) {
                    rc.empower(actionRadiusSquared);
                    System.out.println("Empowered");

                } else if (rc.canMove(Pathfinding.chooseBestNextStep(rc, toCloseEnemy))) {
                    rc.move(Pathfinding.chooseBestNextStep(rc, toCloseEnemy));
                    chaseCount++;
                }
            }

            System.out.println("I moved!");
            int[] coords = Communication.coordDecoder(role); // coords of enemy base or whatever target
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
                    Data.baseId = robot.getID();
                    if (rc.canGetFlag(Data.baseId)) {
                        if (rc.canSetFlag(rc.getFlag(Data.baseId))) {
                            role = Integer.toString(rc.getFlag(Data.baseId));
                            Data.originPoint = robot.getLocation();
                        }
                    }
                }
            }
        }
    }
}
