package savvy.core.fact.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.fact.Fact;

/**
 * fired to initiate Fact creation in the DB
 */
public final class DoFactCreate {
  public final Fact fact;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public DoFactCreate(Fact fact) {
    log.info("fact: {}", fact);
    this.fact = fact;
  }
}
