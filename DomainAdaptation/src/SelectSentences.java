import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;


public class SelectSentences {

	static String srcCorpus = "", trgCorpus = "";
	static String outputFolder = "", scoresAdd = "";
	static int numberOfSentences = 0; 
	static String method = "src";
	static ArrayList<Pair<Double, Integer>> scores;

	static double in_weight = 1, out_weight = 1;
	
	public static void getArgs(String[] args){
		for(int i = 0; i < args.length; i+= 2){
			if(args[i].equals("--src-out"))
				srcCorpus = args[i + 1];
			else if(args[i].equals("--trg-out"))
				trgCorpus = args[i + 1];
			else if(args[i].equals("--scores"))
				scoresAdd = args[i + 1];
			else if(args[i].equals("--output-path"))
				outputFolder = args[i + 1];
			else if(args[i].equals("-n"))
				numberOfSentences = Integer.parseInt(args[i + 1]);
			else if(args[i].equals("--method"))
				method = args[i + 1];

			else if(args[i].equals("--weight-in"))
				in_weight = Double.parseDouble(args[i + 1]);
			else if(args[i].equals("--weight-out"))
				out_weight = Double.parseDouble(args[i + 1]);
				
		}
		if(srcCorpus.equals("") || trgCorpus.equals("") || 
				 outputFolder.equals("") || scoresAdd.equals("") || numberOfSentences == 0){
			System.err.println("inputs are not complete!");
			System.exit(1);
		}
	}
	
	public static void readScores() throws IOException{
		scores = new ArrayList<Pair<Double, Integer>>();
		
		BufferedReader scoresReader = new BufferedReader(new FileReader(scoresAdd));
		String line = "";
		int lineNum = 0;
		double src_in, src_out, trg_in, trg_out;
		while((line =scoresReader.readLine()) != null){
			Scanner sc = new Scanner(line);
			lineNum = sc.nextInt();
			if(method.equals("src")){
				src_in = sc.nextBigDecimal().doubleValue();
				src_out = sc.nextBigDecimal().doubleValue();
				scores.add(new Pair<Double, Integer>(in_weight * src_in - out_weight * src_out, lineNum));
			}else if(method.equals("trg")){
				trg_in = sc.nextBigDecimal().doubleValue();
				trg_out = sc.nextBigDecimal().doubleValue();
				scores.add(new Pair<Double, Integer>(in_weight * trg_in - out_weight * trg_out, lineNum));
			}else if(method.equals("src-trg")){
				src_in = sc.nextBigDecimal().doubleValue();
				src_out = sc.nextBigDecimal().doubleValue();
				trg_in = sc.nextBigDecimal().doubleValue();
				trg_out = sc.nextBigDecimal().doubleValue();
				scores.add(new Pair<Double, Integer>(in_weight*src_in - out_weight*src_out + in_weight*trg_in - out_weight*trg_out, lineNum));
			}else{
				System.err.println("Invalid Method : " + method + " !");
				System.exit(1);
			}
			System.out.println(lineNum);
		}
		scoresReader.close();
	}
	
	public static void selectSentences() throws IOException{
	
		readScores();
		Collections.sort(scores);
		Collections.reverse(scores);
		boolean mark[] = new boolean[scores.size()];
		Arrays.fill(mark, false);
		for(int i = 0; i <scores.size() && i < numberOfSentences && scores.get(i).getFirst() > 0.0; i++){
			mark[scores.get(i).getSecond()] = true;
		}
		
		int lineNum = 0;
		String srcOutName = (new File(srcCorpus)).getName() + ".selected";
		String trgOutName = (new File(trgCorpus)).getName() + ".selected";
		String srcNotSelectedOutName = (new File(srcCorpus)).getName() + ".notselected";
		String trgNotSelectedOutName = (new File(trgCorpus)).getName() + ".notselected";
		BufferedReader srcIn = new BufferedReader(new FileReader(srcCorpus));
		BufferedReader trgIn = new BufferedReader(new FileReader(trgCorpus));
		BufferedWriter srcOut = new BufferedWriter(new FileWriter(outputFolder + "/" + srcOutName));
		BufferedWriter trgOut = new BufferedWriter(new FileWriter(outputFolder + "/" + trgOutName));
		BufferedWriter srcOut_notSel = new BufferedWriter(new FileWriter(outputFolder + "/" + srcNotSelectedOutName));
		BufferedWriter trgOut_notSel = new BufferedWriter(new FileWriter(outputFolder + "/" + trgNotSelectedOutName));
		
		String srcLine = "" , trgLine = "";
		while((srcLine = srcIn.readLine()) != null){
			trgLine = trgIn.readLine();
			
			if(mark[lineNum]){
				srcOut.write(srcLine + "\n");
				trgOut.write(trgLine + "\n");
			}else{
				srcOut_notSel.write(srcLine + "\n");
				trgOut_notSel.write(trgLine + "\n");
			}
				
			lineNum++;
		}
		srcIn.close();
		srcOut.close();
		trgIn.close();
		trgOut.close();
		srcOut_notSel.close();
		trgOut_notSel.close();
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		getArgs(args);
		selectSentences();
	}

}
