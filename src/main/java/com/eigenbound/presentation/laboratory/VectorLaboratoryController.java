package com.eigenbound.presentation.laboratory;

import java.util.ArrayList;
import java.util.List;

import com.eigenbound.application.session.ChallengeSession;
import com.eigenbound.domain.challenge.ChallengeResult;
import com.eigenbound.domain.challenge.VectorChallenge;
import com.eigenbound.domain.generation.GeneratedVectorChallenge;
import com.eigenbound.domain.generation.VectorChallengeGenerator;
import com.eigenbound.domain.math.Vector2;
import com.eigenbound.presentation.canvas.VectorCanvas;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public final class VectorLaboratoryController {

    private static final long INITIAL_SEED = 20260725L;
    private static final int INITIAL_DIFFICULTY = 1;

    private final VectorChallengeGenerator generator = new VectorChallengeGenerator();

    private final List<Button> movementButtons = new ArrayList<>();

    private ChallengeSession session;
    private long currentSeed = INITIAL_SEED;
    private int difficulty = INITIAL_DIFFICULTY;

    @FXML
    private VectorCanvas vectorCanvas;

    @FXML
    private VBox movementButtonContainer;

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
    private Button undoButton;

    @FXML
    private void initialize() {
        loadChallenge();
    }

    @FXML
    private void onUndo() {
        session.undo();
        clearStatus();
        refreshView();
    }

    @FXML
    private void onReset() {
        session.reset();
        clearStatus();
        refreshView();
    }

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
    }

    @FXML
    private void onNewChallenge() {
        currentSeed++;
        loadChallenge();
    }

    @FXML
    private void onIncreaseDifficulty() {
        if (difficulty < 5) {
            difficulty++;
            currentSeed++;
            loadChallenge();
        }
    }

    @FXML
    private void onDecreaseDifficulty() {
        if (difficulty > 1) {
            difficulty--;
            currentSeed++;
            loadChallenge();
        }
    }

    private void loadChallenge() {
        GeneratedVectorChallenge generated = generator.generate(currentSeed, difficulty);

        session = new ChallengeSession(
                generated.challenge());

        vectorCanvas.setChallenge(
                generated.challenge());

        createMovementButtons(
                generated.challenge());

        clearStatus();
        refreshView();
    }

    private void createMovementButtons(
            VectorChallenge challenge) {
        movementButtonContainer.getChildren().clear();
        movementButtons.clear();

        for (int index = 0; index < challenge.availableMoves().size(); index++) {

            Vector2 movement = challenge.availableMoves().get(index);

            Button button = new Button(
                    movementName(index)
                            + "  "
                            + formatVector(movement));

            button.getStyleClass().add("movement-button");
            button.setMaxWidth(Double.MAX_VALUE);

            button.setOnAction(
                    event -> selectMovement(movement));

            movementButtons.add(button);
            movementButtonContainer
                    .getChildren()
                    .add(button);
        }
    }

    private void selectMovement(Vector2 movement) {
        session.selectMove(movement);
        clearStatus();
        refreshView();
    }

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
                        + formatVector(session.currentPosition()));

        stepsLabel.setText(
                "Movimientos restantes: "
                        + session.remainingSteps());

        selectedMovesLabel.setText(
                formatSelectedMovements());

        undoButton.setDisable(!session.canUndo());

        boolean stepLimitReached = session.remainingSteps() == 0;

        for (Button button : movementButtons) {
            button.setDisable(stepLimitReached);
        }

        vectorCanvas.setSelectedMoves(
                session.selectedMoves());
    }

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

    private String movementName(int index) {
        return Character.toString('A' + index);
    }

    private String formatVector(Vector2 vector) {
        return "("
                + formatNumber(vector.x())
                + ", "
                + formatNumber(vector.y())
                + ")";
    }

    private String formatNumber(double value) {
        if (value == Math.rint(value)) {
            return Long.toString((long) value);
        }

        return String.format("%.2f", value);
    }

    private void clearStatus() {
        statusLabel.setText(
                "Combina los vectores para alcanzar el portal.");
        setStatusStyle("status-neutral");
    }

    private void setStatusStyle(String styleClass) {
        statusLabel.getStyleClass().removeAll(
                "status-neutral",
                "status-solved",
                "status-incomplete",
                "status-error");

        statusLabel.getStyleClass().add(styleClass);
    }
}