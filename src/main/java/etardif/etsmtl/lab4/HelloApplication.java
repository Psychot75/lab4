package etardif.etsmtl.lab4;

import etardif.etsmtl.lab4.sort.MergeSort;
import etardif.etsmtl.lab4.sort.QuickSort;
import etardif.etsmtl.lab4.sort.SortingAlgorithm;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class HelloApplication extends Application {

    private SortingAlgorithm quickSort = new QuickSort();
    private SortingAlgorithm mergeSort = new MergeSort();
    private SortVisualizerPane quickSortPane;
    private SortVisualizerPane mergeSortPane;
    private int[] originalArray;
    private int arraySize = 50;

    private Button startButton;
    private Button resetButton;
    private Slider speedSlider;

    private final AtomicInteger sortGeneration = new AtomicInteger(0);
    private final AtomicInteger completedCount = new AtomicInteger(0);

    @Override
    public void start(Stage stage) {
        generateArray();

        quickSortPane = new SortVisualizerPane(quickSort);
        mergeSortPane = new SortVisualizerPane(mergeSort);

        // Title
        Label title = new Label("Sorting Visualizer");
        title.getStyleClass().add("title");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);

        // Visualizer area
        HBox visualizers = new HBox(20, quickSortPane, mergeSortPane);
        visualizers.setAlignment(Pos.CENTER);
        HBox.setHgrow(quickSortPane, Priority.ALWAYS);
        HBox.setHgrow(mergeSortPane, Priority.ALWAYS);
        VBox.setVgrow(visualizers, Priority.ALWAYS);

        // Controls
        startButton = new Button("Start");
        startButton.getStyleClass().add("start-btn");
        startButton.setOnAction(e -> startSort());

        resetButton = new Button("Reset");
        resetButton.getStyleClass().add("reset-btn");
        resetButton.setOnAction(e -> resetSort());

        Label speedLabel = new Label("Speed");
        speedLabel.getStyleClass().add("control-label");

        speedSlider = new Slider(1, 100, 70);
        speedSlider.setPrefWidth(180);
        speedSlider.valueProperty().addListener((obs, old, val) -> updateDelay());

        HBox controls = new HBox(18, startButton, resetButton, speedLabel, speedSlider);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(12, 0, 0, 0));
        controls.getStyleClass().add("controls-bar");

        VBox root = new VBox(15, title, visualizers, controls);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("main-root");

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setTitle("Sorting Visualizer");
        stage.setScene(scene);
        stage.setMinWidth(700);
        stage.setMinHeight(450);
        stage.show();

        quickSortPane.setData(originalArray.clone());
        mergeSortPane.setData(originalArray.clone());
        updateDelay();
    }

    private void generateArray() {
        Random rand = new Random();
        originalArray = new int[arraySize];
        for (int i = 0; i < arraySize; i++) {
            originalArray[i] = rand.nextInt(100) + 1;
        }
    }

    private void updateDelay() {
        long delay = Math.max(5, (long) (101 - speedSlider.getValue()));
        quickSort.setDelay(delay);
        mergeSort.setDelay(delay);
    }

    private void startSort() {
        startButton.setDisable(true);
        int gen = sortGeneration.incrementAndGet();
        completedCount.set(0);
        updateDelay();

        int[] data1 = originalArray.clone();
        int[] data2 = originalArray.clone();

        Thread t1 = new Thread(() -> {
            try {
                quickSort.sort(data1);
            } catch (RuntimeException e) {
                // Sort was cancelled
            }
            onSortComplete(gen);
        });

        Thread t2 = new Thread(() -> {
            try {
                mergeSort.sort(data2);
            } catch (RuntimeException e) {
                // Sort was cancelled
            }
            onSortComplete(gen);
        });

        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();
    }

    private void onSortComplete(int generation) {
        if (generation != sortGeneration.get()) return;
        if (completedCount.incrementAndGet() >= 2) {
            Platform.runLater(() -> startButton.setDisable(false));
        }
    }

    private void resetSort() {
        quickSort.cancel();
        mergeSort.cancel();
        sortGeneration.incrementAndGet();

        generateArray();
        quickSort = new QuickSort();
        mergeSort = new MergeSort();
        updateDelay();

        quickSortPane.setAlgorithm(quickSort);
        mergeSortPane.setAlgorithm(mergeSort);
        quickSortPane.setData(originalArray.clone());
        mergeSortPane.setData(originalArray.clone());

        startButton.setDisable(false);
        completedCount.set(0);
    }
}
