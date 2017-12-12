
public class WriteBack extends Sim {
	
	public void performWriteBack(){

		if (writeBackLS != null && writeBackLS.instrType != null) {
			writeBackLS.writtenBack = true;
			if (writeBackLS.instrType == InstructionType.LOAD) {
				regValidity[writeBackLS.renamedDestination] = true;
				register[writeBackLS.renamedDestination] = writeBackLS.destValue;
			}
		}
		if (writeBackMUL != null && writeBackMUL.instrType != null) {
			writeBackMUL.writtenBack = true;
			regValidity[writeBackMUL.renamedDestination] = true;
			register[writeBackMUL.renamedDestination] = writeBackMUL.destValue;
		}
		if (writeBackBranch != null && writeBackBranch.instrType != null) {
			writeBackBranch.writtenBack = true;
			regValidity[writeBackBranch.renamedDestination] = true;
			register[writeBackBranch.renamedDestination] = writeBackBranch.destValue;
		}
		
		if (writeBackInt != null && writeBackInt.instrType != null) {
			writeBackInt.writtenBack = true;

			switch (writeBackInt.instrType) {
			case HALT:
				break;
			case MOVC:
			case MOV:
			case ADD:
			case SUB:
			case AND:
			case XOR:
			case OR:
					OR();
				    break;
			case BNZ:
			case BZ:
			case BAL:
			case JUMP:
				break;
			default:
				break;
			}
		}
	}
	
	public void OR(){
		register[writeBackInt.renamedDestination] = writeBackInt.destValue;
		regValidity[writeBackInt.renamedDestination] = true;
	}
}
