/*
 * Inserts the documents into a HashMap.
 * The document stopwords.txt was used to remove the stopwords, such as articles, etc.
 * MRR is calculated afterwards.
 * @author Fernando Garza - 146368
 */
package edu.cmu.lti.f13.hw4.hw4_146368.annotators;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.f13.hw4.hw4_146368.VectorSpaceRetrieval;
import edu.cmu.lti.f13.hw4.hw4_146368.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_146368.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_146368.utils.Utils;

public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
		if (iter.isValid()) {
			iter.moveToNext();
			Document doc = (Document) iter.get();
			try {
				createTermFreqVector(jcas, doc);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	/**
	 * 
	 * @param jcas
	 * @param doc
	 * @throws Exception 
	 */

	private void createTermFreqVector(JCas jcas, Document doc) throws Exception {
		ArrayList<Token> arrayList = new ArrayList<Token>();
		//Generate frequency map
		HashMap<String, Integer> mapa_palabras = new HashMap<String, Integer>();
		//Added: remove stopwords
		String sLine;
		String docstopwords = "";
		URL stopwUrl = VectorSpaceRetrieval.class.getResource("/stopwords.txt");
	    if (stopwUrl == null) {
	       throw new IllegalArgumentException("Error opening /stopwords.txt");
	    }
			BufferedReader br = new BufferedReader(new InputStreamReader(stopwUrl.openStream()));
			while ((sLine = br.readLine()) != null)   {
				docstopwords = docstopwords + "\n" + sLine;
			}
			br.close();
			br=null;
		HashMap<String,Integer> mapa_stopwords = new HashMap<String,Integer>();
		String[] stopword = docstopwords.split("\n");
		for(String dato_sw : stopword){
			mapa_stopwords.put(dato_sw,0);
		}
			
		String text = doc.getText();
		int flag=0;
		String[] palabra = text.split("\\s+");
		for(String dato : palabra){
		if(mapa_stopwords.containsKey(dato)){
		}else{
		  if(mapa_palabras.containsKey(dato)){
			  flag = mapa_palabras.get(dato)+1;
		  }else{
			  flag = 1;
		  }
		    mapa_palabras.put(dato, flag);
		}
		}
		//Add tokens in cas
		for(String tok : mapa_palabras.keySet()){
		  Token token  = new Token(jcas);
		  token.setText(tok.toLowerCase());
		  token.setFrequency(mapa_palabras.get(tok));
		  token.addToIndexes();
		  arrayList.add(token);
		}
		FSList  list=Utils.fromCollectionToFSList(jcas, arrayList);
		doc.setTokenList(list);
	}

}
