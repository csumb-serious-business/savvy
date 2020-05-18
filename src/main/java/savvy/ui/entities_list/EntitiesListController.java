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
import savvy.core.entity.events.EntitiesNamesUpdated;
import savvy.core.entity.events.EntitiesRead;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class EntitiesListController implements Initializable {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
  private AutoCompletionBinding<String> _fb = null;
  @FXML private ListView<EntityItemView> lv_entities;
  @FXML private TextField _filter;

  public void filter_action() {
    var filter = _filter.getText();
    log.info("filter entities list: {}", filter);

    EventBus.getDefault().post(new EntitiesFilterAction(filter));
  }

  @Override public void initialize(URL location, ResourceBundle resources) {
    EventBus.getDefault().register(this);
  }

  //=== event listeners =========================================================================\\

  // entities names updated -> autocomplete list
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(EntitiesNamesUpdated ev) {

    if (_fb != null) {
      // dispose old autocomplete binding if it exists
      _fb.dispose();
    }
    _fb = TextFields.bindAutoCompletion(_filter, ev.names);
  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void on(EntitiesRead ev) {
    lv_entities.getItems().clear();
    List<EntityItemView> layouts =
      ev.entities.stream().map(it -> new EntityItemView(it, lv_entities))
        .collect(Collectors.toList());
    lv_entities.getItems().addAll(layouts);

  }

}
