package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import application.exceptions.EmptyPinException;
import application.exceptions.InvalidPinException;
import application.exceptions.NoCertificateFoundException;
import application.exceptions.SunPKCS11NotFoundException;
import application.utils.TokenUtil;

public class EpassToken {
	
	//Constants 
	//private static final String SUN_PKCS11 = "SunPKCS11";
	private static final String PKCS11 = "PKCS11";
	
	private Provider pkcs11Provider;
	private KeyStore keyStore;
	private TokenDetails tokenDetails;
	
	//Method to get Provider separately
	public Provider getProvider() {
		return pkcs11Provider;
	}
	
	//Method to get KeyStore separately
	public KeyStore getKeyStore() {
		return keyStore;
	}
	
	//Method to get TokenDetails (pkcs11Provider and keyStore) together
	public TokenDetails getTokenDetails() {
		return tokenDetails;
	}
	
	/**
	 * Detects and loads the ePass token using the given PIN.
	 * 
	 * @param secretPin The PIN used to unlock the token.
	 * @return TokenDetails containing the provider and keystore.
	 * @throws EmptyPinException if the PIN is empty.
	 * @throws SunPKCS11NotFoundException if the PKCS#11 provider is not found.
	 * @throws NoCertificateFoundException if no certificate is found in the token.
	 */
	public TokenDetails detectToken(String secretPin) throws Exception {		
		
		// Implement the logic to detect the token using the dllPath which is the path to the PKCS#11 library
		// that will be used to access the token. The dllPath is assumed already set up in the configuration file 'epass_config.cfg'
		// and the secretPin. This may involve loading the PKCS#11 library
		
		if(secretPin == null || secretPin.isEmpty()) {
			throw new EmptyPinException("Secret pin is empty.");
		}	
		
		//Unload any previously loaded token
		this.unloadToken();
			
		File configFile = Config.getConfigFile();
		if (!configFile.exists()) {
			throw new FileNotFoundException("Configuration file not found: " + configFile.getAbsolutePath());
		} 

		pkcs11Provider = TokenUtil.loadPkcs11ProviderFromFile(configFile);
        // Load keystore
        keyStore = KeyStore.getInstance(PKCS11, pkcs11Provider); 
               
        try {            
            validatePinWithSignature(secretPin);            
            tokenDetails = new TokenDetails(pkcs11Provider, keyStore);            
            return tokenDetails;
        	
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause != null && cause.getMessage() != null &&
               (cause.getMessage().toLowerCase().contains("ckr_pin_incorrect") ||
                cause.getMessage().toLowerCase().contains("failedloginexception") ||
                cause.getMessage().toLowerCase().contains("password") || 
                cause.getMessage().toLowerCase().contains("login failed"))) {
                throw new InvalidPinException("Incorrect PIN entered.");
            } else {
                throw e;
            }
        }
		
	}
	
	private String getCertificateAlias() throws KeyStoreException {
		if (keyStore == null) {
			throw new KeyStoreException("KeyStore is not initialized.");
		}
		Enumeration<String> aliases = keyStore.aliases();
		if (aliases.hasMoreElements()) {
			return aliases.nextElement();
		} else {
			throw new KeyStoreException("No certificate found in the KeyStore.");
		}
	}
	
	// Method to get X509 certificate
	@SuppressWarnings("unused")
	private X509Certificate getX509Certificate() throws KeyStoreException {
		String alias = getCertificateAlias();
		if (alias != null) {
			return (X509Certificate) keyStore.getCertificate(alias);
		} else {
			throw new KeyStoreException("No certificate found in the KeyStore.");
		}
	}
	
	//method to validate the pin
	private void validatePinWithSignature(String secretPin) throws Exception {
	    keyStore.load(null, secretPin.trim().toCharArray());
	
	    Enumeration<String> aliases = keyStore.aliases();
	    if (!aliases.hasMoreElements()) {
	        throw new NoCertificateFoundException("No certificates found in token.");
	    }
	
	    String alias = aliases.nextElement();
	
	    PrivateKey privateKey;
	    try {
	        privateKey = (PrivateKey) keyStore.getKey(alias, secretPin.trim().toCharArray());
	        if (privateKey == null) {
	            throw new InvalidPinException("Private key is null. Possibly incorrect PIN.");
	        }
	    } catch (UnrecoverableKeyException e) {
	        throw new InvalidPinException("Incorrect PIN might have been entered.");
	    }
	
	    // Validate by attempting to sign something
	    try {
	        Signature signature = Signature.getInstance("SHA256withRSA", pkcs11Provider);
	        signature.initSign(privateKey);
	        signature.update("verify".getBytes());
	        signature.sign();
	        System.out.println("PIN verified successfully.");
	    } catch (Exception e) {
	        throw new InvalidPinException("PIN verification failed during signing: " + e.getMessage());
	    }
	}

	
	public void unloadToken() {
		if (pkcs11Provider != null) {
			Security.removeProvider(pkcs11Provider.getName());
			pkcs11Provider = null;
		}
		if (keyStore != null) {
			keyStore = null;
		}
	}
	
	public boolean isTokenLoaded() {
	    try {
	        return pkcs11Provider != null &&
	               keyStore != null &&
	               pkcs11Provider.getName() != null &&
	               !pkcs11Provider.getName().isEmpty() &&
	               keyStore.getType() != null &&
	               !keyStore.getType().isEmpty() &&
	               keyStore.aliases().hasMoreElements();
	    } catch (KeyStoreException e) {
	        return false;
	    }
	}

}
