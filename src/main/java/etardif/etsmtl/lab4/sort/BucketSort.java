package etardif.etsmtl.lab4.sort;

import java.util.ArrayList;
import java.util.List;

public class BucketSort extends SortingAlgorithm {

    @Override
    public String getName() { return "Bucket Sort"; }

    @Override
    protected void performSort(int[] array, int low, int high) {
        if (array.length <= 1) return;

        int maxVal = array[0], minVal = array[0];
        for (int v : array) {
            if (v > maxVal) maxVal = v;
            if (v < minVal) minVal = v;
        }

        int bucketCount = (int) Math.sqrt(array.length);
        if (bucketCount < 1) bucketCount = 1;
        int range = maxVal - minVal + 1;

        List<List<Integer>> buckets = new ArrayList<>();
        for (int i = 0; i < bucketCount; i++) {
            buckets.add(new ArrayList<>());
        }

        // Distribute into buckets
        for (int i = 0; i < array.length; i++) {
            int bucketIdx = (int) ((long) (array[i] - minVal) * (bucketCount - 1) / range);
            buckets.get(bucketIdx).add(array[i]);
            highlightedIndices = new int[]{i};
            notifyObservers();
        }

        // Sort each bucket with insertion sort and write back
        int idx = 0;
        for (List<Integer> bucket : buckets) {
            // Insertion sort within the bucket
            for (int i = 1; i < bucket.size(); i++) {
                int key = bucket.get(i);
                int j = i - 1;
                while (j >= 0 && bucket.get(j) > key) {
                    bucket.set(j + 1, bucket.get(j));
                    j--;
                }
                bucket.set(j + 1, key);
            }
            // Write sorted bucket back to array
            for (int val : bucket) {
                array[idx] = val;
                highlightedIndices = new int[]{idx};
                notifyObservers();
                idx++;
            }
        }
    }

    @Override
    protected int divide(int[] array, int low, int high) {
        return 0;
    }

    @Override
    protected void merge(int[] array, int low, int mid, int high) {
    }
}
