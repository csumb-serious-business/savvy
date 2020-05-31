// adapted from: https://stackoverflow.com/questions/12737829/javafx-textfield-resize-to-text-length
package savvy.ui.common;

import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class TextFieldUtil {

  private static final Text _text = new Text();

  private static double calculateWidth(TextField field) {

    _text.setText(field.getText());
    _text.setFont(field.getFont());

    _text.setWrappingWidth(0d);
    _text.setLineSpacing(0d);

    double d = Math.min(_text.prefWidth(-1d), 0d);

    _text.setWrappingWidth((int) Math.ceil(d));
    return Math.ceil(_text.getLayoutBounds().getWidth());
  }

  public static void addAutoWidth(TextField field, double min, double max) {
    field.setMinWidth(min);
    field.setMaxWidth(max);
    field.setPrefWidth(calculateWidth(field));
    field.textProperty().addListener((o, p, c) -> field.setPrefWidth(calculateWidth(field) + 20));
  }
}
