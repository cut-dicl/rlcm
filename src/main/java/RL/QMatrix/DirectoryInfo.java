package RL.QMatrix;

/**
 * This class represents a directory of storage system with important saved characteristics 
 * to help RL technique
 * 
 * @author Kakoulli Elena
 */

public class DirectoryInfo {

	
	enum DirectoryLastAccessTimeEnum {
		  RECENT,
		  LESS_RECENT,
		  MUCH_LESS_RECENT,
		  OLD
		}
	
	enum DirectoryAccessFrequencyEnum {
		NOT_FAMOUS, 	//0-1 times
		LESS_FAMOUS,	//2-5 times
		ENOUGH_FAMOUS,	//6-20 times
		FAMOUS
		}
	
	protected DirectoryLastAccessTimeEnum directory_last_access_time;
	protected long last_access_time;
	protected DirectoryAccessFrequencyEnum directory_access_frequency;
	protected int access_frequency;
	protected String name_directory; 
	protected int files = 0; //keep the count of files
	
	/**
	 * 	CONSTRUCTORS
	 */
	
	/**
	 * 
	 * @param last_access_time
	 * @param access_frequency
	 * @param directory_last_access_time (enum)
	 * @param directory_access_frequency (enum)
	 * @param name_directory
	 */
	public DirectoryInfo(long last_access_time, int access_frequency, DirectoryLastAccessTimeEnum directory_last_access_time, DirectoryAccessFrequencyEnum directory_access_frequency, String name_directory) {
		this.name_directory = name_directory;
		this.last_access_time = last_access_time;
		this.access_frequency = access_frequency;
		this.directory_last_access_time = directory_last_access_time; 
		this.directory_access_frequency = directory_access_frequency;
		this.files = 0;
	}
	
	/**
	 * 
	 * @param name_directory or filepath!!!
	 */
	public DirectoryInfo(String name_directory) {
		
		String directory;
		
		if(name_directory.contains("/")) 
			directory = name_directory.substring(0,name_directory.lastIndexOf("/")); 
		else //there is not folder (the file created in home dir)
			directory = ""; 
		
		this.name_directory = directory;
		this.last_access_time = System.currentTimeMillis(); 
		this.access_frequency = 1;
		this.directory_last_access_time = DirectoryLastAccessTimeEnum.RECENT;
		this.directory_access_frequency = DirectoryAccessFrequencyEnum.NOT_FAMOUS;
		this.files = 0;
	}

	
	/**
	 * 	METHODS
	 */
	
	/**
	 * 
	 * @param directory_last_access_time
	 */
	
	public void set_last_access_time() {
		long diff = System.currentTimeMillis() - this.last_access_time;
		
		//update
		this.last_access_time = System.currentTimeMillis();
		
		if(diff < 60) {
			this.directory_last_access_time = DirectoryLastAccessTimeEnum.RECENT;	
		} 
		else if(diff < 3600) {
			this.directory_last_access_time = DirectoryLastAccessTimeEnum.LESS_RECENT;
		}
		else if(diff < 86400) {
			this.directory_last_access_time = DirectoryLastAccessTimeEnum.MUCH_LESS_RECENT;
		}
		else
			this.directory_last_access_time = DirectoryLastAccessTimeEnum.OLD;
	}
	
	public long get_last_access_time() {
		return last_access_time;
	}
	
	public void setDirectory_last_access_time(DirectoryLastAccessTimeEnum directory_last_access_time) {
		this.directory_last_access_time = directory_last_access_time;
	}
	
	public DirectoryLastAccessTimeEnum getDirectory_last_access_time() {
		return this.directory_last_access_time; 
	}
	
	public void set_access_frequency(int access_frequency_num) {		
		this.access_frequency = access_frequency_num;
		
		if(this.access_frequency > 20) 
			this.directory_access_frequency = DirectoryAccessFrequencyEnum.FAMOUS;		
		else if(this.access_frequency > 5)
			this.directory_access_frequency = DirectoryAccessFrequencyEnum.ENOUGH_FAMOUS;
		else if(this.access_frequency > 2)		
			this.directory_access_frequency = DirectoryAccessFrequencyEnum.LESS_FAMOUS;
		else		
			this.directory_access_frequency = DirectoryAccessFrequencyEnum.NOT_FAMOUS;
	}	
	
	public int get_access_frequency() {
		return this.access_frequency;
	}
	
	/**
	 * 
	 * @param directory_access_frequency
	 */
	public void setDirectory_access_frequency(DirectoryAccessFrequencyEnum directory_access_frequency) {
		this.directory_access_frequency = directory_access_frequency;
	}
	
	/**
	 * 
	 * @return
	 */
	public DirectoryAccessFrequencyEnum getDirectory_access_frequency() {
		return this.directory_access_frequency; // Current time - currentfile_last_access_time
	}
	
	/**
	 * 
	 * @param name_directory
	 */
	public void setName_directory(String name_directory) {
		this.name_directory = name_directory; // Current time - currentfile_last_access_time
	}
	
	/**
	 * 
	 * @return
	 */
	public String getName_directory() {
		return this.name_directory; // Current time - currentfile_last_access_time
	}
	
	public int getFiles() {
		return this.files;
	}
	
	public void incrFiles() {
		this.files++;
	}
	
	public void decrFiles() {
		this.files--;
	}
	
	public void incr_access_frequency() {
		this.access_frequency++;
		
		//update file access frequency
		set_access_frequency(this.access_frequency);
	}
	
	@Override
	public String toString() {
		return "\nDirectory name = " + name_directory + 
				"\nDirectory_last_access_time_date = " + last_access_time + 
				"\nDirectory_last_access_time = " + directory_last_access_time + 
				"\nDirectory_access_frequency_num = " + access_frequency + 
				"\nDirectory_access_frequency = " + directory_access_frequency + 
				"\nNumber_of_files = " + files;
	}
	
}