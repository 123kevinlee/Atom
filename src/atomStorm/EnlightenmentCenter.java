package atomStorm;

import battlecode.common.*;
import java.util.*;

import javax.swing.text.Position;

public class EnlightenmentCenter {

    public static int lastInfluenceAmount = 0;
    public static int lastInfluenceGain = 0;
    public static int lastVotes = 0;

    public static ArrayList<RobotType> spawnOrder = new ArrayList<RobotType>();
    public static int spawnOrderCounter = 0;
    public static Direction detectedEnemyDirection = Direction.CENTER;

    public static boolean scoutingPhase = true;

    public static int scoutCount = 0;
    public static int scoutLimit = 4;
    public static Map<Integer, Direction> scoutIds = new HashMap<Integer, Direction>();
    public static Set<Integer> waller = new HashSet<Integer>(); // fricking waller direction is annoying so they can
                                                                // only contribute with absolute units -- nothing based
                                                                // on their direction
    public static Map<Integer, String> scoutLastMessage = new HashMap<Integer, String>();

    public static int guardCount = 0;
    public static int[] optimalFarmingInfluence = new int[] { 21, 41, 63, 85, 107, 130, 154, 178, 203, 229, 255, 282,
            310, 341, 369, 400, 431, 463, 497, 533, 569, 605, 644, 683, 724, 767, 810, 855, 903, 949 };
    public static int farmerInitial = 1;
    public static int farmerLimit = 0;
    public static int farmerCount = 0;
    public static Set<Integer> farmerIds = new HashSet<Integer>();
    public static int poliCount = 0;

    public static int[] mapBorders = new int[4]; // 0=NORTH 1=EAST 2=SOUTH 3=WEST
    public static boolean mapComplete = false;

    public static LinkedHashSet<MapLocation> enemyBases = new LinkedHashSet<MapLocation>();
    public static Map<Direction, MapLocation> enemyCoords = new TreeMap<Direction, MapLocation>();
    public static LinkedHashSet<MapLocation> possibleEnemyBases = new LinkedHashSet<MapLocation>();
    public static LinkedHashMap<MapLocation, Integer> neutralBases = new LinkedHashMap<MapLocation, Integer>();

    public static void run(RobotController rc) throws GameActionException {
        calculateInfluenceGain(rc); // calculates the influence gain between last round and this round
        calculateFarmers(rc); // calculates the amount of farmers in the field
        calculatePoliticians(rc); //calculates the amount of scatter defenders around the base
        //calculateBid(rc); //calculates the amount to bid

        // Panic Bid: logic to uses remaining influence to bid when surrounded by enemies
        // if (rc.senseNearbyRobots(2, rc.getTeam().opponent()).length == 12) {
        //     if (rc.canBid(rc.getInfluence() / 10)) {
        //         rc.bid(rc.getInfluence() / 10);
        //     }
        // }

        if (scoutIds.size() > 0) {
            listenForScoutMessages(rc);
        }

        System.out.println("ENEMY:" + enemyBases.size() + "POSSIBLE:" + possibleEnemyBases.size());
        if (enemyBases.size() > 0) {
            if (rc.getRoundNum() > 2000) {
                int dx = enemyBases.iterator().next().x - rc.getLocation().x;
                int dy = enemyBases.iterator().next().y - rc.getLocation().y;
                int flag = Communication.coordEncoder("ENEMY", dx, dy);
                if (rc.canSetFlag(flag)) {
                    rc.setFlag(flag);
                }
            }
        } else if (possibleEnemyBases.size() > 0) {
            if (rc.getRoundNum() > 2000) {
                int dx = possibleEnemyBases.iterator().next().x - rc.getLocation().x;
                int dy = possibleEnemyBases.iterator().next().y - rc.getLocation().y;
                int flag = Communication.coordEncoder("ENEMY", dx, dy);
                if (rc.canSetFlag(flag)) {
                    rc.setFlag(flag);
                }
            }
        }

        //maybe keep spawning until a scout see's an enemy
        if (farmerCount < farmerInitial) {
            spawnFarmers(rc);
        }
        if (scoutingPhase) {
            scoutPhase(rc);
        }

        if (rc.canSenseRadiusSquared(40)) {
            RobotInfo[] robots = rc.senseNearbyRobots(40, rc.getTeam().opponent());
            for (RobotInfo robot : robots) {
                if (detectedEnemyDirection.equals(Direction.CENTER)) {
                    detectedEnemyDirection = Data.originPoint.directionTo(robot.getLocation());
                }
            }
        }

        System.out.println(detectedEnemyDirection);
        if (detectedEnemyDirection.equals(Direction.CENTER)) {
            spawnFarmers(rc);
            System.out.println("SPAWN FARMER");
        } else if (enemyBases.size() == 0 && possibleEnemyBases.size() == 0) {
            int position = 1;
            switch (detectedEnemyDirection) {
                case NORTH:
                    position = 0;
                    break;
                case EAST:
                    position = 1;
                    break;
                case SOUTH:
                    position = 2;
                    break;
                case WEST:
                    position = 3;
                    break;
                default:
                    break;
            }
            spawnDefenderPoliticians(rc, position);
            System.out.println("SPAWNDEFENDERPOLI");
        } else {
            System.out.println("SPAWN ORDER:" + spawnOrderCounter);
            switch (spawnOrder.get(spawnOrderCounter % spawnOrder.size())) {
                case SLANDERER:
                    System.out.println("SPAWN SLANDERER");
                    spawnFarmers(rc);
                    spawnOrderCounter++;
                    break;
                case MUCKRAKER:
                    System.out.println("SPAWN MUCKRAKER");
                    spawnMuckrakers(rc);
                    spawnOrderCounter++;
                    break;
                case POLITICIAN:
                    int position = 1;
                    switch (detectedEnemyDirection) {
                        case NORTH:
                            position = 0;
                            break;
                        case EAST:
                            position = 1;
                            break;
                        case SOUTH:
                            position = 2;
                            break;
                        case WEST:
                            position = 3;
                            break;
                        default:
                            break;
                    }
                    spawnDefenderPoliticians(rc, position);
                    System.out.println("SPAWNDEFENDERPOLI");
                    spawnOrderCounter++;
                    break;
                default:
                    break;
            }
            // if (poliCount < farmerCount) {
            //     int position = 1;
            //     switch (detectedEnemyDirection) {
            //         case NORTH:
            //             position = 0;
            //             break;
            //         case EAST:
            //             position = 1;
            //             break;
            //         case SOUTH:
            //             position = 2;
            //             break;
            //         case WEST:
            //             position = 3;
            //             break;
            //         default:
            //             break;
            //     }
            //     spawnDefenderPoliticians(rc, position);
            //     System.out.println("SPAWNDEFENDERPOLI");
            // } else {
            //     System.out.println("SPAWN SLANDERER");
            //     spawnFarmers(rc);
            // }
        }

    }

    public static void spawnFarmers(RobotController rc) throws GameActionException {
        Direction spawnLocation = openSpawnLocation(rc, RobotType.SLANDERER);
        int ecInfluence = rc.getInfluence();
        int farmerInfluence = 21;

        for (int i = 0; i < optimalFarmingInfluence.length; i++) {
            if (optimalFarmingInfluence[i] * 2 < ecInfluence) {
                farmerInfluence = optimalFarmingInfluence[i];
            }
        }

        if (farmerCount < farmerInitial) {
            farmerInfluence = 107;
        }

        System.out.println("FINF:" + farmerInfluence);
        if (rc.canBuildRobot(RobotType.SLANDERER, spawnLocation, farmerInfluence)) {
            System.out.println("BUILT FARMER");
            if (rc.canSetFlag(102)) {
                rc.setFlag(102);
            }
            rc.buildRobot(RobotType.SLANDERER, spawnLocation, farmerInfluence);
        }
        if (farmerCount < farmerInitial) {
            farmerCount++;
        }
    }

    //Spawns a circle of politicians to intercept enemy scouts
    public static void spawnDefenderPoliticians(RobotController rc, int position) throws GameActionException {
        Direction spawn = openSpawnLocation(rc, RobotType.POLITICIAN);
        int unitInfluence = Math.max(25, lastInfluenceGain + (int) (rc.getInfluence() / 20));
        String msg = "112" + position;
        if (rc.canBuildRobot(RobotType.POLITICIAN, spawn, unitInfluence)) {
            if (rc.canSetFlag(Integer.parseInt(msg))) {
                rc.setFlag(Integer.parseInt(msg));
            }
            rc.buildRobot(RobotType.POLITICIAN, spawn, unitInfluence);
        }
    }

    public static void spawnAttackingPoliticians(RobotController rc) throws GameActionException {
        Direction spawnLocation = openSpawnLocation(rc, RobotType.POLITICIAN);
        int unitInfluence = Math.max(25, lastInfluenceGain + (int) (rc.getInfluence() / 20));

        if (rc.canBuildRobot(RobotType.POLITICIAN, spawnLocation, unitInfluence)) {
            if (rc.canSetFlag(113)) {
                rc.setFlag(113);
            }
            rc.buildRobot(RobotType.POLITICIAN, spawnLocation, unitInfluence);
        }
    }

    public static void spawnMuckrakers(RobotController rc) throws GameActionException {
        Direction spawnLocation = openSpawnLocation(rc, RobotType.MUCKRAKER);
        int unitInfluence = 1;

        if (rc.canBuildRobot(RobotType.MUCKRAKER, spawnLocation, unitInfluence)) {
            rc.buildRobot(RobotType.MUCKRAKER, spawnLocation, unitInfluence);
        } else {
            if (spawnOrderCounter > 0) {
                spawnOrderCounter--;
            }
        }
    }

    //sends 4 muckrackers as scouts in the 4 cardinal directions
    public static void scoutPhase(RobotController rc) throws GameActionException {
        int dirIndex = scoutCount % 4;
        int influence = 1;
        Direction designatedDirection = Data.directions[dirIndex * 2];

        if (scoutCount < scoutLimit && rc.canBuildRobot(RobotType.MUCKRAKER, designatedDirection, influence)) {
            if (rc.canSetFlag(100)) {
                rc.setFlag(100);
            }

            rc.buildRobot(RobotType.MUCKRAKER, designatedDirection, influence);
            // System.out.println("Created Scout with " + influence + " influence");
            scoutCount++;
            if (rc.canSenseRadiusSquared(3)) {
                for (RobotInfo robot : rc.senseNearbyRobots(3, rc.getTeam())) {
                    if (!scoutIds.keySet().contains(robot.getID())) {
                        scoutIds.put(robot.getID(), designatedDirection);
                    }
                }
            }
        }

        if (scoutCount >= scoutLimit) {
            scoutingPhase = false;
        }
    }

    //logic for scout communication
    public static void listenForScoutMessages(RobotController rc) throws GameActionException {
        Object removeId = null;
        Object[] keys = scoutIds.keySet().toArray();

        for (Object key : keys) {
            if (rc.canGetFlag((int) key)) {
                int flag = rc.getFlag((int) key);
                if (flag != 0 && Integer.toString(flag).length() == 7) {
                    // System.out.println("id: " + key + " msg:" + flag);
                    String msg = Integer.toString(flag);
                    int[] coords = Communication.coordDecoder(msg);

                    //recieved enemy base coords
                    if (msg.charAt(0) == '2') {
                        MapLocation currentLocation = rc.getLocation();
                        coords[0] += currentLocation.x;
                        coords[1] += currentLocation.y;
                        // System.out.println("ENEMY BASE: " + coords[0] + "," + coords[1]);

                        enemyBases.add(new MapLocation(coords[0], coords[1]));
                    }
                    //recieved neutral base coords
                    else if (msg.charAt(0) == '6') {
                        MapLocation currentLocation = rc.getLocation();
                        coords[0] += currentLocation.x;
                        coords[1] += currentLocation.y;
                        // System.out.println("NEUTRAL BASE: " + coords[0] + "," + coords[1]
                        neutralBases.put(new MapLocation(coords[0], coords[1]), 501);
                    }
                    //recieved beacon from scout
                    else if (msg.charAt(0) == '3') {
                        scoutLastMessage.put((int) key, msg);
                    }
                    //recieved wall coords
                    //wall coord will only be added if it has not been discovered before

                    //scouts that hit the wall and change direction screw up the initial array of scouts and their direction
                    //after they hit a wall, only messages listened to will be for exact coords
                    else if (msg.charAt(0) == '4' && !waller.contains(key)) {
                        MapLocation currentLocation = rc.getLocation();
                        switch (scoutIds.get(key)) {
                            case NORTH:
                                if (mapBorders[0] == 0) {
                                    mapBorders[0] = currentLocation.y + coords[1];
                                    waller.add((int) key);
                                }
                                break;
                            case EAST:
                                if (mapBorders[1] == 0) {
                                    mapBorders[1] = currentLocation.x + coords[0];
                                    waller.add((int) key);
                                }
                                break;
                            case SOUTH:
                                if (mapBorders[2] == 0) {
                                    mapBorders[2] = currentLocation.y + coords[1];
                                    waller.add((int) key);
                                }

                                break;
                            case WEST:
                                if (mapBorders[3] == 0) {
                                    mapBorders[3] = currentLocation.x + coords[0];
                                    // System.out.println("x val : " + mapBorders[3]);
                                    // scoutIds.put((int) key, Direction.NORTH);
                                    waller.add((int) key);
                                }
                                break;
                            default:
                                break;
                        }

                        // if the ec has 3 wall coords, it can and will find the fourth
                        int zeroCount = 0;
                        for (int i = 0; i < mapBorders.length; i++) {
                            if (mapBorders[i] == 0) {
                                zeroCount++;
                            }
                        }
                        if (zeroCount == 1 && !mapComplete) {
                            // System.out.println("Calculating World Map...");
                            int missingIndex = 0;
                            for (int i = 0; i < mapBorders.length; i++) {
                                if (mapBorders[i] == 0) {
                                    missingIndex = i;
                                }
                            }
                            if (missingIndex % 2 == 0) { // North or South
                                int width = Math.abs(mapBorders[1] - mapBorders[3]);
                                if (missingIndex == 0) {
                                    mapBorders[missingIndex] = mapBorders[2] + width;
                                } else if (missingIndex == 2) {
                                    mapBorders[missingIndex] = mapBorders[0] - width;
                                }
                            } else { //East or West
                                int height = Math.abs(mapBorders[0] - mapBorders[2]);
                                if (missingIndex == 1) {
                                    mapBorders[missingIndex] = mapBorders[3] + height;
                                } else if (missingIndex == 3) {
                                    mapBorders[missingIndex] = mapBorders[1] - height;
                                }
                            }
                            mapComplete = true; //this variable is used later to determine if enemy coords can be used to calculate possible enemy base locations

                            //after the map is complete, the ec will check if there are any previously stored enemy coords that can be used to calculate possible enemy base locations
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
                                            possibleEnemyBases.add(
                                                    new MapLocation(mapBorders[1] - (currentLocation.x - mapBorders[3]),
                                                            currentLocation.y));
                                            break;
                                        case SOUTH:
                                            possibleEnemyBases.add(new MapLocation(currentLocation.x,
                                                    mapBorders[2] + (mapBorders[0] - currentLocation.y)));
                                            break;
                                        case WEST:
                                            possibleEnemyBases.add(
                                                    new MapLocation(mapBorders[3] + (mapBorders[1] - currentLocation.x),
                                                            currentLocation.y));
                                            break;

                                        default:
                                            break;
                                    }
                                    // System.out.println("Possible Enemy Base:" + possibleEnemyBases.toArray()[possibleEnemyBases.size() - 1].toString());
                                }
                            }
                        }
                    }
                }
            } else {
                //if ec could not get flag of scout, it means the scout has died
                //ec will get the scout's last known location and try to determine where the enemy ec is using it's direction and map coordinates
                //(possible because of guaranteed map symmetry)
                String lastMsg = scoutLastMessage.get(key);
                if (lastMsg != null && lastMsg.length() != 0) {
                    int[] coords = Communication.coordDecoder(lastMsg);
                    if (mapComplete && !waller.contains(key)) {
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
                        // System.out.println("Possible Enemy Base:" + possibleEnemyBases.toArray()[possibleEnemyBases.size() - 1].toString());
                    } else {
                        if (!waller.contains(key)) {
                            MapLocation baseLocation = rc.getLocation();
                            enemyCoords.put(scoutIds.get(key),
                                    new MapLocation(coords[0] + baseLocation.x, coords[1] + baseLocation.y));
                        }
                        // System.out.println("Enemy Coord:" + enemyCoords.get(baseKeys[0]).toString());
                    }
                }
                removeId = key;
            }
        }
        if (removeId != null) {
            scoutIds.remove(removeId);
        }
    }

    // calculates the influence gain between last round and this round
    public static void calculateInfluenceGain(RobotController rc) {
        if (rc.getRoundNum() - Data.initRound == 0) {
            lastInfluenceAmount = rc.getInfluence();
        } else {
            lastInfluenceGain = rc.getInfluence() - lastInfluenceAmount;
            lastInfluenceAmount = rc.getInfluence();
        }
    }

    // calculates the number of farmers in the field
    public static void calculateFarmers(RobotController rc) throws GameActionException {
        int removeId = 0;
        for (Integer id : farmerIds) {
            if (!rc.canGetFlag(id)) { // means farmer died
                removeId = id;
            } else {
                String flag = Integer.toString(rc.getFlag(id));
                if (!flag.equals("0")) {
                    removeId = id;
                }
            }
        }
        farmerIds.remove(removeId);
    }

    public static void calculatePoliticians(RobotController rc) throws GameActionException {
        int temp = 0;
        int temp2 = 0;
        if (rc.canSenseRadiusSquared(40)) {
            RobotInfo[] robots = rc.senseNearbyRobots(40, rc.getTeam());
            for (RobotInfo robot : robots) {
                if (robot.getType().equals(RobotType.POLITICIAN)) {
                    temp++;
                } else if (robot.getType().equals(RobotType.SLANDERER)) {
                    temp2++;
                }
            }
        }
        poliCount = temp;
        farmerCount = temp2;
    }

    //logic for bidding:
    //currently bids 3 between rounds 150 and 1000
    //after round 1000, the ec will bid 1/5 of its influence gain
    public static void calculateBid(RobotController rc) throws GameActionException {
        int round = rc.getRoundNum();
        boolean wonLastRound = false;
        //System.out.println(rc.getTeamVotes());
        if (rc.getTeamVotes() > lastVotes) {
            lastVotes++;
            wonLastRound = true;
        }
        System.out.println("Won Last Round:" + wonLastRound);
        // if (round > 300 && round < 750) {
        //     if (rc.canBid(3)) {
        //         rc.bid(3);
        //         System.out.println("Bid default");
        //     }
        // }
        if (round >= 750) {
            if (wonLastRound == false) {
                if (rc.canBid((int) (lastInfluenceGain * (3 / 4)))) {
                    rc.bid((int) (lastInfluenceGain * (3 / 4)));
                    System.out.println("Bid:" + (int) (lastInfluenceGain * (3 / 4)));
                }
            }
            if (rc.canBid(lastInfluenceGain / 3)) {
                rc.bid(lastInfluenceGain / 3);
                System.out.println("Bid:" + lastInfluenceGain / 3);
            }
        }
    }

    //returns an open spawn location around the ec
    public static Direction openSpawnLocation(RobotController rc, RobotType type) throws GameActionException {
        for (int i = 0; i < Data.directions.length; i++) {
            if (rc.canBuildRobot(type, Data.directions[i], 1)) {
                return Data.directions[i];
            }
        }
        return Direction.CENTER;
    }

    public static void init(RobotController rc) throws GameActionException {
        Data.originPoint = rc.getLocation();
        Data.initRound = rc.getRoundNum();
        spawnOrder.add(RobotType.POLITICIAN);
        spawnOrder.add(RobotType.POLITICIAN);
        spawnOrder.add(RobotType.POLITICIAN);
        spawnOrder.add(RobotType.SLANDERER);
        spawnOrder.add(RobotType.POLITICIAN);
        spawnOrder.add(RobotType.POLITICIAN);
        spawnOrder.add(RobotType.SLANDERER);
    }
}
