package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

import message.MessageData;
import server.SocketThread;

public class DirectoryChooser extends JFrame {
	private SocketThread conn;
	private DefaultMutableTreeNode top;
	private DefaultMutableTreeNode choosingNode;
	private DefaultMutableTreeNode chosenNode;

	public interface InputListener {
		void onInput(DefaultMutableTreeNode chosenNode);
	}

	private InputListener listener;

	public DirectoryChooser(SocketThread conn, InputListener listener) {
		this.conn = conn;
		this.top = new DefaultMutableTreeNode(conn.getIPAddress());
		this.listener = listener;

		setTitle("Browse For Folder");

		initComponents();
		System.out.println("Init components done");
	}

	private void initComponents() {
		setLayout(new BorderLayout(10, 10));

		treePanel = new JPanel();
		bottomPanel = new JPanel();
		foldernamePanel = new JPanel();
		controlPanel = new JPanel();

		selectFolderLabel = new JLabel("Select Folder to Watch");
		selectFolderLabel.setHorizontalAlignment(SwingConstants.LEADING);
		selectFolderLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
		add(selectFolderLabel, BorderLayout.NORTH);

		treePane = new JScrollPane();
		treePanel.setLayout(new BorderLayout());
		treePanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
		tree = new JTree(top);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

				/* if nothing is selected */
				if (node == null)
					return;

				choosingNode = node;
				getChildren(node);

				/* retrieve the node that was selected */
				Object nodeInfo = node.getUserObject();

				/* React to the node selection. */
				setPath(nodeInfo.toString());
			}
		});

		treePane.setViewportView(tree);
		treePanel.add(treePane, BorderLayout.CENTER);

		bottomPanel.setLayout(new BorderLayout(8, 0));
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

		foldernamePanel.setLayout(new BorderLayout(8, 0));
		folderLabel = new JLabel("Folder");
		folderField = new JTextField("testDir");
		foldernamePanel.add(folderLabel, BorderLayout.WEST);
		foldernamePanel.add(folderField, BorderLayout.CENTER);

		controlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 8));
		okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				okButtonActionPerformed(evt);
			}
		});
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		controlPanel.add(okButton);
		controlPanel.add(cancelButton);

		bottomPanel.add(foldernamePanel, BorderLayout.NORTH);
		bottomPanel.add(controlPanel, BorderLayout.SOUTH);

		add(treePanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);

		// setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setPreferredSize(new java.awt.Dimension(300, 500));
		pack();
	}

	protected void okButtonActionPerformed(ActionEvent evt) {
		if (this.choosingNode == top)
			return;
		this.chosenNode = this.choosingNode;
		this.listener.onInput(this.chosenNode);
		this.setVisible(false);
	}

	protected void cancelButtonActionPerformed(ActionEvent evt) {
		this.setVisible(false);
	}

	public void createDrives(List<String> drives) {
		for (String drive : drives) {
			DefaultMutableTreeNode driveNode = new DefaultMutableTreeNode(drive);
			top.add(driveNode);
		}
	}

	public void setPath(String path) {
		folderField.setText(path);
	}

	public String getPath(DefaultMutableTreeNode node) {
		String path = "";
		TreeNode[] treeNode = node.getPath();
		for (int i = 0; i < treeNode.length; i++) {
			path += treeNode[i].toString();
			if (i != treeNode.length - 1)
				path += "\\";
		}
		return path.replace(":\\\\", ":\\").replaceAll("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(:\\d{1,5})?\\\\",
				"");
	}

	public void getChildren(DefaultMutableTreeNode node) {
		if (node.getChildCount() != 0)
			return;

		String path = getPath(node);
		MessageData msg = new MessageData(MessageData.CommandType.GET_DIRS, path);
		try {
			conn.sendRequest(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addChildren(String[] children) {
		for (String child : children) {
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
			choosingNode.add(childNode);
		}
	}

	JLabel selectFolderLabel;
	private JPanel bottomPanel;
	private JPanel foldernamePanel;
	private JPanel controlPanel;
	private JLabel folderLabel;
	private JTextField folderField;
	private JScrollPane treePane;
	private JPanel treePanel;
	private JButton okButton;
	private JButton cancelButton;
	private JTree tree;
}
