package savvy.core.relationship;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Relationship {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  private final String name;
  private final Map<String, String> correlates;

  public Relationship(String name, Map<String, String> correlates) {
    this.name = name;
    this.correlates = correlates;
  }

  public String getName() {
    return name;
  }

  public Map<String, String> getCorrelates() {
    return correlates;
  }
}
