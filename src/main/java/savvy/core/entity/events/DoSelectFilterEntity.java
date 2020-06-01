package savvy.core.entity.events;

import savvy.ui.entities_list.EntitiesListController;

public class DoSelectFilterEntity {
  public void selectFilter(EntitiesListController entitiesListController) {
    entitiesListController.positionCaret();
  }
}
