import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

class TransitionMatrix {
	private double[]   initialSigns;
	private double[][] transitionMatrix;
	private HashMap<String, ArrayList<String>> dictionary;
	private String[] tags;
	private int terminalTagIndex;
	private MaxentTagger tagger;
	
	public static void main(String[] args) throws IOException { 
		new TransitionMatrix(); 
	}
	
	public TransitionMatrix() throws IOException {
		tagger = new MaxentTagger(Constants.taggerPath);
		terminalTagIndex = tagger.getTagIndex(".");
		
		initialSigns = new double[tagger.numTags()];
		
		// Create
		fillTransitionMatrix();
		//mapWords(tagger);
		fillTags();
		
		// Normalize
		normalizeTransitionMatrix();
		normalizeInitialSigns();
	}
	
	/*
	 * Creates a new transition matrix that is not normalized.
	 * @param tagger the tagger...
	 * 
	 * +
	 * 
	 * Creates HashMap...
	 */
	private void fillTransitionMatrix() throws IOException {
		transitionMatrix = new double[tagger.numTags()][tagger.numTags()];
		dictionary = new HashMap<String, ArrayList<String>>();
		
		BufferedReader in = new BufferedReader(new FileReader(Constants.corpusPath));
		
		String line;
		while ((line = in.readLine()) != null) { 	// Read all lines from corpus.
			if (line.equals(""))
				continue;
			String[] sentences = line.split(Constants.terminalSign);
			for (String sentence : sentences) {
				sentence = tagger.tagString(sentence);			// Get tags for each word in sentence.
				String[] words = sentence.split("\\s");			// Make a list of single words.
				int previousTag = -1;							// There is no previous tag yet.
				for (String word : words) {						// For ever word in word...
					if (word.length() > 0) {
						if (previousTag == -1) {				// Go in here if it is the first word of the line.
							previousTag = tagger.getTagIndex(word.split("_")[1]);
							if (previousTag != -1)
								initialSigns[previousTag]++;	
						} else {								// Go here if it is not the first word of the line.
							int currentTag = tagger.getTagIndex(word.split("_")[1]);
							if (currentTag != -1) {
								transitionMatrix[previousTag][currentTag]++;
								previousTag = currentTag;
							}
						}
						// Add word to dictionary.
						String[] tempWord = word.split("_");
						addToDictionary(dictionary, tempWord[1], tempWord[0]);
					}
				}
				// Last tag goes to terminal sign.
				if (previousTag != -1) {
					transitionMatrix[previousTag][terminalTagIndex]++;
				}
			}
		}
		in.close();
	}
	
	private void fillTags() {
		tags = new String[tagger.numTags()];
		for (int i = 0; i < tagger.numTags(); i++) {
			tags[i] = tagger.getTag(i);
		}
	}
	
	/*
	 * Normalize a matrix. If the sum of a row is 0 then do nothing with that row.
	 * @param matrix the matrix to be normalized.
	 */
	private void normalizeTransitionMatrix() {
		for (int i = 0; i < transitionMatrix.length; i++) {
			double c = 1 / arraySum(transitionMatrix[i]);
			if (c == Double.POSITIVE_INFINITY || c == Double.NEGATIVE_INFINITY || c == Double.NaN) { // Vad ska vi göra om hela raden är 0?
				/*
				for (int j = 0; j < matrix[i].length; j++) {
					if (j == i) {
						transitionMatrix[i][j] = 1; 	// Låta den gå till sig själv endast?
					} else {
						transitionMatrix[i][j] = 0;
					}
				}
				*/
			} else {
				for (int j = 0; j < transitionMatrix[i].length; j++) {
					transitionMatrix[i][j] = c * transitionMatrix[i][j];
				}
			}
		}
	}
	
	/*
	 * Normalize an array. If the sum of the array is 0 then do nothing, just return the array.
	 * @param array the array to be normalized.
	 * @return the normalized array.
	 */
	private void normalizeInitialSigns() {
		double c = 1 / arraySum(initialSigns);
		for (int i = 0; i < initialSigns.length; i++) {
			initialSigns[i] = c * initialSigns[i];
		}
	}
	
	/*
	 * @param dArray the array to sum.
	 * @return the sum of all elements in dArray.
	 */
	public double arraySum(double[] dArray) {
		double sum = 0.0;
		for (double d : dArray) {
			sum += d;
		}
		return sum;
	}
	
	/*
	 * Add a word to the dictionary with key == tag
	 * @param dictionary the dictionary
	 * @param tag the tag
	 * @param word the word
	 */
	private void addToDictionary(HashMap<String, ArrayList<String>> dictionary, String tag, String word) {
		ArrayList<String> list = dictionary.get(tag);
		if(list == null) list = new ArrayList<String>();
		
		//System.out.println(word + "\t" + tag);
		list.add(word);
		dictionary.put(tag, list);
	}
	
	public String getTag(String word) {
		String[] s = tagger.tagString(word).split("_");
//		System.out.println(s[s.length - 1]);
		return s[s.length - 1].trim();
	}
	
	public double[][] getMatrix() {
		return transitionMatrix;
	}
	
	public HashMap<String, ArrayList<String>> getDictionary() {
		return dictionary;
	}
	
	public String[] getTags() {
		return tags;
	}
	
	public double[] getInitialDistribution() {
		return initialSigns;
	}
}