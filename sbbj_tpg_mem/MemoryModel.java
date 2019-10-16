package sbbj_tpg_mem;

// A class for representing a probabilistic memory model in TPG.
public class MemoryModel 
{
	// A variable for holding the number of rows for the Memory Model.
	protected int rows = 0;

	// A variable for holding the number of columns for the Memory Model.
	protected int columns = 0;
	
	// A variable for holding the reference to the memory matrix.
	protected double[][] memory = null;
	
	// A variable for holding the reference to the probabilities list.
	protected double[] probabilities = null;
	
	// Create a memory model that holds a rows x columns memory matrix.
	public MemoryModel(int rows, int columns)
	{
		// Store the rows and columns for cleaner code later.
		this.rows = rows;
		this.columns = columns;
		
		// Create the memory matrix.
		memory = new double[rows][columns];
		
		// Generate the list of probabilities. The equation for probability
		// is P_i = 0.25 - (0.001x)^2 where x is the row offset.
		createProbabilities();
	}
	
	// Generate the probabilities list for use with writing.
	protected void createProbabilities()
	{
		// The probabilities are symmetrical and thus
		// we only have to generate them for half of the rows.
		probabilities = new double[rows/2];
		
		// Run the equation and store the result in each index.
		for(int i=0; i < probabilities.length; i++)
			probabilities[i] = 0.25 - Math.pow(0.01 * i, 2.0);									
	}
	
	// Write to the memory model probabilistically.
	public void write(double[] registers)
	{
		// If the registers aren't the same width as the columns, check your parameters!
		if(registers.length != columns)
		{
			System.err.println("MEMORY_MODEL: Registers do not match columns. Why?");
			System.exit(-50);
		}
		
		// We can do two rows at a time, so we only need to iterate rows/2 times.
		for(int i=0; i < (rows-1)/2; i++)
		{
			// Calculate the first and second row indexes.
			int row1 = (rows-1)/2 - i;
			int row2 = (rows-1)/2 + i + 1;
			
			// Iterate through the columns and run the probabilistic memory
			// writes across the entire register set.
			for(int col=0; col < columns; col++)
			{
				// If our RNG is within the write probability, we
				// write the register value to the related column.
				if(TPGAlgorithm.RNG.nextDouble() < probabilities[i])
					memory[row1][col] = registers[col];
				if(TPGAlgorithm.RNG.nextDouble() < probabilities[i])
					memory[row2][col] = registers[col];
			}
		}		
	}
	
	// Treat the memory as a flat array and return the data at the given index.
	public double read(long inputIndex)
	{
		// Ensure the index is within range
		int index = (int)(inputIndex % (rows*columns));
		
		// Turn the index into a row and column.
		int row = index / columns;
		int col = index % columns;
		
		// Return the data stored in memory at this location.
		return memory[row][col];
	}	
}
