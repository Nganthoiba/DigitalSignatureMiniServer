package application.server;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;

import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import application.Config;
import application.StringDataSigner;
import application.services.TokenService;
import application.ui.PinPrompt;

public class HashDataSigningHandler implements HttpHandler{

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String requestMethod = exchange.getRequestMethod();
		/**
		 * Before the browser sends a POST request, it may send an OPTIONS request (preflight) to check 
		 * if the server allows the desired cross-origin request.
		 */
		String clientOrigin = exchange.getRequestHeaders().getFirst("Origin"); // Get Origin header from request

	    if (clientOrigin == null) {
	        clientOrigin = "*"; // Fallback for requests without Origin header
	    }
	    

	    if ("OPTIONS".equalsIgnoreCase(requestMethod)) {
	    	// Add CORS headers for preflight requests
		    List<HttpHeader> responseHeaders = HttpHeaderBuilder.create()
		    		.add("Access-Control-Allow-Origin", clientOrigin)
		    		.add("Access-Control-Allow-Methods", "POST, OPTIONS")
		    		.add("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")
		    		.add("Access-Control-Allow-Credentials", "true")
		    		.build();
	         
	        for(HttpHeader responseHeader : responseHeaders) {
	        	exchange.getResponseHeaders().add(responseHeader.getKey(), responseHeader.getValue());
	        }	        
	        exchange.sendResponseHeaders(204, -1); // No content response for OPTIONS request
	        return;
	    }


		if ("POST".equalsIgnoreCase(requestMethod)) {
			HttpRequestContext httpRequestContext = new HttpRequestContext(exchange);
			String hashDataString = httpRequestContext.post("hashedValue", null);
			if (hashDataString == null) {
	            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", clientOrigin); // Add CORS header
	            Response.send(exchange, "Your hash data is empty", 403);
	            return;
	        }

			
			String secretPin = PinPrompt.requestUserPinBlocking(new Function<String, Boolean>() {
                @Override
                public Boolean apply(String pin) {             	
                	
                	//If there has been already a correct PIN entered, check if the new one is same
                	if(Config.PIN != null && !pin.equals(Config.PIN)) {
                		return false;
                	}
                    return true;
                }
            });
			try {
			TokenService tokenService = new TokenService();
			tokenService.cleanup();
            tokenService.detectToken(Config.getConfigFile(), secretPin);			
				try {					
		            
		            String signatureDataString = StringDataSigner.signStringData(hashDataString, tokenService.getPrivateKey(secretPin), tokenService.getPkcs11Provider());
		            
		            JSONObject signatureObject = new JSONObject();
		            signatureObject.put("signature", signatureDataString);	            
		            signatureObject.put("certificate", Base64.getEncoder().encodeToString(tokenService.getPublicKey().getEncoded()));
		            
		            // Add CORS headers for response
		            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", clientOrigin);
		            exchange.getResponseHeaders().add("Access-Control-Allow-Credentials", "true");
		            Response.send(exchange, signatureObject, 200);
		            
				}
				catch (Exception e) {
					e.printStackTrace();
					exchange.getResponseHeaders().add("Access-Control-Allow-Origin", clientOrigin); // Add CORS header
					Response.send(exchange, "An error occurs. "+e.getMessage(), 500);
				}
			}
			catch(Exception ex) {
				
			}
			
		}
		else {
			exchange.getResponseHeaders().add("Access-Control-Allow-Origin", clientOrigin); // Add CORS header
	        Response.send(exchange, "Only POST method is allowed", 405);
		}		
	}
}
