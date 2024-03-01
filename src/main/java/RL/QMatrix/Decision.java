package RL.QMatrix;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class represents a decision in QMatrix RL technique
 * 
 * @author Kakoulli Elena
 */


public class Decision {
	private static final Logger logger = LoggerFactory.getLogger(Decision.class);
	
    public int oldState; 		//2bitsx7fields=14bits
    public int newState;
    public FileInfo fileObject;
    public int action;			//3bits (8 actions)
    public double reward;
    public Set<Integer> possibleActions = new HashSet<Integer>();
    public int updates; 
    public long time;
    
    public Decision(int oldState, int newState, FileInfo fileObject, int action, double reward) {
        this.oldState = oldState;
        this.newState = newState;
        this.fileObject = fileObject;
        this.action = action;
        this.reward = reward;
        this.updates = 0;
        this.time = System.currentTimeMillis();
    }

    public Decision(int oldState, int newState, FileInfo fileObject, int action, double reward, Set<Integer> possibleActions) {
        this.oldState = oldState;
        this.newState = newState;
        this.fileObject = fileObject;
        this.action = action;
        this.reward = reward;
        this.possibleActions = possibleActions;
        this.updates = 0;
        this.time = System.currentTimeMillis();
    }
    
    public void print() {
    	logger.info(new Timestamp(System.currentTimeMillis()) + ":: oldState = {}, newState = {}, fileObject = {}, action = {}, reward = {}, possibleActions = {}, updates = {}, time = {}", this.oldState, this.newState, this.fileObject.toString(), this.action, this.reward, this.possibleActions, this.updates, this.time); 
    }
    
    public String toString() {
    	return "\noldState = " + oldState +
    			"\nnewState = " + newState +
    			"\naction = " + action +
    			"\nreward = " + reward + 
    			"\npossibleActions = " + possibleActions +
    			"\nupdates = " + updates +
    			"\ndecision time = " + time + "\n"; 
    }

}
