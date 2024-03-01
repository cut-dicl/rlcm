package RL.QMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.github.chen0040.rl.models.QModel;
import com.github.chen0040.rl.utils.IndexValue;
import com.github.chen0040.rl.utils.Vec;

import RL.QMatrix.AdmissionStrategy.Position;

/**
 * This class represents the QModel
 *
 * @author Kakoulli Elena
 */

public class QModelCache extends QModel {
		/**
	    *  Q value for (state_id, action_id) pair
	    *  Q is known as the quality of state-action combination, note that it is different from utility of a state
	    */
		private HashMap<Integer,double[]> Qvalues;	
	    /**
	    *  $\alpha[s, a]$ value for learning rate: alpha(state_id, action_id)
	    */
		private HashMap<Integer,double[]> alphaMatrix;
		private double alpha;
		    
	    /**
	     * discount factor
	     */
	    private double gamma;
	    	
	    private int tiersMovements;
	    private double[] initialQs;
	    
	    
	public QModelCache(int tiersMovements, double[] initialQ, double alpha, double gamma) {		
		//Create Q-Table
		Qvalues = new HashMap<Integer,double[]>();
		
		//Create alpha-table
		alphaMatrix = new HashMap<Integer,double[]>();
	
		this.tiersMovements = tiersMovements;
		this.initialQs = initialQ;
		this.alpha = alpha;
		this.gamma = gamma;
	}
	 
	 @Override
	 public double getQ(int stateId, int actionId){
		 if (!Qvalues.containsKey(stateId)) //if state does not exist
			 initialStateAction(stateId);
		 
	        return Qvalues.get(stateId)[actionId];
	    }
	 
	 @Override
	 public void setQ(int stateId, int actionId, double Qij){
		 if (!Qvalues.containsKey(stateId)) //if state does not exist
			 initialStateAction(stateId);
		
				 double[] actions = Qvalues.get(stateId);
				 actions[actionId] = Qij;
				 Qvalues.put(stateId, actions); 
	 }	
	 
	 public void initialStateAction(int stateId) {
		 
		double[] actions = new double[initialQs.length];
		for(int a=0; a<initialQs.length;a++)
			actions[a] = initialQs[a];
		Qvalues.put(stateId, actions);
		     
		//initialize alpha-values
		double[] alphaValues = new double[initialQs.length];
		for(int a=0; a<initialQs.length; a++)	
			alphaValues[a] = alpha;	    	
		alphaMatrix.put(stateId, alphaValues);
	 }
	 
	 @Override
	 public double getAlpha(int stateId, int actionId){
	        return alphaMatrix.get(stateId)[actionId];
	 }

	 @Override
	 public void setAlpha(double defaultAlpha) {
		 for (HashMap.Entry<Integer,double[]> entry : alphaMatrix.entrySet()) {
			 double[] actions = this.alphaMatrix.get(entry.getKey());
			 for (int i=0;i<actions.length;i++) {
					actions[i] = defaultAlpha;
				}
	    }
	 }
	 
	 @Override
	 public IndexValue actionWithMaxQAtState(int stateId, Set<Integer> actionsAtState){
		 if (!Qvalues.containsKey(stateId)) //if state does not exist
			 initialStateAction(stateId);
		 
	        Vec rowVector = new Vec(Qvalues.get(stateId));
	        return rowVector.indexWithMaxValue(actionsAtState);
	 }

     public void reset(double initialQ) {
		for (HashMap.Entry<Integer,double[]> entry : Qvalues.entrySet()) {
				for (int i=0;i<entry.getValue().length;i++) {
					entry.getValue()[i] = initialQ;
				}
		 }
	 }

	 @Override
	 public IndexValue actionWithSoftMaxQAtState(int stateId,Set<Integer> actionsAtState, Random random) {
	        Vec rowVector = new Vec(Qvalues.get(stateId));
	        double sum = 0;

	        if(actionsAtState==null){
	            actionsAtState = new HashSet<>();
	            for(int i=0; i < Qvalues.get(stateId).length; ++i){
	                actionsAtState.add(i);
	            }
	        }

	        List<Integer> actions = new ArrayList<>();
	        for(Integer actionId : actionsAtState){
	            actions.add(actionId);
	        }

	        double[] acc = new double[actions.size()];
	        for(int i=0; i < actions.size(); ++i){
	            sum += rowVector.get(actions.get(i));
	            acc[i] = sum;
	        }


	        double r = random.nextDouble() * sum;

	        IndexValue result = new IndexValue();
	        for(int i=0; i < actions.size(); ++i){
	            if(acc[i] >= r){
	                int actionId = actions.get(i);
	                result.setIndex(actionId);
	                result.setValue(rowVector.get(actionId));
	                break;
	            }
	        }

	        return result;
	    }
	 
		/*
		 * strategy = 0 //ADMISSION 
		 * strategy = 1 //REPLACEMENT
		 * 
		 */
		public void print(int strategy) {
			int counter = 1;

			for (Iterator<Entry<Integer, double[]>> entry = Qvalues.entrySet().iterator(); entry.hasNext();) {
				Entry<Integer, double[]> state = entry.next();

				double[] actions = state.getValue();

				System.out.println("\n(" + counter + ")\n" + "File State: " + state.getKey());
				counter++;
		
				if (strategy == 1) {
					System.out.println("NOT_EVICT = " + actions[0]);
					System.out.println("EVICT_TO_LOWER_TIER = " + actions[1]);
					System.out.println("EVICT_TO_NONE = " + actions[2]);
				} else if (strategy == 0) {
					for (int i = 0; i < actions.length; i++) {
						System.out.println(Position.validValues(tiersMovements)[i].name() + " = " + actions[i]);
					}
				} else
					System.out.println("\nUsed wrong parameter num for strategy! (0-admission & 1-replacement)\n");
			}

		}
	 
	 public void setGamma(double g){
		 this.gamma = g;
	 }
	 
	 public double getGamma(){
		 return this.gamma;
	 }
	 
	 public double[] getQs(int stateId){
		if(Qvalues.get(stateId)!=null)
	        return Qvalues.get(stateId);
		else 
			return null; //if state does not exist
	    }
}
