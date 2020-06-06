package savvy.ui.facts_list;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
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
  @FXML private TextField _filter_A;
  @FXML private TextField _filter_B;
  @FXML private HBox _filters;
  private AutoCompletionBinding<String> _fAb = null;
  private AutoCompletionBinding<String> _fBb = null;
  private List<String> _identifiers;

  /**
   * updates the autocomplete filter
   *
   * @param entities to use for the suggestions
   */
  private void updateEntitiesAutocomplete(Collection<Entity> entities) {
    // clear old bindings
    if (_fAb != null) {
      _fAb.dispose();
    }

    if (_fBb != null) {
      _fBb.dispose();
    }

    // todo -- this is messy, receive something clean and use it directly [MBR]
    _identifiers =
        entities.stream()
            .map(Entity::getIdentifiers)
            .flatMap(Set::stream)
            .sorted()
            .collect(Collectors.toList());

    _fAb = TextFields.bindAutoCompletion(_filter_A, _identifiers);
    _fBb = TextFields.bindAutoCompletion(_filter_B, _identifiers);
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

  // todo -- this does not work due to
  //  the next new field not being added to the tab order [MBR]
  @FXML
  private void updateFilterFields() {
    log.info("updating filter field");

    // retain non-empty fields
    var checkBlanks = _filters.getChildren().filtered(TextField.class::isInstance);
    var retain = new ArrayList<Node>();
    checkBlanks.forEach(
        f -> {
          if (!((TextField) f).getText().isBlank()) {
            retain.add(f);
          }
        });

    _filters.getChildren().clear();

    AtomicInteger s = new AtomicInteger();

    // set ids for traversal
    retain.forEach(f -> f.setId("f" + s.getAndIncrement()));

    var toAdd = new TextField();
    toAdd.setOnKeyTyped(ev -> updateFilterFields());
    TextFields.bindAutoCompletion(toAdd, _identifiers);
    toAdd.setOnAction(ev -> filter_action());
    toAdd.setId("f" + s.getAndIncrement());

    retain.add(toAdd);
    _filters.getChildren().addAll(retain);
  }

  // === events ==================================================================================\\
  // --- Emitters --------------------------------------------------------------------------------\\
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    EventBus.getDefault().register(this);
  }

  /** filters the facts list view */
  @FXML
  private void filter_action() {
    var filterA = _filter_A.getText();
    var filterB = _filter_B.getText();

    log.info("filter facts list -- A: {}, B: {}", filterA, filterB);

    // search for both
    EventBus.getDefault().post(new DoFactsSearch(List.of(filterA, filterB)));

    _filter_A.clear();
    _filter_B.clear();

    _filter_A.requestFocus();
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
    EventBus.getDefault().post(new DoFactsSearch(List.of()));
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(RelationshipUpdated ev) {
    EventBus.getDefault().post(new DoFactsSearch(List.of()));
  }
}
