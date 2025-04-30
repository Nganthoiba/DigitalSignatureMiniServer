package application.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

import com.sun.net.httpserver.*;
import application.Config;
import application.Coordinate;
import application.PDFSigner;
import application.SignatureDetail;
import application.services.TokenService;
import application.ui.PinPrompt;
import application.utils.FileContext;

import java.io.*;


public class FileUploadHandler implements HttpHandler {

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
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
        	byte[] signedPdfBytes = null;
            try {                
              
                /***
                 * 
                 * CODE TO GET & STORE THE UPLOADED FILE
                 * goes here
                 * 
                 */                
            	
            	HttpRequestContext httpRequestContext = new HttpRequestContext(exchange);
            	FileContext fileContext = httpRequestContext.file("my_file");
            	String fileName = "test_"+System.currentTimeMillis()+ "_" + fileContext.getFileName();
            	
            	
            	String filePath = fileContext.saveFile(Config.STORAGE_PATH + File.separator + "uploads", fileName);
                
                String secretPin = PinPrompt.requestUserPinBlocking(new Function<String, Boolean>() {
                    @Override
                    public Boolean apply(String pin) {
                    	/*
                    	if(pin.matches("\\d{9}") == false) {
                    		return false;
                    	}
                    	*/
                    	
                    	//If there has been already a correct PIN entered, check if the new one is same
                    	if(Config.PIN != null && !pin.equals(Config.PIN)) {
                    		return false;
                    	}
                        return true;
                    }
                });
                //Config.PIN = secretPin;
                //System.out.println("Current PIN: "+secretPin+ "\nStored PIN: "+Config.PIN);
                
                TokenService tokenService = new TokenService();
                //cleanup any existing provider and keystore
                tokenService.cleanup();
                tokenService.detectToken(Config.getConfigFile(), secretPin);
                
                // Now esign the uploaded document
                
                /**
                 * CODE TO SIGN THE DOCUMENT
                 */
                try {
                	
                	float x = Float.parseFloat(httpRequestContext.post("x", "60"));
                	float y = Float.parseFloat(httpRequestContext.post("y", "120"));
         
                	
                	SignatureDetail signDetail = new SignatureDetail();
                	signDetail.coordinate = new Coordinate(x,y);
                	signDetail.location = httpRequestContext.post("location", "");
                	
                	signedPdfBytes = PDFSigner.signPDF(filePath, secretPin, tokenService, signDetail); 
                    
                	List<HttpHeader> headers = HttpHeaderBuilder.create()
                			.add("Content-Type", "application/pdf")
                			.add("Content-Disposition", "attachment; filename=\"signed_doc.pdf\"")
                			// Add CORS headers for response
                			.add("Access-Control-Allow-Origin", clientOrigin)
                			.add("Access-Control-Allow-Credentials", "true")
                			.build();
                	
                	Response.send(exchange, signedPdfBytes, 200, headers);
                }
                catch (Exception e) {
					e.printStackTrace();
					exchange.getResponseHeaders().add("Access-Control-Allow-Origin", clientOrigin);
					Response.send(exchange,"Error signing the PDF: " + e.getMessage() , 500);
					return;
				}   
                
            } catch (Exception e) {
                e.printStackTrace();
                String errorResponse = "Internal Server Error: "+e.getMessage();
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", clientOrigin);
				Response.send(exchange,errorResponse , 500);
				return;
            }
        } else {
            String response = "Only POST method is supported.";
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", clientOrigin);
			Response.send(exchange,response , 500);
        }
    }   

    
}
