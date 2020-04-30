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

	System.out.println("Building Trigram Model . . .");

	NgramModel model = new NgramModel(trainingData, -1);

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
			if(to - from == 3) {
				// return count
				long prefix = NgramUtils.getConcatenateIndex(prev[from], prev[from+1]);
				long word = NgramUtils.getConcatenateIndex(prefix, prev[from+2]);
				return model.getTrigramCount(word);
			}
			else if(to - from == 2 || to - from == 1) {
				return model.getSuffixFertilityCount(prev, from, to);
			}
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
			if(to - from == 2) {
				// return count
				// sum_v count(prev, v)
				long word = NgramUtils.getConcatenateIndex(prev[from], prev[from+1]);
				return model.getBigramCount(word);
			}
			else if(to - from == 1) {
				return model.getMiddleFertilityCount(prev, from, to);
			}
			else if(to - from == 0) {
				return model.getCombinationCount();
			}
			return 0;
		}

		private double getDiscount(int[] prev, int from, int to) {
			if(from >= to || from >= prev.length || to < 0) {
				return 0;
			}
			if(to - from == 2) {
				// return count
				return NgramUtils.d * model.getPrefixFertilityCount(prev, from, to);
			}
			else if(to - from == 1) {
				return NgramUtils.d * model.getPrefixFertilityCount(prev, from, to);
			}
			return 0;
		}

		private double getNgramProbability(int[] ngram, int from, int to) {
			for(int i = to - 1; i >= from; i--) {
				if(ngram[i] >= model.getWordCount() || ngram[i] < 0) {
					return getNgramProbability(ngram, i+1, to);
				}
			}
			if(from >= to || from >= ngram.length || to < 0) {
				return 0;
			}

			if(to - from == 3) {
				// P_3
				long prev2 = NgramUtils.getConcatenateIndex(ngram[from], ngram[from+1]);
				int prev2_index = model.getWordIndex(2, prev2);
				if(prev2_index < 0) {
					return getNgramProbability(ngram, from + 1, to);
				}
				double count = model.getBigramIndexCount(prev2_index);
				if(count == 0.0) {
					return getNgramProbability(ngram, from + 1, to);
				}
				double alpha = NgramUtils.d * model.getPrefixFertilityCount(2, prev2_index) / count;
				long key = NgramUtils.getConcatenateIndex(prev2, ngram[from+2]);
				double fertility = model.getTrigramCount(key) - NgramUtils.d;
				if(fertility < 0) {
					fertility = 0;
				}
				return fertility / count + alpha * getNgramProbability(ngram, from+1, to);
			}
			else if(to - from == 2) {
				int prev_index = ngram[from];
				int count = model.getMiddleFertilityCount(prev_index);
				if(count == 0.0) {
					return getNgramProbability(ngram, from + 1, to);
				}
				double alpha = NgramUtils.d * model.getPrefixFertilityCount(1, prev_index) / count;
				double fertility = model.getSuffixFertilityCount(ngram, from, to) - NgramUtils.d;
				if(fertility < 0) {
					fertility = 0;
				}
				return fertility / count + alpha * getNgramProbability(ngram, from+1, to);
			}
			else if(to - from == 1) {
				int count = model.getCombinationCount();
				if(count == 0.0) {
					return 0;
				}
				double fertility = model.getSuffixFertilityCount(ngram, from, to);
				return fertility / count;
			}
			return 0;

			// double count = getContextCount(ngram, from, to-1);

			// if(count == 0.0) {
			// 	return getNgramProbability(ngram, from + 1, to);
			// }

			// if(to - from == 1) {
			// 	// unigram
			// 	return getFertilityCount(ngram, from, to) / count;
			// }

			// double fertility = getFertilityCount(ngram, from, to) - NgramUtils.d;
			// if(fertility < 0.0) {
			// 	fertility = 0.0;
			// }

			// // calculate alpha
			// double alpha = getDiscount(ngram, from, to-1) / count;

			// return (fertility / count) + (alpha * getNgramProbability(ngram, from + 1, to));
		}
		
	
		@Override
		public double getNgramLogProbability(int[] ngram, int from, int to) {
			if(to - from > 3) {
			System.out.println("WARNING: to - from > 3 for Trigram LM");
			}
	
			double prob = getNgramProbability(ngram, from, to);
			if(prob <= 0) {
				return -100;
			}
			return Math.log(prob);

		}
		
		@Override
		public long getCount(int[] ngram) {
			try {
				if(ngram.length == 1) {
					int word = ngram[0];
					if (word < 0 || word >= model.getWordCount()) return 0;
					return model.getUnigramCount(word);
				}
				else if(ngram.length == 2) {
					long word = NgramUtils.getConcatenateIndex(ngram[0], ngram[1]);
					return model.getBigramCount(word);
				}
				else if(ngram.length == 3) {
					long prefix = NgramUtils.getConcatenateIndex(ngram[0], ngram[1]);
					long word = NgramUtils.getConcatenateIndex(prefix, ngram[2]);
					return model.getTrigramCount(word);
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
