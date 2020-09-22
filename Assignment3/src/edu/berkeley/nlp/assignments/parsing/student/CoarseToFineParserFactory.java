package edu.berkeley.nlp.assignments.parsing.student;

import java.util.List;
import java.util.Collections;

import edu.berkeley.nlp.assignments.parsing.Parser;
import edu.berkeley.nlp.assignments.parsing.ParserFactory;
import edu.berkeley.nlp.ling.Tree;


public class CoarseToFineParserFactory implements ParserFactory {

	float[][] score;

	public Parser getParser(List<Tree<String>> trainTrees) {
		int trainSize = trainTrees.size();

		score = new float[trainSize][trainSize];

		return new Parser() {
			public Tree<String> getBestParse(List<String> sentence) {
				return new Tree<String>("ROOT", Collections.singletonList(new Tree<String>("JUNK")));
			}
		};
	}

	private float tagScore(int X, String word) {
		return 0;
	}

	private float bestScore(int X, int i, int j, List<String> sentence) {
		float score;
		if(j == i + 1) {
			score = tagScore(X, sentence.get(i));
		}
		else {
			score = 0;
		}
		return score;
	}

}