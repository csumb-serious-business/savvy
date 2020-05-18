package savvy.core.entity.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.entity.Entity;

public class EntityUpdated {
  public final Entity previous;
  public final Entity current;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public EntityUpdated(Entity previous, Entity current) {
    log.info("entity: {} -> {}", previous, current);
    this.previous = previous;
    this.current = current;
  }
}
