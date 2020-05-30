package savvy.ui.relationships_list;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import savvy.ui.common.TextUtils;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * The type Relationship item view.
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

  private void viewMode() {
    var lbl_name = new Label();
    lbl_name.setText(_relationship.getName());

    var correlates =
            _relationship.getCorrelates().stream()
                    .map(c -> c.outbound + " ⇔ " + c.inbound)
                    .sorted()
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

  private void editMode() {

//    final double width = this.widthProperty().doubleValue() / 1.7d;
    final double width = this.widthProperty().doubleValue();
    var name = new TextField();
    computeSpacing(name,20);//sets spacing to roughly the width of the text
    name.setText(_relationship.getName());

    var hb_correlates = new HBox();

    _relationship.getCorrelates().forEach(c -> hb_correlates.getChildren().add(toUI(c)));

    hb_correlates.getChildren().add(toUI(new Correlate("", "")));

    var gap = new Region();
    HBox.setHgrow(gap, Priority.ALWAYS);

    var btn_cancel = new Button();
    btn_cancel.setText("Cancel");

    btn_cancel.setOnAction(ev -> this.viewMode());
    var btn_save = new Button();
    btn_save.setText("Save");
    btn_save.setOnAction(
            ev -> {
              var correlates =
                      hb_correlates.getChildren().stream()
                              .map(c -> fromUI((HBox) c))
                              .filter(c -> c.inbound.length() > 0 && c.outbound.length() > 0)
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

  /**
   * given a correlate, generates a UI representation in a HBox
   *
   * @param correlate to render
   * @return an HBox
   */
  private HBox toUI(Correlate correlate) {
    var txt_out = new TextField();
    computeSpacing(txt_out,15);
    txt_out.setText(correlate.outbound);

    var txt_in = new TextField();
    computeSpacing(txt_in,15);
    txt_in.setText(correlate.inbound);

    var label = new Label(" -> ");

    var box = new HBox();
    box.getChildren().addAll(txt_out, label, txt_in);
    return box;
  }

  /**
   * given an HBox representation of a correlate, extracts the correlate's values
   *
   * @param box to extract from
   * @return a correlate based on the HBox's data
   */
  private Correlate fromUI(HBox box) {
    var fields =
            box.getChildren().stream().filter(TextField.class::isInstance).collect(Collectors.toList());
    var outgoing = ((TextField) fields.get(0)).getText();
    var incoming = ((TextField) fields.get(1)).getText();
    return new Correlate(outgoing, incoming);
  }


  /**
   * given a TextField and spacing value, compute the length and set textfield to
   * roughly that length not exceeding 150 units
   * @param textField to extract width from
   * @param spacing add spacing to the end
   */
  private void computeSpacing(TextField textField, double spacing) {
    textField.setMinWidth(70);
    textField.setPrefWidth(100);
    textField.setMaxWidth(150);
    textField.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        textField.setPrefWidth(TextUtils.computeTextWidth(textField.getFont(),
                textField.getText(), 0.0D) + spacing);
      }
    });
  }
}
