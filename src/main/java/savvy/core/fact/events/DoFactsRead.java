package savvy.core.fact.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** fired to initiate reading related Facts from the DB */
public final class DoFactsRead {
  public final String filter;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public DoFactsRead(String filter) {
    log.info("filter: \"{}\"", filter);
    this.filter = filter;
  }
}
