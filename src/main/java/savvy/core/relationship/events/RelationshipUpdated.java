package savvy.core.relationship.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.relationship.Relationship;

public class RelationshipUpdated {
  public final Relationship previous;
  public final Relationship current;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public RelationshipUpdated(Relationship previous, Relationship current) {
    log.info("relationship: {} -> {}", previous, current);
    this.previous = previous;
    this.current = current;
  }
}
