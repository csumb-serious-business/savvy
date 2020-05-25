package savvy.ui.facts_list;

import java.util.Set;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.entity.Entity;
import savvy.core.fact.Fact;
import savvy.core.fact.events.DoFactDelete;
import savvy.core.fact.events.DoFactUpdate;
import savvy.core.relationship.Relationship;

/** controller and layout for an individual Fact Item in the FactsFilterList */
public class FactItemView extends HBox {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  private final ListView<FactItemView> _parent;
  private Fact _fact;
  private boolean inEditMode = false;

  /**
   * claims a fact update and applies it
   *
   * @param previous fact version
   * @param current fact version
   */
  public void claimUpdate(Fact previous, Fact current) {
    if (_fact.equals(previous)) {
      _fact = current;
      if (inEditMode) {
        editMode();
      } else {
        viewMode();
      }
    }
  }

  public FactItemView(Fact fact, ListView<FactItemView> parent) {
    super();

    this.setSpacing(10);

    this._fact = fact;
    this._parent = parent;

    viewMode();
  }

  /** creates & wires the layout for this item's view mode */
  private void viewMode() {
    inEditMode = false;
    var label = new Label();
    label.setText(_fact.toString());

    var gap = new Region();
    HBox.setHgrow(gap, Priority.ALWAYS);

    var btn_delete = new Button();
    btn_delete.setText("Delete");

    btn_delete.setOnAction(
        ev -> {
          EventBus.getDefault().post(new DoFactDelete(_fact));
          _parent.getItems().remove(this);
        });

    var btn_edit = new Button();
    btn_edit.setText("Edit");
    btn_edit.setOnAction(ev -> this.editMode());
    this.getChildren().clear();
    this.getChildren().addAll(label, gap, btn_edit, btn_delete);
  }

  /** creates & wires the layout for this item's edit mode */
  private void editMode() {
    inEditMode = true;

    final double width = this.widthProperty().doubleValue() / 5.0d;

    var subject = new TextField();
    subject.setText(_fact.getSubject().getName());
    subject.setMaxWidth(width);

    var relationship = new TextField();
    relationship.setText(_fact.getRelationship().getName());
    relationship.setMaxWidth(width);

    var object = new TextField();
    object.setText(_fact.getObject().getName());
    object.setMaxWidth(width);

    var gap = new Region();
    HBox.setHgrow(gap, Priority.ALWAYS);

    var btn_cancel = new Button();
    btn_cancel.setText("Cancel");

    btn_cancel.setOnAction(ev -> this.viewMode());

    var btn_save = new Button();
    btn_save.setText("Save");
    btn_save.setOnAction(
        ev -> {
          // go straight to view-mode if no change
          var s = new Entity(subject.getText(), Set.of());
          var r = new Relationship(relationship.getText(), Set.of());
          var o = new Entity(object.getText(), Set.of());
          var fact = new Fact(s, r, o);

          if (this._fact.equals(fact)) {
            this.viewMode();
            return;
          }

          // update the fact data
          EventBus.getDefault().post(new DoFactUpdate(_fact, fact));

          // don't update the underlying fact -- it is handled by event bus

          // go back to view mode
          this.viewMode();
        });
    this.getChildren().clear();
    this.getChildren().addAll(subject, relationship, object, gap, btn_cancel, btn_save);
  }
}
