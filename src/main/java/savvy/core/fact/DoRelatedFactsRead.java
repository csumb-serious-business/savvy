package savvy.core.fact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * fired to initiate reading related Facts from the DB
 */
public final class DoRelatedFactsRead {
  public final String name;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public DoRelatedFactsRead(String name) {
    log.info("name: \"{}\"", name);
    this.name = name;
  }
}
