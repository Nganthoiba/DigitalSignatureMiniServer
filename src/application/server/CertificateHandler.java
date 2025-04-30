package application.server;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import application.CertificateInfo;
import application.Config;
import application.exceptions.EmptyPinException;
import application.services.TokenService;
import application.ui.PinPrompt;

public class CertificateHandler implements HttpHandler{

	@Override
	public void handle(HttpExchange exchange) throws IOException {	
		String secretPin = null;	
		
		try {
			secretPin = PinPrompt.requestUserPinBlocking(new Function<String, Boolean>() {
	            @Override
	            public Boolean apply(String pin) {
	            	            	
	            	//If there has been already a correct PIN entered, check if the new one is same
	            	if(Config.PIN != null && !pin.equals(Config.PIN)) {
	            		return false;
	            	}
	                return true;
	            }
	        });
			
			if(secretPin == null) {
				throw new EmptyPinException("Empty PIN");
			}
			
			TokenService tokenService = new TokenService();
	        //cleanup any existing provider and keystore
			tokenService.cleanup();
			tokenService.detectToken(Config.getConfigFile(), secretPin);
			List<CertificateInfo> certDetails = tokenService.getCertificateDetails();
			CertificateInfo cert = certDetails.get(0);
			Response.send(exchange, cert.toJson(), 200);
		}
		catch(Exception e) {
			//Return response error in json
			JSONObject errorObject = new JSONObject();
			errorObject.put("message", "An error has occured while getting certificate details. "+e.getMessage());
			errorObject.put("status", false);
			errorObject.put("error_details", e);
			Response.send(exchange, errorObject, 500);
		}
		
	}

}
