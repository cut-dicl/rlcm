package RL.QMatrix;


/**
 * This class represents a state of RL technique
 * 
 * @author elenakakoulli
 *
 */
public class State {
	
	public FileInfo fileInfo;
	protected TypeInfo typeInfo;
	protected DirectoryInfo directoryInfo;
	protected Integer stateID;
	
	/**
	 *	Constructor 
	 */
	public State(FileInfo fileInfo) {
		this.fileInfo = fileInfo;
		this.typeInfo = fileInfo.getFile_format();
		this.directoryInfo = fileInfo.getDirectory_path();
	}

	/**
	 *	Methods 
	 */
	
	/**
	 * Info from 3 objects (9 characteristics for state)
	 * ---------------------
	 * FileInfo —> info for file 				(5 characteristics for state)
	 * TypeInfo —> info for file type 			(2 characteristics for state)
	 * DirectoryInfo —> info for file directory	(2 characteristics for state)
	 * 
	 * @return StateID
	 * 
	 */
	
	public boolean equals(State s) {
		
		if(fileInfo.equals(s.fileInfo))
			return true;
		else
			return false;
	}
	
	
	public Integer getStateID() {
	    int stateID = 0;

	    // Map each characteristic to a specific bit position
	    int[] bitPositions = {
	        fileInfo.getFileSize().ordinal(),
	        fileInfo.getLast_modified().ordinal(),
	        fileInfo.getFile_last_access_time().ordinal(),
	        fileInfo.getFile_access_frequency().ordinal(),
	        fileInfo.file_format.getFile_type().ordinal(),
	        typeInfo.getFile_type_last_access_time().ordinal(),
	        typeInfo.getFile_type_access_frequency().ordinal(),
	        directoryInfo.getDirectory_last_access_time().ordinal(),
	        directoryInfo.getDirectory_access_frequency().ordinal()
	    };

	    // Ensure that each ordinal value fits within 2 bits
	    for (int i = 0; i < bitPositions.length; i++) {
	        if (bitPositions[i] >= 4) {
	            throw new IllegalArgumentException("Ordinal value out of range for 2 bits.");
	        }

	        // Combine the bits into stateID at their respective positions
	        stateID |= (bitPositions[i] << (i * 2));
	    }

	    return stateID;
	}
}
