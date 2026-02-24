package etardif.etsmtl.lab4.sort;

import etardif.etsmtl.lab4.interfaces.Observable;

public abstract class SortingAlgorithm extends Observable {

    protected int[] array;
    protected int[] highlightedIndices = new int[0];
    private boolean sortComplete = false;

    public final void sort(int[] data) {
        this.array = data.clone();
        this.sortComplete = false;
        initialize();
        notifyObservers();
        performSort(array, 0, array.length - 1);
        highlightedIndices = new int[0];
        sortComplete = true;
        notifyObservers();
    }

    protected abstract void performSort(int[] array, int low, int high);
    protected abstract int divide(int[] array, int low, int high);
    protected abstract void merge(int[] array, int low, int mid, int high);
    protected void initialize() {}

    public SortStep getCurrentStep() {
        return new SortStep(array.clone(), highlightedIndices.clone(), sortComplete);
    }

    public abstract String getName();
}
