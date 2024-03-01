package RL.QMatrix;

public enum CacheUpdate {
	CREATE, 
	READ,   //get -> create if not exists 			
	WRITE,  //put -> create if not exists
	DELETΕ
}
