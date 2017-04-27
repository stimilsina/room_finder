package com.sulav;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Main {
    /**
     *
     * @param filename config filename
     * @return each line of the file stored as an ArrayList
     */
    public static List<String> parseConfig(String filename)
    {
        List<String> input=new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename)))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                input.add(line);
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
        return input;
    }

    /**
     *
     * @param args-mapfile,configfile,outputfile
     */
    public static void main(String[] args) {
        if (args.length==3)
        {
            Map map = new Map(args[0]);
            List<String> parameters = parseConfig(args[1]);
            map.searchPath(parameters,args[2]);
        }
        else
        {
            System.out.println("Invalid set of command line parameters");
        }
    }
}
