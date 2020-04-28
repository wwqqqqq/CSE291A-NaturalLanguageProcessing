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
    int[] unigramCounter = new int[5000000];    
    
	System.out.println("Building Trigram Model . . .");
	
	System.out.println("Building Fertility Map...");
	FertilityCount fc = new FertilityCount(trainingData);
	System.out.println("Done building Fertility Map.");
    
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
	NgramModel bigram = new NgramModel(2, trainingData);
	System.out.println("Done building Bigram Table.");
	
	System.out.println("Building Trigram Table...");
	NgramModel trigram = new NgramModel(3, trainingData);
	System.out.println("Done building Trigram Table.");

	System.out.println("Done building Trigram Model.");
	
    return new NgramLanguageModel(){
		// Kneser-Ney Trigram Language Model
		@Override
		public int getOrder() {
			return 3;
		}
		
		private int getFertilityCount(int[] prev, int from, int to) {
			// c'(prev[from,to],word)
			// For highest order:
			//	token count of the n-gram
			// For others
			// 	|{u:c(u,x)>0}|
			if(from >= to || from >= prev.length || to < 0) {
				return 0;
			}
			if(from - to == 3) {
				// return count
				long prefix = NgramUtils.getConcatenateIndex(prev[from], prev[from+1]);
				return trigram.getPrefixWordCount(prefix, prev[from+2]);
			}
			// else if(from - to == 2 || from - to == 1) {
			// 	return fc.getFertilityCountforSuffix(prev, from, to-1, prev[to-1]);
			// }
			return 0;
		}
		
		private int getContextCount(int[] prev, int from, int to) {
			// \sum_v c'(prev[from,to],v)
			// For highest order:
			//	token count of the n-gram
			// For others
			// 	|{u:c(u,x)>0}|
			if(from > to || from >= prev.length || to < 0) {
				return 0;
			}
			if(from - to == 2) {
				// return count
				// sum_v count(prev, v)
				return bigram.getPrefixWordCount((long)prev[from], prev[from+1]);
			}
			// else if(from - to == 1) {
			// 	return fc.getFertilityCountforMiddle(prev, from, to);
			// }
			// else if(from - to == 0) {
			// 	return fc.getBigramCount();
			// }
			return 0;
		}

		private double getDiscount(int[] prev, int from, int to) {
			if(from >= to || from >= prev.length || to < 0) {
				return 0;
			}
			if(from - to == 2) {
				// return count
				return NgramUtils.d * fc.getFertilityCountforPrefix(prev, from, to);
			}
			else if(from - to == 1) {
				return NgramUtils.d * fc.getFertilityCountforPrefix(prev, from, to);
			}
			return 0;
		}

		private double getNgramProbability(int[] ngram, int from, int to) {
			for(int i = from; i < to; i++) {
				if(ngram[i] >= wordCounter.length || ngram[i] < 0) {
					return getNgramProbability(ngram, i+1, to);
				}
			}
			if(from >= to || from >= ngram.length || to < 0) {
				return 0;
			}

			double count = getContextCount(ngram, from, to-1);

			if(count == 0.0) {
				return getNgramProbability(ngram, from + 1, to);
			}

			if(from - to == 1) {
				// unigram
				return getFertilityCount(ngram, from, to) / count;
			}

			double fertility = getFertilityCount(ngram, from, to) - NgramUtils.d;
			if(fertility < 0.0) {
				fertility = 0.0;
			}

			// calculate alpha
			// double alpha = getDiscount(ngram, from, to-1) / count;

			return (fertility / count);// + (alpha * getNgramProbability(ngram, from + 1, to));
		}
		
	
		@Override
		public double getNgramLogProbability(int[] ngram, int from, int to) {
			if(to - from > 3) {
			System.out.println("WARNING: to - from > 3 for Trigram LM");
			}
	
			double prob = getNgramProbability(ngram, from, to);
			if(prob == 0) {
				return -100;
			}
			return Math.log(prob);

		}
		
		@Override
		public long getCount(int[] ngram) {
			try {
				if(ngram.length == 1) {
					int word = ngram[0];
					if (word < 0 || word >= wordCounter.length) return 0;
					return wordCounter[word];
				}
				else if(ngram.length == 2) {
					return bigram.getPrefixWordCount((long) ngram[0], ngram[1]);
				}
				else if(ngram.length == 3) {
					long prefix = NgramUtils.getConcatenateIndex(ngram[0], ngram[1]);
					return trigram.getPrefixWordCount(prefix, ngram[2]);
				}
			}
			catch (Exception e) {
				System.out.println("Something went wrong in getCount");
			}
			return 0;
		}
    }; 
  }
}
