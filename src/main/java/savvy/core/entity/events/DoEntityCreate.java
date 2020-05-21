package savvy.core.entity.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.entity.Entity;

/**
 * fired to initiate Entity creation in the DB
 */
public final class DoEntityCreate {
  public final Entity entity;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public DoEntityCreate(Entity entity) {
    log.info("fact: {}", entity);
    this.entity = entity;
  }
}
