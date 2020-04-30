package edu.berkeley.nlp.assignments.assign1.student;

public class UnigramEntry {
    public int value;
    public int prefixCount;
    public int suffixCount;
    public int middleCount;

    public UnigramEntry() {
        value = 0;
        prefixCount = 0;
        suffixCount = 0;
        middleCount = 0;
    }
}