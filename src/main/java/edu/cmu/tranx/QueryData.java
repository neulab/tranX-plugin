package edu.cmu.tranx;

import java.util.List;

public class QueryData {
    String hash;
    String query;
    List<Hypothesis> candidates;
    int selectedIndex;
    String userId;
    String projectName;
    String fileName;
    String eventType;
    String document;
    long clientTimestamp;
}
