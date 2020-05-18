package savvy.core.relationship.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.relationship.Relationship;


/**
 * fired to initiate updating a relationship in the DB
 */
public class DoRelationshipUpdate {
  public final Relationship previous;
  public final Relationship current;

  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public DoRelationshipUpdate(Relationship previous, Relationship current) {
    log.info("relationship: {} -> {}", previous, current);
    this.previous = previous;
    this.current = current;

  }
}
