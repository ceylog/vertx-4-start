package com.wg.vertx.start.api.route;


import com.wg.vertx.start.utils.LogUtils;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;

public class SockjsRouter {

  private static final Logger logger = LoggerFactory.getLogger(SockjsRouter.class);
  final Vertx vertx;

  public SockjsRouter(Vertx vertx) {
    this.vertx = vertx;
  }

  public void setRouter(Router router) {
    SockJSHandlerOptions sockJSHandlerOptions = new SockJSHandlerOptions().setHeartbeatInterval(10000);
    SockJSHandler sockJSHandler = SockJSHandler.create(vertx,sockJSHandlerOptions);
    SockJSBridgeOptions options = new SockJSBridgeOptions()
      .addInboundPermitted(new PermittedOptions().setAddress("to.server"))
      .addOutboundPermitted(new PermittedOptions().setAddress("to.client"));

    router.mountSubRouter("/eventbus", sockJSHandler.bridge(options,
      event -> {
        JsonObject rawMessage = event.getRawMessage();
        System.out.println("rawMessage = " + rawMessage);
        logger.info(LogUtils.SOCKJS_TYPE.buildMessage(event.type().name()));

        boolean result = true;
        switch (event.type()) {
          case SOCKET_CREATED:
            break;
          case SOCKET_IDLE:
            result = false;
            return;
          case SOCKET_CLOSED:
            break;
        }
        event.complete(result);
      }
    ));

    vertx.eventBus().consumer("to.server").handler(message -> {
      System.out.println("address:"+message.address()+",replyAddress:"+message.replyAddress());
      System.out.println("message = " + message.body());
      vertx.eventBus().send("to.client",message.body());
    });
  }
}
