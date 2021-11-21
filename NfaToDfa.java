// import extensions
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class NfaToDfa {

	// line variables from text file
	static String line = null;
	static String line1 = null;
	static String line2 = null;
	static String line3 = null;
	static String line4 = null;
	static String line5 = null;
	static String line6 = null;

	// get these from text file (in order)
	static String [] states; // array for NFA states
	static String [] finalState; // array for NFA final state
	static String [] alphabets; // array for NFA alphabet
	static String startState; // array for NFA start state
	static String [] transitions; // array for NFA transitions
	static String [] inputString; // array for NFA string input
	static ArrayList<Transition> tranList = new ArrayList<>(); // array for new DFA transitions

	// main function 
	public static void main(String[] args) throws IOException {
		// allows program to read the file
		FileReader file = new FileReader("testCases.in");
		BufferedReader buffReader = new BufferedReader(file);	
		
		// if states is not empty	
		while((line = buffReader.readLine())!= null){
			//line = states
			tranList.clear();
			line1 = buffReader.readLine(); // NFA final state 
			line2 = buffReader.readLine(); // NFA alphabet
			line3 = buffReader.readLine(); // NFA start state
			line4 = buffReader.readLine(); // NFA transitions
			line5 = buffReader.readLine(); // NFA input string
			line6 = buffReader.readLine(); // should be empty

			// if a line is empty, print empty line and continue
			if(!checkEmpty()){
				System.err.print("empty line.");
				continue;
			}
			states = line.split(",");
			finalState = line1.split(",");
			
			// if cannot check final state, continue
			if(!checkFinal()){
				continue;
			}

			alphabets = line2.split(",");
			startState = line3;

			// if cannot check start state, print invalid start state and continue
			if(!checkStart()){
				System.err.println(startState + " is an invalid state state.");
				continue;
			}

			transitions = line4.split("#"); // print a # to split each transition
			
			boolean error = false;
			boolean error2 = false;

			// check for errors
			for(String transition : transitions){
				error2 = false;
				String [] tranAR = transition.split(",");
				// error if transition length is not 3
				if(tranAR.length != 3){
					error = true;
					break;
				}

				// error if there is no alphabet
				if(!inArray(tranAR[2], alphabets) &&!tranAR[2].equals("$")){
					error2 = true;
					System.err.println("Error. Transition " + tranAR[2] + " is not included.");
				}

				// error if there are no states
				for ( int i = 0 ; i < 2 ; i++){
					if(!inArray(tranAR[i], states)){
						error2 = true;
						System.err.println("Error. Transition " + tranAR[i] + " is not included.");
						break;
					}	
				}

				if(error2){
					break;
				}
				tranList.add(new Transition(tranAR[0],tranAR[1],tranAR[2]));
			}

			// if there is an error, print the error and continue
			if(error){
				System.err.println("Error. Transition must be size of 3.");
				continue;
			}
			// if there is an error, continue
			if(error2){
				continue;
			}

			inputString = line5.split("#");
			boolean error3 = false;
			String errorInput = "";
			for(String input : inputString){
				String [] inputAR = input.split(",");
				for(String inputAL : inputAR){
					// if input string doesn't match the alphabet, break
					if(!inArray(inputAL, alphabets)){
						error3 = true;
						errorInput = inputAL;
						break;
					}
				}
			}

			// if there is an error within the input string, print the error and continue
			if(error3){
				System.err.println(errorInput + " is an invalid input string.");
				continue;
			}

			System.out.println("\nNFA Successfully Constructed"); // if a valid NFA, print so
			System.out.println("Constructing DFA: "); // if a DFA can be made, print so
			
			// create start state of DFA
			ArrayList<String> initialDFA = getEpsilonInDead(startState, tranList.toArray(new Transition[tranList.size()]));
			// create DFA transitions by storing the transitions into a new array list
			ArrayList<Transition> NFATransitions = new ArrayList<>();
			// create all DFA states by storing the states into a new array list
			ArrayList<ArrayList<String>> allStates = new ArrayList<>();
			NFATransitions = createTrans(initialDFA, tranList.toArray(new Transition[tranList.size()]), alphabets);
			
			// loop through all the NFA transitions to create new DFA transitions
			for(int i = 0; i < NFATransitions.size() ; i ++) {
				createStates(allStates, NFATransitions.get(i).fromAlphabet);
				createStates(allStates, NFATransitions.get(i).toAlphabet);
				createTransition(NFATransitions, createTrans(NFATransitions.get(i).toAlphabet, 
					tranList.toArray(new Transition[tranList.size()]), alphabets));
			}
			// print all the DFA states
			String DFAStates = "";
			for(int i = 0 ; i < allStates.size(); i++) {
				ArrayList<String> stateInAllStates = allStates.get(i);
				DFAStates += printStat(stateInAllStates);
				if(i<allStates.size()-1) {
					// separate each state with a comma
					DFAStates += ",";
				}
			}

			// print the DFA states
			System.out.println("DFA States: " + DFAStates);
			
			// loop to find DFA all final states
			String DFAfinalState = "";
			for(int i = 0 ; i < allStates.size();i++) {
				ArrayList<String> stateInAllStates = allStates.get(i);
				if(checkIfAccept(finalState, stateInAllStates)) {
					DFAfinalState += printStat(stateInAllStates);
					if(i<allStates.size()-1) {
						DFAfinalState += ",";
					}
				}
			}
			// print the DFA final states
			System.out.println("DFA Accept State(s): " + DFAfinalState);
			
			// print the alphabet
			System.out.println("DFA Alphabet: " + line2);
			
			// print the initial states
			String DFAInitState = printStat(initialDFA);
			System.out.println("DFA Start State: " + DFAInitState);
			
			// print all transitions (DFA and NFA)
			String DFATransitions = "";
			for(int i = 0 ; i<NFATransitions.size();i++) {
				DFATransitions +=printStat(NFATransitions.get(i).fromAlphabet);
				DFATransitions +=",";
				DFATransitions +=printStat(NFATransitions.get(i).toAlphabet);
				DFATransitions +=",";
				DFATransitions +=NFATransitions.get(i).alphabet;
				if(i < NFATransitions.size() - 1) {
					DFATransitions +="#";
				}
			}

			// print DFA transitions
			System.out.println("DFA Transitions: " + DFATransitions);
			
			// print input string
			System.out.println("Input String: " + line5);
			
			constructDFA(DFAStates, DFAfinalState, line2, DFAInitState, DFATransitions, line5);
		}
		buffReader.close();
	}
	
	public static void constructDFA(String DFAstates, String DFAacceptStates, String DFAAlphabet, 
		String DFAinitState, String DFAtransitions, String DFAinput ) {
		
		// give lines new variable names
		String line = DFAstates;
		String line1 = DFAacceptStates;
		String line2 = DFAAlphabet;
		String line3 = DFAinitState;
		String line4 = DFAtransitions;
		String line5 = DFAinput;
		states = line.split(",");
		finalState = line1.split(",");
		
		// if there is no final state, return
		if(!checkFinal()){
			return;
		}

		// separate each alphabet with a comma
		alphabets = line2.split(",");
		startState = line3;
		
		// if there is no start state, return
		if(!checkStart()){
			System.err.println(startState + " is an invalid start state.");
			return;
		}

		// separate each transition with a 
		transitions = line4.split("#");
		boolean error = false;
		boolean error2 = false;

		// check for errors 
		for(String transition : transitions) {
			error2 = false;
			String [] tranAR = transition.split(",");
			if(tranAR.length != 3) {
				error = true;
				break;
			}

			// error if other characters are included in DFA
			if(!inArray(tranAR[2], alphabets) &&!tranAR[2].equals("$")){
				error2 = true;
				System.err.println("Error. Transition " + tranAR[2] + " is not included.");
			}
			if(error2) {
				break;
			}
			tranList.add(new Transition(tranAR[0],tranAR[1],tranAR[2]));

			// error if there are no states
			for ( int i = 0 ; i < 2 ;i++){
				if(!inArray(tranAR[i], states)) {
					error2 = true;
					System.err.println("Error. Transition " + tranAR[i] + " is not included.");
					break;
				}	
			}
		}

		// error if transition size is less than 3
		if(error) {
			System.err.println("Error. Transition must be size of 3.");
			return;
		}
		if(error2) {
			return;
		}

		// error for error3
		inputString = line5.split("#");
		boolean error3 = false;
		String errorInput = "";
		for(String input : inputString){
			String [] inputAR = input.split(",");
			for(String inputAL : inputAR){
				if(!inArray(inputAL, alphabets)){
					error3 = true;
					errorInput = inputAL;
					break;
				}
			}
		}
		if(error3){
			System.err.println(errorInput + " is an invalid input string.");
			return;
		}

		// error for error4
		boolean error4 = false;
		for(String state : states){
			for(String alphabet : alphabets){
				if(!existTrans(state,alphabet)){
					error4 = true;
					System.err.println("Missing transition for state " + state + " on input " + alphabet );
					break;
				}
			}
		}
		if(error4){
			return;
		}

		// Construct the DFA and check string 
		System.out.println("DFA Successfully Constructed");
		System.out.println("String Output: ");
		for(String input : inputString){
			String result = readInput(input);
			if(inArray(result, finalState)){
				System.out.println("\tAccepted");
			} else {
				System.out.println("\tRejected");
			}
		}
		System.out.println("");
	}

	//make states 
	private static void createStates(ArrayList<ArrayList<String>> allStat, ArrayList<String> someStat) {
			if(!allStat.contains(someStat)) {
				allStat.add(someStat);
			}		
	}

	//add transition
	public static void createTransition(ArrayList<Transition> nfaTrans,ArrayList<Transition> newTrans) {
		// make new transitions from the nfa transitions for dfa
		for(int i = 0 ; i< newTrans.size() ; i++) {
			Collections.sort(newTrans.get(i).fromAlphabet);
			Collections.sort(newTrans.get(i).toAlphabet);
			int j;
			for (j = 0;j < nfaTrans.size(); j++) {
				Collections.sort(nfaTrans.get(j).fromAlphabet);
				Collections.sort(nfaTrans.get(j).toAlphabet);
				if(nfaTrans.get(j).fromAlphabet.equals(newTrans.get(i).fromAlphabet) && nfaTrans.get(j).toAlphabet.equals(newTrans.get(i).toAlphabet) 
					&& nfaTrans.get(j).alphabet.equals(nfaTrans.get(i).alphabet)){
					break;
				}
			}
			if(j == nfaTrans.size()) {
				nfaTrans.add(newTrans.get(i));
			}
		}
	}

	//read the nfa input 
	private static String readInput(String input) {
		String currentState = startState;
		String [] inputAR = input.split(",");
		for(int i = 0 ; i< inputAR.length ;i++){
			for(int j = 0 ; j < tranList.size() ; j++){
				if(tranList.get(j).from.equals(currentState) && tranList.get(j).alphabet.equals(inputAR[i])){
					currentState = tranList.get(j).to;
					break;
				}
			}
		}
		return currentState;
	}

	// check if there is something in the array
	private static boolean inArray(String s , String [] array){
		for(int i = 0 ; i < array.length;i++){
			if(array[i].equals(s)){
				return true;
			}
		}
		return false;
	}

	//check final states
	private static boolean checkFinal() {
		for(String goal : finalState){
			if(goal.equals("")){
				continue;
			}
			if(!inArray(goal,states)){
				System.err.println("Invalid accept state " + goal + "\n");
				return false;
			}
		}
		return true;
	}

	//check start state
	private static boolean checkStart() {
		return inArray(startState, states);
	}

	// check if any line in input is empty
	private static boolean checkEmpty() {
		if(line1 == "" || line2 == "" || line3 == "" || line4 == "" || line5 == ""|| line6 == "") {
			System.err.println("Error. Line is empty.");
			return false;
		}
		return true;
	}
	
	
	//check and add the epislon to an arraylist
	public static ArrayList<String> addEpsilonInArray(String state,Transition[]transitions){
		ArrayList<String> result = new ArrayList<>();
		result.add(state);
		for(int i = 0 ; i<transitions.length;i++) {
			if(transitions[i].alphabet.equals("$") && transitions[i].from.equals(state)&&!result.contains(transitions[i].to)) {
				result.add(transitions[i].to);
			}
		}
		return result;
	}

	//check if the states is in the array if not adds
	public static void checkAndAdd(ArrayList<String> result, ArrayList<String> arrayToBeAdded) {
		for(int i = 0; i<arrayToBeAdded.size();i++) {
			if(!result.contains(arrayToBeAdded.get(i))) {
				result.add(arrayToBeAdded.get(i));
			}
		}
	}
	
	//get all the epsilon to put it into a dead states 
	public static ArrayList<String> getEpsilonInDead(String state,Transition[]transitions){
		ArrayList<String> result = addEpsilonInArray(state, transitions);
		for(int i = 0 ; i < result.size();i++) {
			ArrayList<String> newOutcome = addEpsilonInArray(result.get(i), transitions);
			for(int j = 0 ; j<newOutcome.size();j++) {
				if (!result.contains(newOutcome.get(j))) {
					result.add(newOutcome.get(j));
				}
			}
		}
		return result;
	}
	
	//check all the accept states
	public static boolean checkIfAccept(String[] acceptStates, ArrayList<String> state) {
		for(int i = 0 ; i < state.size();i++) {
			for( int j = 0 ; j < acceptStates.length ;j++) {
				if(acceptStates[j].equals(state.get(i))) {
					return true;
				}
			}
		}
		return false;
	}
	
	//get a given states
	public static ArrayList<String> getInputStates(ArrayList<String> state, Transition[]transitions, String alphabet){
		ArrayList<String> result = new ArrayList<>();
		for(int i = 0 ; i < state.size() ; i++) {
			for(int j = 0 ; j < transitions.length ;j++) {
				if(transitions[j].alphabet.equals(alphabet) && transitions[j].from.equals(state.get(i))&&!result.contains(transitions[j].to)) {
					result.add(transitions[j].to);
					checkAndAdd(result, getEpsilonInDead(transitions[j].to, transitions));
				}
			}
		}
		return result;
	}

	//add and make transition into a new arrayList
	public static ArrayList<Transition> createTrans(ArrayList<String> state,Transition[]trans,String[]alpha) {
		ArrayList<Transition> result= new ArrayList<>();
		for(int i = 0 ; i< alpha.length ; i++) {
			ArrayList<String> toStates = getInputStates(state, trans, alpha[i]);
			if(toStates.size() == 0) {
				toStates.add("Dead");
			}
			result.add(new Transition(state, toStates, alphabets[i]));
		}
		return result;
	}
	
	// Print all the states 
	public static String printStat(ArrayList<String>states) {
		String r = "";
		for(int i = 0 ; i<states.size();i++) {
			r+=states.get(i);
			if(i < states.size() - 1) {
				r+="|";
			}
		}
		return r;
	}
	
	// check if the transition exists
	private static boolean existTrans(String state, String AL) {
		for(int i = 0 ; i < tranList.size() ; i++){
			if(tranList.get(i).from.equals(state) && tranList.get(i).alphabet.equals(AL)){
				return true;
			}
		}
		return false;
	}
}

// transition class that has all the points and alphabet
class Transition {
	String from;
	String to;
	ArrayList<String> fromAlphabet;
	ArrayList<String> toAlphabet;
	String alphabet;

	public Transition(ArrayList<String> from, ArrayList<String> to, String alphabet){
		this.fromAlphabet = from;
		this.toAlphabet = to;
		this.alphabet = alphabet;
	}

	public Transition(String from, String to, String alphabet){
		this.from = from;
		this.to = to;
		this.alphabet = alphabet;
	}
}