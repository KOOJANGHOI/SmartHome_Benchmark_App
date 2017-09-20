package iotruntime.master;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import java.util.Arrays;

import iotinstaller.MySQLInterface;
import iotinstaller.TableProperty;
import iotinstaller.Table;

/** Class LoadBalancer is a class that derives information
 *  about hosts (compute nodes) from the database and select
 *  a certain host to do a computation at a certain situation
 *  based on the metrics given to calculate the most load-balanced
 *  job assignment
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-01-18
 */
public final class LoadBalancer {

	/**
	 * LoadBalancer class properties
	 * <p>
	 * Class properties to contain host information from table
	 * HOSTADDRESS is in the form of MAC address that gets translated
	 * through DHCP
	 * hmNumProcesses tracks the usage of a certain host/compute node
	 * hmLoadScore tracks the score of the load for each host;
	 * host selection for the next process is based on these scores
	 * +----------------------+-----------+--------+------------+
	 * | HOSTADDRESS          | PROCESSOR | MEMORY | #PROCESSES |
	 * +----------------------+-----------+--------+------------+
	 * | XX:XX:XX:XX:XX:XX    |      3500 |     32 |          1 |
	 * | XX:XX:XX:XX:XX:XX    |      3500 |     32 |          4 |
	 * | ...                  |      ...  |    ... |        ... |
	 * | ...                  |      ...  |    ... |        ... |
	 * +----------------------+-----------+--------+------------+
	 */
	private HashMap<String, Integer> hmHostAddress;
	private int[] arrProcessor;
	private int[] arrMemory;
	private int[] arrNumProcesses;
	private int[] arrLoadScore;
	private Table tbl;
	private boolean bVerbose;

	/**
	 * LoadBalancer class constants
	 */
//	private static final String STR_TABLE_COMPUTE_NODE = "IoTComputeNodePC";
    private static final String STR_TABLE_COMPUTE_NODE = "IoTComputeNode";

	/**
	 * Class constructor
	 */
	public LoadBalancer(boolean _bVerbose) {

		hmHostAddress = new HashMap<String, Integer>();
		arrProcessor = null;
		arrMemory = null;
		arrNumProcesses = null;
		arrLoadScore = null;
		tbl = new Table(STR_TABLE_COMPUTE_NODE, _bVerbose);
		bVerbose = _bVerbose;
		RuntimeOutput.print("LoadBalancer: Creating a load-balancer!", bVerbose);
	}

	/**
	 * setupLoadBalancer() method loads host information from DB
	 *
	 * @return  void
	 */
	public void setupLoadBalancer() {

		String[][] arrTbl = tbl.getGeneralDBTable();
		arrProcessor = new int[arrTbl.length];
		arrMemory = new int[arrTbl.length];
		arrNumProcesses = new int[arrTbl.length];
		arrLoadScore = new int[arrTbl.length];

		for(int i=0; i<arrTbl.length; i++) {

			// Iterate per row from the DB table
			hmHostAddress.put((String) arrTbl[i][0], i);
			arrProcessor[i] = Integer.parseInt((String) arrTbl[i][1]);
			arrMemory[i] = Integer.parseInt((String) arrTbl[i][2]);

			// Initialize #process to 0 for all entries in the beginning
			// Initialize load score to maximum integer value
			arrNumProcesses[i] = 0;
			arrLoadScore[i] = Integer.MAX_VALUE;
		}
		RuntimeOutput.print("LoadBalancer: Initializing load balancer...", bVerbose);
	}

	/**
	 * selectHost() method selects a host based on the metrics
	 *
	 * @return  void
	 */
	public String selectHost() {

		// Variable for highest score that we are going to select
		int iHighestScore = 0;

		//String strHostMACAddress = null;
		String strHostIPAddress = null;

		RuntimeOutput.print("LoadBalancer: Host address number: " + hmHostAddress.size(), bVerbose);

		// Get the first host address from the hashmap
		strHostIPAddress = (String) hmHostAddress.keySet().toArray()[0];
		for(Map.Entry<String, Integer> mapHost : hmHostAddress.entrySet()) {

			// Get the current entry load score
			int iEntryScore = arrLoadScore[mapHost.getValue()];

			// Compare highest score and entry score; select the highest
			if (iHighestScore < iEntryScore) {
				iHighestScore = iEntryScore;
				strHostIPAddress = mapHost.getKey();
			}
		}

		// Calculate the new score for this host and return the host address
		calculateHostScore(strHostIPAddress);
		RuntimeOutput.print("LoadBalancer: Selected host: " + strHostIPAddress, bVerbose);

		return strHostIPAddress;
	}

	/**
	 * calculateHostScore() calculates score for a host based on the metrics
	 * <p>
	 * It also stores the results back to the corresponding hashmaps
	 *
	 * @param   strHostAddress   String host address
	 * @return                   void
	 */
	private void calculateHostScore(String strHostAddress) {

		// Get the previous values
		int iIndex = hmHostAddress.get(strHostAddress);
		int iPrevNumProcesses = arrNumProcesses[iIndex];

		// Calculate the current values
		// Every time we call this method, we increment #process by 1
		// (we add one new process)
		int iCurrNumProcesses = iPrevNumProcesses + 1;
		int iProcessor = arrProcessor[iIndex];
		int iMemory = arrMemory[iIndex];

		// We calculate the score simply with this formula
		// Score = (Processor/current #process) x (Memory/current #process)
		// The more processes a certain node has, the lower its score is.
		// Therefore, we always choose a node that has the highest score.
		// P.S. In this formula we also take the processor and memory specs
		// into account
		int iCurrScore = (iProcessor * iMemory) / iCurrNumProcesses;
		arrLoadScore[iIndex] = iCurrScore;
		arrNumProcesses[iIndex] = iCurrNumProcesses;
		RuntimeOutput.print("LoadBalancer: Calculate host load score for " + strHostAddress, bVerbose);
	}

	/**
	 * printHostInfo() method prints the host information at runtime
	 *
	 * @return  void
	 */
	public void printHostInfo() {

		for(Map.Entry<String, Integer> mapHost : hmHostAddress.entrySet()) {

			RuntimeOutput.print("Host address        : " + mapHost.getKey(), bVerbose);
			RuntimeOutput.print("Processor           : " + arrProcessor[mapHost.getValue()], bVerbose);
			RuntimeOutput.print("Memory              : " + arrMemory[mapHost.getValue()], bVerbose);
			RuntimeOutput.print("Number of processes : " + arrNumProcesses[mapHost.getValue()], bVerbose);
			RuntimeOutput.print("Host score          : " + arrLoadScore[mapHost.getValue()], bVerbose);
		}
	}

	public static void main(String[] args) {

		LoadBalancer lb = new LoadBalancer(true);
		lb.setupLoadBalancer();
		System.out.println("Chosen host: " + lb.selectHost());
		System.out.println("Chosen host: " + lb.selectHost());
		System.out.println("Chosen host: " + lb.selectHost());
		System.out.println("Chosen host: " + lb.selectHost());
		System.out.println("Chosen host: " + lb.selectHost());
		System.out.println("Chosen host: " + lb.selectHost());
		System.out.println("Chosen host: " + lb.selectHost());
		System.out.println("Chosen host: " + lb.selectHost());
		lb.printHostInfo();
	}
}
