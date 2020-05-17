package savvy.core.relationship.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.relationship.Relationship;

import java.util.Set;

/**
 * fired upon return of relationships from the DB
 */
public final class RelationshipsRead {
  public final Set<Relationship> relationships;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public RelationshipsRead(Set<Relationship> relationships) {
    log.info("relationships: {}", relationships.size());
    this.relationships = relationships;
  }
}
