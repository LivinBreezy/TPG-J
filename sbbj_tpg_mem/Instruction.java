package sbbj_tpg_mem;

import java.util.BitSet;

/**
 * This class is designed to add SBB-specific functionality to the existing BitSet class.
 * It functions identically aside from new constructors and the ability to split an Instruction
 * up into smaller parts for easier data management.<p>
 * 
 * Instructions are meant to be used to represent Learners. For more detailed information on how
 * Learners use this class, refer to the Learner documentation.
 * @author Robert Smith
 *
 */
public class Instruction extends BitSet 
{
	private static final long serialVersionUID = -4790942018324759456L;
	
	/**
	 * The number of bits to be contained within the binary string of an Instruction.
	 * The default value is 33 (24 + 3 + 4 + 2).
	 */
	public static final int INSTRUCTION_SIZE = 24 + 3 + 4 + 2;
	
	/**
	 * An instruction that represents the bits used to determine the mode area of
	 * an instruction. The default value is 0x3 (0 0000 0000 0000 0000 0000 0000 0000 0011).
	 */
	public static final Instruction modeMask = new Instruction( 0x3 );   
	
	/**
	 * An instruction that represents the bits used to determine the operation area of
	 * an instruction. The default value is 0x3C (0 0000 0000 0000 0000 0000 0000 0011 1100).
	 */
	public static final Instruction opMask = new Instruction( 0x3C );     
	
	/**
	 * An instruction that represents the bits used to determine the mode area of
	 * an instruction. The default value is 0x1C0 (0 0000 0000 0000 0000 0000 0001 1100 0000).
	 */
	public static final Instruction dstMask = new Instruction( 0x70 );   
	
	/**
	 * An instruction that represents the bits used to determine the source area of
	 * an instruction. The default value is 0x1FFFFFE00 (1 1111 1111 1111 1111 1111 1110 0000 0000).
	 */
	public static final Instruction srcMask = new Instruction( 0x1FFFFFE00L ); 

	// Modes
	
	// REGISTER-REGISTER
	// Rx <- op Rx Ry
	public static final Instruction mode0 = new Instruction( 0x0 );
	public static final int mode0_VALUE = 0;
	
	// INPUT-REGISTER
	// Rx <- op Rx Iy
	public static final Instruction mode1 = new Instruction( 0x1 ); 
	public static final int mode1_VALUE = 1;
	
	// MEMORY-REGISTER
	// Rx <- op Rx My
	public static final Instruction mode2 = new Instruction( 0x2 );
	public static final int mode2_VALUE = 2;
	
	public static final int MODE_COUNT = 9;
		
	// Operations
	public static final Instruction SUM = new Instruction( 0x0 );
	public static final Instruction DIFF = new Instruction( 0x1 );
	public static final Instruction PROD = new Instruction( 0x2 );
	public static final Instruction DIV = new Instruction( 0x3 );
	public static final Instruction COS = new Instruction( 0x4 );
	public static final Instruction LOG = new Instruction( 0x5 );
	public static final Instruction EXP = new Instruction( 0x6 );
	public static final Instruction COND = new Instruction( 0x7 );
	public static final Instruction WRIT = new Instruction( 0x8 );
	public static final int SUM_VALUE = 0;
	public static final int DIFF_VALUE = 1;
	public static final int PROD_VALUE = 2;
	public static final int DIV_VALUE = 3;
	public static final int COS_VALUE = 4;
	public static final int LOG_VALUE = 5;
	public static final int EXP_VALUE = 6;
	public static final int COND_VALUE = 7;
	public static final int WRIT_VALUE = 8;
	
	public static final int OPERATION_COUNT = 9;
	
	// Shift Amounts
	public static final short MODE_SHIFT = 0;
	public static final short OP_SHIFT = (short)(modeMask.cardinality());
	public static final short DEST_SHIFT = (short)(modeMask.cardinality() + opMask.cardinality());
	public static final short SRC_SHIFT = (short)(modeMask.cardinality() + opMask.cardinality() + dstMask.cardinality());
		
	/**
	 * Creates a new instruction of a size defined by INSTRUCTION_SIZE.
	 */
	public Instruction()
	{
		super( INSTRUCTION_SIZE );
	}
	
	/**
	 * Creates a new instruction of a size defined by INSTRUCTION_SIZE with a value equal to the provided parameter.
	 * @param value the value to set this instruction to initially.
	 */
	public Instruction( long value )
	{
		super( INSTRUCTION_SIZE );
		this.or( BitSet.valueOf( new long[]{value} ) );		
	}
	
	/**
	 * Creates a new instruction from a provided instruction so that they are an exact copy.
	 * @param other the instruction to be copied.
	 */
	public Instruction( Instruction other )
	{
		super( INSTRUCTION_SIZE );
		
		for( int i=0; i < INSTRUCTION_SIZE; i++ )
			if( other.get(i) )
				this.set(i, true);
	}
	
	/**
	 * Creates a new instruction from a provided bitset so that they are an exact copy.
	 * @param other the instruction to be copied.
	 */
	public Instruction( BitSet other )
	{
		super( INSTRUCTION_SIZE );
		
		for( int i=0; i < other.size(); i++ )
			if( other.get(i) )
				this.set(i, true);
	}
	
	/**
	 * Retrieves some section of this Instruction's binary string based on a provided mask.
	 * @param mask the mask to choose which bits to retrieve before the shift.
	 * @param shift the amount to bit shift the binary string by after the mask is applied. 
	 * @return the created instruction based on the mask and shift.
	 */
	public Instruction getRegister( Instruction mask, int shift )
	{
		Instruction temp = (Instruction) this.clone();
		
		temp.and( mask );

		temp = new Instruction( temp.get( shift, INSTRUCTION_SIZE ) );
		
		return temp;
	}
	
	/**
	 * Retrieves the Mode section of this instruction and returns it as another Instruction.
	 * @return the Mode section of this binary string as an Instruction.
	 */
	public Instruction getModeRegister()
	{
		return this.getRegister( modeMask, MODE_SHIFT );
	}
	
	/**
	 * Retrieves the Operation section of this instruction and returns it as another Instruction.
	 * @return the Operation section of this binary string as an Instruction.
	 */
	public Instruction getOperationRegister()
	{
		return this.getRegister( opMask, OP_SHIFT );
	}
	
	/**
	 * Retrieves the Destination section of this instruction and returns it as another Instruction.
	 * @return the Destination section of this binary string as an Instruction.
	 */
	public Instruction getDestinationRegister()
	{
		return this.getRegister( dstMask, DEST_SHIFT );
	}
	
	/**
	 * Retrieves the Source section of this instruction and returns it as another Instruction.
	 * @return the Source section of this binary string as an Instruction.
	 */
	public Instruction getSourceRegister()
	{
		return this.getRegister( srcMask, SRC_SHIFT );
	}

	/**
	 * Returns this instruction in its binary string representation.
	 * @return the binary string representation of this object.
	 */
	public String toBinaryString()
	{
		String temp = "";
		
		for( int i=INSTRUCTION_SIZE-1; i > -1; i-- )
			 temp += this.get(i) ? 1 : 0;	
		
		return temp;
	}
	
	/**
	 * Returns a randomly created Instruction of size INSTRUCTION_SIZE.
	 * @return a randomly created Instruction of size INSTRUCTION_SIZE.
	 */
	public static Instruction newRandom()
	{
		Instruction instruction = new Instruction();
		
		for( int j=0; j < INSTRUCTION_SIZE; j++ )
		{
			if( TPGAlgorithm.RNG.nextDouble() < 0.5 )
				instruction.flip(j);
		}
		
		return instruction;		
	}
	
	/**
	 * Returns the maximum binary string length of this instruction.
	 */
	public int size()
	{
		return INSTRUCTION_SIZE;
	}
	
	/**
	 * Returns this Instruction as a long value.
	 */
	public long getLongValue()
	{
		long[] temp = this.toLongArray();
		
		if( temp.length == 0 )
			return 0;
		else
			return temp[0];
	}
	
	/**
	 * Returns the numerical value of this instruction as a String.
	 * @return the numerical value of this instruction.
	 */
	public String toString()
	{
        if( this.toLongArray().length == 0 )
        	return "";
        
		return Long.toString( this.toLongArray()[0], 2 );
	}
}
