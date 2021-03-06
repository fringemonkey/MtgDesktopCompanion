package org.magic.api.pricers.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicEdition;
import org.magic.api.beans.MagicPrice;
import org.magic.api.interfaces.abstracts.AbstractMagicPricesProvider;
import org.magic.services.MTGConstants;
import org.magic.tools.URLTools;

import com.google.gson.JsonElement;

public class ChannelFireballPricer extends AbstractMagicPricesProvider {

	@Override
	public STATUT getStatut() {
		return STATUT.BETA;
	}


	@Override
	public List<MagicPrice> getPrice(MagicEdition me, MagicCard card) throws IOException {

		String keyword = card.getName();
		String url = getString("URL");

		keyword = URLEncoder.encode(keyword, MTGConstants.DEFAULT_ENCODING);

		setProperty("KEYWORD", keyword);

		if (me != null)
			keyword += "&setname=" + URLEncoder.encode(me.getSet(), MTGConstants.DEFAULT_ENCODING);

		String link = url.replaceAll("%CARDNAME%", keyword);

		logger.info(getName() + " Looking for prices " + link);
		JsonElement root = URLTools.extractJson(link);

		String value = root.getAsJsonArray().get(0).getAsString();

		MagicPrice mp = new MagicPrice();
		mp.setUrl("http://store.channelfireball.com/products/search?query="+ URLEncoder.encode(card.getName(), MTGConstants.DEFAULT_ENCODING));
		mp.setSite(getName());
		mp.setCurrency("USD");
		try {
		mp.setValue(Double.parseDouble(value.substring(1).replaceAll(",", "")));
		}catch(Exception e)
		{
			mp.setValue(null);
		}
		ArrayList<MagicPrice> list = new ArrayList<>();
		list.add(mp);

		logger.info(getName() + " found " + list.size() + " item(s)");

		return list;
	}

	@Override
	public String getName() {
		return "Channel Fireball";
	}

	@Override
	public void alertDetected(List<MagicPrice> p) {
		// do nothing

	}

	@Override
	public void initDefault() {
		setProperty("MAX", "5");
		setProperty("URL", "http://magictcgprices.appspot.com/api/cfb/price.json?cardname=%CARDNAME%");
		setProperty("WEBSITE", "http://store.channelfireball.com/");
		setProperty("KEYWORD", "");

	}


}
