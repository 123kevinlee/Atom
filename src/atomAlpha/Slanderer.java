package atomAlpha;

import battlecode.common.*;

public class Slanderer {
    public static Direction scoutDirection;

    public static String role = "";

    public static void run(RobotController rc) throws GameActionException {
        //scout direction code

        // if (rc.getRoundNum() - Data.initRound < 2) {
        //     for (Direction dir : Direction.cardinalDirections()) {
        //         if (!rc.canMove(dir) && rc.getCooldownTurns() == 0) {
        //             switch (dir) {
        //                 case NORTH:
        //                     scoutDirection = Direction.SOUTH;
        //                     break;
        //                 case EAST:
        //                     scoutDirection = Direction.WEST;
        //                     break;
        //                 case SOUTH:
        //                     scoutDirection = Direction.NORTH;
        //                     break;
        //                 case WEST:
        //                     scoutDirection = Direction.EAST;
        //                     break;
        //                 default:
        //                     break;
        //             }
        //         }
        //     }
        // }
        // if (rc.canMove(Pathfinding.chooseBestNextStep(rc, scoutDirection))) {
        //     rc.move(Pathfinding.chooseBestNextStep(rc, scoutDirection));
        //     MapLocation currentLocation = rc.getLocation();
        //     int dx = currentLocation.x - Data.originPoint.x;
        //     int dy = currentLocation.y - Data.originPoint.y;
        //     int outMsg = Communication.coordEncoder("LIKELY", dx, dy);
        //     if (rc.canSetFlag(outMsg)) {
        //         rc.setFlag(outMsg);
        //     }
        // } else {
        //     //For scouts. If pathinfinding doesnt work in a direction, a wall must be there
        //     switch (scoutDirection) {
        //         case NORTH:
        //             if (!rc.onTheMap(rc.getLocation().add(Direction.NORTH))) {
        //                 System.out.println("WALL!");
        //                 MapLocation currentLocation = rc.getLocation();
        //                 int dx = currentLocation.x - Data.originPoint.x;
        //                 int dy = currentLocation.y - Data.originPoint.y;
        //                 int outMsg = Communication.coordEncoder("WALL", dx, dy);
        //                 if (rc.canSetFlag(outMsg)) {
        //                     rc.setFlag(outMsg);
        //                 }

        //                 scoutDirection = Direction.CENTER;
        //             }
        //             break;
        //         case EAST:
        //             if (!rc.onTheMap(rc.getLocation().add(Direction.EAST))) {
        //                 System.out.println("WALL!");
        //                 MapLocation currentLocation = rc.getLocation();
        //                 int dx = currentLocation.x - Data.originPoint.x;
        //                 int dy = currentLocation.y - Data.originPoint.y;
        //                 int outMsg = Communication.coordEncoder("WALL", dx, dy);
        //                 if (rc.canSetFlag(outMsg)) {
        //                     rc.setFlag(outMsg);
        //                 }

        //                 scoutDirection = Direction.CENTER;
        //             }
        //             break;
        //         case SOUTH:
        //             if (!rc.onTheMap(rc.getLocation().add(Direction.SOUTH))) {
        //                 System.out.println("WALL!");
        //                 MapLocation currentLocation = rc.getLocation();
        //                 int dx = currentLocation.x - Data.originPoint.x;
        //                 int dy = currentLocation.y - Data.originPoint.y;
        //                 int outMsg = Communication.coordEncoder("WALL", dx, dy);
        //                 if (rc.canSetFlag(outMsg)) {
        //                     rc.setFlag(outMsg);
        //                 }

        //                 scoutDirection = Direction.CENTER;
        //             }
        //             break;
        //         case WEST:
        //             if (!rc.onTheMap(rc.getLocation().add(Direction.WEST))) {
        //                 System.out.println("WALL!");
        //                 MapLocation currentLocation = rc.getLocation();
        //                 int dx = currentLocation.x - Data.originPoint.x;
        //                 int dy = currentLocation.y - Data.originPoint.y;
        //                 int outMsg = Communication.coordEncoder("WALL", dx, dy);
        //                 if (rc.canSetFlag(outMsg)) {
        //                     rc.setFlag(outMsg);
        //                 }

        //                 scoutDirection = Direction.CENTER;
        //             }
        //             break;
        //         default:
        //             break;
        //     }
        // }
        // //Scout scanning to find enemy base and keep its coordinates
        // int sensorRadius = rc.getType().sensorRadiusSquared;
        // if (rc.canSenseRadiusSquared(sensorRadius)) {
        //     for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, rc.getTeam().opponent())) {
        //         if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
        //             MapLocation baseLocation = robot.getLocation();
        //             int dx = baseLocation.x - Data.originPoint.x;
        //             int dy = baseLocation.y - Data.originPoint.y;
        //             int outMsg = Communication.coordEncoder("ENEMY", dx, dy);
        //             System.out.println("Found Enemy Base:" + outMsg);
        //             if (rc.canSetFlag(outMsg)) {
        //                 rc.setFlag(outMsg);
        //             }
        //         }
        //     }
        // }

        // Below is farming code, given a safe direction to a corner through a flag, the scout will move to the corner, if the corner is covered, the scout will move along the wall to make use of space
        
        System.out.println(role);

        if(role.length() == 7){

            System.out.println("I moved!");
            int[] coords = Communication.coordDecoder(role); // coords of safe corner
            coords[0] += Data.originPoint.x;
            coords[1] += Data.originPoint.y;
            System.out.println("SAFE CORNER: " + coords[0] + "," + coords[1]);
            MapLocation targetLocation = new MapLocation(coords[0], coords[1]);
            Direction targetDirection = Data.originPoint.directionTo(targetLocation);
            System.out.println(rc.canMove(Pathfinding.chooseBestNextStep(rc, targetDirection)));
            if (rc.canMove(Pathfinding.chooseBestNextStep(rc, targetDirection))) {
               
                rc.move(Pathfinding.chooseBestNextStep(rc, targetDirection));
            }
        }
    }

    public static void init(RobotController rc) throws GameActionException {
        if (rc.canSenseRadiusSquared(3)) {
            for (RobotInfo robot : rc.senseNearbyRobots(3, rc.getTeam())) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    Data.baseId = robot.getID();
                    if (rc.canGetFlag(Data.baseId)) {
                        if (rc.canSetFlag(rc.getFlag(Data.baseId))) {
                            role = Integer.toString(rc.getFlag(Data.baseId));
                            Data.originPoint = robot.getLocation();
                            Data.initRound = rc.getRoundNum();
                        }
                    }
                }
            }
        }
    }
}
