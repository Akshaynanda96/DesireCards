package com.moonlitplay.desirecards;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CardTypeService {

	private String lastPickedCard = null;
	private int repeatCount = 0;
	private final int MAX_REPEAT = 2;

	private final Random rand = new Random();

	public CardData getCardType(Integer cardType) {

		CardData cardData = new CardData();
		Map<Integer, String> cardsMap = new HashMap<>();
		String selectedCard;

		if (cardType == 1) {
			selectedCard = pickCardWithLimit(CardJsonLoader.femaleCards);
		} else if (cardType == 2) {
			selectedCard = pickCardWithLimit(CardJsonLoader.maleCards);
		} else {
			selectedCard = null;
		}

		if (selectedCard != null) {
			cardsMap.put(1, selectedCard);
		}

		cardData.setCardsInfo(cardsMap);
		return cardData;
	}

	private String pickCardWithLimit(java.util.List<String> cards) {

		if (cards == null || cards.isEmpty()) return "No cards loaded.";

		String cardValue;
		int attempts = 0;

		do {
			int i = rand.nextInt(cards.size());
			cardValue = cards.get(i);

			attempts++;

			if (attempts > 10) break;

		} while (cardValue.equals(lastPickedCard) && repeatCount >= MAX_REPEAT);

		if (cardValue.equals(lastPickedCard)) {
			repeatCount++;
		} else {
			lastPickedCard = cardValue;
			repeatCount = 1;
		}

		return cardValue;
	}

	public void startNewGame() {
		lastPickedCard = null;
		repeatCount = 0;
	}
}
