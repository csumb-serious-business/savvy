package savvy.core.relationship.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * fired upon app's relationships collection (set) update
 */
public final class RelationshipsNamesUpdated {
  public final List<String> names;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public RelationshipsNamesUpdated(List<String> names) {
    log.info("relationships: {}", names.size());
    this.names = names;
  }
}
