package RL.QMatrix.Policies.Eviction;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import RL.QMatrix.CacheManager;
import RL.QMatrix.CacheUpdate;
import RL.QMatrix.FileInfo;
import RL.QMatrix.AdmissionStrategy.Position;
import RL.QMatrix.Policies.RLpoliciesUtils;
import RL.QMatrix.ReplacementStrategy.Actions;
import edu.cut.smacc.configuration.Configuration;
import edu.cut.smacc.server.cache.common.CacheFile;
import edu.cut.smacc.server.cache.common.StoreOptionType;
import edu.cut.smacc.server.cache.disk.DiskFile;
import edu.cut.smacc.server.cache.memory.MemoryFile;
import edu.cut.smacc.server.cache.policy.eviction.item.EvictionItemPolicy;

/**
 * This class represents the Eviction RL-based policy
 * 
 * @author Kakoulli Elena
 */

public class EvictionItemRL implements EvictionItemPolicy {

	private static final Logger logger = LoggerFactory.getLogger(EvictionItemRL.class);

	private boolean CMOwner;
	private CacheManager CM;
	protected HashMap<String, MemoryFile> memFiles;
	protected HashMap<String, DiskFile> diskFiles;

	@Override
	public void onItemAdd(CacheFile file, StoreOptionType tier) {
		if (CMOwner)
			CM.cacheUpdate(CacheUpdate.CREATE, file.getKey(), file.getActualSize(),
					RLpoliciesUtils.convertStoreOptionType(tier), RLpoliciesUtils.getLastModified(file));

		if (tier == StoreOptionType.DISK_ONLY)
			diskFiles.put(file.getKey(), (DiskFile) file);
		else if (tier == StoreOptionType.MEMORY_ONLY)
			memFiles.put(file.getKey(), (MemoryFile) file);
	}

	@Override
	public void onItemNotAdded(CacheFile file, StoreOptionType tier) {
		if (CMOwner)
			CM.incrFileMisses(file.getKey(), file.getActualSize());
	}

	@Override
	public void onItemAccess(CacheFile file, StoreOptionType tier) {
		if (CMOwner)
			CM.cacheUpdate(CacheUpdate.READ, file.getKey(), file.getActualSize(),
					RLpoliciesUtils.convertStoreOptionType(tier), RLpoliciesUtils.getLastModified(file));
	}

	@Override
	public void onItemUpdate(CacheFile file, StoreOptionType tier) {
		if (CMOwner)
			CM.cacheUpdate(CacheUpdate.WRITE, file.getKey(), file.getActualSize(),
					RLpoliciesUtils.convertStoreOptionType(tier), RLpoliciesUtils.getLastModified(file));
	}

	@Override
	public void onItemDelete(CacheFile file, StoreOptionType tier) {
		if (CMOwner)
			CM.cacheUpdate(CacheUpdate.DELETΕ, file.getKey(), file.getTotalSize()/*file.getActualSize()*/,
					RLpoliciesUtils.convertStoreOptionType(tier), RLpoliciesUtils.getLastModified(file));

		if (tier == StoreOptionType.DISK_ONLY)
			diskFiles.remove(file.getKey(), (DiskFile) file);
		else if (tier == StoreOptionType.MEMORY_ONLY)
			memFiles.remove(file.getKey(), (MemoryFile) file);
		
	}

	@Override
	public void reset() {
		diskFiles.clear();
		memFiles.clear();
	}

	@Override
	public void initialize(Configuration conf) {

		Properties prop = RLpoliciesUtils.getCMproperties(conf);

		CMOwner = !CacheManager.isCacheManagerCreated();

		// take the initial qvalues for admission policy
		double[] admissionQs = new double[Position
				.validValues(Integer.parseInt(prop.getProperty("cachemanager.qmodelcache.tiers"))).length];
		String[] aQs = prop.getProperty("cachemanager.qmodelcache.admission.initialQs").split(",");
		for (int a = 0; a < admissionQs.length; ++a) {
			admissionQs[a] = Double.parseDouble(aQs[a]);
		}

		// take the initial qvalues for replacement/eviction policy
		double[] replacementQs = new double[Actions.values().length];
		String[] rQs = prop.getProperty("cachemanager.qmodelcache.replacement.initialQs").split(",");
		for (int r = 0; r < replacementQs.length; ++r) {
			replacementQs[r] = Double.parseDouble(rQs[r]);
		}

		CM = CacheManager.getCacheManager(Integer.parseInt(prop.getProperty("cachemanager.qmodelcache.tiers")),
				admissionQs, replacementQs,
				Integer.parseInt(prop.getProperty("cachemanager.maximumupdatesnum.updatestrategy")),
				Integer.parseInt(prop.getProperty("cachemanager.maximumupdates.decisions")),
				Integer.parseInt(prop.getProperty("cachemanager.qmodelreplacement.maxLRU")),
				Double.parseDouble(prop.getProperty("cachemanager.qmodelcache.alpha")),
				Double.parseDouble(prop.getProperty("cachemanager.qmodelcache.gamma")),
				Long.parseLong(prop.getProperty("cachemanager.percentage.memorycapacitythreshold")),
				Long.parseLong(prop.getProperty("cachemanager.memory.capacity")),
				Long.parseLong(prop.getProperty("cachemanager.percentage.diskcapacitythreshold")),
				Long.parseLong(prop.getProperty("cachemanager.disk.capacity")));

		memFiles = new HashMap<String, MemoryFile>();
		diskFiles = new HashMap<String, DiskFile>();

		if (CMOwner) {
			logger.info(new Timestamp(System.currentTimeMillis())
					+ ":: ==> CacheManager initialized successfully!\nWith below properties:" + "\nTiers: "
					+ Integer.parseInt(prop.getProperty("cachemanager.qmodelcache.tiers")) + "\nadmissionInitialQs: "
					+ prop.getProperty("cachemanager.qmodelcache.admission.initialQs") + "\nreplacementInitialQs: "
					+ prop.getProperty("cachemanager.qmodelcache.replacement.initialQs") + "\nmaxUpdatesNum: "
					+ Integer.parseInt(prop.getProperty("cachemanager.maximumupdatesnum.updatestrategy"))
					+ "\nmaxDecisions: " + Integer.parseInt(prop.getProperty("cachemanager.maximumupdates.decisions"))
					+ "\nmaxLRU: " + Integer.parseInt(prop.getProperty("cachemanager.qmodelreplacement.maxLRU"))
					+ "\nalpha: " + Double.parseDouble(prop.getProperty("cachemanager.qmodelcache.alpha")) + "\ngamma: "
					+ Double.parseDouble(prop.getProperty("cachemanager.qmodelcache.gamma"))
					+ "\nMemoryCapacityThreshold: "
					+ Long.parseLong(prop.getProperty("cachemanager.percentage.memorycapacitythreshold")) + "%"
					+ "\nMemoryCapacity: " + Long.parseLong(prop.getProperty("cachemanager.memory.capacity"))
					+ "\nDiskCapacityThreshold: "
					+ Long.parseLong(prop.getProperty("cachemanager.percentage.diskcapacitythreshold")) + "%"
					+ "\nDiskCapacity: " + Long.parseLong(prop.getProperty("cachemanager.disk.capacity")));
		}
	}

	@Override
	public CacheFile getItemToEvict(StoreOptionType evictionTier) {

		// Supposed that evictionTier is only one tier no combination
		FileInfo file = CM.cacheRequestReplacement(RLpoliciesUtils.convertStoreOptionTypeToTier(evictionTier));

		if (file != null) {

			if (evictionTier == StoreOptionType.DISK_ONLY)
				return diskFiles.get(file.getPathname());
			else if (evictionTier == StoreOptionType.MEMORY_ONLY)
				return memFiles.get(file.getPathname());
		} else {
			logger.error(new Timestamp(System.currentTimeMillis()) + ":: There is not any file to evict!");
		}
		return null;
	}

}
