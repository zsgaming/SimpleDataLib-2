package regalowl.simpledatalib.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;










import regalowl.simpledatalib.SimpleDataLib;
import regalowl.simpledatalib.events.LogEvent;
import regalowl.simpledatalib.events.LogLevel;
import regalowl.simpledatalib.sql.QueryResult;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;




public class FileTools {
	
	private SimpleDataLib sdl;
	
	public FileTools(SimpleDataLib sdl) {
		this.sdl = sdl;
	}
	
	public ArrayList<String> getFolderContents(String folderpath) {
		File dir = new File(folderpath);
		File[] contents = dir.listFiles();
		ArrayList<String> libContents = new ArrayList<String>();
		if (contents != null) {
			for (int i = 0; i < contents.length; i++) {
				String cpath = contents[i].toString();
				cpath = cpath.substring(cpath.lastIndexOf(File.separator) + 1, cpath.length());
				libContents.add(cpath.toString());
			}
		}
		return libContents;
	}
	
	public void loadExternalJar(String path) {
		try {
			if (!fileExists(path)) return;
			JarFile jarFile = new JarFile(path);
			Enumeration<JarEntry> e = jarFile.entries();
			URL[] urls = { new URL("jar:file:" + path + "!/") };
			URLClassLoader cl = URLClassLoader.newInstance(urls);
			while (e.hasMoreElements()) {
				JarEntry je = (JarEntry) e.nextElement();
				if (je.isDirectory() || !je.getName().endsWith(".class")) continue;
				String className = je.getName().substring(0, je.getName().length() - 6);
				className = className.replace('/', '.');
				cl.loadClass(className);
			}
			jarFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getJarPath() {
		URL url = sdl.getClass().getProtectionDomain().getCodeSource().getLocation();
		File f = null;
		try {
			f = new File(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		String path = "";
		if (!f.isDirectory()) {
			path = f.getParent();
		} else {
			path = f.getAbsolutePath();
		}
		return path;
	}

	public void copyFileFromJar(String resource, String destination) {
		InputStream resStreamIn = this.getClass().getClassLoader().getResourceAsStream(resource);
		if (resStreamIn == null) {
			sdl.getEventPublisher().fireEvent(new LogEvent("[SimpleDataLib["+sdl.getName()+"]]Failed to copy file. [" + resource + "]", null, LogLevel.SEVERE));
			return;
		}
		File newFile = new File(destination);
		try {
			OutputStream ostream = new FileOutputStream(newFile);
			int l;
			byte[] buffer = new byte[4096];
			while ((l = resStreamIn.read(buffer)) > 0) {
				ostream.write(buffer, 0, l);
			}
			ostream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void makeFolder(String path) {
		File folder = new File(path);
		if (!folder.exists()) {
			folder.mkdir();
		}
	}

	public void deleteFile(String path) {
		try {
			File file = new File(path);
			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void deleteDirectory(String path) {
		File file = new File(path);
		wipeDirectory(file);
	}
	public void wipeDirectory(File dir) {
	    for (File file: dir.listFiles()) {
	        if (file.isDirectory()) wipeDirectory(file);
	        file.delete();
	    }
	}
	public void unZipFile(String zipFile, String outputFolder) {
		try {
			File folder = new File(outputFolder);
			if (!folder.exists()) {
				folder.mkdir();
			}
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator + fileName);
				new File(newFile.getParent()).mkdirs();
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				byte[] buffer = new byte[1024];
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				ze = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// There is a better way in Java 7
	public void copyFile(String sourcePath, String destPath) {
		try {
			File sfile = new File(sourcePath);
			File dfile = new File(destPath);
			FileInputStream iStream = new FileInputStream(sfile);
			FileOutputStream oStream = new FileOutputStream(dfile);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = iStream.read(buffer)) > 0) {
				oStream.write(buffer, 0, length);
			}
			iStream.close();
			oStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	
	
	public boolean fileExists(String path) {
		File file = new File(path);
		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}
	public void makeFile(String path) {
		try {
			File file = new File(path);
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void writeStringToFile(String text, String path) {
		try {
			File file = new File(path);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(text);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public String getStringFromFile(String path) {
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF8"));
			String text = "";
			String string;
			while ((string = input.readLine()) != null) {
				text += string;
			}
			input.close();
			return text;
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}
	public ArrayList<String> getStringArrayFromFile(String path) {
		ArrayList<String> text = new ArrayList<String>();
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF8"));
			String string;
			while ((string = input.readLine()) != null) {
				text.add(string);
			}
			input.close();
			return text;
		} catch (IOException e) {
			e.printStackTrace();
			text.add("error");
			return text;
		}
	}


	
	public String getTimeStamp() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
	}
	
	public QueryResult readCSV(String filePath) {
		try {
			QueryResult qr = new QueryResult();
			if (!fileExists(filePath)) {
				return null;
			}
		    CSVReader reader = new CSVReader(new FileReader(filePath));
		    List<String[]> rows = reader.readAll();
		    boolean header = true;
		  	for (String[] row:rows) {
		  		if (header) {
		  			for (int i=0; i < row.length; i++) {
		  				qr.addColumnName(row[i]);
		  			}
		  			header = false;
		  		} else {
		  			for (int i=0; i < row.length; i++) {
		  				String cData = row[i];
		  				if (cData.equals("{{NULL}}")) {cData = null;}
		  				qr.addData(i+1, cData);
		  			}
		  		}
		    }
		  	reader.close();
		  	return qr;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void writeCSV(QueryResult data, String filePath) {
		try {
			if (fileExists(filePath)) {
				deleteFile(filePath);
			}
		    CSVWriter writer = new CSVWriter(new FileWriter(filePath));
			int colCount = data.getColumnCount();
		    String[] columnNames = new String[colCount];
		    ArrayList<String> namesArray = data.getColumnNames();
		    for (int i = 0; i < colCount; i++) {
				columnNames[i] = namesArray.get(i);
			}
		    writer.writeNext(columnNames);
			while (data.next()) {
				String[] row = new String[colCount];
				for (int i = 0; i < colCount; i++) {
					String cData = data.getString(i+1);
					if (cData == null) {cData = "{{NULL}}";}
					row[i] = cData;
				}
				writer.writeNext(row);
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
