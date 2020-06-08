// http://www.java2s.com/Code/Java/JavaFX/UsingFXMLtocreateaUI.htm
package savvy.ui.menu_bar;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ResourceBundle;

public class MenubarController implements Initializable {
    @FXML
    private MenuBar menuBar;

    /**
     * Handle action related to "About" menu item.
     *
     * @param event Event on "About" menu item.
     */
    @FXML
    private void handleAboutAction(final MouseEvent event) throws FileNotFoundException {
        showAbout();
    }

    public void showAbout() throws FileNotFoundException {
        // vBox
        VBox vBox = new VBox();

        //    // creates a centered savvyLogo in the VBox
        //    Image savvyLogo = new Image("savvyLogoTransparent.png");
        //    ImageView imageView = new ImageView();
        //    imageView.setImage(savvyLogo);
        //    imageView.setPreserveRatio(true);
        //    imageView.setFitWidth(400);
        //    imageView.setFitHeight(300);
        //    vBox.setPrefSize(400, 500);
        //    vBox.setMargin(imageView, new Insets(50, 10, 50, 10)); // optional
        vBox.setAlignment(Pos.TOP_CENTER);
        //    vBox.getChildren().add(imageView);

        // creates centered text in the VBox
        String copyright = "\u00a9";
        Text text =
                new Text(
                        "CST 499 Capstone. 2020 "
                                + copyright
                                + " "
                                + "Serious Business"
                                + "\n"
                                + "Disclaimer: The information on this page is fictitious."
                                + "\n"
                                + "It is used for academic purposes only.");
        text.setStyle("-fx-line-spacing: 1em;");
        text.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        text.setTextAlignment(TextAlignment.CENTER);
        vBox.getChildren().add(text);
        vBox.setMargin(text, new Insets(50, 10, 50, 10)); // optional

        // creates a centered csumbLogo in the VBox
        Image csumbLogo = new Image("csumbLogoTransparent.png");
        ImageView imageView2 = new ImageView();
        imageView2.setImage(csumbLogo);
        imageView2.setPreserveRatio(true);
        imageView2.setFitWidth(400);
        imageView2.setFitHeight(300);
        vBox.setPrefSize(400, 600);
        vBox.setMargin(imageView2, new Insets(0, 10, 50, 10)); // optional
        vBox.getChildren().add(imageView2);

        // set stage
        Scene scene = new Scene(vBox, 600, 500);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("About Us");
        stage.setWidth(600);
        stage.setHeight(500);
        text.setWrappingWidth(stage.getWidth() - 20);

        // add close when click off
        stage.initStyle(StageStyle.UNDECORATED);
        EventHandler<MouseEvent> eventHandler =
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        stage.close();
                    }
                };
        stage.addEventFilter(MouseEvent.MOUSE_CLICKED, eventHandler);

        stage.show();
    }
    /**
     * Handle action related to "License" menu item.
     *
     * @param event Event on "License" menu item.
     */
    @FXML
    private void handleLicenseAction(final MouseEvent event) throws FileNotFoundException {
        showLicense();
    }
    public static void showLicense() {
        // text
        Text text = new Text(License.licenseText);
        text.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        text.setTextOrigin(VPos.BASELINE);
        text.setTextAlignment(TextAlignment.LEFT);
        VBox textBox = new VBox(30);
        textBox.getChildren().addAll(text);
        Scene scene = new Scene(textBox, 600, 500);

        // stage
        Stage stage = new Stage();
        stage.setScene(scene);

        // add close when click off
        stage.initStyle(StageStyle.UNDECORATED);

        EventHandler<MouseEvent> eventHandler =
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        stage.close();
                    }
                };
        stage.addEventFilter(MouseEvent.MOUSE_CLICKED, eventHandler);

        stage.setTitle("License");
        stage.setWidth(600);
        stage.setHeight(500);
        text.setWrappingWidth(stage.getWidth() - 20);
        stage.show();
    }

    /**
     * Handle action related to "About" menu item.
     *
     * @param event Event on "About" menu item.
     */
    @FXML
    private void handleShortcutGuideAction(final MouseEvent event) throws IOException {
        showShortcutGuide();
    }

    public void showShortcutGuide() {
        // text
        Text header = new Text("Shortcut Guide");
        Text text =
                new Text(
                        "Switch to Facts Tab: CTRL + F"
                                + "\n"
                                + "\n"
                                + "Switch to Entities Tab: CTRL + E"
                                + "\n"
                                + "\n"
                                + "Switch to Relationships Tab: CTRL + R"
                                + "\n"
                                + "\n");
        VBox textBox = new VBox(10);

        header.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        textBox.setMargin(header, new Insets(50, 50, 0, 50));
        header.setTextAlignment(TextAlignment.CENTER);

        text.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        textBox.setAlignment(Pos.TOP_CENTER);
        text.setTextAlignment(TextAlignment.LEFT);
        textBox.setMargin(text, new Insets(0, 50, 50, 50));
        textBox.getChildren().addAll(header, text);

        Scene scene = new Scene(textBox, 600, 500);

        // stage
        Stage stage = new Stage();
        stage.setScene(scene);

        // add close when click off
        stage.initStyle(StageStyle.DECORATED);

        stage.setTitle("Shortcut Guide");
        stage.setWidth(600);
        stage.setHeight(500);
        text.setWrappingWidth(stage.getWidth() - 20);
        stage.show();
    }
    @Override
    public void initialize(java.net.URL arg0, ResourceBundle arg1) {
        menuBar.setFocusTraversable(true);
    }
}
