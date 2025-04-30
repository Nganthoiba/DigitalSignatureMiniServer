package application;
	
import application.server.ESignServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class Main extends Application {
	private void loadCss(Scene scene) {
		String css = this.getClass().getResource("/resources/css/application.css").toExternalForm();
		scene.getStylesheets().add(css);
	}
	
	@SuppressWarnings("exports")
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/Main.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);	
			this.loadCss(scene);
			primaryStage.setScene(scene);
			primaryStage.setTitle("Digital Signature Mini Server ");
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop() throws Exception {
		super.stop();		
		// Stop the server when the application is closed
		ESignServer.stopServer();
		System.out.println("Application is closed");
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
