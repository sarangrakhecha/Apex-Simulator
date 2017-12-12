
 enum InstructionType {
	 					ADD, 
	 					SUB, 
	 					MUL, 
	 					AND, 
	 					OR, 
	 					XOR, 
	 					MOVC, 
	 					MOV, 
	 					LOAD, 
	 					STORE, 
	 					BZ, 
	 					BNZ, 
	 					JUMP, 	
	 					BAL, 
	 					HALT, 
	 					NOP 
		  }
 
public class Instruction {
	
	public Instruction() {
		contains = false;
		writtenBack = false;
		ageInIQ = 0;
		instrType = null;
	}

	public void print() {
		StringBuilder s = new StringBuilder();
		if (instrType != null) {
			s.append("Instruction:" + instrType.toString());
		} else {
			System.out.println("Empty");
			return;
		}
		s.append("Dest:R " + destination + "\tSrc1:R " + src1 + "\tSrc2:R " + src2 + "\tliteral:" + literal
				+ "\tAddress(PC):" + address);
		System.out.println(s);
	}

	public void printRaw() {
		System.out.println(rawString);
	}

	public String toString() {
		if (renamedOne == null)
			return (rawString);
		else {
			return renamedOne;
		}
	}
	
	public boolean contains;
	public InstructionType instrType = null;
	public String instrName;
	public String rawString;
	public String renamedOne = null;
	public int previousStandIn = -1;
	public int previousRATdeciderbit = -1;
	public int address;
	public int FUtype;
	public int ageInIQ;
	public int noOfOperands;
	public int branchTag=-1;
	public int destination = -1;
	public int renamedDestination = -1;
	public int destValue = -1;
	public int src1 = -1, src2 = -1;
	public int renamedSrc1 = -1, renamedSrc2 = -1;
	public int src1Val = -1;
	public int src2Val = -1;
	public int literal = -1;
	public boolean prediction;
	public boolean src1validity, src2validity;
	public boolean destvalid;
	public boolean isReadyForIssue;
	public boolean isReadyForCommit;
	public boolean writtenBack;
	public boolean isLoadStore;
	public boolean isLSissued;
	
}