package savvy.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import savvy.core.EmbeddedNeo4j;
import savvy.core.Fact;

public class FactItemView extends HBox {
  private final ListView<FactItemView> _parent;
  private final EmbeddedNeo4j _db;
  private Fact _fact;

  public FactItemView(Fact fact, ListView<FactItemView> parent, EmbeddedNeo4j db) {
    super();

    this.setSpacing(10);

    this._fact = fact;
    this._parent = parent;
    this._db = db;

    viewMode();

  }

  private void viewMode() {
    var label = new Label();
    label.setText(_fact.toString());

    var gap = new Region();
    HBox.setHgrow(gap, Priority.ALWAYS);

    var btn_delete = new Button();
    btn_delete.setText("Delete");

    btn_delete.setOnAction(ev -> {
      _db.removeData(_fact.getSubject(), _fact.getRelationship(), _fact.getObject());
      _parent.getItems().remove(this);
    });

    var btn_edit = new Button();
    btn_edit.setText("Edit");
    btn_edit.setOnAction(ev -> this.editMode());
    this.getChildren().clear();
    this.getChildren().addAll(label, gap, btn_edit, btn_delete);
  }

  private void editMode() {

    var subject = new TextField();
    subject.setText(_fact.getSubject());
    subject.setMaxWidth(this.widthProperty().doubleValue()/5.0);

    var relationship = new TextField();
    relationship.setText(_fact.getRelationship());
    relationship.setMaxWidth(this.widthProperty().doubleValue()/5.0);

    var object = new TextField();
    object.setText(_fact.getObject());
    object.setMaxWidth(this.widthProperty().doubleValue()/5.0);

    var gap = new Region();
    HBox.setHgrow(gap, Priority.ALWAYS);

    var btn_cancel = new Button();
    btn_cancel.setText("Cancel");

    btn_cancel.setOnAction(ev -> this.viewMode());

    var btn_save = new Button();
    btn_save.setText("Save");
    btn_save.setOnAction(ev -> {
      // remove the previous fact data
      _db.removeData(_fact.getSubject(), _fact.getRelationship(), _fact.getObject());
      // add the updated version
      _db.addData(subject.getText(), relationship.getText(), object.getText());

      // update the underlying fact
      this._fact = new Fact(subject.getText(), relationship.getText(), object.getText());

      // go back to view mode
      this.viewMode();
    });
    this.getChildren().clear();
    this.getChildren().addAll(subject, relationship, object, gap, btn_cancel, btn_save);

  }

}
