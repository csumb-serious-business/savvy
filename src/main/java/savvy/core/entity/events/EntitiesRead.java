package savvy.core.entity.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.entity.Entity;

import java.util.Set;

/**
 * fired upon return of entities from the DB
 */
public final class EntitiesRead {
  public final Set<Entity> entities;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public EntitiesRead(Set<Entity> entities) {
    log.info("relationships: {}", entities.size());
    this.entities = entities;
  }
}
