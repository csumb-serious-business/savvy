package savvy.ui.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * fired upon app's entities collection (set) update
 */
public final class EntitiesUpdated {
  public final Set<String> entities;
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  public EntitiesUpdated(Set<String> entities) {
    log.info("entities: {}", entities.size());
    this.entities = entities;
  }
}
