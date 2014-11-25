/* Save this in a file called Main.java to compile and test it */

/* Do not add a package declaration */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/* You may add any imports here, if you wish, but only from the 
 standard library */

public class Main {

	static Map<String, String> highestVersion = new HashMap<String, String>();
	static Map<String, Set<Server>> serverGroups = new HashMap<String, Set<Server>>();
	static Map<String, Server> serverMap = new HashMap<String, Server>();

	public static int processData(ArrayList<String> array) {

		int updateCount = 0;
		for (String string : array) {
			String[] record = string.split(",");
			String server = record[0];
			String software = record[2];
			String version = record[3];

			Server s = getServer(server);
			String v = highestVersion.get(software);
			if (v == null) {
				newSoftwareDetected(s, software, version);
				continue;
			}
			int diff = compare(version, v);
			if (diff > 0) {
				// updates the record and return the impacted servers also
				// update higher version of s/w along with servers @ higher
				// version
				updateCount += higherVersionDetected(s, software, version);
			} else if (diff == 0) {
				if(!s.marked)
				serverGroups.get(software).add(s);
			} else {
				// It means current server is at lower version. Mark it if not marked yet.
				if(!s.marked){
					updateCount++;
					s.marked = true;
				}
			}
		}
		return updateCount;
	}

	private static int higherVersionDetected(Server server, String software,
			String version) {

		for (Server s1 : serverGroups.get(software)) {
			s1.marked = true;
		}

		Set<Server> markedServers = serverGroups.get(software);

		HashSet<Server> newServerSet = new HashSet<Server>();
		newServerSet.add(server);
		serverGroups.put(software, newServerSet);

		highestVersion.put(software, version);
		return markedServers.size();
	}

	private static void newSoftwareDetected(Server server, String software,
			String version) {

		highestVersion.put(software, version);
		Set<Server> serverSet = serverGroups.get(software);
		if (serverSet == null) {
			serverSet = new HashSet<Server>();
		}
		if(!server.marked) 	serverSet.add(server);
		serverGroups.put(software, serverSet);
	}

	/**
	 * Version Comparison
	 * 
	 * @param current
	 * @param highest
	 * @return +ve value if currentVersion is higher else -ve; 0 if both are same;
	 */
	public static int compare(String current, String highest) {

		if (current.trim().equals(highest.trim()))
			return 0;

		String currentSplited[] = current.split("\\.");
		String highestSplited[] = highest.split("\\.");
		int i;
		for (i = 0; i < highestSplited.length && i < currentSplited.length; i++) {
			int higerVersion = Integer.parseInt(highestSplited[i].trim());
			int currentVersion = Integer.parseInt(currentSplited[i].trim());

			if (higerVersion == currentVersion)
				continue;

			return currentVersion - higerVersion;
		}
		return currentSplited.length - highestSplited.length;
	}

	private static Server getServer(String server) {
		Server s = serverMap.get(server);
		if (s == null) {
			Server newServer = new Server(server);
			serverMap.put(server, newServer);
			return newServer;
		}
		return s;
	}

	public static void main(String[] args) {
		ArrayList<String> inputData = new ArrayList<String>();
		try {
			Scanner in = new Scanner(new BufferedReader(new FileReader(
					"input.txt")));
			while (in.hasNextLine()) {
				String line = in.nextLine().trim();
				if (!line.isEmpty()) // Ignore blank lines
					inputData.add(line);
			}
			int retVal = processData(inputData);
			PrintWriter output = new PrintWriter(new BufferedWriter(
					new FileWriter("output.txt")));
			output.println("" + retVal);
			output.close();
		} catch (IOException e) {
			System.out.println("IO error in input.txt or output.txt");
		}
	}
}

class Server {
	public Server(String server) {
		name = server;
	}

	public String name;
	public boolean marked;

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return name;
	}
}
