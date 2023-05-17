import java.io.*;
import java.net.*;

import javax.swing.*;

import thread.SocketThread;

public class Server extends JFrame {
	public Server() {

		setTitle("Directory Monitor Server");

		initComponents();
		System.out.println("Init components done");
	}

	private void initComponents() {
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setPreferredSize(new java.awt.Dimension(800, 600));
		setResizable(false);

		pack();
	}

	public static void main(String[] args) throws Exception {
		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Windows".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new Server().setVisible(true);
			}
		});

		ServerSocket serverSocket = null;
		Socket socket = null;

		try {
			serverSocket = new ServerSocket(6969);
			System.out.println("Server started");
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (true) {
			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				System.out.println("I/O error: " + e);
			}
			// new thread for a client
			new SocketThread(socket, System.out).start();
		}
		// serverSocket.close();
	}
}
