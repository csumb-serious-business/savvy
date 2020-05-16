package savvy.ui.app;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.controlsfx.control.textfield.TextFields;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import savvy.core.EmbeddedNeo4j;
import savvy.core.Fact;
import savvy.ui.FactItemView;
import savvy.ui.fact_create.FactCreateEV;

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
  private final Set<String> _entities;
  private final Set<String> _relationships;
  public HBox idFactCreate;
  private EmbeddedNeo4j _db;

  @FXML private TextField _filter;
  @FXML private Text txt_msg;
  @FXML private ListView<FactItemView> lv_facts;

  public AppController() {
    instance = this;
    _entities = FXCollections.observableSet();
    _relationships = FXCollections.observableSet();
  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void onFactCreate(FactCreateEV ev) {
    var fact = ev.fact;
    txt_msg.setFill(Color.FIREBRICK);
    txt_msg.setText("Saving fact: " + fact);
    _db.createFact(fact);
    lv_facts.getItems().add(new FactItemView(fact, lv_facts, _db));

    _relationships.add(fact.getRelationship());

    _entities.addAll(Set.of(fact.getSubject(), fact.getObject()));
    EventBus.getDefault().post(new EntitiesUpdateEV(_entities));

  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void onEntitiesUpdate(EntitiesUpdateEV ev) {
    TextFields.bindAutoCompletion(_filter, _entities);
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
   * filter action for the list view
   */
  public void filter_action() {
    var filter = _filter.getText();
    Set<Fact> data;
    if (filter.equals("")) {
      data = _db.readAllFacts();

    } else {
      data = _db.readRelatedFacts(filter);
    }

    lv_facts.getItems().clear();
    List<FactItemView> layouts =
      data.stream().map(it -> new FactItemView(it, lv_facts, _db)).collect(Collectors.toList());
    lv_facts.getItems().addAll(layouts);
  }

  /**
   * loaded action for the controller overall
   */
  public void loaded_action() {
    // register with event bus
    EventBus.getDefault().register(this);

    List<FactItemView> layouts =
      _db.readAllFacts().stream().map(it -> new FactItemView(it, lv_facts, _db))
        .collect(Collectors.toList());
    lv_facts.getItems().addAll(layouts);

    // populate entities, relationships (triggers value changes)
    _entities.addAll(_db.readAllEntities());
    EventBus.getDefault().post(new EntitiesUpdateEV(_entities));

    _relationships.addAll(_db.readAllRelationships());
    EventBus.getDefault().post(new RelationshipsUpdateEV(_relationships));

  }

  @Override public void initialize(URL location, ResourceBundle resources) {

  }
}
