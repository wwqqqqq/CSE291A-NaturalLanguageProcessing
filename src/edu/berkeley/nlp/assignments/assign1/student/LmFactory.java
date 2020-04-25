package edu.berkeley.nlp.assignments.assign1.student;

import java.util.*;

import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.langmodel.LanguageModelFactory;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.util.CollectionUtils;
import edu.berkeley.nlp.util.StringIndexer;


public class LmFactory implements LanguageModelFactory
{

  /**
   * Returns a new NgramLanguageModel; this should be an instance of a class that you implement.
   * Please see edu.berkeley.nlp.langmodel.NgramLanguageModel for the interface specification.
   * 
   * @param trainingData
   */
  public NgramLanguageModel newLanguageModel(Iterable<List<String>> trainingData) {

    final String STOP = NgramLanguageModel.STOP;
    int[] unigramCounter = new int[10];
    NgramHashMap bigram = new NgramHashMap(2);
	NgramHashMap trigram = new NgramHashMap(3);
	final double d = 0.75;
    
    
    System.out.println("Building Trigram Model . . .");
    
    System.out.println("Building wordCounter...");
	int sent = 0;
	// int max_count = 0;
	for (List<String> sentence : trainingData) {
		sent++;
		if (sent % 1000000 == 0) System.out.println("On sentence " + sent);
		List<String> stoppedSentence = new ArrayList<String>(sentence);
		stoppedSentence.add(0, NgramLanguageModel.START);
		stoppedSentence.add(STOP);
		for (String word : stoppedSentence) {
			int index = EnglishWordIndexer.getIndexer().addAndGetIndex(word);
			if (index >= unigramCounter.length) unigramCounter = CollectionUtils.copyOf(unigramCounter, unigramCounter.length * 2);
			unigramCounter[index]++;
			// if(unigramCounter[index] > max_count) {
			// 	max_count = unigramCounter[index];
			// }
		}
	}
	final int[] wordCounter = CollectionUtils.copyOf(unigramCounter, EnglishWordIndexer.getIndexer().size());
	System.out.println("Done building wordCounter.");
	// System.out.print(max_count); // 19880264 => 25 bits
	// System.out.print(EnglishWordIndexer.getIndexer().size()); // 495172 => 19 bits
	
	System.out.println("Building Bigram Table...");
	sent = 0;
	for (List<String> sentence : trainingData) {
		sent++;
		if (sent % 1000000 == 0) System.out.println("On sentence " + sent);
		List<String> stoppedSentence = new ArrayList<String>(sentence);
		stoppedSentence.add(STOP);
		int prev = EnglishWordIndexer.getIndexer().addAndGetIndex(NgramLanguageModel.START);
		for (String word : stoppedSentence) {
			int index = EnglishWordIndexer.getIndexer().addAndGetIndex(word);
			bigram.addPrefixWord((long)prev, index);
			prev = index;
		}
	}
	System.out.println("Done building Bigram Table.");
	
	System.out.println("Building Trigram Table...");
	sent = 0;
	for (List<String> sentence : trainingData) {
		sent++;
		if (sent % 1000000 == 0) System.out.println("On sentence " + sent);
		List<String> stoppedSentence = new ArrayList<String>(sentence);
		stoppedSentence.add(STOP);
		int N2 = EnglishWordIndexer.getIndexer().addAndGetIndex(NgramLanguageModel.START);
		int N1 = EnglishWordIndexer.getIndexer().addAndGetIndex(NgramLanguageModel.START);
		for (String word : stoppedSentence) {
			int index = EnglishWordIndexer.getIndexer().addAndGetIndex(word);
			long prefix = trigram.getConcatenateIndex(N2, N1);
			trigram.addPrefixWord(prefix, index);
			N2 = N1;
			N1 = index;
		}
	}
	System.out.println("Done building Trigram Table.");

	System.out.println("Building Fertility Map...");
	FertilityCount fc = new FertilityCount(trainingData);
	System.out.println("Done building Fertility Map.");

	System.out.println("Building Context Map...");
	ContextCount cc = new ContextCount(trainingData, d);
	System.out.println("Done building Context Map.");
	
	System.out.println("Done building Trigram Model.");
	
    return new NgramLanguageModel(){
      // Kneser-Ney Trigram Language Model
      @Override
      public int getOrder() {
        return 3;
      }
      
      private int getFertilityCount(int[] prev, int from, int to, int word) {
    	  if(from - to == 2) {
    		  // return count
    		  long prefix = trigram.getConcatenateIndex(prev[from], prev[from+1]);
    		  return trigram.getPrefixWordCount(prefix, word);
    	  }
    	  else if(from - to == 1 || from - to == 0) {
    		  return fc.getFertilityCount(prev, from, to, word)
    	  }
    	  return 0;
      }
      
   
      @Override
      public double getNgramLogProbability(int[] ngram, int from, int to) {
        if(to - from > 3) {
          System.out.println("WARNING: to - from > 3 for Trigram LM");
        }
        
        long prefix = ngram[0];
        // int total = bigram.getPrefixWordCount(prefix, ngram[1]);
		
		if(from - to == 1) {
			// unigram
			return getFertilityCount(ngram, from, to-1, ngram[to-1]) / cc.getFertilityCount(ngram, from, to-1);
		}
		
		// calculate alpha
		double alpha = 1 - cc.getFertilityCountwithDiscount(ngram, from, to-1) / cc.getFertilityCount(ngram, from, to-1);
        
		double fertility = getFertilityCount(ngram, from, to-1, ngram[to-1]) - d;
		if(fertility < 0.0) {
			fertility = 0.0;
		}

		if(from - to == 1) {
			return fertility / count;
		}

		double count = cc.getFertilityCount(ngram, from, to-1, ngram[to-1]);

		return fertility / count + alpha * getNgramLogProbability(ngram, from + 1, to);

      }
    
      @Override
      public long getCount(int[] ngram) {
        if(ngram.length == 1) {
        	int word = ngram[0];
        	if (word < 0 || word >= wordCounter.length) return 0;
        	return wordCounter[word];
        }
        else if(ngram.length == 2) {
        	return bigram.getPrefixWordCount((long) ngram[0], ngram[1]);
        }
        else if(ngram.length == 3) {
        	long prefix = trigram.getConcatenateIndex(ngram[0], ngram[1]);
        	return trigram.getPrefixWordCount(prefix, ngram[2]);
        }
        return 0;
      }
    }; 
  }
}
