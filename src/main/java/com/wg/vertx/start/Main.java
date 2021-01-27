package com.wg.vertx.start;

import com.wg.vertx.start.verticle.MainVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;
import org.infinispan.manager.DefaultCacheManager;

public class Main {

  public static void main(String[] args) {
    ClusterManager cmgr = null;
    try {
      cmgr = new InfinispanClusterManager(new DefaultCacheManager("infinispan-default.xml"));
    } catch (Exception e) {
      e.printStackTrace();
    }

    VertxOptions options = new VertxOptions().setClusterManager(cmgr);

    Vertx.clusteredVertx(options, res -> {
      if (res.succeeded()) {
        Vertx vertx = res.result();
        vertx.deployVerticle(MainVerticle.class.getName());
      } else {
        // failed!
        System.err.println("cluster init failed!");
      }
    });
  }
}
