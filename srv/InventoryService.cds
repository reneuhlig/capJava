using { sap.capire.store as db } from '../db/schema';

service InventoryService {
    entity Products as projection on db.Products;
    entity Articles as projection on db.Articles;

    action sortProductsByTotalValue () returns array of Products;
}

