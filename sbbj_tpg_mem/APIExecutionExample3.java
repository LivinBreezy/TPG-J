package sbbj_tpg_mem;

public class APIExecutionExample3 
{
	public static void main(String[] args)
	{
		// Example Code execution when interacting with any API:

		// Create a TPG instance with the parameters file and training flag
		TPGAlgorithm tpgAlgorithm = new TPGAlgorithm("16-10-2019-05-03-45_0/generation_12/", "play");

		// Grab the TPG learning interface from the wrapper object
		TPGPlay tpg = tpgAlgorithm.getTPGPlay();
		
		// Set some data boundaries for later
		int dataCount = 1000;
		int dataLength = 25;

		// Create an array for holding features
		double[] inputFeatures = null;
		
		// Create a variable for the number of iterations
		int numberOfIterations = 5000; 
		
		// Keep a count of the number of games to play (learning dimensions)
		int gamesToPlay = 1;

		// Create testing set
		double[][] input = new double[dataCount][dataLength];
		
		for( int i=0; i < dataCount; i++ )
			input[i][(int)(Math.random()*dataLength)] = 1.0;
						
		// Main Learning Loop
		for( int i=0; i < numberOfIterations; i++ )
		{
			// Keep a total outcome value.
			double finalOutcome = 0.0;
			
			for( int j=0; j < gamesToPlay; j++ )
			{
				// For simulation only. See the big comment below.
				int count = dataCount;

				// This while loop would normally be while( game.episode_still_running() ), but
				// I don't have a game to simulate for you, so here I'm simply saying that each game
				// runs for 10 "frames" before stopping.
				while( count > 0 )
				{
					// Get the next input "frame"
					inputFeatures = input[count-1];
					
					// Accumulate an outcome by getting TPG to play. The participate 
					// method accepts a list of features and then produces an action in
					// the form of a long integer. Use that to act on the environment.
					// Be careful how you set up your actions, though, as they have to
					// match the conditions under which your agent was trained.
					double outcome = actOnEnvironment( tpg.participate( inputFeatures ) );
					
					// After this, you should probably use the outcome for something, such
					// as gathering statistics about gameplay. You could do that here, or
					// later if you store the data somewhere accessible.
					finalOutcome += outcome;
					
					// Counting down frames for testing purposes
					count--;
				}
			}
			
			// Print the total outcome. Why not?
			System.out.println(finalOutcome);
			
			// Print some stats as defined by TPGPlay.
			tpg.printStats(10);
		}
	}
	
	// Normally this function would be defined by the game API, but if you're using
	// TPG for classification or some other task, this can be used to log TPG's output
	// against the current input (or just don't use it, depending on your needs)
	public static double actOnEnvironment( long action )
	{
		// Return the action multiplied by 1000 * a value in [0.0,1.0) for a simple reward simulation.
		// Adjust this reward to see varying TPG growth behaviour. For example, setting this to an
		// action multiplied by some constant will see TPG learning to spit out large actions all the time.
		return action * 1000 * Math.random();
	}
}
