package savvy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.EmbeddedNeo4j;

import java.io.IOException;

/**
 * entry point for the application
 */
public class App extends Application {
  private static final EmbeddedNeo4j _db = new EmbeddedNeo4j();

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  public static void main(String[] args) {
    launch();
  }

  @Override public void start(Stage stage) throws IOException {
    // init the db
    try {
      _db.createDb(true);
    } catch (IOException e) {
      log.error(e.toString());
    }

    // load the FXML
    var loader = new FXMLLoader(getClass().getClassLoader().getResource("views/app.fxml"));
    Parent root = loader.load();

    // inject the db
    loader.<AppController>getController().setDB(_db);
    loader.<AppController>getController().loaded_action();
    stage.setScene(new Scene(root));

    // show the scene
    stage.show();

  }

}
