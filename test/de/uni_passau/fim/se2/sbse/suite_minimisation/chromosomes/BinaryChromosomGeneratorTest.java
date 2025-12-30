package de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BinaryChromosomGeneratorTest {

    // Test that generated chromosome has correct gene length
    @Test
    void testGeneLengthIsCorrect() {
        int geneLength = 10;
        BinaryChromosomGenerator generator =
                new BinaryChromosomGenerator(geneLength);

        BinaryChromosom chromosome = generator.get();

        assertEquals(geneLength, chromosome.getGenes().length);
    }

    // Test that at least one gene is always true
    @Test
    void testAtLeastOneGeneIsSelected() {
        BinaryChromosomGenerator generator =
                new BinaryChromosomGenerator(20);

        for (int i = 0; i < 100; i++) {
            BinaryChromosom chromosome = generator.get();
            boolean[] genes = chromosome.getGenes();

            boolean hasTrue = false;
            for (boolean gene : genes) {
                if (gene) {
                    hasTrue = true;
                    break;
                }
            }

            assertTrue(hasTrue, "Chromosome must select at least one gene");
        }
    }

    // Test that mutation and crossover operators are assigned
    @Test
    void testOperatorsAreNotNull() {
        BinaryChromosomGenerator generator =
                new BinaryChromosomGenerator(5);

        BinaryChromosom chromosome = generator.get();

        assertNotNull(chromosome.getMutation());
        assertNotNull(chromosome.getCrossover());
    }
}

