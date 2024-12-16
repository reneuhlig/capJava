package customer.store.handlers;

import com.sap.cds.services.cds.CqnService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.persistence.PersistenceService;
import com.sap.cds.Result;
import com.sap.cds.ql.Delete;
import com.sap.cds.ql.Insert;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.Update;
import com.sap.cds.services.cds.CdsCreateEventContext;
import com.sap.cds.services.cds.CdsReadEventContext;
import com.sap.cds.services.cds.CdsDeleteEventContext;
import com.sap.cds.services.cds.CdsUpdateEventContext;
import cds.gen.inventoryservice.Products_;
import cds.gen.inventoryservice.Articles_;
import cds.gen.inventoryservice.Articles;
import cds.gen.inventoryservice.Products;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;



@Component
@ServiceName("InventoryService")
public class InventoryServiceHandler implements EventHandler{
    
    @Autowired
    private PersistenceService db;
    
    @On(event = CqnService.EVENT_CREATE, entity = Articles_.CDS_NAME)
    public void createArticles(CdsCreateEventContext context, List<Articles> articles) {
        Result result = db.run(Insert.into(Articles_.class).entries(articles));
        context.setResult(result);
    }

    @On(event = CqnService.EVENT_READ, entity = Articles_.CDS_NAME)
    public void readArticles(CdsReadEventContext context) {
        Result result = db.run(Select.from(Articles_.class));
        context.setResult(result);
    }

    @On(event= CqnService.EVENT_UPDATE, entity = Articles_.CDS_NAME)
    public void updateArticles(CdsUpdateEventContext context, Articles article) {
    Result result = db.run(Update.entity(Articles_.class).data(article).where(entry -> entry.ID().eq(article.getId())));
    context.setResult(result);
    }

    @On(event= CqnService.EVENT_DELETE, entity = Articles_.CDS_NAME)
    public void deleteArticles(CdsDeleteEventContext context, Articles article) {
        Result result = db.run(Delete.from(Articles_.class).where(entry -> entry.ID().eq(article.getId())));
        context.setResult(result);
    }

    @On(event = CqnService.EVENT_READ, entity = Products_.CDS_NAME)
    public void readProducts(CdsReadEventContext context) {

        Result productResult = db.run(Select.from(Products_.class));
        Result articlesResult = db.run(Select.from(Articles_.class));
    
        Map<String, Integer> articleCountMap = new HashMap<>();
        articlesResult.listOf(Articles.class).forEach(article -> {
            String productId = article.getProductId();
            articleCountMap.put(productId, articleCountMap.getOrDefault(productId, 0) + 1);
        });
    
        List<Products> products = productResult.listOf(Products.class);
        products.forEach(product -> {
            Integer stock = articleCountMap.getOrDefault(product.getId(), 0);
            product.setStock(stock);
            product.setTotalValue(product.getPrice().multiply(new BigDecimal(stock)));
        });

        bubbleSort(products);
    
  
        context.setResult(products);
    }
    

    public void bubbleSort(List<Products> products) {
        int n = products.size();
        boolean swapped;
    
      
        do {
            swapped = false;
            for (int i = 0; i < n - 1; i++) {
           
                if (products.get(i).getTotalValue().compareTo(products.get(i + 1).getTotalValue()) < 0) {
 
                    Products temp = products.get(i);
                    products.set(i, products.get(i + 1));
                    products.set(i + 1, temp);
                    swapped = true;
                }
            }
            n--; 
        } while (swapped);
    }


}