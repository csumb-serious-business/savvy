package savvy.core.relationship;

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
   * clears and reloads the data in this Entities on some updates, it is impossible to predict the
   * change in the db, instead of guessing, we reload everything
   */
  public List<Relationship> refresh() {
    _items.clear();
    _items.addAll(_db.readAllRelationships());

    return _items.stream().sorted().collect(Collectors.toList());
  }

  /**
   * updates a particular relationship in the db
   *
   * @param previous relationship version
   * @param current relationship version
   */
  private boolean relationshipUpdate(Relationship previous, Relationship current) {
    if (previous.equals(current)) {
      return false;
    }

    _db.updateRelationship(previous, current);

    return true;
  }

  /**
   * gets a filtered copy of contained relationship items
   *
   * @param filter to match against
   */
  private List<Relationship> relationshipsFilter(String filter) {
    if (filter.isBlank()) {
      return _items.stream().sorted().collect(Collectors.toList());
    } else {
      return _items.stream().filter(i -> i.hasForm(filter)).sorted().collect(Collectors.toList());
    }
  }

  // === events ==================================================================================\\
  // --- Emitters --------------------------------------------------------------------------------\\
  /**
   * initializes this with a given db
   *
   * @param db to use
   */
  public void init(EmbeddedNeo4j db) {
    _db = db;

    // register with event bus
    EventBus.getDefault().register(this);

    var read = refresh();

    EventBus.getDefault().post(new RelationshipsRead(read));
  }

  // --- DO listeners ----------------------------------------------------------------------------\\
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(DoRelationshipsRead ev) {
    var result = refresh();
    EventBus.getDefault().post(new RelationshipsRead(result));
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(DoRelationshipsFilter ev) {
    var filtered = relationshipsFilter(ev.filter);
    EventBus.getDefault().post(new RelationshipsFiltered(filtered));
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(DoRelationshipUpdate ev) {
    var success = relationshipUpdate(ev.previous, ev.current);
    if (success) {
      EventBus.getDefault().post(new RelationshipUpdated(ev.previous, ev.current));
    }
  }

  // --- ON listeners ---------------------------------------------------------------------------\\
  // fact created -> refresh
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(FactCreated ev) {
    var result = refresh();
    EventBus.getDefault().post(new RelationshipsRead(result));
  }

  // fact updated -> refresh
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(FactUpdated ev) {
    var result = refresh();
    EventBus.getDefault().post(new RelationshipsRead(result));
  }

  // fact deleted -> refresh
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(FactDeleted ev) {
    var result = refresh();
    EventBus.getDefault().post(new RelationshipsRead(result));
  }

  // relationship updated -> refresh
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(RelationshipUpdated ev) {
    var result = refresh();
    EventBus.getDefault().post(new RelationshipsRead(result));
  }
}
