import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseLog {

	private static final String EXCEPTION_S_AT_MAIN_RUN_W_JAVA_D = ".*?(Exception|Error):(?s).*?at.*?\\.(main|run)\\(\\w*.java:\\d*\\)";
	private static final String LF = System.getProperty("line.separator");
	private static final String DLF = LF + LF;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (!validParams(args)) {
			displayUsage();
			return;
		}

		String logFile = args[0];
		String outputFile = args[1];
		String regEx = args.length == 3 ? args[2]
				: EXCEPTION_S_AT_MAIN_RUN_W_JAVA_D;
		FileInputStream ips = null;

		try {

			Pattern pattern = Pattern.compile(regEx);
			CharBuffer buff = null;

			ips = new FileInputStream(logFile);
			FileChannel channel = ips.getChannel();
			ByteBuffer bytBuff = channel.map(FileChannel.MapMode.READ_ONLY, 0,
					(int) channel.size());
			buff = Charset.forName("8859_1").newDecoder().decode(bytBuff);

			Matcher matcher = pattern.matcher(buff);
			int exceptionCount = 0;
			StringBuilder builder = new StringBuilder();

			while (matcher.find()) {
				builder.append("EXCEPTION" + (++exceptionCount) + ":"
						+ matcher.start() + "to" + matcher.end() + LF
						+ matcher.group() + DLF);
			}
			if (exceptionCount > 0) {
				PrintWriter out = null;
				try {
					out = getOutPutWriter(outputFile);
					String details = "Number of exceptions in log file: "
							+ exceptionCount + LF + "Details:" + LF;
					out.print(details + builder.toString());
				} catch (IOException ex) {
					printExceptionAndRethrow(
							"Error While Opening Output file : " + args[2], ex);
				} finally {
					if (out != null)
						out.close();
				}
			} else {
				System.out.println("No Exceptions found in log");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (ips != null) {
					ips.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private static void printExceptionAndRethrow(String msg, IOException ex)
			throws IOException {
		System.out.println(msg);
		ex.printStackTrace();
		throw ex;
	}

	private static PrintWriter getOutPutWriter(String outputFilePath)
			throws IOException, FileNotFoundException {
		File outputFile;
		PrintWriter out;
		outputFile = new File(outputFilePath);
		if (!outputFile.exists()) {
			outputFile.getParentFile().mkdirs();
			outputFile.createNewFile();
		}
		out = new PrintWriter(outputFile);
		return out;
	}

	private static void displayUsage() {
		System.out.println("Invalid Number of Parameters.");
		System.out.println("Usage:");
		System.out
				.println("ParsLog <log file path> <extracted file path> [Exception regEx]");
	}

	private static boolean validParams(String[] args) {
		if (args.length < 2) {
			return false;
		}
		return true;
	}

}
