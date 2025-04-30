package application.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;

public class Response {
	public static void send(HttpExchange exchange, String message, int httpCode) throws IOException {
		String acceptTypeString = exchange.getRequestHeaders().getFirst("Accept");
		System.out.println("Accept Type: "+acceptTypeString);
		if(acceptTypeString !=null && acceptTypeString.contains("application/json")) {
			System.out.println("Sending json back");
			JSONObject respObject = new JSONObject();
			respObject.put("message", message);
			send(exchange, respObject, httpCode);
		}
		else {
			List<HttpHeader> responseHeaders = HttpHeaderBuilder.create().add("Content-Type", "text/plain; charset=UTF-8").build();
			send(exchange, message, responseHeaders, httpCode);
		}
    }
	
	public static void send(HttpExchange exchange, String message, List<HttpHeader> responseHeaders, int httpCode) throws IOException {	
		send(exchange, message.getBytes(StandardCharsets.UTF_8), httpCode, responseHeaders);
    }
	
	public static void send(HttpExchange exchange, JSONObject jsonObject, int httpCode) throws IOException{
		List<HttpHeader> responseHeaders =  HttpHeaderBuilder.create()
    			.add("Content-Type", "application/json")
    			.build();
		byte[] jsonBytes= jsonObject.toString().getBytes(StandardCharsets.UTF_8);
		send(exchange, jsonBytes, httpCode, responseHeaders);
	}
	
	
	//This is common function
	public static void send(HttpExchange exchange, byte[] responseBytes, int httpCode, List<HttpHeader> responseHeaders) throws IOException {
		 if (responseBytes == null) {
            return;
        }
		//By default
		exchange.getResponseHeaders().add("X-Powered-By", "DigiSign-Server");
		if(responseHeaders != null) {
			for(HttpHeader header : responseHeaders) {
				exchange.getResponseHeaders().set(header.getKey(), header.getValue());
			}
		}
		
        exchange.sendResponseHeaders(httpCode, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
	}
		
}
