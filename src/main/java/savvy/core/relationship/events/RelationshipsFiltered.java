package savvy.core.relationship.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.relationship.Relationship;

import java.util.List;

/**
 * fired upon return of filtered list of Relationships
 */
public final class RelationshipsFiltered {
  public final List<Relationship> relationships;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public RelationshipsFiltered(List<Relationship> entities) {
    log.info("relationships: {}", entities.size());
    this.relationships = entities;
  }
}
