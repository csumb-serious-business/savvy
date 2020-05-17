package savvy.ui.facts_filter_list;

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
import savvy.core.entity.EntitiesNamesUpdated;
import savvy.core.fact.FactCreated;
import savvy.core.fact.RelatedFactsRead;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the Fact Item list and filter
 */
public class FactsFilterListController implements Initializable {

  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
  @FXML private ListView<FactItemView> lv_facts;
  @FXML private TextField _filter;
  private AutoCompletionBinding<String> _fb = null;

  /**
   * filters the facts list view
   */
  public void filter_action() {
    log.info("filter facts list: {}", "");

    var filter = _filter.getText();
    EventBus.getDefault().post(new FilterSubmitted(filter));

  }

  @Override public void initialize(URL location, ResourceBundle resources) {
    EventBus.getDefault().register(this);
  }


  //=== event listeners =========================================================================\\

  // related facts read -> populate facts list
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(RelatedFactsRead ev) {
    lv_facts.getItems().clear();
    List<FactItemView> layouts =
      ev.facts.stream().map(it -> new FactItemView(it, lv_facts)).collect(Collectors.toList());

    lv_facts.getItems().addAll(layouts);

  }

  // entities names updated -> autocomplete list
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(EntitiesNamesUpdated ev) {
    if (_fb != null) {
      _fb.dispose();
    }
    _fb = TextFields.bindAutoCompletion(_filter, ev.entities);
  }

  // fact created -> add item to facts list
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FactCreated ev) {
    lv_facts.getItems().add(new FactItemView(ev.fact, lv_facts));
  }

}
