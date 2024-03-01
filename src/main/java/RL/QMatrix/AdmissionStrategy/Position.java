package RL.QMatrix.AdmissionStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import RL.QMatrix.Tier;

/**
 * This class represents a position (in a tier or combination of tiers) and
 * utils An example of tiers position below: enum
 * Tiers{NONE,DISK,SSD,DISK_SSD,MEM,DISK_MEM,SSD_MEM,DISK_SSD_MEM}
 * 
 * @author Kakoulli Elena
 */

public class Position {

	// static method take #tiers and return combination of tiers movements
	private static TiersMovements[] ONE_TIER = { TiersMovements.NONE, TiersMovements.MEM };
	private static TiersMovements[] TWO_TIERS = { TiersMovements.NONE, TiersMovements.DISK, TiersMovements.MEM,
			TiersMovements.DISK_MEM };
	private static TiersMovements[] THREE_TIERS = { TiersMovements.NONE, TiersMovements.DISK, TiersMovements.SSD,
			TiersMovements.DISK_SSD, TiersMovements.MEM, TiersMovements.DISK_MEM, TiersMovements.SSD_MEM,
			TiersMovements.DISK_SSD_MEM };

	public static TiersMovements[] validValues(int tiers) {
		if (tiers == 1)
			return ONE_TIER;
		else if (tiers == 2)
			return TWO_TIERS;
		else // (tiers == 3)
			return THREE_TIERS;

	}

	private TiersMovements PositionInTiers;

	public Position(TiersMovements PositionInTiers) {
		this.PositionInTiers = PositionInTiers;
	}

	public void setPosition(TiersMovements tiers) {
		this.PositionInTiers = tiers;
	}

	public TiersMovements getPosition() {
		return this.PositionInTiers;
	}

	public int toInteger() {
		return PositionInTiers.ordinal();
	}

	public String toString() {
		return PositionInTiers.name();
	}

	public static Position fromInteger(int intValue) {
		switch (intValue) {
		case 0:
			return new Position(TiersMovements.NONE);
		case 1:
			return new Position(TiersMovements.DISK);
		case 2:
			return new Position(TiersMovements.SSD);
		case 3:
			return new Position(TiersMovements.DISK_SSD);
		case 4:
			return new Position(TiersMovements.MEM);
		case 5:
			return new Position(TiersMovements.DISK_MEM);
		case 6:
			return new Position(TiersMovements.SSD_MEM);
		case 7:
			return new Position(TiersMovements.DISK_SSD_MEM);
		}
		return null;
	}

	public static Set<Integer> getTiersMovements(int tiers) {
		TiersMovements[] validTiers = validValues(tiers);
		List<Integer> tiersMoves = new ArrayList<Integer>();

		for (TiersMovements t : validTiers) {
			tiersMoves.add(t.ordinal());
		}
		return tiersMoves.stream().collect(Collectors.toSet());
	}

	public boolean containsTier(Tier tier) {

		switch (tier.ordinal()) {

		case 0: // DISK
			if (this.PositionInTiers == TiersMovements.DISK || this.PositionInTiers == TiersMovements.DISK_MEM
					|| this.PositionInTiers == TiersMovements.DISK_SSD
					|| this.PositionInTiers == TiersMovements.DISK_SSD_MEM)
				return true;

			break;

		case 1: // SSD
			if (this.PositionInTiers == TiersMovements.SSD || this.PositionInTiers == TiersMovements.SSD_MEM
					|| this.PositionInTiers == TiersMovements.DISK_SSD
					|| this.PositionInTiers == TiersMovements.DISK_SSD_MEM)
				return true;

			break;
		case 2: // MEMORY
			if (this.PositionInTiers == TiersMovements.MEM || this.PositionInTiers == TiersMovements.DISK_MEM
					|| this.PositionInTiers == TiersMovements.SSD_MEM
					|| this.PositionInTiers == TiersMovements.DISK_SSD_MEM)
				return true;
			break;

		default:
			break;
		}

		return false;
	}

	public boolean containsTier(TiersMovements tier) {
		switch (tier.ordinal()) {

		case 1: // DISK
			if (this.PositionInTiers == TiersMovements.DISK || this.PositionInTiers == TiersMovements.DISK_SSD
					|| this.PositionInTiers == TiersMovements.DISK_MEM
					|| this.PositionInTiers == TiersMovements.DISK_SSD_MEM) {
				return true;
			}
			break;
		case 2: // SSD
			if (this.PositionInTiers == TiersMovements.SSD || this.PositionInTiers == TiersMovements.DISK_SSD
					|| this.PositionInTiers == TiersMovements.SSD_MEM
					|| this.PositionInTiers == TiersMovements.DISK_SSD_MEM) {
				return true;
			}
			break;
		case 3: // DISK_SSD
			if (this.PositionInTiers == TiersMovements.DISK_SSD
					|| this.PositionInTiers == TiersMovements.DISK_SSD_MEM) {
				return true;
			}
			break;
		case 4: // MEM
			if (this.PositionInTiers == TiersMovements.MEM || this.PositionInTiers == TiersMovements.DISK_MEM
					|| this.PositionInTiers == TiersMovements.SSD_MEM
					|| this.PositionInTiers == TiersMovements.DISK_SSD_MEM) {
				return true;
			}
			break;
		case 5: // DISK_MEM
			if (this.PositionInTiers == TiersMovements.DISK_MEM
					|| this.PositionInTiers == TiersMovements.DISK_SSD_MEM) {
				return true;
			}
			break;
		case 6: // SSD_MEM
			if (this.PositionInTiers == TiersMovements.SSD_MEM || this.PositionInTiers == TiersMovements.DISK_SSD_MEM) {
				return true;
			}
			break;
		case 7: // DISK_SSD_MEM
			if (this.PositionInTiers == TiersMovements.DISK_SSD_MEM) {
				return true;
			}
			break;
		default: // for case 0 and others
			break;
		}

		return false;
	}

	public TiersMovements addTier(TiersMovements tier) {

		switch (tier.ordinal()) {

		case 1: // DISK
			if (this.PositionInTiers == TiersMovements.NONE)
				return TiersMovements.DISK;
			else if (this.PositionInTiers == TiersMovements.SSD)
				return TiersMovements.DISK_SSD;
			else if (this.PositionInTiers == TiersMovements.MEM)
				return TiersMovements.DISK_MEM;
			else if (this.PositionInTiers == TiersMovements.SSD_MEM)
				return TiersMovements.DISK_SSD_MEM;
			break;
		case 2: // SSD
			if (this.PositionInTiers == TiersMovements.NONE)
				return TiersMovements.SSD;
			else if (this.PositionInTiers == TiersMovements.DISK)
				return TiersMovements.DISK_SSD;
			else if (this.PositionInTiers == TiersMovements.MEM)
				return TiersMovements.SSD_MEM;
			else if (this.PositionInTiers == TiersMovements.DISK_MEM)
				return TiersMovements.DISK_SSD_MEM;
			break;
		case 3: // DISK_SSD
			if (this.PositionInTiers == TiersMovements.NONE)
				return TiersMovements.DISK_SSD;
			else if (this.PositionInTiers == TiersMovements.SSD)
				return TiersMovements.DISK_SSD;
			else if (this.PositionInTiers == TiersMovements.MEM)
				return TiersMovements.DISK_SSD_MEM;
			break;
		case 4: // MEM
			if (this.PositionInTiers == TiersMovements.NONE)
				return TiersMovements.MEM;
			else if (this.PositionInTiers == TiersMovements.SSD)
				return TiersMovements.SSD_MEM;
			else if (this.PositionInTiers == TiersMovements.DISK)
				return TiersMovements.DISK_MEM;
			else if (this.PositionInTiers == TiersMovements.DISK_SSD)
				return TiersMovements.DISK_SSD_MEM;
			break;
		case 5: // DISK_MEM
			if (this.PositionInTiers == TiersMovements.NONE)
				return TiersMovements.DISK_MEM;
			else if (this.PositionInTiers == TiersMovements.SSD)
				return TiersMovements.DISK_SSD_MEM;
			break;
		case 6: // SSD_MEM
			if (this.PositionInTiers == TiersMovements.NONE)
				return TiersMovements.SSD_MEM;
			else if (this.PositionInTiers == TiersMovements.DISK)
				return TiersMovements.DISK_SSD_MEM;
			break;
		case 7: // DISK_SSD_MEM
			if (this.PositionInTiers == TiersMovements.NONE)
				return TiersMovements.DISK_SSD_MEM;
			break;
		default: // for case 0 <NONE> and others return the input tier
			break;
		}

		return tier;
	}

	public TiersMovements excludeTier(TiersMovements tier) {
		// take only the tiers combinations
		if (this.PositionInTiers == TiersMovements.DISK_SSD) {
			if (tier == TiersMovements.SSD)
				return TiersMovements.DISK;
			else if (tier == TiersMovements.DISK)
				return TiersMovements.SSD;
			else
				return null; // if there is not any combination
		} else if (this.PositionInTiers == TiersMovements.DISK_MEM) {
			if (tier == TiersMovements.MEM)
				return TiersMovements.DISK;
			else if (tier == TiersMovements.DISK)
				return TiersMovements.MEM;
			else
				return null; // if there is not any combination
		} else if (this.PositionInTiers == TiersMovements.SSD_MEM) {
			if (tier == TiersMovements.MEM)
				return TiersMovements.SSD;
			else if (tier == TiersMovements.SSD)
				return TiersMovements.MEM;
			else
				return null; // if there is not any combination
		} else if (this.PositionInTiers == TiersMovements.DISK_SSD_MEM) {
			if (tier == TiersMovements.MEM)
				return TiersMovements.DISK_SSD;
			else if (tier == TiersMovements.SSD)
				return TiersMovements.DISK_MEM;
			else if (tier == TiersMovements.DISK)
				return TiersMovements.SSD_MEM;
			else
				return null; // if there is not any combination
		} else
			return null; // if there is not any combination
	}

}
