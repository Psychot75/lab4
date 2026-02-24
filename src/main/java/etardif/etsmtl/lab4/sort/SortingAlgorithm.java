package etardif.etsmtl.lab4.sort;

import etardif.etsmtl.lab4.interfaces.Observable;

public abstract class SortingAlgorithm extends Observable {

    protected int[] array;
    protected int[] highlightedIndices = new int[0];
    private boolean sortComplete = false;
    private volatile long delayMs = 30;
    private volatile boolean cancelled = false;

    public final void sort(int[] data) {
        this.array = data.clone();
        this.sortComplete = false;
        this.cancelled = false;
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

    public void setDelay(long delayMs) {
        this.delayMs = delayMs;
    }

    public void cancel() {
        this.cancelled = true;
    }

    @Override
    protected void notifyObservers() {
        if (cancelled) throw new RuntimeException("Sort cancelled");
        super.notifyObservers();
        if (delayMs > 0) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
