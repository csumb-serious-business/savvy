package savvy.ui.fact_create;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.entity.Entities;
import savvy.core.entity.Entity;
import savvy.core.entity.events.EntitiesRead;
import savvy.core.fact.Fact;
import savvy.core.fact.events.DoFactCreate;
import savvy.core.relationship.Relationship;
import savvy.core.relationship.events.RelationshipsRead;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for the Fact Creation view
 */
public class FactCreateController implements Initializable {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  @FXML private TextField _subject;
  @FXML private TextField _relationship;
  @FXML private TextField _object;

  private AutoCompletionBinding<String> _sb = null;
  private AutoCompletionBinding<String> _rb = null;
  private AutoCompletionBinding<String> _ob = null;

  private List<Entity> _entities = null;
  private List<Relationship> _relationships = null;

  /**
   * saves a fact
   */
  public void save_action() {
    Entity s;

    // subject exists -> use existent
    var found = Entities.getEntityWithIdentifier(_entities, _subject.getText());
    if (found.isEmpty()) {
      s = new Entity(_subject.getText(), Set.of());
    } else {
      s = found.get(0);
    }

    // relationship exists -> use existent
    var r = new Relationship(_relationship.getText(), Set.of());

    Entity o;
    // object exists -> use existent
    found = Entities.getEntityWithIdentifier(_entities, _object.getText());
    if (found.isEmpty()) {
      o = new Entity(_object.getText(), Set.of());
    } else {
      o = found.get(0);
    }

    var fact = new Fact(s, r, o);
    log.info("save fact: {}", fact);

    EventBus.getDefault().post(new DoFactCreate(fact));
  }

  @Override public void initialize(URL location, ResourceBundle resources) {
    EventBus.getDefault().register(this);
  }

  /**
   * updates the autocomplete filter
   *
   * @param entities to use for the suggestions
   */
  private void updateEntitiesAutocomplete(Collection<Entity> entities) {
    // clear old bindings
    if (_sb != null) {
      _sb.dispose();
    }

    if (_ob != null) {
      _ob.dispose();
    }

    var names = entities.stream().map(Entity::getName).sorted().collect(Collectors.toList());
    _sb = TextFields.bindAutoCompletion(_subject, names);
    _ob = TextFields.bindAutoCompletion(_object, names);

  }

  /**
   * updates the autocomplete filter
   *
   * @param relationships to use for the suggestions
   */
  private void updateRelationshipsAutocomplete(Collection<Relationship> relationships) {
    // clear old binding
    if (_rb != null) {
      _rb.dispose();
    }

    // todo -- should be relationship and all correlates [MBR]
    var rels =
      relationships.stream().map(Relationship::getName).sorted().collect(Collectors.toList());

    _rb = TextFields.bindAutoCompletion(_relationship, rels);
  }

  //=== event listeners =========================================================================\\

  // entities read -> update entities autocomplete
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(EntitiesRead ev) {
    _entities = ev.entities;
    updateEntitiesAutocomplete(ev.entities);
  }

  // relationships read -> update relationships autocomplete
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(RelationshipsRead ev) {
    _relationships = ev.relationships;
    updateRelationshipsAutocomplete(ev.relationships);
  }


}
