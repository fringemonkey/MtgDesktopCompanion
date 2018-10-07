package org.magic.api.interfaces.abstracts;

import java.io.File;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;

import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicCardAlert;
import org.magic.api.beans.MagicCardStock;
import org.magic.api.beans.MagicCollection;
import org.magic.api.beans.MagicNews;
import org.magic.api.beans.ShopItem;
import org.magic.api.interfaces.MTGDao;
import org.magic.services.MTGConstants;
import org.magic.services.MTGControler;

import com.google.gson.Gson;

public abstract class AbstractMagicDAO extends AbstractMTGPlugin implements MTGDao {

	protected Gson serialiser;
	
	
	@Override
	public PLUGINS getType() {
		return PLUGINS.DAO;
	}

	public AbstractMagicDAO() {
		super();
		confdir = new File(MTGConstants.CONF_DIR, "dao");
		if (!confdir.exists())
			confdir.mkdir();
		load();

		if (!new File(confdir, getName() + ".conf").exists()) {
			initDefault();
			save();
		}
		
		serialiser=new Gson();
	}
	
	@Override
	public void moveCard(MagicCard mc, MagicCollection from, MagicCollection to) throws SQLException {
		removeCard(mc, from);
		saveCard(mc, to);
		
		listStocks(mc, from,true).forEach(cs->{
		
			try {
				cs.setMagicCollection(to);
				saveOrUpdateStock(cs);
			} catch (SQLException e) {
				logger.error("Error saving stock for" + mc + " from " + from + " to " + to);
			}
		});
	}
	
	@Override
	public void deleteStock(MagicCardStock state) throws SQLException
	{
		ArrayList<MagicCardStock> stock = new ArrayList<>();
		stock.add(state);
		deleteStock(stock);
	}
	
	@Override
	public void duplicateTo(MTGDao dao) throws SQLException {
		
		
		logger.debug("duplicate collection");
		for (MagicCollection col : getCollections())
		{
			try {
				dao.saveCollection(col);
			}catch(SQLIntegrityConstraintViolationException e)
			{
				logger.error(col +" already exist");
			}
			
			for (MagicCard mc : listCardsFromCollection(col)) {
				dao.saveCard(mc, col);
			}
		}
		
		logger.debug("duplicate stock");
		for(MagicCardStock stock : listStocks())
			dao.saveOrUpdateStock(stock);
			
		logger.debug("duplicate alerts");
		for(MagicCardAlert alert : listAlerts())
			dao.saveAlert(alert);
		
		logger.debug("duplicate news");
		for(MagicNews news : listNews())
			dao.saveOrUpdateNews(news);
		

		
	}

	
}
