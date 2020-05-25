package savvy.core.fact.events;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.fact.Fact;

/** fired upon return of searched facts from the DB */
public final class FactsSearched {
  public final List<Fact> facts;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public FactsSearched(List<Fact> facts) {
    log.info("facts: {}", facts.size());
    this.facts = facts;
  }
}
