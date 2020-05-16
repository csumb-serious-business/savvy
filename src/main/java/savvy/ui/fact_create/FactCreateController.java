package savvy.ui.fact_create;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import org.controlsfx.control.textfield.TextFields;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.Fact;
import savvy.ui.app.EntitiesUpdateEV;
import savvy.ui.app.RelationshipsUpdateEV;

import java.net.URL;
import java.util.ResourceBundle;

public class FactCreateController implements Initializable {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  @FXML private TextField _subject;
  @FXML private TextField _relationship;
  @FXML private TextField _object;

  public void save_action() {
    var subject = _subject.getText();
    var relationship = _relationship.getText();
    var object = _object.getText();

    var fact = new Fact(subject, relationship, object);
    log.info("save fact: {}", fact);
    EventBus.getDefault().post(new FactCreateEV(fact));
  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void onEntitiesUpdate(EntitiesUpdateEV ev) {
    TextFields.bindAutoCompletion(_subject, ev.entities);
    TextFields.bindAutoCompletion(_object, ev.entities);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onRelationshipsUpdate(RelationshipsUpdateEV ev) {
    TextFields.bindAutoCompletion(_relationship, ev.relationships);
  }

  @Override public void initialize(URL location, ResourceBundle resources) {
    EventBus.getDefault().register(this);

  }
}
