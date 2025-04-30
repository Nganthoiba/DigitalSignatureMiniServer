package application.server;

import java.io.IOException;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import application.Config;
import application.utils.FileContext;

public class FileRequestHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
        	HttpRequestContext context = new HttpRequestContext(exchange);
        	FileContext fileContext = context.file("file_doc");
        	
        	try{
        		fileContext.saveFile(Config.STORAGE_PATH);
        		Response.send(exchange, "File uploaded at "+Config.STORAGE_PATH, 200);
        	}
        	catch(Exception e) {
        		//throw new IOException("Unable to store file. "+e.getMessage(), e);
        		Response.send(exchange, "Unable to store file: "+e.getMessage(), 500);
        	}
        	
        }
        else {
        	Response.send(exchange, "Method Not Allowed", 405);
        }
        	
    }

}
