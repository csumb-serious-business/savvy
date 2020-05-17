package savvy.core.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * an entity, can be either a subject or object in a Fact
 */
public class Entity {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  private final String name;
  private final Set<String> aliases;

  public Entity(String name, Set<String> aliases) {
    this.name = name;
    this.aliases = aliases;
  }

  public String getName() {
    return name;
  }

  public Set<String> getAliases() {
    return aliases;
  }
}
