package savvy.core.relationship;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class Relationship {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  // authoritative name for the relationship
  private final String name;

  // pairs of outgoing and incoming mappings between subjects and objects in
  private final Set<Correlate> correlates;

  public Relationship(String name, Set<Correlate> correlates) {
    this.name = name;
    this.correlates = correlates;
  }

  public String getName() {
    return name;
  }

  public Set<Correlate> getCorrelates() {
    return correlates;
  }

  @Override public String toString() {
    return "Relationship{" + "name='" + name + '\'' + ", correlates=" + correlates + '}';
  }
}
