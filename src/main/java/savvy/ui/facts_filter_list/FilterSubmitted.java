package savvy.ui.facts_filter_list;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * fired upon filter request submitted, used to trigger a db lookup of related facts
 */
public final class FilterSubmitted {
  public final String filter;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public FilterSubmitted(String filter) {
    log.info("fact name: \"{}\"", filter);
    this.filter = filter;
  }
}
