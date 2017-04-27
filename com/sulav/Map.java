package com.sulav;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * Map is an object that can be generated from the xml file with rooms
 *
 * Created by sulav on 4/8/17.
 */
public class Map{
    //Assumption: room index always begins from 1 and all rooms are
    //            sorted on ascending order of room id!
    // So,ith room is present at (i-1)th position of rooms.
    private List<Room> rooms;

    /**
     * validating the rooms that were parsed while passing in the constructor
     * @return
     */
    public boolean isValid()
    {
        if (rooms.size()==0)
        {
            System.out.print("No rooms could be allocated");
            return false;
        }
        for(int i=0;i<rooms.size();i++)
        {
            if (rooms.get(i).getId()!=i+1)
            {
                System.out.println("Invalid id for room:"+rooms.get(i-1).getName());
                System.out.println("Invalid room ids. Rooms ids should begin from 1 in continuous and ascending order");
                return false;
            }
            if("".equals(rooms.get(i).getName()))
            {
                System.out.println("Room name cannot be empty String! error in room "+i+1);
                return false;
            }
            int []dir = new int[4];
            dir[0]=rooms.get(i).getAccess().getEast();
            dir[1]=rooms.get(i).getAccess().getWest();
            dir[2]=rooms.get(i).getAccess().getNorth();
            dir[3]=rooms.get(i).getAccess().getSouth();
            for(int val:dir)
            {
                // -1 means no connection, we have no room 0. counting starts from 1
                // room cannot have door to itself and is uppper bounded by room with maximum room number
                if (val < -1 || val == 0 || val == i + 1 || val > rooms.size()) //offset of 1 in indexing
                {
                    System.out.println("Invalid room connection for room number:"+i+1);
                    return false;
                }
            }
            for(String obj: rooms.get(i).getObjects())
            {
                if ("".equals(obj))
                {
                    System.out.println("Object name cannot be empty");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * This matrix is used during DFS to find connections between rooms
     * and also to avoid using the same door from same direction twice
     * @return a Matrix[total_rooms][4] representing what are in
     * in the four sides of the room in the order east,west,north,south,
     * Empty is represnted by -1
     */
    private int[][] generateMatrix()
    {
        int[][] matrix=new int[rooms.size()][4];
        for(int i=0;i<rooms.size();i++)
        {
            matrix[i][0]=rooms.get(i).getAccess().getEast();
            matrix[i][1]=rooms.get(i).getAccess().getWest();
            matrix[i][2]=rooms.get(i).getAccess().getNorth();
            matrix[i][3]=rooms.get(i).getAccess().getSouth();
        }
        return matrix;
    }

    /**
     *
     * @param doc XML document
     * @param id id of the room
     * @param objects objects present in the room
     * @return A room node in the XML path with objects found at
     * that location as it's children nodes
     */
    private Node getRoom(Document doc, int id, List<String>objects)
    {
        Element room=doc.createElement("room");
        room.setAttribute("id", String.valueOf(id));
        room.setAttribute("name",rooms.get(id-1).getName());
        for(String obj:objects)
        {
            Element object=doc.createElement("object");
            object.setAttribute("name",obj);
            room.appendChild(object);
        }
        return room;
    }

    /**
     *
     * @param path list of id's of rooms visited in order
     * @param objects list of objects that were collected
     * @param outputFileName name of the output xml file
     */
    private void printPathXML(List<Integer> path, List<String> objects,String outputFileName)
    {
        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("route");
            doc.appendChild(rootElement);
            //stores list of objects that were found in a particular room
            List<String>appendObj=new ArrayList<String>();
            for (int node : path)
            {
                //stores list of objects that are not found yet
                List<String>complement=new ArrayList<String>();
                for(String obj:objects)
                {
                    if (rooms.get(node - 1).getObjects().contains(obj))
                    {
                        appendObj.add(obj); //object found in this room
                    }
                    else
                        complement.add(obj); //object not found. should be searched in other rooms later
                }
                //adding room with objects to root
                rootElement.appendChild((getRoom(doc, node, appendObj)));
                //found objects should not be looked for any further
                objects=complement;
                appendObj.clear();
            }
            //writing into the output XML file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(outputFileName));
            transformer.transform(source,result);
        }
        catch(Exception e)
        {
            System.out.println("unable to create output XML file successfully:"+outputFileName);
            e.printStackTrace();
        }
    }

    /**
     * The Complexity=O(All possible connections(doors)*Total Object to be Searched). This is a directed
     * graph problem. So, door is 2 way. A door cannot be visited more than once in the same direction. So it
     * is bound by doors. Also, in item once found is not searched again in next runs.
     * Recursive function for DFS search. Path is written in XML if a path is found collecting all the objects we
     * were searching for.
     * Omits printing if path is not found
     * @param from starting room id
     * @param objects list of objects to be collected
     * @param doors Matrix with list of connections on all the four sides for each room
     * @param traceDFS Saves the path as we move along
     * @param head true means that this is the recursion head and printing is done if all objects were collected
     *             successfully
     * @param outputFileName name of output file
     * @return true if a path is found
     *         false if path is not found
     */
    private boolean searchDFS(int from, List<String> objects, int[][]doors,List<Integer>traceDFS, boolean head, String outputFileName)
    {
        //no connection present
        if (from==-1)
            return false;
        System.out.println("DEBUG::visiting Room "+from);
        //stores a list of objects that are not found
        List<String>newObjects=new ArrayList<>();
        for(int i=0;i<objects.size();i++)
        {
            if (!rooms.get(from - 1).getObjects().contains(objects.get(i)))
                newObjects.add(objects.get(i));
            else
                System.out.println("DEBUG::found "+objects.get(i)+" in"+ " Room "+from);
        }
        //adding to path
        traceDFS.add(from);
        //everything is found we are done.
        if (newObjects.size()==0)
            return true;
        else
        {
            for (int i = 0; i < 4; i++) {
                //room id is in position id-1 in the ArrayList of room
                int val=doors[from-1][i];
                doors[from-1][i]=-1; //this path cannot be revisited. BLOCKING the path
                //if path exists that leads to another room, exploring it.
                if (val!=-1 && searchDFS(val, newObjects,doors,traceDFS,false,outputFileName)==true)
                {
                    if (head) //head of recursion. we can print into the XML now.
                    {
                        printPathXML(traceDFS,objects,outputFileName);
                    }
                    //exiting once path is found
                    return true;
                }
            }
        }
        //path could not be found
        return false;
    }

    /**
     *
     * @param Objects parsed config file. see Main.java.
     * @param outputFileName
     */
    public void searchPath(List<String>Objects, String outputFileName)
    {
        for (String Obj:Objects)
        {
            if ("".equals(Obj))
            {
                System.out.println("One or more objects to be searched for is empty string \nQuitting...");
                System.exit(-2);
            }
        }
        int[][]doors=generateMatrix(); //storing all connection between rooms
        List<Integer> traceResult= new ArrayList<Integer>(); //for storing path
        int id=Integer.parseInt(Objects.get(0)); //extracting id from parsed document
        if (id<1 || id >rooms.size()) //validating id.
        {
            System.out.println("Invalid starting room supplied: "+Objects.get(0));
            System.out.println("Quitting!");
            System.exit(-3);
        }

        Objects.remove(0); //removing id
        if (!searchDFS(id ,Objects,doors,traceResult,true,outputFileName))
            System.out.println("No path found such that all objects would be collected. So, XML has not been generated.");
        else
            System.out.println("Path has been saved in "+outputFileName);
    }

    /**
     * parses the XML file into Room object and adds to the list of room
     * Also Validates at the end
     * @param filename XML file with the map
     */
    public Map(String filename)
    {
        //initializing....
        rooms=new ArrayList<Room>();
        try
        {
            File inputFile = new File(filename);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList roomList = doc.getElementsByTagName("room");
            //For each room storing its properties as objects and adding to ArrayList of rooms
            for(int i=0;i<roomList.getLength();i++)
            {
                Node nNode=roomList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element roomElement = (Element) nNode;
                    int id=Integer.parseInt(roomElement.getAttribute("id"));
                    String name=roomElement.getAttribute("name");
                    String east=roomElement.getAttribute("east");
                    String west=roomElement.getAttribute( "west");
                    String north=roomElement.getAttribute("north");
                    String south=roomElement.getAttribute("south");
                    List<String>Objects=new ArrayList<String>();
                    NodeList ObjList=roomElement.getElementsByTagName("object");
                    //storing objects in each room
                    for(int j=0;j<ObjList.getLength();j++)
                    {
                        Node ObjNode = ObjList.item(j);
                        if (ObjNode.getNodeType() == Node.ELEMENT_NODE)
                        {
                            Element objElement=(Element) ObjNode;
                            String object=objElement.getAttribute("name");
                            if (!"".equals(object)) //name of object
                            {
                                Objects.add(object);
                            }
                        }
                    }
                    Coordinates access=new Coordinates(east,west,north,south);
                    //adding a new room with properties from XML
                    rooms.add(new Room(id,name,access,Objects));
                }
            }
        }
        catch(FileNotFoundException e)
        {
            System.out.println("file not found:"+filename);
            e.printStackTrace();
        }
        catch(IOException e)
        {
            System.out.println("cannot open file:"+filename);
            e.printStackTrace();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        if (!isValid())
        {
            System.out.println("Invalid xml map file. Sorry, request cannot be processed");
            System.exit(-1);
        }
    }
}
