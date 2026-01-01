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
    // Generates random candidate solutions
    private final ChromosomeGenerator<C> chromosomeGenerator;
    // Fitness functions (e.g., size minimization, coverage maximization)
    private final List<FitnessFunction<C>> fitnessFunctions;
    // Controls termination of the search
    private final StoppingCondition stoppingCondition;
    // Current Pareto-optimal solutions
    private final List<C> paretoFront;
    // Initial population size for bootstrapping the Pareto front
    private static final int INITIAL_POPULATION_SIZE = 600;

    public RandomSearch(ChromosomeGenerator<C> chromosomeGenerator,
                        List<FitnessFunction<C>> fitnessFunctions,
                        StoppingCondition stoppingCondition) {

        this.chromosomeGenerator = chromosomeGenerator;
        this.fitnessFunctions = fitnessFunctions;
        this.stoppingCondition = stoppingCondition;
        this.paretoFront = new ArrayList<>();
    }

    //Executes the random search and returns the final Pareto front.
    @Override
    public List<C> findSolution() {

        stoppingCondition.notifySearchStarted();
        // Bootstrap Pareto front with high-quality solutions
        generateInitialPopulation();

        // Continue random sampling until stopping condition is met
        while (!stoppingCondition.searchMustStop()) {
            // Generate multiple samples per iteration to improve diversity
            for (int i = 0; i < 5 && !stoppingCondition.searchMustStop(); i++) {
                C candidate = sampleValidCandidate();
                updateParetoFront(candidate);
                stoppingCondition.notifyFitnessEvaluation();
            }
        }
        return new ArrayList<>(paretoFront);
    }

    //Generates an initial population biased toward meaningful solutions.
    private void generateInitialPopulation() {

        for (int i = 0; i < INITIAL_POPULATION_SIZE && !stoppingCondition.searchMustStop(); i++) {
            C candidate = sampleValidCandidate();
            updateParetoFront(candidate);
            stoppingCondition.notifyFitnessEvaluation();
        }
    }

    //Samples a random chromosome until a valid one is obtained.
    private C sampleValidCandidate() {
        C candidate;
        do {
            candidate = chromosomeGenerator.get();
        } while (!isValidSolution(candidate));
        return candidate;
    }

    @Override
    public StoppingCondition getStoppingCondition() {
        return stoppingCondition;
    }

    //Updates the Pareto front using dominance relations.

    private void updateParetoFront(C candidate) {

        boolean dominated = false;
        Iterator<C> iterator = paretoFront.iterator();

        while (iterator.hasNext()) {
            C existing = iterator.next();

            if (dominates(existing, candidate)) {
                dominated = true;
                break;
            }
            else if (dominates(candidate, existing)) {
                iterator.remove();
            }
        }
        if (!dominated) {
            paretoFront.add(candidate);
        }
    }


    // Filters out meaningless solutions.
    // Ensures solutions have sufficient coverage and are not excessively large.
    private boolean isValidSolution(C candidate) {

        double size = fitnessFunctions.get(0).applyAsDouble(candidate);
        double coverage = fitnessFunctions.get(1).applyAsDouble(candidate);

        // Require sufficient coverage
        if (coverage < 0.6) {
            return false;
        }
        // Prevent bloated test suites
        if (size > 0.6) {
            return false;
        }
        return true;
    }

    //Determines whether solution1 Pareto-dominates solution2.

    private boolean dominates(C solution1, C solution2) {

        boolean strictlyBetterInAtLeastOne = false;

        for (FitnessFunction<C> ff : fitnessFunctions) {

            double f1 = ff.applyAsDouble(solution1);
            double f2 = ff.applyAsDouble(solution2);

            if (ff instanceof MinimizingFitnessFunction) {
                if (f1 > f2) return false;
                if (f1 < f2) strictlyBetterInAtLeastOne = true;
            }
            else {
                if (f1 < f2) return false;
                if (f1 > f2) strictlyBetterInAtLeastOne = true;
            }
        }
        return strictlyBetterInAtLeastOne;
    }
}
