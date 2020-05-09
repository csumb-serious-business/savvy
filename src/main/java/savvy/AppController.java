package savvy;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import savvy.core.EmbeddedNeo4j;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the application's main window
 */
public class AppController implements Initializable {
  public static AppController instance;
  private EmbeddedNeo4j _db;

  @FXML private TextField _subject;
  @FXML private TextField _relationship;
  @FXML private TextField _object;
  @FXML private Text txt_save;
  @FXML private Text txt_load;

  public AppController() {

    instance = this;
  }

  /**
   * sets the embedded neo4j db
   * this application won't run without it
   *
   * @param db the embedded neo4j db
   */
  public void setDB(EmbeddedNeo4j db) {
    this._db = db;
  }

  /**
   * save action from the form
   */
  public void save_action() {
    String subject = _subject.getText();
    String relationship = _relationship.getText();
    String object = _object.getText();

    txt_save.setFill(Color.FIREBRICK);
    txt_save.setText("Saving -- sub: " + subject + ", rel: " + relationship + ", obj: " + object);
    _db.addData(subject, relationship, object);
  }

  /**
   * load action from the form
   */
  public void load_action() {
    txt_load.setFill(Color.FIREBRICK);
    txt_load.setText("Loading: " + _db.readData());
  }

  @Override public void initialize(URL location, ResourceBundle resources) {
  }
}
