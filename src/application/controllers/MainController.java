package application.controllers;

import java.io.File;
import java.util.List;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import application.CertificateInfo;
import application.Config;
import application.services.TokenService;
import application.services.ServerService;
import application.services.TokenMonitorService;
import application.utils.LogWriter;
import application.utils.UIUtils;

public class MainController {

    @FXML private TextField epassDriverFilePath;
    @FXML private Button selectFileButton;
    @FXML private Button runServerButton;
    @FXML private Button stopServerButton;
    @FXML private TextArea consoleTextArea;

    private File selectedDriverFile;
    private TokenService tokenService;
    private final ServerService serverService = new ServerService();
    private TokenMonitorService tokenMonitorService;

    @FXML
    public void initialize() {        
        String libraryPath = Config.get("library");
        if (libraryPath != null && !libraryPath.trim().isEmpty()) {
            selectedDriverFile = new File(libraryPath);
            epassDriverFilePath.setText(libraryPath);
        }
    }

    @SuppressWarnings("exports")
	@FXML
    public void chooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Token Driver");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Driver DLL", "*.dll")
        );
        Stage stage = (Stage) selectFileButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null && file.exists()) {
            selectedDriverFile = file;
            epassDriverFilePath.setText(file.getAbsolutePath());
        }
    }

    @SuppressWarnings("exports")
	@FXML
    public void startServer(ActionEvent event) {
    	log("Starting server ...");
        try {
            if (serverService.isPortInUse(Config.DEFAULT_SERVER_PORT)) {
                UIUtils.showAlert("Port in Use", "The port number "+Config.DEFAULT_SERVER_PORT+" is already in use.");
                log("Server could not be started due to port number "+Config.DEFAULT_SERVER_PORT+" already in use.");
                return;
            }

            if (selectedDriverFile == null || !selectedDriverFile.exists()) {
                UIUtils.showAlert("Driver Missing", "Please select the PKCS#11 DLL file.");
                return;
            }
            /*
            tokenService = new TokenService();
            tokenService.cleanup(); // Clean up any existing provider and keystore
            if(tokenService.loadTokenDriver(selectedDriverFile)) {
            	log("Token loaded successfully.");
            	List<CertificateInfo> certDetails = tokenService.getCertificateDetails();
                for (CertificateInfo info : certDetails) {
                    //System.out.println(info);
                    //log(info.toString());                   
                    
                    //Subject Details
                    log("---------------------------------------------------\n");
                    log("Subject Details: \n");
                    log("---------------------------------------------------\n");
                    log(info.getSubjectDetails());
                    log("Valid From: " + info.getValidFrom());
                    log("Valid Till: " + info.getValidTill()+"\n");
                         
                    
                    //Issuer Details
                    log("---------------------------------------------------\n");
                    log("Issuer Details \n");
                    log("---------------------------------------------------\n");
                    log(info.getIssuerDetails());
                }
            }
            */
            serverService.startServer(Config.DEFAULT_SERVER_PORT);
            log("Server started at http://localhost:" + Config.DEFAULT_SERVER_PORT);

            runServerButton.setDisable(true);
            stopServerButton.setDisable(false);
            
            /**
			 * Start monitoring the token for events like removal or PIN change.
			 * This will help in handling token-related events gracefully.
			 */
            /*
            tokenMonitorService = new TokenMonitorService(tokenService.getPkcs11Provider());
            tokenMonitorService.startMonitoring((title, message) -> {
                Platform.runLater(() -> {
                    log(title + ": " + message);
                    stopServer(null);
                    UIUtils.showAlert(title, message);
                });
            });
            */

        } catch (Exception e) {        	
        	handleExceptionMessage(e);
            e.printStackTrace();
        }
    }
    
    private void handleExceptionMessage(Exception e) {
    	Throwable cause = e;
    	while (cause.getCause() != null) {
			cause = cause.getCause();
		}
	    
		if (e.getMessage() != null) {
			String message = e.getMessage();
			String className = cause.getClass().getSimpleName();
			String causeMessage = cause.getMessage();
			//checking the cause of the exception
			if(cause instanceof java.security.UnrecoverableKeyException) {
				log("Error: "+className+", "+message);
			}			
			if(cause instanceof java.security.KeyStoreException) {
				log("Error: "+className+", "+message+ "\nPlease check if the PKCS#11 driver is installed and the path is correct or the device is plugged in.");
			}
			else if(cause instanceof java.security.ProviderException) {
				log("Error: "+className+", "+message+ "\nPlease check if the PKCS#11 driver is installed and the path is correct or the device is plugged in.");
			}
			else if(cause instanceof javax.security.auth.login.FailedLoginException) {
				log("Error: "+className+", "+message+ "\nPlease check if the PIN is correct.");
			}
			else if(causeMessage.contains("CKR_PIN_INCORRECT")) {
				log("Error: "+className+", "+causeMessage+ "\nPlease check the PIN is correct.");
			}
			
			if(message.contains("PKCS11 not found")) {
				log("Error: "+message+ "\nPlease check if the PKCS#11 driver is installed and the path is correct or the device is plugged in.");
			}
			else {
		        log("An error occurred: " + className + " - " + message);
		    }
			
		} else {
			log("An unknown error occurred.");
		}
	}

    @SuppressWarnings("exports")
	@FXML
    public void stopServer(ActionEvent event) {
        if (serverService.stopServer()) {
        	
        	//tokenService.cleanup(); 
            log("Server stopped.");
            runServerButton.setDisable(false);
            stopServerButton.setDisable(true);
            /*
            if (tokenMonitorService != null) {
                tokenMonitorService.stopMonitoring();
            }
            Config.PIN = null;*/
        }
    }

    private void log(String message) {
        consoleTextArea.appendText(message + "\n");
        LogWriter.writeLog(message);
    }
}
