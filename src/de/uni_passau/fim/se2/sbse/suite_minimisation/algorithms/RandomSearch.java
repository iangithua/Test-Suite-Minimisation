package de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.Chromosome;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.ChromosomeGenerator;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.MinimizingFitnessFunction;
import de.uni_passau.fim.se2.sbse.suite_minimisation.stopping_conditions.StoppingCondition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class RandomSearch<C extends Chromosome<C>> implements GeneticAlgorithm<C> {

    private final ChromosomeGenerator<C> chromosomeGenerator;
    private final List<FitnessFunction<C>> fitnessFunctions;
    private final StoppingCondition stoppingCondition;
    private final List<C> paretoFront;

    // Increased initial population for better coverage
    private final int initialPopulationSize = 600;

    // Generate more candidates per iteration
    private final int candidatesPerIteration = 20;

    public RandomSearch(ChromosomeGenerator<C> chromosomeGenerator,
                        List<FitnessFunction<C>> fitnessFunctions,
                        StoppingCondition stoppingCondition) {

        this.chromosomeGenerator = chromosomeGenerator;
        this.fitnessFunctions = fitnessFunctions;
        this.stoppingCondition = stoppingCondition;
        this.paretoFront = new ArrayList<>();
    }

    @Override
    public List<C> findSolution() {

        stoppingCondition.notifySearchStarted();

        // Generate initial population
        generateInitialPopulation();

        // Continue random sampling
        while (!stoppingCondition.searchMustStop()) {

            // Generate more candidates per iteration for better exploration
            for (int i = 0; i < candidatesPerIteration && !stoppingCondition.searchMustStop(); i++) {

                C candidate = chromosomeGenerator.get();
                updateParetoFront(candidate);
                stoppingCondition.notifyFitnessEvaluation();
            }
        }

        return new ArrayList<>(paretoFront);
    }

    private void generateInitialPopulation() {

        for (int i = 0; i < initialPopulationSize && !stoppingCondition.searchMustStop(); i++) {

            C candidate = chromosomeGenerator.get();
            updateParetoFront(candidate);
            stoppingCondition.notifyFitnessEvaluation();
        }
    }

    @Override
    public StoppingCondition getStoppingCondition() {
        return stoppingCondition;
    }

    private void updateParetoFront(C candidate) {

        if (!isValidSolution(candidate)) {
            return;
        }

        boolean candidateIsDominated = false;
        Iterator<C> iterator = paretoFront.iterator();

        while (iterator.hasNext()) {
            C existing = iterator.next();

            if (dominates(existing, candidate)) {
                candidateIsDominated = true;
                break;
            }
            else if (dominates(candidate, existing)) {
                iterator.remove();
            }
        }

        if (!candidateIsDominated) {
            paretoFront.add(candidate);
        }
    }

    private boolean isValidSolution(C candidate) {

        // Check that solution has both size and coverage
        double size = fitnessFunctions.get(0).applyAsDouble(candidate);
        double coverage = fitnessFunctions.get(1).applyAsDouble(candidate);

        return size > 0 && coverage > 0;
    }

    private boolean dominates(C solution1, C solution2) {

        boolean atLeastOneBetter = false;

        for (FitnessFunction<C> ff : fitnessFunctions) {

            double fitness1 = ff.applyAsDouble(solution1);
            double fitness2 = ff.applyAsDouble(solution2);

            if (ff instanceof MinimizingFitnessFunction) {
                if (fitness1 > fitness2) return false;
                if (fitness1 < fitness2) atLeastOneBetter = true;
            }
            else {
                if (fitness1 < fitness2) return false;
                if (fitness1 > fitness2) atLeastOneBetter = true;
            }
        }

        return atLeastOneBetter;
    }
}

