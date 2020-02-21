package edu.cmu.tranx;

import java.util.List;

public class Utils {
    public static <T> List<T> firstK(List<T> list, int k) {
        if (k > list.size())
            k = list.size();
        return list.subList(0, k);
    }
}
