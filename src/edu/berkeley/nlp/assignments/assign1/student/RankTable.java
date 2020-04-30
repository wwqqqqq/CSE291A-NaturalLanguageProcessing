package edu.berkeley.nlp.assignments.assign1.student;

public class RankTable {
    long[] keys;
    int[] rank;

    private int estimate_max_size;
    private int count;

    public RankTable(int n) {
        estimate_max_size = NgramUtils.estimateSize(n);
        keys = new long[estimate_max_size];
        rank = new int[estimate_max_size];
        count = 1;
    }

    private int hash(long key) {
        int h = NgramUtils.hash(key);
        return (h % (estimate_max_size));
    }

    private int addIndex(long key) {
        int h = hash(key);
        int index = h;
        if(index >= keys.length || count >= keys.length) {
            System.out.println("WARNING: INDEX > ESTIMATE_SIZE");
            return 0;
        }
        for(; index < keys.length; index++) {
            if(rank[index] == 0) {
                keys[index] = key;
                rank[index] = count;
                count++;
                return index;
            }
            if(keys[index] == key) {
                return index;
            }
        }
        for(index = 0; index < h && index < keys.length; index++) {
            if(rank[index] == 0) {
                keys[index] = key;
                rank[index] = count;
                count++;
                return index;
            }
            if(keys[index] == key) {
                return index;
            }
        }

        System.out.println("WARNING: RankTable: nowhere to add index.");
        return -1;
    }

    private int findIndex(long key) {
        int h = hash(key);
        int index = h;
        for(; index < keys.length; index++) {
            if(rank[index] == 0) {
                return -1;
            }
            if(keys[index] == key) {
                return index;
            }
        }
        for(index = 0; index < h && index < keys.length; index++) {
            if(rank[index] == 0) {
                return -1;
            }
            if(keys[index] == key) {
                return index;
            }
        }
        return -1;
    }

    public int insertKey(long key) {
        int index = addIndex(key);
        return rank[index];
    }
    
    public boolean containsKey(long key) {
        int index = findIndex(key);
        if(index < 0 || index >= keys.length) {
            return false;
        }
        return true;
    }

    public int getKeyRank(long key) {
        int index = findIndex(key);
        if(index < 0 || index >= keys.length) {
            return -1;
        }
        return rank[index];
    }

    public int getKeyIndex(long key) {
        return findIndex(key);
    }

    public int getIndexRank(int index) {
        if(index < 0 || index >= keys.length) {
            return -1;
        }
        return rank[index];
    }
}