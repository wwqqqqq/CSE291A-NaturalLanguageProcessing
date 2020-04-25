package edu.berkeley.nlp.assignments.assign1.student;

import java.util.*;

import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.langmodel.LanguageModelFactory;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.util.CollectionUtils;
import edu.berkeley.nlp.util.StringIndexer;

public class ContextCount {
    HashMap<Long,Integer> FertilityCounter;

    private long getConcatenateIndex(int ind1, int ind2) {
		long ind = ind1;
		ind = ind << 20 + ind2;
		return ind;
    }
    
    public ContextCount(Iterable<List<String>> trainingData, double d) {
        HashMap<Long, Set<Long>> FertilitySet = new HashMap<Long, Set<Long>>();
        int sent = 0;
        for (List<String> sentence : trainingData) {
            sent++;
            if (sent % 1000000 == 0) System.out.println("On sentence " + sent);
            List<String> stoppedSentence = new ArrayList<String>(sentence);
            stoppedSentence.add(NgramLanguageModel.STOP);
            int N2 = EnglishWordIndexer.getIndexer().addAndGetIndex(NgramLanguageModel.START);
            int N1 = N2;
            for (String word : stoppedSentence) {
                int index = EnglishWordIndexer.getIndexer().addAndGetIndex(word);
                long middle = N1;
                long context = getConcatenateIndex(N2, index);
                
                if(FertilitySet.containsKey(middle)) {
                    Set<Long> set = FertilitySet.get(middle);
                    set.add(context);
                    FertilitySet.put(middle, set);
                }
                else {
                    Set<Long> set = new HashSet<Long>();
                    set.add(context);
                    FertilitySet.put(middle, set);
                }

                N2 = N1;
                N1 = index;
            }

            FertilityCounter = new HashMap<Long, Integer>();
            for(Map.Entry<Long, Set<Long>> entry : FertilitySet.entrySet()) {
                Set<Long> s = entry.getValue();
                FertilityCounter.put(entry.getKey(), s.size());
            }
        }
    }

    public int getFertilityCount(int[] prev, int from, int to) {
        if(from - to == 2) {
            long middle = getConcatenateIndex(prev[from], prev[from+1]);
            if(FertilityCounter.containsKey(middle)) {
                return FertilityCounter.get(middle);
            }
        }
        else if(from - to == 1) {
            long middle = prev[from];
            if(FertilityCounter.containsKey(middle)) {
                return FertilityCounter.get(middle);
            }
        }
        return 0;
    }

}