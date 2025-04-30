package application.server;
import com.sun.net.httpserver.HttpServer;

import application.Config;
import application.utils.LogWriter;
import application.utils.ResourcePathUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ESignServer {
	private static HttpServer server;
	
	public static HttpServer startServer(int port) throws Exception {
		try {
			server = HttpServer.create(new InetSocketAddress(port), 0);
			//Route for HTML web UI
			Path publicPath = ResourcePathUtil.getAppResourcePath("public");
			server.createContext("/", new StaticFileHandler(publicPath.toString()));
			
			//Route for file upload
			server.createContext("/upload", new FileUploadHandler());
			server.createContext("/testUpload", new FileRequestHandler());			
			server.createContext("/status", new StatusHandler());
			server.createContext("/certDetails", new CertificateHandler());	
			server.createContext("/getHashedSignature", new HashDataSigningHandler());			
			
			//server.setExecutor(Executors.newCachedThreadPool()); // creates a default executor
			server.setExecutor(Executors.newFixedThreadPool(60));
			server.start();
			LogWriter.writeLog("Server started at localhost on port " + port);
			return server;
		} catch (IOException e) {
			LogWriter.writeLog(e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	
	// Stoping the server
	public static Boolean stopServer() {
		if (server != null) {
			server.stop(0);
			((ExecutorService) server.getExecutor()).shutdown();
			System.out.println("Server stopped.");
			return true;
		} 
		return false;
	}
	
	// Check if the port is in use
	public static boolean isPortInUse(int port) {
		try (var socket = new java.net.ServerSocket(port)) {
			return false;
		} catch (IOException e) {
			return true;
		}
	}
	
	//check if the server is running
	public static boolean isServerRunning() {
		if (server != null) {
			return server.getAddress().getPort() > 0;
		}
		return false;
	}
}
