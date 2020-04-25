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
        BuildMiddleCounter(trainingData);
        BuildPrefixCounter(trainingData);
        BuildSuffixCounter(trainingData);
    }

    private void BuildSuffixCounter(Iterable<List<String>> trainingData) {
        System.out.println("BuildSuffixCounter");        
        Set<Long> SuffixSet = new HashSet<Long>();
        SuffixCounter = new NgramHashMap(2);
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
                long key = NgramUtils.getConcatenateIndex(suffix, N2);

                if(!SuffixSet.contains(key)) {
                    SuffixCounter.addOne(suffix);
                    SuffixSet.add(key);
                }

                suffix = index;
                key = NgramUtils.getConcatenateIndex(suffix, N1);
                if(!SuffixSet.contains(key)) {
                    SuffixCounter.addOne(suffix);
                    SuffixSet.add(key);
                }

                N2 = N1;
                N1 = index;
            }
        }
    }

    private void BuildPrefixCounter(Iterable<List<String>> trainingData) {
        System.out.println("BuildPrefixCounter");   
        Set<Long> PrefixSet = new HashSet<Long>();
        PrefixCounter = new NgramHashMap(2);
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
                long key = NgramUtils.getConcatenateIndex(prefix, index);

                if(!PrefixSet.contains(key)) {
                    PrefixCounter.addOne(prefix);
                    PrefixSet.add(key);
                }

                prefix = N1;
                key = NgramUtils.getConcatenateIndex(prefix, index);
                if(!PrefixSet.contains(key)) {
                    PrefixCounter.addOne(prefix);
                    PrefixSet.add(key);
                }

                N2 = N1;
                N1 = index;
            }

        }
    }
    
    private void BuildMiddleCounter(Iterable<List<String>> trainingData) {
        System.out.println("BuildMiddleCounter");   
        Set<Long> MiddleSet = new HashSet<Long>();
        MiddleCounter = new NgramHashMap(1);
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
                long context = NgramUtils.getConcatenateIndex(N2, index);
                long middle = N1;
                long key = NgramUtils.getConcatenateIndex(middle, context);

                if(!MiddleSet.contains(key)) {
                    MiddleCounter.addOne(middle);
                    MiddleSet.add(key);
                }

                N2 = N1;
                N1 = index;
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