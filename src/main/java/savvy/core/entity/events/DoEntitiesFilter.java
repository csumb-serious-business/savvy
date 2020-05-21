package savvy.core.entity.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * fired to initiate gathering a filtered list of entities
 */
public final class DoEntitiesFilter {
  public final String filter;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public DoEntitiesFilter(String filter) {
    log.info("filter: \"{}\"", filter);
    this.filter = filter;
  }
}
