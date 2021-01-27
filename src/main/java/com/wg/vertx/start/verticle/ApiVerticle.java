package com.wg.vertx.start.verticle;

import com.wg.vertx.start.api.handler.BookHandler;
import com.wg.vertx.start.api.handler.BookValidationHandler;
import com.wg.vertx.start.api.handler.ErrorHandler;
import com.wg.vertx.start.api.repository.BookRepository;
import com.wg.vertx.start.api.route.BookRouter;
import com.wg.vertx.start.api.route.HealthCheckRouter;
import com.wg.vertx.start.api.route.HelloRouter;
import com.wg.vertx.start.api.route.SockjsRouter;
import com.wg.vertx.start.api.service.BookService;
import com.wg.vertx.start.utils.DbUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.pgclient.PgPool;

public class ApiVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    final PgPool dbClient = DbUtils.buildDbClient(vertx);
    final Router router = Router.router(vertx);
    final HelloRouter helloRouter = new HelloRouter(vertx);
    final SockjsRouter sockjsRouter = new SockjsRouter(vertx);
    final BookRepository bookRepository = new BookRepository();
    final BookService bookService = new BookService(dbClient, bookRepository);
    final BookHandler bookHandler = new BookHandler(bookService);
    final BookValidationHandler bookValidationHandler = new BookValidationHandler(vertx);
    final BookRouter bookRouter = new BookRouter(vertx, bookHandler, bookValidationHandler);

    HealthCheckRouter.setRouter(vertx, router, dbClient);
    ErrorHandler.buildHandler(router);
    helloRouter.setRouter(router);
    sockjsRouter.setRouter(router);
    bookRouter.setRouter(router);

    router.post().handler(BodyHandler.create());
    // works
//    SessionStore store = LocalSessionStore.create(vertx);
    // breaks with Beta3
    SessionStore store = ClusteredSessionStore.create(vertx);
    SessionHandler sessionHandler = SessionHandler.create(store);
    // default 30 min
    sessionHandler.setSessionTimeout(30 * 60 * 1000);
    sessionHandler.setNagHttps(false);
    router.route().handler(sessionHandler);
    router.route("/static/*").handler(StaticHandler.create("webroot"));



    buildHttpServer(vertx,startPromise,router);
  }

  private void buildHttpServer(Vertx vertx, Promise<Void> startPromise,Router router){
    vertx.createHttpServer().requestHandler(router).listen(8888, http -> {
      if (http.succeeded()) {
        System.out.println("HTTP server started on port 8888");
        startPromise.complete();
      } else {
        System.out.println("http server start failed!");
        startPromise.fail(http.cause());
      }
    });
  }
}
