package sbbj_tpg_mem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TPGPlay
{
	// Create a variable for holding a Team
	protected Team team = null;
	
	// Given a folder name as an input, read in a model and prepare it to play
	public TPGPlay( String modelFolder )
	{
		// Load a model and store it as the sole team.
		team = loadModel(modelFolder);		
	}
		
	// Given an input feature set, produce an action
	public long participate( double[] inputFeatures )
	{
		// If we don't have a Team, it can't play
		if( team == null )
			return -1;

		// Give the team the input features to find an action
		return team.getAction(new HashSet<Team>(), inputFeatures);
	}

	// Print the current status of the TPG algorithm
	public void printStats( int teamCount )
	{
		// Input whatever stats readout you want. There's very little to look at,
		// but you could include outcomes as they're generated.
	}
	
	public HashSet<Learner> createLearnerSet(String folder, Set<Long> idList)
	{
		// Create a scanner variable.
		Scanner reader = null;
		
		// Create a new Array List for holding Learners
		HashSet<Learner> learners = new HashSet<Learner>();
		
		// Iterate through every Learner ID
		for(Long ID: idList)
		{
			try
			{
				// Open the relevant Learner file by ID
				reader = new Scanner(new File(folder + "learner_" + ID + ".txt"));
				
				// Read in the first line of values, in the order of
				// ID, birthday, references, and action.
				long id = reader.nextLong();
				long birthday = reader.nextLong();
				int references = reader.nextInt();
				String actString = reader.next();
				
				// Create an Array List for holding Instructions
				ArrayList<Instruction> instructions = new ArrayList<Instruction>();
				
				// As long as there are Instructions left to read in, do so.
				while(reader.hasNextLong())
					instructions.add(new Instruction(reader.nextLong()));
				
				// Close the scanner.
				reader.close();
				
				// Create a long variable to hold an action.
				long action = 0;
				
				// If the action is a Team reference, set it to a negative number
				// and subtract 1 from it. This becomes relevant in the final part 
				// of the read-in process.				
				if(actString.charAt(0) == 'T')
				{
					action = -1 * Long.valueOf(actString.substring(1)) - 1;
				}
				else
				{
					action = Long.valueOf(actString);
				}
				
				// Construct the new Learner from the values read in.
				Learner learner = new Learner(id, birthday, action, 0, instructions);
				
				// Add the new Learner to the list.
				learners.add(learner);
			}
			catch(FileNotFoundException e)
			{
				System.err.print(e);
				System.exit(0);
			}
		}
		
		// Return the completed Learner list.
		return learners;
	}
	
	public HashSet<Team> createTeamSet(String folder, Set<Long> idList, Set<Learner> learnerList)
	{
		// Create a scanner variable.
		Scanner reader = null;
		
		// Create a new Array List for holding Teams
		HashSet<Team> teams = new HashSet<Team>();
		
		// Iterate through every Team ID
		for(Long ID: idList)
		{
			try
			{
				// Open the relevant Team file by ID
				reader = new Scanner(new File(folder + "team_" + ID + ".txt"));
				
				// Read in the first line of values, in the order of
				// ID, birthday, and references.
				long id = reader.nextLong();
				long birthday = reader.nextLong();
				int references = reader.nextInt();
				
				// Create a new Team from this information
				Team team = new Team(id, birthday, 0);
				
				// As long as there are Learner IDs left to read in, do so.
				while(reader.hasNextLong())
				{
					// Store the current ID we've read in.
					long currentID = reader.nextLong();

					// Check the Learner set for the ID. When we find it,
					// add the associated Learner to the Team.
					for(Learner L: learnerList)
					{
						if(L.getID() == currentID)
						{
							team.addLearner(L);
							break;
						}
					}
				}				
				
				// Close the scanner.
				reader.close();
				
				// Add the new Team to the set.
				teams.add(team);
			}
			catch(FileNotFoundException e)
			{
				System.err.print(e);
				System.exit(0);
			}
		}
		
		// Return the completed Team set.
		return teams;
	}
	
	public Team loadModel(String folder)
	{
		// Attempt to write the root team and the root team ID to the folder.
		try
		{
			// Create a scanner for capturing the root team ID.
			Scanner reader = new Scanner(new File(folder + "root.txt"));
			
			// Get the root team ID from the file.
			long rootTeamID = reader.nextLong();
			
			// Close the scanner for this file.
			reader.close();
			
			// Create a new hash set for storing IDs.
			HashSet<Long> idSet = new HashSet<Long>();
			
			// Create a scanner for capturing the learner IDs.
			reader = new Scanner(new File(folder + "learnerIDs.txt"));
			
			// As long as the file has input, add longs to the ID set.
			while(reader.hasNextLong())
				idSet.add(reader.nextLong());
			
			// Close the scanner for this file.
			reader.close();
			
			// Using the IDs and Learner files, create a set of Learners
			Set<Learner> learners = createLearnerSet(folder, idSet);
			
			// Reset the ID List
			idSet.clear();
			
			// Create a scanner for capturing the learner IDs.
			reader = new Scanner(new File(folder + "teamIDs.txt"));
			
			// As long as the file has input, add longs to the ID set.
			while(reader.hasNextLong())
				idSet.add(reader.nextLong());
			
			// Close the scanner for this file.
			reader.close();
			
			// Using the IDs and Team files, create a set of Teams
			Set<Team> teams = createTeamSet(folder, idSet, learners);
						
			// Finally, make sure all the Learners have the correct action.
			for(Learner L: learners)
			{
				// Create a long variable and retrieve the action from the current Learner.
				long action = L.getActionObject().getAction(null, null);
				
				// If that action is negative, then we know it's actually a Team reference.
				if(action < 0)
				{
					// Search every Team:
					for(Team T: teams)
					{
						// When we find the Team that has the matching ID:
						if(T.getID() == -1*(action+1))
						{
							// Create a new Action holding the Team reference and
							// add it to the Learner with the mutateAction method,
							// then break the inside loop.
							L.mutateAction(new Action(T));
							break;
						}
					}
				}					
			}
			
			// Find and return the root team
			for(Team T: teams)
				if(T.getID() == rootTeamID)
					return T;
		} 
		catch(FileNotFoundException e) 
		{
			System.err.println(e); 
		}
		
		// If we got here, something broke in the read-in process.
		return null;		
	}
}
