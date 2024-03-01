package RL.QMatrix;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import RL.QMatrix.AdmissionStrategy.TiersMovements;
import RL.QMatrix.ReplacementStrategy.Actions;

/**
 * This class represents an agent of RL technique
 * 
 * @author Kakoulli Elena
 */

public abstract class CacheAgent {

	protected List<Decision> decisions;
	protected static Random random = new Random(42);

	protected HashMap<TiersMovements, Double> admission_action_values = new HashMap<TiersMovements, Double>(); 	//mapping for admission action
	protected HashMap<Actions, Double>  eviction_action_values = new HashMap<Actions, Double>(); 				// mapping for eviction action

	public CacheAgent() {
		this.decisions = new CopyOnWriteArrayList<>();

		//Create the mapping for admission action 
		admission_action_values.put(TiersMovements.MEM, 0.0);
		admission_action_values.put(TiersMovements.SSD_MEM, 0.4);
		admission_action_values.put(TiersMovements.SSD, 1.0);
		admission_action_values.put(TiersMovements.DISK_MEM, 1.2);
		admission_action_values.put(TiersMovements.DISK_SSD, 1.4);
		admission_action_values.put(TiersMovements.DISK_SSD_MEM, 1.6);
		admission_action_values.put(TiersMovements.DISK, 2.0);
		admission_action_values.put(TiersMovements.NONE, 6.0);
		
		//Create the mapping for eviction action 
		eviction_action_values.put(Actions.NOT_EVICT, 0.0);
		eviction_action_values.put(Actions.EVICT_TO_LOWER_TIER, 5.0);
		eviction_action_values.put(Actions.EVICT_TO_NONE, 6.0);
		
	}

	public abstract void updateStrategy();

	public int getState(State file) {
		int state = file.getStateID();

		return state;
	}

	public void clearHistory() {
		decisions.clear();
	}

	public List<Decision> getDecisions() {
		return this.decisions;
	}

}