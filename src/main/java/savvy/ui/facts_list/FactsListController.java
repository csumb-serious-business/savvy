package savvy.ui.facts_list;

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
import savvy.core.fact.events.FactCreated;
import savvy.core.fact.events.FactsRead;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the Fact Item list and filter
 */
public class FactsListController implements Initializable {

  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
  @FXML private ListView<FactItemView> lv_facts;
  @FXML private TextField _filter;
  private AutoCompletionBinding<String> _fb = null;

  /**
   * filters the facts list view
   */
  public void filter_action() {
    var filter = _filter.getText();
    log.info("filter facts list: {}", filter);
    EventBus.getDefault().post(new FactsFilterAction(filter));
  }

  @Override public void initialize(URL location, ResourceBundle resources) {
    EventBus.getDefault().register(this);

  }


  //=== event listeners =========================================================================\\

  // related facts read -> populate facts list
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FactsRead ev) {
    lv_facts.getItems().clear();
    List<FactItemView> layouts =
      ev.facts.getItems().stream().map(it -> new FactItemView(it, lv_facts))
        .collect(Collectors.toList());

    lv_facts.getItems().addAll(layouts);

  }

  // entities names updated -> autocomplete list
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(EntitiesNamesUpdated ev) {
    // dispose old autocomplete binding if it exists
    if (_fb != null) {
      _fb.dispose();
    }
    _fb = TextFields.bindAutoCompletion(_filter, ev.names);
  }

  // fact created -> add item to facts list
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FactCreated ev) {
    lv_facts.getItems().add(new FactItemView(ev.fact, lv_facts));
  }

}
