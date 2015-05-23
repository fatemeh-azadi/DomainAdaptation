import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class ScoreSentences {

	static int numberOfTopics = 100;
	static int iterations = 1000;
	static int numberOfThreads = 2;
	static double alpha = 1., beta = 0.01;
	static int numberOfTopWords = 50;
	static int numberOfTopTopics = 15;
	static String srcCorpus_in = "", srcCorpus_out = "", trgCorpus_in = "", trgCorpus_out = "";
	static String srcStopList = "", trgStopList = "";
	static String outputFolder = "";
	static String method = "src-trg";
	
	public static void getArgs(String[] args){
		for(int i = 0; i < args.length; i+= 2){
			if(args[i].equals("--src-in"))
				srcCorpus_in = args[i + 1];
			else if(args[i].equals("--trg-in"))
				trgCorpus_in = args[i + 1];
			else if(args[i].equals("--src-out"))
				srcCorpus_out = args[i + 1];
			else if(args[i].equals("--trg-out"))
				trgCorpus_out = args[i + 1];
			else if(args[i].equals("--src-stopList"))
				srcStopList = args[i + 1];
			else if(args[i].equals("--trg-stopList"))
				trgStopList = args[i + 1];
			else if(args[i].equals("--output-path"))
				outputFolder = args[i + 1];
			else if(args[i].equals("--topics"))
				numberOfTopics = Integer.parseInt(args[i + 1]);
			else if(args[i].equals("--iterations"))
				iterations = Integer.parseInt(args[i + 1]);
			else if(args[i].equals("--threads"))
				numberOfThreads = Integer.parseInt(args[i + 1]);
			else if(args[i].equals("--alpha"))
				alpha = Double.parseDouble(args[i + 1]);
			else if(args[i].equals("--beta"))
				beta = Double.parseDouble(args[i + 1]);
			else if(args[i].equals("--topic-words"))
				numberOfTopWords = Integer.parseInt(args[i + 1]);
			else if(args[i].equals("--method"))
				method = args[i + 1];
		}
		if(method.equals("src") && ( srcCorpus_in.equals("") || 
				srcCorpus_out.equals("")|| 
				srcStopList.equals(""))){
			System.err.println("inputs are not complete!");
			System.err.println("needed parameters for method == src:");
			System.err.println("--src-in   src-in-domain-corpus");
			System.err.println("--src-out   src-out-domain-corpus");
			System.err.println("--src-stopList   src-stop-words-list");
			System.exit(1);
		}
		if(method.equals("trg") && ( trgCorpus_in.equals("") || 
				trgCorpus_out.equals("")|| 
				trgStopList.equals(""))){
			System.err.println("inputs are not complete!");
			System.err.println("needed parameters for method == trg:");
			System.err.println("--trg-in   trg-in-domain-corpus");
			System.err.println("--trg-out   trg-out-domain-corpus");
			System.err.println("--trg-stopList   trg-stop-words-list");
			System.exit(1);
		}
		if(method.equals("src-trg") && (srcCorpus_in.equals("") || trgCorpus_in.equals("") || 
				srcCorpus_out.equals("") || trgCorpus_out.equals("") || 
				srcStopList.equals("") || trgStopList.equals(""))){

			System.err.println("needed parameters for method == src-trg:");
			System.err.println("--src-in   src-in-domain-corpus");
			System.err.println("--src-out   src-out-domain-corpus");
			System.err.println("--src-stopList   src-stop-words-list");
			System.err.println("--trg-in   trg-in-domain-corpus");
			System.err.println("--trg-out   trg-out-domain-corpus");
			System.err.println("--trg-stopList   trg-stop-words-list");
			System.exit(1);
		}
		if(outputFolder.equals("")){
			System.err.println("needed parameter missed:");
			System.err.println("--output-path   outputs-path-address");
		}
	}

	public static TopicModel createTopicModel(String corpAdd, String stopAdd, String outAdd){
		TopicModel tm = new TopicModel(numberOfTopics, iterations, numberOfThreads,
				alpha, beta, numberOfTopWords, numberOfTopTopics);
		try {
			tm.createModel(corpAdd, stopAdd, outAdd);
			tm.readModel(outAdd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("creating " + (new File(outAdd)).getName() + " model failed!");
			System.exit(1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("reading " + (new File(outAdd)).getName() + " model failed!");
			System.exit(1);
		} 		
		return tm;
	}

	public static void calcScores() throws IOException{
		TopicModel src_in = null, src_out = null, trg_in = null, trg_out = null;
		if(method.equals("src") || method.equals("src-trg")){
			src_in = createTopicModel(srcCorpus_in, srcStopList, outputFolder + "/src_in_topicModel");
			src_out = createTopicModel(srcCorpus_out, srcStopList, outputFolder + "/src_out_topicModel");
		}

		if(method.equals("trg") || method.equals("src-trg")){
			trg_in = createTopicModel(trgCorpus_in, trgStopList, outputFolder + "/trg_in_topicModel");
			trg_out = createTopicModel(trgCorpus_out, trgStopList, outputFolder + "/trg_out_topicModel");
		}

		BufferedWriter scoreFile = new BufferedWriter(new FileWriter(outputFolder + "/scores"));
		BufferedReader inputSrc = null;
		if(method.contains("src"))
			inputSrc = new BufferedReader(new FileReader(srcCorpus_out));
		BufferedReader inputTrg = null;
		if(method.contains("trg"))
			inputTrg = new BufferedReader(new FileReader(trgCorpus_out));

		String srcLine = "" , trgLine = "";
		int lineNumber = 0;
		while(true){
			if(method.contains("src") && (srcLine = inputSrc.readLine()) == null)
				break;
			if(method.contains("trg") && (trgLine = inputTrg.readLine()) == null)
				break;
			scoreFile.write(lineNumber + "");
			if(method.contains("src")){
				scoreFile.write(" " + src_in.getSentenceScore(srcLine));
				scoreFile.write(" " + src_out.getSentenceScore(srcLine));
			}
			if(method.contains("trg")){
				scoreFile.write(" " + trg_in.getSentenceScore(trgLine));
				scoreFile.write(" " + trg_out.getSentenceScore(trgLine));
			}
			scoreFile.write("\n");
			lineNumber++;
		}
		scoreFile.close();
		if(method.contains("src"))
		inputSrc.close();
		if(method.contains("trg"))
		inputTrg.close();
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		getArgs(args);
		calcScores();

	}

}
