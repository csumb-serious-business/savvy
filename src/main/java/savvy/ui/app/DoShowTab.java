package savvy.ui.app;

import javafx.scene.input.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DoShowTab {
  public final KeyCode code;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public DoShowTab(KeyCode code) {
    log.info("code: {}", code);
    this.code = code;
  }
}
