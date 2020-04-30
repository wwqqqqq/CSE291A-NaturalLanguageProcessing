package edu.berkeley.nlp.assignments.assign1.student;

public class NgramHashMap {
	// HashMap<Long, HashMap<Integer, Integer>> map = new HashMap<Long, HashMap<Integer, Integer>>();
	// private HashMapEntry[] map;
	private int[] value;
	private RankTable rankTable;
	private int max_size;
	private int count;

	public NgramHashMap(int n) {
		max_size = NgramUtils.exactSize(n);
		rankTable = new RankTable(n);
		value = new int[max_size];
		count = 0;
	}

	private int findIndex(long key) {
		int index = rankTable.getKeyRank(key);
		if(index < 0 || index >= value.length) {
			return -1;
		}
		return index;
	}
	
	public boolean containsKey(long key) {
		int index = findIndex(key);
		if(index < 0) {
			return false;
		}
		return true;
	}

	public int addOne(long key) {
		int rank = rankTable.insertKey(key);
		if(rank < 0 || rank >= value.length) {
			System.out.println("NgramHashMap: out of bound exception");
			System.out.printf("Rank = %d, length = %d\n",rank,value.length);
			return -1;
		}
		value[rank]++;
		return rank;
	}

	public int put(long key, int val) {
		int index = rankTable.insertKey(key);
		value[index] = val;
		return index;
	}

	public int indexOf(long key) {
		return findIndex(key);
	}

	public int get(long key) {
		return getKeyValue(key);
	}

	public int getKeyValue(long key) {
		int index = findIndex(key);
		if(index < 0) {
			return 0;
		}
        return value[index];
	}

	public int getIndexValue(int index) {
		if(index < 0) {
			return 0;
		}
        return value[index];
	}

	public void print(int size) {
		int count = 0;
		for(int i = 0; i < value.length; i++) {
			if(value[i] != 0) {
				System.out.printf("%d\t%d\n",i,value[i]);
				count++;
				if(count > size) {
					break;
				}
			}
		}
	}

	public void print_last(int size) {
		int count = 0;
		for(int i = value.length-1; i >= 0; i--) {
			if(value[i] != 0) {
				System.out.printf("%d\t%d\n",i,value[i]);
				count++;
				if(count > size) {
					break;
				}
			}
		}
	}
}