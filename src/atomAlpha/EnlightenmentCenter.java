package atomAlpha;

import battlecode.common.*;
import java.util.*;

public class EnlightenmentCenter {
    public static boolean scoutingPhase = true;
    public static boolean setGuard = false;
    public static boolean rushPhase = false;
    public static boolean earlyDefensive = false;

    public static int scoutCount = 0;
    public static int scoutLimit = 8;
    public static int scoutWave = 0;
    public static int guardCount = 0;
    // public static boolean[] scoutReturn = { false, false, false, false }; // 0=N,
    // 1=S, 2=E, 3=W
    public static Map<Integer, Direction> scoutIds = new HashMap<Integer, Direction>();
    public static Map<Integer, String> scoutLastMessage = new HashMap<Integer, String>();

    public static int[] mapBorders = new int[4]; // 0=NORTH 1=EAST 2=SOUTH 3=WEST
    public static boolean mapComplete = false;
    public static LinkedHashSet<int[]> enemyBases = new LinkedHashSet<int[]>();
    public static LinkedHashSet<int[]> enemyCoords = new LinkedHashSet<int[]>();
    public static LinkedHashSet<int[]> possibleEnemyBases = new LinkedHashSet<int[]>();

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

            // after first round of scouts - maybe build a defense against rush politicans
            if (scoutWave == 0) {

            }
            if (scoutWave == 2) {

            }
            int influence = Math.min(50, lastInfluenceAmount - bidAmount) / ((scoutWave + 1) % 2);
            Direction designatedDirection = Data.directions[dirIndex * 2];

            // if (scoutCount < scoutLimit && scoutWave % 2 == 0
            // && rc.canBuildRobot(RobotType.MUCKRAKER, designatedDirection, influence)) {
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
            } else if (scoutWave % 2 != 0) {
                /*
                 * if (rc.canSetFlag(100)) { rc.setFlag(100); }
                 */ // gonna need a number for farmers
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
                        } else if (msg.charAt(0) == '3') {
                            scoutLastMessage.put((int) key, msg);
                        }

                        else if (msg.charAt(0) == '4') {
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
                    String lastMsg = scoutLastMessage.get(key);
                    if (lastMsg != null && lastMsg.length() != 0) {
                        int[] coords = Communication.coordDecoder(lastMsg);
                        if (mapComplete) {
                            Direction dir = scoutIds.get(key);
                            MapLocation currentLocation = rc.getLocation();
                            switch (dir) {
                                case NORTH:
                                    possibleEnemyBases.add(new int[] { currentLocation.x,
                                            mapBorders[0] - (currentLocation.y - mapBorders[2]) });
                                    break;
                                case EAST:
                                    possibleEnemyBases.add(new int[] {
                                            mapBorders[1] - (currentLocation.x - mapBorders[3]), currentLocation.y });
                                    break;
                                case SOUTH:
                                    possibleEnemyBases.add(new int[] { currentLocation.x,
                                            mapBorders[2] + (mapBorders[0] - currentLocation.y) });
                                    break;
                                case WEST:
                                    possibleEnemyBases.add(new int[] {
                                            mapBorders[3] + (mapBorders[1] - currentLocation.x), currentLocation.y });
                                    break;

                                default:
                                    break;
                            }
                            // System.out.println(possibleEnemyBases.toString());
                            System.out.println("Possible Enemy Base:" + possibleEnemyBases.iterator().next()[0] + ","
                                    + possibleEnemyBases.iterator().next()[1]);
                        } else {
                            MapLocation baseLocation = rc.getLocation();
                            enemyCoords.add(new int[] { coords[0] + baseLocation.x, coords[1] + baseLocation.y });
                            System.out.println("Enemy Coord:" + enemyCoords.iterator().next()[0] + ","
                                    + enemyCoords.iterator().next()[1]);
                        }
                    }
                    removeId = key;
                }
            }
            if (removeId != null) {
                scoutIds.remove(removeId);
            }
        }
        // System.out.println(mapBorders[0]);
        // System.out.println(enemyBases.get(0)[0] + " " + enemyBases.get(0)[1]);

        if (setGuard == true)

        {
            if (rc.canSetFlag(111)) {
                rc.setFlag(111); // defender politician
            }
            int influence = 10;
            int dirIndex = guardCount % 4;
            if (rc.canBuildRobot(RobotType.POLITICIAN, Data.directions[dirIndex * 2 + 1], influence)) {
                rc.buildRobot(RobotType.POLITICIAN, Data.directions[dirIndex * 2 + 1], influence);
                guardCount++;
            }
        } else {
            if (enemyBases.size() > 0) {
                // System.out.println("YAHOO2");
                MapLocation currentLocation = rc.getLocation();
                int dx = enemyBases.iterator().next()[0] - currentLocation.x;
                int dy = enemyBases.iterator().next()[1] - currentLocation.y;
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
                        .directionTo(new MapLocation(enemyBases.iterator().next()[0], enemyBases.iterator().next()[1]));
                if (rc.canBuildRobot(RobotType.POLITICIAN, dir, influence)) {
                    rc.buildRobot(RobotType.POLITICIAN, dir, influence);
                    guardCount++;
                }
            }
        }

        if (enemyBases.size() == 0 && possibleEnemyBases.size() > 0) {
            // do something
        } else if (enemyBases.size() == 0 && possibleEnemyBases.size() > 0) {

        } else {
            // when there's practically no info
            // more defensive and if there are enemy unit coords -- light search attacks?
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
