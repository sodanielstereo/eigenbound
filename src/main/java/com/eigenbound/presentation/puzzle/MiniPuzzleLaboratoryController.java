package com.eigenbound.presentation.puzzle;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.eigenbound.App;
import com.eigenbound.application.effect.RoomEffect;
import com.eigenbound.application.event.ExpeditionRoomEvent;
import com.eigenbound.application.event.ExpeditionRoomEventFactory;
import com.eigenbound.application.event.ExpeditionRoomResolver;
import com.eigenbound.application.event.MiniPuzzleRoomEvent;
import com.eigenbound.application.session.ExpeditionRun;
import com.eigenbound.application.session.PuzzleSession;
import com.eigenbound.domain.expedition.RoomType;
import com.eigenbound.domain.puzzle.MiniPuzzle;
import com.eigenbound.domain.puzzle.PuzzleOption;
import com.eigenbound.domain.puzzle.PuzzleResult;
import com.eigenbound.domain.puzzle.PuzzleStatus;
import com.eigenbound.domain.puzzle.PuzzleTopic;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

/**
 * Controls the timed laboratory used to resolve expedition mini-puzzles.
 *
 * <p>
 * The controller translates JavaFX actions into {@link PuzzleSession}
 * transitions. Puzzle generation, validation and answer evaluation remain in
 * the application and domain layers.
 * </p>
 */
public final class MiniPuzzleLaboratoryController {

        private static final double WARNING_PROGRESS = 0.30;

        private final ExpeditionRoomEventFactory eventFactory = new ExpeditionRoomEventFactory();

        private final ExpeditionRoomResolver roomResolver = new ExpeditionRoomResolver();

        private final Map<String, Button> optionButtons = new LinkedHashMap<>();

        private final AnimationTimer countdownTimer = new AnimationTimer() {
                @Override
                public void handle(
                                long ignoredFrameTimeNanos) {
                        updateCountdown();
                }
        };

        private ExpeditionRun expeditionRun;
        private MiniPuzzleRoomEvent roomEvent;
        private PuzzleSession puzzleSession;
        private RoomEffect appliedRoomEffect;
        private long startedAtNanos;

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
        private Button cancelButton;

        @FXML
        private Button continueButton;

        /**
         * Loads the pending room event and starts its countdown after FXML
         * injection.
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
                startCountdown();
        }

        /**
         * Cancels an unresolved attempt and returns to the expedition map.
         *
         * @throws IOException when the expedition-map FXML cannot be loaded
         */
        @FXML
        private void onCancelAndReturn()
                        throws IOException {
                countdownTimer.stop();

                if (puzzleSession.isFinished()) {
                        throw new IllegalStateException(
                                        "Finished puzzle cannot be cancelled");
                }

                PuzzleResult cancelledResult = puzzleSession.cancel(
                                elapsedTime());

                roomResolver.resolveMiniPuzzle(
                                expeditionRun,
                                roomEvent,
                                cancelledResult);

                App.setRoot("expedition-map");
        }

        /**
         * Returns to the expedition map after the room has been resolved.
         *
         * @throws IOException when the expedition-map FXML cannot be loaded
         */
        @FXML
        private void onContinue()
                        throws IOException {
                if (appliedRoomEffect == null) {
                        throw new IllegalStateException(
                                        "Puzzle room must be resolved before continuing");
                }

                countdownTimer.stop();
                App.setRoot("expedition-map");
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
                                formatSeconds(puzzle.timeLimit()));
                timeProgressBar.setProgress(1.0);

                renderOptions(puzzle);

                explanationLabel.setText(
                                "La explicación aparecerá cuando termine el intento.");
                setStatus(
                                "Observa el mensaje y elige una respuesta.",
                                "status-neutral");

                continueButton.setVisible(false);
                continueButton.setManaged(false);
        }

        /**
         * Creates one interactive button for every immutable puzzle option.
         */
        private void renderOptions(
                        MiniPuzzle puzzle) {
                optionButtonContainer.getChildren().clear();
                optionButtons.clear();

                for (PuzzleOption option : puzzle.options()) {
                        Button optionButton = new Button(
                                        option.id() + "  ·  " + option.text());

                        optionButton.setMaxWidth(Double.MAX_VALUE);
                        optionButton.setUserData(option.id());
                        optionButton.getStyleClass().add(
                                        "puzzle-option-button");
                        optionButton.setOnAction(
                                        event -> submitOption(option.id()));

                        optionButtons.put(
                                        option.id(),
                                        optionButton);
                        optionButtonContainer.getChildren().add(optionButton);
                }
        }

        /**
         * Starts the monotonic timer used by the current attempt.
         */
        private void startCountdown() {
                startedAtNanos = System.nanoTime();
                countdownTimer.start();
        }

        /**
         * Evaluates one selected option using the elapsed attempt duration.
         */
        private void submitOption(
                        String optionId) {
                if (puzzleSession.isFinished()) {
                        return;
                }

                PuzzleResult result = puzzleSession.submit(
                                optionId,
                                elapsedTime());

                finishAttempt(result);
        }

        /**
         * Refreshes countdown controls and expires the attempt at its deadline.
         */
        private void updateCountdown() {
                if (puzzleSession == null
                                || puzzleSession.isFinished()) {
                        countdownTimer.stop();
                        return;
                }

                Duration timeLimit = puzzleSession
                                .puzzle()
                                .timeLimit();
                Duration elapsed = elapsedTime();
                Duration remaining = remainingTime(
                                timeLimit,
                                elapsed);

                double progress = remaining.toNanos()
                                / (double) timeLimit.toNanos();

                timeRemainingLabel.setText(
                                formatSeconds(remaining));
                timeProgressBar.setProgress(progress);
                updateTimerStyle(progress);

                if (elapsed.compareTo(timeLimit) >= 0) {
                        PuzzleResult result = puzzleSession.timeOut(elapsed);
                        finishAttempt(result);
                }
        }

        /**
         * Stops interaction and renders the terminal result.
         */
        private void finishAttempt(
                        PuzzleResult result) {
                countdownTimer.stop();

                appliedRoomEffect = roomResolver.resolveMiniPuzzle(
                                expeditionRun,
                                roomEvent,
                                result);

                disableOptions();
                highlightAnswers(result);

                explanationLabel.setText(result.explanation());

                switch (result.status()) {
                        case SOLVED -> setStatus(
                                        "¡Señal estabilizada! La respuesta es correcta.",
                                        "status-solved");

                        case INCORRECT -> setStatus(
                                        "Respuesta incorrecta. La señal se perdió.",
                                        "status-error");

                        case TIMED_OUT -> {
                                timeRemainingLabel.setText("0.0 s");
                                timeProgressBar.setProgress(0.0);
                                setStatus(
                                                "Se agotó el tiempo antes de recibir una respuesta válida.",
                                                "status-error");
                        }

                        case CANCELLED -> setStatus(
                                        "El intento fue cancelado.",
                                        "status-incomplete");
                }

                cancelButton.setVisible(false);
                cancelButton.setManaged(false);

                continueButton.setText(
                                result.status() == PuzzleStatus.SOLVED
                                                ? "CONTINUAR EXPEDICIÓN"
                                                : "CONTINUAR SIN BENEFICIO");
                continueButton.setVisible(true);
                continueButton.setManaged(true);
        }

        /**
         * Disables every option after the session reaches a terminal state.
         */
        private void disableOptions() {
                for (Button optionButton : optionButtons.values()) {
                        optionButton.setDisable(true);
                }
        }

        /**
         * Shows the correct answer and marks an incorrect selected answer.
         */
        private void highlightAnswers(
                        PuzzleResult result) {
                String correctOptionId = puzzleSession
                                .puzzle()
                                .correctOptionId();

                Button correctButton = optionButtons.get(correctOptionId);

                if (correctButton != null) {
                        correctButton.getStyleClass().add(
                                        "puzzle-option-correct");
                }

                if (result.status() != PuzzleStatus.INCORRECT) {
                        return;
                }

                result.selectedOptionId()
                                .map(optionButtons::get)
                                .ifPresent(
                                                button -> button.getStyleClass().add(
                                                                "puzzle-option-incorrect"));
        }

        /**
         * Returns elapsed time using Java's monotonic clock.
         */
        private Duration elapsedTime() {
                return elapsedTime(System.nanoTime());
        }

        /**
         * Converts a monotonic timestamp into elapsed attempt time.
         */
        private Duration elapsedTime(
                        long currentTimeNanos) {
                long elapsedNanos = Math.max(
                                0L,
                                currentTimeNanos - startedAtNanos);

                return Duration.ofNanos(elapsedNanos);
        }

        /**
         * Calculates a non-negative countdown duration.
         */
        private Duration remainingTime(
                        Duration timeLimit,
                        Duration elapsed) {
                if (elapsed.compareTo(timeLimit) >= 0) {
                        return Duration.ZERO;
                }

                return timeLimit.minus(elapsed);
        }

        /**
         * Formats countdown time with tenths of a second.
         */
        private String formatSeconds(
                        Duration duration) {
                double seconds = duration.toNanos()
                                / 1_000_000_000.0;

                return String.format(
                                Locale.ROOT,
                                "%.1f s",
                                seconds);
        }

        /**
         * Changes timer colors when less than thirty percent remains.
         */
        private void updateTimerStyle(
                        double progress) {
                boolean warning = progress <= WARNING_PROGRESS;

                setStyleClass(
                                timeRemainingLabel,
                                "puzzle-time-warning",
                                warning);
                setStyleClass(
                                timeProgressBar,
                                "puzzle-time-danger",
                                warning);
        }

        /**
         * Adds or removes one style class without creating duplicates.
         */
        private void setStyleClass(
                        Node node,
                        String styleClass,
                        boolean enabled) {
                if (enabled) {
                        if (!node.getStyleClass().contains(styleClass)) {
                                node.getStyleClass().add(styleClass);
                        }
                } else {
                        node.getStyleClass().remove(styleClass);
                }
        }

        /**
         * Updates the status message and its semantic style.
         */
        private void setStatus(
                        String message,
                        String styleClass) {
                statusLabel.setText(message);
                statusLabel.getStyleClass().removeAll(
                                "status-neutral",
                                "status-solved",
                                "status-incomplete",
                                "status-error");
                statusLabel.getStyleClass().add(styleClass);
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