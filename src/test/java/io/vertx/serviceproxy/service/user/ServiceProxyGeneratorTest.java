package io.vertx.serviceproxy.service.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.serviceproxy.ServiceBinder;

@RunWith(VertxUnitRunner.class)
public class ServiceProxyGeneratorTest 
{
	private static Vertx vertx;
	
	private static AbstractVerticle verticle;
	
	private static final Collection<MessageConsumer<JsonObject>> serviceConsumers = new ArrayList<>(); 
	
	private static ServiceBinder serviceBinder;
	
	@BeforeClass
	public static void setUp(final TestContext testContext) 
	{
        VertxOptions options = new VertxOptions();
 		if (java.lang.management.ManagementFactory.getRuntimeMXBean().
 			    getInputArguments().toString().indexOf("-agentlib:jdwp") > 0)
 		{
 			options.setBlockedThreadCheckInterval(1_000_000L);
 		}
        vertx = Vertx.vertx(options);
        
        verticle = new AbstractVerticle()
        {
        	@Override
        	public void start(final Future<Void> startFuture) throws Exception 
        	{
        		final UserDao currUserDao = UserDao.create(this.vertx, new JsonObject());
        		serviceBinder = new ServiceBinder(vertx);
        		
        		serviceConsumers.add(serviceBinder
        			.setAddress(UserDao.SERVICE_ADDRESS)
        			.register(UserDao.class, currUserDao)
        		);
        		
        		startFuture.complete();
        	}
        };
        
        Thread.currentThread().setName("JUNIT");
        final Async async = testContext.async();
        
        vertx.deployVerticle(verticle, new Handler<AsyncResult<String>>()
		{
			@Override
			public void handle(AsyncResult<String> deployVerticleEvent) 
			{
				if (deployVerticleEvent.succeeded())
				{
					async.complete();
				}
				else
				{
					testContext.fail(deployVerticleEvent.cause());
					async.complete();
				}
			}
		});
	}
	
	
	@AfterClass
	public static void tearDown(final TestContext testContext) throws Exception
	{
		if (serviceBinder != null)
		{
			for(MessageConsumer<JsonObject> serviceConsumer : serviceConsumers)
			{
				serviceBinder.unregister(serviceConsumer);
			}
		}
			
		if (verticle != null)
		{
			verticle.stop();
		}
	}
	
	
	
	
	@Test
	public void testGenerator(TestContext testContext) throws Exception
	{
		final Async async = testContext.async();
		final UserDao daoProxy = UserDao.createProxy(vertx, UserDao.SERVICE_ADDRESS);
		daoProxy.findAll(new Handler<AsyncResult<List<JsonObject>>>() 
		{
			@Override
			public void handle(AsyncResult<List<JsonObject>> event) 
			{
				if (event.succeeded())
				{
					testContext.verify(new Handler<Void>() {
						@Override
						public void handle(Void verifyEvent) 
						{
							assertThat(event.result()).hasSize(2);
							async.complete();
						}
					});
				}
				else
				{
					testContext.fail(event.cause());
					async.complete();
				}
				
			}
		});
	}
}
