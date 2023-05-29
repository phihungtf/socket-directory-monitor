package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;

import message.MessageData;
import server.SocketThread;

import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

class Event {
	private String event;
	private String path;
	private String timestamp;

	public Event(String event, String path, String timestamp) {
		this.event = event;
		this.path = path;
		this.timestamp = timestamp;
	}

	public String getEvent() {
		return event;
	}

	public String getPath() {
		return path;
	}

	public String getTimestamp() {
		return timestamp;
	}
}

class EventTableModel extends AbstractTableModel {
	private List<Event> events;
	private String[] columns;

	public EventTableModel(List<Event> events) {
		super();
		this.events = events;
		columns = new String[] { "Event", "Path", "Timestamp" };
	}

	// Number of column of your table
	public int getColumnCount() {
		return columns.length;
	}

	// Number of row of your table
	public int getRowCount() {
		return events.size();
	}

	// The object to render in a cell
	public Object getValueAt(int row, int col) {
		switch (col) {
			case 0:
				return events.get(row).getEvent();
			case 1:
				return events.get(row).getPath();
			case 2:
				return events.get(row).getTimestamp();
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

public class Monitor extends JFrame implements DirectoryChooser.InputListener {
	private SocketThread conn;
	private List<Event> events;
	private EventTableModel eventTableModel;

	public DirectoryChooser directoryChooser;
	public Boolean isWatching = false;

	public interface UpdateListener {
		void updateClient(String ip, String status, String directory, String lastUpdate);
	}

	private UpdateListener listener;

	public Monitor(UpdateListener listener) {
		events = new ArrayList<>();
		this.listener = listener;

		setTitle("Directory Monitor");

		initComponents();
		System.out.println("Init components done");
	}

	private void initComponents() {
		setLayout(new BorderLayout(10, 10));

		selectDirPanel = new JPanel();
		eventPanel = new JPanel();
		buttonsPanel = new JPanel();

		selectDirPanel.setLayout(new BorderLayout(8, 0));
		selectDirPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

		pathLabel = new JLabel("Watching Path:");
		pathField = new JTextField("");
		pathField.setEditable(false);
		selectDirButton = new JButton("Browse...");
		selectDirButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				selectDirButtonActionPerformed(evt);
			}
		});
		startButton = new JButton("Start");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				startButtonActionPerformed(evt);
			}
		});
		startButton.setEnabled(false);
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		buttonsPanel.add(selectDirButton);
		buttonsPanel.add(startButton);

		selectDirPanel.add(pathLabel, BorderLayout.WEST);
		selectDirPanel.add(pathField, BorderLayout.CENTER);
		selectDirPanel.add(buttonsPanel, BorderLayout.EAST);

		eventPane = new JScrollPane();
		eventPanel.setLayout(new BorderLayout());
		eventPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

		eventTable = new JTable();
		eventTableModel = new EventTableModel(events);
		eventTable.setModel(eventTableModel);
		int[] preferredWidth = { 100, 350, 200 };
		for (int i = 0; i < 3; i++) {
			TableColumn column = eventTable.getColumnModel().getColumn(i);
			column.setPreferredWidth(preferredWidth[i]);
		}

		eventPane.setViewportView(eventTable);
		eventPanel.add(eventPane, BorderLayout.CENTER);

		add(selectDirPanel, BorderLayout.NORTH);
		add(eventPanel, BorderLayout.CENTER);

		// setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setPreferredSize(new java.awt.Dimension(700, 400));
		pack();
	}

	protected void selectDirButtonActionPerformed(ActionEvent evt) {
		// DirectoryChooser directoryChooser = new DirectoryChooser();
		// directoryChooser.setVisible(true);
		MessageData msgData = new MessageData(MessageData.CommandType.GET_DRIVES, null);
		try {
			conn.sendRequest(msgData);
			this.directoryChooser.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void startButtonActionPerformed(ActionEvent evt) {
		MessageData requestData;
		if (isWatching) {
			requestData = new MessageData(MessageData.CommandType.STOP, "");
			this.listener.updateClient(conn.getIPAddress(), "Connected", "", "");
		} else {
			requestData = new MessageData(MessageData.CommandType.WATCH, pathField.getText());
			this.listener.updateClient(conn.getIPAddress(), "Watching", pathField.getText(), "");
		}
		try {
			this.conn.sendRequest(requestData);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setDirectory(String dir) {
		pathField.setText(dir);
	}

	public void addEvent(String event, String path, String timestamp) {
		events.add(new Event(event, path, timestamp));
		eventTableModel.fireTableDataChanged();

		this.listener.updateClient(conn.getIPAddress(), "Watching", pathField.getText(), timestamp);
	}

	public void setConn(SocketThread conn) {
		this.conn = conn;
		this.directoryChooser = new DirectoryChooser(conn, this);
	}

	public void startWatching() {
		System.out.println("Start watching...");
		isWatching = true;
		selectDirButton.setEnabled(false);
		startButton.setText("Stop");
	}

	public void stopWatching() {
		isWatching = false;
		selectDirButton.setEnabled(true);
		startButton.setText("Start");
	}

	public void showErrorMessage(String message) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private JPanel selectDirPanel;
	private JLabel pathLabel;
	private JPanel buttonsPanel;
	private JTextField pathField;
	private JButton selectDirButton;
	private JButton startButton;
	private JScrollPane eventPane;
	private JPanel eventPanel;
	private JTable eventTable;

	@Override
	public void onInput(DefaultMutableTreeNode chosenNode) {
		pathField.setText(this.directoryChooser.getPath(chosenNode));
		startButton.setEnabled(true);
		events.clear();
		eventTableModel.fireTableDataChanged();
	}
}
