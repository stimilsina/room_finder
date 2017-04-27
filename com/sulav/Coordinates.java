package com.sulav;

/**
 * Created by sulav on 4/8/17.
 * Simple class to store which rooms are present
 * on four directions. -1 if there are no rooms or rooms with invalid id
 */
public class Coordinates {
    private int east;
    private int west;
    private int north;
    private int south;

    public int getEast()
    {
        return east;
    }
    public int getWest()
    {
        return west;
    }
    public int getNorth()
    {
        return north;
    }
    public int getSouth()
    {
        return south;
    }

    /**
     *
     * @param str value representing id of rooms
     * @return integer value of room id.
     *         -1 if invalid or empty String
     */
    private int toInt(String str)
    {
        int val=-1;
        try {
            if (!"".equals(str))
                val = Integer.parseInt(str);
        }
        catch(Exception e) //ignoring exception
        {                   //neighbor with invalid room id = unreachable
        }
        return val;
    }

    /**
     * Constructor for instantiating coordinates
     * @param e east
     * @param w west
     * @param n north
     * @param s south
     */
    public Coordinates(String e,String w,String n, String s)
    {
        east=toInt(e);
        west=toInt(w);
        north=toInt(n);
        south=toInt(s);
    }
}
