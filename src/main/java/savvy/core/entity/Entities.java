package savvy.core.entity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.db.EmbeddedNeo4j;
import savvy.core.entity.events.DoEntitiesRead;
import savvy.core.entity.events.DoEntityUpdate;
import savvy.core.entity.events.EntitiesNamesUpdated;
import savvy.core.entity.events.EntitiesRead;
import savvy.core.entity.events.EntityUpdated;
import savvy.core.fact.events.FactCreated;
import savvy.core.fact.events.FactDeleted;
import savvy.core.fact.events.FactUpdated;

import java.util.ArrayList;
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
  private List<String> getNames() {
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

    var names = extractNames(_db.readAllEntities());

    // todo this is a stop-gap for full-fledged entities
    _names.addAll(names);
    notifyNamesUpdated();
  }

  private List<String> extractNames(Collection<Entity> entities) {
    return entities.stream().map(Entity::getName).sorted().collect(Collectors.toList());
  }



  /**
   * clears and reloads the data in this Entities
   * on some updates, it is impossible to predict the change
   * in the db, instead of guessing, we reload everything
   */
  private void refresh() {
    _items.clear();
    _names.clear();
    _aliases.clear();

    var names = extractNames(_db.readAllEntities());

    _names.addAll(names);
    notifyNamesUpdated();
  }


  /**
   * adds an entity
   *
   * @param entity the entity to add
   */
  private void add(Entity entity) {
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
  private void add(String name) {
    _names.add(name);
    notifyNamesUpdated();

  }

  /**
   * adds a collection of entities by name
   *
   * @param names the entity names
   */
  private void addAll(Collection<String> names) {
    names.forEach(this::add);
    notifyNamesUpdated();
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
  private void update(Entity previous, Entity current) {
    var changed = false;
    if (_names.contains(previous.getName())) {
      _names.remove(previous.getName());
      changed = true;
    }

    if (!_names.contains(current.getName())) {
      _names.add(current.getName());
      changed = true;
    }

    if (changed) {
      notifyNamesUpdated();
    }
  }

  //=== event listeners =========================================================================\\
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(DoEntitiesRead ev) {
    Set<Entity> entNames;

    if (ev.filter.equals("")) {
      entNames = _db.readAllEntities();
    } else {
      entNames = _db.readMatchingEntities(ev.filter);
    }

    _items.clear();
    var toAdd =
      entNames.stream().map(n -> new Entity(n.getName(), Set.of())).collect(Collectors.toSet());
    _items.addAll(toAdd);

    var result = new ArrayList<>(_items).stream().sorted().collect(Collectors.toList());

    EventBus.getDefault().post(new EntitiesRead(result));

  }

  // fact create -> addAll
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(DoEntityUpdate ev) {
    if (ev.previous.equals(ev.current)) {
      return;
    }
    var pName = ev.previous.getName();
    var cName = ev.current.getName();
    if (!ev.previous.hasSameName(ev.current)) {
      _db.renameEntity(pName, cName);
    }

    _db.createEntity(cName);

    EventBus.getDefault().post(new EntityUpdated(ev.previous, ev.current));
  }

  // fact create -> addAll
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FactCreated ev) {
    var names = extractNames(ev.fact.getEntities());
    addAll(names);
  }

  // fact update -> update
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FactUpdated ev) {
    var previous = ev.previous;
    var current = ev.current;

    // entity change -> event
    var changes = new HashMap<Entity, Entity>();
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

  @Subscribe(threadMode = ThreadMode.MAIN) public void on(EntityUpdated ev) {
    update(ev.previous, ev.current);
  }

}
