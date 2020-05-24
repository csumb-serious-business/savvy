package savvy.core.relationship;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.db.EmbeddedNeo4j;
import savvy.core.fact.events.FactCreated;
import savvy.core.fact.events.FactDeleted;
import savvy.core.fact.events.FactUpdated;
import savvy.core.relationship.events.DoRelationshipUpdate;
import savvy.core.relationship.events.DoRelationshipsFilter;
import savvy.core.relationship.events.DoRelationshipsRead;
import savvy.core.relationship.events.RelationshipUpdated;
import savvy.core.relationship.events.RelationshipsFiltered;
import savvy.core.relationship.events.RelationshipsRead;

/** models a group of relationships */
public class Relationships {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  private final Set<Relationship> _items;

  private EmbeddedNeo4j _db;

  public Relationships() {
    _items = new HashSet<>();
  }

  public static List<Relationship> getRelationshipsWithForm(
      Collection<Relationship> relationships, String correlate) {
    return relationships.stream().filter(r -> r.hasForm(correlate)).collect(Collectors.toList());
  }

  public List<Relationship> getRelationshipsWithForm(String correlate) {
    return Relationships.getRelationshipsWithForm(_items, correlate);
  }

  /**
   * initializes this with a given db
   *
   * @param db the db to use
   */
  public void init(EmbeddedNeo4j db) {
    _db = db;

    // register with event bus
    EventBus.getDefault().register(this);

    refresh();
  }

  /**
   * clears and reloads the data in this Entities on some updates, it is impossible to predict the
   * change in the db, instead of guessing, we reload everything
   */
  public void refresh() {
    _items.clear();
    _items.addAll(_db.readAllRelationships());

    var result = new ArrayList<>(_items).stream().sorted().collect(Collectors.toList());
    EventBus.getDefault().post(new RelationshipsRead(result));
  }

  /**
   * updates a particular relationship in the db broadcasts the change on the event bus
   *
   * @param previous previous version of the Relationship
   * @param current current version of the Relationship
   */
  private void relationshipUpdate(Relationship previous, Relationship current) {
    if (previous.equals(current)) {
      return;
    }

    _db.updateRelationship(previous, current);

    EventBus.getDefault().post(new RelationshipUpdated(previous, current));
  }

  /**
   * broadcasts a filtered copy of contained Relationship items on the event bus
   *
   * @param filter the string filter to match against
   */
  private void relationshipsFilter(String filter) {
    List<Relationship> relationships;

    if (filter.equals("")) {
      relationships = _items.stream().sorted().collect(Collectors.toList());
    } else {
      relationships =
          _items.stream().filter(i -> i.hasForm(filter)).sorted().collect(Collectors.toList());
    }

    EventBus.getDefault().post(new RelationshipsFiltered(relationships));
  }

  // === event listeners =========================================================================\\
  // --- DOs -------------------------------------------------------------------------------------\\
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(DoRelationshipsRead ev) {
    refresh();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(DoRelationshipsFilter ev) {
    relationshipsFilter(ev.filter);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(DoRelationshipUpdate ev) {
    relationshipUpdate(ev.previous, ev.current);
  }

  // --- ONs -------------------------------------------------------------------------------------\\
  // fact created -> refresh
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(FactCreated ev) {
    refresh();
  }

  // fact updated -> refresh
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(FactUpdated ev) {
    refresh();
  }

  // fact deleted -> refresh
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(FactDeleted ev) {
    refresh();
  }

  // relationship updated -> refresh
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(RelationshipUpdated ev) {
    refresh();
  }
}
