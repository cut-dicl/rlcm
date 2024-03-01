package RL.QMatrix.AdmissionStrategy;

import java.sql.Timestamp;
import java.util.EnumMap;
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

/**
 * This class represents an agent of Q-learning RL technique for Admission Strategy
 * 
 * @author Kakoulli Elena
 */
 
public class AdmissionAgent extends CacheAgent {
	
	private static final Logger logger = LoggerFactory.getLogger(AdmissionAgent.class);
	
    private final QLearner agent;
    protected QModelCache model;
    protected Set<Integer> possibleActions;
    protected TiersMovements[] validTiersMovements;
    protected EnumMap<TiersMovements, Integer> validTiersMovementsMap; // Map TierMovements to index in validTiersMovements
    
    protected long memThresPerc;
    protected long diskThresPerc;
    
      public AdmissionAgent(QLearner learner) {
    	super();
    	this.agent = learner;
    }
      
    public AdmissionAgent(int tiersMovements, double initialQs[], double alpha, double gamma, long memThresPerc, long diskThresPerc) {
    	super();
		
    	this.memThresPerc = memThresPerc;
    	this.diskThresPerc = diskThresPerc;
    	
 		this.model = new QModelCache(tiersMovements, initialQs, alpha, gamma);
		this.agent = new QLearner();		
		this.agent.setModel(model);	
		
        this.validTiersMovements = Position.validValues(tiersMovements);
        this.validTiersMovementsMap = new EnumMap<>(TiersMovements.class);
        this.possibleActions = new HashSet<Integer>();  
        for (int i = 0; i < validTiersMovements.length; ++i) {
            this.validTiersMovementsMap.put(validTiersMovements[i], i);
			this.possibleActions.add(i);
        }
    }
    
 
    public QLearner getQLearner() {
    	return this.agent;
    }

    public int act(State file, AtomicLong memFilesSize, AtomicLong diskFilesSize, boolean forRead) {
    	int state = getState(file);
        int action = 0;
        boolean stateExists = false;
        
    	Position pos = file.fileInfo.getCurrentPosition();
         
        if(!possibleActions.isEmpty()) {	
        	//First check if the state exists if not then you must choose MEM
        	if (((QModelCache) agent.getModel()).getQs(state)!=null) {
        		stateExists= true;
        	}
        		
        	IndexValue iv = agent.selectAction(state, possibleActions);
        	action = iv.getIndex();
        	double value = iv.getValue();
            
            decisions.add(new Decision(state, state, file.fileInfo, action, 0, possibleActions));

            if(value <= 0 || !stateExists || (memFilesSize.get() < this.memThresPerc)){
                // Add memory to current selected tier
                TiersMovements combined = TiersMovements.CombineTiers(validTiersMovements[action], TiersMovements.MEM);
                if (validTiersMovementsMap.containsKey(combined)) {
                    action = validTiersMovementsMap.get(combined);
                }
            } else if(diskFilesSize.get() < this.diskThresPerc) {
                if (validTiersMovements[action] == TiersMovements.NONE 
                    && validTiersMovementsMap.containsKey(TiersMovements.DISK)) {
                    action = validTiersMovementsMap.get(TiersMovements.DISK);
                }
            }

            if (forRead && pos.getPosition() != validTiersMovements[action]) {
                // Make sure the action leads to equal or higher tier placement
                int comp = TiersMovements.CompareHighestTier(pos.getPosition(), validTiersMovements[action]);
                if (comp > 0) {
                    action = validTiersMovementsMap.get(pos.getPosition());
                } else if (comp == 0) {
                    TiersMovements combined = TiersMovements.CombineTiers(pos.getPosition(), validTiersMovements[action]);
                    if (validTiersMovementsMap.containsKey(combined)) {
                        action = validTiersMovementsMap.get(combined);
                    }
                }
            }

            pos = new Position(validTiersMovements[action]);
        }
         
        return pos.getPosition().ordinal();
    }
    
    @Override
    public void updateStrategy() {
		TiersMovements action_tiers = TiersMovements.NONE;

		for (int i = 0; i < decisions.size(); i++) {
			Decision move = decisions.get(i);
		
			FileInfo file = move.fileObject;
			// -------------------------------------------------
			// take the right tier move from action
			action_tiers = validTiersMovements[move.action];
			// -------------------------------------------------

			if (admission_action_values.get(action_tiers) > 5 && file.getFile_access_frequency().ordinal() == 0)
				move.reward = 0.667; // special case
			else
				move.reward = (admission_action_values.get(action_tiers) + file.getFile_access_frequency().ordinal()) / 3.0;

			if (move.reward > 1) {
				move.reward = 2.0 - move.reward;
			}

			agent.update(move.oldState, move.action, move.newState, move.possibleActions, move.reward);	
			move.updates++;
    		logger.info(new Timestamp(System.currentTimeMillis()) + ":: [CM] - AdmissionAgent.update with " + " move.action=" + move.action + " oldState=" + move.oldState +" newState=" + move.newState + " reward=" + move.reward);
     	
		}
    }
    
}
