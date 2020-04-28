package edu.berkeley.nlp.assignments.assign1.student;

public class NgramHashMap {
	// HashMap<Long, HashMap<Integer, Integer>> map = new HashMap<Long, HashMap<Integer, Integer>>();
	private HashMapEntry[] map;
	private int estimate_max_size;
	private int count;

	public NgramHashMap(int n) {
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
			System.out.println("WARNING: Order > 3 in NgramHashMap.");
		}
		map = new HashMapEntry[estimate_max_size];
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
			System.out.println("WARNING: INDEX > ESTIMATE_SIZE");
			return 0;
		}
		for(; index < map.length; index++) {
			if(map[index] == null) {
				map[index] = new HashMapEntry(key);
				count++;
				return index;
			}
			if(map[index].word == key) {
				return index;
			}
		}
		for(index = 0; index < h && index < map.length; index++) {
			if(map[index] == null) {
				map[index] = new HashMapEntry(key);
				count++;
				return index;
			}
			if(map[index].word == key) {
				return index;
			}
		}

		System.out.println("WARNING: NgramHashMap: nowhere to add index.");
		return 0;
	}

	private int findIndex(long key) {
		int h = hash(key);
		int index = h;
		for(; index < map.length; index++) {
			if(map[index] == null || map[index].value == 0) {
				return -1;
			}
			if(map[index].word == key) {
				return index;
			}
		}
		for(index = 0; index < h && index < map.length; index++) {
			if(map[index] == null || map[index].value == 0) {
				return -1;
			}
			if(map[index].word == key) {
				return index;
			}
		}
		return -1;
	}

	private HashMapEntry accessMapIndex(int index) {
		if(index >= map.length || index < 0) {
			System.out.println("WARNING: NgramHashMap: Index out of Boundary!");
			return null;
		}
		if(map[index] == null) {
			System.out.println("WARNING: NgramHashMap: access null entry!");
			return null;
		}
		return map[index];
	}
	
	public boolean containsKey(long key) {
		int index = findIndex(key);
		if(index < 0) {
			return false;
		}
		return true;
	}

	public int addOne(long key) {
		int index = addIndex(key);
		map[index].value++;
		return index;
	}

	public int put(long key, int value) {
		int index = addIndex(key);
		map[index].value = value;
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
		return accessMapIndex(index).value;
	}

	public int getIndexValue(int index) {
		if(index < 0) {
			return 0;
		}
		return accessMapIndex(index).value;
	}

	public void print(int size) {
		int count = 0;
		for(int i = 0; i < map.length; i++) {
			if(map[i] != null) {
				System.out.printf("%d\n",map[i].value);
				count++;
				if(count > size) {
					break;
				}
			}
		}
	}
}