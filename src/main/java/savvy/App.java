package savvy;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.db.EmbeddedNeo4j;
import savvy.ui.app.AppController;
import savvy.ui.app.DoShowTab;

/** entry point for the application */
public class App extends Application {
  private static final EmbeddedNeo4j _db = new EmbeddedNeo4j();
  private static final EventBus events = new EventBus();

  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public static void main(String[] args) {
    launch();
  }

  @Override
  public void start(Stage stage) throws IOException {
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
    var scene = new Scene(root);

    // set accelerators (keyboard shortcuts)
    var f = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
    var e = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN);
    var r = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);

    scene.getAccelerators().put(f, () -> EventBus.getDefault().post(new DoShowTab(f.getCode())));
    scene.getAccelerators().put(e, () -> EventBus.getDefault().post(new DoShowTab(e.getCode())));
    scene.getAccelerators().put(r, () -> EventBus.getDefault().post(new DoShowTab(r.getCode())));

    stage.setScene(scene);

    // show the scene
    stage.show();
  }
}
