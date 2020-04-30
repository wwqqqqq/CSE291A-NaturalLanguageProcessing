package edu.berkeley.nlp.assignments.assign1.student;

public class UnigramMap {
    public int[] value;
    public int[] prefixCount;
    public int[] suffixCount;
    public int[] middleCount;
    private int estimate_max_size;
    private int count;

    public UnigramMap() {
        estimate_max_size = NgramUtils.exactSize(1);
        value = new int[estimate_max_size];
        prefixCount = new int[estimate_max_size];
        suffixCount = new int[estimate_max_size];
        middleCount = new int[estimate_max_size];
        count = 0;
    }

    private int addIndex(int key) {
        if(key < 0 || key >= value.length) {
            System.out.println("WARNING: UnigramMap: index out of bound");
        }
        return key;
    }

    private int findIndex(int index) {
        if(index < 0 || index >= value.length) {
            return -1;
        }
        return index;
    }

    public boolean containsKey(int key) {
        int index = findIndex(key);
        if(index < 0 || index >= value.length) {
            return false;
        }
        return true;
    }

    public int addKey(int key) {
        return addIndex(key);
    }

    public int addValue(int index) {
        if(index < 0 || index >= value.length) {
            return -1;
        }
        value[index]++;
        return index;
    }

    public int addPrefixCount(int index) {
        if(index < 0 || index >= value.length) {
            return -1;
        }
        prefixCount[index]++;
        return index;
    }

    public int addSuffixCount(int index) {
        if(index < 0 || index >= value.length) {
            return -1;
        }
        suffixCount[index]++;
        return index;
    }

    public int addMiddleCount(int index) {
        if(index < 0 || index >= value.length) {
            return -1;
        }
        middleCount[index]++;
        return index;
    }

    public int getValue(int index) {
        if(index < 0 || index >= value.length) {
            return 0;
        }
        return value[index];
    }

    public int getPrefixCount(int index) {
        if(index < 0 || index >= value.length) {
            return 0;
        }
        return prefixCount[index];
    }

    public int getSuffixCount(int index) {
        if(index < 0 || index >= value.length) {
            return 0;
        }
        return suffixCount[index];
    }

    public int getMiddleCount(int index) {
        if(index < 0 || index >= value.length) {
            return 0;
        }
        return middleCount[index];
    }

    public void print(int size) {
        int count = 0;
        for(int i = 0; i < value.length; i++) {
            System.out.printf("%d\t%d\t%d\t%d\t%d\n",i,value[i],prefixCount[i],suffixCount[i],middleCount[i]);
            count++;
            if(count > size) {
                break;
            }
        }
    }

    public void print_last(int size) {
        int count = 0;
        for(int i = value.length - 1; i >= 0; i--) {
            System.out.printf("%d\t%d\t%d\t%d\t%d\n",i,value[i],prefixCount[i],suffixCount[i],middleCount[i]);
            count++;
            if(count > size) {
                break;
            }
        }
    }


}