
/*
 * Retrieves vector space from text files. 
 * @author Fernando Garza - 146368
 */
package edu.cmu.lti.f13.hw4.hw4_146368;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.util.XMLInputSource;

public class VectorSpaceRetrieval {

	public static void main(String [] args) 
			throws Exception {
			
		String sLine;
		String sLine2;

		long startTime=System.currentTimeMillis();
		
		URL descUrl = VectorSpaceRetrieval.class.getResource("/descriptors/retrievalsystem/VectorSpaceRetrieval.xml");
	   if (descUrl == null) {
	      throw new IllegalArgumentException("Error opening VectorSpaceRetrieval.xml");
	   }
		// create AnalysisEngine		
		XMLInputSource input = new XMLInputSource(descUrl);
		AnalysisEngineDescription desc = UIMAFramework.getXMLParser().parseAnalysisEngineDescription(input);
		AnalysisEngine anAnalysisEngine = UIMAFramework.produceAnalysisEngine(desc);
		CAS aCas = anAnalysisEngine.newCAS();
		Set<String> stopWords = new HashSet<String>();
		 URL docUrl2 = VectorSpaceRetrieval.class.getResource("/stopwords.txt");
		    if (docUrl2 == null) {
		       throw new IllegalArgumentException("Error opening /stopwords.txt");
		    }
				BufferedReader br2 = new BufferedReader(new InputStreamReader(docUrl2.openStream()));
				while ((sLine2 = br2.readLine()) != null)   {
					stopWords.add(sLine2);
				}
				br2.close();
				br2=null;
	  URL docUrl = VectorSpaceRetrieval.class.getResource("/data/documents.txt");
    if (docUrl == null) {
       throw new IllegalArgumentException("Error opening data/documents.txt");
    }
		BufferedReader br = new BufferedReader(new InputStreamReader(docUrl.openStream()));
		while ((sLine = br.readLine()) != null)   {
			aCas.setDocumentText(sLine.toLowerCase());
			anAnalysisEngine.process(aCas);
			aCas.reset();
		}
		br.close();
		br=null;
		
		
		anAnalysisEngine.collectionProcessComplete();
		anAnalysisEngine.destroy();	
		long endTime=System.currentTimeMillis();
		
		double totalTime=(endTime-startTime)/1000.0;
		System.out.println("Total time taken: "+totalTime);
		

	}
	
	private String clean(String text, HashSet<String> stopwords){
		String data = "";
		String sampleText = text;
		StringBuffer clean = new StringBuffer();
		int index = 0;

		while (index < sampleText.length()) {
		  // the only word delimiter supported is space, if you want other
		  // delimiters you have to do a series of indexOf calls and see which
		  // one gives the smallest index, or use regex
		  int nextIndex = sampleText.indexOf(" ", index);
		  if (nextIndex == -1) {
		    nextIndex = sampleText.length() - 1;
		  }
		  String word = sampleText.substring(index, nextIndex);
		  if (!stopwords.contains(word.toLowerCase())) {
		    clean.append(word);
		    if (nextIndex < sampleText.length()) {
		      // this adds the word delimiter, e.g. the following space
		      clean.append(sampleText.substring(nextIndex, nextIndex + 1)); 
		    }
		  }
		  index = nextIndex + 1;
		}
		data = clean.toString();
		return data;
	}

}
