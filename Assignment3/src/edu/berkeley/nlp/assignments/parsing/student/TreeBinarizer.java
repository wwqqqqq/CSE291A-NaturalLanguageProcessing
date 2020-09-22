package edu.berkeley.nlp.assignments.parsing.student;

import java.util.*;


import edu.berkeley.nlp.assignments.parsing.*;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.ling.Trees;
import edu.berkeley.nlp.util.Filter;


public class TreeBinarizer {
    int h;
    int v;
    List<String> vq = new ArrayList<String>();
    List<String> hq = new ArrayList<String>();

    public TreeBinarizer(int H, int V) {
        h = H;
        v = V;
        // Integer.MAX_VALUE for inf
    }
    public Tree<String> annotateTreeLosslessBinarization(Tree<String> unAnnotatedTree) {
        return binarizeTree(unAnnotatedTree);
    }

    private String getVerizontalLabel() {
        String label = "@";
        for(int i = 0; i < vq.size(); i++) {
            label += vq.get(i);
            if(i != vq.size() - 1) {
                label += "^";
            }
            else {
                label += "->";
            }
        }
        return label;
    }

    private String getHorizontalTags() {
        String label = "";
        for(int i = 0; i < hq.size(); i++) {
            label += "_" + hq.get(i);
        }
        return label;
    }

    private String getAnnotation() {
        String annotation = "";
        for(int i = vq.size() - 2; i >= 0; i--) {
            annotation += "^" + vq.get(i);
        }
        return annotation;
    }

    private void addVTag(String label) {
        vq.add(label);
        if(vq.size() > v) {
            vq.remove(0);
        }
    }

    private void removeLastVTag() {
        vq.remove(vq.size() - 1);
    }

    private void addHTag(String label) {
        hq.add(label);
        if(hq.size() > h) {
            hq.remove(0);
        }
    }

    private void clearHTag() {
        hq.clear();
    }

    private Tree<String> binarizeTree(Tree<String> tree) {
        List<String> hq_copy = new ArrayList<String>(hq);
        List<String> vq_copy = new ArrayList<String>(vq);
        clearHTag();
        String label = tree.getLabel();
        if (tree.isLeaf()) return new Tree<String>(label);
        addVTag(label);
        label += getAnnotation();
		if (tree.getChildren().size() == 1) { 
            Tree<String> result = new Tree<String>(label, Collections.singletonList(binarizeTree(tree.getChildren().get(0))));
            hq = new ArrayList<String>(hq_copy);
            vq = new ArrayList<String>(vq_copy);
            return result;
        }
        // if (tree.getChildren().size() == 2) { 
        //     List<Tree<String>> children = new ArrayList<Tree<String>>();
        //     String label1 = tree.getChildren().get(0).getLabel();
        //     children.add(binarizeTree(tree.getChildren().get(0)));
        //     Tree<String> right = binarizeTree(tree.getChildren().get(1));
        //     addHTag(label1);
        //     children.add(binarizerHelper(right));
        //     Tree<String> result = new Tree<String>(label, children);
        //     hq = new ArrayList<String>(hq_copy);
        //     vq = new ArrayList<String>(vq_copy);
        //     return result;
        // }
		// otherwise, it's a binary-or-more local tree, so decompose it into a sequence of binary and unary trees.
		String intermediateLabel = getVerizontalLabel();
        Tree<String> intermediateTree = binarizeTreeHelper(tree, 0, intermediateLabel);
        hq = new ArrayList<String>(hq_copy);
        vq = new ArrayList<String>(vq_copy);
		return new Tree<String>(label, intermediateTree.getChildren());
    }

    private Tree<String> binarizerHelper(Tree<String> tree) {
        String label = tree.getLabel();
        label += getHorizontalTags();
        tree.setLabel(label);
        return tree;
    }

    private Tree<String> binarizeTreeHelper(Tree<String> tree, int numChildrenGenerated, String intermediateLabel) {
		Tree<String> leftTree = tree.getChildren().get(numChildrenGenerated);
		List<Tree<String>> children = new ArrayList<Tree<String>>();
        children.add(binarizeTree(leftTree));
        String label = intermediateLabel + getHorizontalTags();
        addHTag(leftTree.getLabel());
		if (numChildrenGenerated < tree.getChildren().size() - 1) {
			Tree<String> rightTree = binarizeTreeHelper(tree, numChildrenGenerated + 1, intermediateLabel);
			children.add(rightTree);
		}
		return new Tree<String>(label, children);
	}

    public Tree<String> unAnnotateTree(Tree<String> annotatedTree) {
        // Remove intermediate nodes (labels beginning with "@"
		// Remove all material on node labels which follow their base symbol (cuts anything after <,>,^,=,_ or ->)
		// Examples: a node with label @NP->DT_JJ will be spliced out, and a node with label NP^S will be reduced to NP
		Tree<String> debinarizedTree = Trees.spliceNodes(annotatedTree, new Filter<String>()
		{
			public boolean accept(String s) {
				return s.startsWith("@");
			}
		});
		Tree<String> unAnnotatedTree = (new Trees.LabelNormalizer()).transformTree(debinarizedTree);
		return unAnnotatedTree;
    }

}