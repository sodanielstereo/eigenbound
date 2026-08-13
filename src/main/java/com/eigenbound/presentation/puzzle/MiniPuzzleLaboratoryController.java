package com.eigenbound.presentation.puzzle;

import java.io.IOException;

import com.eigenbound.App;
import com.eigenbound.application.event.ExpeditionRoomEvent;
import com.eigenbound.application.event.ExpeditionRoomEventFactory;
import com.eigenbound.application.event.MiniPuzzleRoomEvent;
import com.eigenbound.application.session.ExpeditionRun;
import com.eigenbound.application.session.PuzzleSession;
import com.eigenbound.domain.expedition.RoomType;
import com.eigenbound.domain.puzzle.MiniPuzzle;
import com.eigenbound.domain.puzzle.PuzzleOption;
import com.eigenbound.domain.puzzle.PuzzleTopic;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

/**
 * Controls the laboratory used to display expedition mini-puzzles.
 *
 * <p>
 * This first presentation layer loads deterministic content from the room
 * event factory and renders it without duplicating puzzle rules. Answer
 * evaluation remains owned by {@link PuzzleSession} and will be connected to
 * the controls in the next implementation step.
 * </p>
 */
public final class MiniPuzzleLaboratoryController {

    private final ExpeditionRoomEventFactory eventFactory = new ExpeditionRoomEventFactory();

    private ExpeditionRun expeditionRun;
    private MiniPuzzleRoomEvent roomEvent;
    private PuzzleSession puzzleSession;

    @FXML
    private Label topicLabel;

    @FXML
    private Label promptLabel;

    @FXML
    private VBox optionButtonContainer;

    @FXML
    private Label roomTypeLabel;

    @FXML
    private Label roomIdLabel;

    @FXML
    private Label seedLabel;

    @FXML
    private Label difficultyLabel;

    @FXML
    private Label timeLimitLabel;

    @FXML
    private Label timeRemainingLabel;

    @FXML
    private ProgressBar timeProgressBar;

    @FXML
    private Label explanationLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button continueButton;

    /**
     * Loads the pending room event after every FXML control has been injected.
     */
    @FXML
    private void initialize() {
        expeditionRun = App.gameContext()
                .requireActiveExpeditionRun();

        ExpeditionRoomEvent generatedEvent = eventFactory.create(
                expeditionRun);

        if (!(generatedEvent instanceof MiniPuzzleRoomEvent miniPuzzleEvent)) {
            throw new IllegalStateException(
                    "Pending room does not contain a mini-puzzle event");
        }

        roomEvent = miniPuzzleEvent;
        puzzleSession = new PuzzleSession(
                roomEvent.generatedPuzzle().puzzle());

        renderPuzzle();
    }

    /**
     * Cancels the pending room and returns to the expedition map.
     *
     * @throws IOException when the expedition-map FXML cannot be loaded
     */
    @FXML
    private void onCancelAndReturn()
            throws IOException {
        expeditionRun.cancelPendingRoom();
        App.setRoot("expedition-map");
    }

    /**
     * Reserved for the completed-result navigation added in the next commit.
     */
    @FXML
    private void onContinue() {
        throw new IllegalStateException(
                "Puzzle must be resolved before continuing");
    }

    /**
     * Renders immutable puzzle and expedition metadata.
     */
    private void renderPuzzle() {
        MiniPuzzle puzzle = puzzleSession.puzzle();

        topicLabel.setText(topicName(puzzle.topic()));
        promptLabel.setText(puzzle.prompt());

        roomTypeLabel.setText(
                "Tipo: " + roomName(roomEvent.room().type()));
        roomIdLabel.setText(
                "Sala: " + roomEvent.room().id());
        seedLabel.setText(
                "Semilla: " + roomEvent.seed());
        difficultyLabel.setText(
                "Dificultad: " + roomEvent.difficulty());

        long timeLimitSeconds = puzzle.timeLimit().toSeconds();

        timeLimitLabel.setText(
                "Límite: " + timeLimitSeconds + " s");
        timeRemainingLabel.setText(
                timeLimitSeconds + " s");
        timeProgressBar.setProgress(1.0);

        renderOptions(puzzle);

        explanationLabel.setText(
                "La explicación aparecerá cuando termine el intento.");
        statusLabel.setText(
                "Observa el mensaje y elige una respuesta.");

        continueButton.setVisible(false);
        continueButton.setManaged(false);
    }

    /**
     * Creates one presentation button for every immutable puzzle option.
     */
    private void renderOptions(
            MiniPuzzle puzzle) {
        optionButtonContainer.getChildren().clear();

        for (PuzzleOption option : puzzle.options()) {
            Button optionButton = new Button(
                    option.id() + "  ·  " + option.text());

            optionButton.setMaxWidth(Double.MAX_VALUE);
            optionButton.setUserData(option.id());
            optionButton.getStyleClass().add(
                    "puzzle-option-button");

            optionButtonContainer.getChildren().add(optionButton);
        }
    }

    /**
     * Returns the user-facing room name.
     */
    private String roomName(
            RoomType type) {
        return switch (type) {
            case REST -> "Descanso";
            case REWARD -> "Recompensa";
            case START -> "Inicio";
            case VECTOR_CHALLENGE -> "Desafío vectorial";
            case ELITE_CHALLENGE -> "Desafío élite";
            case BOSS -> "Jefe";
        };
    }

    /**
     * Returns the user-facing academic topic name.
     */
    private String topicName(
            PuzzleTopic topic) {
        return switch (topic) {
            case LINEAR_ALGEBRA -> "ÁLGEBRA LINEAL";
            case CRYPTOGRAPHY -> "CRIPTOGRAFÍA";
            case PROBABILITY -> "PROBABILIDAD";
            case STATISTICS -> "ESTADÍSTICA";
            case GRAPH_THEORY -> "TEORÍA DE GRAFOS";
            case AUTOMATA_THEORY -> "TEORÍA DE AUTÓMATAS";
        };
    }
}