package com.opencommunity.openTeamOneServer.util;

import com.opencommunity.openTeamOneServer.data.TenantParameter;
import com.opencommunity.openTeamOneServer.persistence.TenantParameterRepository;
import org.springframework.lang.NonNull;

import java.io.*;

public class StreamUtil {

	private static final int MAX_BUFFER_SIZE = 2 * 1024 * 1024;

	public static void pipeStream(InputStream inputStream, OutputStream outputStream) throws IOException {
		byte[] buffer = new byte[MAX_BUFFER_SIZE];
		int bytesRead = inputStream.read(buffer, 0, MAX_BUFFER_SIZE);
		while (bytesRead > 0) {
			outputStream.write(buffer, 0, bytesRead);
			bytesRead = inputStream.read(buffer, 0, MAX_BUFFER_SIZE);
		}
	}

	public static void writeFile(InputStream inputStream, File targetFile) throws IOException {
		if (targetFile.exists())
			targetFile.delete();
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
		pipeStream(inputStream, outputStream);
		outputStream.close();
	}

	public static void writeFile(byte[] content, File targetFile) throws IOException {
		if (targetFile.exists())
			targetFile.delete();
		FileOutputStream outputStream = new FileOutputStream(targetFile);
		outputStream.write(content, 0 , content.length);
		outputStream.close();
	}

	public static byte[] readStream(InputStream inputStream) throws IOException {
		if (inputStream == null)
			return null;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		pipeStream(inputStream, outputStream);
		outputStream.close();
		return outputStream.toByteArray();
	}

	public static byte[] readFile(@NonNull File file) throws IOException {
		FileInputStream inputStream = new FileInputStream(file);
		byte[] content = readStream(inputStream);
		inputStream.close();
		return content;
	}

	public static File getDataDirectory(TenantParameterRepository tpr, String subdirectory) {
		TenantParameter tp = tpr.findTopByName("dataDirectory");
		if (tp == null)
			return null;
		File directory = new File(tp.value);
		if (subdirectory != null)
			directory = new File(directory, subdirectory);
		directory.mkdirs();
		return directory.isDirectory() ? directory : null;
	}

	public static File getFile(TenantParameterRepository tpr, String subdirectory, String fileId) {
		File directory = getDataDirectory(tpr, subdirectory);
		return directory == null || fileId == null ? null : new File(directory, fileId);
	}

}
