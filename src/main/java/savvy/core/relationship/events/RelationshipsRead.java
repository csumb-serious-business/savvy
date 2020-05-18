package savvy.core.relationship.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.relationship.Relationships;

/**
 * fired upon return of relationships from the DB
 */
public final class RelationshipsRead {
  public final Relationships relationships;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public RelationshipsRead(Relationships relationships) {
    log.info("relationships: {}", relationships.getItems().size());
    this.relationships = relationships;
  }
}
