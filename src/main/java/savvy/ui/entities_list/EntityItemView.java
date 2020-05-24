package savvy.ui.entities_list;

import java.util.stream.Collectors;
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
import savvy.core.entity.events.DoEntityUpdate;

/** controller and layout for an individual Fact Item in the FactsFilterList */
public class EntityItemView extends HBox {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  private final ListView<EntityItemView> _parent;
  private Entity _entity;

  public EntityItemView(Entity entity, ListView<EntityItemView> parent) {
    super();

    this.setSpacing(10);

    _entity = entity;
    _parent = parent;

    viewMode();
  }

  /** creates & wires the layout for this item's view mode */
  private void viewMode() {
    var lbl_name = new Label();
    lbl_name.setText(_entity.getName());

    var lbl_aliases = new Label();
    lbl_aliases.setText(String.join(", ", _entity.getAliases()));

    var gap = new Region();
    HBox.setHgrow(gap, Priority.ALWAYS);

    // no deletion, since that would cascade to all facts.
    // (causing possibly unexpected consequences)
    // instead when all related facts have on reference to
    // a relationship it is removed

    var btn_edit = new Button();
    btn_edit.setText("Edit");
    btn_edit.setOnAction(ev -> this.editMode());
    this.getChildren().clear();
    this.getChildren().addAll(lbl_name, lbl_aliases, gap, btn_edit);
  }

  /** creates & wires the layout for this item's edit mode */
  private void editMode() {

    final double width = this.widthProperty().doubleValue() / 5.0d;

    var name = new TextField();
    name.setText(_entity.getName());
    name.setMaxWidth(width);

    // for each alias, add a text field
    var hb_aliases = new HBox();
    _entity
        .getAliases()
        .forEach(
            a -> {
              var field = new TextField();
              field.setText(a);
              hb_aliases.getChildren().add(field);
            });

    // add a blank text field for new aliases
    hb_aliases.getChildren().add(new TextField());

    // todo when an alias box is blank and is not the last remaining,
    //  remove it when it loses focus

    var gap = new Region();
    HBox.setHgrow(gap, Priority.ALWAYS);

    var btn_cancel = new Button();
    btn_cancel.setText("Cancel");

    btn_cancel.setOnAction(ev -> this.viewMode());

    var btn_save = new Button();
    btn_save.setText("Save");
    btn_save.setOnAction(
        ev -> {
          var aliasNames =
              hb_aliases.getChildren().stream()
                  .filter(TextField.class::isInstance)
                  .map(a -> ((TextField) a).getText())
                  .filter(s -> s.length() > 0)
                  .collect(Collectors.toSet());

          // go straight to view-mode if no change
          var entity = new Entity(name.getText(), aliasNames);

          if (_entity.equals(entity)) {
            this.viewMode();
            return;
          }

          // update the relationship data
          EventBus.getDefault().post(new DoEntityUpdate(_entity, entity));

          // update the relationship
          this._entity = new Entity(name.getText(), aliasNames);

          // go back to view mode
          this.viewMode();
        });
    this.getChildren().clear();
    this.getChildren().addAll(name, hb_aliases, gap, btn_cancel, btn_save);
  }
}
