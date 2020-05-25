package savvy.core.fact.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** fired to initiate Fact creation in the DB */
public final class DoFactCreate {
  public final String subject;
  public final String relationship;
  public final String object;

  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public DoFactCreate(String subject, String relationship, String object) {
    log.info("proposed fact: {} | {} | {}", subject, relationship, object);
    this.subject = subject;
    this.relationship = relationship;
    this.object = object;
  }
}
