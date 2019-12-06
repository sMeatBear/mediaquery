package comparator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.PriorityQueue;
import java.util.Queue;

import org.json.simple.parser.ParseException;

import dataaccess.VideoAnalyzer;
import model.Video;
import util.Cache;
import util.VideoConst;

public class SearchEngine {
	private String dbPath;
	// all videos path in db folder
	private String[] dbVideos;
	private int step = VideoConst.STEP;
	private int k = VideoConst.K;
	
	public class RankInfo implements Comparable<RankInfo>{
		String videoPath;
		double sim;
		
		public RankInfo(String videoPath, double sim) {
			this.videoPath = videoPath;
			this.sim = sim;
		}
		
		public String[] toStringArr() {
			return new String[] {videoPath, "" + sim};
		}
		
		@Override
		public int compareTo(RankInfo r2) {
			if (r2.sim - sim > 1e-6) return 1;
			else return -1;
		}
	}
	
	/**
	 * @param dbPath all db videos folder
	 */
	public SearchEngine(String dbPath) {
		if (!dbPath.endsWith("/")) dbPath += "/";
		this.dbPath = dbPath;
		dbVideos = getDBVideos();
	}
	
	/**
	 * @param dbPath all db videos folder
	 * @param step frame read step
	 * @param k the num of main colors extracted from each frame
	 */
	public SearchEngine(String dbPath, int step, int k) {
		this(dbPath);
		this.step = step;
		this.k = k;
	}
	
	
	/**
	 * @return a arrays contains {path, similarity} with desc order
	 * @throws ParseException 
	 */
	public String[][] search(String queryFolder) throws FileNotFoundException, IOException, ParseException {
		return search(queryFolder, step, k);
	}
	
	/**
	 * @param step frame read step
	 * @param k the num of main colors extracted from each frame
	 * @return a arrays contains {path, similarity} with desc order
	 */
	public String[][] search(String queryFolder, int step, int k) throws FileNotFoundException, IOException, ParseException {
		Queue<RankInfo> q = new PriorityQueue<>();
		
		// analyse all videos
		VideoAnalyzer va = new VideoAnalyzer();
		Video query = va.analyseVideo(queryFolder, step, k);
		
		Video[] dbs = new Video[dbVideos.length];
		for (int i = 0; i < dbVideos.length; i++) {
			String path = dbPath + dbVideos[i];
			
			// cache video
			File f = new File(VideoConst.CACHE_PATH + dbVideos[i] + ".json");
			if (f.exists()) {
				dbs[i] = Cache.readCache(f.getPath());
			} else {
				dbs[i] = va.analyseVideo(path, step, k);
				Cache.cacheVideo(dbs[i]);
			}
		}
		
		// compare
		for (int i = 0; i < dbs.length; i++) {
			// compare color
			double sim = ColorComp.compare(query, dbs[i]);
			// TODO: compare others
			
			RankInfo ri = new RankInfo(dbs[i].getPath(), sim);
			q.offer(ri);
		}
		
		// output result
		String[][] res = new String[dbs.length][];
		int i = 0;
		while (!q.isEmpty()) {
			res[i] = q.poll().toStringArr();
			i++;
		}
		
		return res;
	}
	
	/**
	 * @return all db videos folder path
	 */
	public String[] getDBVideos() {
		File f = new File(dbPath);
		String[] folders = f.list();
		
		return folders;
	}
}
