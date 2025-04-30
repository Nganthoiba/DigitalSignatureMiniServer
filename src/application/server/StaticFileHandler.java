package application.server;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
// import java.nio.file.Paths;
import java.nio.file.Paths;

import com.sun.net.httpserver.*;

import application.utils.LogWriter;
import application.utils.ResourcePathUtil;

public class StaticFileHandler implements HttpHandler {
	private final String baseDirectory;
	
	public StaticFileHandler(String baseDir) {
		this.baseDirectory = baseDir;
	}
	
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {	
		
		
		String uri = exchange.getRequestURI().toString();
		
		LogWriter.writeLog("Requested URI: "+uri);
		
		if (uri.equals("/")) {
			uri = "/index.html"; // Default to index.html if no specific file is requested
		}
		
		
		Path requestedPath = Paths.get(baseDirectory, uri).normalize();
		//Path basePath = Paths.get(baseDirectory).toAbsolutePath().normalize();
		if (!requestedPath.startsWith(Paths.get(baseDirectory).normalize())) {
		    //exchange.sendResponseHeaders(403, -1); // Forbidden
			Response.send(exchange, "Forbidden", 403);
		    return;
		}
				
		
		if (Files.exists(requestedPath) && !Files.isDirectory(requestedPath)) {
			
			String contentType = Files.probeContentType(requestedPath);
			if (contentType == null) {
			    contentType = "application/octet-stream";
			}
			exchange.getResponseHeaders().set("Content-Type", contentType);
			exchange.getResponseHeaders().set("Cache-Control", "no-cache");
			exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
			exchange.sendResponseHeaders(200, Files.size(requestedPath));
			try (OutputStream os = exchange.getResponseBody()) {
				Files.copy(requestedPath, os);
			}
		} else {			
			// If the file is not found, send a 404 response
			String response = "404, The requested resource '"+uri+"' is not found.";	
			System.out.println(response);
			Response.send(exchange, response, 404);
		}
	}
	
}
