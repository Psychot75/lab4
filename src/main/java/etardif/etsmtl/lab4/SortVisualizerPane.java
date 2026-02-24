package etardif.etsmtl.lab4;

import etardif.etsmtl.lab4.interfaces.Observable;
import etardif.etsmtl.lab4.interfaces.Observer;
import etardif.etsmtl.lab4.sort.SortStep;
import etardif.etsmtl.lab4.sort.SortingAlgorithm;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SortVisualizerPane extends VBox {

    private static final Color BAR_COLOR = Color.web("#3b82f6");
    private static final Color HIGHLIGHT_COLOR = Color.web("#ef4444");
    private static final Color COMPLETE_COLOR = Color.web("#22c55e");

    private final Canvas canvas;
    private final Label titleLabel;
    private SortingAlgorithm algorithm;
    private Observer observer;
    private int[] data;
    private int[] highlightedIndices = new int[0];
    private boolean sortComplete = false;

    public SortVisualizerPane(SortingAlgorithm algorithm) {
        this.algorithm = algorithm;
        this.getStyleClass().add("visualizer-pane");
        this.setAlignment(Pos.TOP_CENTER);

        titleLabel = new Label(algorithm.getName());
        titleLabel.getStyleClass().add("visualizer-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);

        canvas = new Canvas();
        Pane canvasHolder = new Pane(canvas);
        canvasHolder.getStyleClass().add("canvas-holder");
        VBox.setVgrow(canvasHolder, Priority.ALWAYS);

        canvas.widthProperty().bind(canvasHolder.widthProperty());
        canvas.heightProperty().bind(canvasHolder.heightProperty());
        canvas.widthProperty().addListener((obs, old, val) -> draw());
        canvas.heightProperty().addListener((obs, old, val) -> draw());

        this.getChildren().addAll(titleLabel, canvasHolder);
        setupObserver();
    }

    private void setupObserver() {
        observer = new Observer() {
            @Override
            public void update(Observable o) {
                SortingAlgorithm algo = (SortingAlgorithm) o;
                SortStep step = algo.getCurrentStep();
                Platform.runLater(() -> {
                    data = step.getArrayState();
                    highlightedIndices = step.getHighlightedIndices();
                    sortComplete = step.isSortComplete();
                    draw();
                });
            }
        };
        algorithm.attach(observer);
    }

    public void setData(int[] newData) {
        this.data = newData.clone();
        this.highlightedIndices = new int[0];
        this.sortComplete = false;
        draw();
    }

    public void setAlgorithm(SortingAlgorithm newAlgorithm) {
        if (algorithm != null) {
            algorithm.detach(observer);
        }
        this.algorithm = newAlgorithm;
        titleLabel.setText(newAlgorithm.getName());
        setupObserver();
    }

    private void draw() {
        if (data == null || data.length == 0) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, w, h);

        int n = data.length;
        double barWidth = w / n;
        int maxVal = Arrays.stream(data).max().orElse(1);

        Set<Integer> highlighted = new HashSet<>();
        for (int idx : highlightedIndices) highlighted.add(idx);

        for (int i = 0; i < n; i++) {
            double barHeight = (data[i] / (double) maxVal) * (h - 4);
            double x = i * barWidth;
            double y = h - barHeight;

            if (sortComplete) {
                gc.setFill(COMPLETE_COLOR);
            } else if (highlighted.contains(i)) {
                gc.setFill(HIGHLIGHT_COLOR);
            } else {
                gc.setFill(BAR_COLOR);
            }

            double gap = Math.max(1, barWidth * 0.12);
            gc.fillRect(x + gap / 2, y, Math.max(barWidth - gap, 1), barHeight);
        }
    }
}
