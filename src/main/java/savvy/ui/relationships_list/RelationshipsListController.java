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
import savvy.core.relationship.events.RelationshipsNamesUpdated;
import savvy.core.relationship.events.RelationshipsRead;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
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

  @Override public void initialize(URL location, ResourceBundle resources) {
    EventBus.getDefault().register(this);
  }

  //=== event listeners =========================================================================\\

  // relationship names updated -> autocomplete list
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(RelationshipsNamesUpdated ev) {

    if (_fb != null) {
      // dispose old autocomplete binding if it exists
      _fb.dispose();
    }
    _fb = TextFields.bindAutoCompletion(_filter, ev.names);
  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void on(RelationshipsRead ev) {
    lv_relationships.getItems().clear();
    List<RelationshipItemView> layouts =
      ev.relationships.getItems().stream().map(it -> new RelationshipItemView(it, lv_relationships))
        .collect(Collectors.toList());
    lv_relationships.getItems().addAll(layouts);

  }

}
