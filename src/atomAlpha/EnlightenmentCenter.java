package atomAlpha;

import battlecode.common.*;
import java.util.*;

public class EnlightenmentCenter {
    public static boolean scoutingPhase = true;
    public static boolean setGuard = false;
    public static boolean rushPhase = false;
    public static boolean earlyDefensive = false;

    public static int scoutCount = 0;
    public static int guardCount = 0;
    // public static boolean[] scoutReturn = { false, false, false, false }; // 0=N,
    // 1=S, 2=E, 3=W
    public static Map<Integer, Direction> scoutIds = new TreeMap<Integer, Direction>();

    public static int[] mapBorders = new int[4]; // 0=NORTH 1=EAST 2=SOUTH 3=WEST
    public static ArrayList<int[]> enemyBases = new ArrayList<int[]>();
    public static boolean mapComplete = false;

    public static int scoutLimit = 8;

    public static boolean begunInfluenceCalc = false;
    public static int lastInfluenceAmount = 0;
    public static int lastInfluenceGain = 0;

    public static void run(RobotController rc, int turnCount) throws GameActionException {
        // Influence Calc
        if (!begunInfluenceCalc) {
            lastInfluenceAmount = rc.getInfluence();
            begunInfluenceCalc = true;
        } else {
            lastInfluenceGain = rc.getInfluence() - lastInfluenceAmount;
            lastInfluenceAmount = rc.getInfluence();
        }
        // System.out.println(lastInfluenceAmount);
        // base bid on gain and amount needed to be spent later

        if (scoutingPhase) {
            int dirIndex = scoutCount % 4;

            int lowestPossibleBid = 3; // don't know what to set this to for now 5?
            // int bidAmount = Math.max((int) Math.floor(lastInfluenceAmount * 1 / 15),
            // lowestPossibleBid);
            int bidAmount = 0;
            if (rc.canBid(bidAmount)) {
                rc.bid(bidAmount);
                System.out.println("Bid: " + bidAmount);
            }

            int influence = lastInfluenceAmount - bidAmount;
            Direction designatedDirection = Helper.directions[dirIndex * 2];

            // after first round of scouts - maybe build a defense against rush politicans

            if (scoutCount < scoutLimit && rc.canBuildRobot(RobotType.MUCKRAKER, designatedDirection, influence)) {
                if (rc.canSetFlag(100)) {
                    rc.setFlag(100);
                }
                rc.buildRobot(RobotType.MUCKRAKER, designatedDirection, influence);
                System.out.println("Created Scout with " + influence + " influence");
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
                            // System.out.println("ENEMY BASE: " + coords[0] + "," + coords[1]);

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
                rc.setFlag(111); // defender politician
            }
            int influence = 10;
            int dirIndex = guardCount % 4;
            if (rc.canBuildRobot(RobotType.POLITICIAN, Helper.directions[dirIndex * 2 + 1], influence)) {
                rc.buildRobot(RobotType.POLITICIAN, Helper.directions[dirIndex * 2 + 1], influence);
                guardCount++;
            }
            if (guardCount >= 4) {
                setGuard = false;
            }
        } else {
            if (enemyBases.size() > 0) {
                // System.out.println("YAHOO2");
                MapLocation currentLocation = rc.getLocation();
                int dx = enemyBases.get(0)[0] - currentLocation.x;
                int dy = enemyBases.get(0)[1] - currentLocation.y;
                if (rc.canSetFlag(Communication.coordEncoder("ENEMY", dx, dy))) {
                    rc.setFlag(Communication.coordEncoder("ENEMY", dx, dy));
                }

                int lowestPossibleBid = 3; // don't know what to set this to for now 5?
                // int bidAmount = Math.max((int) Math.floor(lastInfluenceAmount * 1 / 10),
                // lowestPossibleBid);
                int bidAmount = 0;
                if (rc.canBid(bidAmount)) {
                    rc.bid(bidAmount);
                    System.out.println("Bid: " + bidAmount);
                }

                // int influence = (int) Math.floor((int) (lastInfluenceAmount - bidAmount) /
                // 5);
                int influence = 0;
                if (rc.getInfluence() > 100) {
                    influence = 60;
                }
                // int influence = (int) Math.floor((lastInfluenceGain - bidAmount) * (1 / 4));
                Direction dir = rc.getLocation()
                        .directionTo(new MapLocation(enemyBases.get(0)[0], enemyBases.get(0)[1]));
                if (rc.canBuildRobot(RobotType.POLITICIAN, dir, influence)) {
                    rc.buildRobot(RobotType.POLITICIAN, dir, influence);
                    guardCount++;
                }
            }
        }

        /*
         * if (enemyBases.size() > 0) { // System.out.println("YAHOO2"); MapLocation
         * currentLocation = rc.getLocation(); int dx = enemyBases.get(0)[0] -
         * currentLocation.x; int dy = enemyBases.get(0)[1] - currentLocation.y; if
         * (rc.canSetFlag(Communication.coordEncoder("ENEMY", dx, dy))) {
         * rc.setFlag(Communication.coordEncoder("ENEMY", dx, dy)); } int influence =
         * 10; Direction dir = rc.getLocation().directionTo(new
         * MapLocation(enemyBases.get(0)[0], enemyBases.get(0)[1])); if
         * (rc.canBuildRobot(RobotType.MUCKRAKER, dir, influence)) {
         * rc.buildRobot(RobotType.MUCKRAKER, dir, influence); } }
         */
    }
}
