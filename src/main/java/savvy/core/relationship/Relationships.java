package savvy.core.relationship;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.db.EmbeddedNeo4j;
import savvy.core.fact.events.FactCreated;
import savvy.core.fact.events.FactDeleted;
import savvy.core.fact.events.FactUpdated;
import savvy.core.relationship.events.DoRelationshipsRead;
import savvy.core.relationship.events.RelationshipsNamesUpdated;
import savvy.core.relationship.events.RelationshipsRead;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * models a group of relationships
 */
public class Relationships {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  private final Set<Relationship> _items;
  private final Set<String> _names;
  private final Map<String, String> _correlates;

  private EmbeddedNeo4j _db;

  public Relationships() {
    _items = new HashSet<>();
    _names = new HashSet<>();
    _correlates = new HashMap<>();
  }

  /**
   * @return a sorted list of names among all relationships
   */
  public List<String> getNames() {
    return _names.stream().sorted().collect(Collectors.toList());
  }

  /**
   * fires a names updated notification for listeners
   */
  private void notifyNamesUpdated() {
    EventBus.getDefault().post(new RelationshipsNamesUpdated(getNames()));
  }

  /**
   * iniltializes this with a given db
   *
   * @param db the db to use
   */
  public void init(EmbeddedNeo4j db) {
    _db = db;

    // register with event bus
    EventBus.getDefault().register(this);

    var relationships = _db.readAllRelationships();

    // todo this is a stop-gap for full-fledged relationships
    _names.addAll(relationships);
    notifyNamesUpdated();

  }

  /**
   * clears and reloads the data in this Entities
   * on some updates, it is impossible to predict the change
   * in the db, instead of guessing, we reload everything
   */
  public void refresh() {
    _items.clear();
    _names.clear();
    _correlates.clear();

    _names.addAll(_db.readAllRelationships());
    notifyNamesUpdated();
  }

  /**
   * adds a relationship by name
   *
   * @param name the relationship's name
   */
  public void add(String name) {
    // todo -- stop-gap
    if (_names.contains(name)) {
      return;
    }
    _names.add(name);
    notifyNamesUpdated();
  }

  /**
   * updates an relationship, given a previous name and a current name
   *
   * @param previous the relationship name to change from
   * @param current  the relationship name to change to
   */
  public void update(String previous, String current) {
    // todo -- stop-gap
    var changed = false;
    if (_names.contains(previous)) {
      _names.remove(previous);
      changed = true;
    }

    if (!_names.contains(current)) {
      _names.add(current);
      changed = true;
    }

    if (changed) {
      notifyNamesUpdated();
    }
  }

  //=== event listeners =========================================================================\\

  @Subscribe(threadMode = ThreadMode.MAIN) public void on(DoRelationshipsRead ev) {
    Set<String> relNames;

    if (ev.filter.equals("")) {
      relNames = _db.readAllRelationships();
    } else {
      relNames = _db.readMatchingRelationships(ev.filter);
    }

    _items.clear();
    var toAdd =
      relNames.stream().map(n -> new Relationship(n, Set.of())).collect(Collectors.toSet());
    _items.addAll(toAdd);

    EventBus.getDefault().post(new RelationshipsRead(_items));
  }

  // fact created -> add
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FactCreated ev) {
    add(ev.fact.getRelationship());
  }

  // fact updated -> update
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FactUpdated ev) {
    update(ev.previous.getRelationship(), ev.current.getRelationship());
  }

  // fact deleted -> refresh
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FactDeleted ev) {
    refresh();
  }


}
