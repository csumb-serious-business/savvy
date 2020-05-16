package savvy.ui.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public final class EntitiesUpdateEV {
  public final Set<String> entities;
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  public EntitiesUpdateEV(Set<String> entities) {
    log.info("entities: {}", entities.size());
    this.entities = entities;
  }
}
