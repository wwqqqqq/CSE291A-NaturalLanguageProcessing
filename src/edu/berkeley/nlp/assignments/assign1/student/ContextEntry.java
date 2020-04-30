package edu.berkeley.nlp.assignments.assign1.student;

public class ContextEntry {
    public long word;
    public int value;
    public int prefixCount;
    public int suffixCount;

    public ContextEntry() {
        word = 0;
        value = 0;
        prefixCount = 0;
        suffixCount = 0;
    }

    public ContextEntry(long key) {
        word = key;
        value = 0;
        prefixCount = 0;
        suffixCount = 0;
    }
}