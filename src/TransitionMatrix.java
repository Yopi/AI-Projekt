import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

class TransitionMatrix {
	private double[]   initialSigns;
	private double[][] transitionMatrix;
	private HashMap<String, ArrayList<String>> dictionary;
	private String[] tags;
	private int terminalTagIndex;
	public MaxentTagger tagger;
	public ArrayList<String> sentences;
	
	public static void main(String[] args) { 
		new TransitionMatrix(); 
	}
	
	public TransitionMatrix() {
		tagger = new MaxentTagger(Constants.taggerPath);
		terminalTagIndex = tagger.getTagIndex(".");
		
		transitionMatrix = new double[tagger.numTags()][tagger.numTags()];
		dictionary = new HashMap<String, ArrayList<String>>();
		initialSigns = new double[tagger.numTags()];
		
		// Create
		try {
			fillTransitionMatrix();
		} catch(IOException e) {
			System.err.println("Could not fill transitions matrix");
			System.exit(-1);
		}
		//mapWords(tagger);
		fillTags();
		
		// Normalize
		normalizeTransitionMatrix();
		initialSigns = normalizeArray(initialSigns);
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
		sentences = new ArrayList<String>();
	
		boolean successful = getSentencesFromFile();
		
		if (!successful) {
			getSentences();
		}
		
		String[] words;
		for(String sentence : sentences) {
			int previousTag = -1; // There is no previous tag yet.
			
			words = sentence.split("\\s");
			for (String word : words) {	// For every word in word...
				if (previousTag == -1) { 
					// Go in here if it is the first word of the line.
					previousTag = tagger.getTagIndex(word.split("_")[1]);
					initialSigns[previousTag]++;	
				} else {					
					// Go here if it is not the first word of the line.
					int currentTag = tagger.getTagIndex(word.split("_")[1]);
					transitionMatrix[previousTag][currentTag]++;
					previousTag = currentTag;
				}

				// Add word to dictionary.
				String[] tempWord = split(word);
				addToDictionary(dictionary, tempWord[1], tempWord[0]);
			}

			// Last tag goes to terminal sign.
			if (previousTag != -1) transitionMatrix[previousTag][terminalTagIndex]++;
		}
	}
	
	private boolean getSentencesFromFile() throws IOException{
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader("data/sentences.bin"));

            File file = new File(Constants.corpusPath);
            // Check if the file at Constants.corpusPath has changed.
            try {
                if (Long.parseLong(br.readLine()) != file.lastModified()) {
                    // The file has changed.
                    br.close();
                    throw new IOException();
                }
            } catch (NumberFormatException e) {
                br.close();
                throw new IOException();
            }
             
            String sentence;
            while ((sentence = br.readLine()) != null) {
                sentences.add(sentence);
            }
            br.close();
        } catch(IOException e) {
            return false;
        }
        return true;
    }
	
	private void getSentences() {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new FileReader(Constants.corpusPath));
			
			String line;
			while ((line = in.readLine()) != null) { 	// Read all lines from corpus.
				if (line.equals(""))
					continue;
				sb.append(line);
			}
			in.close();
		} catch (IOException e) {
			System.err.println("Fail reading error.");
			System.exit(1);
		}
		
		String[] tempSentences = sb.toString().split("(?<=[.!?])");
		for (String sentence : tempSentences) {
			sentence = sentence.replace("_", " ");
			sentence = tagger.tagString(sentence);  // Get tags for each word in line.
			sentences.add(sentence.trim());
		}
		saveSentencesToFile();
	}
     
    private void saveSentencesToFile() {
    	try {
	        // Save sentences to file.
	        File theFile = new File("data/sentences.bin");
	        theFile.createNewFile();
	        FileWriter fw = new FileWriter("data/sentences.bin");
	         
	        File file = new File(Constants.corpusPath);
	        fw.write(file.lastModified() + "\n");   // Save the last modified date of the file at Constants.corpusPath.
	        for (String sentence : sentences) {
	            fw.write(sentence + "\n");
	        }
	        fw.close();
    	} catch (IOException e) {
    		System.err.println("File saving error.");
    	}
    }
	
	private String[] split(String word) {
		String[] s = new String[2];
		int i = word.lastIndexOf("_");
		s[0] = word.substring(0, i);
		s[1] = word.substring(i + 1, word.length());
		return s;
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
	public double[] normalizeArray(double[] array) {
		double c = 1 / arraySum(array);
		for (int i = 0; i < array.length; i++) {
			array[i] = c * array[i];
		}
		return array;
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
		for (Entry<String, ArrayList<String>> entry : dictionary.entrySet()) {
		    ArrayList<String> words = entry.getValue();
		    if(words.contains(word)) 
		    	return entry.getKey();
		}
		
		return "";
	}
	
	public List<String> getTagsForWord(String word) {
		List<String> tags = new ArrayList<String>();
		word = word.split("_")[0];
		for (Entry<String, ArrayList<String>> entry : dictionary.entrySet()) {
		    ArrayList<String> words = entry.getValue();
		    if(words.contains(word)) 
		    	tags.add(entry.getKey());
		}

		if(tags.size() == 0) System.out.println(word + " did not have any tags at all");
		return tags;
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
	
	public int getTagIndex(String tag) {
		return tagger.getTagIndex(tag);
	}
}