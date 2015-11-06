package com.tayek.utilities;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
public class OldUtilities {
	public static List<File> addFiles(List<File> files,File dir) {
		if(files==null) files=new LinkedList<File>();
		if(!dir.isDirectory()) {
			files.add(dir);
			return files;
		}
		for(File file:dir.listFiles())
			addFiles(files,file);
		return files;
	}
	void mumble() {
		method();
	}
	public static String method(int n) {
		return Thread.currentThread().getStackTrace()[n].getClassName()+'.'+Thread.currentThread().getStackTrace()[n].getMethodName() + "()";
	}
	public static String method() {
		return method(2);
	}
	public static String shortMethod(int n) {
		return '.'+Thread.currentThread().getStackTrace()[n].getMethodName() + "()";
	}
	public static String shortMethod() {
		return shortMethod(2);
	}
	public static Properties load(final Reader reader) {
		try {
			Properties properties=new Properties();
			properties.load(reader);
			return properties;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static Properties load(final InputStream inputStream) {
		final Properties p = new Properties(defaultProperties); // add in any
																// new defaults?
		try {
			p.load(inputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return p;
	}
	public static final Properties defaultProperties = new Properties();
	static { /* add some properties */}
	public static String getString(String key, ResourceBundle resourceBundle) {
		String string = null;
		try {
			string=resourceBundle.getString(key);
		} catch (MissingResourceException e) {}
		return string;
	}

	public static void storeXml(final OutputStream outputStream, final Properties properties) {
		try {
			properties.storeToXML(outputStream, null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static void store(final OutputStream outputStream, final Properties properties) {
		try {
			properties.store(outputStream, null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static void store(final Writer writer, final Properties properties) {
		try {
			properties.store(writer, null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static void storeXml(final File propertiesFile, Properties p) {
		try {
			final OutputStream out = new FileOutputStream(propertiesFile);
			storeXml(out, p);
			out.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static Properties load(final File propertiesFile) {
		Properties p = null;
		try {
			final InputStream in = new FileInputStream(propertiesFile);
			p = load(in);
		} catch (FileNotFoundException e) {
			System.out.println(e);
		}
		return p;
	}
	public static void store(final File propertiesFile, Properties p) {
		try {
			final OutputStream out = new FileOutputStream(propertiesFile);
			store(out, p);
			out.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static byte[] save(final Object o) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(baos);
			out.writeObject(o);
			out.flush();
			out.close();
			return baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static Object restore(final ObjectInputStream objectInputStream) {
		try {
			final Object o = objectInputStream.readObject();
			objectInputStream.close();
			return o;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	public static Object restore(final byte[] b) {
		try {
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
			return restore(in);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static Object restore(final File file) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
			return restore(in);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static void toFile(final byte[] b, final File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			out.write(b);
			out.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static void toFile(final String s, final File file) {
		try {
			Writer out = new FileWriter(file);
			out.write(s);
			out.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static void fromFile(final StringBuffer stringBuffer, final File file) {
		try {
			Reader r = new FileReader(file);
			int c = 0;
			while ((c = r.read()) != -1)
				stringBuffer.append((char) c);
			r.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	static List<String>toStrings(final BufferedReader r) {
		final List<String> l = new LinkedList<String>();
		String line = null;
		try {
			for (line = r.readLine(); (line = r.readLine()) != null;)
				l.add(line);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return l;
	}
	public static String cat(final String[] data) {
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < data.length; i++)
			sb.append(data[i]).append('\n');
		return sb.toString();
	}
	public static String cat(final List<String> strings) {
		final StringBuffer sb = new StringBuffer();
		for (String string:strings)
			sb.append(string).append('\n');
		return sb.toString();
	}
	static List<String> getFileAsListOfStrings(final File file) {
		BufferedReader r = null;
		try {
			r = new BufferedReader(new FileReader(file));
			final List<String> l = toStrings(r);
			r.close();
			return l;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	static List<String> getDataThatMayHaveLineFeeds(final String[] data) {
		final BufferedReader r = new BufferedReader(new StringReader(cat(data))); // mes1
																					// has
																					// line
																					// feeds!
		return toStrings(r);
	}
	public static void removeCr(final StringBuffer stringBuffer, final String string) {
		for (int i = 0; i < string.length(); i++)
			if (string.charAt(i) != '\r') stringBuffer.append(string.charAt(i));
	}
	public static String noEol(String string) {
		String s=string;
		if (s.charAt(s.length() - 1) == '\n') s = s.substring(0, s.length() - 1);
		if (s.charAt(s.length() - 1) == '\r') s = s.substring(0, s.length() - 1);
		return s;
	}
	public static void close(final Reader r) {
		try {
			r.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static void close(final Writer w) {
		try {
			w.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static int uniform(final int n) {
		return (int) Math.floor(Math.random() * n);
	}
}
