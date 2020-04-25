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

	System.out.println("Building Fertility Map...");
	FertilityCount fc = new FertilityCount(trainingData);
	System.out.println("Done building Fertility Map.");


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
				long prefix = NgramUtils.getConcatenateIndex(prev[from], prev[from+1]);
				return trigram.getPrefixWordCount(prefix, word);
			}
			else if(from - to == 1 || from - to == 0) {
				return fc.getFertilityCountforSuffix(prev, from, to, word);
			}
			return 0;
		}
		
		private int getContextCount(int[] prev, int from, int to) {
			if(from - to == 2) {
				// return count
				return bigram.getPrefixWordCount((long)prev[from], prev[from+1]);
			}
			else if(from - to == 1) {
				return fc.getFertilityCountforMiddle(prev, from, to);
			}
			return 0;
		}

		private double getContextCountwithDiscount(int[] prev, int from, int to) {
			if(from - to == 2) {
				// return count
				long prefix = NgramUtils.getConcatenateIndex(prev[from], prev[from+1]);
				return bigram.getPrefixWordCount((long)prev[from], prev[from+1]) - NgramUtils.d * fc.getFertilityCountforPrefix(prev, from, to);
			}
			else if(from - to == 1) {
				return fc.getFertilityCountforMiddle(prev, from, to) - NgramUtils.d * fc.getFertilityCountforPrefix(prev, from, to);
			}
			return 0;
		}
		
	
		@Override
		public double getNgramLogProbability(int[] ngram, int from, int to) {
			if(to - from > 3) {
			System.out.println("WARNING: to - from > 3 for Trigram LM");
			}

			for(int i = from; i < to; i++) {
				if(ngram[i] >= wordCounter.length || ngram[i] < 0) {
					return -100;
				}
			}
			
			try {
				if(to-from==3) {
					long prefix = NgramUtils.getConcatenateIndex(ngram[from], ngram[from+1]);

					double prob = trigram.getPrefixWordCount(prefix, ngram[from+2]);
					int prefixCount = bigram.getPrefixWordCount(ngram[from], ngram[from+1]);
					if(prob == 0) {
						// System.out.println("WARNING: word count is 0");
						return -100;
					}
					prob = prob / prefixCount;

					return Math.log(prob);
				}
				else if(to-from==2) {
					double prob = bigram.getPrefixWordCount(ngram[from], ngram[from+1]);
					if(prob == 0) {
						return -100;
					}
					prob = prob / wordCounter[ngram[from]];
					return Math.log(prob);
				}
				else if(to-from==1) {
					double prob = wordCounter[ngram[from]];
					if(prob == 0) {
						return -100;
					}
					prob = prob / wordCounter.length;
					return Math.log(prob);
				}
			}
			catch(Exception e) {
				System.out.println("Something went wrong in getNgramLogProbability.");
			}
			return -100;

			// double count = getContextCount(ngram, from, to-1);

			// if(count == 0.0) {
			// 	return 0;
			// }

			// if(from - to == 1) {
			// 	// unigram
			// 	return getFertilityCount(ngram, from, to-1, ngram[to-1]) / count;
			// }

			// double fertility = getFertilityCount(ngram, from, to-1, ngram[to-1]) - NgramUtils.d;
			// if(fertility < 0.0) {
			// 	fertility = 0.0;
			// }

			// // calculate alpha
			// double alpha = 1 - getContextCountwithDiscount(ngram, from, to-1) / count;

			// return fertility / count + alpha * getNgramLogProbability(ngram, from + 1, to);

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
