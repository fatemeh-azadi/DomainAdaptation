import java.io.*;
import java.util.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;

public class TopicModel {

	int numberOfTopics = 100;
	int iterations = 1000;
	int numberOfThreads = 2;
	double alpha = 1., beta = 0.01;
	int numberOfTopWords = 50;
	int numberOfTopTopics = 15;
	HashMap<Integer, Double> topicProbs;
	HashMap<String, HashMap<Integer, Double> > wordTopics;
	HashMap<Integer, HashMap<String, Double> > topicWords;

	public TopicModel(int numTopics, int iterations, int numThreads, double alpha,
				double beta, int numberOfTopWords, int numberOfTopTopics){
		numberOfTopics = numTopics;
		numberOfThreads = numThreads;
		this.iterations = iterations;
		this.alpha = alpha;
		this.beta = beta;
		topicProbs = new HashMap<Integer, Double>();
		wordTopics = new HashMap<String, HashMap<Integer, Double>>();
		topicWords = new HashMap<Integer, HashMap<String, Double>>();
		this.numberOfTopWords = numberOfTopWords;
		this.numberOfTopTopics = numberOfTopTopics;

	}

	public void createModel(String corpusAdd, String stopListAdd, String modelFile) throws IOException{

		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
		pipeList.add( new CharSequenceLowercase() );
		pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
		pipeList.add( new TokenSequenceRemoveStopwords(new File(stopListAdd), "UTF-8", false, false, false) );
		pipeList.add( new TokenSequence2FeatureSequence() );

		InstanceList instances = new InstanceList (new SerialPipes(pipeList));

		Reader fileReader = new InputStreamReader(new FileInputStream(new File(corpusAdd)), "UTF-8");
		instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
				3, 2, 1)); // data, label, name fields

		ParallelTopicModel model = new ParallelTopicModel(numberOfTopics, alpha, beta);

		model.addInstances(instances);

		model.setNumThreads(numberOfThreads);
		model.setNumIterations(iterations);
		model.estimate();

		model.write(new File(modelFile));


	}

	public void readModel(String modelFile) throws Exception{

		ParallelTopicModel model = ParallelTopicModel.read(new File(modelFile));

		// The data alphabet maps word IDs to strings
		Alphabet dataAlphabet = model.getAlphabet();
		numberOfTopics = model.getNumTopics();

//		System.out.println(numberOfTopics);
		double p[] = new double[numberOfTopics];
		Arrays.fill(p, 0.0);
		int numOfInstances = model.getData().size();
//		System.out.println(numOfInstances);
		for (int i = 0; i < numOfInstances; i++) {
			double[] topicDistribution = model.getTopicProbabilities(i);

			for (int j = 0; j < topicDistribution.length; j++){
				p[j] += topicDistribution[j];
			}

		}

		for (int j = 0; j < numberOfTopics; j++){
			topicProbs.put(j, p[j] / (double)numOfInstances);
//			System.out.print(topicProbs.get(j) + " ");
		}
//		System.out.println();

		// Get an array of sorted sets of word ID/count pairs
		ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();

		// Show top 5 words in topics with proportions for the first document
		for (int topic = 0; topic < numberOfTopics; topic++) {
			Iterator<IDSorter> iterator = topicSortedWords.get(topic)
					.iterator();

			int rank = 0;
//			System.out.print(topic);
			HashMap<String, Double> words = new HashMap<String, Double>();
			while (iterator.hasNext() && rank < numberOfTopWords) {
				IDSorter idCountPair = iterator.next();

				String word = (String)dataAlphabet.lookupObject(idCountPair.getID()); 
				words.put(
						word,
						idCountPair.getWeight()); // topics word
//				System.out.print(word + " " + idCountPair.getWeight() + " ||| ");
				HashMap<Integer,Double> tmp_topic_weight=new HashMap<Integer,Double>();
				if(wordTopics.containsKey(word)){
					tmp_topic_weight=wordTopics.get(word);
				}
				tmp_topic_weight.put(topic, idCountPair.getWeight());
				wordTopics.put(word, tmp_topic_weight);
				rank++;
			}
//			System.out.println();
			topicWords.put(topic, words);

		}

		for(String word:wordTopics.keySet()){
			HashMap<Integer,Double> tmp = wordTopics.get(word);
			List<Pair<Double, Integer>> sortedList = new ArrayList();
			for(Map.Entry<Integer, Double> entry:tmp.entrySet()){
				sortedList.add(new Pair(entry.getValue(), entry.getKey()));
			}
			Collections.sort(sortedList);
			Collections.reverse(sortedList);
			HashMap<Integer,Double> wordTopTopics=new HashMap<Integer,Double>();

//			System.out.print(word + " ");
			for(int i = 0; i < numberOfTopTopics && i < sortedList.size(); i++){
				wordTopTopics.put(sortedList.get(i).getSecond(), sortedList.get(i).getFirst());
//				System.out.print(sortedList.get(i).getSecond() + " " + sortedList.get(i).getFirst() + " ||| ");
			}
//			System.out.println();
			wordTopics.put(word, wordTopTopics);
		}          

	}
	
	public double getSentenceScore(String s){
		double score = 0;
		double topicWeights[] = new double[numberOfTopics];
		String words[] = s.split("\\s+");
		
		for(int t = 0; t < numberOfTopics; t++){
			topicWeights[t] = 0;
			HashMap<String, Double> tWords = topicWords.get(t);
			for(int i = 0; i < words.length; i++){
				if(tWords.containsKey(words[i]))
					topicWeights[t] += 1.;
			}
		}
		
		for(int t = 0; t < numberOfTopics; t++){
			score += (topicWeights[t] * topicProbs.get(t));
		}
		score /= (double)words.length;
		return score;
	}
		
}

class Pair<F,S> implements Comparable<Pair<F, S>>, Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 17542930513205536L;
	private F first;
	private S second;

	public Pair(F first, S second) { 
		this.first = first;
		this.second = second;
	}

	public F getFirst() { return first; }
	public S getSecond() { return second; }

	@Override
	public int compareTo(Pair<F,S> o) {
		// TODO Auto-generated method stub
		if(((Comparable<F>) first).compareTo(o.first) == 0)
			return ((Comparable<S>) second).compareTo(o.second);
		return ((Comparable<F>) first).compareTo(o.first);

	}
}

