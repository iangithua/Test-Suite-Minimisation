package de.uni_passau.fim.se2.sbse.suite_minimisation.crossover;

import static org.junit.jupiter.api.Assertions.*;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.BinaryChromosom;
import de.uni_passau.fim.se2.sbse.suite_minimisation.mutation.BinaryMutation;
import de.uni_passau.fim.se2.sbse.suite_minimisation.utils.Pair;
import org.junit.jupiter.api.Test;

public class BinaryCrossoverTest {

    // Test that crossover produces offspring of correct length
    @Test
    void testOffspringLength() {
        boolean[] genes1 = { true, true, false, false };
        boolean[] genes2 = { false, false, true, true };

        BinaryChromosom parent1 =
                new BinaryChromosom(genes1, new BinaryMutation(0.1), new BinaryCrossover());
        BinaryChromosom parent2 =
                new BinaryChromosom(genes2, new BinaryMutation(0.1), new BinaryCrossover());

        BinaryCrossover crossover = new BinaryCrossover();

        Pair<BinaryChromosom> offspring =
                crossover.apply(parent1, parent2);

        assertEquals(genes1.length, offspring.getFst().getGenes().length);
        assertEquals(genes2.length, offspring.getSnd().getGenes().length);
    }

    // Test that offspring genes come from parents
    @Test
    void testGenesAreInheritedFromParents() {
        boolean[] genes1 = { true, true, true, true };
        boolean[] genes2 = { false, false, false, false };

        BinaryChromosom parent1 =
                new BinaryChromosom(genes1, new BinaryMutation(0.1), new BinaryCrossover());
        BinaryChromosom parent2 =
                new BinaryChromosom(genes2, new BinaryMutation(0.1), new BinaryCrossover());

        BinaryCrossover crossover = new BinaryCrossover();

        Pair<BinaryChromosom> offspring =
                crossover.apply(parent1, parent2);

        boolean[] childGenes = offspring.getFst().getGenes();

        // Each gene must match one of the parents
        for (boolean gene : childGenes) {
            assertTrue(gene == true || gene == false);
        }
    }
}

