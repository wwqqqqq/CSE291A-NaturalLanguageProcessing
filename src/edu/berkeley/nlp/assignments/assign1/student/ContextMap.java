package edu.berkeley.nlp.assignments.assign1.student;

public class ContextMap {
    private ContextEntry[] map;
    private int estimate_max_size;
    private int count;

    public ContextMap(int n) {
        estimate_max_size = NgramUtils.estimateSize(n);
        map = new ContextEntry[estimate_max_size];
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
            System.out.println("WARNING: INDEX > ESTIMATE_SIZE");
            return 0;
        }
        for(; index < map.length; index++) {
            if(map[index] == null) {
                map[index] = new ContextEntry(key);
                count++;
                return index;
            }
            if(map[index].word == key) {
                return index;
            }
        }
        for(index = 0; index < h && index < map.length; index++) {
            if(map[index] == null) {
                map[index] = new ContextEntry(key);
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

    private ContextEntry accessMapIndex(int index) {
        if(index >= map.length || index < 0) {
            // System.out.println("WARNING: ContextMap: Index out of Boundary!");
            return null;
        }
        if(map[index] == null) {
            // System.out.println("WARNING: ContextMap: access null entry!");
            return null;
        }
        return map[index];
    }
    
    public boolean containsKey(long key) {
        int index = findIndex(key);
        if(index < 0 || index >= map.length) {
            return false;
        }
        return true;
    }

    public int addKey(long key) {
        return addIndex(key);
    }

    public int addKeyValue(long key) {
        int index = addIndex(key);
        map[index].value++;
        return index;
    }

    public int addKeyPrefixCount(long key) {
        int index = addIndex(key);
        map[index].prefixCount++;
        return index;
    }

    public int addKeySuffixCount(long key) {
        int index = addIndex(key);
        map[index].suffixCount++;
        return index;
    }

    public int addIndexValue(int index) {
        map[index].value++;
        return index;
    }

    public int addIndexPrefixCount(int index) {
        map[index].prefixCount++;
        return index;
    }

    public int addIndexSuffixCount(int index) {
        map[index].suffixCount++;
        return index;
    }

    // public int put(long key, int value) {
    //     int index = addIndex(key);
    //     map[index].value = value;
    //     return index;
    // }

    public int indexOf(long key) {
        return findIndex(key);
    }

    public int getKeyValue(long key) {
        int index = findIndex(key);
        ContextEntry ce = accessMapIndex(index);
        if(ce == null) {
            return 0;
        }
        return ce.value;
    }

    public int getIndexValue(int index) {
        ContextEntry ce = accessMapIndex(index);
        if(ce == null) {
            return 0;
        }
        return ce.value;
    }

    public int getKeyPrefixCount(long key) {
        int index = findIndex(key);
        ContextEntry ce = accessMapIndex(index);
        if(ce == null) {
            return 0;
        }
        return ce.prefixCount;
    }

    public int getIndexPrefixCount(int index) {
        ContextEntry ce = accessMapIndex(index);
        if(ce == null) {
            return 0;
        }
        return ce.prefixCount;
    }

    public int getKeySuffixCount(long key) {
        int index = findIndex(key);
        ContextEntry ce = accessMapIndex(index);
        if(ce == null) {
            return 0;
        }
        return ce.suffixCount;
    }

    public int getIndexSuffixCount(int index) {
        ContextEntry ce = accessMapIndex(index);
        if(ce == null) {
            return 0;
        }
        return ce.suffixCount;
    }

    public void print(int size) {
        int count = 0;
        for(int i = 0; i < map.length; i++) {
            if(map[i] != null) {
                System.out.printf("%d\t%d\t%d\t%d\n",i,map[i].value,map[i].prefixCount,map[i].suffixCount);
                count++;
                if(count > size) {
                    break;
                }
            }
        }
    }

    public void print_last(int size) {
        int count = 0;
        for(int i = map.length - 1; i >= 0; i--) {
            if(map[i] != null) {
                System.out.printf("%d\t%d\t%d\t%d\n",i,map[i].value,map[i].prefixCount,map[i].suffixCount);
                count++;
                if(count > size) {
                    break;
                }
            }
        }
    }

    public void print_word(long key) {
        int i = findIndex(key);
        ContextEntry ce = accessMapIndex(i);
        if(ce == null) {
            System.out.println("Not exist");
            return;
        }
        System.out.printf("%d\t%d\t%d\t%d\n",i,map[i].value,map[i].prefixCount,map[i].suffixCount);
    }
}