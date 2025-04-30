package application.server;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class StatusHandler implements HttpHandler{
	@Override
	public void handle(HttpExchange exchange) throws IOException { 
		
        Response.send(exchange, "Server is running", 200);
	}
}
