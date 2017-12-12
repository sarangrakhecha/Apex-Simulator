
public class Execute extends Sim {
	
	Util util= new Util();
	public void execute(LoadStore loadstore){

		writeBackLS = nextLSFU;
		nextLSFU = curLSFU;
		curLSFU = loadStoreFU;
		
		if (util.checkLoadStoreQueueForIssue()) {
			loadStoreFU = toLSFU;
			toLSFU.isLSissued = true;
			loadstore.performLoadStore(loadStoreFU);
		} else {
			loadStoreFU = null;
		}
		
		if (nextLSFU != null && nextLSFU.instrType != null) {
			LSFU = nextLSFU.renamedDestination;
			LSFUValue = nextLSFU.destValue;
		} else {
			LSFU = -1;
			LSFUValue = -1;
		}
		
		MulEX = -1;
		MulEXValue = -1;
		
		if (multimer > 0) {
			++multimer;
		}

		if (multimer == 5) {
			multimer = 0;
			writeBackMUL = ExecMultiplyFU;
			ExecMultiplyFU = null;
		} else {
			writeBackMUL = null;
			MulEX = -1;
			MulEXValue = -1;
		}
		
		if (multimer == 4) {
			MulEX = ExecMultiplyFU.renamedDestination;
			MulEXValue = ExecMultiplyFU.destValue;
		}

		if (multimer == 0) {
			if (util.checkIssueQueueForMul()) {

				ExecMultiplyFU = MultiplyFU;
				int operand2 = 0;
				operand2 = ExecMultiplyFU.src2 != -1 ? ExecMultiplyFU.src2Val : ExecMultiplyFU.literal;
				ExecMultiplyFU.destValue = ExecMultiplyFU.src1Val * operand2;
				multimer = 1;
			} else {
				ExecMultiplyFU = null;
			}
		}
		
		branchEX = -1;
		branchEXValue = -1;
		writeBackBranch = ExecBranchFU;
		
		if (util.checkIssueQueueForBranch()) {
			ExecBranchFU = BranchFU;
			switch (ExecBranchFU.instrType) {
			case BAL:
				masterStall = false;
				archRegister[16] = ExecBranchFU.address + 1;
				PCValue = IntExecuteFU.destValue + (ExecBranchFU.literal)/4;
				break;
				
			case JUMP:
				masterStall = false;
				PCValue = ExecBranchFU.src1Val + (ExecBranchFU.literal)/4;
				break;
			default:
				break;
				
			}
		}
		IntEX = -1;
		IntEXValue = -1;
		writeBackInt = IntExecuteFU;
		
		if (util.checkIssueQueueForInt()) {
			IntExecuteFU = IntegerFU;
			switch (IntExecuteFU.instrType) {
			
			case ADD:
					int operand1 = 0;
					operand1 = IntExecuteFU.src2 != -1 ? IntExecuteFU.src2Val : IntExecuteFU.literal;
					IntExecuteFU.destValue = IntExecuteFU.src1Val + operand1;
					if (IntExecuteFU.destValue == 0)
						PSW = 0;
					else
						PSW = -1;
				break;
				
			case SUB:
				int operand2 = 0;
				operand2 = IntExecuteFU.src2 != -1 ? IntExecuteFU.src2Val : IntExecuteFU.literal;
				IntExecuteFU.destValue = IntExecuteFU.src1Val - operand2;
				if (IntExecuteFU.destValue == 0)
					PSW = 0;
				else
					PSW = -1;
				break;
				
			case MOVC:
			case MOV:
				if (IntExecuteFU.instrType == InstructionType.MOVC) {
					IntExecuteFU.destValue = IntExecuteFU.literal;
				} else if (IntExecuteFU.instrType == InstructionType.MOV) {
					IntExecuteFU.destValue = IntExecuteFU.src1Val;
				}
				break;
				
			case AND:
				int operand3 = 0;
				operand2 = IntExecuteFU.src2 != -1 ? IntExecuteFU.src2Val : IntExecuteFU.literal;
				IntExecuteFU.destValue = IntExecuteFU.src1Val & operand3;
				if (IntExecuteFU.destValue == 0)
					PSW = 0;
				else
					PSW = -1;
				break;
				
			case OR:
				int operand4 = 0;
				operand2 = IntExecuteFU.src2 != -1 ? IntExecuteFU.src2Val : IntExecuteFU.literal;
				IntExecuteFU.destValue = IntExecuteFU.src1Val | operand4;
				if (IntExecuteFU.destValue == 0)
					PSW = 0;
				else
					PSW = -1;
				
			case XOR:
				int operand6 = 0;
				operand6 = IntExecuteFU.src2 != -1 ? IntExecuteFU.src2Val : IntExecuteFU.literal;
				IntExecuteFU.destValue = IntExecuteFU.src1Val ^ operand6;
				if (IntExecuteFU.destValue == 0)
					PSW = 0;
				else
					PSW = -1;
				break;
			
			case HALT:
				break;
				
			case BNZ:
			case BZ:
				util.processConditionalBranches(IntExecuteFU);
				break;
				
			
				
			default:
				break;

			}
			if (IntExecuteFU.destination != -1) {
				IntEX = IntExecuteFU.renamedDestination;
				IntEXValue = IntExecuteFU.destValue;
			}
		} else {
			IntExecuteFU = null;
		}
	}
}
