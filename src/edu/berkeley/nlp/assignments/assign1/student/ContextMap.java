package edu.berkeley.nlp.assignments.assign1.student;

public class ContextMap {
    // private ContextEntry[] map;
    private int max_size;
    private int count;
    private RankTable rankTable;
    private int[] value;
    private int[] prefixCount;
    private int[] suffixCount;

    public ContextMap(int n) {
        max_size = NgramUtils.exactSize(n);
        rankTable = new RankTable(n);
        value = new int[max_size];
        prefixCount = new int[max_size];
        suffixCount = new int[max_size];
        count = 0;
    }

    private int addIndex(long key) {
        int rank = rankTable.insertKey(key);
        if(rank < 0 || rank >= value.length) {
            System.out.println("WARNING: ContextMap: nowhere to add index.");
            System.out.printf("Rank = %d, length = %d\n",rank,value.length);
            return -1;
        }
        return rank;
    }

    private int findIndex(long key) {
        int rank = rankTable.getKeyRank(key);
        if(rank < 0 || rank >= value.length) {
            return -1;
        }
        return rank;
    }

    public boolean containsKey(long key) {
        int index = findIndex(key);
        if(index < 0) {
            return false;
        }
        return true;
    }

    public int addKey(long key) {
        return addIndex(key);
    }

    public int addKeyValue(long key) {
        int index = addIndex(key);
        if(index < 0) {
            return -1;
        }
        value[index]++;
        return index;
    }

    public int addKeyPrefixCount(long key) {
        int index = addIndex(key);
        if(index < 0) {
            return -1;
        }
        prefixCount[index]++;
        return index;
    }

    public int addKeySuffixCount(long key) {
        int index = addIndex(key);
        if(index < 0) {
            return -1;
        }
        suffixCount[index]++;
        return index;
    }

    public int addIndexValue(int index) {
        if(index < 0 || index >= value.length) {
            return -1;
        }
        value[index]++;
        return index;
    }

    public int addIndexPrefixCount(int index) {
        if(index < 0 || index >= value.length) {
            return -1;
        }
        prefixCount[index]++;
        return index;
    }

    public int addIndexSuffixCount(int index) {
        if(index < 0 || index >= value.length) {
            return -1;
        }
        suffixCount[index]++;
        return index;
    }

    public int indexOf(long key) {
        return findIndex(key);
    }

    public int getKeyValue(long key) {
        int index = findIndex(key);
        if(index < 0) {
            return 0;
        }
        return value[index];
    }

    public int getIndexValue(int index) {
        if(index < 0 || index >= value.length) {
            return 0;
        }
        return value[index];
    }

    public int getKeyPrefixCount(long key) {
        int index = findIndex(key);
        if(index < 0) {
            return 0;
        }
        return prefixCount[index];
    }

    public int getIndexPrefixCount(int index) {
        if(index < 0 || index >= value.length) {
            return 0;
        }
        return prefixCount[index];
    }

    public int getKeySuffixCount(long key) {
        int index = findIndex(key);
        if(index < 0) {
            return 0;
        }
        return suffixCount[index];
    }

    public int getIndexSuffixCount(int index) {
        if(index < 0 || index >= value.length) {
            return 0;
        }
        return suffixCount[index];
    }

    public void print(int size) {
        int count = 0;
        for(int i = 0; i < value.length; i++) {
            if(value[i] != 0) {
                System.out.printf("%d\t%d\t%d\t%d\n",i,value[i],prefixCount[i],suffixCount[i]);
                count++;
                if(count > size) {
                    break;
                }
            }
        }
    }

    public void print_last(int size) {
        int count = 0;
        for(int i = value.length - 1; i >= 0; i--) {
            if(value[i] != 0) {
                System.out.printf("%d\t%d\t%d\t%d\n",i,value[i],prefixCount[i],suffixCount[i]);
                count++;
                if(count > size) {
                    break;
                }
            }
        }
    }

    public void print_word(long key) {
        int i = findIndex(key);
        if(i < 0)
            return;
        System.out.printf("%d\t%d\t%d\t%d\n",i,value[i],prefixCount[i],suffixCount[i]);
    }
}