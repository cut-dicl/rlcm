package RL.QMatrix.AdmissionStrategy;

/**
 * Tiers Movements enumeration
 * 
 * @author Kakoulli Elena
 */

public enum TiersMovements {
	NONE,			//0
	DISK,			//1
	SSD,			//2
	DISK_SSD,		//3
	MEM,			//4
	DISK_MEM,		//5
	SSD_MEM,		//6
	DISK_SSD_MEM	//7
;

	public static TiersMovements valueOf(int i) {
		
		switch(i) {
		case 0: return TiersMovements.NONE;
		case 1: return TiersMovements.DISK;
		case 2: return TiersMovements.SSD;
		case 3: return TiersMovements.DISK_SSD;
		case 4: return TiersMovements.MEM;
		case 5: return TiersMovements.DISK_MEM;
		case 6: return TiersMovements.SSD_MEM;
		case 7: return TiersMovements.DISK_SSD_MEM;
		}
		return TiersMovements.NONE;
	}

    /**
     * Combine the two tier movements into one tier movement representing the union of the two.
     * 
     * @param t1
     * @param t2
     * @return
     */
    public static TiersMovements CombineTiers(TiersMovements t1, TiersMovements t2) {

        switch (t1) {
            case NONE:
                return t2;
            case MEM:
                switch (t2) {
                    case NONE:
                    case MEM:
                        return MEM;
                    case SSD:
                    case SSD_MEM:
                        return SSD_MEM;
                    case DISK:
                    case DISK_MEM:
                        return DISK_MEM;
                    case DISK_SSD:
                    case DISK_SSD_MEM:
                        return DISK_SSD_MEM;
                }
                break;
            case SSD:
                switch (t2) {
                    case NONE:
                    case SSD:
                        return SSD;
                    case MEM:
                    case SSD_MEM:
                        return SSD_MEM;
                    case DISK:
                    case DISK_SSD:
                        return DISK_SSD;
                    case DISK_MEM:
                    case DISK_SSD_MEM:
                        return DISK_SSD_MEM;
                }
                break;
            case DISK:
                switch (t2) {
                    case NONE:
                    case DISK:
                        return DISK;
                    case MEM:
                    case DISK_MEM:
                        return DISK_MEM;
                    case SSD:
                    case DISK_SSD:
                        return DISK_SSD;
                    case SSD_MEM:
                    case DISK_SSD_MEM:
                        return DISK_SSD_MEM;
                }
                break;
            case DISK_MEM:
                switch (t2) {
                    case NONE:
                    case DISK:
                    case MEM:
                    case DISK_MEM:
                        return DISK_MEM;
                    case SSD:
                    case DISK_SSD:
                    case SSD_MEM:
                    case DISK_SSD_MEM:
                        return DISK_SSD_MEM;
                }
                break;
            case DISK_SSD:
                switch (t2) {
                    case NONE:
                    case DISK:
                    case SSD:
                    case DISK_SSD:
                        return DISK_SSD;
                    case MEM:
                    case SSD_MEM:
                    case DISK_MEM:
                    case DISK_SSD_MEM:
                        return DISK_SSD_MEM;
                }
                break;
            case SSD_MEM:
                switch (t2) {
                    case NONE:
                    case SSD:
                    case MEM:
                    case SSD_MEM:
                        return SSD_MEM;
                    case DISK:
                    case DISK_MEM:
                    case DISK_SSD:
                    case DISK_SSD_MEM:
                        return DISK_SSD_MEM;
                }
                 break;
           case DISK_SSD_MEM:
                return DISK_SSD_MEM;
        }

        return null;
    }

    /**
     * Return the highest (single) tier from the provided tier movements
     * 
     * @param tier
     * @return
     */
    public static TiersMovements GetHighestTier(TiersMovements tier) {
        switch (tier) {
            case NONE:
            case MEM:
            case SSD:
            case DISK:
                return tier;
            case SSD_MEM:
            case DISK_MEM:
            case DISK_SSD_MEM:
                return TiersMovements.MEM;
            case DISK_SSD:
                return TiersMovements.SSD;
        }

        return null;
    }

    /**
     * Compare the highest tier of the two provided tier movements.
     * If t1 is higher return 1. If t2 is higher return -1. If equal, return 0.
     * For example, MEM is higher than DISK_SSD, SSD is lower than MEM, and MEM equals SSD_MEM.
     * @param t1
     * @param t2
     * @return
     */
    public static int CompareHighestTier(TiersMovements t1, TiersMovements t2) {
        TiersMovements ht1 = GetHighestTier(t1);
        TiersMovements ht2 = GetHighestTier(t2);

        if (ht1.ordinal() == ht2.ordinal())
            return 0;
        else if (ht1.ordinal() > ht2.ordinal())
            return 1;
        else
            return -1;
    }
}