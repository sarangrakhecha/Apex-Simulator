
public class Decode extends Sim {
	
	Util util= new Util();
	public void perfromDecode(){
		
		if (stalled)
			stalled = util.checkDecodeStall(actualDecode);
		
		if (!stalled) {
			actualDecode = actualFetchtoNext;
		
			if (actualDecode != null) {
				stalled = util.checkDecodeStall(actualDecode);
			
				if (!stalled) {
					util.renameReadPutintoIQnROB(actualDecode);
					if (actualDecode.instrType == InstructionType.JUMP || actualDecode.instrType == InstructionType.BAL) {
						actualFetch = null;
						actualFetchtoNext = null;
						masterStall = true;
					}
				
					if (actualDecode.instrType == InstructionType.BZ || actualDecode.instrType == InstructionType.BNZ) {
						if (actualDecode.literal > 0) {
							actualDecode.prediction = false;
						}
					
						else {
							actualDecode.prediction = true;
							PCValue = actualDecode.address + (actualDecode.literal)/4;
							actualFetch = null;
							actualFetchtoNext = null;
						}
					}
				}
			}
		} else {
			if (actualDecode != null && actualDecode.instrType != null)
				util.forwardCheckDuringDecodeStall(actualDecode);
			return;
		}
	}
}
