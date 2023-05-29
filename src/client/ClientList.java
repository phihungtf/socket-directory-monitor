package client;

import java.util.*;

public class ClientList {
	private List<Client> clients;

	public ClientList() {
		clients = new ArrayList<>();
	}

	public void add(Client client) {
		clients.add(client);
	}

	public void remove(String host, int port) {
		for (int i = 0; i < clients.size(); i++) {
			if (clients.get(i).getHost().equals(host) && clients.get(i).getPort() == port) {
				clients.remove(i);
				break;
			}
		}
	}

	public void updateClient(String ip, String status, String directory, String lastUpdate) {
		for (int i = 0; i < clients.size(); i++) {
			String clientIP = clients.get(i).getHost() + ":" + clients.get(i).getPort();
			if (clientIP.equals(ip)) {
				clients.get(i).setStatus(status);
				clients.get(i).setDirectory(directory);
				clients.get(i).setLastUpdate(lastUpdate);
				break;
			}
		}
	}

	public List<Client> getClients() {
		return clients;
	}

	public Client get(int index) {
		return clients.get(index);
	}

	public int size() {
		return clients.size();
	}

	public void clear() {
		clients.clear();
	}
}
