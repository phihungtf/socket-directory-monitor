package gui;

import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.table.*;

import client.*;
import server.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

class ClientTableModel extends AbstractTableModel {
	private ClientList clients;
	private String[] columns;

	public ClientTableModel(ClientList clients) {
		super();
		this.clients = clients;
		columns = new String[] { "IP Address", "Status", "Watching directory", "Last update" };
	}

	// Number of column of your table
	@Override
	public int getColumnCount() {
		return columns.length;
	}

	// Number of row of your table
	@Override
	public int getRowCount() {
		return clients.size();
	}

	// The object to render in a cell
	@Override
	public Object getValueAt(int row, int col) {
		switch (col) {
			case 0:
				return clients.get(row).getHost() + ":" + clients.get(row).getPort();
			case 1:
				return clients.get(row).getStatus();
			case 2:
				return clients.get(row).getDirectory();
			case 3:
				return clients.get(row).getLastUpdate();
			default:
				return null;
		}
	}

	/**
	 * This method returns true if the cell at the given row and column is
	 * editable. Otherwise, it returns false.
	 */
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	/**
	 * This method is called whenever a value in the table is changed.
	 * 
	 * @param value the new value
	 * @param row   the row of the cell that was changed
	 * @param col   the column of the cell that was changed
	 */
	public void setValueAt(Object value, int row, int col) {
		fireTableCellUpdated(row, col);
	}

	// Optional, the name of your column
	public String getColumnName(int col) {
		return columns[col];
	}
}

public class Main extends JFrame implements Monitor.UpdateListener {
	private SocketServer server;
	private ClientTableModel clientModel;
	private ClientList clients;

	private Boolean isRunning = false;

	public Main() {
		clients = new ClientList();

		setTitle("Directory Monitor Server");
		initComponents();
		System.out.println("Init components done");
	}

	private void initComponents() {
		setLayout(new BorderLayout(10, 10));

		startServerPanel = new JPanel();
		clientPanel = new JPanel();
		controlPanel = new JPanel();

		startServerPanel.setLayout(new BorderLayout(8, 0));
		startServerPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

		portLabel = new JLabel("Port:");
		portField = new JTextField("6969");
		startServerButton = new JButton("Start server");
		startServerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				startServerButtonActionPerformed(evt);
			}
		});

		startServerPanel.add(portLabel, BorderLayout.WEST);
		startServerPanel.add(portField, BorderLayout.CENTER);
		startServerPanel.add(startServerButton, BorderLayout.EAST);

		clientPane = new JScrollPane();
		clientPanel.setLayout(new BorderLayout());
		clientPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

		clientTable = new JTable();
		clientModel = new ClientTableModel(clients);
		clientTable.setModel(clientModel);
		int[] preferredWidth = { 100, 75, 140, 100 };
		for (int i = 0; i < 3; i++) {
			TableColumn column = clientTable.getColumnModel().getColumn(i);
			column.setPreferredWidth(preferredWidth[i]);
		}

		clientPane.setViewportView(clientTable);
		clientPanel.add(clientPane, BorderLayout.CENTER);

		controlPanel.setLayout(new BorderLayout(8, 0));
		controlPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

		monitorButton = new JButton("Monitor...");
		monitorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				monitorButtonActionPerformed(evt);
			}
		});
		monitorButton.setEnabled(false);
		controlPanel.add(monitorButton, BorderLayout.WEST);

		disconnectButton = new JButton("Disconnect");
		disconnectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				disconnectButtonActionPerformed(evt);
			}
		});
		disconnectButton.setEnabled(false);
		controlPanel.add(disconnectButton, BorderLayout.EAST);

		add(startServerPanel, BorderLayout.NORTH);
		add(clientPanel, BorderLayout.CENTER);
		add(controlPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setPreferredSize(new java.awt.Dimension(400, 600));
		pack();
	}

	protected void disconnectButtonActionPerformed(ActionEvent evt) {
		int row = clientTable.getSelectedRow();
		if (row == -1) {
			JOptionPane.showMessageDialog(this, "Please select a client to disconnect", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		Client client = clients.get(row);
		try {
			disconnectClient(client);
			clients.remove(client.getHost(), client.getPort());
			clientModel.fireTableDataChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void disconnectClient(Client client) throws Exception {
		client.getConn().disconnect();
	}

	protected void startServerButtonActionPerformed(ActionEvent evt) {
		if (!isRunning) {
			startServer();
		} else {
			stopServer();
		}
	}

	public void startServer() {
		server = new SocketServer(this);
		int port = Integer.parseInt(portField.getText());
		server.setPort(port);
		server.start();
		isRunning = true;
		startServerButton.setText("Stop server");
		portField.setEditable(false);
		monitorButton.setEnabled(true);
		disconnectButton.setEnabled(true);
	}

	public void stopServer() {
		try {
			for (Client client : clients.getClients()) {
				disconnectClient(client);
			}
			clients.clear();
			clientModel.fireTableDataChanged();
			server.stopServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
		isRunning = false;
		startServerButton.setText("Start server");
		portField.setEditable(true);
		monitorButton.setEnabled(false);
		disconnectButton.setEnabled(false);
	}

	protected void monitorButtonActionPerformed(ActionEvent evt) {
		int row = clientTable.getSelectedRow();
		if (row == -1) {
			JOptionPane.showMessageDialog(this, "Please select a client to monitor", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		Client client = clients.get(row);
		Monitor monitor = client.getConn().getMonitor();

		monitor.setVisible(true);
	}

	public void addClient(String host, int port, SocketThread conn) {
		clients.add(new Client(host, port, conn));
		clientModel.fireTableDataChanged();
	}

	public Monitor createMonitor() {
		Monitor monitor = new Monitor(this);
		return monitor;
	}

	@Override
	public void updateClient(String ip, String status, String directory, String lastUpdate) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		if (!lastUpdate.equals("")) {
			Date lastUpdateDate = null;
			try {
				lastUpdateDate = df.parse(lastUpdate);
			} catch (Exception e) {
				e.printStackTrace();
			}
			lastUpdate = sdf.format(lastUpdateDate);
		}
		clients.updateClient(ip, status, directory, lastUpdate);
		clientModel.fireTableDataChanged();
	}

	private JPanel startServerPanel;
	private JLabel portLabel;
	private JTextField portField;
	private JButton startServerButton;
	private JScrollPane clientPane;
	private JPanel clientPanel;
	private JTable clientTable;
	private JPanel controlPanel;
	private JButton monitorButton;
	private JButton disconnectButton;
}
