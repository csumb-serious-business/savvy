package savvy.core.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.fact.Fact;

/**
 * fired upon creation of fact in the DB
 */
public final class FactCreated {
  public final Fact fact;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public FactCreated(Fact fact) {
    log.info("fact: {}", fact);
    this.fact = fact;
  }
}
