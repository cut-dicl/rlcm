package RL.QMatrix;

import java.time.Instant;

/**
 * This class represents a type of objects in storage system 
 * to help RL technique
 * 
 * @author Kakoulli Elena
 */

public class TypeInfo {
	
	enum FileFormatEnum {
		TEXT,   
		IMAGE,  
		VIDEO,  
		BINARY 	
	}
	
	enum FileTypeLastAccessTimeEnum {
		  RECENT,
		  LESS_RECENT,
		  MUCH_LESS_RECENT,
		  OLD
		}
	
	enum FileTypeAccessFrequencyEnum {
		NOT_FAMOUS, 	//0-1 times
		LESS_FAMOUS,	//2-5 times
		ENOUGH_FAMOUS,	//6-20 times
		FAMOUS
		}
	
	protected FileFormatEnum file_type;
	protected long fileTypeLastAccessTime;
	protected int fileTypeAccessFrequency;
	protected FileTypeLastAccessTimeEnum file_type_last_access_time;
	protected FileTypeAccessFrequencyEnum file_type_access_frequency;
	protected int files = 0; //keep the count of files
   
    /**
     * 	CONSTRUCTORS
     */
    
    
    /**
     * 
     * @param file_type
     * @param file_type_last_access_time
     * @param file_type_access_frequency
     */
    public TypeInfo(FileFormatEnum file_type, long fileTypeLastAccessTime, int fileTypeAccessFrequency, FileTypeLastAccessTimeEnum file_type_last_access_time, FileTypeAccessFrequencyEnum file_type_access_frequency) {
    	this.file_type = file_type;
    	this.fileTypeLastAccessTime = fileTypeLastAccessTime;
    	this.fileTypeAccessFrequency = fileTypeAccessFrequency;
    	this.file_type_last_access_time = file_type_last_access_time;
    	this.file_type_access_frequency = file_type_access_frequency;
    	this.files = 0;
    }
	
    /**
     * 
     * @param file_type
     */
    public TypeInfo(FileFormatEnum file_type) {
    	Instant instant = Instant.now();
    	this.file_type = file_type;
    	this.fileTypeLastAccessTime = instant.getEpochSecond();
    	this.fileTypeAccessFrequency = 1;
    	this.file_type_last_access_time = FileTypeLastAccessTimeEnum.RECENT;
    	this.file_type_access_frequency = FileTypeAccessFrequencyEnum.NOT_FAMOUS;
    	this.files = 0;
    }
    
    /**
     * 
     * @param filename
     */
    public TypeInfo(String filename) {	
    	if(filename.contains(".")) {
    		
		String extension = filename.substring(filename.lastIndexOf(".") + 1);
		/** TEXT
		 ** .doc and .docx - Microsoft Word file.
		 ** .odt - OpenOffice Writer document file.
		 ** .pdf - PDF file.
		 **	.rtf - Rich Text Format.
		 ** .tex - A LaTeX document file.
		 ** .txt - Plain text file.
		 ** .wpd - WordPerfect document.
		 **/

		/** IMAGE - Photo Image file formats
		 * TIF, JPG, PNG, GIF. 
		 */
		
		/** VIDEO
		 * MP4
		 * MOV
		 * WMV
		 * AVI
		 */
		
		/** BINARY
		 * ZIP, TAR, RAR, GZ, JAR.
		 */
		
		if(extension.compareTo("doc") == 0 || extension.compareTo("docx") == 0 || extension.compareTo("odt") == 0 ||
		   extension.compareTo("pdf") == 0 || extension.compareTo("rtf") == 0 || extension.compareTo("tex") == 0 ||
		   extension.compareTo("txt") == 0 || extension.compareTo("wpd") == 0) {
			this.file_type = FileFormatEnum.TEXT;	
		}
		else if(extension.compareTo("tif") == 0 || extension.compareTo("jpg") == 0 || extension.compareTo("png") == 0 ||
				extension.compareTo("gif") == 0) {
			this.file_type = FileFormatEnum.IMAGE;	
		}
		else if(extension.compareTo("mp4") == 0 || extension.compareTo("mov") == 0 || extension.compareTo("wmv") == 0 || 
				extension.compareTo("avi") == 0) {
			this.file_type = FileFormatEnum.VIDEO;	
		}
		else if(extension.compareTo("zip") == 0 || extension.compareTo("tar") == 0 || extension.compareTo("rar") == 0 ||		
				extension.compareTo("gz") == 0 || extension.compareTo("jar") == 0 )
				this.file_type = FileFormatEnum.BINARY;
		else 
			this.file_type = FileFormatEnum.BINARY; 
    	}
    	else
    		this.file_type = FileFormatEnum.BINARY; 
    	
    	this.fileTypeLastAccessTime = System.currentTimeMillis(); 
    	this.fileTypeAccessFrequency = 1;
		this.file_type_last_access_time = FileTypeLastAccessTimeEnum.RECENT;
    	this.file_type_access_frequency = FileTypeAccessFrequencyEnum.NOT_FAMOUS;
    	this.files = 0;
		
	}
    
    /**
     * 	METHODS
     */
    
    /**
     * 
     * @param file_type
     */
    public void setFile_type(FileFormatEnum file_type) {
    	this.file_type = file_type;
    }
    
    /**
     * 
     * @return
     */
    public FileFormatEnum getFile_type() {
    	return this.file_type;
    }
    
    public void set_fileTypeLastAccessTime() {
	 	long diff = System.currentTimeMillis() - this.fileTypeLastAccessTime;
    	
		//update
		this.fileTypeLastAccessTime = System.currentTimeMillis();
		  
	  
		if(diff < 60) {
			this.file_type_last_access_time = FileTypeLastAccessTimeEnum.RECENT;	
		} 
		else if(diff < 3600) {
			this.file_type_last_access_time = FileTypeLastAccessTimeEnum.LESS_RECENT;
		}
		else if(diff < 86400) {
			this.file_type_last_access_time = FileTypeLastAccessTimeEnum.MUCH_LESS_RECENT;
		}
		else
			this.file_type_last_access_time = FileTypeLastAccessTimeEnum.OLD;
	}
	
	public long get_fileTypeLastAccessTime() {
		return fileTypeLastAccessTime;
	}
	
    /**
     * 
     * @param file_type_last_access_time
     */
    public void setFile_type_last_access_time(FileTypeLastAccessTimeEnum file_type_last_access_time) {
    	this.file_type_last_access_time = file_type_last_access_time;
    }
    
    /**
     * 
     * @return
     */
    public FileTypeLastAccessTimeEnum getFile_type_last_access_time() {
    	return this.file_type_last_access_time;
    }
    
    public void set_fileTypeAccessFrequency(int access_frequency_num) {		
		this.fileTypeAccessFrequency = access_frequency_num;
		
		if(this.fileTypeAccessFrequency > 20) 
			this.file_type_access_frequency = FileTypeAccessFrequencyEnum.FAMOUS;		
		else if(this.fileTypeAccessFrequency > 5)
			this.file_type_access_frequency = FileTypeAccessFrequencyEnum.ENOUGH_FAMOUS;
		else if(this.fileTypeAccessFrequency > 2)		
			this.file_type_access_frequency = FileTypeAccessFrequencyEnum.LESS_FAMOUS;
		else		
			this.file_type_access_frequency = FileTypeAccessFrequencyEnum.NOT_FAMOUS;
	}	
	
	public int get_fileTypeAccessFrequency() {
		return this.fileTypeAccessFrequency;
	}
    /**
     * 
     * @param file_type_access_frequency
     */
    public void setFile_type_access_frequency(FileTypeAccessFrequencyEnum file_type_access_frequency) {
    	this.file_type_access_frequency = file_type_access_frequency;
    }
    
    /**
     * 
     * @return
     */
    public FileTypeAccessFrequencyEnum getFile_type_access_frequency() {
    	return this.file_type_access_frequency;
    }
    
    public int getFiles() {
		return files;
	}
	
	public void incrFiles() {
		files++;
	}
	
	public void decrFiles() {
		files--;
	}
	
	public void incr_access_frequency() {
		this.fileTypeAccessFrequency++;
		
		//update file access frequency
		set_fileTypeAccessFrequency(this.fileTypeAccessFrequency);
	}
	
	@Override
	public String toString() {
		return "\nFile Type = " + file_type + 
				"\nType_last_access_time_date = " + fileTypeLastAccessTime + 
				"\nType_last_access_time = " + file_type_last_access_time + 
				"\nType_access_frequency_num = " + fileTypeAccessFrequency + 
				"\nType_access_frequency = " + file_type_access_frequency + 
				"\nNumber_of_files = " + files;
	}
}