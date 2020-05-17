package savvy.core.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * fired upon app's entities' names list update
 */
public final class EntitiesNamesUpdated {
  public final List<String> entities;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public EntitiesNamesUpdated(List<String> entities) {
    log.info("entities: {}", entities.size());
    this.entities = entities;
  }
}
