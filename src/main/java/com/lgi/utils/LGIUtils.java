package com.lgi.utils;

import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LGIUtils {
	public static boolean checkPathExists(String directoryPath) {
		Path targetPath = Paths.get(directoryPath);

		if (Files.exists(targetPath) && Files.isDirectory(targetPath)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // If the ServerSocket is successfully created, the port is available
            return true;
        } catch (Exception e) {
            // If an exception occurs, the port is not available
            return false;
        }
    }

}
