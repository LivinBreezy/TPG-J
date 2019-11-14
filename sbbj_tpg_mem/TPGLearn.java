package sbbj_tpg_mem;

import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TPGLearn
{
	// Create a population list for all Teams
	protected ArrayList<Team> teams = new ArrayList<Team>();
	
	// Create a population list for Root Teams
	protected ArrayList<Team> rootTeams = new ArrayList<Team>();
	
	// Create a population list for Learners
	protected ArrayList<Learner> learners = new ArrayList<Learner>();

	// Create a list for holding actions
	protected ArrayList<Long> actions = new ArrayList<Long>();

	// Create a list for holding task labels
	protected HashSet<String> labels = new HashSet<String>();
	
	// Create a queue for Teams during the learning process
	LinkedList<Team> teamQueue = new LinkedList<Team>();
	
	// Team Population Size
	protected int teamPopSize = 0;
	
	// Root Team Gap Percentage
	protected double teamGap = 0.0;

	// Probability of Learner Deletion
	protected double probLearnerDelete = 0.0;
	
	// Probability of Learner Addition
	protected double probLearnerAdd = 0.0;
	
	// Probability of Action Mutation
	protected double probMutateAction = 0.0;
	
	// Probability that when an Action is Mutated it will be a Team
	protected double probActionIsTeam = 0.0;
	
	// Maximum Team Size
	protected int maximumTeamSize = 0;
	
	// Number of Training Epochs
	protected long epochs = -1;
	
	// Learners: Maximum Program Size
	protected int maximumProgramSize = 0;
	
	// Learners: Probability of Instruction Deletion 
	protected double probProgramDelete = 0.0;
	
	// Learners: Probability of Instruction Add
	protected double probProgramAdd = 0.0;
	
	// Learners: Probability of Instruction Swap
	protected double probProgramSwap = 0.0;
	
	// Learners: Probability of Instruction Mutate
	protected double probProgramMutate = 0.0;

	// Random seed value from the parameters file
	protected int seed = 0;

	// Create a date format and store it for saving files later
	DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
	Date currentDate = new Date();
	
	// Given a map of arguments, initialize all the basic state values for the TPG algorithm
	public TPGLearn( Map<String, String> arguments )
	{
		// Gather all the assigned variables from the arguments map
		teamPopSize = Integer.valueOf( arguments.get("teamPopSize") );
		teamGap = Double.valueOf( arguments.get("teamGap") );
		probLearnerDelete = Double.valueOf( arguments.get("probLearnerDelete") );
		probLearnerAdd = Double.valueOf( arguments.get("probLearnerAdd") );
		probMutateAction = Double.valueOf( arguments.get("probMutateAction") );
		probActionIsTeam = Double.valueOf( arguments.get("probActionIsTeam") );
		maximumTeamSize = Integer.valueOf( arguments.get("maximumTeamSize") );
		maximumProgramSize = Integer.valueOf( arguments.get("maximumProgramSize") );
		probProgramDelete = Double.valueOf( arguments.get("probProgramDelete") );
		probProgramAdd = Double.valueOf( arguments.get("probProgramAdd") );
		probProgramSwap = Double.valueOf( arguments.get("probProgramSwap") );
		probProgramMutate = Double.valueOf( arguments.get("probProgramMutate") );	
		seed = Integer.valueOf( arguments.get("seed") );
		
		// Print all the arguments to the screen to confirm they were read properly
		System.out.println("arg teamPopSize " + teamPopSize);
		System.out.println("arg teamGap " + teamGap);
		System.out.println("arg probProgramDelete " + probProgramDelete);
		System.out.println("arg probProgramAdd " + probProgramAdd);
		System.out.println("arg probMutateAction " + probMutateAction);
		System.out.println("arg probActionIsTeam " + probActionIsTeam);
		System.out.println("arg maximumTeamSize " + maximumTeamSize);
		System.out.println("arg maximumProgramSize " + maximumProgramSize);
		System.out.println("arg probProgramDelete " + probProgramDelete);
		System.out.println("arg probProgramAdd " + probProgramAdd);
		System.out.println("arg probProgramSwap " + probProgramSwap);
		System.out.println("arg probProgramMutate " + probProgramMutate);
		System.out.println("arg seed " + seed);
	}
	
	// Provide the list of available actions as a set of numbers
	public boolean setActions( long[] acts )
	{
		// Retrieve the actions from the long array and place them in the list
		for( int i=0; i < acts.length; i++ )
			this.actions.add(acts[i]);
		
		// Return true if successful
		return true;
	}
	
	// Fully initialize the TPG algorithm and wait for the learning process to start
	public boolean initialize()
	{
		// Create an initial population of Teams and Learners
		if( !initializePopulations() ) 
			return false;

		// Create a new linked list to act as a queue for the remaining Teams to act
		teamQueue = new LinkedList<Team>();
		
		// Add all the current root Teams to the Team queue
		teamQueue.addAll(rootTeams);
				
		// All the Teams are generated, so move to generation 0
		epochs++;

		// Everything completed successfully; return true
		return true;
	}
	
	// Generate a population of Teams and Learners to completion
	public boolean initializePopulations()
	{
		// Create two action variables
		long action1 = 0;
		long action2 = 0;
		
		// Create a team and learner variable
		Team team = null;
		Learner learner = null;

		int teamsToKeep = (int)teamPopSize;
		
		// Generate a number of teams equal to the keep threshold
		for( int i=0; i < teamsToKeep; i++ )
		{
			// Get two different random actions
			action1 = (long)(TPGAlgorithm.RNG.nextDouble() * actions.size());
			
			// Generate actions until action1 and action2 are different
			do
			{
				action2 = (long)(TPGAlgorithm.RNG.nextDouble() * actions.size());
			}
			while( action1 == action2 );

			// Create a new Team with age -1 (pre-learning age)
			team = new Team(-1);
			
			// Create a Learner with the first action and add it to the Team
			// as well as the Learner population.
			learner = new Learner(-1, actions.get((int)action1), maximumProgramSize );
			team.addLearner(learner);
			learners.add(learner);
			
			// Create a Learner with the second action and add it to the Team
			// as well as the Learner population.
			learner = new Learner(-1, actions.get((int)action1), maximumProgramSize );
			team.addLearner(learner);
			learners.add(learner);
			
			// Since teams can be initialized with any number of Learners
			// up to the maximumTeamSize, we randomize more here.
			//long learnerThreshold = (long)(TPGAlgorithm.RNG.nextDouble() * (maximumTeamSize-2));
			long learnerThreshold = (long)maximumTeamSize-2;
			
			for( int j=0; j < learnerThreshold; j++ )
			{
				learner = new Learner(-1, actions.get((int)(TPGAlgorithm.RNG.nextDouble() * actions.size())), maximumProgramSize);
				team.addLearner(learner);
				learners.add(learner);
			}
			
			// Add the final Team to the Team Population
			teams.add(team);
			rootTeams.add(team);
		}
		
		// We've generated all the teams successfully; return true
		return true;		
	}
	
	// Given an input feature set, produce an action
	public long participate( double[] inputFeatures )
	{
		// If we have no Teams left during this learning phase, then we can't learn anything
		if( teamQueue.isEmpty() )
			return -1;
		
		// Otherwise get the next Team
		Team team = teamQueue.getFirst();
		
		// Give the team the input features to find an action
		return team.getAction(new HashSet<Team>(), inputFeatures);
	}
	
	// Given an input feature set and required action set, produce an action
	public long participate( double[] inputFeatures, long[] actions )
	{
		// If we have no Teams left during this learning phase, then we can't learn anything
		if( teamQueue.isEmpty() )
			return -1;
		
		// Otherwise get the next Team
		Team team = teamQueue.getFirst();
		
		// Give the team the input features to find an action
		long action = team.getAction(new HashSet<Team>(), inputFeatures);
		
		// If the team's action is in the action list, return it
		for( int i=0; i < actions.length; i++ )
			if( action == actions[i] )
				return action;
		
		// If the action is not in the allowed action list, return a default of 0
		return 0L;
	}
	
	// Provide a reward to the current Team after a participation cycle has ended
	public boolean reward( String label, double reward )
	{
		// If there's no Team to reward, we don't bother
		if( teamQueue.isEmpty() )
			return false;
		
		// If there's a Team, remove it from the queue and set its outcome
		teamQueue.pop().setOutcome(label, reward);
		
		// Add this label to the label set, duplicates are automatically discarded
		labels.add(label);		
				
		// Return true if the reward was successful
		return true;
	}

	// Generate new Teams from the current Root Team population.
	// This generates Teams regardless of what is in the current root list, so if
	// you only want the best Teams to reproduce, make sure the selection() method
	// is executed once before executing the generateTeams() method.
	public void generateNewTeams()
	{
		// Create a list to hold the parent Teams
		ArrayList<Team> parents = new ArrayList<Team>();
		int size = 0;
		
		// Create variables for parents and various child Teams
		Team parent1 = null, parent2 = null;
		Team child1 = null, child2 = null, childX = null, childY = null;
		
		// Lists to hold the Learners belonging to the parents
		ArrayList<Learner> parent1Learners = new ArrayList<Learner>();
		ArrayList<Learner> parent2Learners = new ArrayList<Learner>();
		
		// Lists to hold the Learners belonging to the new children
		ArrayList<Learner> child1Learners = new ArrayList<Learner>();
		ArrayList<Learner> child2Learners = new ArrayList<Learner>();
		
		// Set intersection and difference
		ArrayList<Learner> learnerIntersection = new ArrayList<Learner>();
		ArrayList<Learner> learnerDifference = new ArrayList<Learner>();
		
		// Get parent teams into list
		for( Team team : rootTeams )
			parents.add( team );
		
		// Keep a count of the number of parents we have
		size = parents.size();
		
		// Keep creating children until we reach the required population size
		while( teams.size() < teamPopSize )
		{
			// To start, retrieve one parent from the 
			parent1 = parents.get((int)(TPGAlgorithm.RNG.nextDouble() * size));
			
			// Retrieve a second parent which is not the same as the first
			do
			{
				parent2 = parents.get((int)(TPGAlgorithm.RNG.nextDouble() * size));
			}
			while( parent1 == parent2 );
			
			// Empty the current parent Learner lists
			parent1Learners.clear();
			parent2Learners.clear();
			
			// Retrieve the current Learner lists from the parents
			parent1Learners.addAll( parent1.getLearners() );
			parent2Learners.addAll( parent2.getLearners() );
			
			// Calculate the Intersection and Difference of the two Learner lists
			learnerIntersection = Miscellaneous.intersectionLearner(parent1Learners, parent2Learners);
			learnerDifference = Miscellaneous.symmetricDifferenceLearner(parent1Learners, parent2Learners);
			
			// Create children Teams
			child1 = new Team(epochs);
			child2 = new Team(epochs);
			
			// Both children get all the Learners from the Intersection list
			for( Learner learner : learnerIntersection )
			{
				child1.addLearner( learner );
				child2.addLearner( learner );
			}
			
			// Add each Learner in the Difference list to one of the children
			for( Learner learner : learnerDifference )
			{
				// We decide the child priority, where X is higher priority than Y
				if( TPGAlgorithm.RNG.nextDouble() < 0.5 )
				{
					childX = child1;
					childY = child2;
				}
				else
				{
					childX = child2;
					childY = child1;
				}
				
				// We add the current Learner to child X, except under these conditions:
				// 		1. We can't add a Learner if the child's Team is already full.
				//		2. We can't add more than two Learners to child X if child Y has less than two.
				// If either of these conditions are met, we add the Learner to child Y instead.
				if( childX.size() < maximumTeamSize && !( childX.size() >= 2 && childY.size() < 2 ) )
					childX.addLearner(learner);
				else
					childY.addLearner(learner);
			}
			
			// If, somehow, we've broken the team boundary rules then we have to crash. What happened?
			if( (child1.size() > maximumTeamSize || child2.size() > maximumTeamSize) || (child1.size() < 2 && child2.size() < 2) )
				throw new RuntimeException("Team generation has failed.");
			
			// Empty the current child Learner lists
			child1Learners.clear();
			child2Learners.clear();
			
			// Retrieve the current Learner lists from the children
			child1Learners.addAll(child1.getLearners());
			child2Learners.addAll(child2.getLearners());
			
			// If the first child has the same Learner set as either parent, they MUST be mutated
			if( Miscellaneous.equalLists(child1Learners, parent1Learners) || Miscellaneous.equalLists(child1Learners, parent2Learners) )
				while( !mutate(epochs , child1) );
			else
				mutate(epochs, child1);
			
			// If the second child has the same Learner set as either parent, they MUST be mutated
			if( Miscellaneous.equalLists(child2Learners, parent1Learners) || Miscellaneous.equalLists(child2Learners, parent2Learners) )
				while( !mutate(epochs , child2) );
			else
				mutate(epochs, child2);

			// Insert the new Teams into the Root Team population
			rootTeams.add( child1 );
			rootTeams.add( child2 );
			
			// Also insert the new Teams into the regular Team population
			teams.add( child1 );
			teams.add( child2 );			
		}
	}
	
	// Mutate a Team and the Learners it references
	public boolean mutate( long epoch, Team team )
	{
		// Create a flag for whether or not this Team has mutated
		boolean changedTeam = false;
		
		// Create a flag for whether or not a Learner has mutated
		boolean changedLearner = false;
		
		// Create a list for holding Learners during the mutation process		
		ArrayList<Learner> learnerSet = new ArrayList<Learner>();
		
		// Create a variable for holding Learners
		Learner learner = null;
		
		// Retrieve all Learners in the current Team and store them in a list
		learnerSet.addAll( team.getLearners() );
		
		// We want to consider the Learners in arbitrary order, so shuffle the list
		Collections.shuffle( learnerSet );
		
		// For every Learner attached to this Team, we will try to perform a Learner (Program) Delete
		for( int i=0; i < learnerSet.size(); i++ )
		{
			// We can't ever have less than two Learners, so stop when we have two.
			if( team.getLearners().size() <= 2 )
				break;
			
			// Retrieve the current learner for easy use
			learner = learnerSet.get(i);
			
			// If the current Team only has one atomic action and that action
			// belongs to this Learner, we should skip it to maintain the 1 Atomic Action minimum.
			if( Miscellaneous.countAtomicActions(team.getLearners()) == 1 && learner.getActionObject().isAtomic() )
				continue;
			
			// Mutation: Learner Deletion Event
			if( TPGAlgorithm.RNG.nextDouble() < probLearnerDelete )
			{
				// Remove the Learner from the team (but not the learnerSet here)
				team.removeLearner(learner);

				// We deleted a Learner, so the Team has changed
				changedTeam = true;
			}
		}
		
		// For every Learner in the ORIGINAL Team, attempt to add Learners and Mutate Programs/Actions
		for( Learner l : learnerSet )
		{
			// If we reach the maximum Team size limit, stop mutating
			if( team.getLearners().size() == maximumTeamSize )
				break;
			
			// Mutation: Learner (Program) Add Event
			if( TPGAlgorithm.RNG.nextDouble() < probLearnerAdd )
			{
				// We haven't changed this Learner yet
				changedLearner = false;
				
				// Create a new Learner based on the one in the current for loop iteration
				learner = new Learner(epoch, l);
				
				// Mutation: Learner's Program. The mutateProgram() method returns true if changes were made. False otherwise.
				changedLearner = learner.mutateProgram(probProgramDelete, probProgramAdd, probProgramSwap, probProgramMutate, maximumProgramSize);
				
				// Mutation: Mutate the Learner's Action
				if( TPGAlgorithm.RNG.nextDouble() < probMutateAction )
				{
					// Create a variable for holding an Action
					Action action = null;
					
					// Randomly choose between the Action being a Team or an Atomic
					if( TPGAlgorithm.RNG.nextDouble() < probActionIsTeam )
					{
						// Create a variable for holding a random Team
						Team actionTeam = null;
						
						// Grab a random Team from the list, making sure not to
						// choose a root team if the root teams are too small
						do
						{
							actionTeam = teams.get((int)(TPGAlgorithm.RNG.nextDouble() * teams.size()));
						} while( rootTeams.size() <= 5 && rootTeams.contains(actionTeam) );
						
						// Create a new Action with the Team as the action
						action = new Action( actionTeam );
												
						// Increase the references to this Team
						actionTeam.increaseReferences();						
					}
					else
						action = new Action( actions.get((int)(TPGAlgorithm.RNG.nextDouble() * actions.size())) );
					
					// Attempt to mutate the Learner's Action. If successful, mutateAction() returns true. False otherwise.
					// If this Learner was changed earlier, OR it with the previous result to ensure it doesn't disappear.
					changedLearner = learner.mutateAction(action) || changedLearner;
				}
				
				// If the Learner didn't mutate when we don't bother keeping it
				if( !changedLearner )
				{
					// Setting learner to null will force the unreferenced one to be garbage collected
					learner = null;
				}
				else
				{
					// Add the mutated Learner to the Team's Learner list
					team.addLearner(learner);
					
					// Add the mutated Learner to the Learner population
					learners.add(learner);
					
					// The Team has changed, so make sure we flag it
					changedTeam = true;
				}
			}
		}
		
		// Teams should never have fewer than two Learners, ever
		if( team.size() < 2 )
			throw new RuntimeException("team.size() is < 2. This error is confusing to you, but rest assured that the world will not explode in 42 seconds.");
		
		// Was the team mutated in this iteration?
		return changedTeam;
	}

	public void selection()
	{
		// Crate a hash map to store the Team outcome map
		HashMap<Team, ArrayList<Double>> outcomeMap = new HashMap<Team, ArrayList<Double>>();
		
		// Create a list for storing Teams marked for deletion
		ArrayList<Team> selectedForDeletion = new ArrayList<Team>();
		
		// Number of root teams that make it to the next generation
		int keep = (int)Math.floor(rootTeams.size() * teamGap);
		
		// Create an OpenDouble for holding outcome values
		OpenDouble outcome = new OpenDouble(0.0);
		
		// For every root Team, store their outcomes in the outcome map
		for( Team team : rootTeams )
		{
			// Create a list for holding outcomes
			ArrayList<Double> outcomeList = new ArrayList<Double>();
			
			// Store that list in the outcome map with the current Team as the key
			outcomeMap.put( team, outcomeList );
			
			// For every outcome label seen, retrieve that label's outcome from the Team and store it in their list
			for( String label : labels )
			{
				// When updating the outcome object value, if this Team didn't receive a reward for the label, something is broken
				if( !team.getOutcome(label, outcome) )
					throw new RuntimeException("Cannot find outcome " + label + " on a Team during selection.");
				
				// Otherwise we add the outcome's value to this Team's outcome list
				outcomeList.add( outcome.getValue() );
			}
		}
		
		// Determine which Teams need to be marked for deletion during the algorithm cleanup phase.
		if( labels.size() == 1 )
		{
			// Rank the root Teams by their outcomes
			Miscellaneous.sortTeamsBySingleOutcome( rootTeams, outcomeMap );			
			
			// Save the number one ranked Team to a file
			saveBestTeam(rootTeams.get(0));
			
			// Since the Teams are ranked, starting at the index after the last Team to be kept, place the remaining Teams in the deletion list
			for( int i=keep; i < rootTeams.size(); i++ )
				selectedForDeletion.add( rootTeams.get(i) );
		}
		else
		{
			// We won't reach here in this single-task implementation. Once you introduce more tasks
			// or dimensions, you will need to add the ability to handle those dimensions here.
			// Classically, SBB and TPG use pareto dominance to handle multiple dimensions, but there
			// are other methods of handling this problem.
		}
		
		// Delete any Teams found in the deletion list
		for( Team team : selectedForDeletion )
		{
			// Make sure this Team dereferences its Learners before it's deleted
			team.erase();
			
			// Remove the Team from all Team-related Populations
			teams.remove(team);
			rootTeams.remove(team);
		}

		// Force the outcome map to be flagged for garbage collection
		outcomeMap = null;
	}
	
	// Use this method for cleaning up any "extra" data. This method
	// always removes any Learners without a reference.
	public void cleanup()
	{
		// Make a copy of the Learner population list. If you don't do this, you'll
		// make all kinds of crazy errors happen as the population list changes size while you iterate.
		ArrayList<Learner> allLearners = new ArrayList<Learner>(learners);

		// Iterate through all Learners...
		for( Learner learner : allLearners )
		{
			// If a Learner has zero references...
			if( learner.getReferences() == 0 )
			{
				// Remove it from the Learner population
				learners.remove( learner );
				
				// If this Learner references a Team, make sure we de-reference it
				if( !learner.getActionObject().isAtomic() )
					learner.getActionObject().team.decreaseReferences();
			}
		}
	}

	// Roll over to the next learning session
	public long nextEpoch()
	{
		// Run the cleanup() method once to ensure everything is tidy
		cleanup();
		
		// Clear the outcome maps of all Teams
		for( Team team : teams )
			team.outcomes.clear();
		
		// Clear the current root Team population
		rootTeams.clear();
				
		// If a Team has no references to it (aka an in-degree of zero), then it's a root Team
		for( Team team : teams )
			if( team.getReferences() == 0 )
				rootTeams.add(team);
		
		// Reset the Team queue and add all the root Teams to it
		teamQueue.clear();
		teamQueue.addAll(rootTeams);
				
		// The number of epochs increases and is returned
		return ++epochs;
	}
	
	// Save the current best model
	public void saveBestTeam(Team team)
	{
		// Create a String for holding the folder name.
		String folder = dateFormat.format(currentDate) + "_" + seed + "/generation_" + epochs + "/";
		
		// If the appropriate directory doesn't exist, create it.
		if(!Files.isDirectory(Paths.get(folder)))
			new File(folder).mkdirs();
				
		// Attempt to write the root team and the root team ID to the folder.
		try
		{
			// Store the root team in its own file and note the ID in the root file.
			File filePath = new File(folder + "root.txt");
			FileWriter writer = new FileWriter(filePath);
			writer.write("" + team.getID());
			writer.close();
			
			// Create sets for holding unique teams and learners
			HashSet<Team> teamSet = new HashSet<Team>();
			HashSet<Learner> learnerSet = new HashSet<Learner>();
			
			// Recursively find all Teams attached to the current root team
			team.findAllTeams(teamSet);
			
			// Add all of the learners from each discovered team to the 
			// learner set. Since Learners and Teams have custom hashes
			// and equals methods, no duplicates will be stored.
			for(Team T: teamSet)
				learnerSet.addAll(T.getLearners());
			
			// Store every Team in the team set to its own file. The team
			// files are named team_#.txt, where # is the team's ID.
			for(Team T: teamSet)				
			{
				filePath = new File(folder + "team_" + T.getID() + ".txt");
				writer = new FileWriter(filePath);
				writer.write(T.storageOutput());
				writer.close();
			}
						
			// Store every Learner in the learner set to its own file.
			// The learner files are named learner_#.txt, where # is 
			// the learner's ID.
			for(Learner L: learnerSet)
			{
				filePath = new File(folder + "learner_" + L.getID() + ".txt");
				writer = new FileWriter(filePath);
				writer.write(L.storageOutput());
				writer.close();
			}
			
			// Store all of the team IDs in a teamIDs.txt file. The IDs
			// are stored one per line. There is no guarantee of order.
			filePath = new File(folder + "teamIDs.txt");
			writer = new FileWriter(filePath);
			for(Team T: teamSet)				
				writer.write(T.getID() + "\n");
			writer.close();
			
			// Store all of the learner IDs in a learnerIDs.txt file. 
			// The IDs are stored one per line. There is no guarantee of order.
			filePath = new File(folder + "learnerIDs.txt");
			writer = new FileWriter(filePath);
			for(Learner L: learnerSet)				
				writer.write(L.getID() + "\n");
			writer.close();						
		} 
		catch(IOException e) 
		{
			System.err.println(e); 
		}
		
		// Explicit return to signify the end of the method.
		return;
	}
	
	// Print the current status of the TPG algorithm
	public void printStats( int teamCount )
	{
		// If the input value is out of bounds, set it to the proper amount
		if( teamCount > rootTeams.size() )
			teamCount = rootTeams.size();
		else if( teamCount <= 0 )
			return;
		
		// Crate a hash map to store the Team outcome map
		HashMap<Team, ArrayList<Double>> outcomeMap = new HashMap<Team, ArrayList<Double>>();
		
		// Create a copy of the root Teams list
		ArrayList<Team> rootCopy = new ArrayList<Team>(rootTeams);
		
		// Print some general information
		System.out.println("Generation: " + epochs + "\n\tRoot Teams: " + rootTeams.size() + ", Teams: " + teams.size() + ", \n\tLearners: " + learners.size() + ", Labels: " + labels.size());
				
		// Create an OpenDouble for holding outcome values
		OpenDouble outcome = new OpenDouble(0.0);
		
		// For every root Team, store their outcomes in the outcome map
		for( Team team : rootTeams )
		{
			// Create a list for holding outcomes
			ArrayList<Double> outcomeList = new ArrayList<Double>();
			
			// Store that list in the outcome map with the current Team as the key
			outcomeMap.put( team, outcomeList );
			
			// For every outcome label seen, retrieve that label's outcome from the Team and store it in their list
			for( String label : labels )
			{
				// When updating the outcome object value, if this Team didn't receive a reward for the label, something is broken
				if( !team.getOutcome(label, outcome) )
					throw new RuntimeException("Cannot find outcome " + label + " on a Team during stats gathering.");
							
				// Otherwise we add the outcome's value to this Team's outcome list
				outcomeList.add( outcome.getValue() );
			}
		}
		
		// Rank the root Teams by their outcomes
		Miscellaneous.sortTeamsBySingleOutcome( rootCopy, outcomeMap );
		
		// Print the top Teams based on the provided teamCount value
		for( int i=0; i < teamCount; i++ )
			System.out.println("\tTeam " + rootCopy.get(i).getID() + ": " + outcomeMap.get(rootCopy.get(i)).get(0));
	}
	
	// Return the number of Teams still in the Team queue
	public int remainingTeams()
	{
		return teamQueue.size();
	}
	
	// Return the total number of Teams in the Team population
	public int getTeamCount()
	{
		return teams.size();
	}
	
	// Return the total number of Learners in the Learner population	
	public int getLearnerCount()
	{
		return learners.size();
	}
	
	// Return the number of epochs (generations) passed
	public long getEpochs()
	{
		return epochs;
	}
}
