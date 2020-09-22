package edu.berkeley.nlp.assignments.assign1.student;

public class NgramUtils {
    public static long getConcatenateIndex(long ind1, int ind2) {
        long ind2l = ind2;
		return (ind1 << 20) | ind2l;
	}
	
	public static long getConcatenateIndex(int ind1, int ind2) {
        long ind1l = ind1;
        long ind2l = ind2;
		ind1l = (ind1l << 20) | ind2l;
		return ind1l;
    }

    public static long getConcatenateIndex(long ind1, long ind2) {
        return (ind1 << 40) | ind2;
    }
    
    static final public double d = 0.8;
}