package com.lgi.server;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import com.lgi.search.LuceneQueryTransformer;
import static com.lgi.utils.LGIUtils.checkPathExists;
import static com.lgi.utils.LGIUtils.isPortAvailable;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LGIMain {
	private static final Logger logger = Logger
			.getLogger(LGIMain.class.getName());
	private static LuceneQueryTransformer lqt;
	private static Directory indexDirectory;
	private static final int port = 5555; 
	
	static {
		try {
			LogManager.getLogManager()
					.readConfiguration(LGIMain.class
							.getResourceAsStream("/logging.properties"));
		} catch (IOException e) {
			System.err
					.println("Could not load default logging.properties file");
			e.printStackTrace();
		}
	}

	
    private static void StartServer() {
    	try (ZMQ.Context context = ZMQ.context(1);
				ZMQ.Socket socket = context.socket(SocketType.REP)) {

			// Bind to a TCP address
			socket.bind("tcp://127.0.0.1:" + port);

			System.out.println("Server started on port " + port);

			while (!Thread.currentThread().isInterrupted()) {
				byte[] request = socket.recv();
				String query = new String(request, StandardCharsets.UTF_8);
				String response;
				try {
					response = lqt.executeQuery(query);
					socket.send(response.getBytes(StandardCharsets.UTF_8));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			cleanUp();
		} 
    	
    }
    
    private static void cleanUp () {
		lqt.closeResources();
		try {
			indexDirectory.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		String defaultDirectory = "indexedData";
		try {
			String os = System.getProperty("os.name").toLowerCase();
			if (!os.contains("linux")) {
				throw new Exception("LGI runs only in the linux ");
			}
			if (!isPortAvailable(port)) {
				throw new Exception("port number " + port + " is not available");
			}
			String targetDirectory ;
			if (args.length > 0) {
				// If a directory path is provided as a command-line argument
				targetDirectory = args[0];
				if (!checkPathExists(targetDirectory)) {
					System.out.println("Provided path is invalid. "
							+ "Checking the default " + defaultDirectory
							+ " in the current working directory.");
					if (!checkPathExists(defaultDirectory)) {
						throw new Exception("Not able to find the indexed data");
					}
					targetDirectory = defaultDirectory;
				}
			} else {
				System.out.println("No directory path provided. "
						+ "Checking the default " + defaultDirectory
						+ " directory in the current working directory.");
				if (!checkPathExists(defaultDirectory)) {
					throw new Exception("Not able to find the indexed data");
				}
				targetDirectory = defaultDirectory;
			}

			indexDirectory = FSDirectory.open(Paths.get(targetDirectory));
			lqt = new LuceneQueryTransformer(indexDirectory);
			StartServer();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
}

