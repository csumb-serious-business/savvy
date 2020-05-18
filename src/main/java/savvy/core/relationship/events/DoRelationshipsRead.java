package savvy.core.relationship.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * fired to initiate reading relationships from the DB
 */
public final class DoRelationshipsRead {
  public final String filter;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public DoRelationshipsRead(String filter) {
    log.info("filter: \"{}\"", filter);
    this.filter = filter;
  }
}
