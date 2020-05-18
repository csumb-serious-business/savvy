package savvy.ui.fact_create;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.entity.Entity;
import savvy.core.entity.events.EntitiesNamesUpdated;
import savvy.core.fact.Fact;
import savvy.core.fact.events.DoFactCreate;
import savvy.core.relationship.events.RelationshipsNamesUpdated;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Controller for the Fact Creation view
 */
public class FactCreateController implements Initializable {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  @FXML private TextField _subject;
  @FXML private TextField _relationship;
  @FXML private TextField _object;

  private AutoCompletionBinding<String> _sb = null;
  private AutoCompletionBinding<String> _rb = null;
  private AutoCompletionBinding<String> _ob = null;

  /**
   * saves a fact
   */
  public void save_action() {
    var s = new Entity(_subject.getText(), Set.of());
    var r = _relationship.getText();
    var o = new Entity(_object.getText(), Set.of());

    var fact = new Fact(s, r, o);
    log.info("save fact: {}", fact);
    EventBus.getDefault().post(new DoFactCreate(fact));
  }

  @Override public void initialize(URL location, ResourceBundle resources) {
    EventBus.getDefault().register(this);
  }


  //=== event listeners =========================================================================\\

  // app.entities -> autocomplete list
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(EntitiesNamesUpdated ev) {
    // clear old bindings
    if (_sb != null) {
      _sb.dispose();
    }

    if (_ob != null) {
      _ob.dispose();
    }

    _sb = TextFields.bindAutoCompletion(_subject, ev.names);
    _ob = TextFields.bindAutoCompletion(_object, ev.names);
  }

  // app.relationships -> autocomplete list
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(RelationshipsNamesUpdated ev) {

    // clear old binding
    if (_rb != null) {
      _rb.dispose();
    }

    _rb = TextFields.bindAutoCompletion(_relationship, ev.names);
  }
}
