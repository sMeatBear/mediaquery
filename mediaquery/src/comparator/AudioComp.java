package comparator;

import java.util.List;

import model.Video;

public class AudioComp {
	/**
	 * @param query query video obj
	 * @param db db video obj
	 */
	public static double compare(Video query, Video db) {
		return compare(query.getRmsList(), db.getRmsList());
	}
	
	/**
	 * @param qSound rms chunk list of query sound
	 * @param dbSound rms chunk list of query sound
	 * @return difference of RMS
	 */
	public static double compare(List<Double> qSound, List<Double> dbSound) {
		double d = Double.MAX_VALUE;
		int move = dbSound.size() - qSound.size();
		
		for (int i = 0; i <= move; i++) {
			// current section's difference
			double curr = 0;
			for (int j = 0; j < qSound.size(); j++) {
				curr += Math.abs(dbSound.get(i + j) - qSound.get(j));
			}
			d = Math.min(d, curr / qSound.size());
		}
		
		if(!isSim(d)) return 0;
		
		return d;
	}
	
	/**
	 * @param diff sound similarity
	 */
	public static boolean isSim(double diff) {
		int threshold = 40;
		if (diff > threshold) return false;
		else return true;
	}
}
