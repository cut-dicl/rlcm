package RL.QMatrix.Policies.Admission;

import java.sql.Timestamp;
import java.util.Properties;

/**
 * This class represents the Admission RL-based policy
 * 
 * @author Kakoulli Elena
 */

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import RL.QMatrix.CacheManager;
import RL.QMatrix.CacheUpdate;
import RL.QMatrix.AdmissionStrategy.Position;
import RL.QMatrix.Policies.RLpoliciesUtils;
import RL.QMatrix.ReplacementStrategy.Actions;
import edu.cut.smacc.configuration.Configuration;
import edu.cut.smacc.server.cache.common.CacheFile;
import edu.cut.smacc.server.cache.common.StoreOptionType;
import edu.cut.smacc.server.cache.policy.admission.AdmissionPolicy;

public class AdmissionRL implements AdmissionPolicy{

	private static final Logger logger = LoggerFactory.getLogger(AdmissionRL.class);
    
	private static boolean CMOwner;
	private CacheManager CM;
        
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
    public void onItemAdd(CacheFile file, StoreOptionType tier) {		
        if (CMOwner)
            CM.cacheUpdate(CacheUpdate.CREATE, file.getKey(), file.getActualSize(),
                    RLpoliciesUtils.convertStoreOptionType(tier), RLpoliciesUtils.getLastModified(file));
	}

	@Override
	public void onItemNotAdded(CacheFile file, StoreOptionType tier) {
		// to increase miss count to CM (after call admit if the decision is not cache-none)
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
        //delete update from CM
		if (CMOwner) 
            CM.cacheUpdate(CacheUpdate.DELETΕ, file.getKey(), file.getTotalSize()/*file.getActualSize()*/,
                    RLpoliciesUtils.convertStoreOptionType(tier), RLpoliciesUtils.getLastModified(file));
	}

	@Override
    public void reset() {
		//do nothing
	}

	@Override
	public StoreOptionType getReadAdmissionLocation(CacheFile file) {
		StoreOptionType location = RLpoliciesUtils.convertTiersMovements(CM.cacheRequestAdmission(file.getKey(), file.getActualSize(), true));

		return location;
	}

	@Override
	public StoreOptionType getWriteAdmissionLocation(CacheFile file) {
		StoreOptionType location = RLpoliciesUtils.convertTiersMovements(CM.cacheRequestAdmission(file.getKey(), file.getActualSize(), false));

		return location;
	}
	
}
