package com.example.chatbot;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class ChatbotAPI {
    private static final String API_KEY = "API_KEY"; // Replace na lang
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    public interface ResponseCallback {
        void onResponse(String response);
    }

    public static void sendMessage(String message, ResponseCallback callback) {
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            String systemInstruction = "Act as an online professional chat support  that guides users into how Commodono works. \n" +
                    "\n" +
                    "Here is the breakdown of what is the commodono app." +
                    " It is an app that digitalizes the experience of donating" +
                    " by getting the information of the donations and logging donations." +
                    " By digitalizing the experience of donating we can contribute to a" +
                    " more wholesome and inclusive community. As a reward for donating," +
                    " users can get points from their donation in which they can exchange" +
                    " for something valuable. The users gets point when the post/area to" +
                    " be donated verifies their donation. Just answer with minimal words but meaningful" +
                    "make it short and don't mention unnecessary information";

            JSONArray userPartsArray = new JSONArray();
            JSONObject textPart = new JSONObject();
            textPart.put("text", systemInstruction + "Translate: " + message);
            userPartsArray.put(textPart);

            JSONObject userContentsObject = new JSONObject();
            userContentsObject.put("role", "user");
            userContentsObject.put("parts", userPartsArray);

            JSONArray contentsArray = new JSONArray();
            contentsArray.put(userContentsObject);

            json.put("contents", contentsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onResponse("Error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();

                System.out.println("Raw API Response: " + responseBody);

                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    if (jsonResponse.has("error")) {
                        callback.onResponse("Error: " + jsonResponse.getJSONObject("error").getString("message"));
                        return;
                    }

                    JSONArray candidates = jsonResponse.optJSONArray("candidates");
                    if (candidates != null && candidates.length() > 0) {
                        JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
                        if (content != null) {
                            JSONArray parts = content.optJSONArray("parts");
                            if (parts != null && parts.length() > 0) {
                                String botResponse = parts.getJSONObject(0).optString("text", "No response.");
                                callback.onResponse(botResponse.trim());
                                return;
                            }
                        }
                    }

                    callback.onResponse("Error: No valid response from AI.");
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onResponse("Error processing response.");
                }
            }
        });
    }
}
