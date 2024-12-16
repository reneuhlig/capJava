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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;


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
        Result result = context.getService().run(Select.from(Articles_.class));
        context.setResult(result);
    }

    @After(event = CqnService.EVENT_READ, entity = Products_.CDS_NAME)
    public void afterReadProducts(CdsReadEventContext context, List<Products> products) {
        for(Products product: products) {
            Result countResult = context.getService().run(Select.from(Articles_.class)
            .columns(c-> CQL.count(c.get("ID")).as("stock"))
            .groupBy(g -> g.get("productId"))
            .where(entity -> entity.product_ID().eq(product.getId())));

            // Berechne den totalValue
            if (product.getPrice() != null) {
                product.setTotalValue(product.getPrice().multiply(BigDecimal.valueOf(1)));
            }

        }
    }





    // @After(event = CqnService.EVENT_READ, entity = Products_.CDS_NAME)
    // public void afterReadProducts(List<Products> products) {
    //     products.forEach(product -> {
    //         String productId = product.getId();
    //         long count = getCountOfArticlesByProduct(context, productId);
    //         product.setStock(count);
    //         if (product.getPrice() != null) {
    //             product.setTotalValue(product.getPrice().multiply(BigDecimal.valueOf(count)));
    //         }
    //     });
    // }

    // @On(event = "sortProductsByTotalValue")
    // public List<Products> sortProductsByTotalValue(CdsReadEventContext context) {
    //     List<Products> products = context.getService().run(Select.from(Products_.class)).listOf(Products.class);
    //     products.forEach(product -> {
    //         String productId = product.getId();
    //         long count = getCountOfArticlesByProduct(context, productId);
    //         product.setStock(count);
    //         if (product.getPrice() != null) {
    //             product.setTotalValue(product.getPrice().multiply(BigDecimal.valueOf(count)));
    //         }
    //     });
    //     return products.stream().sorted((a, b) -> b.getTotalValue().compareTo(a.getTotalValue())).collect(Collectors.toList());
    // }

    // private void updateProductStockAndValue(CdsRuntime context, String productId) {
    //     long count = getCountOfArticlesByProduct(context, productId);
    //     BigDecimal totalValue = getPriceByProduct(context, productId).multiply(BigDecimal.valueOf(count));

    //     CqnUpdate update = Update.entity(Products_.class)
    //                              .data(Products.STOCK, count)
    //                              .data(Products.TOTAL_VALUE, totalValue)
    //                              .where(a -> a.ID().eq(productId));

    //     context.getService().run(update);
    // }

    // private long getCountOfArticlesByProduct(CdsRuntime context, String productId) {
    //     Result result = context.getService().run(
    //         Select.from(Articles_.class)
    //               .columns(Aggregations.count(a -> a.ID()).as("count"))
    //               .where(a -> a.PRODUCT_ID().eq(productId))
    //     );
    //     return result.first().map(r -> (Long) r.get("count")).orElse(0L);
    // }

    // private BigDecimal getPriceByProduct(CdsRuntime context, String productId) {
    //     Result result = context.getService().run(
    //         Select.from(Products_.class)
    //               .columns(a -> a.price())
    //               .where(a -> a.ID().eq(productId))
    //     );
    //     return result.first().map(r -> (BigDecimal) r.get("price")).orElse(BigDecimal.ZERO);
    // }

    // private String getProductByArticle(CdsRuntime context, String articleId) {
    //     Result result = context.getService().run(
    //         Select.from(Articles_.class)
    //               .columns(a -> a.product_ID())
    //               .where(a -> a.ID().eq(articleId))
    //     );
    //     return result.first().map(r -> (String) r.get("product_ID")).orElse(null);
    // }

}