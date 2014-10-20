import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class NGram {
	ArrayList<ArrayList<String[]>> nSeries;
	ArrayList<String[]> nSeries;
	
	public static void main(String[] args) throws IOException { 
		new NGram(2); 
	}
	/*
	public NGram(int gram) throws IOException {
		nSeries = new ArrayList<ArrayList<String[]>>();
		
		BufferedReader in = new BufferedReader(new FileReader(Constants.corpusPath));
		
		String line;
		while((line = in.readLine()) != null) {
			String[] sentences = line.split(Constants.terminalSign);
			for (String sentence : sentences) {
				String[] words = sentence.split("\\s");
				for (int i = 0; i < words.length; i++) {
						String[] part = new String[Math.min(i + 1, gram)];
						int start = i;
						if (start > gram)
							start -= gram;
						System.arraycopy(words, 0, part, Math.min(i, gram-i+1), part.length);
						addSeries(j, part);
				}
			}
		}
	}
	*/
	
	public void addSeries(int seriesN, String[] s) {
		ArrayList<String[]> list;
		try { list = nSeries.get(seriesN); }
		catch (Exception e) {
			list = new ArrayList<String[]>();
			nSeries.add(list);
		}
		
		list.add(s);
	}
}
