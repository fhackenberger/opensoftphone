package org.acoveo.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Filesystem {
	public static Pattern COMMAND_AND_ARGS_PATTERN = Pattern.compile(
			"(" +
				"(" +
					"\"(" +
						"[^\\\\\"]|\\\\\"|\\\\[^\"]" +
					")+\"" +
				")|(" +
					"(" +
						"[^\\\\ ]|\\\\ |\\\\[^\\ ]" +
					")+(\\ |$)" +
				")" +
			")");
	
	/** Delete a directory recursively
	 * 
	 * @param path The directory to delete
	 * @return Whether the deletion was successful
	 */
	public static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					deleteDirectory(file);
				} else {
					file.delete();
				}
			}
		}
		return (path.delete());
	}
	
	/** Create a temporary directory with a given prefix
	 * 
	 * @param prefix The prefix to use
	 * @return
	 * @throws IOException
	 */
	public static File createTempDir(String prefix) throws IOException {
		File tempFile = File.createTempFile(prefix, "");
		if (!tempFile.delete())
			throw new IOException();
		if (!tempFile.mkdir())
			throw new IOException();
		tempFile.deleteOnExit();
		return tempFile;
	}
	
	/** Find files within a directory recursively
	 * 
	 * @param directory The directory to search
	 * @param filter A filename filter to apply (can be null)
	 * @return A list of files found
	 */
	public static List<File> findFilesRecursive(File directory, FilenameFilter filter) {
		List<File> appendableList = new LinkedList<File>();
		findFilesRecursiveInternal(directory, filter, appendableList);
		return appendableList;
	}
	
	/** Calculates an MD5 hash for a given file
	 * 
	 * @param file The file to process (will be read from beginning to end)
	 * @return The hash in hex format
	 * @throws NoSuchAlgorithmException, FileNotFoundException, IOException
	 */
	public static String calculateFileHashMD5(File file) throws NoSuchAlgorithmException, IOException {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		InputStream is = new FileInputStream(file);
		byte[] buffer = new byte[8192];
		int read = 0;
		try {
			while( (read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}
			byte[] md5sum = digest.digest();
			BigInteger bigInt = new BigInteger(1, md5sum);
			return bigInt.toString(16);
		} finally {
			is.close();
		}
	}
	
	/** Copy a file
	 * 
	 * Copies the content of file src to the file dest
	 * @param dest The file to copy to
	 * @param src The file to copy from
	 * @throws IOException Thrown if an error occurred while copying
	 */
	public static void copyFile(File dest, File src) throws IOException {
		FileChannel srcChannel = new FileInputStream(src).getChannel();
		FileChannel dstChannel = new FileOutputStream(dest).getChannel();

		dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

		srcChannel.close();
		dstChannel.close();
	}
	
	/**
	 * Copies from source to destination stream in 512 byte blocks.
	 * @param dest
	 * @param src
	 * @throws IOException
	 */
	public static void copyStream(OutputStream dest, InputStream src) throws IOException {
		byte b[] = new byte[512];
		int len=0;
	    while((len=src.read(b)) != -1) {
	    	dest.write(b,0,len);
        }
	}	
	
	/** Returns the file extension (without the dot) of a file
	 * 
	 * @param file The file to inspect
	 * @return The file extension or an empty string if there is none
	 */
	public static String getFileExtension(File file) {
		String fileName = file.getName();
		int dotPosition = fileName.lastIndexOf(".");
		if(dotPosition >= 0 && dotPosition < fileName.length() - 1) {
			return file.getName().substring(dotPosition + 1);
		}
		return "";
	}
	
	/** Parses a string into a command and its arguments
	 * 
	 * @param commandAndArgs A string containing the command and its arguments
	 * @return A list of string ready to be passed to {@code ProcessBuilder}, never null
	 */
	public static List<String> parseStringToCommandAndArgs(String commandAndArgs) {
		List<String> result = new LinkedList<String>();
		if(commandAndArgs == null || commandAndArgs.isEmpty()) {
			return result;
		}
		Matcher matcher = COMMAND_AND_ARGS_PATTERN.matcher(commandAndArgs);
		while(matcher.find()) {
			if(matcher.groupCount() > 0) {
				result.add(matcher.group(0).trim());
			}
		}
		return result;
	}

	protected static void findFilesRecursiveInternal(File directory, FilenameFilter filter, List<File> appendableList) {
		final DirectoryFilter dirFilter = new DirectoryFilter();
		File[] files = directory.listFiles(filter);
		if(files != null) {
			for (File file : files) {
				appendableList.add(file);
			}
		}
		File[] directories = directory.listFiles(dirFilter);
		if(directories != null) {
			for (File dir : directories) {
				findFilesRecursiveInternal(dir, filter, appendableList);
			}
		}
	}
	
	protected static class DirectoryFilter implements FilenameFilter {
		public boolean accept(File dir, String s) {
			File file = new File(dir, s);
			if (file.isDirectory())
				return true;
			return false;
		}
	};
}
