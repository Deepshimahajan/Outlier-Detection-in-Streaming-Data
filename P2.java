import java.net.*;
import java.util.*;
import java.io.*;

public class P2 {
	
	String host;
	int port;
	int windowSize;

	int p;
	int cellWidth;
	int recentCellNo = 0;
	int dimension;
	int density;
	int countOutliers = 0;
	HashMap<Integer, ArrayList<Integer>> cellLowerLimits;
	HashMap<Integer, ArrayList<Integer>> cellUpperLimits;
	HashMap<Integer, ArrayList<Integer>> cellNeighbors;
	HashMap<Integer, Integer> cellCount;
	HashMap<Integer, ArrayList<Integer>> window;	//Timestamp, Coordinate in d dimension

	public void start() {
	
		window = new HashMap<Integer, ArrayList<Integer>>();
		cellLowerLimits = new HashMap<Integer, ArrayList<Integer>>();
		cellUpperLimits = new HashMap<Integer, ArrayList<Integer>>();
		cellNeighbors = new HashMap<Integer, ArrayList<Integer>>();
		cellCount = new HashMap<Integer, Integer>();
		// Connect with the server  
		try{   
		    Socket socket = new Socket(host, port);
		    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));    
		    String line;
		    int point_count = 0;
		    while((line = reader.readLine())!=null){ 
		    	//Parse Line
			    	String[] tokens = line.trim().split(",");
			    	if(tokens.length == 1) {
			    		//Ignoring this data packet as it doen't have data point information
			    		continue;
			    	}
	
			    	int ts = Integer.parseInt(tokens[0].trim());
			    	ArrayList<Integer> coordinates = new ArrayList<Integer>();
	
			    	for(int tok_no = 1; tok_no < tokens.length; tok_no ++ ) 
			    		coordinates.add(Integer.parseInt(tokens[tok_no].trim()));
	
			    	point_count++;
			    	if(point_count == 1) {
			    		dimension = coordinates.size();
			    		if(dimension<1) {
			    			System.err.print("Dimension cannot be less 1");
			    			System.exit(0);
			    		}
			    		p = (int)Math.ceil(Math.pow(windowSize, (float)1/(dimension+1)));
			    		cellWidth = (int)Math.ceil(65535/p);
			    		density = (int)Math.ceil(Math.log(p));
			    	}
	
			    	//Add point to window
			    	window.put(ts,coordinates);
	
			    	int cellNo = findCell(coordinates);
			    	if(cellNo == -1) {
			    		//Create cell
			    		cellNo = createCell(coordinates);
			    	}
			    	cellCount.put(cellNo,cellCount.get(cellNo)+1);
	
			    	if(window.size() > windowSize) {
			    		//Remove point from window and remove the cell if the count of data in cell becomes zero
			    		int affectedCellNo = findCell(window.get(ts-windowSize));
			    		cellCount.put(affectedCellNo,cellCount.get(affectedCellNo)-1);
			    		if(cellCount.get(affectedCellNo) == 0) {
			    			removeCell(affectedCellNo);
			    		}
			    		window.remove(ts-windowSize);
			    	}
	
			    	if(point_count > windowSize) {
			    		//Detect outlier
			    		int numberOfPoints = cellCount.get(cellNo);
			    		if(numberOfPoints < density) {
			    			boolean isOutlier = true;
			    			for(Integer nCell : cellNeighbors.get(cellNo)) {
			    				if(cellCount.get(nCell) >= density) {
			    					isOutlier = false;
			    					break;
			    				}
			    			}
			    			if(isOutlier) {
			    				System.out.println(line.trim() + " is an Outlier.");
			    				countOutliers++;
			    			}
			    		}
			    	}    	
		    }
		    if(countOutliers==0) {
	    		System.out.println("There are no Outliers.");
		    }
		    socket.close();
		}
		catch(Exception e) {
			System.out.println("Unable to connect to server at " + host + ":" + port);
			e.printStackTrace();
		}
	}
	
	public int findCell(ArrayList<Integer> coordinates) {
		for(Integer cellNo : cellLowerLimits.keySet()) {
			boolean allMatch = true;
			for(int d = 0; d < dimension; d++) {
				if(!(cellLowerLimits.get(cellNo).get(d) <= coordinates.get(d) && coordinates.get(d) < cellUpperLimits.get(cellNo).get(d))) {
					allMatch = false;
					break;
				}
			}
			if(allMatch) {
				return cellNo;
			}
		}
		return -1;
	}
	//Create new cell for data if no cell present for that data point.
	public int createCell(ArrayList<Integer> coordinates) {
		ArrayList<Integer> lowerLimits = new ArrayList<Integer>();
		ArrayList<Integer> upperLimits = new ArrayList<Integer>();

		recentCellNo++;
		int cellNo = recentCellNo;
		for(int d=0;d<dimension;d++) {
			int lowerLimit = (int)(Math.floor((float)coordinates.get(d)/(float)cellWidth)) * cellWidth;
			int upperLimit = lowerLimit + cellWidth;
			lowerLimits.add(lowerLimit);
			upperLimits.add(upperLimit);
		}

		cellLowerLimits.put(cellNo,lowerLimits);
		cellUpperLimits.put(cellNo,upperLimits);
		cellCount.put(cellNo,0);
		// Update the neighbors for that cell
		cellNeighbors.put(cellNo,new ArrayList<Integer>());
		for(Integer c : cellLowerLimits.keySet()) {
			if(c == cellNo) continue;
			if(isNeighbor(c,cellNo)) {
				cellNeighbors.get(c).add(cellNo);
				cellNeighbors.get(cellNo).add(c);
			}
		}
		return cellNo;
	}
	
	public boolean isNeighbor(int targetCell, int curCell) {
		for(int d = 0; d < dimension; d++) {
			if(Math.abs(cellLowerLimits.get(curCell).get(d) - cellLowerLimits.get(targetCell).get(d)) > (cellUpperLimits.get(curCell).get(d) - cellLowerLimits.get(curCell).get(d))) {
				return false;
			}
		}
		return true;
	}

	public void removeCell(int cellNo) {
		cellUpperLimits.remove(cellNo);
		cellLowerLimits.remove(cellNo);
		//Remove from neighbor list
		for(Integer neighborCellNo : cellNeighbors.get(cellNo)) {
			cellNeighbors.get(neighborCellNo).remove(Integer.valueOf(cellNo));
		}

		cellNeighbors.remove(cellNo);
		cellCount.remove(cellNo);
	}
	public static void main(String[] args) {
		P2 obj = new P2();
		Scanner in = new Scanner(System.in);
		obj.windowSize = in.nextInt() ;
		if(obj.windowSize <= 0) {
			System.err.print("WindowSize Cannot be less than or equal to zero");
			System.exit(0);
		}
		in.nextLine();
		String line = in.nextLine(); 
		obj.host = line.split(":")[0];
		obj.port = Integer.parseInt(line.split(":")[1]); 
		obj.start();
		in.close();
	}
}