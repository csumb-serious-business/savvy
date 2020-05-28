package savvy.ui.app;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
import savvy.core.fact.events.DoFactsSearch;
import savvy.core.fact.events.FactCreated;
import savvy.core.fact.events.FactDeleted;
import savvy.core.fact.events.FactUpdated;
import savvy.core.relationship.Relationships;
import savvy.core.relationship.events.DoRelationshipsFilter;

/** Controller for the application's main window */
public class AppController implements Initializable {
  public static AppController instance;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  private final Entities _entities;
  private final Relationships _relationships;
  private final Facts _facts;

  private EmbeddedNeo4j _db;

  @FXML private TabPane tabs;
  @FXML private Text txt_app_msg;
  @FXML private Tab tab_facts;
  @FXML private Tab tab_entities;
  @FXML private Tab tab_relationships;

  public AppController() {
    instance = this;
    _facts = new Facts();
    _entities = new Entities();
    _relationships = new Relationships();
  }

  /**
   * sets the embedded neo4j db; this application won't run without it
   *
   * @param db embedded neo4j
   */
  public void setDB(EmbeddedNeo4j db) {
    this._db = db;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {}

  // === events ==================================================================================\\
  // --- Emitters --------------------------------------------------------------------------------\\
  /** loaded action for the controller overall */
  public void loaded_action() {
    // register with event bus
    EventBus.getDefault().register(this);

    _facts.init(_db);
    _entities.init(_db);
    _relationships.init(_db);

    // manually populate the lists
    EventBus.getDefault().post(new DoFactsSearch(""));
    EventBus.getDefault().post(new DoRelationshipsFilter(""));
    EventBus.getDefault().post(new DoEntitiesFilter(""));
  }

  // --- DO listeners ----------------------------------------------------------------------------\\
  // do show tab -> switch tab
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(DoShowTab ev) {
    switch (ev.code) {
      case F:
        tabs.getSelectionModel().select(tab_facts);
        break;
      case E:
        tabs.getSelectionModel().select(tab_entities);
        break;
      case R:
        tabs.getSelectionModel().select(tab_relationships);
        break;
    }
  }

  // --- ON listeners ---------------------------------------------------------------------------\\

  // fact created -> message
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(FactCreated ev) {
    txt_app_msg.setText("Saved fact: " + ev.fact);
  }

  // fact updated -> message
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(FactUpdated ev) {
    txt_app_msg.setText("Updated fact: " + ev.previous + " -> " + ev.current);
  }

  // fact deleted -> message
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(FactDeleted ev) {
    txt_app_msg.setText("Deleted fact: " + ev.fact);
  }
}
