package savvy.core.relationship.events;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.relationship.Relationship;

/** fired upon return of relationships from the DB */
public final class RelationshipsRead {
  public final List<Relationship> relationships;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public RelationshipsRead(List<Relationship> relationships) {
    log.info("relationships: {}", relationships.size());
    this.relationships = relationships;
  }
}
