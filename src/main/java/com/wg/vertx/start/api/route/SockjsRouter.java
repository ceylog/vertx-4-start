package com.wg.vertx.start.api.route;

import com.wg.vertx.start.utils.LogUtils;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

public class SockjsRouter {

  private static final Logger logger = LoggerFactory.getLogger(SockjsRouter.class);
  final Vertx vertx;

  public SockjsRouter(Vertx vertx){
    this.vertx = vertx;
  }

  public void setRouter(Router router){

    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    SockJSBridgeOptions options = new SockJSBridgeOptions();

    router.mountSubRouter("/eventbus", sockJSHandler.bridge(options,
      event -> {
        JsonObject rawMessage = event.getRawMessage();
        logger.info(LogUtils.SOCKJS_TYPE.buildMessage(event.type().name()));
        event.complete(true);
      }));
  }
}
