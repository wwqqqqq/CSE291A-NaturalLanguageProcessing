package edu.berkeley.nlp.assignments.assign1.student;

public class UnigramMap {
    private UnigramEntry[] map;
    private int estimate_max_size;
    private int count;

    public UnigramMap() {
        estimate_max_size = NgramUtils.estimateSize(1);
        map = new UnigramEntry[estimate_max_size];
        count = 0;
    }

    private int addIndex(int key) {
        if(key < 0 || key >= map.length) {
            System.out.println("WARNING: UnigramMap: index out of bound");
        }
        if(map[key] == null) {
            map[key] = new UnigramEntry();
        }
        return key;
    }

    private int findIndex(int key) {
        int index = key;
        if(index < 0 || index >= map.length) {
            return -1;
        }
        return index;
    }

    private UnigramEntry accessMapIndex(int index) {
        if(index >= map.length || index < 0) {
            // System.out.println("WARNING: UnigramMap: Index out of Boundary!");
            return null;
        }
        if(map[index] == null) {
            // System.out.println("WARNING: UnigramMap: access null entry!");
            return null;
        }
        return map[index];
    }

    public boolean containsKey(int key) {
        int index = findIndex(key);
        if(index < 0 || index >= map.length) {
            return false;
        }
        return true;
    }

    public int addKey(int key) {
        return addIndex(key);
    }

    public int addValue(int index) {
        map[index].value++;
        return index;
    }

    public int addPrefixCount(int index) {
        map[index].prefixCount++;
        return index;
    }

    public int addSuffixCount(int index) {
        map[index].suffixCount++;
        return index;
    }

    public int addMiddleCount(int index) {
        map[index].middleCount++;
        return index;
    }

    public int getValue(int index) {
        UnigramEntry ue = accessMapIndex(index);
        if(ue == null) {
            return 0;
        }
        return ue.value;
    }

    public int getPrefixCount(int index) {
        UnigramEntry ue = accessMapIndex(index);
        if(ue == null) {
            return 0;
        }
        return ue.prefixCount;
    }

    public int getSuffixCount(int index) {
        UnigramEntry ue = accessMapIndex(index);
        if(ue == null) {
            return 0;
        }
        return ue.suffixCount;
    }

    public int getMiddleCount(int index) {
        UnigramEntry ue = accessMapIndex(index);
        if(ue == null) {
            return 0;
        }
        return ue.middleCount;
    }

    public void print(int size) {
        int count = 0;
        for(int i = 0; i < map.length; i++) {
            if(map[i] != null) {
                System.out.printf("%d\t%d\t%d\t%d\t%d\n",i,map[i].value,map[i].prefixCount,map[i].suffixCount,map[i].middleCount);
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
                System.out.printf("%d\t%d\t%d\t%d\t%d\n",i,map[i].value,map[i].prefixCount,map[i].suffixCount,map[i].middleCount);
                count++;
                if(count > size) {
                    break;
                }
            }
        }
    }

}