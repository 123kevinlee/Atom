package atomAlpha;

import battlecode.common.*;
import java.util.*;

public class EnlightenmentCenter {
    public static boolean scoutingPhase = true;
    public static boolean setGuard = false;
    public static boolean rushPhase = false;

    public static int scoutCount = 0;
    public static int guardCount = 0;
    // public static boolean[] scoutReturn = { false, false, false, false }; // 0=N,
    // 1=S, 2=E, 3=W
    public static Map<Integer, Direction> scoutIds = new TreeMap<Integer, Direction>();

    public static int[] mapBorders = new int[4]; // 0=NORTH 1=EAST 2=SOUTH 3=WEST
    public static ArrayList<int[]> enemyBases = new ArrayList<int[]>();
    public static boolean mapComplete = false;

    public static int scoutLimit = 13;

    public static void run(RobotController rc, int turnCount) throws GameActionException {
        if (scoutingPhase && scoutCount < scoutLimit) {
            if (rc.canSetFlag(100)) {
                rc.setFlag(100);
            }

            int dirIndex = scoutCount % 4;
            // System.out.println(scoutCount);
            int influence = 1;
            Direction designatedDirection = Helper.directions[dirIndex * 2];

            if (rc.canBuildRobot(RobotType.SLANDERER, designatedDirection, influence)) {
                rc.buildRobot(RobotType.SLANDERER, designatedDirection, influence);
                scoutCount++;
                if (rc.canSenseRadiusSquared(1)) {
                    for (RobotInfo robot : rc.senseNearbyRobots(1, rc.getTeam())) {
                        scoutIds.put(robot.getID(), designatedDirection);
                    }
                }
            }

            Object removeId = null;
            Object[] keys = scoutIds.keySet().toArray();

            for (Object key : keys) {
                if (rc.canGetFlag((int) key)) {
                    int flag = rc.getFlag((int) key);
                    if (flag != 0) {
                        // System.out.println("id: " + key + " msg:" + flag);

                        String msg = Integer.toString(flag);
                        int[] coords = Communication.coordDecoder(msg);

                        if (msg.charAt(0) == '2') {
                            MapLocation currentLocation = rc.getLocation();
                            coords[0] += currentLocation.x;
                            coords[1] += currentLocation.y;
                            System.out.println("ENEMY BASE: " + coords[0] + "," + coords[1]);

                            enemyBases.add(coords);
                            // System.out.println(enemyBases.get(0)[0] + " " + enemyBases.get(0)[1]);
                        } else if (msg.charAt(0) == '4') {
                            // System.out.println("WALL: " + coords[0] + "," + coords[1]);

                            MapLocation currentLocation = rc.getLocation();

                            switch (scoutIds.get(key)) {
                                case NORTH:
                                    mapBorders[0] = currentLocation.y + coords[1];
                                    break;
                                case EAST:
                                    mapBorders[1] = currentLocation.x + coords[0];
                                    break;
                                case SOUTH:
                                    mapBorders[2] = currentLocation.y + coords[1];
                                    break;
                                case WEST:
                                    mapBorders[3] = currentLocation.x + coords[0];
                                    break;
                                default:
                                    break;
                            }

                            // method to find last border using the other 3 border values
                            int zeroCount = 0;
                            for (int i = 0; i < mapBorders.length; i++) {
                                if (mapBorders[i] == 0) {
                                    zeroCount++;
                                }
                            }
                            if (zeroCount == 1 && !mapComplete) {
                                System.out.println("Calculating World Map...");
                                int missingIndex = 0;
                                for (int i = 0; i < mapBorders.length; i++) {
                                    if (mapBorders[i] == 0) {
                                        missingIndex = i;
                                    }
                                }
                                if (missingIndex % 2 == 0) { // North Or South
                                    int width = Math.abs(mapBorders[1] - mapBorders[3]);
                                    if (missingIndex == 0) {
                                        mapBorders[missingIndex] = mapBorders[2] + width;
                                    } else if (missingIndex == 2) {
                                        mapBorders[missingIndex] = mapBorders[0] - width;
                                    }
                                } else {
                                    int height = Math.abs(mapBorders[0] - mapBorders[2]);
                                    if (missingIndex == 1) {
                                        mapBorders[missingIndex] = mapBorders[3] + height;
                                    } else if (missingIndex == 3) {
                                        mapBorders[missingIndex] = mapBorders[1] - height;
                                    }
                                }
                                mapComplete = true;
                            }
                        }
                    }
                } else {
                    System.out.println(key + " DEAD");
                    removeId = key;
                    // do a last known location for potential enemy bases
                }
            }
            if (removeId != null) {
                scoutIds.remove(removeId);
            }
        }
        // System.out.println(mapBorders[0]);
        // System.out.println(enemyBases.get(0)[0] + " " + enemyBases.get(0)[1]);

        if (setGuard == true) {
            if (rc.canSetFlag(111)) {
                rc.setFlag(111);
                // defender politician
            }
            int influence = 10;
            int dirIndex = guardCount % 4;
            if (rc.canBuildRobot(RobotType.POLITICIAN, Helper.directions[dirIndex * 2 + 1], influence)) {
                rc.buildRobot(RobotType.POLITICIAN, Helper.directions[dirIndex * 2 + 1], influence);
                guardCount++;
            }
        } else {
            if (enemyBases.size() > 0) {
                //System.out.println("YAHOO2");
                if (rc.canSetFlag(encodeTarget(enemyBases.get(0)))) {
                    rc.setFlag(encodeTarget(enemyBases.get(0)));
                }
                int influence = 10;
                Direction dir = rc.getLocation()
                        .directionTo(new MapLocation(enemyBases.get(0)[0], enemyBases.get(0)[1]));
                if (rc.canBuildRobot(RobotType.POLITICIAN, dir, influence)) {
                    rc.buildRobot(RobotType.POLITICIAN, dir, influence);
                    guardCount++;
                }
            }
        }
    }

    public static int encodeTarget(int[] coords) {
        String out = "";
        int x = coords[0];
        int y = coords[1];
        if (x < 0) {
            out += "9";
            x = Math.abs(x);
        } else {
            out += "8";
        }

        if (x < 10) {
            out += "0" + x;
        } else {
            out += x;
        }

        if (y < 0) {
            out += "9";
        } else {
            out += "8";
        }

        if (y < 10) {
            out += "0" + y;
        } else {
            out += y;
        }

        return Integer.parseInt(out);
    }
}
