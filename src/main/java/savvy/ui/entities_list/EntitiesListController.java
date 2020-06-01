package savvy.ui.entities_list;

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
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.entity.Entity;
import savvy.core.entity.events.DoEntitiesFilter;
import savvy.core.entity.events.DoSelectFilterEntity;
import savvy.core.entity.events.EntitiesFiltered;
import savvy.core.entity.events.EntitiesRead;
import savvy.core.fact.events.DoSelectSubject;
import savvy.core.relationship.events.DoSelectFilterRelationship;
import savvy.ui.app.DoShowTab;

public class EntitiesListController implements Initializable {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  @FXML private ListView<EntityItemView> lv_entities;
  @FXML private TextField _filter;

  private AutoCompletionBinding<String> _fb = null;

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

    var identifiers =
        entities.stream()
            .map(Entity::getIdentifiers)
            .flatMap(Set::stream)
            .sorted()
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
        entities.stream()
            .map(it -> new EntityItemView(it, lv_entities))
            .collect(Collectors.toList());
    lv_entities.getItems().addAll(layouts);
  }

  // === events ==================================================================================\\
  /**
   * Handle action related to input (in this case specifically only responds to keyboard event ENTER
   * when on the filter field).
   *
   * @param event Input event.
   */
  @FXML
  private void handleKeyInput(final InputEvent event) {
    if (event instanceof KeyEvent) {
      final KeyEvent keyEvent = (KeyEvent) event;
      if (keyEvent.getCode() == KeyCode.ENTER) {
        filter_action();
      }
    }
  }
  // --- Emitters --------------------------------------------------------------------------------\\
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    EventBus.getDefault().register(this);
  }

  /** filters the list view */
  public void filter_action() {
    var filter = _filter.getText();
    log.info("filter entities list: {}", filter);

    EventBus.getDefault().post(new DoEntitiesFilter(filter));
    positionCaret();
  }

  public void positionCaret() {
    _filter.requestFocus();
    _filter.positionCaret(0);
    _filter.selectAll();
  }

  // --- DO listeners ----------------------------------------------------------------------------\\
  // NONE

  //  --- ON listeners ---------------------------------------------------------------------------\\
  // entities read -> update autocomplete & list view
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(EntitiesRead ev) {
    updateAutocomplete(ev.entities);
    updateEntitiesLV(ev.entities);
  }

  // entities filtered -> update list view
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(EntitiesFiltered ev) {
    updateEntitiesLV(ev.entities);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(DoSelectFilterEntity ev) {
    ev.selectFilter(this);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(DoShowTab ev) {
    switch (ev.code) {
      case F:
        EventBus.getDefault().post(new DoSelectSubject());
      case R:
        EventBus.getDefault().post(new DoSelectFilterRelationship());
    }
  }
}
