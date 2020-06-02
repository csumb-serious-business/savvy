package savvy.ui.app;

import javafx.scene.input.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TabShown {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public TabShown(KeyCode code){
        switch (code) {
            case E:
                log.info("Switched to Entities tab");
                break;
            case F:
                log.info("Switched to Facts tab");
                break;
            case R:
                log.info("Switched to Relationship tab");
                break;
        }
    }
}
