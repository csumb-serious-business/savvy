package savvy.ui.relationships_list;

import java.net.URL;
import java.util.Collection;
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
import savvy.core.fact.events.DoSelectSubject;
import savvy.core.relationship.Relationship;
import savvy.core.relationship.events.DoRelationshipsFilter;
import savvy.core.relationship.events.DoSelectFilterRelationship;
import savvy.core.relationship.events.RelationshipsFiltered;
import savvy.core.relationship.events.RelationshipsRead;
import savvy.ui.app.DoShowTab;

public class RelationshipsListController implements Initializable {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  @FXML private ListView<RelationshipItemView> lv_relationships;
  @FXML private TextField _filter;

  private AutoCompletionBinding<String> _fb = null;

  /**
   * updates the autocomplete filter
   *
   * @param relationships to use for the suggestions
   */
  private void updateAutocomplete(Collection<Relationship> relationships) {
    // dispose old autocomplete binding if it exists
    if (_fb != null) {
      _fb.dispose();
    }
    var available =
        relationships.stream()
            .map(Relationship::allForms)
            .flatMap(Set::stream)
            .sorted()
            .collect(Collectors.toList());
    _fb = TextFields.bindAutoCompletion(_filter, available);
  }

  /**
   * updates the relationships list view
   *
   * @param relationships to populate the list view with
   */
  private void updateRelationshipsLV(Collection<Relationship> relationships) {
    lv_relationships.getItems().clear();
    var layouts =
        relationships.stream()
            .map(it -> new RelationshipItemView(it, lv_relationships))
            .collect(Collectors.toList());
    lv_relationships.getItems().addAll(layouts);
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

  /** filters the relationships view */
  public void filter_action() {
    var filter = _filter.getText();
    log.info("filter relationships list: {}", filter);

    EventBus.getDefault().post(new DoRelationshipsFilter(filter));
    _filter.clear();
    _filter.requestFocus();
  }

  public void positionCaret() {
    _filter.requestFocus();
    _filter.positionCaret(0);
    _filter.selectAll();
  }

  // --- DO listeners ----------------------------------------------------------------------------\\
  // NONE

  //  --- ON listeners ---------------------------------------------------------------------------\\
  // relationship names updated -> update autocomplete & list view
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(RelationshipsRead ev) {
    updateAutocomplete(ev.relationships);
    updateRelationshipsLV(ev.relationships);
  }

  // relationships filtered -> update list view
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(RelationshipsFiltered ev) {
    updateRelationshipsLV(ev.relationships);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(DoSelectFilterRelationship ev) {
    ev.selectFilter(this);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(DoShowTab ev) {
    switch (ev.code) {
      case E:
        EventBus.getDefault().post(new DoSelectFilterRelationship());
      case F:
        EventBus.getDefault().post(new DoSelectSubject());
    }
  }
}
