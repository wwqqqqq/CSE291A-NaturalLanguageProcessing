package edu.berkeley.nlp.assignments.assign1.student;

public class NgramHashSet {
    private Long[] map;
	private int estimate_max_size;
	private int count;

	public NgramHashSet(int n) {
		if(n == 1) {
			estimate_max_size = 1000000; // 495172
		}
		else if(n == 2) {
			estimate_max_size = 10000000; // 8374230
		}
		else if(n == 3) {
			estimate_max_size = 50000000; // 25760367
		}
		else {
			System.out.println("WARNING: Order > 3 in NgramHashSet.");
		}
		map = new Long[estimate_max_size];
		count = 0;
	}

	private int hash(long key) {
		// TODO: design a hash function
		int h = (int)((key ^ (key >>> 32)) * 3875239);
		if (h < 0) {
			h = - (h + 1);
		}
		return (h % estimate_max_size);
	}

	private int addIndex(long key) {
		int h = hash(key);
		int index = h;
		if(index >= map.length || count >= map.length) {
			System.out.println("WARNING: NgramHashSet: INDEX > ESTIMATE_SIZE");
			return 0;
		}
		for(; index < map.length; index++) {
			if(map[index] == 0) {
				map[index] = key;
				return index;
			}
			if(map[index] == key) {
				return index;
			}
		}
		for(index = 0; index < h && index < map.length; index++) {
			if(map[index] == 0) {
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
			if(map[index] == 0) {
				return -1;
			}
			if(map[index] == key) {
				return index;
			}
		}
		for(index = 0; index < h && index < map.length; index++) {
			if(map[index] == 0) {
				return -1;
			}
			if(map[index] == key) {
				return index;
			}
		}
		return -1;
	}

	public boolean containsKey(long key) {
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