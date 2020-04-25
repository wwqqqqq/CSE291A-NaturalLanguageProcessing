package edu.berkeley.nlp.assignments.assign1.student;

import java.util.*;

public class NgramHashMap {
	HashMap<Long, HashMap<Integer, Integer>> map = new HashMap<Long, HashMap<Integer, Integer>>();
	public NgramHashMap(int n) {}
	
	public long getConcatenateIndex(long ind1, int ind2) {
		return ind1<<20 + ind2;
	}
	
	public long getConcatenateIndex(int ind1, int ind2) {
		long ind = ind1;
		ind = ind << 20 + ind2;
		return ind;
	}
	
	
	public int addPrefixWord(long prefix, int wordIndex) {
		int val = 1;
		if(map.containsKey(prefix)) {
			HashMap<Integer, Integer> innerMap = map.get(prefix);
			if(innerMap.containsKey(wordIndex)) {
				val = innerMap.get(wordIndex) + 1;
			}
			innerMap.put(wordIndex, val);
			map.put(prefix, innerMap);
		}
		else {
			HashMap<Integer, Integer> innerMap = new HashMap<Integer, Integer>();
			innerMap.put(wordIndex, 1);
			map.put(prefix, innerMap);
		}
		return val;
	}
	
	public int getPrefixWordCount(long prefix, int wordIndex) {
		if(map.containsKey(prefix)) {
			HashMap<Integer, Integer> innerMap = map.get(prefix);
			if(innerMap.containsKey(wordIndex)) {
				return (innerMap.get(wordIndex));
			}
		}
		return 0;
    }
    
    public int getWordNumber(long prefix) {
        if(map.containsKey(prefix)) {
			HashMap<Integer, Integer> innerMap = map.get(prefix);
			return innerMap.size();
		}
		return 0;
    }
	
}