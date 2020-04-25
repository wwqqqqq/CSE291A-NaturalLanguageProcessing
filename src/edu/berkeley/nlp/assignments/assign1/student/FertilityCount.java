package edu.berkeley.nlp.assignments.assign1.student;

import java.util.*;

import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.langmodel.LanguageModelFactory;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.util.CollectionUtils;
import edu.berkeley.nlp.util.StringIndexer;

public class FertilityCount {
    // HashMap<Long,Integer> SuffixCounter;
    // HashMap<Long,Integer> PrefixCounter;
    // HashMap<Long,Integer> MiddleCounter;
    NgramHashMap SuffixCounter;
    NgramHashMap PrefixCounter;
    NgramHashMap MiddleCounter;

    public FertilityCount(Iterable<List<String>> trainingData) {
        // BuildMiddleCounter(trainingData);
        // BuildPrefixCounter(trainingData);
        // BuildSuffixCounter(trainingData);
    }

    private void BuildSuffixCounter(Iterable<List<String>> trainingData) {
        HashMap<Long, Set<Long>> SuffixSet = new HashMap<Long, Set<Long>>();
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
                long suffix = NgramUtils.getConcatenateIndex(N1, index);
                
                if(SuffixSet.containsKey(suffix)) {
                    Set<Long> set = SuffixSet.get(suffix);
                    set.add((long)N2);
                    SuffixSet.put(suffix, set);
                }
                else {
                    Set<Long> set = new HashSet<Long>();
                    set.add((long)N2);
                    SuffixSet.put(suffix, set);
                }

                suffix = index;
                if(SuffixSet.containsKey(suffix)) {
                    Set<Long> set = SuffixSet.get(suffix);
                    set.add((long)N1);
                    SuffixSet.put(suffix, set);
                }
                else {
                    Set<Long> set = new HashSet<Long>();
                    set.add((long)N1);
                    SuffixSet.put(suffix, set);
                }

                N2 = N1;
                N1 = index;
            }

            SuffixCounter = new NgramHashMap(2);

            for(Map.Entry<Long, Set<Long>> entry : SuffixSet.entrySet()) {
                Set<Long> s = entry.getValue();
                SuffixCounter.put(entry.getKey(), s.size());
            }
        }
    }

    private void BuildPrefixCounter(Iterable<List<String>> trainingData) {
        HashMap<Long, Set<Long>> PrefixSet = new HashMap<Long, Set<Long>>();
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

                long prefix = NgramUtils.getConcatenateIndex(N2, N1);
                if(PrefixSet.containsKey(prefix)) {
                    Set<Long> set = PrefixSet.get(prefix);
                    set.add((long)index);
                    PrefixSet.put(prefix, set);
                }
                else {
                    Set<Long> set = new HashSet<Long>();
                    set.add((long)index);
                    PrefixSet.put(prefix, set);
                }

                prefix = N1;
                if(PrefixSet.containsKey(prefix)) {
                    Set<Long> set = PrefixSet.get(prefix);
                    set.add((long)index);
                    PrefixSet.put(prefix, set);
                }
                else {
                    Set<Long> set = new HashSet<Long>();
                    set.add((long)index);
                    PrefixSet.put(prefix, set);
                }

                N2 = N1;
                N1 = index;
            }

            PrefixCounter = new NgramHashMap(2);

            for(Map.Entry<Long, Set<Long>> entry : PrefixSet.entrySet()) {
                Set<Long> s = entry.getValue();
                PrefixCounter.put(entry.getKey(), s.size());
            }

        }
    }
    
    private void BuildMiddleCounter(Iterable<List<String>> trainingData) {
        HashMap<Long, Set<Long>> MiddleSet = new HashMap<Long, Set<Long>>();
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
                long context = NgramUtils.getConcatenateIndex(N2, index);
                
                if(MiddleSet.containsKey(middle)) {
                    Set<Long> set = MiddleSet.get(middle);
                    set.add(context);
                    MiddleSet.put(middle, set);
                }
                else {
                    Set<Long> set = new HashSet<Long>();
                    set.add(context);
                    MiddleSet.put(middle, set);
                }

                N2 = N1;
                N1 = index;
            }

            MiddleCounter = new NgramHashMap(1);

            for(Map.Entry<Long, Set<Long>> entry : MiddleSet.entrySet()) {
                Set<Long> s = entry.getValue();
                MiddleCounter.put(entry.getKey(), s.size());
            }

        }
    }

    public int getFertilityCountforSuffix(int[] prev, int from, int to, int word) {
        if(from - to == 1) {
            long suffix = NgramUtils.getConcatenateIndex(prev[from], word);
            if(SuffixCounter.containsKey(suffix)) {
                return SuffixCounter.get(suffix);
            }
        }
        else if(from - to == 0) {
            long suffix = word;
            if(SuffixCounter.containsKey(suffix)) {
                return SuffixCounter.get(suffix);
            }
        }
        return 0;
    }

    public int getFertilityCountforMiddle(int[] prev, int from, int to) {
        if(from - to == 2) {
            long middle = NgramUtils.getConcatenateIndex(prev[from], prev[from+1]);
            if(MiddleCounter.containsKey(middle)) {
                return MiddleCounter.get(middle);
            }
        }
        else if(from - to == 1) {
            long middle = prev[from];
            if(MiddleCounter.containsKey(middle)) {
                return MiddleCounter.get(middle);
            }
        }
        return 0;
    }

    public int getFertilityCountforPrefix(int[] prev, int from, int to) {
        if(from - to == 2) {
            long prefix = NgramUtils.getConcatenateIndex(prev[from], prev[from+1]);
            if(PrefixCounter.containsKey(prefix)) {
                return PrefixCounter.get(prefix);
            }
        }
        else if(from - to == 1) {
            long prefix = prev[from];
            if(PrefixCounter.containsKey(prefix)) {
                return PrefixCounter.get(prefix);
            }
        }
        return 0;
    }

}