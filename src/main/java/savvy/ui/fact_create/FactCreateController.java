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
import savvy.core.entity.EntitiesNamesUpdated;
import savvy.core.events.DoFactCreate;
import savvy.core.fact.Fact;
import savvy.core.relationship.RelationshipsNamesUpdated;

import java.net.URL;
import java.util.ResourceBundle;

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
    var subject = _subject.getText();
    var relationship = _relationship.getText();
    var object = _object.getText();

    var fact = new Fact(subject, relationship, object);
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

    _sb = TextFields.bindAutoCompletion(_subject, ev.entities);
    _ob = TextFields.bindAutoCompletion(_object, ev.entities);
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
