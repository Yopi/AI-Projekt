import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class NGram {
	private ArrayList<ArrayList<String[]>> nSeries;
	
	public NGram(int gram) {
		MaxentTagger tagger = new MaxentTagger(Constants.taggerPath);
		
		gram = gram + 1;
		nSeries = new ArrayList<ArrayList<String[]>>();
		
		for (int i = 0; i < gram; i++) {
			nSeries.add(new ArrayList<String[]>());
		}
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(Constants.corpusPath));
			
			String line;
			boolean startOfSentence = false;
			int howManyLeft = 0;
			while ((line = in.readLine()) != null) {
				if (line.equals("")) continue;
				line = tagger.tagString(line);
				
				String[] words;
				String[] newSentenceWordList = new String[gram-1];
				words = line.split("\\s");
				for (int i = 0; i < words.length; i++) {
					int length = Math.min(i+1, gram);
					String[] wordList = new String[length];
					length = Math.min(i, gram-1);
					int minus = 0;
					
					if (startOfSentence) {
						newSentenceWordList[howManyLeft] = words[i];
						nSeries.get(howManyLeft).add(Arrays.copyOfRange(newSentenceWordList, 0, howManyLeft+1));
						howManyLeft++;
						if (howManyLeft == gram-1) {
							startOfSentence = false;
							howManyLeft = 0;
						}
					}
					
					for (int j = i; j >= 0 && length - minus >= 0; j--) {
						wordList[length - minus] = words[j];
						minus++;
					}
					nSeries.get(length).add(wordList);
					
					if (split(words[i])[0].matches(Constants.terminalSign)) {
						startOfSentence = true;
					}
				}
			}
			
			in.close();
		} catch(IOException e) {
			System.err.println("n-gram could not be generated");
			System.exit(-1);
		}
	}
	
	private String[] split(String word) {
		String[] s = new String[2];
		int i = word.lastIndexOf("_");
		s[0] = word.substring(0, i);
		s[1] = word.substring(i + 1, word.length());
		return s;
	}
	
	public ArrayList<String> getNextWord(String previousWords) {
		String[] s;
		if (previousWords.equals("")) {
			s = new String[0];
		} else {
			s = previousWords.split(Constants.terminalSign);
			previousWords = s[s.length-1];
			s = previousWords.split("\\s");
		}
		String[] relWords = new String[Math.min(s.length, nSeries.size()-1)];
		System.arraycopy(s, s.length - relWords.length, relWords, 0, relWords.length);
		return getNextWord(relWords);
	}
	
	public ArrayList<String> getNextWord(String[] previousWords) {
		if (previousWords == null) {
			previousWords = new String[0];
		}
		
		if (previousWords.length >= nSeries.size()) {
			return null;
		}
		
		ArrayList<String> nextWord = new ArrayList<String>();
		
		ArrayList<String[]> nS = nSeries.get(previousWords.length);
		
		for (int i = 0; i < nS.size(); i++) {
			if (Collections.indexOfSubList(Arrays.asList(nS.get(i)), Arrays.asList(previousWords)) == 0) {
				nextWord.add(nS.get(i)[previousWords.length]);
			}
		}
		return nextWord;
	}
	
	void printArray(ArrayList<ArrayList<String[]>> doubleArray) {
		for (ArrayList<String[]> array : doubleArray) {
			for (String[] sa : array) {
				if (sa.length > 1) {
					System.out.print(sa[0]);
					for (int i = 1; i < sa.length; i++) {
						System.out.print(", " + sa[i]);
					}
				} else {
					System.out.print(sa[0]);
				}
				System.out.println();
			}
		}
	}
}
