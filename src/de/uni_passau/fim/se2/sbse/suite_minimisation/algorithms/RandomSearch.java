package de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.Chromosome;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.ChromosomeGenerator;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.MinimizingFitnessFunction;
import de.uni_passau.fim.se2.sbse.suite_minimisation.stopping_conditions.StoppingCondition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class RandomSearch<C extends Chromosome<C>> implements GeneticAlgorithm<C> {

    private final ChromosomeGenerator<C> chromosomeGenerator;
    private final List<FitnessFunction<C>> fitnessFunctions;
    private final StoppingCondition stoppingCondition;
    private final List<C> paretoFront;
    private final Random random;

    // Increased parameters for better exploration
    private final int initialPopulationSize = 500;
    private final int candidatesPerIteration = 50;

    // Adaptive sampling: percentage of candidates derived from existing solutions
    private final double guidedSamplingRate = 0.7;

    public RandomSearch(ChromosomeGenerator<C> chromosomeGenerator,
                        List<FitnessFunction<C>> fitnessFunctions,
                        StoppingCondition stoppingCondition) {

        this.chromosomeGenerator = chromosomeGenerator;
        this.fitnessFunctions = fitnessFunctions;
        this.stoppingCondition = stoppingCondition;
        this.paretoFront = new ArrayList<>();
        this.random = new Random();
    }

    @Override
    public List<C> findSolution() {

        stoppingCondition.notifySearchStarted();
        generateInitialPopulation();

        while (!stoppingCondition.searchMustStop()) {

            for (int i = 0; i < candidatesPerIteration && !stoppingCondition.searchMustStop(); i++) {

                C candidate;

                // Use guided sampling when Pareto front is non-empty
                if (!paretoFront.isEmpty() && random.nextDouble() < guidedSamplingRate) {
                    candidate = generateGuidedCandidate();
                } else {
                    candidate = chromosomeGenerator.get();
                }

                updateParetoFront(candidate);
                stoppingCondition.notifyFitnessEvaluation();
            }

            // Periodically diversify the Pareto front
            if (random.nextDouble() < 0.1) {
                diversifyParetoFront();
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

    //Generates a candidate by mutating an existing Pareto front solution.
    //This improves exploration by building on known good solutions.
    private C generateGuidedCandidate() {
        // Select one or two random solutions from the Pareto front
        C parent1 = paretoFront.get(random.nextInt(paretoFront.size()));
        C parent2 = paretoFront.get(random.nextInt(paretoFront.size()));

        // Create offspring using crossover
        var offspring = parent1.crossover(parent2);

        // Randomly pick one of the two offspring
        C candidate = random.nextBoolean() ? offspring.getFst() : offspring.getSnd();

        // Mutate to add variation
        candidate.mutate();

        return candidate;
    }

    //Removes crowded solutions to maintain diversity.
    //Keeps the Pareto front spread across the objective space.
    private void diversifyParetoFront() {
        if (paretoFront.size() <= 10) {
            return; // Keep small fronts intact
        }

        // Simple crowding: remove solutions that are too similar
        List<C> toRemove = new ArrayList<>();

        for (int i = 0; i < paretoFront.size() - 1; i++) {
            for (int j = i + 1; j < paretoFront.size(); j++) {
                if (areTooSimilar(paretoFront.get(i), paretoFront.get(j))) {
                    // Remove the one with worse average fitness
                    if (getAverageFitness(paretoFront.get(i)) < getAverageFitness(paretoFront.get(j))) {
                        toRemove.add(paretoFront.get(i));
                    } else {
                        toRemove.add(paretoFront.get(j));
                    }
                    break;
                }
            }
        }

        paretoFront.removeAll(toRemove);
    }

    //Checks if two solutions are too similar in objective space.
    private boolean areTooSimilar(C solution1, C solution2) {
        double threshold = 0.05; // 5% difference threshold

        for (FitnessFunction<C> ff : fitnessFunctions) {
            double fitness1 = ff.applyAsDouble(solution1);
            double fitness2 = ff.applyAsDouble(solution2);

            double maxFitness = Math.max(Math.abs(fitness1), Math.abs(fitness2));
            if (maxFitness == 0) maxFitness = 1.0;

            double relativeDiff = Math.abs(fitness1 - fitness2) / maxFitness;

            if (relativeDiff > threshold) {
                return false; // Sufficiently different
            }
        }

        return true; // Too similar across all objectives
    }

    //Computes normalized average fitness across all objectives.
    private double getAverageFitness(C solution) {
        double sum = 0;

        for (FitnessFunction<C> ff : fitnessFunctions) {
            double fitness = ff.applyAsDouble(solution);

            // Normalize by treating minimizing objectives as negative
            if (ff instanceof MinimizingFitnessFunction) {
                sum -= fitness;
            } else {
                sum += fitness;
            }
        }

        return sum / fitnessFunctions.size();
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