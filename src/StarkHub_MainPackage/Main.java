package StarkHub_MainPackage;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Main extends Application {

    final static String IP_ADDRESS="localhost";
    final static int PORT=5000;

    private Socket sock;

    private BufferedReader input;
//    private PrintWriter output;

    @Override
    public void start(Stage stage) throws Exception {
        try {
            sock=new Socket(IP_ADDRESS,PORT);
            input = new BufferedReader(
                    new InputStreamReader(sock.getInputStream()));

            String trending=input.readLine();
            FXMLLoader loader=new FXMLLoader((getClass().getResource("LoginSignup.fxml")));
            Parent root = loader.load();
            LoginSignupController lsController=loader.getController();
            lsController.setSocket(sock);
            lsController.setTrending(trending);
            stage.setTitle("StarkHub Login Window");
            Scene scene = new Scene(root);
            String path = new File("src/resources/css/material.css").getAbsolutePath();
            scene.getStylesheets().add("file://"+path);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);


    }

}
