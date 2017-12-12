
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class Util extends Sim {
	
	
	public void shiftLSQ() {
		loadStoreQueue[0] = loadStoreQueue[1];
		loadStoreQueue[1] = loadStoreQueue[2];
		loadStoreQueue[2] = null;
	}
	
	public void processConditionalBranches(Instruction instruction) {
		boolean taken = false;
		if (instruction.instrType == InstructionType.BZ) {
			taken = (instruction.src1Val == 0) ? true : false;
		}
		if (instruction.instrType == InstructionType.BNZ) {
			taken = (instruction.src1Val != 0) ? true : false;
		}
		if (taken == instruction.prediction) {
		} else {
			if (instruction.instrType == InstructionType.BZ) {
				if (instruction.src1Val == 0) {
					PCValue = instruction.address + (instruction.literal)/4;
				} else {
					PCValue = instruction.address + 1;
				}
			}

			if (instruction.instrType == InstructionType.BNZ) {

				if (instruction.src1Val != 0) {
					PCValue = instruction.address + (instruction.literal)/4;
					masterStall=false;
				} else {
					PCValue = instruction.address + 1;
				}
			}
			flushProcessorAfterMisprediction(instruction);
		}
	}

	
	
	
	
	public void updateIssueQueue() {
		Instruction instruction = null;
		IntegerFU = null;
		toLSFU = null;
		MultiplyFU = null;
		BranchFU = null;
		for (int i = 0; i < 12; i++) {
			instruction = issueQueue[i];
			if (instruction != null) {
				if (instruction.src1 != -1 && !instruction.src1validity && checkForwardedPaths(instruction.renamedSrc1)) {
					instruction.src1Val = forwardResult;
					instruction.src1validity = true;
				}
				if (instruction.src2 != -1 && !instruction.src2validity && checkForwardedPaths(instruction.renamedSrc2)) {
					instruction.src2Val = forwardResult;
					instruction.src2validity = true;
				}
			}
		}

		instruction = null;
		for (int i = 0; i < 12; i++) {
			instruction = issueQueue[i];
			if (instruction != null) {
				++instruction.ageInIQ;
				instruction.isReadyForIssue = true;
				if (instruction.src1 != -1)
					instruction.isReadyForIssue &= instruction.src1validity;
				if (instruction.src2 != -1)
					instruction.isReadyForIssue &= instruction.src2validity;
			}
		}
		for (int i = 0; i < 3; i++) {
			if (loadStoreQueue[i] != null)
				++loadStoreQueue[i].ageInIQ;
		}
	}

	
	
	public void insertintoBIS(Instruction instruction) {
		BIS.push(instruction);
		return;

	}

	
	
	public boolean checkDecodeStall(Instruction instruction) {
		if (instruction != null) {
			if (IQ_Size == 12 || reorderBuffer.size() == 40) {
				return true;

			} else if (instruction.destination != -1 && freelist.size() == 0) {
				return true;
			}

			if (instruction.isLoadStore) {
				if (LSQ_Size == 3) {
					return tryFreeingUpLSQ();
				}
			}
		}
		return false;
	}
	
	

	public void flushProcessorAfterMisprediction(Instruction conditionalInstruction) {
		actualFetch = null;
		actualFetchtoNext = null;
		actualDecode = null;
		Instruction instruction;

		while (conditionalInstruction.branchTag <= topBIS) {
			for (int i = 0; i < 12; i++) {
				if (i < 3) {
					if (loadStoreQueue[i] != null && loadStoreQueue[i].branchTag == topBIS) {
						loadStoreQueue[i] = null;
						LSQ_Size--;
					}
				}

				if (issueQueue[i] != null && issueQueue[i].branchTag == topBIS) {
					issueQueue[i] = null;
					IQ_Size--;
				}
			}
			--topBIS;
		}
		topBIS = conditionalInstruction.branchTag;

		if (ExecMultiplyFU != null) {
			if (ExecMultiplyFU.branchTag >= topBIS)
				ExecMultiplyFU = null;
		}
		if (loadStoreFU != null) {
			if (loadStoreFU.branchTag >= topBIS)
				loadStoreFU = null;
		}
		if (curLSFU != null) {
			if (curLSFU.branchTag >= topBIS)
				curLSFU = null;
		}
		if (nextLSFU != null) {
			if (nextLSFU.branchTag >= topBIS)
				nextLSFU = null;
		}

		if (ExecBranchFU != null) {
			if (ExecBranchFU.branchTag >= topBIS)
				ExecBranchFU = null;
		}
		while (!BIS.isEmpty() && BIS.peek().branchTag > conditionalInstruction.branchTag) {
		}
		reInItRATandAllocatedList();

		Queue<Instruction> newReorderBuffer = new LinkedList<Instruction>();
		while (!reorderBuffer.isEmpty() && reorderBuffer.peek().branchTag < conditionalInstruction.branchTag) {
			instruction = reorderBuffer.remove();
			if (instruction.destination != -1) {
				RAT.put(instruction.destination, instruction.renamedDestination);
				RATdecision[instruction.destination] = 1;
				freelist.remove(instruction.renamedDestination);
			}
			newReorderBuffer.add(instruction);

		}
		newReorderBuffer.add(conditionalInstruction);
		reorderBuffer = newReorderBuffer;
	}

	
	
	
	public void removeLoadStoreFromIssueQueue() {
		Instruction instruction = null;
		for (int i = 0; i < 12; i++) {
			instruction = issueQueue[i];
			if (instruction != null && instruction.isLoadStore && instruction.isReadyForIssue) {
				issueQueue[i] = null;
				--IQ_Size;
			}
		}
	}	
	
	
	public void putIntoLSQ(Instruction instruction) {
		for (int i = 0; i < 3; i++) {
			if (loadStoreQueue[i] == null) {
				loadStoreQueue[i] = instruction;
				++LSQ_Size;
				return;
			}
		}
		System.exit(1);
	}

	
	public boolean checkForwardedPaths(int registerToLookup) {
		if (registerToLookup == IntEX) {
			forwardResult = IntEXValue;
			return true;
		} else if (registerToLookup == MulEX) {
			forwardResult = MulEXValue;
			return true;
		} else if (registerToLookup == LSFU) {
			forwardResult = LSFUValue;
			return true;
		} else if (registerToLookup == LSQ) {
			forwardResult = LSQValue;
			return true;
		} 
		if (registerToLookup == branchEX) {
			forwardResult = branchEXValue;
			return true;
		} else {
			return false;
		}
	}
	
	
	
	
	public static void reInItRATandAllocatedList() {
		HashMap<Integer, Integer> newRAT = new HashMap<Integer, Integer>();
		for (int i = 0; i < 16; i++) {
			newRAT.put(i, i);
		}
		freelist.clear();
		for (int i = 0; i < URF_SIZE; i++) {
			freelist.add(i);
		}
		RAT = newRAT;
		for (int i = 0; i < 16; i++) {
			RATdecision[i] = 0;
		}
	}
	
	
	
	
	
	public boolean checkLoadStoreQueueForIssue() {
		LSQ = -1;
		LSQValue = -1;
		boolean yesFrom0to2 = false, yesFrom1to2 = false;
		toLSFU = null;

		if (loadStoreQueue[0] != null && !loadStoreQueue[0].isLSissued && loadStoreQueue[0].isReadyForIssue
				&& loadStoreQueue[0].ageInIQ > IQ_span) {
			loadStoreQueue[0].isLSissued = true;
			toLSFU = loadStoreQueue[0];
			return true;
		}

		if (loadStoreQueue[0] != null && loadStoreQueue[1] != null && !loadStoreQueue[1].isLSissued
				&& loadStoreQueue[1].isReadyForIssue && loadStoreQueue[1].ageInIQ > IQ_span) {
			if (loadStoreQueue[1].instrType == InstructionType.LOAD) {
				if (loadStoreQueue[0].instrType == InstructionType.STORE) {

					if (loadStoreQueue[0].src2validity) {
						if ((loadStoreQueue[1].src1Val + loadStoreQueue[1].literal) != (loadStoreQueue[0].src2Val
								+ loadStoreQueue[0].literal)) {
							toLSFU = loadStoreQueue[1];
							loadStoreQueue[1].isLSissued = true;
							return true;
						} else {
							if (loadStoreQueue[0].src1validity) {
								doforwarding(loadStoreQueue[0], loadStoreQueue[1]);
							} else {
							}
						}
					}
					else {
					}
				}
				else {
					toLSFU = loadStoreQueue[1];
					loadStoreQueue[1].isLSissued = true;
					return true;

				}
			}
			else {
				if (loadStoreQueue[0].instrType == InstructionType.STORE) {
					if (loadStoreQueue[1].isReadyForIssue) {
						toLSFU = loadStoreQueue[1];
						loadStoreQueue[1].isLSissued = true;
						return true;
					}

				}
			}
		}

		if (loadStoreQueue[0] != null && loadStoreQueue[1] != null && loadStoreQueue[2] != null
				&& !loadStoreQueue[2].isLSissued && loadStoreQueue[2].isReadyForIssue
				&& loadStoreQueue[2].ageInIQ > IQ_span) {
			if (loadStoreQueue[2].instrType == InstructionType.LOAD) {
				if (loadStoreQueue[0].instrType == InstructionType.LOAD
						&& loadStoreQueue[1].instrType == InstructionType.LOAD) {

					toLSFU = loadStoreQueue[2];
					loadStoreQueue[2].isLSissued = true;
					return true;

				} else if (loadStoreQueue[0].instrType == InstructionType.STORE) {
					if (loadStoreQueue[0].src2validity) {
						if ((loadStoreQueue[0].src2Val + loadStoreQueue[0].literal) != (loadStoreQueue[2].src1Val
								+ loadStoreQueue[2].literal)) {
							yesFrom0to2 = true;
						}
						else {
							if (loadStoreQueue[0].src1validity) {
								doforwarding(loadStoreQueue[0], loadStoreQueue[2]);

							} else {
							}
						}
					} else {
					}

				}

				if (loadStoreQueue[1].instrType == InstructionType.STORE) {
					if (loadStoreQueue[1].src2validity) {
						if ((loadStoreQueue[1].src2Val + loadStoreQueue[1].literal) != (loadStoreQueue[2].src1Val
								+ loadStoreQueue[2].literal)) {
							yesFrom1to2 = true;
						}
						else {
							if (loadStoreQueue[1].src1validity) {
								doforwarding(loadStoreQueue[1], loadStoreQueue[2]);

							} else {
							}
						}
					} else {
					}

				}
				if (loadStoreQueue[0].instrType == InstructionType.STORE
						&& loadStoreQueue[1].instrType == InstructionType.STORE) {
					if (yesFrom0to2 && yesFrom1to2) {
						toLSFU = loadStoreQueue[2];
						loadStoreQueue[2].isLSissued = true;
						return true;
					}
				}
			}
			else {
			}
		}

		toLSFU = null;
		return false;
	}

	

	public void doforwarding(Instruction from, Instruction to) {
		to.destValue = from.src1Val;
		to.isLSissued = true;
		to.writtenBack = true;
		register[to.renamedDestination] = to.destValue;
		LSQ = to.renamedDestination;
		LSQValue = to.destValue;
	}
	
	
	
	
	
	public void removeLoadOnly() {
		while (loadStoreQueue[0] != null && loadStoreQueue[0].instrType == InstructionType.LOAD
				&& loadStoreQueue[0].isLSissued) {
				shiftLSQ();
			--LSQ_Size;
		}
		while (loadStoreQueue[1] != null && loadStoreQueue[1].instrType == InstructionType.LOAD
				&& loadStoreQueue[1].isLSissued) {
			loadStoreQueue[1] = loadStoreQueue[2];
			loadStoreQueue[2] = null;
			--LSQ_Size;
		}
		if (loadStoreQueue[2] != null && loadStoreQueue[2].instrType == InstructionType.LOAD
				&& loadStoreQueue[2].isLSissued) {
			loadStoreQueue[2] = null;
			--LSQ_Size;
		}

	}


	
	
	
	public void renameReadPutintoIQnROB(Instruction instruction) {
		StringBuilder sb = new StringBuilder();
		sb.append(instruction.instrName + " ");
		if (instruction.src1 != -1) {
			instruction.renamedSrc1 = RAT.get(instruction.src1);
			if (RATdecision[instruction.src1] == 0) {
				instruction.src1Val = archRegister[instruction.renamedSrc1];
				instruction.src1validity = true;
			} else {
				if (regValidity[instruction.renamedSrc1] == true) {
					instruction.src1Val = register[instruction.renamedSrc1];
					instruction.src1validity = true;
				}

				else if (checkForwardedPaths(instruction.renamedSrc1)) {
					instruction.src1Val = forwardResult;
					instruction.src1validity = true;
				} else {
					instruction.src1validity = false;
				}
			}
		}
		if (instruction.src2 != -1) {
			instruction.renamedSrc2 = RAT.get(instruction.src2);
			if (RATdecision[instruction.src2] == 0) {
				instruction.src2Val = archRegister[instruction.renamedSrc2];
				instruction.src2validity = true;
			} else {
				if (regValidity[instruction.renamedSrc2] == true) {
					instruction.src2Val = register[instruction.renamedSrc2];
					instruction.src2validity = true;
				} else if (checkForwardedPaths(instruction.renamedSrc2)) {
					instruction.src2Val = forwardResult;
					instruction.src2validity = true;
				} else {
					instruction.src2validity = false;
				}
			}
		}

		if (instruction.destination != -1) {
			int x = freelist.remove(0);

			regValidity[x] = false;
			int currentStandin = RAT.get(instruction.destination);
			instruction.previousStandIn = currentStandin;
			instruction.previousRATdeciderbit = RATdecision[instruction.destination] == 0 ? 0 : 1;
			RAT.put(instruction.destination, x);
			RATdecision[instruction.destination] = 1;
			instruction.renamedDestination = x;
			sb.append("P" + instruction.renamedDestination);
		}
		if (instruction.src1 != -1) {
			if (RATdecision[instruction.src1] != 0)
				sb.append(" P" + instruction.renamedSrc1);
			else {
				sb.append(" (" + instruction.src1Val + ")");
			}
		}
		if (instruction.src2 != -1) {
			if (RATdecision[instruction.src2] != 0)
				sb.append(" P" + instruction.renamedSrc2);
			else {
				sb.append(" (" + instruction.src2Val + ")");
			}
		}
		if (instruction.literal != -1) {
			sb.append(" " + instruction.literal);
		}
		instruction.renamedOne = sb.toString();
		instruction.isReadyForIssue = true;
		if (instruction.src1 != -1)
			instruction.isReadyForIssue = instruction.src1validity;
		if (instruction.src2 != -1)
			instruction.isReadyForIssue &= instruction.src2validity;

		if (instruction.instrType == InstructionType.BZ || instruction.instrType == InstructionType.BNZ) {
			BIS.push(instruction);
			++topBIS;
		}
		instruction.branchTag = topBIS;

		for (int i = 0; i < 12; i++) {
			if (issueQueue[i] == null && instruction.instrType != null) {
				issueQueue[i] = instruction;
				IQ_Size++;
				break;
			}

		}

		if (instruction.instrType == InstructionType.LOAD || instruction.instrType == InstructionType.STORE) {
			putIntoLSQ(instruction);
		}
		if (instruction.instrType != null)
			reorderBuffer.add(instruction);

	}


	public boolean checkIssueQueueForInt() {
		IntegerFU = null;
		Instruction instruction;
		for (int i = 0; i < 12; i++) {
			instruction = issueQueue[i];
			if (instruction != null) {
				if (instruction.isReadyForIssue && instruction.FUtype == 0 && instruction.ageInIQ > IQ_span) {
					IntegerFU = issueQueue[i];
					issueQueue[i] = null;
					IQ_Size--;
					return true;
				}
			}
		}
		return false;
	}
	
	
	public boolean checkIssueQueueForMul() {
		MultiplyFU = null;
		Instruction instruction;
		for (int i = 0; i < 12; i++) {
			instruction = issueQueue[i];
			if (instruction != null) {
				if (instruction.isReadyForIssue && instruction.FUtype == 1 && instruction.ageInIQ > IQ_span) {
					MultiplyFU = issueQueue[i];
					issueQueue[i] = null;
					IQ_Size--;
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean checkIssueQueueForBranch() {
		BranchFU = null;
		Instruction instruction;
		for (int i = 0; i < 12; i++) {
			instruction = issueQueue[i];
			if (instruction != null) {
				if (instruction.isReadyForIssue && instruction.FUtype == 3 && instruction.ageInIQ > IQ_span) {
					BranchFU = issueQueue[i];
					issueQueue[i] = null;
					IQ_Size--;
					return true;
				}
			}
		}
		return false;
	}

	
	
	
	public void forwardCheckDuringDecodeStall(Instruction instruction) {
		if (instruction.src1 != -1 && checkForwardedPaths(instruction.renamedSrc1)) {
			instruction.src1Val = forwardResult;
			instruction.src1validity = true;
		}
		if (instruction.src2 != -1 && checkForwardedPaths(instruction.renamedSrc2)) {
			instruction.src2Val = forwardResult;
			instruction.src2validity = true;
		}
	}

	
	
	public boolean tryFreeingUpLSQ() {
		if (LSQ_Size > 3 || LSQ_Size < 0) {
			System.exit(1);
		}
		if (loadStoreQueue[0] != null && loadStoreQueue[0].instrType == InstructionType.LOAD) {
			if (loadStoreQueue[0].isLSissued) {
				loadStoreQueue[0] = null;
				--LSQ_Size;
				shiftLSQ();
				return false;

			}
		} else if (loadStoreQueue[0] != null && loadStoreQueue[0].instrType == InstructionType.STORE) {
			if (loadStoreQueue[0].isReadyForCommit) {
				loadStoreQueue[0] = null;
				--LSQ_Size;
				shiftLSQ();
				return false;
			}
		}
		return true;
	}

	
	public Instruction setInstrBeans(Instruction instruction) {
		if (instruction == null || instruction.instrType == null) {
			return null;
		}
		Instruction newinst = new Instruction();
		newinst.rawString = new String(instruction.rawString);
		newinst.isLoadStore = instruction.isLoadStore;
		newinst.FUtype = instruction.FUtype;
		newinst.address = instruction.address;
		newinst.instrName = instruction.instrName;
		newinst.instrType = instruction.instrType;
		newinst.src1 = instruction.src1;
		newinst.src2 = instruction.src2;
		newinst.destination = instruction.destination;
		newinst.literal = instruction.literal;

		return newinst;
	}
}
