package savvy.core.fact.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** fired to initiate searching related Facts from the DB */
public final class DoFactsSearch {
  public final String filter;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public DoFactsSearch(String filter) {
    log.info("searching for: \"{}\"", filter);
    this.filter = filter;
  }
}
