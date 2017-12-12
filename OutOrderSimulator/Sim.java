
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;
import java.util.StringTokenizer;

public class Sim {
	
	public static String filename;
	public static final int IQ_span = 2;
	public static int PSW = -1;
	public static Util util;
	
	public static int PCValue;
	public static int LSQ_Size;
	public static int IQ_Size;
	public static Queue<Instruction> reorderBuffer;
	public static List<Integer> freelist;
	public static Map<Integer,Integer> RAT;
	
	public static Instruction actualFetch = null, actualDecode = null;
	public static Instruction IntegerFU = null, MultiplyFU = null;
	public static Instruction IntExecuteFU = null, ExecMultiplyFU = null; 
	public static Instruction BranchFU = null, ExecBranchFU = null, writeBackBranch = null;
	public static Instruction loadStoreFU = null, curLSFU = null;
	public static Instruction nextLSFU = null, writeBackInt = null;
	public static Instruction writeBackMUL = null, writeBackLS = null, toLSFU = null;
	
	public static int archRegister[];
	public static int register[];
	public static boolean regValidity[];
	public static int memory[];
	public static Instruction[] instructions;
	public static Instruction[] loadStoreQueue;
	public static Instruction[] issueQueue;
	public static int[] RATdecision; 
	
	
	public static Stack<Instruction> BIS;
	public static int topBIS = -1;
	
	public static Instruction actualFetchtoNext = null;
	public static boolean stalled = false;
	public static boolean masterStall;
	public static int inCycles;
	public static boolean isItEnd = false;
	public static int multimer = 0;

	public static int IntEX = -1;
	public static int MulEX = -1;
	public static int branchEX = -1;
	
	public static int LSFU = -1;
	public static int LSQ = -1;
	public static int IntEXValue = -1;
	public static int MulEXValue = -1;
	public static int branchEXValue = -1;
	public static int LSFUValue = -1;
	public static int LSQValue = -1;
	public static int forwardResult = -1;
	public static int URF_SIZE = 32;

	public Sim() {
		PCValue = 4000;
		instructions = new Instruction[2000];
		archRegister = new int[17];
		register = new int[URF_SIZE];
		regValidity = new boolean[URF_SIZE];
		loadStoreQueue = new Instruction[3];
		issueQueue = new Instruction[100];
		IQ_Size = 0;
		reorderBuffer = new LinkedList<Instruction>();
		freelist = new LinkedList<Integer>();
		RATdecision = new int[16];
		memory = new int[10000];
		for (int i = 0; i < 2000; i++) {
			instructions[i] = new Instruction();
		}
		BIS = new Stack<Instruction>();
		RAT = new HashMap<Integer, Integer>();
		
		RAT.put(0, 0);
		RAT.put(1, 1);
		RAT.put(2, 2);
		RAT.put(3, 3);
		RAT.put(4, 4);
		RAT.put(5, 5);
		RAT.put(6, 6);
		RAT.put(7, 7);
		RAT.put(8, 8);
		RAT.put(9, 9);
		RAT.put(10, 10);
		RAT.put(11, 11);
		RAT.put(12, 12);
		RAT.put(13, 13);
		RAT.put(14, 14);
		RAT.put(15, 15);
		
		RATdecision[0] = 0;
		RATdecision[1] = 0;
		RATdecision[2] = 0;
		RATdecision[3] = 0;
		RATdecision[4] = 0;
		RATdecision[5] = 0;
		RATdecision[6] = 0;
		RATdecision[7] = 0;
		RATdecision[8] = 0;
		RATdecision[9] = 0;
		RATdecision[10] = 0;
		RATdecision[11] = 0;
		RATdecision[12] = 0;
		RATdecision[13] = 0;
		RATdecision[14] = 0;
		RATdecision[15] = 0;
		
		for (int i = 0; i < URF_SIZE; i++) {
			freelist.add(i);
		}

		for (int i = 0; i < URF_SIZE; i++)
			regValidity[i] = false;

		stalled = false;
		LSQ_Size = 0;
	}

	public static void main(String[] args) {
		
		Sim simulator = new Sim();
		String INPUT_FILE ="";
		int incycles=0;
		boolean yes = true;
		
		Fetch fetch = new Fetch();
		Decode decode = new Decode();
		Execute exec = new Execute();
		LoadStore loadstore = new LoadStore();
		WriteBack writeback = new WriteBack();
		Retire retire = new Retire();
		
		Util util = new Util();
		
		while(yes){
			
			@SuppressWarnings("resource")
			Scanner sc = new Scanner(System.in);
			
			System.out.println("");
			System.out.println("Enter command (load <filename>, Initialize, Simulate <n>, Display): ");
			
			String command = sc.nextLine();
			command = command.toLowerCase();
			System.out.println("Command Entered : "+command);
			
			if(command.contains("load")){
				try{
					INPUT_FILE = (command.split(" ")[1].trim());
					System.out.println("INPUT FILE NAME :: "+INPUT_FILE);
				} catch (Exception e){
					System.out.println("Please provide the Input File");
				}
					simulator.loadfile(INPUT_FILE);
			} else if(command.contains("initialize")){
				PCValue = 4000;
				System.out.println("==== Processor initialized ====");
			} else if(command.contains("simulate")){
				try{
					incycles = Integer.parseInt(command.split(" ")[1].trim());
				} catch (NumberFormatException e){
					System.out.println("Please provide the number of Cycles to be Simulated");
				}
					simulator.simulate(fetch, decode, exec, loadstore, writeback,retire, util, incycles);
					displayAll();
			} else if(command.contains("display")){
						displayAll();
			}else if(command.contains("print_map_tables"))
			{
				printRenameTable();
			}else if(command.contains("print_iq"))
			{
				printIssueQueue();
			}
			else if(command.contains("print_rob"))
			{
				printreorderbuffer();
			}
			else if(command.contains("print_memory"))
			{
				printMemory();
			}
			else if(command.contains("print_urf"))
			{
				printRegisters();
				printFreeList();
			}
			else if(command.contains("set_urf_size"))
			{
				try{
					URF_SIZE = Integer.parseInt(command.split(" ")[1].trim());
					simulator = new Sim();
				} catch (NumberFormatException e){
					System.out.println("Please provide the URF_SIZE before Simulation");
				}
			}
			
				else {
			
				System.out.println("");
				System.out.println(" You have entered the Wrong Input. Please run again");
				System.out.println("");
				break;
			}
		}
	}
	
	public void loadfile(String filename) {
		BufferedReader br;
		int lastDest = -1;
		String line;
		int i = 0;
		try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			
			   while ((line = br.readLine()) != null) {
				
				line = line.trim().replaceAll(",", " ");
				line = line.replaceAll("#", " ");
				line = line.replaceAll(" +", " ");
				
				// System.out.println(i + " " + line);
				Sim.instructions[i].rawString = line;
				Sim.instructions[i].contains = true;
				Sim.instructions[i].address = i + 4000;
				Sim.preProcessInstructions(Sim.instructions[i]);
				
				if (instructions[i].instrType == InstructionType.BZ || instructions[i].instrType == InstructionType.BNZ) {
					instructions[i].src1 = lastDest;
				}
				lastDest = instructions[i].destination;

				if (instructions[i].instrType == InstructionType.STORE) {
					instructions[i].src2 = instructions[i].src1;
					instructions[i].src1 = instructions[i].destination;
					instructions[i].destination = -1;
				}
				i++;
			}
			System.out.println("Input File Loaded");
		} catch (FileNotFoundException e) {
			System.out.println("Input file not found.");
			System.exit(0);
		} catch (IOException e) {
			System.out.println("IO error occured\n" + e);
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Problem with instructions in the  file.Please Try Again \n" + e);
			System.exit(0);
		}
	}
	
	
	public void simulate(Fetch fetch, Decode decode,Execute exec, LoadStore loadstore,
							WriteBack writeback, Retire retire,	Util util, int inCycles) {

		for (int i = 0; i < inCycles; i++) {
			if (PCValue < 4000) {
				System.out.println("Invalid PC Value:" + PCValue);
				System.exit(-1);
			}
			if (!masterStall) {
				fetch.performFetch();
				decode.perfromDecode();
			} else {
				actualDecode = null;
			}
			util.updateIssueQueue();
			exec.execute(loadstore);
			writeback.performWriteBack();
			retire.performRetire();

			util.removeLoadStoreFromIssueQueue();
			util.removeLoadOnly();
		}
	}

	
	
	
	public static void preProcessInstructions(Instruction instruction) throws Exception {
		String d;
		String string = instruction.rawString;
		instruction.FUtype = 0;
		StringTokenizer tokenizer = new StringTokenizer(string, " ");
		instruction.instrName = tokenizer.nextToken();
		if (instruction.instrName.equals("XOR")) {
			instruction.instrType = InstructionType.XOR;
		} else {
			instruction.instrType = InstructionType.valueOf(instruction.instrName);
		}
		if (instruction.instrType == InstructionType.LOAD || instruction.instrType == InstructionType.STORE)
			instruction.isLoadStore = true;
		else {
			instruction.isLoadStore = false;
		}
		if (instruction.instrType == InstructionType.HALT)
			return;

		if (instruction.instrType == InstructionType.MUL)
			instruction.FUtype = 1;
		if (instruction.instrType == InstructionType.LOAD || instruction.instrType == InstructionType.STORE)
			instruction.FUtype = 2;
		if (instruction.instrType == InstructionType.JUMP || instruction.instrType == InstructionType.BAL)
			instruction.FUtype = 3;
		if (tokenizer.hasMoreTokens()) {
			if (instruction.instrType == InstructionType.JUMP) {
				d = tokenizer.nextToken();
				if (d.equals("X")) {
					instruction.src1 = 16;
				} else if (d.charAt(0) != 'R') {
					instruction.literal = Integer.parseInt(d);
					System.out.println("JUMP/BZ/BNZ" + d);
					return;
				} else {
					instruction.src1 = Integer.parseInt(d.substring(1));
				}
			} else {

				d = tokenizer.nextToken();
				if (d.charAt(0) != 'R') {
					instruction.literal = Integer.parseInt(d);
					return;
				} else {
					instruction.destination = Integer.parseInt(d.substring(1));
				}
			}
		}

		if (tokenizer.hasMoreTokens()) {
			d = tokenizer.nextToken();
			if (d.charAt(0) != 'R') {
				instruction.literal = Integer.parseInt(d);
				return;
			} else if (d.equals("X")) {
				instruction.src1 = 16;
			} else {
				instruction.src1 = Integer.parseInt(d.substring(1));
			}
		}
		if (tokenizer.hasMoreTokens()) {

			d = tokenizer.nextToken();
			if (d.charAt(0) != 'R') {
				instruction.literal = Integer.parseInt(d);
				return;
			} else {
				instruction.src2 = Integer.parseInt(d.substring(1));
			}
		}
	}
	
	
	
	
	
	public static void displayAll() {
		System.out.println("-----------------------------------------------------");
		System.out.println("Program Counter:" + PCValue);
		printRegisters();
		//printForwardingPaths();
		printIssueQueue();
		printStages();
		
		printreorderbuffer();
		printFreeList();
		printRenameTable();
		printMemory();
		System.out.println("-----------------------------------------------------");
	}

	
	
	
	public static void printRegisters() {
		System.out.print("\nArchitectural Registers :           ");
		System.out.print("R0=" + archRegister[0]);
		System.out.print("\tR1=" + archRegister[1]);
		System.out.print("\tR2=" + archRegister[2]);
		System.out.print("\tR3=" + archRegister[3]);
		System.out.print("\tR4=" + archRegister[4]);
		System.out.print("\tR5=" + archRegister[5]);
		System.out.print("\tR6=" + archRegister[6]);
		System.out.print("\tR7=" + archRegister[7]);
		System.out.print("\tR8=" + archRegister[8]);
		System.out.print("\tR9=" + archRegister[9]);
		System.out.print("\tR10=" + archRegister[10]);
		System.out.print("\tR11=" + archRegister[11]);
		System.out.print("\tR12=" + archRegister[12]);
		System.out.print("\tR13=" + archRegister[13]);
		System.out.print("\tR14=" + archRegister[14]);
		System.out.print("\tR15=" + archRegister[15]);
		System.out.print("\tX=" + archRegister[16]);
		
		System.out.println("");
		System.out.print("Physical Registers :           ");
		for(int i=0;i< URF_SIZE;i++)
		{
			System.out.print("\tP["+i+"] : " + register[i]);
		}
		/*System.out.print("P0:" + register[0]);
		System.out.print("\tP1:" + register[1]);
		System.out.print("\tP2:" + register[2]);
		System.out.print("\tP3:" + register[3]);
		System.out.print("\tP4:" + register[4]);
		System.out.print("\tP5:" + register[5]);
		System.out.print("\tP6:" + register[6]);
		System.out.print("\tP7:" + register[7]);
		System.out.print("\tP8:" + register[8]);
		System.out.print("\tP9:" + register[9]);
		System.out.print("\tP10:" + register[10]);
		System.out.print("\tP11:" + register[11]);
		System.out.print("\tP12:" + register[12]);
		System.out.print("\tP13:" + register[13]);
		System.out.print("\tP14:" + register[14]);
		System.out.print("\tP15:" + register[15]);*/
		
		System.out.println("");
		System.out.print("Allocated bit : \t\t");
		for (int i = 0; i < 16; i++) {
			System.out.print("" +!freelist.contains(i) + "\t");
		}
		System.out.println("");
		System.out.print("Status bit :    \t\t");
		for (int i = 0; i < 16; i++) {
			System.out.print("" +regValidity[i] + "\t");
		}
		System.out.println("");
	}

	
	
	

	
	public static void printForwardingPaths() {
		StringBuilder sb = new StringBuilder();
		System.out.println("");
		System.out.println("Forwarding Information ");
		System.out.println("-----------------------");
		System.out.println("From    Data   Value");
		System.out.println("-----------------------");
		sb.append("IntFU\tP" + IntEX + "\t" + IntEXValue + "\n");
		sb.append("MulFU\tP" + MulEX + "\t" + MulEXValue + "\n");
		sb.append("LS FU\tP" + LSFU + "\t" + LSFUValue + "\n");
		System.out.println(sb.toString());
	}



	
	
	public static void printFUs() {
		System.out.print("In Int FU\t");
		System.out.println(IntExecuteFU);
		System.out.print("In Mul FU\t");
		System.out.print(ExecMultiplyFU);
		if (ExecMultiplyFU != null)
			System.out.print("\tCycle: " + multimer);
		System.out.println();
		System.out.print("In Branch FU\t");
		System.out.println(ExecBranchFU);
		System.out.print("In LS FU\t");
		System.out.print(loadStoreFU + "(Stage1)\n");
		System.out.print("In LS FU\t");
		System.out.print(curLSFU + "(Stage2)\n");
		//System.out.print("In LS FU\t");
		//System.out.print(nextLSFU + "(Stage3)");
		System.out.println("");
	}	

	
	
	public static void printIssueQueue() {
		System.out.println("Issue Queue:(Size:" + IQ_Size + ")");
		for (int i = 0; i < 100; i++) {

		if (issueQueue[i] != null) {
			System.out.println(issueQueue[i] + "\tIssuable:" + issueQueue[i].isReadyForIssue + "\tAge in Queue:" + issueQueue[i].ageInIQ);
		}
		}
		System.out.println("");
	}


	
	
	public static void printStages() {
		System.out.println("Pipeline Stages");
		System.out.println("----------------");
		System.out.print("In Fetch\t");
		System.out.print(actualFetch + "\n");
		System.out.print("In Decode\t");
		System.out.print(actualDecode + "\n");
		System.out.print("In EX\n");
		printFUs();
	}
	
	
	
	
	
	public static void printLSQ() {
		System.out.println("");
		System.out.println("Load Store Queue:(Size:" + LSQ_Size + ")");
		boolean addResolved = false;
		for (int i = 0; i < 3; i++) {
			if (loadStoreQueue[i] != null) {
				addResolved = loadStoreQueue[i].instrType == InstructionType.LOAD ? loadStoreQueue[i].src1validity
						: loadStoreQueue[i].src2validity;
				System.out.println(loadStoreQueue[i] + "\tAddress Resolved:" + addResolved + "\t,IsIssuable:"
						+ loadStoreQueue[i].isReadyForIssue + ", issued:" + loadStoreQueue[i].isLSissued
						+ "\t isReadyForCommit:" + loadStoreQueue[i].isReadyForCommit);
			} else {
			}
		}
	}

	

	
	
	public static void printreorderbuffer() {
		System.out.println("\nReorder Buffer: [Instruction, Is ready for commit ?]");
		for (Instruction i : reorderBuffer) {
			System.out.println(i + " :" + i.isReadyForCommit + "");
		}
	}

	
	
	
	
	public static void printFreeList() {
		System.out.println("");
		System.out.print("\nFree Registers: ");
		System.out.print(freelist);
	}

	
	
	
	public static void printRenameTable() {
		System.out.println("");
		System.out.println("");
		System.out.println("Register Alias Table:");
		char decide1, decide2;
		decide1 = RATdecision[0] == 0 ? 'R' : 'P';
		decide2 = RATdecision[8] == 0 ? 'R' : 'P';
		System.out.println("R0 -> " + decide1 + RAT.get(0) + "\t\tR8 -> " + decide2 + RAT.get(8));
		decide1 = RATdecision[1] == 0 ? 'R' : 'P';
		decide2 = RATdecision[9] == 0 ? 'R' : 'P';
		System.out.println("R1 -> " + decide1 + RAT.get(1) + "\t\tR9 -> " + decide2 + RAT.get(9));
		decide1 = RATdecision[2] == 0 ? 'R' : 'P';
		decide2 = RATdecision[10] == 0 ? 'R' : 'P';
		System.out.println("R2 -> " + decide1 + RAT.get(2) + "\t\tR10 -> " + decide2 + RAT.get(10));
		decide1 = RATdecision[3] == 0 ? 'R' : 'P';
		decide2 = RATdecision[11] == 0 ? 'R' : 'P';
		System.out.println("R3 -> " + decide1 + RAT.get(3) + "\t\tR11 -> " + decide2 + RAT.get(11));
		decide1 = RATdecision[4] == 0 ? 'R' : 'P';
		decide2 = RATdecision[12] == 0 ? 'R' : 'P';
		System.out.println("R4 -> " + decide1 + RAT.get(4) + "\t\tR12 -> " + decide2 + RAT.get(12));
		decide1 = RATdecision[5] == 0 ? 'R' : 'P';
		decide2 = RATdecision[13] == 0 ? 'R' : 'P';
		System.out.println("R5 -> " + decide1 + RAT.get(5) + "\t\tR13 -> " + decide2 + RAT.get(13));
		decide1 = RATdecision[6] == 0 ? 'R' : 'P';
		decide2 = RATdecision[14] == 0 ? 'R' : 'P';
		System.out.println("R6 -> " + decide1 + RAT.get(6) + "\t\tR14 -> " + decide2 + RAT.get(14));
		decide1 = RATdecision[7] == 0 ? 'R' : 'P';
		decide2 = RATdecision[15] == 0 ? 'R' : 'P';
		System.out.println("R7 -> " + decide1 + RAT.get(7) + "\t\tR15 -> " + decide2 + RAT.get(15));
	}

	public static void printRRenameTable()
	{
		System.out.println("Back End Table:");
	}
	
	public static void printBIS() {
		System.out.println("\nBIS: "+BIS);
		System.out.println("");
	}
	
		
	
	
	

	public static void printMemory() {
		System.out.println("");
		System.out.println("PRINTING MEMORY LOCATIONS: ");
		System.out.println("");
		for (int i = 0; i < 100; i++) {
			System.out.print("Memory["+i+"]"+": "+memory[i] + "\t");
			if ((i + 1) % 10 == 0)
				System.out.println();
		}
	}
}
