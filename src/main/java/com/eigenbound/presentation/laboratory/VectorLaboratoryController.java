package com.eigenbound.presentation.laboratory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.eigenbound.application.explanation.VectorAdditionStep;
import com.eigenbound.application.explanation.VectorExplanation;
import com.eigenbound.application.explanation.VectorExplanationService;
import com.eigenbound.application.hint.HintLevel;
import com.eigenbound.application.hint.VectorHint;
import com.eigenbound.application.hint.VectorHintService;
import com.eigenbound.application.session.ChallengeSession;
import com.eigenbound.domain.challenge.ChallengeResult;
import com.eigenbound.domain.challenge.ChallengeStatus;
import com.eigenbound.domain.challenge.VectorChallenge;
import com.eigenbound.domain.generation.GeneratedVectorChallenge;
import com.eigenbound.domain.generation.VectorChallengeGenerator;
import com.eigenbound.domain.math.Vector2;
import com.eigenbound.presentation.canvas.VectorCanvas;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Controls the interactive Vector Laboratory screen.
 *
 * <p>
 * This controller connects the JavaFX interface with the application and
 * domain layers. It manages challenge generation, player movement selection,
 * challenge validation and progressive hints.
 * </p>
 *
 * <p>
 * Mathematical operations and search algorithms are not implemented in
 * this controller. They remain delegated to {@link ChallengeSession},
 * {@link VectorChallengeGenerator} and {@link VectorHintService}.
 * </p>
 */
public final class VectorLaboratoryController {

    private static final long INITIAL_SEED = 20260725L;
    private static final int INITIAL_DIFFICULTY = 1;

    private static final HintLevel[] HINT_SEQUENCE = {
            HintLevel.CONCEPTUAL,
            HintLevel.DIRECTIONAL,
            HintLevel.NEXT_MOVE,
            HintLevel.FULL_SOLUTION
    };

    private final VectorExplanationService explanationService = new VectorExplanationService();

    private final VectorChallengeGenerator generator = new VectorChallengeGenerator();

    private final VectorHintService hintService = new VectorHintService();

    private final List<Button> movementButtons = new ArrayList<>();

    private ChallengeSession session;
    private long currentSeed = INITIAL_SEED;
    private int difficulty = INITIAL_DIFFICULTY;
    private int currentHintIndex;

    @FXML
    private VectorCanvas vectorCanvas;

    @FXML
    private VBox movementButtonContainer;

    @FXML
    private VBox explanationContainer;

    @FXML
    private Label seedLabel;

    @FXML
    private Label difficultyLabel;

    @FXML
    private Label targetLabel;

    @FXML
    private Label positionLabel;

    @FXML
    private Label stepsLabel;

    @FXML
    private Label selectedMovesLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label hintLabel;

    @FXML
    private Button undoButton;

    @FXML
    private Button hintButton;

    @FXML
    private Label explanationSummaryLabel;

    /**
     * Initializes the laboratory after all FXML components have been injected.
     */
    @FXML
    private void initialize() {
        loadChallenge();
    }

    /**
     * Removes the most recently selected movement.
     */
    @FXML
    private void onUndo() {
        session.undo();
        clearStatus();
        resetHints();
        clearExplanation();
        refreshView();
    }

    /**
     * Removes every selected movement and returns to the starting position.
     */
    @FXML
    private void onReset() {
        session.reset();
        clearStatus();
        resetHints();
        clearExplanation();
        refreshView();
    }

    /**
     * Evaluates the current sequence and displays both the result and its
     * step-by-step mathematical explanation.
     */
    @FXML
    private void onCheck() {
        ChallengeResult result = session.check();

        switch (result.status()) {
            case SOLVED -> {
                statusLabel.setText(
                        "¡Portal estabilizado! Alcanzaste el objetivo.");
                setStatusStyle("status-solved");
            }

            case INCOMPLETE -> {
                statusLabel.setText(
                        "Aún no alcanzas el objetivo. "
                                + "Observa la dirección resultante.");
                setStatusStyle("status-incomplete");
            }

            case INVALID_MOVE -> {
                statusLabel.setText(
                        "La secuencia contiene un movimiento inválido.");
                setStatusStyle("status-error");
            }

            case STEP_LIMIT_EXCEEDED -> {
                statusLabel.setText(
                        "Superaste el límite de movimientos.");
                setStatusStyle("status-error");
            }
        }

        showExplanation();
    }

    /**
     * Displays the next progressive hint for the current attempt.
     */
    @FXML
    private void onHint() {
        if (currentHintIndex >= HINT_SEQUENCE.length) {
            return;
        }

        HintLevel level = HINT_SEQUENCE[currentHintIndex];

        VectorHint hint = hintService.generateHint(
                session,
                level);

        hintLabel.setText(hint.message());

        clearMovementHighlights();

        if (level == HintLevel.NEXT_MOVE
                && !hint.movements().isEmpty()) {
            highlightMovement(
                    hint.movements().get(0));
        }

        currentHintIndex++;
        updateHintButton();
    }

    /**
     * Generates another challenge using the next deterministic seed.
     */
    @FXML
    private void onNewChallenge() {
        currentSeed++;
        loadChallenge();
    }

    /**
     * Increases the challenge difficulty when the maximum has not been reached.
     */
    @FXML
    private void onIncreaseDifficulty() {
        if (difficulty < 5) {
            difficulty++;
            currentSeed++;
            loadChallenge();
        }
    }

    /**
     * Decreases the challenge difficulty when the minimum has not been reached.
     */
    @FXML
    private void onDecreaseDifficulty() {
        if (difficulty > 1) {
            difficulty--;
            currentSeed++;
            loadChallenge();
        }
    }

    /**
     * Generates and loads the challenge associated with the current seed and
     * difficulty.
     */
    private void loadChallenge() {
        GeneratedVectorChallenge generated = generator.generate(
                currentSeed,
                difficulty);

        session = new ChallengeSession(
                generated.challenge());

        vectorCanvas.setChallenge(
                generated.challenge());

        createMovementButtons(
                generated.challenge());

        clearStatus();
        resetHints();
        clearExplanation();
        refreshView();
    }

    /**
     * Creates one interactive button for every movement available in the
     * challenge.
     *
     * @param challenge challenge whose movements must be displayed
     */
    private void createMovementButtons(
            VectorChallenge challenge) {
        movementButtonContainer
                .getChildren()
                .clear();

        movementButtons.clear();

        for (int index = 0; index < challenge.availableMoves().size(); index++) {

            Vector2 movement = challenge.availableMoves().get(index);

            Button button = new Button(
                    movementName(index)
                            + "  "
                            + formatVector(movement));

            button.getStyleClass()
                    .add("movement-button");

            button.setMaxWidth(Double.MAX_VALUE);

            button.setOnAction(
                    event -> selectMovement(movement));

            movementButtons.add(button);

            movementButtonContainer
                    .getChildren()
                    .add(button);
        }
    }

    /**
     * Adds a movement to the current attempt.
     *
     * @param movement movement selected by the player
     */
    private void selectMovement(Vector2 movement) {
        session.selectMove(movement);
        clearStatus();
        resetHints();
        clearExplanation();
        refreshView();
    }

    /**
     * Refreshes every interface component that depends on session state.
     */
    private void refreshView() {
        VectorChallenge challenge = session.challenge();

        seedLabel.setText(
                "Semilla: " + currentSeed);

        difficultyLabel.setText(
                "Dificultad: " + difficulty);

        targetLabel.setText(
                "Objetivo: "
                        + formatVector(challenge.target()));

        positionLabel.setText(
                "Posición: "
                        + formatVector(
                                session.currentPosition()));

        stepsLabel.setText(
                "Movimientos restantes: "
                        + session.remainingSteps());

        selectedMovesLabel.setText(
                formatSelectedMovements());

        undoButton.setDisable(!session.canUndo());

        boolean challengeSolved = session.check().status() == ChallengeStatus.SOLVED;

        boolean movementsDisabled = session.remainingSteps() == 0
                || challengeSolved;

        for (Button button : movementButtons) {
            button.setDisable(movementsDisabled);
        }

        vectorCanvas.setSelectedMoves(
                session.selectedMoves());
    }

    /**
     * Resets progressive hints whenever the current attempt changes.
     */
    private void resetHints() {
        currentHintIndex = 0;

        hintLabel.setText(
                "Las pistas aparecerán de forma progresiva.");

        clearMovementHighlights();
        updateHintButton();
    }

    /**
     * Updates the hint button according to the current progressive hint level.
     */
    private void updateHintButton() {
        if (currentHintIndex >= HINT_SEQUENCE.length) {
            hintButton.setText(
                    "PISTAS COMPLETADAS");
            hintButton.setDisable(true);
            return;
        }

        hintButton.setText(
                "PISTA "
                        + (currentHintIndex + 1)
                        + "/"
                        + HINT_SEQUENCE.length);

        hintButton.setDisable(false);
    }

    /**
     * Highlights the interface button associated with a suggested movement.
     *
     * @param suggestedMovement movement recommended by the hint service
     */
    private void highlightMovement(
            Vector2 suggestedMovement) {
        VectorChallenge challenge = session.challenge();

        for (int index = 0; index < movementButtons.size(); index++) {

            Vector2 movement = challenge.availableMoves().get(index);

            if (movement.equals(suggestedMovement)) {
                movementButtons.get(index)
                        .getStyleClass()
                        .add("suggested-movement");
            }
        }
    }

    /**
     * Removes hint highlighting from every movement button.
     */
    private void clearMovementHighlights() {
        for (Button button : movementButtons) {
            button.getStyleClass()
                    .remove("suggested-movement");
        }
    }

    /**
     * Formats the currently selected movement sequence.
     *
     * @return readable representation of the selected movements
     */
    private String formatSelectedMovements() {
        if (session.selectedMoves().isEmpty()) {
            return "Secuencia: ninguna";
        }

        StringBuilder builder = new StringBuilder("Secuencia: ");

        for (int index = 0; index < session.selectedMoves().size(); index++) {

            if (index > 0) {
                builder.append(" + ");
            }

            builder.append(
                    formatVector(
                            session.selectedMoves().get(index)));
        }

        return builder.toString();
    }

    /**
     * Generates an alphabetical name for a movement.
     *
     * @param index zero-based movement index
     * @return movement name
     */
    private String movementName(int index) {
        return Character.toString('A' + index);
    }

    /**
     * Formats a vector using mathematical coordinate notation.
     *
     * @param vector vector to format
     * @return formatted vector
     */
    private String formatVector(Vector2 vector) {
        return "("
                + formatNumber(vector.x())
                + ", "
                + formatNumber(vector.y())
                + ")";
    }

    /**
     * Formats a number without unnecessary decimal places.
     *
     * @param value numeric value
     * @return readable numeric representation
     */
    private String formatNumber(double value) {
        if (value == Math.rint(value)) {
            return Long.toString((long) value);
        }

        return String.format(
                Locale.ROOT,
                "%.2f",
                value);
    }

    /**
     * Restores the default challenge status message.
     */
    private void clearStatus() {
        statusLabel.setText(
                "Combina los vectores para alcanzar el portal.");
        setStatusStyle("status-neutral");
    }

    /**
     * Applies one status style while removing all other possible status styles.
     *
     * @param styleClass CSS class that must be applied
     */
    private void setStatusStyle(String styleClass) {
        statusLabel.getStyleClass().removeAll(
                "status-neutral",
                "status-solved",
                "status-incomplete",
                "status-error");

        statusLabel.getStyleClass()
                .add(styleClass);
    }

    /**
     * Builds and displays the mathematical explanation of the current attempt.
     */
    private void showExplanation() {
        VectorExplanation explanation = explanationService.explain(session);

        explanationContainer.getChildren().clear();

        if (explanation.steps().isEmpty()) {
            Label placeholder = new Label(
                    "Todavía no has seleccionado ningún vector.");

            placeholder.setWrapText(true);
            placeholder.getStyleClass()
                    .add("explanation-placeholder");

            explanationContainer
                    .getChildren()
                    .add(placeholder);
        } else {
            for (VectorAdditionStep step : explanation.steps()) {
                explanationContainer
                        .getChildren()
                        .add(createStepView(step));
            }
        }

        explanationSummaryLabel.setText(
                explanation.summary());

        explanationSummaryLabel.setVisible(true);
        explanationSummaryLabel.setManaged(true);

        setExplanationSummaryStyle(
                explanation.status());
    }

    /**
     * Creates the JavaFX representation of one vector addition.
     *
     * @param step vector addition to display
     * @return view containing its number and equation
     */
    private VBox createStepView(
            VectorAdditionStep step) {
        Label stepNumber = new Label(
                "Paso " + step.stepNumber());

        stepNumber.getStyleClass()
                .add("explanation-step-number");

        Label equation = new Label(
                step.equation());

        equation.setWrapText(true);
        equation.getStyleClass()
                .add("explanation-equation");

        VBox stepView = new VBox(
                3,
                stepNumber,
                equation);

        stepView.getStyleClass()
                .add("explanation-step");

        return stepView;
    }

    /**
     * Clears the previous explanation after the current attempt changes.
     */
    private void clearExplanation() {
        explanationContainer.getChildren().clear();

        Label placeholder = new Label(
                "Comprueba tu secuencia para ver la explicación.");

        placeholder.setWrapText(true);
        placeholder.getStyleClass()
                .add("explanation-placeholder");

        explanationContainer
                .getChildren()
                .add(placeholder);

        explanationSummaryLabel.setText("");
        explanationSummaryLabel.setVisible(false);
        explanationSummaryLabel.setManaged(false);

        explanationSummaryLabel
                .getStyleClass()
                .removeAll(
                        "explanation-summary-solved",
                        "explanation-summary-incomplete",
                        "explanation-summary-error");
    }

    /**
     * Applies a visual style to the explanation summary.
     *
     * @param status result of the explained attempt
     */
    private void setExplanationSummaryStyle(
            ChallengeStatus status) {
        explanationSummaryLabel
                .getStyleClass()
                .removeAll(
                        "explanation-summary-solved",
                        "explanation-summary-incomplete",
                        "explanation-summary-error");

        String styleClass = switch (status) {
            case SOLVED ->
                "explanation-summary-solved";

            case INCOMPLETE ->
                "explanation-summary-incomplete";

            case INVALID_MOVE, STEP_LIMIT_EXCEEDED ->
                "explanation-summary-error";
        };

        explanationSummaryLabel
                .getStyleClass()
                .add(styleClass);
    }
}