package RL.QMatrix.ReplacementStrategy;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chen0040.rl.learning.qlearn.QLearner;
import com.github.chen0040.rl.utils.IndexValue;

import RL.QMatrix.CacheAgent;
import RL.QMatrix.Decision;
import RL.QMatrix.FileInfo;
import RL.QMatrix.QModelCache;
import RL.QMatrix.State;
import RL.QMatrix.AdmissionStrategy.AdmissionAgent;
import RL.QMatrix.AdmissionStrategy.TiersMovements;

/**
 * This class represents an agent of Q-learning RL technique for Replacement Strategy
 * 
 * @author Kakoulli Elena
 */

public class ReplacementAgent extends CacheAgent {
		
		private static final Logger logger = LoggerFactory.getLogger(AdmissionAgent.class);
		
	    private final QLearner agent;
	    protected QModelCache model;
	    protected Set<Integer> possibleActions;
	    
	    public ReplacementAgent(QLearner learner) {
	    	super();
	    	this.agent = learner;
	    }
	      
	    public ReplacementAgent(double initialQs[], double alpha, double gamma) {
	    	super();
			
	    	//Create QModel   			 	
			this.model = new QModelCache(Actions.values().length, initialQs, alpha, gamma); //3 Actions		
			this.agent = new QLearner();
			this.agent.setModel(model);
			this.possibleActions = new HashSet<Integer>(); 
			for(Actions action: Actions.values()) {
				possibleActions.add(action.ordinal());
    		}
	    }
	    
	    public QLearner getQLearner() {
	    	return this.agent;
	    }
	    
	    public int act(State file, AtomicLong currentMemCapacity, AtomicLong currentDiskCapacity) {
	    	int state = getState(file);
	    	int action = 0;
	
	    	if (!possibleActions.isEmpty()) {
				IndexValue iv = agent.selectAction(state, possibleActions);
				action = iv.getIndex();
				double value = iv.getValue();
				
				decisions.add(new Decision(state, state, file.fileInfo, action, 0, possibleActions));
				
				if (value <= 0 /*|| !stateExists*/) {
    				action = Actions.NOT_EVICT.ordinal();
				} else {
					// give a 2nd chance before evict it from MEM
					if (action == Actions.EVICT_TO_NONE.ordinal()
							&& file.fileInfo.getCurrentPosition().getPosition() == TiersMovements.MEM) {

						action = Actions.EVICT_TO_LOWER_TIER.ordinal();
                    }
				}

			}

			return action;
	    }
	   
		@Override
		public void updateStrategy() {

			for (int i = decisions.size() - 1; i >= 0; --i) {

				Decision decision = decisions.get(i);
				FileInfo file = decision.fileObject;
				Actions action = Actions.NOT_EVICT;

				switch (decision.action) {

				case 0: // object has not evicted
					action = Actions.NOT_EVICT;
					break;
				case 1: // object has evicted to lower tier
					action = Actions.EVICT_TO_LOWER_TIER;
					break;
				case 2: // object has evicted to none - delete
					action = Actions.EVICT_TO_NONE;
					break;
				default:
					logger.error(new Timestamp(System.currentTimeMillis()) + ":: Undefined action: {}", decision.action);
				}

				// -------------------------------------------------

				if (eviction_action_values.get(action) > 5 && file.getFile_access_frequency().ordinal() == 0)
					decision.reward = 0.667; // special case
				else
					decision.reward = (eviction_action_values.get(action) + file.getFile_access_frequency().ordinal())
						/ 3.0;

				if (decision.reward > 1) {
					decision.reward = 2.0 - decision.reward;
				}

				agent.update(decision.oldState, decision.action, decision.newState, decision.possibleActions,
						decision.reward);
				decision.updates++;

				logger.info(new Timestamp(System.currentTimeMillis()) + ":: [CM] - ReplacementAgent.update with decision.action=" + decision.action + " oldState="
						+ decision.oldState + " newState=" + decision.newState + " reward=" + decision.reward);
			}
		}
	    
	}
