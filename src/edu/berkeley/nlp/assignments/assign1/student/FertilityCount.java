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
    int count = 0;

    public FertilityCount(Iterable<List<String>> trainingData) {
        BuildSuffixCounter(trainingData);
        SuffixCounter.print(10);
        SuffixCounter.print_last(10);
        BuildMiddleCounter(trainingData);
        MiddleCounter.print(10);
        MiddleCounter.print_last(10);
        BuildPrefixCounter(trainingData);
        PrefixCounter.print(10);
        PrefixCounter.print_last(10);
    }

    private void BuildSuffixCounter(Iterable<List<String>> trainingData) {
        System.out.println("BuildSuffixCounter");        
        NgramHashSet SuffixSet3 = new NgramHashSet(3);
        NgramHashSet SuffixSet2 = new NgramHashSet(2);
        SuffixCounter = new NgramHashMap(2);
        int sent = 0;
        int START = EnglishWordIndexer.getIndexer().addAndGetIndex(NgramLanguageModel.START);
        for (List<String> sentence : trainingData) {
            sent++;
            if (sent % 1000000 == 0) System.out.println("On sentence " + sent);
            List<String> stoppedSentence = new ArrayList<String>(sentence);
            stoppedSentence.add(NgramLanguageModel.STOP);
            int N2 = START;
            int N1 = N2;
            for (String word : stoppedSentence) {
                int index = EnglishWordIndexer.getIndexer().addAndGetIndex(word);
                long suffix = NgramUtils.getConcatenateIndex(N1, index);
                long key = NgramUtils.getConcatenateIndex(suffix, N2);
                
                if(N1 != START && !SuffixSet3.contains(key)) {
                    SuffixCounter.addOne(suffix);
                    SuffixSet3.add(key);
                }

                suffix = index;
                key = NgramUtils.getConcatenateIndex(suffix, N1);
                if(!SuffixSet2.contains(key)) {
                    SuffixCounter.addOne(suffix);
                    SuffixSet2.add(key);
                }

                N2 = N1;
                N1 = index;
            }
        }
    }

    private void BuildPrefixCounter(Iterable<List<String>> trainingData) {
        System.out.println("BuildPrefixCounter");   
        NgramHashSet PrefixSet3 = new NgramHashSet(3);
        NgramHashSet PrefixSet2 = new NgramHashSet(2);
        PrefixCounter = new NgramHashMap(2);
        int sent = 0;
        int START = EnglishWordIndexer.getIndexer().addAndGetIndex(NgramLanguageModel.START);
        for (List<String> sentence : trainingData) {
            sent++;
            if (sent % 1000000 == 0) System.out.println("On sentence " + sent);
            List<String> stoppedSentence = new ArrayList<String>(sentence);
            stoppedSentence.add(NgramLanguageModel.STOP);
            int N2 = START;
            int N1 = N2;
            for (String word : stoppedSentence) {
                int index = EnglishWordIndexer.getIndexer().addAndGetIndex(word);

                long prefix = NgramUtils.getConcatenateIndex(N2, N1);
                long key = NgramUtils.getConcatenateIndex(prefix, index);

                if(N1 != START && N2 != START && !PrefixSet3.contains(key)) {
                    PrefixCounter.addOne(prefix);
                    PrefixSet3.add(key);
                }

                prefix = N1;
                key = NgramUtils.getConcatenateIndex(prefix, index);
                if(N1 != START && !PrefixSet2.contains(key)) {
                    PrefixCounter.addOne(prefix);
                    PrefixSet2.add(key);
                }

                N2 = N1;
                N1 = index;
            }

        }
    }
    
    private void BuildMiddleCounter(Iterable<List<String>> trainingData) {
        System.out.println("BuildMiddleCounter");   
        NgramHashSet MiddleSet = new NgramHashSet(3);
        NgramHashSet Bigram = new NgramHashSet(2);
        MiddleCounter = new NgramHashMap(1);
        int sent = 0;
        int START = EnglishWordIndexer.getIndexer().addAndGetIndex(NgramLanguageModel.START);
        for (List<String> sentence : trainingData) {
            sent++;
            if (sent % 1000000 == 0) System.out.println("On sentence " + sent);
            List<String> stoppedSentence = new ArrayList<String>(sentence);
            stoppedSentence.add(NgramLanguageModel.STOP);
            int N2 = START;
            int N1 = N2;
            for (String word : stoppedSentence) {
                int index = EnglishWordIndexer.getIndexer().addAndGetIndex(word);
                long context = NgramUtils.getConcatenateIndex(N2, index);
                long middle = N1;
                long key = NgramUtils.getConcatenateIndex(middle, context);

                if(N1 != START && !MiddleSet.contains(key)) {
                    MiddleCounter.addOne(middle);
                    MiddleSet.add(key);
                }

                key = NgramUtils.getConcatenateIndex(N1, index);

                if(N1 != START && !Bigram.contains(key)) {
                    count++;
                    Bigram.add(key);
                }

                N2 = N1;
                N1 = index;
            }

        }
    }

    public int getFertilityCountforSuffix(int[] prev, int from, int to, int word) {
        // c'(x) = |{u: c(u,x)>0}|
        if(to - from == 1) {
            long suffix = NgramUtils.getConcatenateIndex(prev[from], word);
            return SuffixCounter.get(suffix);
        }
        else if(to - from == 0) {
            long suffix = word;
            return SuffixCounter.get(suffix);
        }
        return 0;
    }

    public int getFertilityCountforMiddle(int[] prev, int from, int to) {
        if(to - from == 2) {
            long middle = NgramUtils.getConcatenateIndex(prev[from], prev[from+1]);
            return MiddleCounter.get(middle);
        }
        else if(to - from == 1) {
            long middle = prev[from];
            return MiddleCounter.get(middle);
        }
        return 0;
    }

    public int getFertilityCountforPrefix(int[] prev, int from, int to) {
        if(to - from == 2) {
            long prefix = NgramUtils.getConcatenateIndex(prev[from], prev[from+1]);
            return PrefixCounter.get(prefix);
        }
        else if(to - from == 1) {
            long prefix = prev[from];
            return PrefixCounter.get(prefix);
        }
        return 0;
    }

    public int getBigramCount() {
        return count;
    }

}