package com.wg.vertx.start.api.route;

import com.wg.vertx.start.api.model.User;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

public class HelloRouter {

  private final Vertx vertx;

  public HelloRouter(Vertx vertx) {
    this.vertx = vertx;
  }

  public void setRouter(Router router) {
    router.mountSubRouter("/hello",buildBookRouter());
  }

  private Router buildBookRouter() {
    final Router router = Router.router(vertx);

    Route route = router.route("/some/path/");
    route.handler(ctx -> {

      HttpServerResponse response = ctx.response();
      // enable chunked responses because we will be adding data as
      // we execute over other handlers. This is only required once and
      // only if several handlers do output.
      response.setChunked(true);

      response.write("route1\n");

      // Call the next matching route after a 5 second delay
      ctx.vertx().setTimer(5000, tid -> ctx.next());
    });

    route.handler(ctx -> {

      HttpServerResponse response = ctx.response();
      response.write("route2\n");

      // Call the next matching route after a 5 second delay
      ctx.vertx().setTimer(5000, tid -> ctx.next());
    });

    route.handler(ctx -> {

      HttpServerResponse response = ctx.response();
      response.write("route3");

      // Now end the response
      ctx.response().end();
    });

    router.get("/t1").respond(ctx -> Future.succeededFuture("h111"));
    router.get("/user").respond(ctx -> Future.succeededFuture(new User("ff", "fff@abc.com")));

    router.get("/some/path1")
      .respond(
        ctx -> ctx
          .response()
          .putHeader("Content-Type", "text/plain")
          .end("hello world!"));

    router.get("/some/path2")
      // in this case, the handler ensures that the connection is ended
      .respond(
        ctx -> ctx
          .response()
          .setChunked(true)
          .write("Write some text..."));

    router.route(HttpMethod.POST, "/catalogue/products/:productType/:productID/")
      .handler(ctx -> {
        String productType = ctx.pathParam("productType");
        String productID = ctx.pathParam("productID");
        System.out.println("productType:" + productType + ",productID:" + productID);
        // Do something with them...
        ctx.response().putHeader("content-type", "text/plain").end("ok");
      });



    return router;
  }
}
