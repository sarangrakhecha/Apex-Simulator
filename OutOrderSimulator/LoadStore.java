
public class LoadStore extends Sim{
	
	public void performLoadStore(Instruction instruction){

		if (instruction.instrType == InstructionType.LOAD) {
			if (instruction.literal == -1) {

				instruction.destValue = memory[instruction.src1Val + instruction.src2Val];
			} else {

				instruction.destValue = memory[instruction.src1Val + instruction.literal];
			}
		}
		else if (instruction.instrType == InstructionType.STORE) {
			if (instruction.literal == -1) {

			} else {

			}
		}
	}
}
