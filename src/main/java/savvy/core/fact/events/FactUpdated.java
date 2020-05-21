package savvy.core.fact.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.entity.Entity;
import savvy.core.fact.Fact;

import java.util.HashMap;
import java.util.Map;

public class FactUpdated {
  public final Fact previous;
  public final Fact current;
  public final Map<Entity, Entity> entityChanges;

  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public FactUpdated(Fact previous, Fact current) {
    log.info("fact: {} -> {}", previous, current);
    this.previous = previous;
    this.current = current;

    // entity changes -> diff
    var changes = new HashMap<Entity, Entity>();
    if (!previous.getSubject().equals(current.getSubject())) {
      changes.put(previous.getSubject(), current.getSubject());
    }

    if (!previous.getObject().equals(current.getObject())) {
      changes.put(previous.getObject(), current.getObject());
    }
    entityChanges = changes;
  }
}
