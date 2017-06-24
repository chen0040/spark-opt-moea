package com.github.chen0040.spark.moea;


import com.github.chen0040.moea.algorithms.NSGAII;
import com.github.chen0040.moea.components.DE;
import com.github.chen0040.moea.components.Mutation;
import com.github.chen0040.moea.components.Population;
import com.github.chen0040.moea.components.Solution;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created by xschen on 19/6/2017.
 */
public class SparkGDE3 extends SparkNSGAII {
   private static final long serialVersionUID = -6713947168965879195L;

   public SparkGDE3(JavaSparkContext context) {
      super(context);
   }


   @Override
   public void evolve()
   {
      int index = 0;
      int populationSize = getPopulationSize();
      Solution[] parents = new Solution[4];
      Population children = new Population();
      for (int i = 0; i < populationSize; ++i )
      {

         parents[0] = population.get(i);
         Set<Integer> indices = new HashSet<>();
         indices.add(i);
         for (int j = 1; j < 4; ++j)
         {
            do
            {
               index = nextInt(populationSize);
            } while (indices.contains(index));
            indices.add(index);
            parents[j] = population.get(index);
         }

         List<Solution> result = DE.apply(this, parents);

         for (Solution child : result)
         {
            Mutation.apply(this, child);
            children.add(child);
         }
      }

      evaluate(children);

      merge2(children);

      currentGeneration++;
   }
}
