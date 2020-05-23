package savvy.ui.relationships_list;

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
import savvy.core.relationship.Correlate;
import savvy.core.relationship.Relationship;
import savvy.core.relationship.events.DoRelationshipUpdate;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * controller and layout for an individual Fact Item in the FactsFilterList
 */
public class RelationshipItemView extends HBox {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  private final ListView<RelationshipItemView> _parent;
  private Relationship _relationship;

  public RelationshipItemView(Relationship relationship, ListView<RelationshipItemView> parent) {
    super();

    this.setSpacing(10);

    _relationship = relationship;
    _parent = parent;

    viewMode();

  }


  /**
   * creates & wires the layout for this item's view mode
   */
  private void viewMode() {
    var lbl_name = new Label();
    lbl_name.setText(_relationship.getName());

    var correlates =
      _relationship.getCorrelates().stream().map(c -> c.outgoing + " -> " + c.incoming).sorted()
        .collect(Collectors.toList());
    var lbl_correlates = new Label();
    lbl_correlates.setText(String.join(", ", correlates));


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
    this.getChildren().addAll(lbl_name, lbl_correlates, gap, btn_edit);
  }

  /**
   * creates & wires the layout for this item's edit mode
   */
  private void editMode() {

    final double width = this.widthProperty().doubleValue() / 5.0d;

    var name = new TextField();
    name.setText(_relationship.getName());
    name.setMaxWidth(width);

    var hb_correlates = new HBox();

    _relationship.getCorrelates().forEach(c -> hb_correlates.getChildren().add(toUI(c)));

    hb_correlates.getChildren().add(toUI(new Correlate("", "")));

    hb_correlates.setMaxWidth(width);

    var gap = new Region();
    HBox.setHgrow(gap, Priority.ALWAYS);

    var btn_cancel = new Button();
    btn_cancel.setText("Cancel");

    btn_cancel.setOnAction(ev -> this.viewMode());

    var btn_save = new Button();
    btn_save.setText("Save");
    btn_save.setOnAction(ev -> {

      var correlates = hb_correlates.getChildren().stream().map(c -> fromUI((HBox) c))
        .filter(c -> c.incoming.length() > 0 && c.outgoing.length() > 0)
        .collect(Collectors.toSet());

      // go straight to view-mode if no change
      var relationship = new Relationship(name.getText(), correlates);

      if (_relationship.equals(relationship)) {
        this.viewMode();
        return;
      }

      // update the relationship data
      EventBus.getDefault().post(new DoRelationshipUpdate(_relationship, relationship));

      // update the relationship
      this._relationship = new Relationship(name.getText(), Set.of());

      // go back to view mode
      this.viewMode();
    });
    this.getChildren().clear();
    this.getChildren().addAll(name, hb_correlates, gap, btn_cancel, btn_save);

  }

  private HBox toUI(Correlate correlate) {
    var txt_out = new TextField();
    txt_out.setText(correlate.outgoing);

    var txt_in = new TextField();
    txt_in.setText(correlate.incoming);

    var label = new Label(" -> ");

    var box = new HBox();
    box.getChildren().addAll(txt_out, label, txt_in);
    return box;

  }

  private Correlate fromUI(HBox box) {
    var fields =
      box.getChildren().stream().filter(TextField.class::isInstance).collect(Collectors.toList());
    var outgoing = ((TextField) fields.get(0)).getText();
    var incoming = ((TextField) fields.get(1)).getText();

    return new Correlate(outgoing, incoming);
  }
}
