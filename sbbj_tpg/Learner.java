package sbbj_tpg;

import java.util.ArrayList;

public class Learner
{
	// The number of general purpose registers held by all Learners
	public static final int REGISTERS = 8;
	
	// An array representing the general purpose registers for this Learner
	public double[] registers = null;
	
	// Static variable holding the next ID to be used for a new Learner
	protected static long count = 0;
	
	// Unique ID of this Learner
	protected long ID = 0;
	
	// Time step at which this Learner was generated
	protected long birthday = 0;
	
	// The action held by this Learner
	protected Action action = null;
	
	// The number of Teams currently referencing this Learner
	protected int teamReferenceCount = 0;
	
	// This Learner's program for calculating a bid based on an input
	ArrayList<Instruction> program = new ArrayList<Instruction>();
	
	// Reconstruct a learner with an atomic action: we can build it, we have the technology
	public Learner( long ID, long birthday, long action, int nRefs, ArrayList<Instruction> program )
	{
		this.ID = ID;
		this.birthday = birthday;
		this.action = new Action(action);
		this.teamReferenceCount = nRefs;
		this.program = program;
		
		// Initialize a new set of general purpose registers
		registers = new double[REGISTERS];
	}
	
	// Reconstruct a learner with a Team action: we can build it, we have the technology
	public Learner( long ID, long birthday, Team action, int nRefs, ArrayList<Instruction> program )
	{
		this.ID = ID;
		this.birthday = birthday;
		this.action = new Action(action);
		this.teamReferenceCount = nRefs;
		this.program = program;
		
		// Initialize a new set of general purpose registers
		registers = new double[REGISTERS];
	}
	
	// Create a new learner, storing the time it was made, the action, and its maximum program size
	public Learner( long gtime, long action, int maxProgSize )
	{
		// Grab a unique ID and increment the counter
		ID = count++;
		
		// Today is this Learner's birthday!
		this.birthday = gtime;
		
		// Set this Learner's action to the one provided
		this.action = new Action(action);
		
		// This Learner doesn't belong to any Teams yet
		this.teamReferenceCount = 0;
		
		// Initialize a new set of general purpose registers
		registers = new double[REGISTERS];
		
		// Create a new Instruction variable and make sure it's initialized to null
		Instruction in = null;
		
		// Generate a program up to maxProgSize, with a minimum of 1 instruction.
		int progSize = 1 + ((int)(TPGAlgorithm.RNG.nextDouble() * maxProgSize));
		
		// Randomize progSize many instructions and store them as the current Learner's program
		for( int i=0; i < progSize; i++ )
		{
			// Create a new empty Instruction (binary string of all 0's.
			in = new Instruction();
			
			// There's a 50% chance for each bit to be set to 1.
			// See the Instruction class for default lengths.
			for( int j=0; j < in.size(); j++ )
				if( TPGAlgorithm.RNG.nextDouble() < 0.5 )
					in.flip(j);
			
			// Add the randomly generated instruction to the Learner's program
			program.add(in);
		}
	}
		
	// Create a new uniquely ID'd Learner which is otherwise a copy of another Learner
	public Learner( long gtime, Learner other )
	{
		// Grab a unique ID and increment the counter
		ID = count++;
		
		// Today is this Learner's birthday!
		this.birthday = gtime;
		
		// Copy the other Learner's action
		this.action = other.action;
		
		// This Learner doesn't belong to any Teams yet
		this.teamReferenceCount = 0;
		
		// Initialize a new set of general purpose registers
		registers = new double[REGISTERS];
		
		// Copy the other Learner's program
		for( Instruction in : other.program )
			program.add( new Instruction(in) );
		
		// If the Learner we're copying is holding a pointer to a Team,
		// we have to increment it here because this new Learner is
		// also pointing to that Team.
		if( !this.action.isAtomic() )
			this.action.team.increaseReferences();
	}
	
	// Calculate a bird from the feature set 
	public double bid( double[] inputFeatures )
	{
		// Make sure all the general purpose registers are set to 0.
		// If you want to add simple memory, comment this for loop out!
		//for( int i=0; i < registers.length; i++ )
		//	registers[i] = 0;
		
		// Use the Learner's program to generate a bid and return it.
		// Uses the formula: bid = 1/(1+e^x), where x is the program output.
		// Throw the formula into Wolfram Alpha if you don't know what it looks like.
		return 1 / ( 1 + Math.exp( -run( inputFeatures ) ) );
	}
	
	// Run the program on the given input feature set and return a pre-bid output
	protected double run( double[] inputFeatures )
	{
		Instruction mode;
		Instruction operation;
		
		int destinationRegister;
		double sourceValue;

		// For every instruction in this Learner's program:
		for( Instruction instruction : program )
		{
			// Retrieve the mode register
			mode = instruction.getModeRegister();
			
			// Retrieve the operation register
			operation = instruction.getOperationRegister();
			
			// Retrieve the destination register
			destinationRegister = (int) instruction.getDestinationRegister().getLongValue();
			
			// Mode0 lets an instruction decide between using the input feature set or the general purpose registers
			if( mode.equals( Instruction.mode0 ) )
			{
				sourceValue = registers[ (int) instruction.getSourceRegister().getLongValue() % REGISTERS ];
			}
			else
			{
				sourceValue = inputFeatures[ (int) (instruction.getSourceRegister().getLongValue() % inputFeatures.length) ];
			}
			
			// Perform the appropriate operation
			if( operation.equals( Instruction.SUM ) )
				registers[destinationRegister] += sourceValue;
			else if( operation.equals( Instruction.DIFF ) )
				registers[destinationRegister] -= sourceValue;
			else if( operation.equals( Instruction.PROD ) )
				registers[destinationRegister] *= sourceValue;
			else if( operation.equals( Instruction.DIV ) )
				registers[destinationRegister] /= sourceValue;
			else if( operation.equals( Instruction.COS ) )
				registers[destinationRegister] = Math.cos( sourceValue );
			else if( operation.equals( Instruction.LOG ) )
				registers[destinationRegister] = Math.log( Math.abs(sourceValue) );
			else if( operation.equals( Instruction.EXP ) )
				registers[destinationRegister] = Math.exp( sourceValue );
			else if( operation.equals( Instruction.COND ) )
			{	
				if( registers[destinationRegister] < sourceValue )
					registers[destinationRegister] *= -1;
			}					
			else
			{	
				throw new RuntimeException("Invalid Operation found in Learner.run()");
			}
			
			// If the value of registers[destination] is infinite or not a number, zero it
			if( Double.isInfinite(registers[destinationRegister]) || Double.isNaN(registers[destinationRegister]) )
				registers[destinationRegister] = 0;
		}
		
		// Return the value of the first general purpose register
		return registers[0];		
	}
	
	public int size()
	{
		return program.size();
	}
	
	public long getID()
	{
		return ID;
	}
	
	public Action getActionObject()
	{
		return action;
	}
	
	public long getBirthday()
	{
		return birthday;
	}
	
	// Change this Learner's current action to a new one
	public boolean mutateAction( Action action )
	{
		// Store a copy of the current action
		Action a = this.action;
		
		// Store the new action in this Learner
		this.action = action;
		
		// If we're placing a reference to a Team, make sure we dereference it
		if( !a.isAtomic() )
			a.team.decreaseReferences();
		
		// If the previous action and the new action are different, return true
		return !a.equals(action);
	}
	
	// Perform various mutation operations to this Learner's program
	public boolean mutateProgram( double programDelete, double programAdd, double programSwap, double programMutate, int maxProgramSize )
	{
		boolean changed = false;
		int i = 0;
		int j = 0;
		
		// Choose a random instruction from the program set and remove it.
		if( program.size() > 1 && TPGAlgorithm.RNG.nextDouble() < programDelete )
		{
			i = (int) (TPGAlgorithm.RNG.nextDouble() * program.size());
			program.remove(i);
			
			changed = true;
		}
		
		// Insert a random instruction into the program set.
		if( program.size() < maxProgramSize && TPGAlgorithm.RNG.nextDouble() < programAdd )
		{
			Instruction instruction = Instruction.newRandom();
			
			i = (int) (TPGAlgorithm.RNG.nextDouble() * (program.size()+1) );
			program.add( i, instruction );
			
			changed = true;
		}
		
		// Flip a single bit of a random instruction from the program set.
		if( TPGAlgorithm.RNG.nextDouble() < programMutate )
		{
			i = (int) TPGAlgorithm.RNG.nextDouble() * program.size();
			j = (int) TPGAlgorithm.RNG.nextDouble() * Instruction.INSTRUCTION_SIZE;
			
			program.get(i).flip(j);
			
			changed = true;
		}
		
		// Swap the positions of two instructions in the bid se.
		if( program.size() > 1 && TPGAlgorithm.RNG.nextDouble() < programSwap )
		{
			i = (int) (TPGAlgorithm.RNG.nextDouble() * program.size());
			
			// Keep randomizing a second integer until it's not equal to the first
			do
			{
				j = (int) (TPGAlgorithm.RNG.nextDouble() * program.size());
			}
			while( i == j );
			
			// Swap the two instructions
			Instruction temp = program.get(i);
			program.set(i, program.get(j));
			program.set(j, temp);
				
			changed = true;
		}
		
		// If this Learner's program was mutated, return true
		return changed;		
	}
	
	// Increase the number of references to this Learner and return the new value
	public int increaseReferences()
	{
		return ++teamReferenceCount;
	}
	
	// Decrease the number of references to this Learner and return the new value
	public int decreaseReferences()
	{
		return --teamReferenceCount;
	}
	
	// Return the number of references to this Team
	public int getReferences()
	{
		return teamReferenceCount;
	}
	
	// Return a string representation of this Learner
	public String toString()
	{
		return "[" + ID + " " + action + "]";
	}	
}