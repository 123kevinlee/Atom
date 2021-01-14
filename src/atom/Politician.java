package atom;

import battlecode.common.*;

public class Politician {
    public static String role = "";
    public static MapLocation originPoint;

    public static void run(RobotController rc) throws GameActionException {
        Team enemy = rc.getTeam().opponent();

        if (rc.canSenseRadiusSquared(25)) {
            for (RobotInfo robot : rc.senseNearbyRobots(25)) {
                if (robot.getTeam().equals(Team.NEUTRAL) || robot.getTeam().equals(rc.getTeam().opponent())) {
                    if (robot.getLocation().isWithinDistanceSquared(rc.getLocation(), 9)) {
                        if (rc.canEmpower(9)) {
                            rc.empower(9);
                        } else {
                            Direction nextDir = Pathfinding.basicBug(rc, robot.getLocation());
                            if (rc.canMove(nextDir)) {
                                rc.move(nextDir);
                            }
                            // //mostly for when mucks are going to possible coords
                            // //and they are wrong, so they switch targets
                            // //System.out.println("NEW TARGET");
                            int relx = robot.getLocation().x % 128;
                            int rely = robot.getLocation().y % 128;
                            int newFlag = Communication.coordEncoder("ENEMY", relx, rely);
                            if (rc.canSetFlag(newFlag)) {
                                rc.setFlag(newFlag);
                                role = Integer.toString(newFlag);
                            }
                        }
                    }
                }
            }
        }

        if (role.equals("1")) {
            if (rc.canSenseRadiusSquared(25)) {
                for (RobotInfo robot : rc.senseNearbyRobots(25)) {
                    if (robot.getTeam().equals(rc.getTeam())) {
                        Direction away = rc.getLocation().directionTo(robot.getLocation()).opposite();
                        MapLocation target = rc.getLocation().add(away).add(away).add(away);
                        away = Pathfinding.basicBug(rc, target);
                        if (rc.canMove(away)) {
                            rc.move(away);
                        }
                    }
                }
            }
        }

        //logic for politicians that just converted from enemy politician or a slanderer
        if (role.equals("")) {
            isConvertedEnemy(rc);
        } else if (role.length() == 7) {
            int[] coords = Communication.relCoordDecoder(role);
            int[] distance = Pathfinding.getDistance(Data.relOriginPoint, coords);
            MapLocation target = Data.originPoint.translate(distance[0], distance[1]);
            Direction nextDir = Pathfinding.basicBug(rc, target);
            if (rc.canMove(nextDir)) {
                rc.move(nextDir);
            }
        }
    }

    public static void isConvertedEnemy(RobotController rc) throws GameActionException {
        if (rc.canSenseRadiusSquared(25)) {
            RobotInfo robots[] = rc.senseNearbyRobots(25, rc.getTeam());
            for (RobotInfo robot : robots) {
                if (rc.canGetFlag(robot.getID())) {
                    String allyFlag = Integer.toString(rc.getFlag(robot.getID()));
                    //attempts to get any attacking orders from other allied units
                    if ((allyFlag.charAt(0) == '2' & allyFlag.length() == 7)) {
                        if (rc.canSetFlag(Integer.parseInt(allyFlag))) {
                            rc.setFlag(Integer.parseInt(allyFlag));
                            role = allyFlag;
                        }
                    } else {
                        Direction randomDirection = Data.directions[(int) (Math.random() * Data.directions.length)];
                        if (rc.canMove(randomDirection)) {
                            rc.move(randomDirection);
                        }
                    }
                }
            }
        }
    }

    public static void init(RobotController rc) throws GameActionException {
        if (rc.canSenseRadiusSquared(3)) {
            for (RobotInfo robot : rc.senseNearbyRobots(3, rc.getTeam())) {
                if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    Data.baseId = robot.getID();
                    role = Integer.toString(rc.getFlag(Data.baseId));
                    Data.originPoint = robot.getLocation();
                    Data.relOriginPoint[0] = Data.originPoint.x % 128;
                    Data.relOriginPoint[1] = Data.originPoint.y % 128;
                    Data.initRound = rc.getRoundNum();
                    if (rc.canGetFlag(Data.baseId)) {
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
