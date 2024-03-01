/**
 * 
 */
package RL.QMatrix;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import RL.QMatrix.AdmissionStrategy.Position;
import RL.QMatrix.AdmissionStrategy.TiersMovements;

/**
 * This class represents an object in the storage with its uniquely saved characteristics
 *
 * @author Kakoulli Elena
 */

public class FileInfo {
	
	private static final Logger logger = LoggerFactory.getLogger(FileInfo.class);
	
	enum SizeEnum {
		  SMALL,  //0B-1KB (>0B <1KB)
		  MEDIUM, //1KB-1MB (>=1KB <1MB)
		  LARGE,  //1MB-1GB (>=1MB <1GB)
		  HUGE    //>=1GB
		}
	
	enum LastModifiedEnum {
		  RECENTLY, 			//last 10 minutes
		  LESS_RECENTLY,		//less 1 hour
		  MUCH_LESS_RECENTLY, 	//less 1 day
		  OLD
		}
	
	enum FileLastAccessTimeEnum {
		  RECENT,
		  LESS_RECENT,
		  MUCH_LESS_RECENT,
		  OLD
		}
	
	public enum FileAccessFrequencyEnum {
		NOT_FAMOUS, 	//0-1 times
		LESS_FAMOUS,	//2-5 times
		ENOUGH_FAMOUS,	//6-20 times
		FAMOUS
	}
	
	protected String pathname; 	
	protected long size; //bytes
	protected SizeEnum fileSize; 
	protected long last_modified_date; //seconds
	protected LastModifiedEnum last_modified; //[Long type (save epoch)] Current time - last modified time
	protected long file_last_access_time_date; //seconds
	protected FileLastAccessTimeEnum file_last_access_time; // Current time - file_last_access_time
	protected int file_access_frequency_num;
	protected FileAccessFrequencyEnum file_access_frequency; 
	protected TypeInfo file_format;
	protected DirectoryInfo directory_path; //(part of key)
	protected Position currentPosition;
	protected boolean evict; // 0=not_evict & 1=evict(delete)
	protected int misses;
	
	
	/**
	 * CONSTRUCTORS
	 */
	
	public FileInfo(String pathname, long size, SizeEnum fileSize, long last_modified_date, LastModifiedEnum last_modified, long file_last_access_time_date, FileLastAccessTimeEnum file_last_access_time, int file_access_frequency_num, FileAccessFrequencyEnum file_access_frequency, TypeInfo file_format, DirectoryInfo directory_path){
        this.pathname = pathname;
        this.size = size;
        this.fileSize = fileSize;
        this.last_modified_date = last_modified_date;
        this.last_modified = last_modified;
        this.file_last_access_time_date = file_last_access_time_date;
        this.file_last_access_time = file_last_access_time;
        this.file_access_frequency_num = file_access_frequency_num;
        this.file_access_frequency = file_access_frequency;
        this.file_format = file_format;
        this.directory_path = directory_path;
        this.currentPosition = new Position(TiersMovements.NONE);
        this.evict = false;
        this.misses = 0;
        
    }
	
	public FileInfo(String pathname, long size, SizeEnum fileSize, String directory_path){
        this.pathname = pathname;
        this.size = size;
        setSize(size);
        this.last_modified_date = System.currentTimeMillis();
        this.last_modified = LastModifiedEnum.RECENTLY;
        this.file_last_access_time_date = System.currentTimeMillis();
        this.file_last_access_time = FileLastAccessTimeEnum.RECENT;
        this.file_access_frequency_num = 1;
        this.file_access_frequency = FileAccessFrequencyEnum.NOT_FAMOUS;
        this.file_format = new TypeInfo(pathname);
        this.directory_path = new DirectoryInfo(directory_path); 
        this.currentPosition = new Position(TiersMovements.NONE);
        this.evict = false;
        this.misses = 0;
        
    }
	
	public FileInfo(String pathname, long size, TypeInfo type, DirectoryInfo directory_path){
        this.pathname = pathname;
        this.size = size;
        setSize(size);
        this.last_modified_date = System.currentTimeMillis();
        setLast_modified();
        this.file_last_access_time_date = System.currentTimeMillis();
        setFile_last_access_time();
        this.file_access_frequency_num = 1;
        setFile_access_frequency(file_access_frequency_num);
        this.file_format = type;
        this.directory_path = directory_path; 
        this.currentPosition = new Position(TiersMovements.NONE);
        this.evict = false;
        this.misses = 0;
        
    }
	
	/**
	 * METHODS
	 */
	
	public void setPathname(String pathname) {
		this.pathname = pathname;
	}
	
	public String getPathname() {
		return this.pathname;
	}
	
	public void setSize(long size) {
		  
		this.size = size;
	
		if(this.size < 1000) {
			this.fileSize = SizeEnum.SMALL;
		}
		else if(this.size < 1000000) {
			this.fileSize = SizeEnum.MEDIUM;
		}	 
		else if(this.size < 1000000000) {	 
			this.fileSize = SizeEnum.LARGE;
		}	 
		else
			this.fileSize = SizeEnum.HUGE;
	}
	
	public long getSize() {
		return size;
	}
	
	public SizeEnum getFileSize() {
		return this.fileSize;
	}
	
	public void setLast_modified(long date) {
		long diff = date - this.last_modified_date;
		//update
		this.last_modified_date = date;
			  
		if(diff < 60) {
			this.last_modified = LastModifiedEnum.RECENTLY;	
		}
		else if(diff < 3600) {
			this.last_modified = LastModifiedEnum.LESS_RECENTLY;
		}
		else if(diff < 86400) {
			this.last_modified = LastModifiedEnum.MUCH_LESS_RECENTLY;
		}
		else
			this.last_modified = LastModifiedEnum.OLD;
	}
	
	public void setLast_modified() {
		long diff = System.currentTimeMillis() - this.last_modified_date;

		//update
		this.last_modified_date = System.currentTimeMillis();
	  
		if(diff < 60) {
			this.last_modified = LastModifiedEnum.RECENTLY;	
		} 
		else if(diff < 3600) {
			this.last_modified = LastModifiedEnum.LESS_RECENTLY;
		}
		else if(diff < 86400) {
			this.last_modified = LastModifiedEnum.MUCH_LESS_RECENTLY;
		}
		else
			this.last_modified = LastModifiedEnum.OLD;
	}
	
	public long getLast_modified_date() {
		return last_modified_date;
	}
	
	public LastModifiedEnum getLast_modified() {
		return this.last_modified;
	}
	
	public void setFile_last_access_time() {
		long diff = System.currentTimeMillis() - this.file_last_access_time_date;
		
		//update	
		this.file_last_access_time_date = System.currentTimeMillis();
	  
		if(diff < 60) {
			this.file_last_access_time = FileLastAccessTimeEnum.RECENT;	
		} 
		else if(diff < 3600) {
			this.file_last_access_time = FileLastAccessTimeEnum.LESS_RECENT;
		}
		else if(diff < 86400) {
			this.file_last_access_time = FileLastAccessTimeEnum.MUCH_LESS_RECENT;
		}
		else
			this.file_last_access_time = FileLastAccessTimeEnum.OLD;
	}
	
	public long getFile_last_access_time_date() {
		return file_last_access_time_date;
	}
	
	public FileLastAccessTimeEnum getFile_last_access_time() {
		return this.file_last_access_time; 
	}
	
	public void setFile_access_frequency(int file_access_frequency_num) {		
		this.file_access_frequency_num = file_access_frequency_num;
		
		if(this.file_access_frequency_num > 20) 
			this.file_access_frequency = FileAccessFrequencyEnum.FAMOUS;		
		else if(this.file_access_frequency_num >= 6)
			this.file_access_frequency = FileAccessFrequencyEnum.ENOUGH_FAMOUS;
		else if(this.file_access_frequency_num >= 1)		
			this.file_access_frequency = FileAccessFrequencyEnum.LESS_FAMOUS;
		else		
			this.file_access_frequency = FileAccessFrequencyEnum.NOT_FAMOUS;
	}		
	
	public void incrFile_access_frequency() {
		this.file_access_frequency_num++;
		
		//update file access frequency
		setFile_access_frequency(this.file_access_frequency_num);
	}
	
	public int getFile_access_frequency_num() {
		return this.file_access_frequency_num;
	}
	
	public FileAccessFrequencyEnum getFile_access_frequency() {
		return this.file_access_frequency; 
	}
	
	public void setFile_format(String filename) {	
			this.file_format = new TypeInfo(filename);
	}
	
	public TypeInfo getFile_format() {
		return this.file_format; 
	}
	
	public void setDirectory_path(String directory_path) {
		this.directory_path = new DirectoryInfo(directory_path); 
	}
	
	public DirectoryInfo getDirectory_path() {
		return this.directory_path; 
	}
	
	public void setCurrentPosition(TiersMovements tier) {
		this.currentPosition.setPosition(tier);
	}
	
	public Position getCurrentPosition() {
	 return this.currentPosition;
	}
	
	public boolean getEvict() {
		return evict;
	}
	
	public void setEvict(boolean decision) {
		this.evict = decision;
	}
	
	public int getMisses() {
		return misses;
	}
	
	public void setMisses(int v) {
		this.misses = v;
	}
	
	public void incrMisses() {
		this.misses++;
	}
	
	 public void print() {
		 logger.info(new Timestamp(System.currentTimeMillis()) + ":: pathname = {}, size = {}, fileSize = {}, last_modified_date = {}, last_modified = {}, file_last_access_time_date = {}, file_last_access_time = {}, file_access_frequency_num = {}, file_access_frequency = {}, file_format = {}, directory_path = {}, currentPosition = {}", pathname, size, fileSize, last_modified_date, last_modified, file_last_access_time_date, file_last_access_time, file_access_frequency_num, file_access_frequency, file_format.getFile_type(), directory_path.getName_directory(), currentPosition.getPosition()); 
	 }
	
	 public String toString() {
		return "\npathname = " + pathname + 
				"\nsize = " + size + 
				"\nfileSize = " + fileSize +
				"\nlast_modified_date = " + last_modified_date + 
				"\nlast_modified = " + last_modified + 
				"\nfile_last_access_time_date = " + file_last_access_time_date + 
				"\nfile_last_access_time = " + file_last_access_time + 
				"\nfile_access_frequency_num = " + file_access_frequency_num + 
				"\nfile_access_frequency = " + file_access_frequency + 
				"\nfile_format = " + file_format.getFile_type() + 
				"\ndirectory_path = " + directory_path.getName_directory() + 
				"\ncurrentPosition = " + currentPosition.getPosition() +
				"\nevict = " + evict;
	 } 
	   
	 public boolean equals(FileInfo f){
		 if(f != null){
			   if(pathname.equals(f.getPathname()))
				   return true;
			   else
				   return false;
		   }
		   return false;
	 }
	 
	 public int hashCode() {
		 return pathname.hashCode();
	 }
}