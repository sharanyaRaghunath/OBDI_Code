package org.biojava.ontology.io;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.TreeMap;


public class MapConfigFile{
	

public TreeMap<String,String> ReadConfigFile (String mapfile){
	
	TreeMap<String, String> map = null;
       
    try {
        @SuppressWarnings("resource")
		LineNumberReader lnr = new LineNumberReader(new FileReader(new File(mapfile)));
        String line = lnr.readLine();
        map = new TreeMap<String,String>();
        while(line != null) {
        	//note the "3" in the next line is how many columns there are in the index file.
        	//be sure to maintain this if the format changes.
            String[] tokens = line.split("\\s*\\=\\s*",2); //<----- NOTE SPECIAL TREATMENT of pipe char
            //the format of the index file is as follows: <Title Text>|MLExperiment|ExperimentFolder|ConditionFolder
            System.out.println("Mapping description \"" + tokens[0]+ "\" to " + tokens[1]);
            if(map.containsKey(tokens[0])) {
            	System.err.println("ERROR: config file has duplicate labels: '" + tokens[0] + "'");
            	System.exit(1);
            }
            map.put(tokens[0], tokens[1]); 
            line = lnr.readLine();
        }
    } catch (Exception e) {
        System.err.println("Unable to open config file" + mapfile + ": dying");
        return null;
    } 
   
    return map;
}
}
 
