import java.util.Collections;

public class Retire extends Sim {
	
	public void performRetire(){while (reorderBuffer.size() > 0 && reorderBuffer.peek().isReadyForCommit) {
		Instruction retiring = reorderBuffer.remove();
		
		if (retiring.instrType == InstructionType.HALT) {
			System.out.println("****  HALT Instruction Encountered ****");
			displayAll();
			System.exit(0);
		}

		if (retiring.destination != -1 && retiring.renamedDestination==RAT.get(retiring.destination) ){
			register[retiring.destination] = -1;
			archRegister[retiring.destination] = retiring.destValue;
			freelist.add(retiring.renamedDestination);
			Collections.sort(freelist);
			RAT.put(retiring.destination, retiring.destination);
			RATdecision[retiring.destination] = 0;
			regValidity[retiring.renamedDestination] = false;
		}

		if (retiring.instrType == InstructionType.STORE) {
			memory[retiring.src2Val + retiring.literal] = retiring.src1Val;
		}

		if (retiring.instrType == InstructionType.BZ || retiring.instrType == InstructionType.BNZ) {
			Instruction removed = BIS.remove(0);
			if (removed.address != retiring.address) {
				System.exit(0);
			} else {
			}
		}
	}

	for (Instruction i : reorderBuffer) {
		i.isReadyForCommit = i.writtenBack;
	}
  }
}
