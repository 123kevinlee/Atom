package atomAlpha;

import battlecode.common.*;
import java.util.*;

public class EnlightenmentCenter {
    static final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
            Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };

    public static int lastInfluenceAmount = 0;
    public static int lastInfluenceGain = 0;
    public static int lastVotes = 0;

    public static boolean scoutingPhase = true;
    public static boolean setGuard = false;
    public static boolean rushPhase = false;
    public static boolean earlyDefensive = false;
    public static boolean firstFarmers = true;

    public static boolean guardsFull = false;

    public static int scoutCount = 0;
    public static int scoutLimit = 8;
    public static int guardCount = 0;
    public static int begFarmerLimit = 4;
    public static int farmerCount = 0;

    public static Map<Integer, Direction> scoutIds = new HashMap<Integer, Direction>();
    public static Map<Integer, String> scoutLastMessage = new HashMap<Integer, String>();
    public static int[] mapBorders = new int[4]; // 0=NORTH 1=EAST 2=SOUTH 3=WEST
    public static boolean mapComplete = false;
    public static LinkedHashSet<MapLocation> enemyBases = new LinkedHashSet<MapLocation>();
    public static Map<Direction, MapLocation> enemyCoords = new TreeMap<Direction, MapLocation>();
    public static LinkedHashSet<MapLocation> possibleEnemyBases = new LinkedHashSet<MapLocation>();

    public static void run(RobotController rc) throws GameActionException {
        calculateInfluenceGain(rc);

        if (scoutingPhase) {
            int dirIndex = scoutCount % 4;
            int influence = 1;
            Direction designatedDirection = directions[dirIndex * 2];

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

            if (scoutCount >= scoutLimit && !guardsFull) {
                setGuard = true;
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

                            enemyBases.add(new MapLocation(coords[0], coords[1]));
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

                                if (enemyCoords.size() > 0) {
                                    Object[] baseKeys = enemyCoords.keySet().toArray();
                                    currentLocation = rc.getLocation();
                                    for (Object bKey : baseKeys) {
                                        switch ((Direction) bKey) {
                                            case NORTH:
                                                possibleEnemyBases.add(new MapLocation(currentLocation.x,
                                                        mapBorders[0] - (currentLocation.y - mapBorders[2])));
                                                break;
                                            case EAST:
                                                possibleEnemyBases.add(new MapLocation(
                                                        mapBorders[1] - (currentLocation.x - mapBorders[3]),
                                                        currentLocation.y));
                                                break;
                                            case SOUTH:
                                                possibleEnemyBases.add(new MapLocation(currentLocation.x,
                                                        mapBorders[2] + (mapBorders[0] - currentLocation.y)));
                                                break;
                                            case WEST:
                                                possibleEnemyBases.add(new MapLocation(
                                                        mapBorders[3] + (mapBorders[1] - currentLocation.x),
                                                        currentLocation.y));
                                                break;

                                            default:
                                                break;
                                        }
                                        System.out.println("Possible Enemy Base:"
                                                + possibleEnemyBases.toArray()[possibleEnemyBases.size() - 1]
                                                        .toString());
                                    }

                                    // System.out.println(possibleEnemyBases.toString());
                                }
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
                                    possibleEnemyBases.add(new MapLocation(currentLocation.x,
                                            mapBorders[0] - (currentLocation.y - mapBorders[2])));
                                    break;
                                case EAST:
                                    possibleEnemyBases.add(new MapLocation(
                                            mapBorders[1] - (currentLocation.x - mapBorders[3]), currentLocation.y));
                                    break;
                                case SOUTH:
                                    possibleEnemyBases.add(new MapLocation(currentLocation.x,
                                            mapBorders[2] + (mapBorders[0] - currentLocation.y)));
                                    break;
                                case WEST:
                                    possibleEnemyBases.add(new MapLocation(
                                            mapBorders[3] + (mapBorders[1] - currentLocation.x), currentLocation.y));
                                    break;

                                default:
                                    break;
                            }
                            // System.out.println(possibleEnemyBases.toString());
                            System.out.println("Possible Enemy Base:"
                                    + possibleEnemyBases.toArray()[possibleEnemyBases.size() - 1].toString());
                        } else {
                            MapLocation baseLocation = rc.getLocation();
                            enemyCoords.put(scoutIds.get(key),
                                    new MapLocation(coords[0] + baseLocation.x, coords[1] + baseLocation.y));
                            Object[] baseKeys = enemyCoords.keySet().toArray();
                            System.out.println("Enemy Coord:" + enemyCoords.get(baseKeys[0]).toString());
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

        if (setGuard == true) {
            int influence = 10;
            int dirIndex = guardCount % 4;
            if (rc.canSetFlag(111) && rc.canBuildRobot(RobotType.POLITICIAN, directions[dirIndex * 2 + 1], influence)) {
                rc.setFlag(111); // defender politician
                rc.buildRobot(RobotType.POLITICIAN, directions[dirIndex * 2 + 1], influence);
                guardCount++;
            }

            if (guardCount > 4) {
                setGuard = false;
                guardsFull = true;
            }
        } else {
            if (enemyBases.size() > 0) {
                // System.out.println("YAHOO2");
                MapLocation currentLocation = rc.getLocation();
                MapLocation baseLocation = (MapLocation) enemyBases.toArray()[enemyBases.size() - 1];
                int dx = baseLocation.x - currentLocation.x;
                int dy = baseLocation.y - currentLocation.y;

                int influence = 1;
                if (rc.getInfluence() > 100) {
                    influence = 60;
                }
                // int influence = (int) Math.floor((lastInfluenceGain - bidAmount) * (1 / 4));
                Direction dir = rc.getLocation().directionTo(new MapLocation(baseLocation.x, baseLocation.y));
                if (rc.canBuildRobot(RobotType.MUCKRAKER, dir, influence)) {
                    if (rc.canSetFlag(Communication.coordEncoder("ENEMY", dx, dy))) {
                        rc.setFlag(Communication.coordEncoder("ENEMY", dx, dy));
                    }

                    rc.buildRobot(RobotType.MUCKRAKER, dir, influence);
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

        calculateBid(rc);
    }

    public static void calculateInfluenceGain(RobotController rc) {
        if (rc.getRoundNum() - Data.initRound == 0) {
            lastInfluenceAmount = rc.getInfluence();
        } else {
            lastInfluenceGain = rc.getInfluence() - lastInfluenceAmount;
            lastInfluenceAmount = rc.getInfluence();
        }
    }

    public static void calculateBid(RobotController rc) throws GameActionException {
        // Do Later
    }

    public static void init(RobotController rc) throws GameActionException {
        Data.originPoint = rc.getLocation();
        Data.initRound = rc.getRoundNum();
    }

    public static void scoutPhase(RobotController rc) throws GameActionException {

    }

    public static void createDefensePhase(RobotController rc) throws GameActionException {
        // Basic Defense Phase after scouts are sent out
    }

    public static void mainPhase(RobotController rc) throws GameActionException {

    }
}
