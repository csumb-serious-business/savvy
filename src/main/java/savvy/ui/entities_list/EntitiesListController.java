package savvy.ui.entities_list;

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
import savvy.core.entity.events.EntitiesFiltered;
import savvy.core.entity.events.EntitiesRead;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public class EntitiesListController implements Initializable {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  @FXML private ListView<EntityItemView> lv_entities;
  @FXML private TextField _filter;

  private AutoCompletionBinding<String> _fb = null;

  public void filter_action() {
    var filter = _filter.getText();
    log.info("filter entities list: {}", filter);

    EventBus.getDefault().post(new EntitiesFilterAction(filter));
  }

  /**
   * updates the autocomplete filter
   *
   * @param entities to use for the suggestions
   */
  private void updateAutocomplete(Collection<Entity> entities) {
    // dispose old autocomplete binding if it exists
    if (_fb != null) {
      _fb.dispose();
    }

    var identifiers = entities.stream().map(Entity::getIdentifiers).flatMap(Set::stream).sorted()
      .collect(Collectors.toList());

    _fb = TextFields.bindAutoCompletion(_filter, identifiers);
  }

  /**
   * updates the entities list view
   *
   * @param entities to populate the list view with
   */
  private void updateEntitiesLV(Collection<Entity> entities) {
    lv_entities.getItems().clear();
    List<EntityItemView> layouts =
      entities.stream().map(it -> new EntityItemView(it, lv_entities)).collect(Collectors.toList());
    lv_entities.getItems().addAll(layouts);
  }

  @Override public void initialize(URL location, ResourceBundle resources) {
    EventBus.getDefault().register(this);
  }

  //=== event listeners =========================================================================\\
  // entities read -> update autocomplete & list view
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(EntitiesRead ev) {
    updateAutocomplete(ev.entities);
    updateEntitiesLV(ev.entities);
  }

  // entities filtered -> update list view
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(EntitiesFiltered ev) {
    updateEntitiesLV(ev.entities);
  }

}
