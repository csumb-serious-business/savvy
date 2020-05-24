package savvy.core.entity.events;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.entity.Entity;

/** fired upon return of entities from the DB */
public final class EntitiesRead {
  public final List<Entity> entities;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public EntitiesRead(List<Entity> entities) {
    log.info("entities: {}", entities.size());
    this.entities = entities;
  }
}
