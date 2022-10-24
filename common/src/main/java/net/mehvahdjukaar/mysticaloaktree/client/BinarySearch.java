package net.mehvahdjukaar.mysticaloaktree.client;

import java.util.List;

public class BinarySearch {

    public static <T extends Comparable<T>> int find(List<T> list, T target) {
        return find(list, 0, list.size() - 1, target);
    }

    public static <T extends Comparable<T>> int find(List<T> list, int min, int max, T target) {
        if (max >= min) {
            int mid = min + (max - min) / 2;

            // If the element is present at the
            // middle itself
            var j = list.get(mid);
            int compared = j.compareTo(target);
            if (compared == 0) {
                //pick higher most
                //this is written so bad
                int i = 1;
                mid += i;
                if (mid < list.size()) {
                    j = list.get(mid);

                    while (mid + i < list.size() && j.compareTo(target) == 0) {
                        mid += i;
                        j = list.get(mid);
                    }
                }

                return mid - i;
            }

            // If element is smaller than mid, then
            // it can only be present in left subarray
            else if (compared > 0) {
                return find(list, min, mid - 1, target);
            }
            // Else the element can only be present
            // in right subarray
            else {
                return find(list, mid + 1, max, target);
            }
        }
        // We reach here when element is not present
        // in array
        return min;
    }
}
