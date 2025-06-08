package com.legacyminecraft.poseidon.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A wrapper class for the Minecraft session API
 * 
 * TODO maybe make the HTTP requests asynchronous? idk if it really matters
 * 
 * @author moderator_man
 */
public class SessionAPI
{
    public static final String SESSION_BASE = "http://session.minecraft.net/game/";

    public static boolean hasJoined(String username, String serverId)
    {
        HTTPResponse response = httpGetRequest(SESSION_BASE + String.format("checkserver.jsp?user=%s&serverId=%s", username, serverId));
        if (response.getResponse() != "YES")
            return false;
        return true;
    }

    public static void hasJoined(String username, String serverId, String ip, SessionRequestRunnable callback)
    {
        try
        {
            boolean checkIP = ip == "127.0.0.1" || ip == "localhost";
            StringBuilder sb = new StringBuilder();
            sb.append("https://sessionserver.mojang.com/session/minecraft/hasJoined");
            sb.append("?username=" + username);
            sb.append("&serverId=" + serverId);
            if (checkIP)
                sb.append("&ip=" + ip);
            String requestUrl = sb.toString();
            
            HTTPResponse response = httpGetRequest(requestUrl);
            JSONObject obj = (JSONObject) new JSONParser().parse(response.getResponse());
            String res_username = (obj.containsKey("name") ? (String) obj.get("name") : "nousername");
            String res_uuid = (obj.containsKey("id") ? (String) obj.get("id") : "nouuid");
            String res_ip = (obj.containsKey("ip") ? (String) obj.get("ip") : "noip");
            callback.callback(response.getResponseCode(), res_username, res_uuid, res_ip);
        } catch (Exception ex) {
            System.out.println(String.format("Failed to authenticate session for '%s': %s", username, ex.getMessage()));
            // TODO: if debug, print the stack trace
        }
    }

    private static HTTPResponse httpGetRequest(String url)
    {
        try
        {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Project-Poseidon/0");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);
            in.close();
            return new HTTPResponse(response.toString(), con.getResponseCode());
        } catch (Exception ex) {
            ex.printStackTrace();
            return new HTTPResponse("", -1);
        }
    }
}
