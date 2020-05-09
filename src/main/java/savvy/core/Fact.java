package savvy.core;

public class Fact {
  private final String subject;
  private final String relationship;
  private final String object;

  public Fact(String subject, String relationship, String object) {
    this.subject = subject;
    this.relationship = relationship;
    this.object = object;
  }

  @Override public String toString() {
    return "Fact{" + "subject='" + subject + '\'' + ", relationship='" + relationship + '\''
      + ", object='" + object + '\'' + '}';
  }

  public String getSubject() {
    return subject;
  }

  public String getRelationship() {
    return relationship;
  }

  public String getObject() {
    return object;
  }
}
