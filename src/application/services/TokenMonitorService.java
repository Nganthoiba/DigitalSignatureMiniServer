package application.services;

import java.security.KeyStore;
import java.security.Provider;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import application.Config;

public class TokenMonitorService {
	private ScheduledExecutorService tokenMonitor;
    private final Provider provider;

    public interface TokenLostCallback {
        void onTokenLost(String title, String message);
    }

    public TokenMonitorService(Provider provider) {
        this.provider = provider;
    }

    public void startMonitoring(TokenLostCallback callback) {
        stopMonitoring(); // in case if it's already running

        tokenMonitor = Executors.newSingleThreadScheduledExecutor();
        tokenMonitor.scheduleAtFixedRate(() -> {
            try {
                KeyStore ks = KeyStore.getInstance(Config.PKCS11, provider);
                ks.load(null, null); // no PIN needed for checking presence

                if (!ks.aliases().hasMoreElements()) {
                    callback.onTokenLost("Certificate Missing", 
                        "Token present but no certificates found. Try reinserting the token.");
                }

            } catch (java.security.ProviderException ex) {
                callback.onTokenLost("Token Removed", 
                    "The ePass2003 token device may have been removed. Server has been stopped.");
            } catch (Exception ex) {
                callback.onTokenLost("Token Monitoring Error", 
                    "An error occurred while monitoring the token: " + ex.getMessage());
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public void stopMonitoring() {
        if (tokenMonitor != null && !tokenMonitor.isShutdown()) {
            tokenMonitor.shutdownNow();
        }
    }
}
