package edu.berkeley.nlp.assignments.assign1.student;

import java.util.*;

import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.langmodel.LanguageModelFactory;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.util.CollectionUtils;
import edu.berkeley.nlp.util.StringIndexer;

public class NgramModel {
    private NgramHashMap map;
    public NgramModel(int n, Iterable<List<String>> trainingData) {
        map = new NgramHashMap(n);
        if(n == 2) {
            BuildBigramModel(trainingData);
        }
        else if(n == 3) {
            BuildTrigramModel(trainingData);
        }
    }

    private void BuildBigramModel(Iterable<List<String>> trainingData) {
        int sent = 0;
        for (List<String> sentence : trainingData) {
            sent++;
            if (sent % 1000000 == 0) System.out.println("On sentence " + sent);
            List<String> stoppedSentence = new ArrayList<String>(sentence);
            stoppedSentence.add(NgramLanguageModel.STOP);
            int prev = EnglishWordIndexer.getIndexer().addAndGetIndex(NgramLanguageModel.START);
            for (String word : stoppedSentence) {
                int index = EnglishWordIndexer.getIndexer().addAndGetIndex(word);
                addPrefixWord((long)prev, index);
                prev = index;
            }
        }
    }

    private void BuildTrigramModel(Iterable<List<String>> trainingData) {
        int sent = 0;
        for (List<String> sentence : trainingData) {
            sent++;
            if (sent % 1000000 == 0) System.out.println("On sentence " + sent);
            List<String> stoppedSentence = new ArrayList<String>(sentence);
            stoppedSentence.add(NgramLanguageModel.STOP);
            int N2 = EnglishWordIndexer.getIndexer().addAndGetIndex(NgramLanguageModel.START);
            int N1 = EnglishWordIndexer.getIndexer().addAndGetIndex(NgramLanguageModel.START);
            for (String word : stoppedSentence) {
                int index = EnglishWordIndexer.getIndexer().addAndGetIndex(word);
                long prefix = NgramUtils.getConcatenateIndex(N2, N1);
                addPrefixWord(prefix, index);
                N2 = N1;
                N1 = index;
            }
        }
    }

    private int addPrefixWord(long prefix, int wordIndex) {
		long key = NgramUtils.getConcatenateIndex(prefix, wordIndex);
		int index = map.addOne(key);
		return map.getIndexValue(index);
	}
	
	public int getPrefixWordCount(long prefix, int wordIndex) {
		long key = NgramUtils.getConcatenateIndex(prefix, wordIndex);
		return map.getKeyValue(key);
    }
}