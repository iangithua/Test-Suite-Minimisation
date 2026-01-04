package de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.Chromosome;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.ChromosomeGenerator;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.sbse.suite_minimisation.stopping_conditions.StoppingCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RandomSearch<C extends Chromosome<C>> implements GeneticAlgorithm<C> {
    private final ChromosomeGenerator<C> chromosomeGenerator;
    private final List<FitnessFunction<C>> fitnessFunctions;
    private final StoppingCondition stoppingCondition;


    public RandomSearch(ChromosomeGenerator<C> chromosomeGenerator,
                        List<FitnessFunction<C>> fitnessFunctions,
                        StoppingCondition stoppingCondition) {

        this.chromosomeGenerator = chromosomeGenerator;
        this.fitnessFunctions = fitnessFunctions;
        this.stoppingCondition = stoppingCondition;
    }

    @Override
    public List<C> findSolution() {
        List<C> solutions = new ArrayList<>();
        stoppingCondition.notifySearchStarted();
        // Collect all solutions during the search
        while (!stoppingCondition.searchMustStop()) {
            C candidate = chromosomeGenerator.get();
            // Generate 3 candidates per iteration (like CODE 1)
            for (FitnessFunction<C> fitnessFunction : fitnessFunctions) {

                fitnessFunction.applyAsDouble(candidate);
            }
            solutions.add(candidate);
            stoppingCondition.notifyFitnessEvaluation();
        }

        // Build Pareto front from all collected solutions at the end
        List<List<C>> paretoFront = findParetoFront(solutions);
        return paretoFront.isEmpty() ? List.of(chromosomeGenerator.get()) : paretoFront.get(0);
    }

    @Override
    public StoppingCondition getStoppingCondition() {
        return stoppingCondition;
    }
    private List<List<C>>  findParetoFront(List<C> solutions) {
        Map<C, Integer> dominationCount = new HashMap<>();
        Map<C, List<C>> dominatedSolutions = new HashMap<>();

        // Initialize maps
        for (C individual : solutions) {
            dominationCount.put(individual, 0);
            dominatedSolutions.put(individual, new ArrayList<>());
        }

        // Build fronts
        List<List<C>> fronts = new ArrayList<>();
        List<C> currentFront = new ArrayList<>();

        // First front: solutions with domination count = 0
        for (C individual : solutions) {
            if (dominationCount.get(individual) == 0) {
                currentFront.add(individual);
            }
        }

        // Build subsequent fronts
        while (!currentFront.isEmpty()) {
            fronts.add(new ArrayList<>(currentFront));
            List<C> nextFront = new ArrayList<>();

            for (C individual : currentFront) {
                for (C dominated : dominatedSolutions.get(individual)) {
                    dominationCount.put(dominated, dominationCount.get(dominated) - 1);
                    if (dominationCount.get(dominated) == 0) {
                        nextFront.add(dominated);
                    }
                }
            }

            currentFront = nextFront;
        }

        return fronts;
    }
    }
