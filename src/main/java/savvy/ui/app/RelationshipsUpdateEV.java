package savvy.ui.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public final class RelationshipsUpdateEV {
  public final Set<String> relationships;
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  public RelationshipsUpdateEV(Set<String> relationships) {
    log.info("relationships: {}", relationships.size());
    this.relationships = relationships;
  }
}
