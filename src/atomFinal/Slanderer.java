package atomFinal;

import battlecode.common.*;

public class Slanderer {
    public static Direction safeDirection = Direction.CENTER;
    public static String role = "";
    public static int boundary = 16;

    public static void run(RobotController rc) throws GameActionException {
        //System.out.println("HERE");
        logic(rc);
    }

    public static void logic(RobotController rc) throws GameActionException {
        Direction randomDir = Data.directions[(int) (Math.random() * 8)];
        int boundary = 16;
        //System.out.println("HERE");
        if (rc.getInfluence() == 130 && rc.getRoundNum() - Data.initRound < 5) {
            if (rc.canMove(Direction.SOUTH)) {
                rc.move(Direction.SOUTH);
            }
            boundary = 8;
        }

        MapLocation thisLocation = rc.getLocation();
        if (rc.canSenseRadiusSquared(20)) {
            RobotInfo[] robots = rc.senseNearbyRobots(20, rc.getTeam().opponent());
            for (RobotInfo robot : robots) {
                if (robot.getType().equals(RobotType.MUCKRAKER)) {
                    Direction nextDir = Pathfinding.basicBug(rc,
                            thisLocation.add(thisLocation.directionTo(robot.getLocation()).opposite()));
                    if (rc.canMove(nextDir)) {
                        rc.move(nextDir);
                        return;
                    }
                }
            }
        }

        if (thisLocation.distanceSquaredTo(Data.originPoint) < 6) {
            //System.out.println("AWAY");
            Direction nextDir = Pathfinding.basicBug(rc,
                    thisLocation.add(thisLocation.directionTo(Data.originPoint).opposite()));
            if (rc.canMove(nextDir)) {
                rc.move(nextDir);
                return;
            }
        } else if (thisLocation.distanceSquaredTo(Data.originPoint) > boundary) {
            Direction nextDir = Pathfinding.basicBug(rc, thisLocation.add(thisLocation.directionTo(Data.originPoint)));
            if (rc.canMove(nextDir)) {
                rc.move(nextDir);
                return;
            }
        }

        Direction nextDir = Pathfinding.basicBug(rc, rc.getLocation().add(randomDir));
        if (rc.canMove(nextDir)) {
            rc.move(nextDir);
        }
    }

    public static void init(RobotController rc) throws GameActionException {
        if (rc.canSenseRadiusSquared(2)) {
            for (RobotInfo robot : rc.senseNearbyRobots(2, rc.getTeam())) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    Data.baseId = robot.getID();
                    if (rc.canGetFlag(Data.baseId)) {
                        role = Integer.toString(rc.getFlag(Data.baseId));
                        Data.originPoint = robot.getLocation();
                        Data.relOriginPoint[0] = Data.originPoint.x % 128;
                        Data.relOriginPoint[1] = Data.originPoint.y % 128;
                        Data.initRound = rc.getRoundNum();
                        if (rc.canSetFlag(rc.getFlag(Data.baseId))) {
                            if (rc.canSetFlag(Integer.parseInt(role))) {
                                rc.setFlag(Integer.parseInt(role));
                            }
                        }
                    }
                }
            }
        }
    }
}
