package StarkHub_MainPackage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSnackbarLayout;
import com.jfoenix.controls.JFXTextField;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginSignupController implements Initializable {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private MediaView mediaView;
    private MediaPlayer mp;
    private Media me;
    @FXML
    private JFXTextField signUpUserName,signUpEmail,loginUserName;
    @FXML
    private JFXPasswordField loginPassword,signUpPassword;

    private JFXSnackbar snackbar;
    private Socket sock;
    private  Stage progress=null;
    private String notifications=null,trending,username=null;
    private Thread videoThread;

    final static String sf = "Successful";
    final static String usf = "Unsuccessful";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            String path = new File("src/Graphics/LoginSignupBackground.mp4").getAbsolutePath();
            snackbar = new JFXSnackbar(anchorPane);

            System.out.println(path);
            me = new Media(new File(path).toURI().toString());
            videoThread= new Thread(() -> {
                while(true){
                    mp = new MediaPlayer(me);
                    mediaView.setMediaPlayer(mp);
                    mp.setVolume(0);
                    mp.setAutoPlay(true);
                    try{
                        Thread.sleep(8000);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            });
            videoThread.setDaemon(true);
            videoThread.start();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setSocket(Socket s){
        sock=s;
//        System.out.println(sock.isClosed());
    }
    public void onLogin() throws Exception {
//        System.out.println(sock.isClosed());

        if (loginUserName.getText().isEmpty()||loginPassword.getText().isEmpty())
            snackbar.enqueue(new JFXSnackbar.SnackbarEvent(new JFXSnackbarLayout("Please fill all details")));
        else {
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(sock.getInputStream()));
            PrintWriter output = new PrintWriter(sock.getOutputStream(), true);
            PojoFromClient pfc=new PojoFromClient();
            Gson gson = new GsonBuilder().serializeNulls().create();

//            System.out.println(input.readLine());
            Task<String> task=new Task<>() {
                @Override
                protected String call() throws Exception {
                    String ip;
                    try(final DatagramSocket socket = new DatagramSocket()){
                        socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                        ip = socket.getLocalAddress().getHostAddress();
                    }
                    System.out.println(ip);
                    pfc.setHeader(2);
                    pfc.setIPAddress(ip);
                    pfc.setUserName(loginUserName.getText());
                    pfc.setPassword(loginPassword.getText());
                    username=loginUserName.getText();
                    output.println(gson.toJson(pfc));
                    return input.readLine();
                }
            };
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ui/progress.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            progress = new Stage(StageStyle.UNDECORATED);
            progress.initModality(Modality.WINDOW_MODAL);
            progress.setScene(new Scene(root1));
            progress.setAlwaysOnTop(true);
            progress.show();
            task.setOnSucceeded(workerStateEvent -> {
                progress.close();
                if(task.getValue().equals(usf))
                    snackbar.enqueue(new JFXSnackbar.SnackbarEvent(new JFXSnackbarLayout("Invalid username or password")));
                else{
                    notifications=task.getValue();
                    openMainHub();
                }

            });
            Thread thread=new Thread(task);
            thread.start();
        }
    }
    public void onRegister() throws Exception {

        if (signUpEmail.getText().isEmpty()||signUpPassword.getText().isEmpty()||signUpUserName.getText().isEmpty())
            snackbar.enqueue(new JFXSnackbar.SnackbarEvent(new JFXSnackbarLayout("Please fill all details")));
        else {
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(sock.getInputStream()));
            PrintWriter output = new PrintWriter(sock.getOutputStream(), true);
            PojoFromClient pfc=new PojoFromClient();
            Gson gson = new GsonBuilder().serializeNulls().create();

//            System.out.println(input.readLine());
            Task<String> task=new Task<>() {
                @Override
                protected String call() throws Exception {
                    pfc.setHeader(1);
                    pfc.setEmailID(signUpEmail.getText());
                    pfc.setUserName(signUpUserName.getText());
                    pfc.setPassword(signUpPassword.getText());
                    output.println(gson.toJson(pfc));
                    return input.readLine();
                }
            };
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ui/progress.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            progress = new Stage(StageStyle.UNDECORATED);
            progress.initModality(Modality.WINDOW_MODAL);
            progress.setScene(new Scene(root1));
            progress.setAlwaysOnTop(true);
            progress.show();
            task.setOnSucceeded(workerStateEvent -> {
                progress.close();
                if(task.getValue().equals(usf))
                    snackbar.enqueue(new JFXSnackbar.SnackbarEvent(new JFXSnackbarLayout("Some error occurred")));
                else{
                    snackbar.enqueue(new JFXSnackbar.SnackbarEvent(new JFXSnackbarLayout("Registration successful!, Please login now")));
                    signUpUserName.setText("");
                    signUpPassword.setText("");
                    signUpEmail.setText("");
                }

            });
            Thread thread=new Thread(task);
            thread.start();
        }
    }
    public void openMainHub(){
        try {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ui/MainHub.fxml"));
        Parent root1 = null;

            root1 = (Parent) fxmlLoader.load();
        MainHubController mhc=fxmlLoader.getController();
        mhc.setTrending(trending);
        mhc.setNotifications(notifications);
        mhc.setUserName(username);
        mhc.setSock(sock);
        Stage stage = new Stage(StageStyle.UNDECORATED);
        Scene scene=new Scene(root1);
        scene.getStylesheets().add(Main.cssPath);
        stage.setScene(scene);
        stage.show();
        ((Stage)anchorPane.getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setTrending(String trending) {
        this.trending = trending;
    }
}
