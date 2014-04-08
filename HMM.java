import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import javax.swing.JFileChooser;


/**
 * Creates usable HMM graph
 * 
 * Dartmouth CS 10, Winter 2014
 * @author Paul Champeau
 */

public class HMM {
	TreeMap<String, TreeMap<String, Float>> emissionMap;
	TreeMap<String, ProbMap> emissionMapTemp;
	TreeMap<String, TreeMap<String, Float>> transMap;
	TreeMap<String, ProbMap> transMapTemp;
	ArrayList<String> textFile;
	ArrayList<String> typeFile;
	public int WORD_COUNT = 57237;
	public static Scanner scan;


	public HMM() {
		this.emissionMap = new TreeMap<String, TreeMap<String, Float>>();
		this.transMap = new TreeMap<String, TreeMap<String, Float>>();
		this.emissionMapTemp = new TreeMap<String, ProbMap>();
		this.transMapTemp = new TreeMap<String, ProbMap>();
		this.textFile = new ArrayList<String>();
		this.typeFile = new ArrayList<String>();
		scan = new Scanner(System.in);
	}
	/**
	 * This method populates the emissionMapTemp and transMapTemp instance variables from the 
	 * selected files, taking in two corresponding sentences at a time (one is the words, one is the word types).
	 * @param lineWord	
	 * @param lineType
	 */
	public void updateFrequency(String lineWord, String lineType) {
		String oldWordType = "start";
		if (lineType != null && lineWord != null) {
			String[] wordTypes = lineType.split(" ");
			String[] words = lineWord.split(" ");
			int i = 0;
			for (String currentWordType: wordTypes) {

				//if not at first word of sentence
				if (oldWordType.equals("start")) {
					updateProbabilities(transMapTemp, oldWordType, currentWordType);
					}
				
				else {
					String word = words[i-1].toLowerCase();	//
					updateProbabilities(emissionMapTemp, oldWordType, word);
					updateProbabilities(transMapTemp, oldWordType, currentWordType);

				}
				i++;

				oldWordType = currentWordType;
			}
			updateProbabilities(emissionMapTemp, oldWordType, words[i-1]);
			oldWordType = "";		//signifies we are now at a new sentence/line

		}
	}
	/**
	 * This method is called after the entire file has been read, and converts each value in the 
	 * emissionMapTemp and transMapTemp maps into log probabilities and then stores the result into
	 * the transMap and emissionMap instance variables.
	 */
	public void updateFinalMaps() {
		//prints out probMap for each tag ID and makes new map with ln(frequency/denominator) for each
		//wordType in transMapTemp
		TreeMap<String, TreeMap<String, Float>> tagIDsFinal = new TreeMap<String, TreeMap<String, Float>>();
		for (String key: this.transMapTemp.keySet()) {
			ProbMap probMap = this.transMapTemp.get(key);
			tagIDsFinal.put(key, this.transMapTemp.get(key).map);

			for (String key2: probMap.map.keySet()) {
				tagIDsFinal.get(key).put(key2, (float) Math.log(probMap.map.get(key2) / probMap.getDenominator()));
				System.out.println(key + ": " + key2 + " " + tagIDsFinal.get(key).get(key2));
			}
		}

		//makes new map with ln(frequency/denominator) for each word in emissionMapTemp
		TreeMap<String, TreeMap<String, Float>> emissionMapFinal = new TreeMap<String, TreeMap<String, Float>>();
		for (String key: this.emissionMapTemp.keySet()) {
			ProbMap probMap = this.emissionMapTemp.get(key);
			emissionMapFinal.put(key, this.emissionMapTemp.get(key).map);
			for (String key2: probMap.map.keySet()) {

				emissionMapFinal.get(key).put(key2, (float) Math.log(probMap.map.get(key2) / probMap.getDenominator()));
				
			}
		}

		this.transMap = tagIDsFinal;
		this.emissionMap = emissionMapFinal;		
	}

	/**
	 * This method updates and normalizes the probabilities for each possible 
	 * emission and transition.
	 * @param tagIDs
	 * @param oldWordType
	 * @param currentWord
	 */
	public void updateProbabilities(TreeMap<String, ProbMap> tagIDs, String oldWordType, String currentWord) {
		//if this is first time we are seeing the old word type, add it to the tagID Map
		if (!tagIDs.containsKey(oldWordType)) {
			tagIDs.put(oldWordType, new ProbMap());
		}

		//if tagIDs probMap for oldWord doesn't have the currentWord (whether an actual word or word type), add it
		if (!tagIDs.get(oldWordType).map.containsKey(currentWord)) {

			//physically adds currentWord to probability map with frequency of 1.
			ProbMap probabilities = tagIDs.get(oldWordType);
			probabilities.map.put(currentWord, (float) 1);
		}
		else {
			//increment numerator for current word in old word's map
			ProbMap probabilities = tagIDs.get(oldWordType);
			Float frequency = probabilities.map.get(currentWord);
			probabilities.map.put(currentWord, frequency + 1);
		}

		tagIDs.get(oldWordType).incrementDenominator();
	}
	/**
	 * This method uses the Viterbi algorithm to predict the most likely parts of speech for
	 * the inputed sentence. It returns an ArrayList of Strings that each word's corresponding
	 * part of speech.
	 * @param sentence
	 * @return
	 */
	public ArrayList<String> predict(String sentence){
		//Splits the String and creates the necessary maps
		String words[] = sentence.split(" ");
		System.out.println("SENTENCE " + sentence + " LENGTH " + words.length);
		TreeMap<String, Float> prevscores = new TreeMap<String, Float>();
		ArrayList<TreeMap<String, String>> backtrack = new ArrayList<TreeMap<String, String>>(); 
		Float zero = new Float(0);
		prevscores.put("start", zero);
		//Looping over every word in the String
		for (int i = 0; i < words.length; i++){
			TreeMap<String, Float> scores = new TreeMap<String, Float>();
			Set<String> keys = prevscores.keySet();
			backtrack.add(new TreeMap<String, String>());
			//Looping over all the previous states
			for(String state : keys){
				Set<String> tagKeys = this.transMap.get(state).keySet();
				//Looping over all the possible states
				for(String tagKey : tagKeys){
				
					float nextscore = (prevscores.get(state) + this.transMap.get(state).get(tagKey));
					if(this.emissionMap.containsKey(tagKey) && this.emissionMap.get(tagKey).containsKey(words[i])){
						nextscore += this.emissionMap.get(tagKey).get(words[i]);
					}
					else nextscore += -200;
					if(!scores.containsKey(tagKey) || nextscore > scores.get(tagKey)){
						scores.put(tagKey, nextscore);
						backtrack.get(i).put(tagKey, state);
					}
				}
			}
			prevscores = scores;

		}
		String finalState = "";
		Set<String> prevKeys = prevscores.keySet();
		float max = -100000;
		for(String key : prevKeys){
			float num = prevscores.get(key);
			if(num > max) {
				max = num;
				finalState = key;
			}
		}

		String[] end_states = new String[sentence.split(" ").length];
		end_states[backtrack.size() - 1] = finalState;

		String state = finalState;
		for(int i = backtrack.size() - 1; i>=1; i-- ) {
			state = backtrack.get(i).get(state);
			end_states[i - 1] = state;
		}
		
		ArrayList<String> sequence = new ArrayList<String>();
		for(int i = 0; i < end_states.length; i++) {
			sequence.add(end_states[i]);
		}
		return sequence;
	}

	/**
	 * This method is a file chooser that returns the path to the
	 * selected file
	 * @return
	 */
	public static String setFilePath() {
		JFileChooser fc = new JFileChooser("."); // start at current directory
		int returnVal = fc.showOpenDialog(null);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			String pathName = file.getAbsolutePath();
			return pathName;
		}
		else
			return "";
	}
	/**
	 * This is a cross-validation method which allows the user to select how many partitions
	 * into which they would like to divide the data. It then prints to the console the percent
	 * accuracy of the algorithmic predictions.
	 * @param n
	 * @return
	 */
	public float crossValidation(Integer n) {
		System.out.println("Choose tags file...");
		String typeFileName = setFilePath();
		System.out.println("Choose words file...");
		String sentenceFile = setFilePath();
		BufferedReader inputType = null;
		BufferedReader inputWord = null;
		
		try {
			inputType = new BufferedReader( new FileReader(typeFileName));
			inputWord = new BufferedReader( new FileReader(sentenceFile));
			String lineType;
			String lineWord;
			//make two array lists of the entire text files, line by line
			while ((lineType = inputType.readLine()) != null && (lineWord = inputWord.readLine()) != null) {
				this.typeFile.add(lineType);
				this.textFile.add(lineWord);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				inputType.close();
				inputWord.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		double correctFreq = 0.0;
		double totalPredictions = 0.0;
		
		int lineNumber = 0;
		Integer iteration = 0; 		
		while (iteration < n) {
			ArrayList<String> sentences = new ArrayList<String>();
			ArrayList<String> types = new ArrayList<String>();
			
			//for each iteration, will deal out the correct line for cross-validation to testing
			//and the rest will be used for tagging
			while (lineNumber < this.textFile.size()) {
				if ((lineNumber) % n == iteration ) {
					sentences.add(this.textFile.get(lineNumber));
					types.add(this.typeFile.get(lineNumber));
				}
				else {
					updateFrequency(this.textFile.get(lineNumber), this.typeFile.get(lineNumber));
				}
				lineNumber ++;
			}
			//once training is complete for this iteration, create the final probability maps 
			updateFinalMaps();

			int i = 0;
			for (String sentence: sentences) {
				System.out.println(sentence);

				ArrayList<String> prediction = predict(sentence);
				String[] actual = types.get(i).split(" ");
				String s = "";
				
				//for each tag, if they are equal, add 1 to correctFrequency
				for (int num = 0; num < prediction.size(); num ++){
					if (actual[num].equals(prediction.get(num))) {
						correctFreq += 1;
					}
					//increase the total number of predictions
					totalPredictions += 1;
					s += prediction.get(num) + " ";
				}
				s = s.trim();
				System.out.println("Prediction: " + s);
				i++;	//next iteration
			}
				this.transMap.clear();
				this.transMapTemp.clear();
				this.emissionMap.clear();
				this.emissionMapTemp.clear();
			iteration += 1;
		}
		return (float) (correctFreq / totalPredictions) * 100;
	}




	public static void main(String [] args) {
		HMM hmm = new HMM();
		System.out.println("What number would you like to divide the data by for cross-Validation?");
		String number = scan.nextLine();
		System.out.println(hmm.crossValidation(Integer.valueOf(number)) + "% accuracy") ;

		for (int i = 0; i < hmm.textFile.size(); i ++) {
			hmm.updateFrequency(hmm.textFile.get(i), hmm.typeFile.get(i));
		}
		hmm.updateFinalMaps();

		System.out.println("Type 'q' to Quit \nReady for input: ");
		while (true) {
			String inputText = scan.nextLine();
			if (inputText.equals("q")) {
				return;
			}
			ArrayList<String> predictions = hmm.predict(inputText);
			System.out.println(predictions);
		}
	}
}





