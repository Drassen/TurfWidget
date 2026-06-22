package com.turfgame.widget.net;

import androidx.annotation.NonNull;

import com.turfgame.widget.model.CharStats;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Thin client for the Turf "users" endpoint. Replaces the old Apache
 * {@code HttpClient} based implementation with {@link HttpURLConnection},
 * which is the supported HTTP stack on modern Android. Connection and read
 * timeouts are always set so a stalled request can never block its caller
 * indefinitely.
 */
public final class TurfApi {

    private static final String ENDPOINT = "https://api.turfgame.com/v4/users";
    private static final int CONNECT_TIMEOUT_MS = 15_000;
    private static final int READ_TIMEOUT_MS = 15_000;

    private TurfApi() {
    }

    /**
     * Fetches the stats for the given account email.
     *
     * @throws TurfApiException if the email is missing, the request fails, or
     *                          the response cannot be parsed.
     */
    @NonNull
    public static CharStats fetchUser(String email) throws TurfApiException {
        if (email == null || email.trim().isEmpty()) {
            throw new TurfApiException(TurfApiException.Reason.NO_ACCOUNT, "No account configured");
        }

        HttpURLConnection connection = null;
        try {
            JSONArray requestBody = new JSONArray().put(new JSONObject().put("email", email));

            connection = (HttpURLConnection) new URL(ENDPOINT).openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            try (OutputStream out = connection.getOutputStream()) {
                out.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
            }

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new TurfApiException(TurfApiException.Reason.SERVER,
                        "Unexpected HTTP status " + status);
            }

            return parse(readBody(connection.getInputStream()));
        } catch (TurfApiException e) {
            throw e;
        } catch (IOException e) {
            throw new TurfApiException(TurfApiException.Reason.NETWORK, "Network error", e);
        } catch (JSONException e) {
            throw new TurfApiException(TurfApiException.Reason.PARSE, "Malformed response", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static CharStats parse(String json) throws JSONException, TurfApiException {
        JSONArray users = new JSONArray(json);
        if (users.length() == 0) {
            throw new TurfApiException(TurfApiException.Reason.PARSE, "No user in response");
        }

        JSONObject user = users.getJSONObject(0);
        int points = user.optInt("points", 0);
        int pointsPerHour = user.optInt("pointsPerHour", 0);
        int place = user.optInt("place", 0);
        JSONArray zones = user.optJSONArray("zones");
        int zoneCount = zones != null ? zones.length() : 0;

        return new CharStats(points, pointsPerHour, place, zoneCount);
    }

    private static String readBody(InputStream in) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        }
    }
}
