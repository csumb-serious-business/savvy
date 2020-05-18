package savvy.core.entity.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.entity.Entities;

/**
 * fired upon return of entities from the DB
 */
public final class EntitiesRead {
  public final Entities entities;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public EntitiesRead(Entities entities) {
    log.info("relationships: {}", entities.getItems().size());
    this.entities = entities;
  }
}
