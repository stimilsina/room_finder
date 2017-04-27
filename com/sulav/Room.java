package com.sulav;
import java.util.List;
/**
 * Stores a room as an object
 * Created by sulav on 4/8/17.
 */
public class Room {
    private int id;
    private String name;
    private Coordinates access; //direction
    private List<String> objects;

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public Coordinates getAccess()
    {
        return access;
    }

    public List<String> getObjects()
    {
        return objects;
    }

    /**
     * constructor for instantiating a room
     * @param id
     * @param name
     * @param access coordinates that tell what is there on the four sides
     * @param objects objects in this room
     */
    public Room(int id, String name, Coordinates access,List<String> objects)
    {
        this.id=id;
        this.name=name;
        this.access=access;
        this.objects=objects;
    }
}
