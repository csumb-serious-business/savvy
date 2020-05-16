package savvy.core.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.Fact;
import savvy.core.db.EmbeddedNeo4j;

import java.util.Set;

/**
 * fired upon return of related facts from the DB
 */
public final class RelatedFactsRead {
  public final Set<Fact> facts;
  public final EmbeddedNeo4j db;
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  public RelatedFactsRead(Set<Fact> facts, EmbeddedNeo4j db) {
    log.info("facts: {}", facts.size());
    this.facts = facts;
    this.db = db;
  }
}
