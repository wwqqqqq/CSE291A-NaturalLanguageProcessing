package edu.berkeley.nlp.assignments.assign1.student;

import java.util.*;

import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.langmodel.LanguageModelFactory;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.util.CollectionUtils;
import edu.berkeley.nlp.util.StringIndexer;

public class FertilityCount {
    HashMap<Long,Integer> FertilityCounter;

    private long getConcatenateIndex(int ind1, int ind2) {
		long ind = ind1;
		ind = ind << 20 + ind2;
		return ind;
    }
    
    public FertilityCount(Iterable<List<String>> trainingData) {
        HashMap<Long, Set<Integer>> FertilitySet = new HashMap<Long, Set<Integer>>();
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
                long suffix = getConcatenateIndex(N1, index);
                
                if(FertilitySet.containsKey(suffix)) {
                    Set<Integer> set = FertilitySet.get(suffix);
                    set.add(N2);
                    FertilitySet.put(suffix, set);
                }
                else {
                    Set<Integer> set = new HashSet<Integer>();
                    set.add(N2);
                    FertilitySet.put(suffix, set);
                }

                suffix = index;
                if(FertilitySet.containsKey(suffix)) {
                    Set<Integer> set = FertilitySet.get(suffix);
                    set.add(N1);
                    FertilitySet.put(suffix, set);
                }
                else {
                    Set<Integer> set = new HashSet<Integer>();
                    set.add(N1);
                    FertilitySet.put(suffix, set);
                }

                N2 = N1;
                N1 = index;
            }

            FertilityCounter = new HashMap<Long, Integer>();
            for(Map.Entry<Long, Set<Integer>> entry : FertilitySet.entrySet()) {
                Set<Integer> s = entry.getValue();
                FertilityCounter.put(entry.getKey(), s.size());
            }
        }
    }

    public int getFertilityCount(int[] prev, int from, int to, int word) {
        if(from - to == 1) {
            long suffix = getConcatenateIndex(prev[from], word);
            if(FertilityCounter.containsKey(suffix)) {
                return FertilityCounter.get(suffix);
            }
        }
        else if(from - to == 0) {
            long suffix = word;
            if(FertilityCounter.containsKey(suffix)) {
                return FertilityCounter.get(suffix);
            }
        }
        return 0;
    }
}