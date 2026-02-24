package etardif.etsmtl.lab4;

import etardif.etsmtl.lab4.sort.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class HelloApplication extends Application {

    // Settings page controls (injected from settings-view.fxml)
    @FXML private ComboBox<String> algorithmCombo;
    @FXML private Spinner<Integer> sizeSpinner;
    @FXML private Slider speedSlider;
    @FXML private Label speedValueLabel;
    @FXML private TextArea manualInput;
    @FXML private CheckBox soundCheckBox;

    // Visualization page controls (injected from visualization-view.fxml)
    @FXML private Button backButton;
    @FXML private Button playPauseButton;
    @FXML private Slider volumeSlider;
    @FXML private HBox visualizerContainer;

    private StackPane root;
    private Parent settingsPage;
    private Parent visualizationPage;

    private final List<SortingAlgorithm> activeAlgorithms = new ArrayList<>();
    private ToneGenerator activeTone;
    private int[] originalArray;

    private final AtomicInteger sortGeneration = new AtomicInteger(0);
    private final AtomicInteger completedCount = new AtomicInteger(0);

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader settingsLoader = new FXMLLoader(getClass().getResource("settings-view.fxml"));
        settingsLoader.setController(this);
        settingsPage = settingsLoader.load();

        FXMLLoader vizLoader = new FXMLLoader(getClass().getResource("visualization-view.fxml"));
        vizLoader.setController(this);
        visualizationPage = vizLoader.load();

        algorithmCombo.setItems(FXCollections.observableArrayList(
                "Quick Sort", "Merge Sort", "Bucket Sort", "All"));
        algorithmCombo.setValue("All");

        sizeSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(5, Integer.MAX_VALUE, 50, 5));

        speedSlider.valueProperty().addListener((obs, old, val) ->
                speedValueLabel.setText(String.valueOf(val.intValue())));

        volumeSlider.valueProperty().addListener((obs, old, val) -> {
            if (activeTone != null) {
                activeTone.setVolume(val.doubleValue() / 100.0);
            }
        });

        root = new StackPane(settingsPage);

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setTitle("Sorting Visualizer");
        stage.setScene(scene);
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.show();
    }

    @FXML
    private void onStart() {
        if (!parseManualInput()) {
            generateArray(sizeSpinner.getValue());
        }

        String choice = algorithmCombo.getValue();
        visualizerContainer.getChildren().clear();
        activeAlgorithms.clear();

        if ("Quick Sort".equals(choice) || "All".equals(choice)) {
            activeAlgorithms.add(new QuickSort());
        }
        if ("Merge Sort".equals(choice) || "All".equals(choice)) {
            activeAlgorithms.add(new MergeSort());
        }
        if ("Bucket Sort".equals(choice) || "All".equals(choice)) {
            activeAlgorithms.add(new BucketSort());
        }

        long delay = Math.max(5, (long) (101 - speedSlider.getValue()));
        boolean soundEnabled = soundCheckBox.isSelected();
        activeTone = soundEnabled ? new ToneGenerator() : null;
        if (activeTone != null) {
            activeTone.setVolume(volumeSlider.getValue() / 100.0);
        }
        ToneGenerator sharedTone = activeTone;

        for (SortingAlgorithm algo : activeAlgorithms) {
            algo.setDelay(delay);
            SortVisualizerPane pane = new SortVisualizerPane(algo);
            pane.setToneGenerator(sharedTone);
            HBox.setHgrow(pane, Priority.ALWAYS);
            visualizerContainer.getChildren().add(pane);
            pane.setData(originalArray.clone());
        }

        root.getChildren().setAll(visualizationPage);
        backButton.setDisable(true);
        playPauseButton.setText("Pause");

        int gen = sortGeneration.incrementAndGet();
        completedCount.set(0);
        int expected = activeAlgorithms.size();

        for (SortingAlgorithm algo : activeAlgorithms) {
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
    }

    @FXML
    private void onPlayPause() {
        if (activeAlgorithms.isEmpty()) return;
        boolean anyPaused = activeAlgorithms.stream().anyMatch(SortingAlgorithm::isPaused);
        if (anyPaused) {
            activeAlgorithms.forEach(SortingAlgorithm::resume);
            playPauseButton.setText("Pause");
        } else {
            activeAlgorithms.forEach(SortingAlgorithm::pause);
            playPauseButton.setText("Play");
        }
    }

    @FXML
    private void onBack() {
        for (SortingAlgorithm algo : activeAlgorithms) {
            algo.cancel();
        }
        sortGeneration.incrementAndGet();
        if (activeTone != null) {
            activeTone.close();
            activeTone = null;
        }
        root.getChildren().setAll(settingsPage);
    }

    private boolean parseManualInput() {
        String text = manualInput.getText();
        if (text == null || text.trim().isEmpty()) return false;
        try {
            String[] parts = text.split("[,\\s]+");
            List<Integer> nums = new ArrayList<>();
            for (String p : parts) {
                String trimmed = p.trim();
                if (!trimmed.isEmpty()) nums.add(Integer.parseInt(trimmed));
            }
            if (nums.isEmpty()) return false;
            originalArray = nums.stream().mapToInt(Integer::intValue).toArray();
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void generateArray(int size) {
        Random rand = new Random();
        originalArray = new int[size];
        for (int i = 0; i < size; i++) {
            originalArray[i] = rand.nextInt(100) + 1;
        }
    }
}
