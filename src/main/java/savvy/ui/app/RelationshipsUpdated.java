package savvy.ui.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * fired upon app's relationships collection (set) update
 */
public final class RelationshipsUpdated {
  public final Set<String> relationships;
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  public RelationshipsUpdated(Set<String> relationships) {
    log.info("relationships: {}", relationships.size());
    this.relationships = relationships;
  }
}
