package savvy.ui.entities_list;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * fired upon filter request submitted, used to trigger a db lookup of related facts
 */
public final class EntitiesFilterAction {
  public final String filter;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public EntitiesFilterAction(String filter) {
    log.info("entities filter: \"{}\"", filter);
    this.filter = filter;
  }
}
