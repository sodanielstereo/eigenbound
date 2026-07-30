package com.eigenbound.presentation.expedition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.eigenbound.domain.expedition.ExpeditionMap;
import com.eigenbound.domain.expedition.ExpeditionMapTestFixtures;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExpeditionMapLayoutCalculatorTest {

    private static final double EPSILON = 1e-9;

    private ExpeditionMapLayoutCalculator calculator;
    private ExpeditionMap map;

    @BeforeEach
    void setUp() {
        calculator = new ExpeditionMapLayoutCalculator();

        map = ExpeditionMapTestFixtures.validMap();
    }

    @Test
    void shouldCreatePositionForEveryNode() {
        Map<String, ExpeditionNodePosition> positions = calculator.calculate(
                map,
                800,
                600,
                50);

        assertEquals(
                map.nodes().size(),
                positions.size());
    }

    @Test
    void shouldPlaceStartAtLeftPadding() {
        Map<String, ExpeditionNodePosition> positions = calculator.calculate(
                map,
                800,
                600,
                50);

        assertEquals(
                50,
                positions.get("start").x(),
                EPSILON);
    }

    @Test
    void shouldPlaceBossAtRightPadding() {
        Map<String, ExpeditionNodePosition> positions = calculator.calculate(
                map,
                800,
                600,
                50);

        assertEquals(
                750,
                positions.get("boss").x(),
                EPSILON);
    }

    @Test
    void singleNodeLayerShouldBeVerticallyCentered() {
        Map<String, ExpeditionNodePosition> positions = calculator.calculate(
                map,
                800,
                600,
                50);

        assertEquals(
                300,
                positions.get("start").y(),
                EPSILON);

        assertEquals(
                300,
                positions.get("boss").y(),
                EPSILON);
    }

    @Test
    void allPositionsShouldRemainInsideBounds() {
        Map<String, ExpeditionNodePosition> positions = calculator.calculate(
                map,
                800,
                600,
                50);

        for (ExpeditionNodePosition position : positions.values()) {

            assertTrue(position.x() >= 50);
            assertTrue(position.x() <= 750);
            assertTrue(position.y() >= 50);
            assertTrue(position.y() <= 550);
        }
    }

    @Test
    void calculationShouldBeDeterministic() {
        Map<String, ExpeditionNodePosition> first = calculator.calculate(
                map,
                800,
                600,
                50);

        Map<String, ExpeditionNodePosition> second = calculator.calculate(
                map,
                800,
                600,
                50);

        assertEquals(first, second);
    }

    @Test
    void returnedMapShouldBeImmutable() {
        Map<String, ExpeditionNodePosition> positions = calculator.calculate(
                map,
                800,
                600,
                50);

        assertThrows(
                UnsupportedOperationException.class,
                positions::clear);
    }

    @Test
    void shouldRejectNullMap() {
        assertThrows(
                NullPointerException.class,
                () -> calculator.calculate(
                        null,
                        800,
                        600,
                        50));
    }

    @Test
    void shouldRejectInvalidDimensions() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        map,
                        0,
                        600,
                        50));
    }

    @Test
    void shouldRejectExcessivePadding() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        map,
                        800,
                        600,
                        400));
    }
}