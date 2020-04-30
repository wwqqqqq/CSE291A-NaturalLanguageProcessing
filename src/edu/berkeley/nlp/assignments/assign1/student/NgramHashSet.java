package edu.berkeley.nlp.assignments.assign1.student;

public class NgramHashSet {
    private Long[] map;
	private int estimate_max_size;
	private int count;

	public NgramHashSet(int n) {
		estimate_max_size = NgramUtils.estimateSize(n);
        map = new Long[estimate_max_size];
		count = 0;
	}

	private int hash(long key) {
		int h = NgramUtils.hash(key);
		return (h % (estimate_max_size));
	}

	private int addIndex(long key) {
		int h = hash(key);
		int index = h;
		if(index >= map.length || count >= map.length) {
			System.out.println("WARNING: NgramHashSet: INDEX > ESTIMATE_SIZE");
			return 0;
		}
		for(; index < map.length; index++) {
			if(map[index] == null) {
				map[index] = key;
				return index;
			}
			if(map[index] == key) {
				return index;
			}
		}
		for(index = 0; index < h && index < map.length; index++) {
			if(map[index] == null) {
				map[index] = key;
				return index;
			}
			if(map[index] == key) {
				return index;
			}
		}

		System.out.println("WARNING: NgramHashSet: nowhere to add index.");
		return 0;
	}

	private int findIndex(long key) {
		int h = hash(key);
		int index = h;
		for(; index < map.length; index++) {
			if(map[index] == null) {
				return -1;
			}
			if(map[index] == key) {
				return index;
			}
		}
		for(index = 0; index < h && index < map.length; index++) {
			if(map[index] == null) {
				return -1;
			}
			if(map[index] == key) {
				return index;
			}
		}
		return -1;
	}

	public boolean contains(long key) {
		int index = findIndex(key);
		if(index < 0) {
			return false;
		}
		return true;
	}

	public int add(long key) {
        int index = addIndex(key);
        return index;
    }
}