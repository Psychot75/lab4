package etardif.etsmtl.lab4.sort;

public class QuickSort extends SortingAlgorithm {

    @Override
    public String getName() { return "Quick Sort"; }

    @Override
    protected void performSort(int[] array, int low, int high) {
        if (low < high) {
            int pivotIndex = divide(array, low, high);
            performSort(array, low, pivotIndex - 1);
            performSort(array, pivotIndex + 1, high);
        }
    }

    @Override
    protected int divide(int[] array, int low, int high) {
        int pivot = array[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            highlightedIndices = new int[]{j, high};
            notifyObservers();
            if (array[j] <= pivot) {
                i++;
                swap(array, i, j);
                highlightedIndices = new int[]{i, j};
                notifyObservers();
            }
        }
        swap(array, i + 1, high);
        highlightedIndices = new int[]{i + 1, high};
        notifyObservers();
        return i + 1;
    }

    @Override
    protected void merge(int[] array, int low, int mid, int high) {}

    private void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}
