package atomAlphaV12;

import battlecode.common.*;
import java.util.*;

public class EnlightenmentCenter {

    public static int lastInfluenceAmount = 0;
    public static int lastInfluenceGain = 0;
    public static int lastVotes = 0;

    public static ArrayList<RobotType> spawnOrder = new ArrayList<RobotType>();
    public static int spawnOrderCounter = 0;

    public static boolean scoutingPhase = true;
    public static boolean setGuard = true;
    public static boolean guardsFull = false;
    public static boolean firstFarmers = true;

    public static int scoutCount = 0;
    public static int scoutLimit = 4;
    public static Map<Integer, Direction> scoutIds = new HashMap<Integer, Direction>();
    public static Set<Integer> waller = new HashSet<Integer>(); // fricking waller direction is annoying so they can
                                                                // only contribute with absolute units -- nothing based
                                                                // on their direction
    public static Map<Integer, String> scoutLastMessage = new HashMap<Integer, String>();

    public static int guardCount = 0;
    public static int farmerLimit = 5;
    public static int farmerCount = 0;
    public static Set<Integer> farmerIds = new HashSet<Integer>();

    public static LinkedHashMap<MapLocation, Boolean> muckrakerWall = new LinkedHashMap<MapLocation, Boolean>();
    public static int lastWallerSpawn = 0;
    public static int muckrakerWallspawned = 0;

    public static int scatterPoliCounter = 0;

    public static int[] mapBorders = new int[4]; // 0=NORTH 1=EAST 2=SOUTH 3=WEST
    public static boolean mapComplete = false;

    public static LinkedHashSet<MapLocation> enemyBases = new LinkedHashSet<MapLocation>();
    public static Map<Direction, MapLocation> enemyCoords = new TreeMap<Direction, MapLocation>();
    public static LinkedHashSet<MapLocation> possibleEnemyBases = new LinkedHashSet<MapLocation>();
    public static LinkedHashMap<MapLocation, Integer> neutralBases = new LinkedHashMap<MapLocation, Integer>();

    public static void run(RobotController rc) throws GameActionException {
        calculateInfluenceGain(rc); // calculates the influence gain between last round and this round
        calculateFarmers(rc); // calculates the amount of farmers in the field

        // Panic Bid: logic to uses remaining influence to bid when surrounded by enemies
        if (rc.senseNearbyRobots(2, rc.getTeam().opponent()).length == 12) {
            if (rc.canBid(rc.getInfluence() / 10)) {
                rc.bid(rc.getInfluence() / 10);
            }
        }

        //checks if scouts have returned any information: 
        //allows for things to happen in the time after scouts and guards have been spawned
        //but no map information has been gathered
        int mapInitX = 0;
        int mapInitY = 0;
        for (int i = 0; i < mapBorders.length; i++) {
            if (mapBorders[i] != 0) {
                if (i % 2 == 0) {
                    mapInitY++;
                } else {
                    mapInitX++;
                }
            }
        }

        if (scoutingPhase) {
            scoutPhase(rc);
        } else if (firstFarmers == true && mapInitX > 0 && mapInitY > 0) {
            spawnFarmers(rc);
        }
        //Time between scouts finding map coords and farmers being sent out
        else if (mapInitX == 0 || mapInitY == 0) {
            scatterPoliticians(rc);
            //buildWall(rc);
        } else if (enemyBases.size() > 0) {
            hasEnemyBaseCoords(rc);
        } else if (enemyBases.size() == 0 && possibleEnemyBases.size() > 0) {
            hasPossibleEnemyBaseCoords(rc);
        }
        // else if (enemyBases.size() == 0 && possibleEnemyBases.size() == 0 && enemyCoords.size() > 0) {
        //     //do later
        // } 
        else {
            //hasNoInfo(rc);
        }
        if (scoutIds.size() > 0) {
            listenForScoutMessages(rc);
        }
        calculateBid(rc);
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

    //logic for bidding:
    //currently bids 3 between rounds 150 and 1000
    //after round 1000, the ec will bid 1/5 of its influence gain
    public static void calculateBid(RobotController rc) throws GameActionException {
        if (rc.getRoundNum() > 150 && rc.getRoundNum() < 1000) {
            if (rc.canBid(3)) {
                rc.bid(3);
            }
        } else {
            if (rc.canBid(lastInfluenceGain / 5)) {
                rc.bid(lastInfluenceGain / 5);
                // System.out.println("Bid:" + lastInfluenceGain / 4);
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

        if (scoutCount >= scoutLimit && !guardsFull) {
            scoutingPhase = false;
            setGuard = true;
        }
    }

    //logic for spawning farmers
    public static void spawnFarmers(RobotController rc) throws GameActionException {
        int farmerInfluence = 10;

        if (farmerLimit > 5) {
            farmerInfluence = Math.min((int) (rc.getInfluence() / 2), 949);
        }

        MapLocation Base = rc.getLocation();
        Direction safeDir = Direction.CENTER;

        // stores a location within the mapBorders array, which will store the closest wall to the EC
        int arrayLocX = -1;
        int arrayLocY = -1;

        int cornerCoordX = 0;
        int cornerCoordY = 0;

        int bases0X = Math.abs(Base.x);
        int bases0Y = Math.abs(Base.y);

        if (mapBorders[0] == 0 || mapBorders[2] == 0) {
            for (int i = 0; i < mapBorders.length; i += 2) {
                if (Math.abs(Base.y - mapBorders[i]) != bases0Y) {
                    arrayLocY = i;
                }
            }
        } else {
            // if the border values are all defined, this code will find the true closest border
            int minBorder = Math.abs(Base.y - mapBorders[0]);
            arrayLocY = 0;

            for (int i = 0; i < mapBorders.length; i += 2) {
                if (Math.abs(Base.y - mapBorders[i]) < minBorder) {
                    arrayLocY = i;
                }
            }
        }

        // System.out.println(arrayLocY);

        // Obtaining the closest border value for x even if adjacent borders are unidentified
        if (mapBorders[1] == 0 || mapBorders[3] == 0) {
            for (int i = 1; i < mapBorders.length; i += 2) {
                if (Math.abs(Base.x - mapBorders[i]) != bases0X) {
                    arrayLocX = i;
                }
            }
        } else {
            // if the border values are all defined, this code will find the true closest border
            int minBorder = Math.abs(Base.x - mapBorders[1]);
            arrayLocX = 1;

            for (int i = 1; i < mapBorders.length; i += 2) {
                if (Math.abs(Base.x - mapBorders[i]) < minBorder) {
                    arrayLocX = i;
                }
            }
        }

        // If the location in the mapBorders array was actually obtained and extant
        if (arrayLocY != -1 && arrayLocX != -1) {
            cornerCoordY = mapBorders[arrayLocY];
            cornerCoordX = mapBorders[arrayLocX];
            // System.out.println("The closest corner is " + mapBorders[arrayLocY] + " , " + mapBorders[arrayLocX]);
            MapLocation safeCorner = new MapLocation(cornerCoordX, cornerCoordY);
            safeDir = Base.directionTo(safeCorner);

            // System.out.println(safeCorner.toString());
            // ("The safe direction is: " + safeDir);
            MapLocation currentLocation = rc.getLocation();
            int dx = safeCorner.x - currentLocation.x;
            int dy = safeCorner.y - currentLocation.y;
            // System.out.println(dx);
            // System.out.println(dy);

            if (scoutCount > 3 && farmerCount <= farmerLimit && safeDir != Direction.CENTER
                    && rc.canBuildRobot(RobotType.SLANDERER, safeDir, farmerInfluence)) {
                if (rc.canSetFlag(Communication.coordEncoder("CORNER", dx, dy))) {
                    rc.setFlag(Communication.coordEncoder("CORNER", dx, dy));
                }

                rc.buildRobot(RobotType.SLANDERER, safeDir, farmerInfluence);
                // System.out.println("Created Farmer with " + farmerInfluence + " influence in the " + safeDir + " Direction" + ", dx : " + dx + ", dy: " + dy);
                farmerCount++;
                if (rc.canSenseRadiusSquared(3)) {
                    for (RobotInfo robot : rc.senseNearbyRobots(3, rc.getTeam())) {
                        farmerIds.add(robot.getID());
                    }
                }
            }
        }
        if (farmerCount == farmerLimit) {
            firstFarmers = false;
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

    //spawns 4 politicans in 4 corners around the ec
    public static void createDefensePhase(RobotController rc) throws GameActionException {
        int influence = 12;
        int dirIndex = guardCount % 4;
        if (rc.canSetFlag(111)
                && rc.canBuildRobot(RobotType.POLITICIAN, Data.directions[dirIndex * 2 + 1], influence)) {
            rc.setFlag(111); // defender politician
            rc.buildRobot(RobotType.POLITICIAN, Data.directions[dirIndex * 2 + 1], influence);
            guardCount++;
        }
        if (guardCount > 3) {
            setGuard = false;
            guardsFull = true;
        }
    }

    //Spawns a circle of politicians to intercept enemy scouts
    public static void scatterPoliticians(RobotController rc) throws GameActionException {
        Direction spawn = openSpawnLocation(rc, RobotType.POLITICIAN);
        String msg = "112" + Integer.toString(scatterPoliCounter % 4);
        if (rc.canBuildRobot(RobotType.POLITICIAN, spawn, 12)) {
            if (rc.canSetFlag(Integer.parseInt(msg))) {
                rc.setFlag(Integer.parseInt(msg));
                scatterPoliCounter++;
            }
            rc.buildRobot(RobotType.POLITICIAN, spawn, 12);
        }
    }

    public static void buildWall(RobotController rc) throws GameActionException {
        if (muckrakerWall.containsValue(false) && rc.getRoundNum() - lastWallerSpawn > 10) {
            // add beacon
            // System.out.println(muckrakerWall.toString());
            int fillLocation = -1;
            Object[] keys = muckrakerWall.keySet().toArray();
            if (rc.canSenseRadiusSquared(40)) {
                for (int i = 0; i < keys.length; i++) {
                    MapLocation key = (MapLocation) keys[i];
                    // Boolean occupied = rc.isLocationOccupied(key);
                    // muckrakerWall.put(key, occupied);
                    if (muckrakerWall.get(key) == false) {
                        fillLocation = i;
                    }
                }
            }
            if (fillLocation != -1) {
                Direction spawnDir = openSpawnLocation(rc, RobotType.MUCKRAKER);
                if (rc.canBuildRobot(RobotType.MUCKRAKER, spawnDir, 1)) {
                    // System.out.println("Target:" + (MapLocation) keys[fillLocation]);
                    int dx = ((MapLocation) keys[fillLocation]).x - rc.getLocation().x;
                    int dy = ((MapLocation) keys[fillLocation]).y - rc.getLocation().y;
                    int flag = Communication.coordEncoder("WALL", dx, dy);
                    if (rc.canSetFlag(flag)) { // wall defender
                        rc.setFlag(flag);
                    }
                    rc.buildRobot(RobotType.MUCKRAKER, spawnDir, 1);
                    muckrakerWall.put((MapLocation) (keys[fillLocation]), true);
                    // make waller wait
                    muckrakerWallspawned++;
                    if (muckrakerWallspawned % 8 == 0) {
                        lastWallerSpawn = rc.getRoundNum();
                    }
                }
            }
        }
    }

    //logic for attacking if the ec has definite enemy ec coords
    public static void hasEnemyBaseCoords(RobotController rc) throws GameActionException {
        // if (neutralBases.size() > 0) {
        // spawnOrder.add(RobotType.POLITICIAN);         //adding politician to spawn order if there is a neutral base
        // }
        switch (spawnOrder.get(spawnOrderCounter % spawnOrder.size())) {
            case POLITICIAN:
                Direction spawnDir = openSpawnLocation(rc, RobotType.POLITICIAN);
                int currentInfluence = rc.getInfluence();
                if (currentInfluence / 5 > 11) {
                    int unitInfluence = currentInfluence / 5;
                    if (rc.canBuildRobot(RobotType.POLITICIAN, spawnDir, unitInfluence)) {
                        int dx = enemyBases.iterator().next().x - rc.getLocation().x;
                        int dy = enemyBases.iterator().next().y - rc.getLocation().y;
                        int flag = Communication.coordEncoder("ENEMY", dx, dy);
                        /* if (spawnOrder.size() > 4 && spawnOrder.size() % 5 == 4) {
                            Object[] keys = neutralBases.keySet().toArray();
                            MapLocation baseLocation = (MapLocation) keys[0];
                            dx = baseLocation.x - rc.getLocation().x;
                            dy = baseLocation.y - rc.getLocation().y;
                            flag = Communication.coordEncoder("ENEMY", dx, dy);
                            if (unitInfluence - neutralBases.get(keys[0]) > 0) {
                                unitInfluence = neutralBases.get(keys[0]);                          //future neutral base logic
                            }
                            int remaining = neutralBases.get(keys[0]) - unitInfluence;
                            if (remaining == 0) {
                                spawnOrder.remove(spawnOrder.size() - 1);
                            }
                            neutralBases.put((MapLocation) keys[0], remaining);
                        } */
                        if (rc.canSetFlag(flag)) {
                            rc.setFlag(flag);
                        }
                        rc.buildRobot(RobotType.POLITICIAN, spawnDir, unitInfluence);
                        spawnOrderCounter++;
                    }
                } else {
                    spawnDir = openSpawnLocation(rc, RobotType.MUCKRAKER);
                    int unitInfluence = 1;
                    if (rc.canBuildRobot(RobotType.MUCKRAKER, spawnDir, unitInfluence)) {
                        int dx = enemyBases.iterator().next().x - rc.getLocation().x;
                        int dy = enemyBases.iterator().next().y - rc.getLocation().y;
                        int flag = Communication.coordEncoder("ENEMY", dx, dy);
                        if (rc.canSetFlag(flag)) {
                            rc.setFlag(flag);
                        }
                        rc.buildRobot(RobotType.MUCKRAKER, spawnDir, unitInfluence);
                        spawnOrderCounter++;
                    }
                }
                break;
            case MUCKRAKER:
                spawnDir = openSpawnLocation(rc, RobotType.MUCKRAKER);
                int unitInfluence = 1;
                if (rc.canBuildRobot(RobotType.MUCKRAKER, spawnDir, unitInfluence)) {
                    int dx = enemyBases.iterator().next().x - rc.getLocation().x;
                    int dy = enemyBases.iterator().next().y - rc.getLocation().y;
                    int flag = Communication.coordEncoder("ENEMY", dx, dy);
                    if (rc.canSetFlag(flag)) {
                        rc.setFlag(flag);
                    }
                    rc.buildRobot(RobotType.MUCKRAKER, spawnDir, unitInfluence);
                    spawnOrderCounter++;
                }
                break;
            case SLANDERER:
                farmerLimit++;
                firstFarmers = true;
                spawnOrderCounter++;
                break;
            default:
                break;
        }
    }

    //logic for attack if the ec has possible enemy ec coords
    public static void hasPossibleEnemyBaseCoords(RobotController rc) throws GameActionException {
        // if (neutralBases.size() > 0) {
        // spawnOrder.add(RobotType.POLITICIAN);  //adds politician to spawn order if there is a neutral ec
        // }
        switch (spawnOrder.get(spawnOrderCounter % spawnOrder.size())) {
            case POLITICIAN:
                Direction spawnDir = openSpawnLocation(rc, RobotType.POLITICIAN);
                int currentInfluence = rc.getInfluence();
                if (currentInfluence / 5 > 11) {
                    int unitInfluence = currentInfluence / 5;
                    if (rc.canBuildRobot(RobotType.POLITICIAN, spawnDir, unitInfluence)) {
                        int dx = possibleEnemyBases.iterator().next().x - rc.getLocation().x;
                        int dy = possibleEnemyBases.iterator().next().y - rc.getLocation().y;
                        int flag = Communication.coordEncoder("ENEMY", dx, dy);
                        /* if (spawnOrder.size() > 4 && spawnOrder.size() % 5 == 4) { 
                            Object[] keys = neutralBases.keySet().toArray();
                            MapLocation baseLocation = (MapLocation) keys[0];
                            dx = baseLocation.x - rc.getLocation().x;
                            dy = baseLocation.y - rc.getLocation().y;
                            flag = Communication.coordEncoder("ENEMY", dx, dy);
                            if (unitInfluence - neutralBases.get(keys[0]) > 0) {
                                unitInfluence = neutralBases.get(keys[0]);                     //future logic for neutral bases
                            }
                            int remaining = neutralBases.get(keys[0]) - unitInfluence;
                            if (remaining == 0) {
                                spawnOrder.remove(spawnOrder.size() - 1);
                            }
                            neutralBases.put((MapLocation) keys[0], remaining);
                        } */
                        if (rc.canSetFlag(flag)) {
                            rc.setFlag(flag);
                        }
                        rc.buildRobot(RobotType.POLITICIAN, spawnDir, unitInfluence);
                        spawnOrderCounter++;
                    }
                } else {
                    spawnDir = openSpawnLocation(rc, RobotType.MUCKRAKER);
                    int unitInfluence = 1;
                    if (rc.canBuildRobot(RobotType.MUCKRAKER, spawnDir, unitInfluence)) {
                        int dx = possibleEnemyBases.iterator().next().x - rc.getLocation().x;
                        int dy = possibleEnemyBases.iterator().next().y - rc.getLocation().y;
                        int flag = Communication.coordEncoder("ENEMY", dx, dy);
                        if (rc.canSetFlag(flag)) {
                            rc.setFlag(flag);
                        }
                        rc.buildRobot(RobotType.MUCKRAKER, spawnDir, unitInfluence);
                        spawnOrderCounter++;
                    }
                }
                break;
            case MUCKRAKER:
                spawnDir = openSpawnLocation(rc, RobotType.MUCKRAKER);
                int unitInfluence = 1;
                if (rc.canBuildRobot(RobotType.MUCKRAKER, spawnDir, unitInfluence)) {
                    int dx = possibleEnemyBases.iterator().next().x - rc.getLocation().x;
                    int dy = possibleEnemyBases.iterator().next().y - rc.getLocation().y;
                    int flag = Communication.coordEncoder("ENEMY", dx, dy);
                    if (rc.canSetFlag(flag)) {
                        rc.setFlag(flag);
                    }
                    rc.buildRobot(RobotType.MUCKRAKER, spawnDir, unitInfluence);
                    spawnOrderCounter++;
                }
                break;
            case SLANDERER:
                farmerLimit++;
                firstFarmers = true;
                spawnOrderCounter++;
                break;
            default:
                break;
        }
    }

    public static void hasEnemyUnitCoords(RobotController rc) throws GameActionException {
        //write later
    }

    //logic if the ec has no information
    public static void hasNoInfo(RobotController rc) throws GameActionException {
        scoutLimit += 4;
        scoutingPhase = true;
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

        //MapLocation origin = Data.originPoint;
        // muckrakerWall.put(origin.translate(0, 3), false);
        // muckrakerWall.put(origin.translate(1, 3), false);
        // muckrakerWall.put(origin.translate(2, 3), false);
        // muckrakerWall.put(origin.translate(3, 3), false);
        // muckrakerWall.put(origin.translate(3, 2), false);
        // muckrakerWall.put(origin.translate(3, 1), false);
        // muckrakerWall.put(origin.translate(3, 0), false);
        // muckrakerWall.put(origin.translate(3, -1), false);
        // muckrakerWall.put(origin.translate(3, -2), false);
        // muckrakerWall.put(origin.translate(3, -3), false);

        // muckrakerWall.put(origin.translate(2, -3), false);
        // muckrakerWall.put(origin.translate(1, -3), false);
        // muckrakerWall.put(origin.translate(0, -3), false);
        // muckrakerWall.put(origin.translate(-1, -3), false);
        // muckrakerWall.put(origin.translate(-2, -3), false);
        // muckrakerWall.put(origin.translate(-3, -3), false);
        // muckrakerWall.put(origin.translate(-3, -2), false);
        // muckrakerWall.put(origin.translate(-3, -1), false);
        // muckrakerWall.put(origin.translate(-3, 0), false);
        // muckrakerWall.put(origin.translate(-3, 1), false);
        // muckrakerWall.put(origin.translate(-3, 2), false);
        // muckrakerWall.put(origin.translate(-3, 3), false);
        // muckrakerWall.put(origin.translate(-2, 3), false);
        // muckrakerWall.put(origin.translate(-1, 3), false);

        spawnOrder.add(RobotType.MUCKRAKER);
        spawnOrder.add(RobotType.POLITICIAN);
        spawnOrder.add(RobotType.MUCKRAKER);
        spawnOrder.add(RobotType.POLITICIAN);
        spawnOrder.add(RobotType.MUCKRAKER);
        spawnOrder.add(RobotType.POLITICIAN);
        spawnOrder.add(RobotType.POLITICIAN);
        //spawnOrder.add(RobotType.POLITICIAN);
        spawnOrder.add(RobotType.SLANDERER);
    }
}
