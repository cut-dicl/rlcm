package RL.QMatrix.Policies;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import RL.QMatrix.Tier;
import RL.QMatrix.AdmissionStrategy.TiersMovements;
import edu.cut.smacc.configuration.Configuration;
import edu.cut.smacc.server.cache.common.CacheFile;
import edu.cut.smacc.server.cache.common.StoreOptionType;

/**
 * This class added for Reinforcement Learning policies - common help methods
 * 
 * @author Kakoulli Elena
 */

public class RLpoliciesUtils {

	private static final Logger logger = LoggerFactory.getLogger(RLpoliciesUtils.class);

	public static Properties getCMproperties(Configuration conf) {
		Properties prop = new Properties();
		File configFile = new File(conf.getString("rl.config.file"));
		logger.info(new Timestamp(System.currentTimeMillis())
				+ ":: Cache Manager Configuration file { cachemanager.config.properties }\nPathname:"
				+ configFile.getPath());

		if (!configFile.exists()) {
			logger.error(new Timestamp(System.currentTimeMillis())
					+ ":: Cache Manager Configuration file (cachemanager.config.properties) does not exist!\nPathname:"
					+ configFile.getPath());
			System.exit(1);
		}

		try (InputStream input = new FileInputStream(configFile)) {
			// load the properties file
			prop.load(input);

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return prop;
	}

	public static TiersMovements convertStoreOptionType(StoreOptionType tier) {
		// convert MEMORY_ONLY, DISK_ONLY, MEMORY_DISK, S3_ONLY to CM Position
		// NONE, DISK, SSD, DISK_SSD, MEM, DISK_MEM, SSD_MEM, DISK_SSD_MEM

		TiersMovements convertedTier = TiersMovements.NONE;

		switch (tier.ordinal()) {

		case 0: // MEMORY_ONLY
			convertedTier = TiersMovements.MEM;
			break;
		case 1: // DISK_ONLY
			convertedTier = TiersMovements.DISK;
			break;
		case 2: // MEMORY_DISK
			convertedTier = TiersMovements.DISK_MEM;
			break;
		case 3: // S3_ONLY
			convertedTier = TiersMovements.NONE;
			break;

		default:
			System.err.println(
					new Timestamp(System.currentTimeMillis()) + ":: RLpoliciesUtils received a wrong StoreOptionType!");
			logger.error(
					new Timestamp(System.currentTimeMillis()) + ":: RLpoliciesUtils received a wrong StoreOptionType!");
		}
		return convertedTier;
	}

	public static StoreOptionType convertTiersMovements(TiersMovements tier) {
		// convert MEMORY_ONLY, DISK_ONLY, MEMORY_DISK, S3_ONLY to CM Position
		// NONE, DISK, SSD, DISK_SSD, MEM, DISK_MEM, SSD_MEM, DISK_SSD_MEM

		StoreOptionType convertedTier = StoreOptionType.S3_ONLY; // OR NULL???

		switch (tier.ordinal()) {

		case 0: // NONE to S3_ONLY
			convertedTier = StoreOptionType.S3_ONLY;
			break;
		case 1: // DISK to DISK_ONLY
			convertedTier = StoreOptionType.DISK_ONLY;
			break;
		case 2: // SSD
			System.err.println(new Timestamp(System.currentTimeMillis()) + ":: SMACC does not support SSD tier!");
			logger.error(new Timestamp(System.currentTimeMillis()) + ":: SMACC does not support SSD tier!");
			break;
		case 3: // DISK_SSD
			System.err.println(new Timestamp(System.currentTimeMillis()) + ":: SMACC does not support SSD tier!");
			logger.error(new Timestamp(System.currentTimeMillis()) + ":: SMACC does not support SSD tier!");
			break;
		case 4: // MEM to MEMORY_ONLY
			convertedTier = StoreOptionType.MEMORY_ONLY;
			break;
		case 5: // DISK_MEM to MEMORY_DISK
			convertedTier = StoreOptionType.MEMORY_DISK;
			break;
		case 6: // SSD_MEM
			System.err.println(new Timestamp(System.currentTimeMillis()) + ":: SMACC does not support SSD tier!");
			logger.error(new Timestamp(System.currentTimeMillis()) + ":: SMACC does not support SSD tier!");
			break;
		case 7: // DISK_SSD_MEM
			System.err.println(new Timestamp(System.currentTimeMillis()) + ":: SMACC does not support SSD tier!");
			logger.error(new Timestamp(System.currentTimeMillis()) + ":: SMACC does not support SSD tier!");
			break;

		default:
			System.err.println(
					new Timestamp(System.currentTimeMillis()) + ":: RLpoliciesUtils received a wrong StoreOptionType!");
			logger.error(
					new Timestamp(System.currentTimeMillis()) + ":: RLpoliciesUtils received a wrong StoreOptionType!");
		}
		return convertedTier;
	}

	public static Tier convertStoreOptionTypeToTier(StoreOptionType tier) {
		// convert MEMORY_ONLY, DISK_ONLY, MEMORY_DISK, S3_ONLY to CM Tier Position
		// DISK, SSD, MEM
		// So supported only MEMORY & DISK Tiers!

		Tier convertedTier = null; 

		switch (tier.ordinal()) {

		case 0: // MEMORY_ONLY
			convertedTier = Tier.MEM;
			break;
		case 1: // DISK_ONLY
			convertedTier = Tier.DISK;
			break;
		default:
			System.err.println(
					new Timestamp(System.currentTimeMillis()) + ":: RLpoliciesUtils received a wrong StoreOptionType!");
			logger.error(
					new Timestamp(System.currentTimeMillis()) + ":: RLpoliciesUtils received a wrong StoreOptionType!");

		}
		return convertedTier;
	}

    public static long getLastModified(CacheFile file) {
        if (file.getLastModified() != 0) {
            return file.getLastModified();
        } else {
            return System.currentTimeMillis();
        }
    }
}
