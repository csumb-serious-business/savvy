package savvy.core.fact.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.fact.Fact;

/**
 * fired to initiate Fact update in the DB
 */
public final class DoFactUpdate {
  public final Fact previous;
  public final Fact current;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public DoFactUpdate(Fact previous, Fact current) {
    log.info("fact: {} -> {}", previous, current);
    this.previous = previous;
    this.current = current;
  }
}
