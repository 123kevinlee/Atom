package atom;

import battlecode.common.*;
import battlecode.schema.SpawnedBodyTable;

import java.util.*;

public class EnlightenmentCenter {

    /*To Do 
        Send for neutral bases, send poli to attack bases, send mucks, bidding, make slanderers escape
    */
    public static int lastInfluenceAmount = 0;
    public static int lastInfluenceGain = 0;
    public static int lastVotes = 0;

    public static ArrayList<RobotType> spawnOrder = new ArrayList<RobotType>();
    public static int spawnOrderCounter = 0;
    public static int initialSetupCount = 0;

    public static Map<Integer, Direction> scoutIds = new HashMap<Integer, Direction>();
    public static Set<Integer> waller = new HashSet<Integer>(); // fricking waller direction is annoying so they can
                                                                // only contribute with absolute units -- nothing based
                                                                // on their direction
    public static Map<Integer, String> scoutLastMessage = new HashMap<Integer, String>();

    public static int[] optimalFarmingInfluence = new int[] { 21, 41, 63, 85, 107, 130, 154, 178, 203, 229, 255, 282,
            310, 341, 369, 400, 431, 463, 497, 533, 569, 605, 644, 683, 724, 767, 810, 855, 903, 949 };

    public static int[] mapBorders = new int[4]; // 0=NORTH 1=EAST 2=SOUTH 3=WEST
    public static boolean mapComplete = false;

    public static MapLocation danger = null;

    public static LinkedHashSet<MapLocation> enemyBases = new LinkedHashSet<MapLocation>();
    public static Map<Direction, MapLocation> enemyCoords = new TreeMap<Direction, MapLocation>();
    public static LinkedHashSet<MapLocation> possibleEnemyBases = new LinkedHashSet<MapLocation>();
    public static LinkedHashMap<MapLocation, Integer> neutralBases = new LinkedHashMap<MapLocation, Integer>();
    public static LinkedHashSet<MapLocation> alliedBases = new LinkedHashSet<MapLocation>();

    public static void run(RobotController rc) throws GameActionException {
        if (rc.canSenseRadiusSquared(-1)) {
            RobotInfo[] robots = rc.senseNearbyRobots();
            for (RobotInfo robot : robots) {
                if (robot.getTeam().equals(rc.getTeam().opponent())
                        && robot.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
                    enemyBases.add(robot.getLocation());
                    spawnTakeoverPolitician(rc, 150, enemyBases.iterator().next());
                } else if (robot.getTeam().equals(Team.NEUTRAL)) {
                    neutralBases.put(robot.getLocation(), robot.getInfluence());
                } else if (robot.getTeam().equals(rc.getTeam().opponent())) {
                    MapLocation robotLocation = robot.getLocation();
                    int relx = robotLocation.x % 128;
                    int rely = robotLocation.y % 128;
                    int flag = Communication.coordEncoder("WARN", relx, rely);
                    if (rc.canSetFlag(flag) && Integer.toString(rc.getFlag(rc.getID())).charAt(0) != '5') {
                        rc.setFlag(flag);
                    }
                }
            }
        }

        calculateInfluenceGain(rc); // calculates the influence gain between last round and this round
        if (rc.getTeamVotes() < 751) {
            calculateBid(rc); //calculates the amount to bid
        }

        if (scoutIds.size() > 0) {
            listenForScoutMessages(rc);
        }

        if (rc.getRoundNum() < 5) {
            if (rc.canBuildRobot(RobotType.SLANDERER, Direction.SOUTH, 130)) {
                rc.buildRobot(RobotType.SLANDERER, Direction.SOUTH, 130);
            }
        }
        //if (rc.getRoundNum() < 20) {
        switch (initialSetupCount) {
            case 0:
                spawnScout(rc, Direction.NORTH);
                break;
            case 1:
                spawnScout(rc, Direction.EAST);
                break;
            case 2:
                spawnScout(rc, Direction.WEST);
                break;
            case 3:
                spawnScout(rc, Direction.SOUTH);
                break;
            case 4:
                spawnScout(rc, Direction.NORTHEAST);
                break;
            case 5:
                spawnScout(rc, Direction.SOUTHWEST);
                break;
            case 6:
                spawnScout(rc, Direction.NORTHWEST);
                break;
            case 7:
                spawnScout(rc, Direction.SOUTHEAST);
                break;
            //}
        }

        //System.out.println("INFGAIN:" + lastInfluenceGain);
        Object[] neutralBaseKeys = neutralBases.keySet().toArray();
        for (Object key : neutralBaseKeys) {
            if (neutralBases.get(key) != 1000 && rc.getInfluence() - neutralBases.get(key) > 75) {
                spawnTakeoverPolitician(rc, neutralBases.get(key) + 12, (MapLocation) key);
                System.out.println("BIG BOI SPAWNED FOR" + key.toString());
            }
        }

        // if (rc.getRoundNum() > 500 && !spawnOrder.contains(RobotType.MUCKRAKER)) {
        //     spawnOrder.add(RobotType.MUCKRAKER);
        // }

        RobotType spawn = spawnOrder.get(spawnOrderCounter % spawnOrder.size());
        Direction spawnDir = openSpawnLocation(rc, RobotType.SLANDERER);
        switch (spawn) {
            case SLANDERER:
                int influence = 1;
                if (lastInfluenceGain / 2 > 14) {
                    influence = lastInfluenceGain / 2;
                }
                if (lastInfluenceGain <= 250)
                    spawnFarmer(rc, spawnDir);
                spawnTargetedMuckraker(rc, influence);
                break;
            case MUCKRAKER:
                influence = 1;
                if (lastInfluenceGain / 2 > 14) {
                    influence = lastInfluenceGain / 2;
                }
                spawnTargetedMuckraker(rc, influence);
                break;
            case POLITICIAN:
                influence = 14;
                if (lastInfluenceGain / 2 > 14) {
                    influence = lastInfluenceGain / 2;
                }
                if (rc.getRoundNum() > 500) {
                    int randNum = (int) (Math.random() * 5);
                    if (randNum == 0) {
                        spawnTargetedPolitician(rc, influence);
                    } else {
                        spawnPolitician(rc, influence);
                    }
                } else {
                    spawnPolitician(rc, influence);
                }
                break;
            default:
                break;
        }
        spawnOrderCounter++;
    }

    public static void spawnTargetedPolitician(RobotController rc, int influence) throws GameActionException {
        Direction spawnDir = openSpawnLocation(rc, RobotType.POLITICIAN);
        if (rc.canBuildRobot(RobotType.POLITICIAN, spawnDir, influence)) {
            MapLocation targetLocation = Data.originPoint;
            if (enemyBases.size() > 0) {
                targetLocation = enemyBases.iterator().next();
            } else if (possibleEnemyBases.size() > 0) {
                targetLocation = possibleEnemyBases.iterator().next();
            }
            int relx = targetLocation.x % 128;
            int rely = targetLocation.y % 128;
            int flag = Communication.coordEncoder("ENEMY", relx, rely);
            if (rc.canSetFlag(flag)) {
                rc.setFlag(flag);
            }
            rc.buildRobot(RobotType.POLITICIAN, spawnDir, influence);
        }
    }

    public static void spawnTakeoverPolitician(RobotController rc, int influence, MapLocation target)
            throws GameActionException {
        Direction spawnDir = openSpawnLocation(rc, RobotType.POLITICIAN);
        if (rc.canBuildRobot(RobotType.POLITICIAN, spawnDir, influence)) {
            int relx = target.x % 128;
            int rely = target.y % 128;
            int flag = Communication.coordEncoder("NEUTRAL", relx, rely);
            if (rc.canSetFlag(flag)) {
                rc.setFlag(flag);
            }
            rc.buildRobot(RobotType.POLITICIAN, spawnDir, influence);
            neutralBases.put(target, 1000);
        }
    }

    public static void spawnTargetedMuckraker(RobotController rc, int influence) throws GameActionException {
        Direction spawnDir = openSpawnLocation(rc, RobotType.MUCKRAKER);
        int unitInfluence = influence;
        MapLocation targetLocation = Data.originPoint;
        if (enemyBases.size() > 0) {
            targetLocation = enemyBases.iterator().next();
        } else if (possibleEnemyBases.size() > 0) {
            targetLocation = possibleEnemyBases.iterator().next();
        }
        if (rc.canBuildRobot(RobotType.MUCKRAKER, spawnDir, unitInfluence)
                && !targetLocation.equals(Data.originPoint)) {
            int relx = targetLocation.x % 128;
            int rely = targetLocation.y % 128;
            int flag = Communication.coordEncoder("ENEMY", relx, rely);
            if (rc.canSetFlag(flag)) {
                rc.setFlag(flag);
            }
            rc.buildRobot(RobotType.MUCKRAKER, spawnDir, unitInfluence);
        }
    }

    public static void spawnMuckraker(RobotController rc) throws GameActionException {
        Direction spawnDir = openSpawnLocation(rc, RobotType.MUCKRAKER);
        int unitInfluence = 1;
        if (rc.canBuildRobot(RobotType.MUCKRAKER, spawnDir, unitInfluence)) {
            if (rc.canSetFlag(0)) {
                rc.setFlag(0);
            }
            rc.buildRobot(RobotType.MUCKRAKER, spawnDir, unitInfluence);
        }
    }

    public static void spawnPolitician(RobotController rc, int influence) throws GameActionException {
        Direction spawnDir = openSpawnLocation(rc, RobotType.POLITICIAN);
        if (rc.canBuildRobot(RobotType.POLITICIAN, spawnDir, influence)) {
            if (rc.canSetFlag(0)) {
                rc.setFlag(0);
            }
            rc.buildRobot(RobotType.POLITICIAN, spawnDir, influence);
        } else {
            spawnTargetedMuckraker(rc, 1);
        }
    }

    public static void spawnScout(RobotController rc, Direction dir) throws GameActionException {
        if (rc.canBuildRobot(RobotType.MUCKRAKER, dir, 1)) {
            if (rc.canSetFlag(100)) {
                rc.setFlag(100);
                //System.out.println("SETFLAG100");
            }
            rc.buildRobot(RobotType.MUCKRAKER, dir, 1);
            if (rc.canSenseRadiusSquared(2)) {
                for (RobotInfo robot : rc.senseNearbyRobots(2, rc.getTeam())) {
                    if (!scoutIds.keySet().contains(robot.getID())) {
                        scoutIds.put(robot.getID(), dir);
                    }
                }
            }
            if (initialSetupCount < 8) {
                initialSetupCount++;
            }
        }
    }

    public static void spawnFarmer(RobotController rc, Direction dir) throws GameActionException {
        int optimalInfluence = 21;
        for (int i = 0; i < optimalFarmingInfluence.length; i++) {
            if (optimalFarmingInfluence[i] * 3 < rc.getInfluence()) {
                optimalInfluence = optimalFarmingInfluence[i];
            }
        }
        if (rc.canBuildRobot(RobotType.SLANDERER, dir, optimalInfluence)) {
            if (rc.canSetFlag(0)) {
                rc.setFlag(0);
            }
            rc.buildRobot(RobotType.SLANDERER, dir, optimalInfluence);
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
        // if (round > 200 && round < 500) {
        //     if (rc.canBid(3)) {
        //         rc.bid(3);
        //         System.out.println("Bid default");
        //     }
        // } else if (round >= 500) {
        if (round >= 300)
            if (wonLastRound == false) {
                if (rc.canBid((int) (lastInfluenceGain))) {
                    rc.bid((int) (lastInfluenceGain));
                    //System.out.println("Bid:" + (int) (lastInfluenceGain));
                }
            }
        if (rc.canBid(lastInfluenceGain / 3)) {
            rc.bid(lastInfluenceGain / 3);
            //System.out.println("Bid:" + lastInfluenceGain / 3);
        }
    }

    //logic for scout communication
    public static void listenForScoutMessages(RobotController rc) throws GameActionException {
        Object removeId = null;
        Object[] keys = scoutIds.keySet().toArray();

        for (Object key : keys) {
            if (rc.canGetFlag((int) key)) {
                int flag = rc.getFlag((int) key);
                if (flag != 0 && Integer.toString(flag).length() >= 7) {
                    //System.out.println("id: " + key + " msg:" + flag);
                    String msg = Integer.toString(flag);
                    if (msg.length() == 8) {
                        if (msg.charAt(0) == '1') {
                            int[] coords = Communication.relCoordDecoder(msg.substring(1));
                            int influence = Integer.parseInt(Character.toString(msg.charAt(1))) * 100;
                            int[] distance = Pathfinding.getDistance(Data.relOriginPoint, coords);
                            MapLocation neutralBase = Data.originPoint.translate(distance[0], distance[1]);
                            neutralBases.put(neutralBase, influence);
                            //System.out.println("NEUTRAL BASES:" + neutralBases.toString());
                        }
                    }
                    int[] coords = Communication.relCoordDecoder(msg);

                    //recieved enemy base coords
                    if (msg.charAt(0) == '2') {
                        int[] distance = Pathfinding.getDistance(Data.relOriginPoint, coords);
                        MapLocation enemyBase = Data.originPoint.translate(distance[0], distance[1]);
                        enemyBases.add(enemyBase);

                        //System.out.println("ENEMY BASES:" + enemyBases.toString());
                    }
                    //recieved neutral base coords

                    //recieved beacon from scout
                    else if (msg.charAt(0) == '3') {
                        scoutLastMessage.put((int) key, msg);
                    }
                    //recieved ally base coords
                    else if (msg.charAt(0) == '6') {
                        int[] distance = Pathfinding.getDistance(Data.relOriginPoint, coords);
                        MapLocation alliedBase = Data.originPoint.translate(distance[0], distance[1]);
                        alliedBases.add(alliedBase);
                        //System.out.println("ALLY BASES:" + alliedBases.toString());
                    }
                    //recieved warning
                    else if (msg.charAt(0) == '7') {
                        int[] distance = Pathfinding.getDistance(Data.relOriginPoint, coords);
                        if (danger == null) {
                            danger = Data.originPoint.translate(distance[0], distance[1]);
                        }
                    }
                    //recieved wall coords
                    //wall coord will only be added if it has not been discovered before

                    //scouts that hit the wall and change direction screw up the initial array of scouts and their direction
                    //after they hit a wall, only messages listened to will be for exact coords
                    else if (msg.charAt(0) == '4' && !waller.contains(key)) {
                        int[] distance = Pathfinding.getDistance(Data.relOriginPoint, coords);
                        MapLocation wall = Data.originPoint.translate(distance[0], distance[1]);
                        switch (scoutIds.get(key)) {
                            case NORTH:
                                if (mapBorders[0] == 0) {
                                    mapBorders[0] = wall.y;
                                    waller.add((int) key);
                                }
                                break;
                            case EAST:
                                if (mapBorders[1] == 0) {
                                    mapBorders[1] = wall.x;
                                    waller.add((int) key);
                                }
                                break;
                            case SOUTH:
                                if (mapBorders[2] == 0) {
                                    mapBorders[2] = wall.y;
                                    waller.add((int) key);
                                }
                                break;
                            case WEST:
                                if (mapBorders[3] == 0) {
                                    mapBorders[3] = wall.x;
                                    waller.add((int) key);
                                }
                                break;
                            default:
                                break;
                        }

                        //if the ec has 3 wall coords, it can and will find the fourth
                        int zeroCount = 0;
                        for (int i = 0; i < mapBorders.length; i++) {
                            if (mapBorders[i] == 0) {
                                zeroCount++;
                            }
                        }
                        if (zeroCount == 1 && !mapComplete) {
                            //System.out.println("Calculating World Map...");
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
                            MapLocation currentLocation = rc.getLocation();
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
                                        case NORTHEAST:
                                            possibleEnemyBases.add(
                                                    new MapLocation(mapBorders[1] - (currentLocation.x - mapBorders[3]),
                                                            mapBorders[0] - (currentLocation.y - mapBorders[2])));
                                            break;
                                        case SOUTHEAST:
                                            possibleEnemyBases.add(
                                                    new MapLocation(mapBorders[1] - (currentLocation.x - mapBorders[3]),
                                                            mapBorders[2] + (mapBorders[0] - currentLocation.y)));
                                            break;
                                        case SOUTHWEST:
                                            possibleEnemyBases.add(
                                                    new MapLocation(mapBorders[3] + (mapBorders[1] - currentLocation.x),
                                                            mapBorders[2] + (mapBorders[0] - currentLocation.y)));
                                            break;
                                        case NORTHWEST:
                                            possibleEnemyBases.add(
                                                    new MapLocation(mapBorders[3] + (mapBorders[1] - currentLocation.x),
                                                            mapBorders[0] - (currentLocation.y - mapBorders[2])));
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
                    int[] coords = Communication.relCoordDecoder(lastMsg);
                    int[] distance = Pathfinding.getDistance(Data.relOriginPoint, coords);
                    MapLocation enemyPosition = Data.originPoint.translate(distance[0], distance[1]);

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
                        //System.out.println("Possible Enemy Bases:" + possibleEnemyBases.toString());
                    } else {
                        if (!waller.contains(key)) {
                            enemyCoords.put(scoutIds.get(key), new MapLocation(enemyPosition.x, enemyPosition.y));
                        }
                        //System.out.println("ENEMY COORDS: " + enemyCoords.toString());
                    }
                }
                removeId = key;
            }
        }
        if (removeId != null) {
            scoutIds.remove(removeId);
        }
        //System.out.println(mapBorders[0] + " " + mapBorders[1] + " " + mapBorders[2] + " " + mapBorders[3]);
        //System.out.println(possibleEnemyBases.toString());
    }

    //returns an open spawn location around the ec
    public static Direction openSpawnLocation(RobotController rc, RobotType type) throws GameActionException {
        Direction[] directions = new Direction[] { Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST,
                Direction.NORTHEAST, Direction.SOUTHWEST, Direction.SOUTHEAST, Direction.NORTHWEST };
        for (int i = 0; i < directions.length; i++) {
            if (rc.canBuildRobot(type, directions[i], 1)) {
                return directions[i];
            }
        }
        return Direction.CENTER;
    }

    public static void init(RobotController rc) throws GameActionException {
        Data.originPoint = rc.getLocation();
        Data.initRound = rc.getRoundNum();

        Data.relOriginPoint[0] = Data.originPoint.x % 128;
        Data.relOriginPoint[1] = Data.originPoint.y % 128;

        spawnOrder.add(RobotType.POLITICIAN);
        spawnOrder.add(RobotType.POLITICIAN);
        spawnOrder.add(RobotType.SLANDERER);
        spawnOrder.add(RobotType.POLITICIAN);
        spawnOrder.add(RobotType.POLITICIAN);
        spawnOrder.add(RobotType.SLANDERER);

        //spawnOrder.add(RobotType.MUCKRAKER);
    }
}
