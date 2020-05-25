package savvy.ui.facts_list;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.entity.Entity;
import savvy.core.entity.events.EntitiesRead;
import savvy.core.entity.events.EntityUpdated;
import savvy.core.fact.Fact;
import savvy.core.fact.events.DoFactsSearch;
import savvy.core.fact.events.FactCreated;
import savvy.core.fact.events.FactUpdated;
import savvy.core.fact.events.FactsSearched;
import savvy.core.relationship.events.RelationshipUpdated;

/** Controller for the Fact Item list and filter */
public class FactsListController implements Initializable {

  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
  @FXML private ListView<FactItemView> lv_facts;
  @FXML private TextField _filter;
  private AutoCompletionBinding<String> _fb = null;
  private String lastFilter = "";

  /**
   * updates the autocomplete filter
   *
   * @param entities to use for the suggestions
   */
  private void updateEntitiesAutocomplete(Collection<Entity> entities) {
    // clear old bindings
    if (_fb != null) {
      _fb.dispose();
    }

    // todo -- this is messy, receive something clean and use it directly [MBR]
    var identifiers =
        entities.stream()
            .map(Entity::getIdentifiers)
            .flatMap(Set::stream)
            .sorted()
            .collect(Collectors.toList());

    _fb = TextFields.bindAutoCompletion(_filter, identifiers);
  }

  private void refreshFact(Fact previous, Fact current) {
    lv_facts.getItems().forEach(it -> it.claimUpdate(previous, current));
  }

  /**
   * clears and repopulates the facts in the list view
   *
   * @param facts to populate into the view
   */
  private void refresh(Collection<Fact> facts) {
    lv_facts.getItems().clear();
    List<FactItemView> layouts =
        facts.stream().map(it -> new FactItemView(it, lv_facts)).collect(Collectors.toList());

    lv_facts.getItems().addAll(layouts);
  }

  // === events ==================================================================================\\
  // --- Emitters --------------------------------------------------------------------------------\\
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    EventBus.getDefault().register(this);
  }

  /** filters the facts list view */
  public void filter_action() {
    var filter = _filter.getText();
    lastFilter = filter;
    log.info("filter facts list: {}", filter);
    EventBus.getDefault().post(new DoFactsSearch(filter));
  }

  // --- DO listeners ----------------------------------------------------------------------------\\
  // NONE

  // --- ON listeners ---------------------------------------------------------------------------\\
  // related facts read -> populate facts list
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(FactsSearched ev) {
    refresh(ev.facts);
  }

  // entities names updated -> autocomplete list
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(EntitiesRead ev) {
    updateEntitiesAutocomplete(ev.entities);
  }

  // fact created -> add item to facts list
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(FactCreated ev) {
    lv_facts.getItems().add(new FactItemView(ev.fact, lv_facts));
  }

  // fact updated -> item's internal values
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(FactUpdated ev) {
    refreshFact(ev.previous, ev.current);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(EntityUpdated ev) {
    EventBus.getDefault().post(new DoFactsSearch(""));
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(RelationshipUpdated ev) {
    EventBus.getDefault().post(new DoFactsSearch(""));
  }
}
