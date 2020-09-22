package edu.berkeley.nlp.assignments.assign1.student;

public class HashMapEntry {
    public long word;
    public int value;

    public HashMapEntry() {
        word = 0;
        value = 0;
    }

    public HashMapEntry(long key) {
        word = key;
        value = 0;
    }
}