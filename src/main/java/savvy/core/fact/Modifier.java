package savvy.core.fact;

import java.io.Serializable;
import java.util.Objects;

/**
 * for concrete relationship instances, accounts for entity/relationship modifiers and relationship
 * direction
 */
public class Modifier implements Comparable<Modifier>, Serializable {
  public final boolean isOutbound;
  public final String subject;
  public final String relationship;
  public final String object;

  public Modifier(boolean isOutbound, String subject, String relationship, String object) {
    this.isOutbound = isOutbound;
    this.subject = subject;
    this.relationship = relationship;
    this.object = object;
  }

  @Override
  public int compareTo(Modifier o) {
    return this.toString().compareTo(o.toString());
  }

  @Override
  public String toString() {
    return "Modifiers{"
        + "isOutbound="
        + isOutbound
        + ", subject='"
        + subject
        + '\''
        + ", relationship='"
        + relationship
        + '\''
        + ", object='"
        + object
        + '\''
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Modifier)) return false;
    Modifier modifiers = (Modifier) o;
    return isOutbound == modifiers.isOutbound
        && Objects.equals(subject, modifiers.subject)
        && Objects.equals(relationship, modifiers.relationship)
        && Objects.equals(object, modifiers.object);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isOutbound, subject, relationship, object);
  }
}
