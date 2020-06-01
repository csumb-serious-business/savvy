package savvy.core.relationship.events;

import savvy.ui.relationships_list.RelationshipsListController;

public class DoSelectFilterRelationship {
  public void selectFilter(RelationshipsListController relationshipsListController) {
    relationshipsListController.positionCaret();
  }
}
