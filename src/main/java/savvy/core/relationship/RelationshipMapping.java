package savvy.core.relationship;

/**
 * conveniently wraps relevant relationship properties for verbatim views
 */
public class RelationshipMapping {
  public final Relationship relationship;
  public final boolean isOutbound;
  public final String modifiers;

  public RelationshipMapping(Relationship relationship, boolean isOutbound, String modifiers) {
    this.relationship = relationship;
    this.isOutbound = isOutbound;
    this.modifiers = modifiers;
  }
}
