package savvy.core.entity;

/** conveniently wraps relevant entity properties for verbatim views */
public class EntityMapping {
  public final Entity entity;
  public final String modifiers;

  public EntityMapping(Entity entity, String modifiers) {
    this.entity = entity;
    this.modifiers = modifiers;
  }
}
