package sbbj_tpg;

import java.util.*;
import java.io.*;

public class TPGAlgorithm 
{
	// A map for holding arguments from the parameters file
	public HashMap<String, String> arguments = null;
	
	// A variable for holding a static Random Number Generator 
	public static Random RNG = null;
	
	// TPG Framework Objects
	protected TPGLearn tpgLearn = null;
	protected TPGPlay tpgPlay = null;
	
	// Create a new TPGAlgorithm in Learn or Play mode
	public TPGAlgorithm( String inputFile, String type )
	{
		if( type.equals("learn") )
		{
			System.out.println("Starting TPG in Learning Mode.");
			startLearning( inputFile );
		}
		else if( type.equals("play") )
		{
			System.out.println("Starting TPG in Play Mode.");
			startPlaying( inputFile );
		}
		else
			throw new RuntimeException("Uh, we had a slight input parameters malfunction, but uh... everything's perfectly all right now. We're fine. We're all fine here now, thank you. How are you?");
	}

	// Start a Learn session
	public void startLearning( String argumentsFile )
	{
		// Create new data structures for storage
		arguments = new HashMap<String, String>();
		
		// Set the procedure type to all before checking it
		arguments.put("procedureType", "all");
	
		// Get the arguments
		readArgumentsToMap( argumentsFile );

		// Set the seed for the RNG
		RNG = new Random( Integer.parseInt(arguments.get("seed")) );
		if( Integer.valueOf(arguments.get("seed")) == 0 )
			RNG = new Random( System.currentTimeMillis() );
		
		// Create a new TPGLearn object to start the learning process
		tpgLearn = new TPGLearn(arguments);
	}
	
	// Start a Play session
	public void startPlaying( String modelFile )
	{
		// Creates a new TPGPlay object. Does not need arguments or RNG.
		tpgPlay = new TPGPlay(modelFile);
	}
	
	// Get the TPGLearn object. This returns null if TPGAlgorithm is in Play mode.
	public TPGLearn getTPGLearn()
	{
		return tpgLearn;
	}
	
	// Get the TPGPlay object. This returns null if TPGAlgorithm is in Learn mode.
	public TPGPlay getTPGPlay()
	{
		return tpgPlay;
	}
	
	// Read the arguments from a file and store them in an arguments map
	public void readArgumentsToMap( String fileName )
	{
		// Create a variable for holding a Scanner
		Scanner argumentsInput = null;

		try
		{		
			// Create a new scanner for scanning the file
			argumentsInput = new Scanner( new File(fileName) );			
		
			// Create a variable for holding String arrays
			String line[] = null;
			
			// While there are more arguments to be read in...
			while( argumentsInput.hasNextLine() )
			{
				// Read in the next line from the file and split it using the equals sign
				line = argumentsInput.nextLine().split("=");
				
				// Save the line in the arguments map as argName->argValue
				arguments.put(line[0], line[1]);
			}
			
			// Close the scanner when we're done
			argumentsInput.close();
		}
		catch( FileNotFoundException e )
		{
			throw new RuntimeException("The arguments file name provided does not exist.");
		}
	}
}