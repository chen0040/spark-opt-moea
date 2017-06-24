package com.github.chen0040.spark.moea;


import com.github.chen0040.data.utils.TupleTwo;
import com.github.chen0040.moea.components.*;
import com.github.chen0040.moea.enums.ReplacementType;
import com.github.chen0040.moea.utils.InvertedCompareUtils;
import com.github.chen0040.moea.utils.TournamentSelection;
import com.github.chen0040.moea.utils.TournamentSelectionResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.spark.api.java.JavaSparkContext;


/**
 * Created by xschen on 17/6/2017.
 * NSGA-II
 */
@Getter
@Setter
public class SparkNSGAII extends Mediator {

   private int displayEvery = -1;

   @Setter(AccessLevel.NONE)
   private NondominatedPopulation archive = new NondominatedPopulation();

   @Setter(AccessLevel.NONE)
   protected int currentGeneration = 0;

   @Setter(AccessLevel.NONE)
   protected NondominatedSortingPopulation population = new NondominatedSortingPopulation();

   @Setter(AccessLevel.NONE)
   private JavaSparkContext context;

   public SparkNSGAII(JavaSparkContext context){
      this.context = context;
   }

   public NondominatedPopulation solve(){
      initialize();
      int maxGenerations = this.getMaxGenerations();
      for(int generation = 0; generation < maxGenerations; ++generation) {
         evolve();
         if(displayEvery > 0 && generation % displayEvery == 0){
            System.out.println("Generation #" + generation + "\tArchive size: " + archive.size());
         }
      }

      return archive;
   }

   public void initialize(){
      archive.setMediator(this);
      archive.clear();

      population.setMediator(this);
      population.initialize();
      evaluate(population);
      population.sort();
      currentGeneration = 0;
   }

   public void evolve()
   {
      Population offspring = new Population();
      offspring.setMediator(this);

      int populationSize = this.getPopulationSize();

      while (offspring.size() < populationSize)
      {
         TournamentSelectionResult<Solution> tournament = TournamentSelection.select(population.getSolutions(), this.getRandomGenerator(), (s1, s2) ->
         {
            int flag;
            if ((flag = InvertedCompareUtils.ConstraintCompare(s1, s2))==0) // return -1 if s1 is better
            {
               if ((flag = InvertedCompareUtils.ParetoObjectiveCompare(s1, s2)) == 0) // return -1 if s1 is better
               {
                  flag = InvertedCompareUtils.CrowdingDistanceCompare(s1, s2); // return -1 if s1 is better
               }
            }

            return flag < 0; // return -1 if s1 is better
         });

         TupleTwo<Solution, Solution> tournament_winners = tournament.getWinners();

         TupleTwo<Solution, Solution> children = Crossover.apply(this, tournament_winners._1(), tournament_winners._2());

         Mutation.apply(this, children._1());
         Mutation.apply(this, children._2());

         offspring.add(children._1());
         offspring.add(children._2());
      }

      evaluate(offspring);

      ReplacementType replacementType = this.getReplacementType();
      if(replacementType == ReplacementType.Generational) {
         merge1(offspring);
      } else if(replacementType == ReplacementType.Tournament) {
         merge2(offspring);
      }

      currentGeneration++;
   }

   protected void evaluate(Population population) {

      for (int i = 0; i < population.size(); ++i)
      {
         Solution s = population.getSolutions().get(i);
         s.evaluate(this);

         //System.out.println("cost1: " + s.getCost(0) + "\tcost2:" + s.getCost(1));

         boolean is_archivable = archive.add(s);

         if (archive.size() > this.getMaxArchive())
         {
            archive.truncate(this.getMaxArchive());
         }
      }
   }

   protected void merge2(Population children)
   {
      int populationSize = this.getPopulationSize();

      Population offspring = new Population();

      for (int i = 0; i < populationSize; i++)
      {
         Solution s1 = children.get(i);
         Solution s2 = population.get(i);
         int flag = 0;
         if ((flag = InvertedCompareUtils.ConstraintCompare(s1, s2)) == 0)
         {
            if ((flag = InvertedCompareUtils.ParetoObjectiveCompare(s1, s2)) == 0)
            {
               flag = InvertedCompareUtils.CrowdingDistanceCompare(s1, s2);
            }
         }

         if (flag < 0)
         {
            offspring.add(children.get(i));
         }
         else if (flag > 0)
         {
            offspring.add(children.get(i));
         }
         else
         {
            offspring.add(children.get(i));
            offspring.add(population.get(i));
         }
      }

      population.clear();

      population.add(offspring);

      population.prune(populationSize);
   }

   protected void merge1(Population children)
   {
      int populationSize = this.getPopulationSize();

      population.add(children);

      population.truncate(populationSize);
   }
}
