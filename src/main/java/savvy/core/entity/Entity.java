package savvy.core.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * an entity, can be either a subject or object in a Fact
 */
public class Entity implements Comparable<Entity> {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  // authoritative name for the entity
  private final String name;

  // a set of aliases for the entity
  private final Set<String> aliases;

  public Entity(String name, Set<String> aliases) {
    this.name = name;
    this.aliases = aliases;
  }

  public Set<String> getIdentifiers() {
    var identifiers = new HashSet<>(this.aliases);
    identifiers.add(name);

    return identifiers;
  }

  @Override public String toString() {
    return "Entity{" + "name='" + name + '\'' + ", aliases=" + aliases + '}';
  }

  public String getName() {
    return name;
  }

  public Set<String> getAliases() {
    return aliases;
  }

  @Override public int compareTo(Entity o) {
    return this.toString().compareTo(o.toString());
  }

  public boolean hasSameName(Entity o) {
    return this.getName().equals(o.getName());
  }

  public boolean hasSameAliases(Entity o) {
    return this.getAliases().equals(o.getAliases());
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Entity))
      return false;
    Entity entity = (Entity) o;
    return Objects.equals(name, entity.name) && Objects.equals(aliases, entity.aliases);
  }

  @Override public int hashCode() {
    return Objects.hash(name, aliases);
  }
}
