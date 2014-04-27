/*
 * A class to evaluate each of the documents vs. the query. 
 * Cosine similarity was implemented.
 * MRR is calculated afterwards.
 * @author Fernando Garza - 146368
 */
package edu.cmu.lti.f13.hw4.hw4_146368.casconsumers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f13.hw4.hw4_146368.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_146368.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_146368.utils.Utils;


public class RetrievalEvaluator extends CasConsumer_ImplBase {

	/** query id number **/
	public ArrayList<Integer> qIdList;

	/** query and text relevant values **/
	public ArrayList<Integer> relList;
	
	public ArrayList<HashMap<String, Integer>> stringFreqList;
	public ArrayList<Double> rankArray;
	public ArrayList<Double> scoreArray;
	public ArrayList<Integer> docs;

		
	public void initialize() throws ResourceInitializationException {

		qIdList = new ArrayList<Integer>();

		relList = new ArrayList<Integer>();
		
		stringFreqList = new ArrayList<HashMap<String,Integer>>();

	}

	/**
	 * TODO :: 1. construct the global word dictionary 2. keep the word
	 * frequency for each sentence
	 */
	@Override
	public void processCas(CAS aCas) throws ResourceProcessException {

		JCas jcas;
		try {
			jcas =aCas.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}

		FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();
	
		if (it.hasNext()) {
			Document doc = (Document) it.next();

			//Make sure that your previous annotators have populated this in CAS
			FSList fsTokenList = doc.getTokenList();
			ArrayList<Token> tokenList = Utils.fromFSListToCollection(fsTokenList, Token.class);

			qIdList.add(doc.getQueryID());

			relList.add(doc.getRelevanceValue());
			
			
			//Do something useful here
			HashMap<String, Integer> freqMap = new HashMap<String, Integer>();
			for(Token token : tokenList){
			  String word = token.getText();
			  int freq = token.getFrequency();
			  freqMap.put(word, freq);
			}
			stringFreqList.add(freqMap);

		}

	}

	/**
	 * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2.
	 * Compute the MRR metric
	 */
	@Override
	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {
		super.collectionProcessComplete(arg0);
		scoreArray = new ArrayList<Double>();

		rankArray = new ArrayList<Double>();		
		int sindex = 0;
		ArrayList<Dataq> DataSet = new ArrayList<Dataq>();

		// TODO :: compute the cosine similarity measure
		for(int i=0; i<qIdList.size(); i++){

		int query = 0;
		docs = new ArrayList<Integer>();
		int dId = qIdList.get(i);
			if(relList.get(i)==99){
				query = i;
				for(int j=0; j<qIdList.size(); j++){
					if(qIdList.get(j) == dId){
					if(relList.get(j)!= 99){
						  HashMap<String, Integer> queryMap= stringFreqList.get(query);
						  HashMap<String, Integer> docMap = stringFreqList.get(j);
						  double score = computeCosineSimilarity(queryMap, docMap);
						  DataSet.add(new Dataq(qIdList.get(j), relList.get(j),score));
						  sindex++;
					}
					Collections.sort(DataSet, new ComparatorS());
					}
				}
			}

		}
int dqid=0;
int rank = 1;
for(Dataq data: DataSet){
	if(data.qid==dqid){
		if(data.rel == 0){
			rank++;
		}else{
			rankArray.add(1.0/rank);		
		}
	}else{
		rank = 1;
		dqid++;
		if(data.rel == 0){
			rank++;
		}else{
			rankArray.add(1.0/rank);		
		}		
	}
}

		
		// TODO :: compute the rank of retrieved sentences

		
		// TODO :: compute the metric:: mean reciprocal rank
		double metric_mrr = compute_mrr();
		System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
	}

	/**
	 * 
	 * @return cosine_similarity
	 */
	private double computeCosineSimilarity(Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		double cosine_similarity=0.0;
		int cumulative_docs = 0;
		int cumulative_query = 0;
		double innerprod = 0.0;
		double corrit_r = -2 + (double)(Math.random()*-3); 

		for(String data: docVector.keySet()){
			cumulative_docs+=(Math.pow(docVector.get(data), 2));
		}		
		for(String data: queryVector.keySet()){
			cumulative_query+=(Math.pow(queryVector.get(data), 2));
		}
		for(String dataq : queryVector.keySet()){
		      for(String datad: docVector.keySet()){
		        if(dataq.matches(datad))
		          innerprod +=
		          (1.0*(queryVector.get(dataq)*docVector.get(datad)));
		      }
		    }
		cosine_similarity = innerprod/(1.0*Math.sqrt(cumulative_docs*cumulative_query));
if(cosine_similarity==0.0){
cosine_similarity = corrit_r;	
}
		return cosine_similarity;
	}

	/**
	 * 
	 * @return mrr
	 */
	private double compute_mrr() {
		double metric_mrr=0.0;

		// TODO :: compute Mean Reciprocal Rank (MRR) of the text collection
		for(double data : rankArray){
			  System.out.println("rank : " + data);
			  metric_mrr += data;
		}
			metric_mrr = metric_mrr/rankArray.size();
		return metric_mrr;
	}
	public class ComparatorS implements Comparator<Dataq> {
	    public int compare(Dataq v1, Dataq v2) {
	    if(v1.qid == v2.qid){
		if (v1.score <= v2.score){
	                    return 1;
	            }else{
	                    return -1;
	            }

	    }else{
	    return 0;
	    }
	    }
		}
	public class Dataq
	{
	    public Dataq(Integer integer, Integer integer2, double score2) {
	    	this.qid = integer;
	    	this.rel = integer2;
	    	this.score = score2;
		}
		public int qid;
	    public int rel;
	    public double score;

	    // Add constructor, get, set, as needed.
	}


}
