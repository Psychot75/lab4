package etardif.etsmtl.lab4.sort;

public class SortStep {

    private final int[] arrayState;
    private final int[] highlightedIndices;
    private final boolean sortComplete;

    public SortStep(int[] arrayState, int[] highlightedIndices, boolean sortComplete) {
        this.arrayState = arrayState;
        this.highlightedIndices = highlightedIndices;
        this.sortComplete = sortComplete;
    }

    public int[] getArrayState() { return arrayState; }
    public int[] getHighlightedIndices() { return highlightedIndices; }
    public boolean isSortComplete() { return sortComplete; }
}