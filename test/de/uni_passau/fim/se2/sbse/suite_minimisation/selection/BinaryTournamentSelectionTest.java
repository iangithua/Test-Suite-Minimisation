package de.uni_passau.fim.se2.sbse.suite_minimisation.selection;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.Chromosome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BinaryTournamentSelectionTest {

    private Random random;
    private Comparator<DummyChromosome> fitnessComparator;
    private BinaryTournamentSelection<DummyChromosome> selection;

    @BeforeEach
    void setUp() {
        random = new Random(42); // Fixed seed for reproducibility
        // Comparator: higher fitness is better
        fitnessComparator = Comparator.comparingDouble(DummyChromosome::getFitness);
    }

    @Test
    @DisplayName("Should throw NullPointerException when comparator is null")
    void testConstructorThrowsExceptionForNullComparator() {
        assertThrows(NullPointerException.class, () ->
                new BinaryTournamentSelection<DummyChromosome>(null, random)
        );
    }

    @Test
    @DisplayName("Should throw NullPointerException when random is null")
    void testConstructorThrowsExceptionForNullRandom() {
        assertThrows(NullPointerException.class, () ->
                new BinaryTournamentSelection<>(fitnessComparator, null)
        );
    }

    @Test
    @DisplayName("Should create selection operator with valid parameters")
    void testConstructorWithValidParameters() {
        assertDoesNotThrow(() ->
                new BinaryTournamentSelection<>(fitnessComparator, random)
        );
    }

    @Test
    @DisplayName("Should throw NullPointerException when population is null")
    void testApplyThrowsExceptionForNullPopulation() {
        selection = new BinaryTournamentSelection<>(fitnessComparator, random);

        assertThrows(NullPointerException.class, () ->
                selection.apply(null)
        );
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when population is empty")
    void testApplyThrowsExceptionForEmptyPopulation() {
        selection = new BinaryTournamentSelection<>(fitnessComparator, random);
        List<DummyChromosome> emptyPopulation = new ArrayList<>();

        assertThrows(NoSuchElementException.class, () ->
                selection.apply(emptyPopulation)
        );
    }

    @Test
    @DisplayName("Should return the only individual when population size is 1")
    void testApplyWithSingleIndividual() {
        selection = new BinaryTournamentSelection<>(fitnessComparator, random);
        DummyChromosome only = new DummyChromosome(5.0);
        List<DummyChromosome> population = List.of(only);

        DummyChromosome selected = selection.apply(population);

        assertSame(only, selected);
    }

    @Test
    @DisplayName("Should select better individual from two individuals")
    void testApplySelectsBetterOfTwo() {
        selection = new BinaryTournamentSelection<>(fitnessComparator, random);
        DummyChromosome worse = new DummyChromosome(3.0);
        DummyChromosome better = new DummyChromosome(7.0);
        List<DummyChromosome> population = List.of(worse, better);

        DummyChromosome selected = selection.apply(population);

        // Should always select the better one
        assertEquals(better, selected);
    }

    @Test
    @DisplayName("Should select best individual from larger population")
    void testApplyWithLargerPopulation() {
        selection = new BinaryTournamentSelection<>(fitnessComparator, new Random(123));

        DummyChromosome best = new DummyChromosome(10.0);
        DummyChromosome good = new DummyChromosome(7.0);
        DummyChromosome medium = new DummyChromosome(5.0);
        DummyChromosome poor = new DummyChromosome(2.0);

        List<DummyChromosome> population = List.of(poor, medium, good, best);

        // Run multiple times and check we get reasonable selections
        Set<DummyChromosome> selectedIndividuals = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            DummyChromosome selected = selection.apply(population);
            selectedIndividuals.add(selected);
            assertNotNull(selected);
        }

        // Should have selected multiple different individuals
        assertTrue(selectedIndividuals.size() >= 2,
                "Should select different individuals across multiple tournaments");
    }

    @Test
    @DisplayName("Should handle equal fitness individuals")
    void testApplyWithEqualFitness() {
        selection = new BinaryTournamentSelection<>(fitnessComparator, random);

        DummyChromosome first = new DummyChromosome(5.0);
        DummyChromosome second = new DummyChromosome(5.0);

        List<DummyChromosome> population = List.of(first, second);

        DummyChromosome selected = selection.apply(population);

        // Should select one of them (doesn't matter which)
        assertTrue(selected == first || selected == second);
    }

    @Test
    @DisplayName("Should work with reverse comparator (minimization)")
    void testApplyWithReverseComparator() {
        // Reverse comparator: lower fitness is better (minimization)
        Comparator<DummyChromosome> minimizingComparator =
                Comparator.comparingDouble(DummyChromosome::getFitness).reversed();

        selection = new BinaryTournamentSelection<>(minimizingComparator, new Random(42));

        DummyChromosome high = new DummyChromosome(10.0);
        DummyChromosome low = new DummyChromosome(2.0);

        List<DummyChromosome> population = List.of(high, low);

        // With reversed comparator, should prefer lower fitness
        DummyChromosome selected = selection.apply(population);
        assertEquals(low, selected);
    }

    @RepeatedTest(20)
    @DisplayName("Should produce probabilistic selection behavior")
    void testProbabilisticBehavior() {
        selection = new BinaryTournamentSelection<>(fitnessComparator, new Random());

        DummyChromosome best = new DummyChromosome(10.0);
        DummyChromosome worst = new DummyChromosome(1.0);
        DummyChromosome middle1 = new DummyChromosome(5.0);
        DummyChromosome middle2 = new DummyChromosome(6.0);

        List<DummyChromosome> population = List.of(worst, middle1, middle2, best);

        DummyChromosome selected = selection.apply(population);

        // Selected individual should be from the population
        assertTrue(population.contains(selected));
    }

    @Test
    @DisplayName("Should handle population with all identical individuals")
    void testApplyWithIdenticalIndividuals() {
        selection = new BinaryTournamentSelection<>(fitnessComparator, random);

        DummyChromosome individual = new DummyChromosome(5.0);
        // Create multiple references to same fitness value
        List<DummyChromosome> population = List.of(
                new DummyChromosome(5.0),
                new DummyChromosome(5.0),
                new DummyChromosome(5.0)
        );

        DummyChromosome selected = selection.apply(population);

        assertNotNull(selected);
        assertEquals(5.0, selected.getFitness());
    }

    @Test
    @DisplayName("Should select from large population efficiently")
    void testApplyWithLargePopulation() {
        selection = new BinaryTournamentSelection<>(fitnessComparator, random);

        List<DummyChromosome> largePopulation = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            largePopulation.add(new DummyChromosome(i * 1.0));
        }

        long startTime = System.nanoTime();
        DummyChromosome selected = selection.apply(largePopulation);
        long endTime = System.nanoTime();

        assertNotNull(selected);
        assertTrue(selected.getFitness() >= 0.0 && selected.getFitness() < 1000.0);

        // Should complete quickly (under 1ms for selection)
        long durationMs = (endTime - startTime) / 1_000_000;
        assertTrue(durationMs < 10, "Selection should be fast even with large populations");
    }

    @Test
    @DisplayName("Should maintain selection pressure toward better individuals")
    void testSelectionPressure() {
        selection = new BinaryTournamentSelection<>(fitnessComparator, new Random(789));

        DummyChromosome excellent = new DummyChromosome(10.0);
        DummyChromosome good = new DummyChromosome(7.0);
        DummyChromosome poor = new DummyChromosome(3.0);
        DummyChromosome terrible = new DummyChromosome(1.0);

        List<DummyChromosome> population = List.of(terrible, poor, good, excellent);

        Map<DummyChromosome, Integer> selectionCounts = new HashMap<>();
        int trials = 100;

        for (int i = 0; i < trials; i++) {
            DummyChromosome selected = selection.apply(population);
            selectionCounts.merge(selected, 1, Integer::sum);
        }

        // Better individuals should be selected more often
        int excellentCount = selectionCounts.getOrDefault(excellent, 0);
        int terribleCount = selectionCounts.getOrDefault(terrible, 0);

        assertTrue(excellentCount > terribleCount,
                "Better individuals should be selected more frequently");
    }

    @Test
    @DisplayName("Should handle custom comparator logic")
    void testCustomComparator() {
        // Custom comparator based on multiple criteria
        Comparator<DummyChromosome> customComparator = (c1, c2) -> {
            // Prefer chromosomes with fitness values closer to 5.0
            double diff1 = Math.abs(c1.getFitness() - 5.0);
            double diff2 = Math.abs(c2.getFitness() - 5.0);
            return Double.compare(diff2, diff1); // Reverse: smaller diff is better
        };

        selection = new BinaryTournamentSelection<>(customComparator, random);

        DummyChromosome target = new DummyChromosome(5.0);   // Closest to 5.0
        DummyChromosome far1 = new DummyChromosome(1.0);
        DummyChromosome far2 = new DummyChromosome(10.0);

        List<DummyChromosome> population = List.of(far1, target, far2);

        // Run multiple times
        int targetSelected = 0;
        for (int i = 0; i < 50; i++) {
            DummyChromosome selected = selection.apply(population);
            if (selected.equals(target)) {
                targetSelected++;
            }
        }

        // Target should be selected more often due to custom comparator
        assertTrue(targetSelected > 20,
                "Custom comparator should favor chromosomes closer to 5.0");
    }

    @Test
    @DisplayName("Should allow same individual to be selected in tournament")
    void testCanSelectSameIndividualTwice() {
        // With single individual, it must compete against itself
        selection = new BinaryTournamentSelection<>(fitnessComparator, random);

        DummyChromosome only = new DummyChromosome(5.0);
        List<DummyChromosome> population = List.of(only);

        // Should not throw exception and should return the only individual
        DummyChromosome selected = selection.apply(population);
        assertSame(only, selected);
    }

    @Test
    @DisplayName("Should work correctly with unmodifiable list")
    void testApplyWithUnmodifiableList() {
        selection = new BinaryTournamentSelection<>(fitnessComparator, random);

        DummyChromosome c1 = new DummyChromosome(3.0);
        DummyChromosome c2 = new DummyChromosome(7.0);

        List<DummyChromosome> unmodifiablePopulation =
                Collections.unmodifiableList(Arrays.asList(c1, c2));

        assertDoesNotThrow(() -> {
            DummyChromosome selected = selection.apply(unmodifiablePopulation);
            assertNotNull(selected);
        });
    }

    // Helper class for testing

    static class DummyChromosome extends Chromosome<DummyChromosome> {

        private final double fitness;

        DummyChromosome(double fitness) {
//            super(List.of(), List.of());
            this.fitness = fitness;
        }

        public double getFitness() {
            return fitness;
        }

        @Override
        public DummyChromosome copy() {
            return new DummyChromosome(fitness);
        }

        @Override
        public DummyChromosome self() {
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof DummyChromosome other)) return false;
            return Double.compare(fitness, other.fitness) == 0;
        }

        @Override
        public int hashCode() {
            return Double.hashCode(fitness);
        }

        @Override
        public String toString() {
            return "DummyChromosome{fitness=" + fitness + "}";
        }
    }
}
