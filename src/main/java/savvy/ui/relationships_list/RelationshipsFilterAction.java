package savvy.ui.relationships_list;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** fired upon filter request submitted, used to trigger a db lookup of related facts */
public final class RelationshipsFilterAction {
  public final String filter;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public RelationshipsFilterAction(String filter) {
    log.info("relationships filter: \"{}\"", filter);
    this.filter = filter;
  }
}
