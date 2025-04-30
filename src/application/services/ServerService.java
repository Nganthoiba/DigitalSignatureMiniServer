package application.services;

import application.server.ESignServer;

public class ServerService {
	public boolean isPortInUse(int port) {
        return ESignServer.isPortInUse(port);
    }

    public void startServer(int port) throws Exception {
        ESignServer.startServer(port);
    }

    public boolean stopServer() {
        return ESignServer.stopServer();
    }
}
