package savvy.core.fact.events;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.entity.Entity;
import savvy.core.fact.Fact;

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
    if (!previous.subject.equals(current.subject)) {
      changes.put(previous.subject, current.subject);
    }

    if (!previous.object.equals(current.object)) {
      changes.put(previous.object, current.object);
    }
    entityChanges = changes;
  }
}
