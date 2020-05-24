package savvy.core.fact.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.fact.Facts;

/** fired upon return of related facts from the DB */
public final class FactsRead {
  public final Facts facts;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public FactsRead(Facts facts) {
    log.info("facts: {}", facts.getItems().size());
    this.facts = facts;
  }
}
