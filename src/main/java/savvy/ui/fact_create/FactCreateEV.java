package savvy.ui.fact_create;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.Fact;

public final class FactCreateEV {
  public final Fact fact;
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  public FactCreateEV(Fact fact) {
    log.info("fact: {}", fact);
    this.fact = fact;
  }
}
