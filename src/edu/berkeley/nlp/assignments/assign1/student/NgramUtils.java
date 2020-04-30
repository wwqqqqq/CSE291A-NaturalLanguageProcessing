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

    public static int estimateSize(int n) {
        int estimate_max_size = 0;
        if(n == 1) {
			estimate_max_size = 500001; // 495172
		}
		else if(n == 2) {
			estimate_max_size = 16000001; // 8374230
		}
		else if(n == 3) {
			estimate_max_size = 50000001; // 41736000
		}
		else {
			System.out.println("WARNING: Order > 3 in NgramHashSet.");
        }
        return estimate_max_size;
    }

    public static int exactSize(int n) {
        if(n == 1) {
			return 495174; // 495172
		}
		else if(n == 2) {
			return 8374232; // 8374230
		}
		else if(n == 3) {
			return 41736002; // 41736000
		}
		else {
			System.out.println("WARNING: Order > 3 in NgramHashSet.");
        }
        return 0;
    }

    public static int hash(long key) {
        // TODO: design a hash function
        int h = (int)((key ^ (key >>> 32)) * 3875239);
		if (h < 0) {
			h = - (h + 1);
        }
        return h;
    }
}