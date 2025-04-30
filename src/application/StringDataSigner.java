package application;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Signature;
import java.util.Base64;

public class StringDataSigner {
	public static String signStringData(String stringData, PrivateKey privateKey, Provider provider) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA", provider);
        signature.initSign(privateKey);
        signature.update(stringData.getBytes(StandardCharsets.UTF_8));
        byte[] signedHash = signature.sign();
        return Base64.getEncoder().encodeToString(signedHash);
    }
}
