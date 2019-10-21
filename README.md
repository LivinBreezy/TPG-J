# TPG-J
This is the most up-to-date repository for TPG-J. The current version is v1.1.

# UPDATE 10/16/2019:
   ### Initial Release Version
   * With this update, TPG-J has been pushed to version 1.0 and 1.1 for regular and memory respectively. 
   ### TPGLearn Code Changes
   * TPGLearn.saveBestTeam(..) added, which allows the algorithm to store the best Team every generation. Could be used to store any Team, although it may be more useful to set up a helper with a more descriptive name.
   * In every generation, the TPGLearn.selection(..) method will now rank the Teams and store the best Team for that generation to a file. The run will be stored in a folder with a numerical date and the seed attached. There is a sample folder (16-10-2019-05-03-45_0) provided in the repository so you can see the directory and file structure.
   ### Team and Learner Code Changes
   * Teams and Learners have been given a hashCode() and equals() method to allow correct storage in Set objects.
   * Team.storageOutput() and Learner.storageOutput() have been added to produce a string representation of the given object explicitly for storing the object in a file.
   ### TPGPlay Code Changes
   * TPGPlay should be fully functional. It requires a path to an existing Team, a sample of which is provided in the 16-10-2019-05-03-45_0 folder. The path is already included for TPGPlay by default, so the APIExecutionExample3 class should be able to execute the TPGPLay object without any changes.
   ### TPG-MEM Added
   * The memory model version of TPG-J was uploaded. It contains a memory matrix which all Learners have access to statically. Instructions have been updated to accomodate the new bit width, as the mode section and operation section have each grown by 1 bit. The Learners in TPG-MEM have been modified to better handle the new Instruction format. 
# UPDATE 2/7/2019: 
   ### TPGLearn Code Changes
   * TPGLearn.mutate() now ensures the number of root teams will never fall below 5.
   * All instances of increments and decrements moved from TPGLearn to their respective objects' add/remove methods in the Team and Learner classes.
   ### Learner Code Changes
   * Learner.Learner(..) constructor for rebuilding a Learner is now overloaded, accepting either a long action or a Team action.

# UPDATE 11/16/2018: 
   ### TPGLearn Code Changes
   * TPGLearn.initializePopulations() now initializes the Team population to a size of TeamPopSize (was TeamPopSize/2).
   * TPGLearn.initialize() does not use generateNewTeams() to fill out the Team population anymore.
   * TPGLearn.nextEpoch() now calls  TPGLearn.cleanup() first to ensure Root Team population sanity.
   ### Learner Code Changes
   * Learner.Learner(long, Learner) constructor (used for cloning a Learner) now increments a cloned action's Team pointer when applicable.
   * Learner.mutateAction(Action) now decrements a mutated action's Team pointer when applicable.

# Future Additions
   * Add another parameter so that the minimum root teams can be set in the parameters file.

# API Functions:

## TPGAlgorithm

   ### getTPGLearn()

      Retrieves a TPGLearn object after the algorithm has performed its parameter setup.
  
## TPGLearn
  
  ### boolean setActions( long[] acts )
      
      Set the actions available to the Learners during execution. If actions are added 
      successfully, this returns true.
    
  ### boolean initialize()
    
    Used to set up the Team and Learner populations for the current task. Performs the 
    following functions in order:
    
      1. Initialize the Team and Learner populations. Actions are assigned from the action pool. 
      2. Create the first round of Team offspring based on the Team gap.
      3. Set every applicable Team to be a Root Team.
      4. Set the current epoch to 1.
    
    If the initialization is successfully completed, this returns true.
  
  ### int remainingTeams()
    
    Teams still waiting to act wait internally in a Team queue. This method returns the 
    number of Teams which are waiting as an int.
  
  ### long participate( double[] inputFeatures )
  
    This method returns -1 if there are no Teams left to participate. If there are Teams 
    remaining, returns the action suggested by the current participating Team based on 
    the input provided. 
    
  ### long participate( double[] inputFeatures, long[] actions )
  
    This method acts the same as participate(double[]), except after a Team processes the 
    input, it checks to ensure the Team's suggested action is valid by comparing it to the 
    provided actions array. If the Team's suggested action is not valid, this will return 
    a default action of 0. 
  
  ### boolean reward( String label, double reward )
  
    If there is no Team in the queue, this returns false. Otherwise this rewards the Team 
    based on the provided reward value against a task named by the provided label. If your 
    Teams are learning to play multiple games/scenarios, each of those games or scenarios 
    should have a different label.
    
    This method also removes the currently participating Team from the internal queue. 
    
  ### void selection()
  
    Typically called once a training generation is completed. Forces TPG to rank all of the 
    Teams available, then perform selection in order to remove the worst Teams.
    
  ### void generateNewTeams()
  
    Generates a new population of Teams using the current Root Teams. This will always 
    generate a new population, regardless of what's in the current Root Teams list, so 
    if you want only the best Teams to reproduce you should call selection() first.
    
  ### void printStats( int teamCount )
  
    Prints some simple statistics for the top number of teams as determined by the 
    teamCount value. printStats(10) will print the top 10 teams, for example.
    
  ### long nextEpoch():
  
    Advances the algorithm to the next generation of training. This process includes 
    clearing out outcome maps, resetting the Root Teams list, clearing the Team Queue, 
    and removing any Learners which are not currently attached to any Teams. This method 
    then returns the new epoch value as a long integer.
