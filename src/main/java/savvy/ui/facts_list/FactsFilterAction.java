package savvy.ui.facts_list;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * fired upon filter request submitted, used to trigger a db lookup of related facts
 */
public final class FactsFilterAction {
  public final String filter;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public FactsFilterAction(String filter) {
    log.info("facts filter: \"{}\"", filter);
    this.filter = filter;
  }
}
