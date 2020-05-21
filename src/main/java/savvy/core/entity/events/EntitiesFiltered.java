package savvy.core.entity.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.entity.Entity;

import java.util.List;

/**
 * fired upon return of entities from the DB
 */
public final class EntitiesFiltered {
  public final List<Entity> entities;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public EntitiesFiltered(List<Entity> entities) {
    log.info("entities: {}", entities.size());
    this.entities = entities;
  }
}
