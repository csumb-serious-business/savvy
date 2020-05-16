package savvy.core.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.Fact;

/**
 * fired to initiate Fact deletion in the DB
 */
public final class DoFactDelete {
  public final Fact fact;
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  public DoFactDelete(Fact fact) {
    log.info("fact: {}", fact);
    this.fact = fact;
  }
}
