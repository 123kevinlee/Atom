package atomV11;

import battlecode.common.*;

public class Data {
    public static int baseId = 0;
    public static MapLocation originPoint = new MapLocation(0, 0);
    public static int[] relOriginPoint = new int[2];
    public static int initRound = 0;

    public static Boolean wasSlanderer = false;
    public static Boolean wasAlly = false;

    static final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
            Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };
}
