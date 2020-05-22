package savvy.ui.app;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.db.EmbeddedNeo4j;
import savvy.core.entity.Entities;
import savvy.core.entity.events.DoEntitiesFilter;
import savvy.core.fact.Facts;
import savvy.core.fact.events.DoFactsRead;
import savvy.core.fact.events.FactCreated;
import savvy.core.fact.events.FactDeleted;
import savvy.core.fact.events.FactUpdated;
import savvy.core.relationship.Relationships;
import savvy.core.relationship.events.DoRelationshipsRead;
import savvy.ui.entities_list.EntitiesFilterAction;
import savvy.ui.facts_list.FactsFilterAction;
import savvy.ui.relationships_list.RelationshipsFilterAction;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the application's main window
 */
public class AppController implements Initializable {
  public static AppController instance;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  private final Entities _entities;
  private final Relationships _relationships;
  private final Facts _facts;

  private EmbeddedNeo4j _db;

  @FXML private Text txt_msg;


  public AppController() {
    instance = this;
    _facts = new Facts();
    _entities = new Entities();
    _relationships = new Relationships();
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
   * loaded action for the controller overall
   */
  public void loaded_action() {
    // register with event bus
    EventBus.getDefault().register(this);

    _facts.init(_db);
    _entities.init(_db);
    _relationships.init(_db);

    // manually populate the lists
    EventBus.getDefault().post(new FactsFilterAction(""));
    EventBus.getDefault().post(new RelationshipsFilterAction(""));
    EventBus.getDefault().post(new EntitiesFilterAction(""));

  }

  @Override public void initialize(URL location, ResourceBundle resources) {

  }


  //=== event listeners =========================================================================\\
  // fact created -> message
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FactCreated ev) {
    txt_msg.setText("Saved fact: " + ev.fact);
  }

  // fact updated -> message
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FactUpdated ev) {
    txt_msg.setText("Updated fact: " + ev.previous + " -> " + ev.current);
  }

  // fact deleted -> message
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FactDeleted ev) {
    txt_msg.setText("Deleted fact: " + ev.fact);
  }

  // facts filter submitted -> dispatch DoRelatedFactsRead
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FactsFilterAction ev) {
    var matches = _entities.getEntityWithIdentifier(ev.filter);
    log.info("matches: {}", matches);

    if (matches.size() > 0) {
      EventBus.getDefault().post(new DoFactsRead(matches.get(0).getName()));
    } else {
      EventBus.getDefault().post(new DoFactsRead(ev.filter));
    }
  }

  // relationships filter submitted -> dispatch DoRelationshipsRead
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(RelationshipsFilterAction ev) {
    EventBus.getDefault().post(new DoRelationshipsRead(ev.filter));
  }

  // entities filter submitted -> dispatch DoEntitiesRead
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(EntitiesFilterAction ev) {
    EventBus.getDefault().post(new DoEntitiesFilter(ev.filter));
  }

}
