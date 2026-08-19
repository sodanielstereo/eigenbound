package com.eigenbound.presentation.expedition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.eigenbound.application.session.ExpeditionRunState;
import com.eigenbound.application.session.ExpeditionSummary;
import com.eigenbound.domain.expedition.ExpeditionResources;

class ExpeditionOutcomeViewModelTest {

        @Test
        void shouldFormatVictoryOutcome() {
                ExpeditionOutcomeViewModel viewModel = ExpeditionOutcomeViewModel.from(
                                new ExpeditionSummary(
                                                ExpeditionRunState.VICTORY,
                                                73L,
                                                3,
                                                new ExpeditionResources(
                                                                65,
                                                                100,
                                                                40L),
                                                5));

                assertEquals("VICTORIA", viewModel.title());
                assertEquals(
                                "El núcleo fue estabilizado. La expedición terminó con éxito.",
                                viewModel.message());
                assertEquals("Semilla: 73", viewModel.seedText());
                assertEquals("Dificultad: 3", viewModel.difficultyText());
                assertEquals(
                                "Estabilidad final: 65 / 100",
                                viewModel.stabilityText());
                assertEquals("Insight final: 40", viewModel.insightText());
                assertEquals(
                                "Habitaciones visitadas: 5",
                                viewModel.visitedRoomsText());
                assertEquals("outcome-victory", viewModel.styleClass());
        }

        @Test
        void shouldFormatDefeatOutcome() {
                ExpeditionOutcomeViewModel viewModel = ExpeditionOutcomeViewModel.from(
                                new ExpeditionSummary(
                                                ExpeditionRunState.DEFEAT,
                                                91L,
                                                4,
                                                new ExpeditionResources(
                                                                0,
                                                                100,
                                                                12L),
                                                3));

                assertEquals("DERROTA", viewModel.title());
                assertEquals(
                                "La expedición colapsó al quedarse sin Estabilidad.",
                                viewModel.message());
                assertEquals(
                                "Estabilidad final: 0 / 100",
                                viewModel.stabilityText());
                assertEquals("Insight final: 12", viewModel.insightText());
                assertEquals("outcome-defeat", viewModel.styleClass());
        }

        @Test
        void shouldRejectNullSummary() {
                assertThrows(
                                NullPointerException.class,
                                () -> ExpeditionOutcomeViewModel.from(null));
        }

        @Test
        void shouldRejectBlankPresentationValues() {
                assertThrows(
                                IllegalArgumentException.class,
                                () -> new ExpeditionOutcomeViewModel(
                                                "",
                                                "Message",
                                                "Seed",
                                                "Difficulty",
                                                "Stability",
                                                "Insight",
                                                "Rooms",
                                                "outcome-victory"));
        }
}