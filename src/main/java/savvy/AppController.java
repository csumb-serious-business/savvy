package savvy;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import savvy.core.EmbeddedNeo4j;
import savvy.core.Fact;
import savvy.ui.FactItemView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for the application's main window
 */
public class AppController implements Initializable {
  public static AppController instance;
  private EmbeddedNeo4j _db;


  @FXML private TextField _subject;
  @FXML private TextField _relationship;
  @FXML private TextField _object;
  @FXML private TextField _filter;
  @FXML private Text txt_msg;
  //  @FXML private ListView<Fact> lv_facts;
  @FXML private ListView<FactItemView> lv_facts;

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

    txt_msg.setFill(Color.FIREBRICK);
    txt_msg.setText("Saving -- sub: " + subject + ", rel: " + relationship + ", obj: " + object);
    _db.addData(subject, relationship, object);
    var fact = new Fact(subject, relationship, object);
    lv_facts.getItems().add(new FactItemView(fact, lv_facts, _db));
    //    lv_facts.getItems().add(new Fact(subject, relationship, object));
  }

  /**
   * filter action for the list view
   */
  public void filter_action() {
    //    var selection = lv_facts.getSelectionModel().getSelectedItem();
    var filter = _filter.getText();
    Set<Fact> data;
    if (filter.equals("")) {
      data = _db.readAll();

    } else {
      data = _db.readData(filter);
    }

    lv_facts.getItems().clear();
    List<FactItemView> layouts =
      data.stream().map(it -> new FactItemView(it, lv_facts, _db)).collect(Collectors.toList());
    lv_facts.getItems().addAll(layouts);
    //    lv_facts.getItems().addAll(data);
  }

  public void loaded_action() {
    List<FactItemView> layouts =
      _db.readAll().stream().map(it -> new FactItemView(it, lv_facts, _db))
        .collect(Collectors.toList());
    lv_facts.getItems().addAll(layouts);
    //    lv_facts.getItems().addAll(_db.readAll());
  }

  @Override public void initialize(URL location, ResourceBundle resources) {

  }
}
