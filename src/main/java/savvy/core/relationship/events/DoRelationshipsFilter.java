package savvy.core.relationship.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** fired to initiate gathering a filtered list of relationships */
public final class DoRelationshipsFilter {
  public final String filter;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public DoRelationshipsFilter(String filter) {
    log.info("filter: \"{}\"", filter);
    this.filter = filter;
  }
}
