package edu.cmu.tranx;

import java.util.ArrayList;

public class Hypothesis {
    int id;
    double score;
    String value;
    transient String tree_repr;
    transient ArrayList<String> actions;
    transient String htmlValue;
}
