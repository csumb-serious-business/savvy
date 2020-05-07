package savvy;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import savvy.core.EmbeddedNeo4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * entry point for the application
 */
public class App extends Application {
  private static final EmbeddedNeo4j _db = new EmbeddedNeo4j();

  public static void main(String[] args) {
    launch();
  }

  @Override public void start(Stage stage) {
    Logger log = LoggerFactory.getLogger(this.getClass());

    // init the db
    try {
      _db.createDb(true);
    } catch (IOException e) {
      log.error(e.toString());
    }

    // build the UI
    var grid = new GridPane();
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(25, 25, 25, 25));

    // add 3 fields for sub, rel & obj
    var hbInputs = new HBox(10);
    var subF = new TextField();
    var relF = new TextField();
    var objF = new TextField();

    hbInputs.getChildren().addAll(subF, relF, objF);
    grid.add(hbInputs, 1, 1);

    // add buttons for save & load
    var hbBtns = new HBox(10);
    hbBtns.setAlignment(Pos.BOTTOM_RIGHT);

    var btnSave = new Button("Save");
    hbBtns.getChildren().add(btnSave);

    var btnLoad = new Button("Load");
    hbBtns.getChildren().add(btnLoad);

    grid.add(hbBtns, 1, 4);

    // add message text for save & load
    var vbMsgs = new VBox(10);
    final var txtSave = new Text();
    final var txtLoad = new Text();
    vbMsgs.getChildren().addAll(txtSave, txtLoad);

    grid.add(vbMsgs, 1, 2);

    // wire events for save & load
    btnSave.setOnAction(e -> {
      txtSave.setFill(Color.FIREBRICK);
      txtSave.setText(
        "Saving -- sub: " + subF.getText() + ", rel: " + relF.getText() + ", obj: " + objF
          .getText());
      _db.addData(subF.getText(), relF.getText(), objF.getText());
    });

    btnLoad.setOnAction(e -> {
      txtLoad.setFill(Color.FIREBRICK);
      txtLoad.setText("Loading: " + _db.readData());
    });

    // paint the scene
    var scene = new Scene(grid, 640, 480);
    stage.setScene(scene);
    stage.show();

  }

}
