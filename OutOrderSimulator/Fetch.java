
public class Fetch extends Sim {
	
	Util util= new Util();
	public void performFetch(){

		if (isItEnd && stalled)
			return;

		if (stalled)
			return;

		actualFetchtoNext = actualFetch;
		if (isItEnd)
			actualFetch = null;

		actualFetch = util.setInstrBeans(instructions[PCValue - 4000]);
		if (instructions[PCValue - 4000 + 1].contains) {
			PCValue++;
		} else {
			if (!isItEnd) {
				PCValue++;
				isItEnd = true;
			} 
		}
	}
}
