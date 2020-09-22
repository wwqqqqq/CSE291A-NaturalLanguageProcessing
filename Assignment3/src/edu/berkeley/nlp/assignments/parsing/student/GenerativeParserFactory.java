package edu.berkeley.nlp.assignments.parsing.student;

import java.util.*;


import edu.berkeley.nlp.assignments.parsing.*;
import edu.berkeley.nlp.ling.Tree;


public class GenerativeParserFactory implements ParserFactory {

	SimpleLexicon lexicon;
	Grammar grammar;
	UnaryClosure uc;

	List<BinaryRule> binaryRules;
	List<UnaryRule> unaryRules;
	TreeBinarizer binarizer;
	
	public Parser getParser(List<Tree<String>> trainTrees) {
		binarizer = new TreeBinarizer(2,2); // h, v (Integer.MAX_VALUE represents inf)
		System.out.print("Annotating / binarizing training trees ... ");
		List<Tree<String>> annotatedTrainTrees = annotateTrees(trainTrees);
		System.out.println("done.");
		System.out.print("Building grammar ... ");
		grammar = Grammar.generativeGrammarFromTrees(annotatedTrainTrees);
		System.out.println("done. (" + grammar.getLabelIndexer().size() + " states)");
		uc = new UnaryClosure(grammar.getLabelIndexer(), grammar.getUnaryRules());
		// unaryRules = grammar.getUnaryRules();
		unaryRules = new ArrayList<UnaryRule>(uc.getPathMap().keySet());
		binaryRules = grammar.getBinaryRules();

		// System.out.println(trainTrees.get(4));
		// System.out.println(annotatedTrainTrees.get(4));


		// printUnaryRuleScore();
		// printBinaryRuleScore();

		System.out.print("Discarding grammar and setting up a baseline parser ... ");
		// For FeaturizedLexiconDiscriminativeParserFactory, you should construct an instance of your own 
		// of LexiconFeaturizer here.
		lexicon = new SimpleLexicon(annotatedTrainTrees);
		// printAllTags();
		System.out.println("done.");

		return new Parser() {
			double[][][] topScore;
			double[][][] bottomScore;
			int tagSize;
			int length;
			private void constructScoreTables(List<String> sentence) {
				for(int diff = 0; diff < length; diff++) {
					for(int i = 0; i < length - diff; i++) {
						int j = i + diff;
						// top (binary)
						for(BinaryRule bRule : binaryRules) {
							int X = bRule.getParent();
							int Y = bRule.getLeftChild();
							int Z = bRule.getRightChild();
							double ruleScore = bRule.getScore();
							double maxScore = topScore[i][j][X];
							for(int k = i; k < j; k++) {
								// if(bottomScore[i][k][Y] != Double.NEGATIVE_INFINITY || bottomScore[k+1][j][Z] != Double.NEGATIVE_INFINITY) {
								// 	System.out.print(grammar.getLabelIndexer().get(Y) + ",");
								// 	System.out.print(grammar.getLabelIndexer().get(Z) + "\t");
								// 	System.out.print(bottomScore[i][k][Y]);
								// 	System.out.print("\t");
								// 	System.out.print(bottomScore[k+1][j][Z]);
								// 	System.out.printf("\t i=%d,k=%d,j=%d\n",i,k,j);
								// }
								maxScore = Math.max(maxScore, ruleScore + bottomScore[i][k][Y] + bottomScore[k+1][j][Z]);
							}
							topScore[i][j][X] = maxScore;
						}
						// bottom (unary)
						if(diff == 0) {
							for(String tag : lexicon.getAllTags()) {
								int tagIndex = grammar.getLabelIndexer().indexOf(tag);
								if(tagIndex >= 0 && tagIndex < tagSize) {
									bottomScore[i][j][tagIndex] = lexicon.scoreTagging(sentence.get(i), tag);
									// if(bottomScore[i][j][tagIndex] != Double.NEGATIVE_INFINITY) {
									// 	System.out.print(tag + " " + sentence.get(i) + " ");
									// 	System.out.println(bottomScore[i][j][tagIndex]);
									// }
								}
							}
							for(UnaryRule uRule : unaryRules) {
								int X = uRule.getParent();
								int Y = uRule.getChild();
								double ruleScore = uRule.getScore();
								bottomScore[i][j][X] = Math.max(bottomScore[i][j][X], ruleScore + bottomScore[i][j][Y]);
							}
						}
						else {
							for(UnaryRule uRule : unaryRules) {
								int X = uRule.getParent();
								int Y = uRule.getChild();
								double ruleScore = uRule.getScore();
								bottomScore[i][j][X] = Math.max(bottomScore[i][j][X], ruleScore + topScore[i][j][Y]);
							}
						}
					}
				}
			}

			private void printScoreTagging(List<String> sentence) {
				for(String tag : lexicon.getAllTags()) {
					for(String word : sentence) {
						System.out.print(tag + ", " + word + ": ");
						System.out.println(lexicon.scoreTagging(word, tag));
					}
				}
			}

			private void printTopTable() {
				for(int X = 0; X < tagSize; X++) {
					String label = grammar.getLabelIndexer().get(X);
					System.out.println(label);
					for(int i = 0; i < length; i++) {
						for(int j = i; j < length; j++) {
							System.out.print(topScore[i][j][X]);
							System.out.print("\t");
						}
						System.out.print("\n");
					}
				}
			}

			private void printBottomTable() {
				Set<Integer> tags = new HashSet<Integer>();
				for(UnaryRule uRule : unaryRules) {
					int X = uRule.getParent();
					if(tags.contains(X)) {
						continue;
					}
					tags.add(X);
					String label = grammar.getLabelIndexer().get(X);
					System.out.println(label);
					for(int i = 0; i < length; i++) {
						for(int j = i; j < length; j++) {
							System.out.print(bottomScore[i][j][X]);
							System.out.print("\t");
						}
						System.out.print("\n");
					}
				}
			}


			private Tree<String> constructTree(List<String> sentence, int st, int ed, Boolean binary, int parentTag) {
				String label = grammar.getLabelIndexer().get(parentTag);
				if(st == ed) {
					// leaf
					// return new Tree<String>(label, Collections.singletonList(new Tree<String>(sentence.get(st))));
					return unwrapTransitiveRules(parentTag, sentence.get(st), st);
				}
				if(binary) {
					double maxScore = Double.NEGATIVE_INFINITY;
					int k = st;
					int leftTag = 0;
					int rightTag = 0;
					List<BinaryRule> brules = grammar.getBinaryRulesByParent(parentTag);
					for(int mid = st; mid < ed; mid++) {
						for(BinaryRule br : brules) {
							int Y = br.getLeftChild();
							int Z = br.getRightChild();
							// parentTag -> Y Z
							// st - mid - Y
							// mid+1 - ed - Z
							double score = br.getScore() + bottomScore[st][mid][Y] + bottomScore[mid+1][ed][Z];
							if(score > maxScore) {
								k = mid;
								leftTag = Y;
								rightTag = Z;
								maxScore = score;
							}
						}
					}
					if(maxScore == Double.NEGATIVE_INFINITY) {
						label = "?";
					}
					List<Tree<String>> children = new ArrayList<Tree<String>>();
					children.add(constructTree(sentence, st, k, false, leftTag));
					children.add(constructTree(sentence, k+1, ed, false, rightTag));
					return new Tree<String>(label, children);
				}
				else {
					double maxScore = Double.NEGATIVE_INFINITY;
					int childTag = 0;
					List<UnaryRule> urules = uc.getClosedUnaryRulesByParent(parentTag);
					for(UnaryRule ur : urules) {
						int Y = ur.getChild();
						double score = ur.getScore() + topScore[st][ed][Y];
						if(score > maxScore) {
							childTag = Y;
							maxScore = score;
						}
					}
					if(maxScore == Double.NEGATIVE_INFINITY) {
						label = "?";
					}
					return unwrapTransitiveRules(parentTag, childTag, sentence, st, ed);
					// return new Tree<String>(label, Collections.singletonList(constructTree(sentence, st, ed, true, childTag)));
					
				}
			}

			private Boolean accept(int tag) {
				String s = grammar.getLabelIndexer().get(tag);
				return !s.startsWith("@");
			}
			

			private Tree<String> unwrapTransitiveRules(int parentTag, String word, int ind) {
				double maxScore = Double.NEGATIVE_INFINITY;
				// if(accept(parentTag)) {
				// 	return new Tree<String>(grammar.getLabelIndexer().get(parentTag), Collections.singletonList(new Tree<String>(word)));
				// }
				List<UnaryRule> rules = uc.getClosedUnaryRulesByParent(parentTag);
				int Y = parentTag;
				for(UnaryRule ur : rules) {
					String tag = grammar.getLabelIndexer().get(ur.getChild());
					if(accept(ur.getChild())){
						// System.out.print(tag + " ");
						// System.out.println(lexicon.scoreTagging(sentence.get(ind), tag));
						double score = ur.getScore() + lexicon.scoreTagging(word, tag); //bottomScore[ind][ind][ur.getChild()];
						if(score > maxScore) {
							Y = ur.getChild();
							maxScore = score;
						}
					}
				}
				
				UnaryRule unaryrule = new UnaryRule(parentTag, Y);
				List<Integer> path = uc.getPath(unaryrule);
				// System.out.println(grammar.getLabelIndexer().get(Y));
				Tree<String> tree = new Tree<String>(grammar.getLabelIndexer().get(Y), Collections.singletonList(new Tree<String>(word)));
				for(int i = path.size() - 2; i >= 0; i--) {
					String label = grammar.getLabelIndexer().get(path.get(i));
					tree = new Tree<String>(label, Collections.singletonList(tree));
				}
				return tree;
			}

			private Tree<String> unwrapTransitiveRules(int parentTag, int childTag, List<String> sentence, int st, int ed) {
				if(parentTag == childTag) {
					// identity rule
					return constructTree(sentence, st, ed, true, childTag);
				}
				UnaryRule ur = new UnaryRule(parentTag, childTag);
				List<Integer> path = uc.getPath(ur);
				Tree<String> tree = constructTree(sentence, st, ed, true, childTag);
				for(int i = path.size() - 2; i >= 0; i--) {
					String label = grammar.getLabelIndexer().get(path.get(i));
					tree = new Tree<String>(label, Collections.singletonList(tree));
				}
				return tree;
			}

			private void initScoreTable(double[][][] table) {
				for(double[][] table2 : table) {
					for(double[] table1 : table2) {
						Arrays.fill(table1, Double.NEGATIVE_INFINITY);
					}
				}
			}

			public Tree<String> getBestParse(List<String> sentence) {
				tagSize = grammar.getLabelIndexer().size();
				length = sentence.size();
				topScore = new double[length][length][tagSize];
				bottomScore = new double[length][length][tagSize];
				// printScoreTagging(sentence);
				initScoreTable(topScore);
				initScoreTable(bottomScore);
				constructScoreTables(sentence);
				// printUnaryRuleScore(0);
				// printTopTable();
				// printBottomTable();
				Tree<String> parse = constructTree(sentence, 0, length - 1, false, grammar.getLabelIndexer().indexOf("ROOT")); // first rule is ROOT->? (unary)
				return binarizer.unAnnotateTree(parse);
				// return parse;
			}
		};
	}


	private List<Tree<String>> annotateTrees(List<Tree<String>> trees) {
		List<Tree<String>> annotatedTrees = new ArrayList<Tree<String>>();
		for (Tree<String> tree : trees) {
			annotatedTrees.add(binarizer.annotateTreeLosslessBinarization(tree));
		}
		return annotatedTrees;
	}

	private void printUnaryRuleScore(int tag) {
		List<UnaryRule> urules = uc.getClosedUnaryRulesByParent(tag);
		for(UnaryRule ur : urules) {
			System.out.print(ur);
			System.out.print(": ");
			System.out.println(ur.getScore());
		}
	}

	private void printUnaryRuleScore() {
		for(UnaryRule uRule : unaryRules) {
			int X = uRule.getParent();
			int Y = uRule.getChild();
			double ruleScore = uRule.getScore();
			if(grammar.getLabelIndexer().get(Y).length() < 5) {
				System.out.print(grammar.getLabelIndexer().get(X) + " -> ");
				System.out.print(grammar.getLabelIndexer().get(Y) + "\t");
				System.out.println(ruleScore);
			}
		}
	}

	private void printBinaryRuleScore() {
		for(BinaryRule bRule : binaryRules) {
			int X = bRule.getParent();
			int Y = bRule.getLeftChild();
			int Z = bRule.getRightChild();
			double ruleScore = bRule.getScore();
			if(grammar.getLabelIndexer().get(Y).length() < 5 && grammar.getLabelIndexer().get(Z).length() < 5) {
				System.out.print(grammar.getLabelIndexer().get(X) + " -> ");
				System.out.print(grammar.getLabelIndexer().get(Y) + ",");
				System.out.print(grammar.getLabelIndexer().get(Z) + "\t");
				System.out.println(ruleScore);
			}
		}
	}

	private void printAllTags() {
		Set<String> tags = lexicon.getAllTags();
		for(String tag : tags) {
			System.out.println(tag);
		}
	}

}
