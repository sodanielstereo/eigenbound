package com.eigenbound.presentation.expedition;

import com.eigenbound.App;
import com.eigenbound.domain.expedition.ExpeditionMap;
import com.eigenbound.domain.expedition.analysis.ReachabilityMatrix;
import com.eigenbound.domain.expedition.analysis.WarshallReachabilityAnalyzer;
import com.eigenbound.domain.expedition.generation.ExpeditionMapGenerator;
import com.eigenbound.domain.expedition.generation.GeneratedExpedition;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controls the procedural expedition-map screen.
 *
 * <p>
 * The controller generates deterministic expedition graphs, sends them to
 * the canvas and uses Warshall's algorithm to display reachability data.
 * </p>
 */
public final class ExpeditionMapController {

    private static final long INITIAL_SEED = 20260730L;
    private static final int MIN_DIFFICULTY = 1;
    private static final int MAX_DIFFICULTY = 5;

    private final ExpeditionMapGenerator generator = new ExpeditionMapGenerator();

    private final WarshallReachabilityAnalyzer analyzer = new WarshallReachabilityAnalyzer();

    private long currentSeed = INITIAL_SEED;
    private int difficulty = MIN_DIFFICULTY;

    @FXML
    private ExpeditionMapCanvas expeditionCanvas;

    @FXML
    private Label seedLabel;

    @FXML
    private Label difficultyLabel;

    @FXML
    private Label roomCountLabel;

    @FXML
    private Label routeCountLabel;

    @FXML
    private Label reachableCountLabel;

    @FXML
    private Label bossLayerLabel;

    /**
     * Generates the first expedition after FXML injection.
     */
    @FXML
    private void initialize() {
        generateExpedition();
    }

    /**
     * Generates another expedition using the next seed.
     */
    @FXML
    private void onNewExpedition() {
        currentSeed++;
        generateExpedition();
    }

    /**
     * Increases expedition difficulty.
     */
    @FXML
    private void onIncreaseDifficulty() {
        if (difficulty < MAX_DIFFICULTY) {
            difficulty++;
            currentSeed++;
            generateExpedition();
        }
    }

    /**
     * Decreases expedition difficulty.
     */
    @FXML
    private void onDecreaseDifficulty() {
        if (difficulty > MIN_DIFFICULTY) {
            difficulty--;
            currentSeed++;
            generateExpedition();
        }
    }

    /**
     * Navigates to the interactive Vector Laboratory.
     *
     * @throws IOException when the laboratory FXML cannot be loaded
     */
    @FXML
    private void onEnterVectorLaboratory()
            throws IOException {

        App.setRoot("vector-laboratory");
    }

    /**
     * Generates, analyzes and renders the current expedition.
     */
    private void generateExpedition() {
        GeneratedExpedition generated = generator.generate(
                currentSeed,
                difficulty);

        ExpeditionMap map = generated.map();

        ReachabilityMatrix reachability = analyzer.analyze(map);

        expeditionCanvas.setExpeditionMap(map);

        seedLabel.setText(
                "Semilla: " + currentSeed);

        difficultyLabel.setText(
                "Dificultad: " + difficulty);

        roomCountLabel.setText(
                "Habitaciones: "
                        + map.nodes().size());

        routeCountLabel.setText(
                "Rutas: "
                        + map.edges().size());

        reachableCountLabel.setText(
                "Accesibles desde el inicio: "
                        + reachability
                                .reachableFrom(
                                        map.startNodeId())
                                .size());

        bossLayerLabel.setText(
                "Capas hasta el jefe: "
                        + map.findNode(
                                map.bossNodeId()).layer());
    }
}