package RL.QMatrix;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import RL.QMatrix.TypeInfo.FileFormatEnum;
import RL.QMatrix.AdmissionStrategy.AdmissionAgent;
import RL.QMatrix.AdmissionStrategy.Position;
import RL.QMatrix.AdmissionStrategy.TiersMovements;
import RL.QMatrix.ReplacementStrategy.AccessBasedList;
import RL.QMatrix.ReplacementStrategy.ReplacementAgent;

/**
 * This class represents the Cache Manager (interface/interconnection with SMACC
 * for ML-SMACC) in the storage system with all created file objects.
 *
 * @author Kakoulli Elena
 */

public class CacheManager {

	private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);

	// Management of files, types, directories
	private HashMap<FileFormatEnum, TypeInfo> types; // keep a list with all type formats exist
	private HashMap<String, DirectoryInfo> directories; // keep a list with all directories exist
	protected ConcurrentHashMap<String, FileInfo> files;// keep a list with all files exist
	protected HashMap<String, FileInfo> filesDeleted; // keep a list with all files deleted exist
	protected Map<String, Integer> fileToevict; // keep the last selected to evict file
	
	// RL Agents (Admission & Replacement/Eviction)
	private AdmissionAgent AA;
	private ReplacementAgent RA;

	private int updates; // keep the count of updates to cache/cloud system
	private int maxUpdatesNum; // maximum number of updates to do an update of Qvalues
	private int maxDecisions; // maximum number of decisions then delete Q-entry
	private AtomicLong memFilesSize; // current size of all cached files (@MEM tier)
	private AtomicLong diskFilesSize; // current size of all cached files (@DISK tier)

	private int counterToPrintQs;
	
	// LRU Lists of files
	private int maxLRU; // parameter for #files from lru list
	private AccessBasedList<FileInfo> filesLRU_atMEM; // LRU list for files in memory
	private AccessBasedList<FileInfo> filesLRU_atSSD; // LRU list for files in ssd
	private AccessBasedList<FileInfo> filesLRU_atDISK; // LRU list for files in disk

	private static CacheManager CMInstance = null;

	private final Object lockAA = new Object();
	private final Object lockRA = new Object();

	// CONSTRUCTORS
	/*
	 * Parameters: 
	 * 1. tiersMovements = possible positions to move a file 
	 * 2. *initialQs = initial value for Q-values in Q-table 
	 * 3. maxUpdatesNum = maximum number of updates to do an update of Qvalues 
	 * 4. maxDecisions = maximum number of decisions then delete Q-entry 
	 * 5. maxLRU = parameter for max number of files from lru list 
	 * 6. alpha = alpha value for Qmodel 
	 * 7. gamma = gamma value for Qmodel 
	 * 8. memThresPerc = percentage of mem capacity to directly cache in mem
	 * 9. memcapacity = memory capacity
	 * 
	 */
	private CacheManager(int tiersMovements, double admissionInitialQs[], double replacementInitialQs[], int maxUpdatesNum, int maxDecisions, int maxLRU,
			double alpha, double gamma, long memThresPerc, long memCapacity, long diskThresPerc, long diskCapacity) {

		this.maxUpdatesNum = maxUpdatesNum;
		this.maxDecisions = maxDecisions;
		this.maxLRU = maxLRU;
		this.AA = new AdmissionAgent(tiersMovements, admissionInitialQs, alpha, gamma, (long) (memThresPerc * memCapacity / 100), (long) (diskThresPerc * diskCapacity / 100));
		this.RA = new ReplacementAgent(replacementInitialQs, alpha, gamma);
		this.filesLRU_atMEM = new AccessBasedList<>();
		this.filesLRU_atSSD = new AccessBasedList<>();
		this.filesLRU_atDISK = new AccessBasedList<>();

		this.types = new HashMap<FileFormatEnum, TypeInfo>();
		this.directories = new HashMap<String, DirectoryInfo>();
		this.files = new ConcurrentHashMap<String, FileInfo>();
		this.filesDeleted = new HashMap<String, FileInfo>();
		this.updates = 0;
        this.fileToevict = new HashMap<>();

		this.memFilesSize = new AtomicLong(0);
		this.diskFilesSize = new AtomicLong(0);
		this.counterToPrintQs = 0;
	}

	public static boolean isCacheManagerCreated() {
		return CMInstance != null;
	}

	public static CacheManager getCacheManager(int tiersMovements, double admissionInitialQs[], double replacementInitialQs[], int maxUpdatesNum, int maxDecisions,
			int maxLRU, double alpha, double gamma, long memThresPerc, long memCapacity, long diskThresPerc, long diskCapacity) {
		if (CMInstance == null) {
			CMInstance = new CacheManager(tiersMovements, admissionInitialQs, replacementInitialQs, maxUpdatesNum, maxDecisions, maxLRU, alpha, gamma,
					memThresPerc, memCapacity, diskThresPerc, diskCapacity);
		}
		return CMInstance;
	}

	/*
	 * API - REQUEST PROCESSES:
	 *
	 * 1. Admission strategy 2. Replacement strategy (Full Cache)
	 * 
	 * 
	 */

	public TiersMovements cacheRequestAdmission(String pathname, long filesize, boolean forRead) {

		/*
		 * Admission: 1. Create new FileInfo object - new state if not exist Needed
		 * parameters-fields: - String key [ name of the file ] - long size [ bytes ] -
		 * TypeInfo file_format [ FileFormatEnum: TEXT/IMAGE/VIDEO/BINARY ] -
		 * DirectoryInfo directory_path [ part of namefile - directory path ]
		 *
		 * 2. Need to check type and directory of file (exist or not)
		 * 
		 */

		FileInfo file = getFile(pathname, filesize);
		TiersMovements tierMovement = admission(file, forRead);

		return tierMovement;
	}

	public FileInfo cacheRequestReplacement(Tier tier) {

		/*
		 * Replacement: 1. Must select a file to evict from LRU list
		 * 
		 */

		FileInfo file = cacheReplacement(tier); // Cache Replacement
		logger.info(new Timestamp(System.currentTimeMillis()) + ":: [CM] - cacheRequestReplacement for location: " + tier.name());
		return file;
	}

	public synchronized void cacheUpdate(CacheUpdate update, String pathname, long filesize, TiersMovements tiers,
			long last_modified_date) {
		/*
		 * API - UPDATES PROCESSES:
		 *
		 * 1. Create file (update and for move to another tier) 
		 * 2. Read File 
		 * 3. Write File
		 * 4. Delete File
		 * 
		 */

		FileInfo file;

		switch (update.ordinal()) {

		case 0:
			logger.info(new Timestamp(System.currentTimeMillis())
					+ ":: ==> CacheManager received CREATE request for file < " + pathname + " > in " + tiers.toString()
					+ " with file size: " + filesize + " memFilesSize: " + this.memFilesSize.get());

			// if the file exists but not in the requested tier add it in the tier
			if (files.get(pathname) != null && files.get(pathname).getCurrentPosition().getPosition() != tiers) {

				if (!files.get(pathname).getCurrentPosition().containsTier(tiers)) {
	
					logger.info(new Timestamp(System.currentTimeMillis()) + ":: The file < " + pathname
							+ " > exists in < " + files.get(pathname).getCurrentPosition() + " >.");

					if (tiers == TiersMovements.MEM) 
						this.memFilesSize.addAndGet(filesize);
				
					else if (tiers == TiersMovements.DISK) 
						this.diskFilesSize.addAndGet(filesize);
					
					// Change currentPosition of the file (add requested tier)
					file = files.get(pathname);
					TiersMovements newPos = file.getCurrentPosition().addTier(tiers);
					files.get(pathname).setCurrentPosition(newPos);
		
					logger.info(new Timestamp(System.currentTimeMillis()) + ":: New location for the file < " + pathname
							+ " >: " + files.get(pathname).getCurrentPosition());
	
					// Be sure for filesize consistency
					if (files.get(pathname).getSize() != filesize)
						files.get(pathname).setSize(filesize);	

				} else { // if the file exists in the requested tier
					
					// update the capacity of memory/disk if exists there
					if (files.get(pathname).getCurrentPosition().containsTier(TiersMovements.MEM) && tiers == TiersMovements.MEM && filesize!=files.get(pathname).getSize()) {
						this.memFilesSize.addAndGet(-files.get(pathname).getSize());
						this.memFilesSize.addAndGet(filesize);
					}
					else if(files.get(pathname).getCurrentPosition().containsTier(TiersMovements.DISK) && tiers == TiersMovements.DISK && filesize!=files.get(pathname).getSize()) {
						this.diskFilesSize.addAndGet(-files.get(pathname).getSize());
						this.diskFilesSize.addAndGet(filesize);
					}
					
					if (files.get(pathname).getSize() != filesize) {
						
						// be sure for filesize consistency
						files.get(pathname).setSize(filesize);
					}
				}
			} else if (files.get(pathname) == null) { // if the file does not exist, create the file in this tier
				createFile(pathname, filesize);
				files.get(pathname).setCurrentPosition(tiers);

				if (tiers == TiersMovements.MEM) 
					this.memFilesSize.addAndGet(filesize);
				else if (tiers == TiersMovements.DISK) 
					this.diskFilesSize.addAndGet(filesize);
				
			}
			// below is when the file exists but check if the size is the same - do we need this???
			else {
				// update the capacity of memory/disk if exists there
				if (files.get(pathname).getCurrentPosition().containsTier(TiersMovements.MEM) && tiers == TiersMovements.MEM && filesize!=files.get(pathname).getSize()) {
					this.memFilesSize.addAndGet(-files.get(pathname).getSize());
					this.memFilesSize.addAndGet(filesize);
				}
				else if (files.get(pathname).getCurrentPosition().containsTier(TiersMovements.DISK) && tiers == TiersMovements.DISK && filesize!=files.get(pathname).getSize()) {
						this.diskFilesSize.addAndGet(-files.get(pathname).getSize());
						this.diskFilesSize.addAndGet(filesize);
				}

				// Be sure for filesize consistency
				if (files.get(pathname).getSize() != filesize) {
					
					files.get(pathname).setSize(filesize);
				}
			}

			files.get(pathname).setLast_modified(last_modified_date);
			addFileToLRU(files.get(pathname), tiers);

			break;

		case 1: 
			logger.info(
					new Timestamp(System.currentTimeMillis()) + ":: ==> CacheManager received READ request for file < "
							+ pathname + " in the tier < " + tiers + " > with filesize: "+ filesize);
			// if the file exists check the tier for access
			if (files.get(pathname) != null) {
				if (files.get(pathname).getCurrentPosition().getPosition() != tiers
						&& files.get(pathname).getCurrentPosition().containsTier(tiers) == false) {
				
					if(files.get(pathname).getCurrentPosition().getPosition()==TiersMovements.NONE) {
					files.get(pathname).getCurrentPosition().setPosition(files.get(pathname).getCurrentPosition().addTier(tiers));
					addFileToLRU(files.get(pathname), tiers);
					}
					else {
						logger.info(new Timestamp(System.currentTimeMillis())
								+ ":: CacheManager received READ request for file < " + pathname
								+ " > but does not exist in the tier < " + tiers + " >");
						
						//just for test
						System.err.println(new Timestamp(System.currentTimeMillis())
								+ ":: CacheManager received READ request for file < " + pathname
								+ " > but does not exist in the tier < " + tiers + " >");
					}
				} else {
					files.get(pathname).incrFile_access_frequency();
					files.get(pathname).setFile_last_access_time();
					files.get(pathname).getDirectory_path().incr_access_frequency();
					files.get(pathname).getDirectory_path().set_last_access_time();
					files.get(pathname).getFile_format().incr_access_frequency();
					files.get(pathname).getFile_format().set_fileTypeLastAccessTime();

					filesLRU_atMEM.accessItem(files.get(pathname));
					filesLRU_atSSD.accessItem(files.get(pathname));
					filesLRU_atDISK.accessItem(files.get(pathname));
				}
			} else {
				logger.error(new Timestamp(System.currentTimeMillis()) + ":: [CacheManager] - READ request: The file < "
						+ pathname + " > does not exist!");
			}
			updates++;
			break;

		case 2: 
			logger.info(new Timestamp(System.currentTimeMillis())
					+ ":: ==> CacheManager received WRITE request for file < " + pathname + " > in " + tiers + " .");
			// if the file exists check the tier for update
			if (files.get(pathname) != null) {
				if (files.get(pathname).getCurrentPosition().getPosition() != tiers
						&& files.get(pathname).getCurrentPosition().containsTier(tiers) == false) {

					if (files.get(pathname).getCurrentPosition().getPosition() == TiersMovements.NONE) {

						files.get(pathname).getCurrentPosition()
								.setPosition(files.get(pathname).getCurrentPosition().addTier(tiers));
						files.get(pathname).setLast_modified();
						addFileToLRU(files.get(pathname), tiers);

						if (files.get(pathname).getSize() != filesize) {
							files.get(pathname).setSize(filesize);
						}

						// keep memory size updated
						if (tiers == TiersMovements.MEM)
							this.memFilesSize.addAndGet(filesize);
						else if (tiers == TiersMovements.DISK)
							this.diskFilesSize.addAndGet(filesize);

					} else {
						logger.info(new Timestamp(System.currentTimeMillis())
								+ ":: CacheManager received WRITE request for file < " + pathname
								+ " > but does not exist in the tier < " + tiers + " >");

						// just for test
						System.err.println("CacheManager received WRITE request for file < " + pathname
								+ " > but does not exist in the tier < " + tiers + " >");
					}
				} else {
					// on write update maybe the size changed so keep it consistent
					// keep memory size updated
					if (files.get(pathname).getCurrentPosition().containsTier(TiersMovements.MEM) && tiers == TiersMovements.MEM && filesize!=files.get(pathname).getSize()) {
						this.memFilesSize.addAndGet(-files.get(pathname).getSize());
						this.memFilesSize.addAndGet(filesize);
					}
					else if (files.get(pathname).getCurrentPosition().containsTier(TiersMovements.DISK) && tiers == TiersMovements.DISK && filesize!=files.get(pathname).getSize()) {
						this.diskFilesSize.addAndGet(-files.get(pathname).getSize());
						this.diskFilesSize.addAndGet(filesize);
					}
					
					if (files.get(pathname).getSize() != filesize) {
						files.get(pathname).setSize(filesize);
					}
					files.get(pathname).setLast_modified();
					files.get(pathname).getDirectory_path().set_last_access_time();
					files.get(pathname).getFile_format().set_fileTypeLastAccessTime();

					filesLRU_atMEM.accessItem(files.get(pathname));
					filesLRU_atSSD.accessItem(files.get(pathname));
					filesLRU_atDISK.accessItem(files.get(pathname));
	
				}
			} else {
				logger.error(new Timestamp(System.currentTimeMillis())
						+ ":: [CacheManager] - WRITE request: The file < " + pathname + " > does not exist!");
			}
			updates++;
			break;

		case 3:
			logger.info(new Timestamp(System.currentTimeMillis())
					+ ":: ==> CacheManager received DELETE request for file < " + pathname + " > from "
					+ tiers.toString() + " with file size: " + filesize + " memFilesSize: " + this.memFilesSize.get());

			// must check the tiers of the file (delete or keep it to the remaining tiers)
			if (files.containsKey(pathname)) {
				// update memory capacity size (supposed the deletion request comes only from one tier)
				if (tiers == TiersMovements.MEM) {
					this.memFilesSize.addAndGet(-filesize);
				}
				else if(tiers == TiersMovements.DISK){
					this.diskFilesSize.addAndGet(-filesize);
				}
				deleteFileFromLRU(files.get(pathname), tiers);

				file = files.get(pathname);
				Position tiersIncluded = new Position(tiers);
				if (tiersIncluded.getPosition() == file.getCurrentPosition().getPosition()) { // suppose is only one
																								// tier
					filesDeleted.put(pathname, file);
					files.get(pathname).setCurrentPosition(TiersMovements.NONE);
					
					logger.info(new Timestamp(System.currentTimeMillis()) + ":: [CacheManager] - Delete request: The file < " + pathname + " > deleted successfully!");
				} else {
					// delete file for the requested tier and keep to others
					// just change currentPosition of the file
					TiersMovements newPos = file.getCurrentPosition().excludeTier(tiers);
					if (newPos == null) { // check if is null then the file does not exist in the requested tier
						System.err.println("The file < " + pathname + " > does not exist in the location: " + tiers);
						logger.error(new Timestamp(System.currentTimeMillis()) + ":: [CacheManager] - Delete request: The file < " + pathname
								+ " > does not exist in the location: " + tiers);
					} else {
						files.get(pathname).setCurrentPosition(newPos);

						logger.info(new Timestamp(System.currentTimeMillis()) + ":: [CacheManager] - Delete request: The file < " + pathname
								+ " > deleted successfully from < " + tiers + " > and exists to remaining tiers <"
								+ files.get(pathname).getCurrentPosition().toString() + " >.");
					}
				}
			} else {
				System.err.println("The file < " + pathname + " > does not exist!");
				logger.error(new Timestamp(System.currentTimeMillis()) + ":: [CacheManager] - Delete request: The file < " + pathname + " > does not exist!");
			}

			break;

		default: 
			logger.error(new Timestamp(System.currentTimeMillis()) + ":: CacheManager received a wrong cache update request!");
		}
		
		checkForUpdate();
				
	}

	public void checkForUpdate() {

		if (updates >= maxUpdatesNum) {
            updates = 0; // reset counter for updates

            new Thread(new Runnable() {
                @Override
                public void run() {

                	synchronized (lockAA) {
						AA.updateStrategy();
						for (Decision a : AA.getDecisions()) {
							if (a.updates == maxDecisions) {
								logger.info(new Timestamp(System.currentTimeMillis()) + ":: Deletion of an admission decision:" + a.toString());
								AA.getDecisions().remove(a);
							}
						}
					}
					
					
					synchronized (lockRA) {
						RA.updateStrategy();

						for (Decision r : RA.getDecisions()) {
							if (r.updates == maxDecisions) {
								logger.info(new Timestamp(System.currentTimeMillis()) + ":: Deletion of an admission decision:" + r.toString());
								RA.getDecisions().remove(r);
							}
						}
						
					}

					logger.info(new Timestamp(System.currentTimeMillis()) + ":: Done UpdateStrategy for Qvalues - rewards/penalties.");

                }
			}
            ).start();
        }
	}

	public FileInfo createFile(String pathname, long size) {
		if (!files.containsKey(pathname)) {
			FileInfo file = new FileInfo(pathname, size, checkType(pathname), checkDirectory(pathname));
			files.put(file.getPathname(), file);
	
			return file;
		} else { // if file exists
			System.err.println(new Timestamp(System.currentTimeMillis()) + ":: The file with pathname << " + pathname + " >> exists!!!");
			logger.error(new Timestamp(System.currentTimeMillis()) + ":: The file with pathname << " + pathname + " >> exists!!!");
			return files.get(pathname);
		}
	}

	public FileInfo getFile(String pathname, long filesize) {
		if (!files.containsKey(pathname))
			createFile(pathname, filesize);
		return files.get(pathname);
	}

	public void incrFileMisses(String pathname, long filesize) {
	
		if (files.containsKey(pathname)) {
			if (files.get(pathname).getSize() != filesize) { // update size
				files.get(pathname).setSize(filesize);
			}
			files.get(pathname).incrMisses();

		} else { // if file does not exist
			logger.info(new Timestamp(System.currentTimeMillis()) + ":: The file with pathname << " + pathname + " >> does not exist!!!");
		}

	}

	public TiersMovements admission(FileInfo file, boolean forRead) {
		State state = new State(file);
		TiersMovements tiers = TiersMovements.valueOf(AA.act(state, this.memFilesSize, this.diskFilesSize, forRead));
		return tiers;
	}

	public FileInfo cacheReplacement(Tier tier) {
	
		Iterator<FileInfo> iter = getLRUIterator(tier);

		int counter = 0;
		FileInfo lruInTier = null;
		
		while (iter.hasNext() && counter < maxLRU) {
			State state = new State(iter.next());
			int action = RA.act(state, this.memFilesSize, this.diskFilesSize);
			if (action != 0) {
				// Store pathname & action for next downgrade action
				this.fileToevict.put(state.fileInfo.getPathname(), action);

				// return the file to evict or evict to lower tier
				return state.fileInfo;
			}
			// if there is not a file to evict return the LRU file in the requested tier
			if (lruInTier == null)
				lruInTier = state.fileInfo;

			counter++;
		}

		if (lruInTier == null) {
			logger.error(new Timestamp(System.currentTimeMillis()) + ":: There is not any file to evict and return null!");
		} 
		else {
			logger.info(new Timestamp(System.currentTimeMillis()) + ":: getLRUItem evicted file < " + lruInTier.getPathname() + " >");
		}
		return lruInTier; // if there is not a file to evict return null

	}

	public boolean fileDeleteOrDowngrade(String pathname) {
		// Delete : 0
		// Downgrade : 1
		if (files.containsKey(pathname)) {

			State state = new State(files.get(pathname));
			int decision;

			if (this.fileToevict != null) {
				if (this.fileToevict.containsKey(pathname)) {
					decision = this.fileToevict.get(pathname);
					this.fileToevict.remove(pathname);
				}

				else
					decision = RA.act(state, this.memFilesSize, this.diskFilesSize);
			} else
				decision = RA.act(state, this.memFilesSize, this.diskFilesSize);

			if (decision == 2) { // if action == EVICT_TO_NONE
				files.get(pathname).setEvict(true);
                				return false;
			} else { // if action == NOT_EVICT OR EVICT_TO_LOWER_TIER
				if (decision == 1 || decision == 0) // if action == EVICT_TO_LOWER_TIER (keep info for update create)
					files.get(pathname).setEvict(true);
                				return true;
			}
		}
		// if file does not exist
		logger.info(new Timestamp(System.currentTimeMillis()) + ":: [CM]-fileDeleteOrDowngrade: The file with pathname << " + pathname + " >> does not exist!!!");
        		return true;
	}

	public DirectoryInfo checkDirectory(String pathname) {
		DirectoryInfo dir = new DirectoryInfo(pathname);
		if (!directories.containsKey(dir.getName_directory()))
			directories.put(dir.getName_directory(), dir);
		// increase count of files contains
		directories.get(dir.getName_directory()).incrFiles();

		return directories.get(dir.getName_directory());
	}

	public TypeInfo checkType(String pathname) {
		TypeInfo type = new TypeInfo(pathname);
		if (!types.containsKey(type.getFile_type()))
			types.put(type.getFile_type(), type);
		// increase count of files contains
		types.get(type.getFile_type()).incrFiles();

		return types.get(type.getFile_type());
	}

	public void deleteFileFromLRU(FileInfo file, TiersMovements tiers) {
		if (tiers == TiersMovements.DISK || tiers == TiersMovements.DISK_MEM || tiers == TiersMovements.DISK_SSD
				|| tiers == TiersMovements.DISK_SSD_MEM) {
			filesLRU_atDISK.deleteItem(file);
		}
		if (tiers == TiersMovements.MEM || tiers == TiersMovements.DISK_MEM || tiers == TiersMovements.SSD_MEM
				|| tiers == TiersMovements.DISK_SSD_MEM) {
			filesLRU_atMEM.deleteItem(file);
		}
		if (tiers == TiersMovements.SSD || tiers == TiersMovements.DISK_SSD || tiers == TiersMovements.SSD_MEM
				|| tiers == TiersMovements.DISK_SSD_MEM) {
			filesLRU_atSSD.deleteItem(file);
		}
	}

	public void addFileToLRU(FileInfo file, TiersMovements tiers) {
		if (tiers == TiersMovements.DISK || tiers == TiersMovements.DISK_MEM || tiers == TiersMovements.DISK_SSD
				|| tiers == TiersMovements.DISK_SSD_MEM) {
			filesLRU_atDISK.add(file);
		}
		if (tiers == TiersMovements.MEM || tiers == TiersMovements.DISK_MEM || tiers == TiersMovements.SSD_MEM
				|| tiers == TiersMovements.DISK_SSD_MEM) {
			filesLRU_atMEM.add(file);
		}
		if (tiers == TiersMovements.SSD || tiers == TiersMovements.DISK_SSD || tiers == TiersMovements.SSD_MEM
				|| tiers == TiersMovements.DISK_SSD_MEM) {
			filesLRU_atSSD.add(file);
		}
	}

	public Iterator<FileInfo> getLRUIterator(Tier tier) {
		Iterator<FileInfo> iter = null;

		if (tier == Tier.DISK) {
			return filesLRU_atDISK.getLRUItemIterator();
		} else if (tier == Tier.MEM) {
			return filesLRU_atMEM.getLRUItemIterator();
		} else if (tier == Tier.SSD) {
			return filesLRU_atSSD.getLRUItemIterator();
		}
		return iter;
	}

	public void deleteFile(FileInfo file) {
        // find the file and delete it from hashmap table
        files.remove(file.getPathname());

		// delete it from type and directory lists
		FileFormatEnum type = file.getFile_format().getFile_type();
		if (types.get(type).getFiles() == 1) // is the only file with this type must delete the type???
			types.remove(type);
		else
			types.get(type).decrFiles();

		String directory = file.getDirectory_path().getName_directory();
		if (directories.get(directory).getFiles() == 1) // is the only file in this directory must delete the directory???
			directories.remove(directory);
		else
			directories.get(directory).decrFiles();
	}

	public HashMap<FileFormatEnum, TypeInfo> getTypes() {
		return types;
	}

	public HashMap<String, DirectoryInfo> getDirectories() {
		return directories;
	}

	public void printFileTypes() {
		System.out.println("\n*** Types of files in the system ***\n");
		for (Map.Entry<FileFormatEnum, TypeInfo> type : types.entrySet()) {
			System.out.println(type.getValue().toString());
			System.out.println();
			System.out.println("Files with type < " + type.getValue().getFile_type().name() + " >:");
			for (Map.Entry<String, FileInfo> file : files.entrySet()) {
				if (file.getValue().getFile_format().getFile_type().ordinal() == type.getValue().getFile_type()
						.ordinal()) {
					System.out.println(file.getValue().toString());
				}
			}
		}
	}

	public void printDirectories() {
		System.out.println("\n*** Directories of files in the system ***\n");
		for (Map.Entry<String, DirectoryInfo> dir : directories.entrySet()) {
			System.out.println(dir.getValue().toString());
			System.out.println();
			System.out.println("Files in directory < " + dir.getValue().getName_directory() + " >:");
			for (Map.Entry<String, FileInfo> file : files.entrySet()) {
				if (file.getValue().getDirectory_path().getName_directory() == dir.getValue().getName_directory()) {
					System.out.println(file.getValue().toString());
				}
			}

		}
	}

	public void printFiles() {
		System.out.println("\n*** Files in the system ***");
		for (Map.Entry<String, FileInfo> file : files.entrySet())
			System.out.println(file.getValue().toString());
		// System.out.println("***********************");
	}

	public void printLRUFiles() {
		System.out.println("\n*** LRU Files in the system at MEMORY ***");
		Iterator<FileInfo> iterateM = filesLRU_atMEM.getLRUItemIterator();
		while (iterateM.hasNext())
			System.out.println(iterateM.next().toString());

		// System.out.println("***********************");
		System.out.println("\n*** LRU Files in the system at SSD ***");
		Iterator<FileInfo> iterateD = filesLRU_atSSD.getLRUItemIterator();
		while (iterateD.hasNext())
			System.out.println(iterateD.next().toString());

		System.out.println("\n*** LRU Files in the system at DISK ***");
		Iterator<FileInfo> iterate = filesLRU_atMEM.getLRUItemIterator();
		while (iterate.hasNext())
			System.out.println(iterate.next().toString());

	}

	public void printCMstatus() {
		printFiles();
		printFileTypes();
		printDirectories();
		printLRUFiles();
	}
}
