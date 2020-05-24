package savvy.core.entity.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.entity.Entity;

/** fired upon creation of fact in the DB */
public final class EntityCreated {
  public final Entity entity;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public EntityCreated(Entity entity) {
    log.info("fact: {}", entity);
    this.entity = entity;
  }
}
