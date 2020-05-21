package savvy.ui.relationships_list;

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
import savvy.core.relationship.Relationship;
import savvy.core.relationship.events.RelationshipsFiltered;
import savvy.core.relationship.events.RelationshipsRead;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;


public class RelationshipsListController implements Initializable {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  @FXML private ListView<RelationshipItemView> lv_relationships;
  @FXML private TextField _filter;

  private AutoCompletionBinding<String> _fb = null;

  public void filter_action() {
    var filter = _filter.getText();
    log.info("filter relationships list: {}", filter);

    EventBus.getDefault().post(new RelationshipsFilterAction(filter));
  }

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
    var available = relationships.stream().map(Relationship::allForms).flatMap(Set::stream).sorted()
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
    var layouts = relationships.stream().map(it -> new RelationshipItemView(it, lv_relationships))
      .collect(Collectors.toList());
    lv_relationships.getItems().addAll(layouts);
  }

  @Override public void initialize(URL location, ResourceBundle resources) {
    EventBus.getDefault().register(this);
  }



  //=== event listeners =========================================================================\\

  // relationship names updated -> update autocomplete & list view
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(RelationshipsRead ev) {
    updateAutocomplete(ev.relationships);
    updateRelationshipsLV(ev.relationships);
  }

  // relationships filtered -> update list view
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(RelationshipsFiltered ev) {
    updateRelationshipsLV(ev.relationships);
  }

}
