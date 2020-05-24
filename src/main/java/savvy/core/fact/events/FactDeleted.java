package savvy.core.fact.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.fact.Fact;

/** fired upon deletion of fact in the DB */
public final class FactDeleted {
  public final Fact fact;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public FactDeleted(Fact fact) {
    log.info("fact: {}", fact);
    this.fact = fact;
  }
}
