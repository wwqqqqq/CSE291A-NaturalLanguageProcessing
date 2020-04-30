package edu.berkeley.nlp.assignments.assign1.student;

import java.util.*;

import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.langmodel.LanguageModelFactory;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.util.CollectionUtils;
import edu.berkeley.nlp.util.StringIndexer;

public class NgramModel {
    private NgramHashMap trigram_map;
    private ContextMap bigram_map;
    private UnigramMap unigram_map;
    private int wordCount;
    private int twoCombinationCount;
    private int maxSentence;
    public NgramModel(Iterable<List<String>> trainingData, int limit) {
        maxSentence = limit;
        wordCount = 0;
        twoCombinationCount = 0;
        unigram_map = new UnigramMap();
        bigram_map = new ContextMap(2);
        trigram_map = new NgramHashMap(3);

        BuildTrigramModel(trainingData);
        BuildBigramModel(trainingData);
        BuildUnigramModel(trainingData);
    }

    private void CalculateCombination(Iterable<List<String>> trainingData) {
        System.out.println("Calculate Combination . . .");
        NgramHashSet combSet = new NgramHashSet(2);
        int sent = 0;
        int START = EnglishWordIndexer.getIndexer().addAndGetIndex(NgramLanguageModel.START);
        for (List<String> sentence : trainingData) {
            sent++;
            if(maxSentence > 0 && sent > maxSentence) {
                break;
            }
            if (sent % 1000000 == 0) System.out.println("On sentence " + sent);
            List<String> stoppedSentence = new ArrayList<String>(sentence);
            stoppedSentence.add(NgramLanguageModel.STOP);
            int N1 = START;
            for (String word : stoppedSentence) {
                int index = EnglishWordIndexer.getIndexer().addAndGetIndex(word);

                long key = NgramUtils.getConcatenateIndex(N1, index);

                if(!combSet.contains(key)) {
                    twoCombinationCount++;
                    combSet.add(key);
                }

                N1 = index;
            }
        }
        System.out.printf("Calculate Combination Done: Combination Count = %d\n", twoCombinationCount);
    }

    private void BuildUnigramModel(Iterable<List<String>> trainingData) {
        System.out.println("Build Unigram Model . . . ");
        int sent = 0;
        int START = EnglishWordIndexer.getIndexer().addAndGetIndex(NgramLanguageModel.START);
        NgramHashSet SuffixSet = new NgramHashSet(2);
        NgramHashSet MiddleSet = new NgramHashSet(3);
        // NgramHashSet PrefixSet = new NgramHashSet(2);
        int sent_count = 0;
        for (List<String> sentence : trainingData) {
            sent++;
            if(sent > sent_count) {
                sent_count = sent;
            }
            if(maxSentence > 0 && sent > maxSentence) {
                break;
            }
            if (sent % 1000000 == 0) System.out.println("On sentence " + sent);
            List<String> stoppedSentence = new ArrayList<String>(sentence);
            int N2 = START;
            int N1 = START;
            stoppedSentence.add(NgramLanguageModel.STOP);
            stoppedSentence.add(0, NgramLanguageModel.START);
            for (String word : stoppedSentence) {
                int index = EnglishWordIndexer.getIndexer().addAndGetIndex(word);
                if(index > wordCount) {
                    wordCount = index;
                }
                unigram_map.addKey(index);
                unigram_map.addValue(index);
                // Suffix Count and Prefix Count and Combination Count: c'(x)
                long key = NgramUtils.getConcatenateIndex(index, N1);
                if(!SuffixSet.contains(key)) {
                    SuffixSet.add(key);
                    unigram_map.addPrefixCount(N1);
                    unigram_map.addSuffixCount(index);
                    twoCombinationCount++;
                }
                // Middle Count
                long context = NgramUtils.getConcatenateIndex(N2, index);
                long middle = N1;
                key = NgramUtils.getConcatenateIndex(middle, context);
                if(!MiddleSet.contains(key)) {
                    unigram_map.addMiddleCount(N1);
                    MiddleSet.add(key);
                }
                // // Prefix Count
                // key = NgramUtils.getConcatenateIndex(N1, index);
                // if(!PrefixSet.contains(key)) {
                //     unigram_map.addPrefixCount(N1);
                //     PrefixSet.add(key);
                // }

                N2 = N1;
                N1 = index;
            }
        }
        System.out.printf("Build Unigram Model Done: wordCount = %d, sentCount = %d\n", wordCount, sent_count);
        unigram_map.print(10);
        unigram_map.print_last(10);
    }

    private void BuildBigramModel(Iterable<List<String>> trainingData) {
        System.out.println("Build Bigram Model . . .");
        int sent = 0;
        int START = EnglishWordIndexer.getIndexer().addAndGetIndex(NgramLanguageModel.START);
        NgramHashSet SuffixSet = new NgramHashSet(3);
        NgramHashSet PrefixSet = new NgramHashSet(3);
        NgramHashSet combSet = new NgramHashSet(2);
        for (List<String> sentence : trainingData) {
            sent++;
            if(maxSentence > 0 && sent > maxSentence) {
                break;
            }
            if (sent % 1000000 == 0) System.out.println("On sentence " + sent);
            List<String> stoppedSentence = new ArrayList<String>(sentence);
            stoppedSentence.add(NgramLanguageModel.STOP);
            int N2 = START;
            int N1 = START;
            long prefix = NgramUtils.getConcatenateIndex(N2, N1);
            int prefix_index = bigram_map.addKey(prefix);
            for (String word : stoppedSentence) {
                int index = EnglishWordIndexer.getIndexer().addAndGetIndex(word);
                long biword = NgramUtils.getConcatenateIndex(N1, index);
                long key = biword;
                int map_index = bigram_map.addKey(key);

                // Build bigram count
                bigram_map.addIndexValue(map_index);

                // Build the SuffixCount (c'(x))
                key = NgramUtils.getConcatenateIndex(biword, N2);
                
                if(!SuffixSet.contains(key)) {
                    bigram_map.addIndexSuffixCount(map_index);
                    SuffixSet.add(key);
                }

                // Build the PrefixCount (to compute discount)
                key = NgramUtils.getConcatenateIndex(prefix, index);
                if(!PrefixSet.contains(key)) {
                    bigram_map.addIndexPrefixCount(prefix_index);
                    PrefixSet.add(key);
                }

                prefix = biword;
                prefix_index = map_index;

                N2 = N1;
                N1 = index;
            }
        }
        System.out.println("Build Bigram Model Done");
        bigram_map.print(10);
        bigram_map.print_last(10);
    }

    private void BuildTrigramModel(Iterable<List<String>> trainingData) {
        System.out.println("Build Trigram Model . . .");
        int sent = 0;
        int START = EnglishWordIndexer.getIndexer().addAndGetIndex(NgramLanguageModel.START);
        for (List<String> sentence : trainingData) {
            sent++;
            if(maxSentence > 0 && sent > maxSentence) {
                break;
            }
            if (sent % 1000000 == 0) System.out.println("On sentence " + sent);
            List<String> stoppedSentence = new ArrayList<String>(sentence);
            stoppedSentence.add(NgramLanguageModel.STOP);
            int N2 = START;
            int N1 = START;
            for (String word : stoppedSentence) {
                int index = EnglishWordIndexer.getIndexer().addAndGetIndex(word);
                long prefix = NgramUtils.getConcatenateIndex(N2, N1);
                long key = NgramUtils.getConcatenateIndex(prefix, index);
                trigram_map.addOne(key);
                N2 = N1;
                N1 = index;
            }
        }
        System.out.println("Build Trigram Model Done");
        trigram_map.print(10);
        trigram_map.print_last(10);
    }

    public int getWordCount() {
        return wordCount;
    }

    public int getUnigramCount(int wordIndex) {
        return unigram_map.getValue(wordIndex);
    }

    public int getBigramCount(long word) {
        return bigram_map.getKeyValue(word);
    }

    public int getBigramIndexCount(int index) {
        return bigram_map.getIndexValue(index);
    }

    public int getTrigramCount(long word) {
        return trigram_map.getKeyValue(word);
    }

    public int getCombinationCount() {
        return twoCombinationCount;
    }

    public int getSuffixFertilityCount(int[] prev, int from, int to) {
        // c'(x) = |{u: c(u,x)>0}|
        if(to - from == 2) {
            long suffix = NgramUtils.getConcatenateIndex(prev[from], prev[from+1]);
            return bigram_map.getKeySuffixCount(suffix);
        }
        else if(to - from == 1) {
            return unigram_map.getSuffixCount(prev[from]);
        }
        return 0;
    }

    public int getMiddleFertilityCount(int[] prev, int from, int to) {
        if(to - from == 1) {
            return unigram_map.getMiddleCount(prev[from]);
        }
        return 0;
    }

    public int getMiddleFertilityCount(int wordIndex) {
        return unigram_map.getMiddleCount(wordIndex);
    }

    public int getPrefixFertilityCount(int[] prev, int from, int to) {
        if(to - from == 2) {
            long prefix = NgramUtils.getConcatenateIndex(prev[from], prev[from+1]);
            return bigram_map.getKeyPrefixCount(prefix);
        }
        else if(to - from == 1) {
            return unigram_map.getPrefixCount(prev[from]);
        }
        return 0;
    }

    public int getPrefixFertilityCount(int n, int index) {
        if(n == 1) {
            return unigram_map.getPrefixCount(index);
        }
        else if(n == 2) {
            return bigram_map.getIndexPrefixCount(index);
        }
        return 0;
    }

    public int getWordIndex(int n, long word) {
        if(n == 2) {
            return bigram_map.indexOf(word);
        }
        else if(n == 3) {
            return trigram_map.indexOf(word);
        }
        return (int)word;
    }
}