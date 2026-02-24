package etardif.etsmtl.lab4;

import etardif.etsmtl.lab4.sort.MergeSort;
import etardif.etsmtl.lab4.sort.QuickSort;
import etardif.etsmtl.lab4.sort.SortingAlgorithm;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class HelloApplication extends Application {

    private StackPane root;
    private VBox settingsPage;
    private VBox visualizationPage;

    private ComboBox<String> algorithmCombo;
    private Spinner<Integer> sizeSpinner;
    private Slider speedSlider;

    private HBox visualizerContainer;
    private SortingAlgorithm quickSort;
    private SortingAlgorithm mergeSort;
    private SortVisualizerPane quickSortPane;
    private SortVisualizerPane mergeSortPane;
    private int[] originalArray;

    private final AtomicInteger sortGeneration = new AtomicInteger(0);
    private final AtomicInteger completedCount = new AtomicInteger(0);
    private Button backButton;

    @Override
    public void start(Stage stage) {
        buildSettingsPage();
        buildVisualizationPage();

        root = new StackPane(settingsPage);

        Scene scene = new Scene(root, 900, 550);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setTitle("Sorting Visualizer");
        stage.setScene(scene);
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.show();
    }

    private void buildSettingsPage() {
        Label title = new Label("Sorting Visualizer");
        title.getStyleClass().add("page-title");

        Label algoLabel = new Label("Algorithm");
        algoLabel.getStyleClass().add("field-label");
        algorithmCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Quick Sort", "Merge Sort", "Both"));
        algorithmCombo.setValue("Both");
        algorithmCombo.setMaxWidth(Double.MAX_VALUE);

        Label sizeLabel = new Label("Array Size");
        sizeLabel.getStyleClass().add("field-label");
        sizeSpinner = new Spinner<>(5, 200, 50, 5);
        sizeSpinner.setEditable(true);
        sizeSpinner.setMaxWidth(Double.MAX_VALUE);

        Label speedLabel = new Label("Speed");
        speedLabel.getStyleClass().add("field-label");
        speedSlider = new Slider(1, 100, 70);
        Label speedValue = new Label("70");
        speedValue.getStyleClass().add("speed-value");
        speedSlider.valueProperty().addListener((obs, old, val) ->
                speedValue.setText(String.valueOf(val.intValue())));
        HBox speedRow = new HBox(10, speedSlider, speedValue);
        speedRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(speedSlider, Priority.ALWAYS);

        Button startButton = new Button("Start Sorting");
        startButton.getStyleClass().add("primary-btn");
        startButton.setMaxWidth(Double.MAX_VALUE);
        startButton.setOnAction(e -> goToVisualization());

        VBox form = new VBox(14,
                algoLabel, algorithmCombo,
                sizeLabel, sizeSpinner,
                speedLabel, speedRow,
                startButton);
        form.setMaxWidth(320);

        settingsPage = new VBox(30, title, form);
        settingsPage.setAlignment(Pos.CENTER);
        settingsPage.setPadding(new Insets(40));
        settingsPage.getStyleClass().add("page");
    }

    private void buildVisualizationPage() {
        backButton = new Button("Back to Settings");
        backButton.getStyleClass().add("back-btn");
        backButton.setOnAction(e -> goToSettings());

        HBox topBar = new HBox(backButton);
        topBar.setAlignment(Pos.CENTER_LEFT);

        visualizerContainer = new HBox(16);
        visualizerContainer.setAlignment(Pos.CENTER);
        VBox.setVgrow(visualizerContainer, Priority.ALWAYS);

        visualizationPage = new VBox(12, topBar, visualizerContainer);
        visualizationPage.setPadding(new Insets(20));
        visualizationPage.getStyleClass().add("page");
    }

    private void goToVisualization() {
        int size = sizeSpinner.getValue();
        generateArray(size);

        String choice = algorithmCombo.getValue();
        visualizerContainer.getChildren().clear();

        quickSort = new QuickSort();
        mergeSort = new MergeSort();
        long delay = Math.max(5, (long) (101 - speedSlider.getValue()));
        quickSort.setDelay(delay);
        mergeSort.setDelay(delay);

        int expectedCompletions;

        if ("Quick Sort".equals(choice)) {
            quickSortPane = new SortVisualizerPane(quickSort);
            HBox.setHgrow(quickSortPane, Priority.ALWAYS);
            visualizerContainer.getChildren().add(quickSortPane);
            quickSortPane.setData(originalArray.clone());
            expectedCompletions = 1;
        } else if ("Merge Sort".equals(choice)) {
            mergeSortPane = new SortVisualizerPane(mergeSort);
            HBox.setHgrow(mergeSortPane, Priority.ALWAYS);
            visualizerContainer.getChildren().add(mergeSortPane);
            mergeSortPane.setData(originalArray.clone());
            expectedCompletions = 1;
        } else {
            quickSortPane = new SortVisualizerPane(quickSort);
            mergeSortPane = new SortVisualizerPane(mergeSort);
            HBox.setHgrow(quickSortPane, Priority.ALWAYS);
            HBox.setHgrow(mergeSortPane, Priority.ALWAYS);
            visualizerContainer.getChildren().addAll(quickSortPane, mergeSortPane);
            quickSortPane.setData(originalArray.clone());
            mergeSortPane.setData(originalArray.clone());
            expectedCompletions = 2;
        }

        root.getChildren().setAll(visualizationPage);
        backButton.setDisable(true);

        int gen = sortGeneration.incrementAndGet();
        completedCount.set(0);

        if ("Quick Sort".equals(choice) || "Both".equals(choice)) {
            startSortThread(quickSort, gen, expectedCompletions);
        }
        if ("Merge Sort".equals(choice) || "Both".equals(choice)) {
            startSortThread(mergeSort, gen, expectedCompletions);
        }
    }

    private void startSortThread(SortingAlgorithm algo, int gen, int expected) {
        int[] data = originalArray.clone();
        Thread t = new Thread(() -> {
            try {
                algo.sort(data);
            } catch (RuntimeException e) {
                // cancelled
            }
            if (gen == sortGeneration.get() && completedCount.incrementAndGet() >= expected) {
                Platform.runLater(() -> backButton.setDisable(false));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void goToSettings() {
        if (quickSort != null) quickSort.cancel();
        if (mergeSort != null) mergeSort.cancel();
        sortGeneration.incrementAndGet();
        root.getChildren().setAll(settingsPage);
    }

    private void generateArray(int size) {
        Random rand = new Random();
        originalArray = new int[size];
        for (int i = 0; i < size; i++) {
            originalArray[i] = rand.nextInt(100) + 1;
        }
    }
}
