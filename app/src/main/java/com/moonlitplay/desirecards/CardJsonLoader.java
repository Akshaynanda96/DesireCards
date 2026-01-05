package com.moonlitplay.desirecards;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardJsonLoader {

	public static List<String> femaleCards = new ArrayList<>();
	public static List<String> maleCards = new ArrayList<>();
	public static List<Integer> versionList = new ArrayList<>();
	public static JSONObject allVersionsJson;

	private static final String JSON_URL =
			"https://raw.githubusercontent.com/Akshaynanda96/DesireCards/main/cards.json";

	public static void loadCards(Context context, Runnable onComplete) {

		femaleCards.clear();
		maleCards.clear();
		versionList.clear();

		RequestQueue queue = Volley.newRequestQueue(context);
		queue.getCache().clear();  // 🚀 Clear old cache

		JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, JSON_URL, null,
				response -> {
					try {
						allVersionsJson = response.getJSONObject("versions");

						JSONArray keys = allVersionsJson.names();
						for (int i = 0; i < keys.length(); i++) {
							versionList.add(Integer.parseInt(keys.getString(i)));
						}

						Log.d("CardJsonLoader", "Downloaded Latest JSON ✔");
						onComplete.run();

					} catch (Exception e) {
						Log.e("CardJsonLoader", "JSON Parse Error: " + e.getMessage());
					}
				},
				error -> Log.e("CardJsonLoader", "Volley Error: " + error.getMessage())
		) {
			@Override
			public Map<String, String> getHeaders() {
				Map<String, String> headers = new HashMap<>();
				headers.put("Cache-Control", "no-cache, no-store, must-revalidate");
				headers.put("Pragma", "no-cache");
				headers.put("Expires", "0");
				return headers;
			}
		};

		request.setShouldCache(false);  // 🚫 No caching
		queue.add(request);
	}

	public static void loadVersion(int version) {
		femaleCards.clear();
		maleCards.clear();

		try {
			JSONObject selectedObj = allVersionsJson.getJSONObject(String.valueOf(version));

			JSONArray femaleArr = selectedObj.getJSONArray("female_cards");
			JSONArray maleArr = selectedObj.getJSONArray("male_cards");

			for (int i = 0; i < femaleArr.length(); i++) {
				femaleCards.add(femaleArr.getString(i));
			}

			for (int i = 0; i < maleArr.length(); i++) {
				maleCards.add(maleArr.getString(i));
			}

			Log.d("CardJsonLoader", "Loaded Version: " + version);

		} catch (Exception e) {
			Log.e("CardJsonLoader", "Load Version Error: " + e.getMessage());
		}
	}
}
