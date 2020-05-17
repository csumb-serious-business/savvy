package savvy.core.entity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.db.EmbeddedNeo4j;
import savvy.core.fact.FactCreated;
import savvy.core.fact.FactDeleted;
import savvy.core.fact.FactUpdated;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * models a group of entities
 */
public class Entities {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  private final Set<Entity> _items;
  private final Set<String> _names;
  private final Set<String> _aliases;

  private EmbeddedNeo4j _db;

  public Entities() {
    _items = new HashSet<>();
    _names = new HashSet<>();
    _aliases = new HashSet<>();
  }

  /**
   * @return a sorted list of names among all entities
   */
  public List<String> getNames() {
    return _names.stream().sorted().collect(Collectors.toList());
  }

  /**
   * @return a sorted list of identifiers among all entities
   * these include all names and aliases
   */
  public List<String> getIdentifiers() {
    var set = new HashSet<>(_names);
    set.addAll(_aliases);
    return set.stream().sorted().collect(Collectors.toList());
  }


  /**
   * fires a names updated notification for listeners
   */
  private void notifyNamesUpdated() {
    EventBus.getDefault().post(new EntitiesNamesUpdated(getNames()));
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

    var entities = _db.readAllEntities();

    // todo this is a stop-gap for full-fledged entities
    _names.addAll(entities);
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
    _aliases.clear();

    _names.addAll(_db.readAllEntities());
    notifyNamesUpdated();
  }


  /**
   * adds an entity
   *
   * @param entity the entity to add
   */
  public void add(Entity entity) {
    _items.add(entity);
    _names.add(entity.getName());
    _aliases.addAll(entity.getAliases());
    notifyNamesUpdated();
  }

  /**
   * adds an entity by name
   *
   * @param name the entity's name
   */
  public void add(String name) {
    _names.add(name);
    notifyNamesUpdated();

  }

  /**
   * adds a collection of entities by name
   *
   * @param names the entity names
   */
  public void addAll(Collection<String> names) {
    names.forEach(this::add);
  }

  //  public void addAll(Collection<Entity> entities) {
  //    entities.forEach(this::add);
  //  }


  /**
   * updates an entity, given a previous name and a current name
   *
   * @param previous the entity name to change from
   * @param current  the entity name to change to
   */
  public void update(String previous, String current) {
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
  // fact create -> addAll
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FactCreated ev) {
    addAll(ev.fact.getEntities());
  }

  // fact update -> update
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FactUpdated ev) {
    var previous = ev.previous;
    var current = ev.current;

    // entity change -> event
    var changes = new HashMap<String, String>();
    if (!previous.getSubject().equals(current.getSubject())) {
      changes.put(previous.getSubject(), current.getSubject());
    }

    if (!previous.getObject().equals(current.getObject())) {
      changes.put(previous.getObject(), current.getObject());
    }

    changes.forEach(this::update);
  }

  // fact deleted -> refresh
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FactDeleted ev) {
    refresh();
  }


}
