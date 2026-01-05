package com.moonlitplay.desirecards;

import java.util.HashMap;
import java.util.Map;

public class CardData
{
	private Map<Integer, String> cardsInfo = new HashMap<>();

	public Map<Integer, String> getCardsInfo()
	{
		return cardsInfo;
	}

	public void setCardsInfo(Map<Integer, String> cardsInfo)
	{
		this.cardsInfo = cardsInfo;
	}
}

