package com.eigenbound.presentation.expedition;

import java.io.IOException;

import com.eigenbound.App;
import com.eigenbound.application.session.ExpeditionSession;
import com.eigenbound.domain.expedition.ExpeditionMap;
import com.eigenbound.domain.expedition.ExpeditionNode;
import com.eigenbound.domain.expedition.RoomType;
import com.eigenbound.domain.expedition.analysis.ReachabilityMatrix;
import com.eigenbound.domain.expedition.analysis.WarshallReachabilityAnalyzer;
import com.eigenbound.domain.expedition.generation.ExpeditionMapGenerator;
import com.eigenbound.domain.expedition.generation.GeneratedExpedition;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controls the procedural and interactive expedition-map screen.
 *
 * <p>
 * The controller generates deterministic expedition graphs, creates the
 * session that tracks player progress, handles room selections and updates
 * both the canvas and the information panel.
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

        private ExpeditionSession expeditionSession;

        @FXML
        private ExpeditionMapCanvas expeditionCanvas;

        @FXML
        private Label seedLabel;

        @FXML
        private Label difficultyLabel;

        @FXML
        private Label currentRoomLabel;

        @FXML
        private Label visitedRoomsLabel;

        @FXML
        private Label availableRoomsLabel;

        @FXML
        private Label expeditionStatusLabel;

        @FXML
        private Label roomCountLabel;

        @FXML
        private Label routeCountLabel;

        @FXML
        private Label reachableCountLabel;

        @FXML
        private Label bossLayerLabel;

        /**
         * Configures canvas interaction and generates the first expedition after
         * FXML injection.
         */
        @FXML
        private void initialize() {
                expeditionCanvas.setOnNodeSelected(
                                this::handleNodeSelection);

                generateExpedition();
        }

        /**
         * Generates another expedition using the next seed.
         */
        @FXML
        private void onNewExpedition() {
                advanceSeed();
                generateExpedition();
        }

        /**
         * Increases expedition difficulty and generates a new map.
         */
        @FXML
        private void onIncreaseDifficulty() {
                if (difficulty < MAX_DIFFICULTY) {
                        difficulty++;
                        advanceSeed();
                        generateExpedition();
                }
        }

        /**
         * Decreases expedition difficulty and generates a new map.
         */
        @FXML
        private void onDecreaseDifficulty() {
                if (difficulty > MIN_DIFFICULTY) {
                        difficulty--;
                        advanceSeed();
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
         * Handles a node selected through the expedition canvas.
         *
         * @param nodeId identifier of the selected room
         */
        private void handleNodeSelection(
                        String nodeId) {
                if (expeditionSession == null) {
                        return;
                }

                if (!expeditionSession.canMoveTo(nodeId)) {
                        setExpeditionStatus(
                                        "Esa habitación no está disponible desde tu posición actual.",
                                        "status-incomplete");

                        return;
                }

                expeditionSession.moveTo(nodeId);

                expeditionCanvas.redraw();
                updateProgressLabels();

                ExpeditionNode currentNode = expeditionSession.currentNode();

                if (expeditionSession.isCompleted()) {
                        setExpeditionStatus(
                                        "Llegaste al jefe. Expedición completada.",
                                        "status-solved");

                        return;
                }

                setExpeditionStatus(
                                "Entraste a "
                                                + roomName(currentNode.type())
                                                + ". Elige una ruta disponible para continuar.",
                                "status-neutral");
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

                expeditionSession = new ExpeditionSession(map);

                expeditionCanvas.setExpeditionSession(
                                expeditionSession);

                updateGenerationLabels(
                                map,
                                reachability);

                updateProgressLabels();

                setExpeditionStatus(
                                "Selecciona una habitación resaltada para avanzar.",
                                "status-neutral");
        }

        /**
         * Updates labels related to map generation and graph analysis.
         */
        private void updateGenerationLabels(
                        ExpeditionMap map,
                        ReachabilityMatrix reachability) {
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
                                                                map.bossNodeId())
                                                                .layer());
        }

        /**
         * Updates labels representing the current expedition progress.
         */
        private void updateProgressLabels() {
                ExpeditionNode currentNode = expeditionSession.currentNode();

                currentRoomLabel.setText(
                                "Habitación actual: "
                                                + roomName(currentNode.type())
                                                + " · "
                                                + currentNode.id());

                visitedRoomsLabel.setText(
                                "Habitaciones visitadas: "
                                                + expeditionSession
                                                                .visitedNodes()
                                                                .size());

                availableRoomsLabel.setText(
                                "Opciones disponibles: "
                                                + expeditionSession
                                                                .availableNodes()
                                                                .size());
        }

        /**
         * Updates the expedition message and its visual style.
         */
        private void setExpeditionStatus(
                        String message,
                        String styleClass) {
                expeditionStatusLabel.setText(message);

                expeditionStatusLabel
                                .getStyleClass()
                                .removeAll(
                                                "status-neutral",
                                                "status-solved",
                                                "status-incomplete",
                                                "status-error");

                expeditionStatusLabel
                                .getStyleClass()
                                .add(styleClass);
        }

        /**
         * Returns the user-facing name of a room type.
         */
        private String roomName(
                        RoomType type) {
                return switch (type) {
                        case START ->
                                "Inicio";

                        case VECTOR_CHALLENGE ->
                                "Desafío vectorial";

                        case ELITE_CHALLENGE ->
                                "Desafío élite";

                        case REST ->
                                "Descanso";

                        case REWARD ->
                                "Recompensa";

                        case BOSS ->
                                "Jefe";
                };
        }

        /**
         * Advances the deterministic seed without relying on silent long overflow.
         *
         * <p>
         * Every {@code long} value is valid as a random seed, so the sequence
         * explicitly continues at {@link Long#MIN_VALUE} after reaching
         * {@link Long#MAX_VALUE}.
         * </p>
         */
        private void advanceSeed() {
                currentSeed = currentSeed == Long.MAX_VALUE
                                ? Long.MIN_VALUE
                                : currentSeed + 1;
        }
}