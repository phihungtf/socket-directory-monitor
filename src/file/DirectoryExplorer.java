package file;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DirectoryExplorer {
	public static final String SEPARATOR = "|";
	private File currentDirectory;

	public DirectoryExplorer() {
		currentDirectory = new File("");
	}

	public List<String> list() {
		return getDirectories(currentDirectory.getAbsolutePath());
	}

	public void goUp() {
		if (currentDirectory.getParent() == null) {
			return;
		}
		File newDirectory = new File(currentDirectory.getParent());
		if (!newDirectory.exists()) {
			return;
		}
		currentDirectory = newDirectory;
	}

	public String getCurrentDirectory() {
		return currentDirectory.getAbsolutePath();
	}

	public void setCurrentDirectory(String path) {
		File newDirectory = new File(path);
		if (!newDirectory.exists()) {
			return;
		}
		currentDirectory = newDirectory;
	}

	public void goDown(String directory) {
		File newDirectory = new File(currentDirectory.getAbsolutePath() + "/" + directory);
		if (!newDirectory.exists()) {
			return;
		}
		currentDirectory = newDirectory;
	}

	public static List<String> getDrives() {
		List<String> drives = new ArrayList<>();
		// roots of the path name
		File root[] = File.listRoots();
		// check if the root is null or not
		if (root != null) {
			// get the roots of the path name
			for (int i = 0; i < root.length; i++) {
				drives.add(root[i].getPath());
			}

		}

		return drives;
	}

	public static List<String> getDirectories(String path) {
		List<String> directories = new ArrayList<>();
		File f = new File(path);

		File[] files = f.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return !file.isHidden() && file.isDirectory();
			}
		});

		for (int i = 0; i < files.length; i++) {
			directories.add(files[i].getName());
		}

		return directories;
	}

	public static String arrayToString(List<String> list) {
		return String.join(SEPARATOR, list);
	}

	public static List<String> stringToArray(String str) {
		return List.of(str.split("\\" + SEPARATOR));
	}
}