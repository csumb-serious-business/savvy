package savvy.ui.app;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.Fact;
import savvy.core.db.EmbeddedNeo4j;
import savvy.core.events.DoFactCreate;
import savvy.core.events.DoFactDelete;
import savvy.core.events.DoFactUpdate;
import savvy.core.events.RelatedFactsRead;
import savvy.ui.facts_filter_list.FilterSubmitted;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Controller for the application's main window
 */
public class AppController implements Initializable {
  public static AppController instance;
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final Set<String> _entities;
  private final Set<String> _relationships;
  @FXML private HBox idFactCreate;
  @FXML private Pane idFactsFilterList;
  private EmbeddedNeo4j _db;

  @FXML private Text txt_msg;


  public AppController() {
    instance = this;
    _entities = FXCollections.observableSet();
    _relationships = FXCollections.observableSet();
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
   * loads a comprehensive collection (set) of entities for embedded views to use
   */
  private void loadEntities() {
    // populate entities (triggers value changes)
    _entities.clear();
    _entities.addAll(_db.readAllEntities());
    EventBus.getDefault().post(new EntitiesUpdated(_entities));
  }

  /**
   * loads a comprehensive collection (set) of relationships for embedded views to use
   */
  private void loadRelationships() {
    // populate relationships (triggers value changes)
    _relationships.clear();
    _relationships.addAll(_db.readAllRelationships());
    EventBus.getDefault().post(new RelationshipsUpdated(_relationships));
  }

  /**
   * loaded action for the controller overall
   */
  public void loaded_action() {
    // register with event bus
    EventBus.getDefault().register(this);

    loadEntities();
    loadRelationships();
    EventBus.getDefault().post(new FilterSubmitted(""));

  }

  @Override public void initialize(URL location, ResourceBundle resources) {

  }


  //=== event listeners =========================================================================\\
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(DoFactCreate ev) {
    var fact = ev.fact;
    txt_msg.setFill(Color.FIREBRICK);
    txt_msg.setText("Saving fact: " + fact);
    _db.createFact(fact);

    _relationships.add(fact.getRelationship());

    _entities.addAll(Set.of(fact.getSubject(), fact.getObject()));
    EventBus.getDefault().post(new EntitiesUpdated(_entities));

  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void on(DoFactUpdate ev) {
    var previous = ev.previous;
    var current = ev.current;
    _db.deleteFact(previous);
    _db.createFact(current);

    // relationship change -> event
    if (!previous.getRelationship().equals(current.getRelationship())) {
      loadRelationships();
    }

    // entity change -> event
    var changes = new HashMap<String, String>();
    if (!previous.getSubject().equals(current.getSubject())) {
      changes.put(previous.getSubject(), current.getSubject());
    }

    if (!previous.getObject().equals(current.getObject())) {
      changes.put(previous.getObject(), current.getObject());
    }

    changes.forEach((p, c) -> {
      _entities.remove(p);
      _entities.add(c);
    });

    if (changes.size() > 0) {
      loadEntities();
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void on(DoFactDelete ev) {
    _db.deleteFact(ev.fact);
    loadEntities();
    loadRelationships();
  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FilterSubmitted ev) {
    Set<Fact> facts;
    if (ev.filter.equals("")) {
      facts = _db.readAllFacts();
    } else {
      facts = _db.readRelatedFacts(ev.filter);
    }
    EventBus.getDefault().post(new RelatedFactsRead(facts, _db));
  }


}
