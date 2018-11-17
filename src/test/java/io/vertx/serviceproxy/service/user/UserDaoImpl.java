package io.vertx.serviceproxy.service.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;

public class UserDaoImpl implements UserDao, Serializable
{
	private static final long serialVersionUID = 1L;
	
	private final JsonObject config;

	/**
	 * Implementation du service W2UserDao.
	 * 
	 * @param vertx référence vers le verticle DAO.
	 * @param config configuration de l'accès à la base de données MongoDB.
	 */
	public UserDaoImpl(final Vertx vertx, final JsonObject config)
	{
		this.config = config;
	}
	
	@Override
	public void findAll(final Handler<AsyncResult<List<JsonObject>>> result) 
	{
		try
		{
			final List<JsonObject> results = new ArrayList<>();
			
			final User julienPongeUser = new User();
			julienPongeUser.setFamilyName("PONGE");
			julienPongeUser.setGivenName("Julien");
			julienPongeUser.setEmail("julien@ponge.org");
			
			final User julienVietUser = new User();
			julienVietUser.setFamilyName("VIET");
			julienVietUser.setGivenName("Julien");
			julienVietUser.setEmail("julien@julienviet.com");
			
			results.add(serializeToJson(julienPongeUser));
			results.add(serializeToJson(julienVietUser));
			
			result.handle(Future.succeededFuture(results));
		}
		catch (VertxException ve)
		{
			result.handle(ServiceException.fail(400, ve.getMessage()));
		}
	}
	
	@Override
	public void close() 
	{
		// do nothing
	}
	
	
	
	private JsonObject serializeToJson(User user) 
	{
		final JsonObject json = new JsonObject();
		
		json.put("familyName", user.getFamilyName());
		json.put("givenName", user.getGivenName());
		json.put("email", user.getEmail());
		
		if (user.getId() != null && !"".equals(user.getId()))
		{
			json.put("_id", user.getId());
		}
		
		return json;
	}
}
