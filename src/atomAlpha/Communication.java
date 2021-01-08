package atomAlpha;

public class Communication {
    public static int coordEncoder(String type, int dx, int dy) {
        String outMsg = "";
        switch (type) {
            case "ENEMY":
                outMsg += "2";
                break;
            case "LIKELY":
                outMsg += "3";
                break;
            case "WALL":
                outMsg += "4";
                break;
            case "CORNER":
                outMsg += "5";
                break;
        }
        Boolean xNeg = dx < 0;
        Boolean yNeg = dy < 0;
        if (xNeg == true) {
            outMsg += "9";
            dx = Math.abs(dx);
        } else {
            outMsg += "8";
        }
        if (dx < 10) {
            outMsg += "0" + dx;
        } else {
            outMsg += dx;
        }
        if (yNeg == true) {
            outMsg += "9";
            dy = Math.abs(dy);
        } else {
            outMsg += 8;
        }
        if (dy < 10) {
            outMsg += "0" + dy;
        } else {
            outMsg += dy;
        }
        return Integer.parseInt(outMsg);
    }

    public static int[] coordDecoder(String msg) {
        int coords[] = new int[] { 1, 1 };
        if (msg.charAt(1) == '9') {
            coords[0] *= -1;
        }
        coords[0] *= Integer.parseInt(msg.substring(2, 4));
        if (msg.charAt(4) == '9') {
            coords[1] *= -1;
        }
        coords[1] *= Integer.parseInt(msg.substring(5));

        return coords;
    }
}
