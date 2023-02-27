package com.example.applicazionetesi;

import android.util.Pair;

import java.io.Serializable;
import java.util.List;

public class SerializablePair implements Serializable {
    private final transient List<Pair<Integer, Integer>> first;
    private final transient List<Pair<Integer, Integer>> second;

    public SerializablePair(List<Pair<Integer, Integer>> first, List<Pair<Integer, Integer>> second) {
        this.first = first;
        this.second = second;
    }

    public List<Pair<Integer, Integer>> getFirst() {
        return first;
    }

    public List<Pair<Integer, Integer>> getSecond() {
        return second;
    }
}
