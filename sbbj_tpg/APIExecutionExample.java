package sbbj_tpg;

public class APIExecutionExample 
{
	public static void main(String[] args)
	{
		// Example Code execution when interacting with any API:

		// Create a TPG instance with the parameters file and training flag
		TPGAlgorithm tpgAlgorithm = new TPGAlgorithm("parameters.arg", "learn");

		// Grab the TPG learning interface from the wrapper object
		TPGLearn tpg = tpgAlgorithm.getTPGLearn();

		// Get the action pool from the API and give it to TPG in the form of a long array (long[])
		tpg.setActions( new long[] {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L} );

		// Run the initialize method to create Team/Learner populations and prep for beginning learning
		tpg.initialize();

		// Create a variable for holding reward
		double reward = 0.0;
		
		// Create an array for holding features
		double[] inputFeatures = null;
		
		// Create a variable for the number of iterations
		int numberOfIterations = 1000; 
		
		// Keep a count of the number of games to play (learning dimensions)
		int gamesToPlay = 1;
		
		// Main Learning Loop
		for( int i=0; i < numberOfIterations; i++ )
		{
			for( int j=0; j < gamesToPlay; j++ )
			{
				// Let every Team play the current game once
				while( tpg.remainingTeams() > 0 )
				{
					// For simulation only. See the big comment below.
					int count = 10;
					
					// Reset the reward to 0.
					reward = 0.0;
					
					// This while loop would normally be while( game.episode_still_running() ), but
					// I don't have a game to simulate for you, so here I'm simply saying that each game
					// runs for 10 "frames" before offering reward and moving to the next Team.
					while( count > 0 )
					{
						// Convert the gameState to a double[] somehow. This is a 5 feature space. A very small frame.
						inputFeatures = new double[]{1.0, 2.1, 3.2, 4.3, 5.4};
						
						// Accumulate the reward by getting TPG to play. TPG receives the input features,
						// then returns an action label, which is enacted on the environment. The environment
						// then returns a reward which can be applied immediately or stored for later use,
						// depending on what you want the algorithm to do.
						reward += actOnEnvironment( tpg.participate( inputFeatures ) );
						
						// Counting down frames for testing purposes
						count--;
					}
					
					// Reward the current Team. This automatically rotates the current Team.
					// The "game" string should be unique to the game the Team just played.
					// In single-game learning just make it static, but when you move on to
					// playing multiple games, you'll need to make sure the labels are correct.
					tpg.reward( "game", reward );
				}
			}
			
			// Print the current top 10 Team population outcomes and some simple environment values
			tpg.printStats(10);
			
			// Tell TPG to Perform Selection
			tpg.selection();
			
			// Tell TPG to Reproduce and Mutate with the current Teams
			tpg.generateNewTeams();
			
			// Reset TPG so it increases the generation count and finds the new Root Teams
			tpg.nextEpoch();
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
