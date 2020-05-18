package savvy.core.fact.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.fact.Fact;

import java.util.Set;

/**
 * fired upon return of related facts from the DB
 */
public final class FactsRead {
  public final Set<Fact> facts;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public FactsRead(Set<Fact> facts) {
    log.info("facts: {}", facts.size());
    this.facts = facts;
  }
}
