package customer.store.handlers;

import com.sap.cds.services.cds.CqnService;
import com.sap.cds.services.handler.annotations.After;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.Result;
import com.sap.cds.ql.CQL;
import com.sap.cds.ql.Delete;
import com.sap.cds.ql.Insert;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.Update;
import com.sap.cds.ql.cqn.CqnInsert;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.ql.cqn.CqnUpdate;
import com.sap.cds.reflect.CdsEntity;
import com.sap.cds.services.cds.CdsCreateEventContext;
import com.sap.cds.services.cds.CdsReadEventContext;
import com.sap.cds.services.cds.CdsDeleteEventContext;
import com.sap.cds.services.cds.CdsUpdateEventContext;
import com.sap.cds.services.EventContext;
import com.sap.cds.reflect.CdsModel;
import com.sap.cds.services.runtime.CdsRuntime;

import cds.gen.inventoryservice.Products_;
import cds.gen.inventoryservice.Articles_;
import cds.gen.inventoryservice.Articles;
import cds.gen.inventoryservice.Products;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;



@Component
@ServiceName("InventoryService")
public class InventoryServiceHandler {

    
    @On(event = CqnService.EVENT_CREATE, entity = Articles_.CDS_NAME)
    public void createArticles(CdsCreateEventContext context) {
        List<Map<String, Object>> articles = context.getCqn().entries();
        Result result = context.getService().run(Insert.into(Articles_.class).entries(articles));
        context.setResult(result);
    }

    @On(event = CqnService.EVENT_READ, entity = Articles_.CDS_NAME)
    public void readArticles(CdsReadEventContext context) {
        Result result = context.getService().run(Select.from(Articles_.class));
        context.setResult(result);
    }

    @On(event= CqnService.EVENT_UPDATE, entity = Articles_.CDS_NAME)
    public void updateArticles(CdsUpdateEventContext context, Articles article) {
    Result result = context.getService().run(Update.entity(Articles_.class).data(article).where(entry -> entry.ID().eq(article.getId())));
    context.setResult(result);
    }

    @On(event= CqnService.EVENT_DELETE, entity = Articles_.CDS_NAME)
    public void deleteArticles(CdsDeleteEventContext context, Articles article) {
        Result result = context.getService().run(Delete.from(Articles_.class).where(entry -> entry.ID().eq(article.getId())));
        context.setResult(result);
    }

    @On(event = CqnService.EVENT_READ, entity = Products_.CDS_NAME)
    public void readProducts(CdsReadEventContext context) {
        Result result = context.getService().run(Select.from(Products_.class));
        Result countResult = context.getService().run(
            Select.from(Articles_.class)
                .columns((Products_.ID), "count(*) as stock")
                .groupBy(Products_.ID)
        );

        Map<String, Integer> articleCountMap = new HashMap<>();
        countResult.listOf(Map.class).forEach(entry -> {
            String productId = (String) entry.get("product_ID");
            Integer stock = ((Long) entry.get("stock")).intValue();
            articleCountMap.put(productId, stock);
        });

        List<Products> products = result.listOf(Products.class);
            products.forEach(product -> {
            Integer stock = articleCountMap.getOrDefault(product.getId(), 1);
            product.setStock(stock);
            product.setTotalValue(product.getPrice().multiply(new BigDecimal(stock)));
        });

        bubbleSort(products);
        
        context.setResult(products);
    }

    public void bubbleSort(List<Products> products) {
        int n = products.size();
        boolean swapped;
    
        // Äußere Schleife: Durchläuft die Liste mehrfach
        do {
            swapped = false;
            for (int i = 0; i < n - 1; i++) {
                // Vergleicht benachbarte Elemente und tauscht sie, falls nötig
                if (products.get(i).getTotalValue().compareTo(products.get(i + 1).getTotalValue()) < 0) {
                    // Elemente tauschen
                    Products temp = products.get(i);
                    products.set(i, products.get(i + 1));
                    products.set(i + 1, temp);
                    swapped = true;
                }
            }
            n--; // Letztes Element ist bereits sortiert
        } while (swapped);
    }


}