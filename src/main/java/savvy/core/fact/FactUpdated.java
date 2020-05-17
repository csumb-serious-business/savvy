package savvy.core.fact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FactUpdated {
  public final Fact previous;
  public final Fact current;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public FactUpdated(Fact previous, Fact current) {
    log.info("fact: {} -> {}", previous, current);
    this.previous = previous;
    this.current = current;
  }
}
