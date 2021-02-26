package io.openems.edge.meter.fronius;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;

public class SolarwebAPIClientMeter {

	private static final String BASE_URL = "https://api.solarweb.com/swqapi/pvsystems/";
	
	private String accessKeyID, accessKeyValue, pvSystemID, lastResponseFlowdata;
	
	public SolarwebAPIClientMeter(String accessKeyID, String accessKeyValue, String pvSystemID) {
		this.accessKeyID = accessKeyID;
		this.accessKeyValue = accessKeyValue;
		this.pvSystemID = pvSystemID;
	}
	
	public void updateFlowdata() throws OpenemsNamedException {
		String endpoint = this.pvSystemID + "/flowdata";
		String response = this.sendGetRequest(endpoint);
		this.lastResponseFlowdata = response;
	}
	
	public int getPowerFeedIn() throws OpenemsException {
		/*
		 *  Parse Json object
		 */
		Gson gson = new GsonBuilder().create();
		JsonObject jsonResponse = gson.fromJson(this.lastResponseFlowdata, JsonObject.class);
		
		/*
		 * Find channel with name "PowerFeedIn" in Json-Array, 
		 * increase index while it is smaller than the size of the array
		 */
		String channelName;
		int channelIndex = 0;
		do {
			channelName = jsonResponse.getAsJsonObject("data")
					.getAsJsonArray("channels")
					.get(channelIndex)
					.getAsJsonObject()
					.getAsJsonPrimitive("channelName")
					.getAsString();
			channelIndex ++;
		}while (!channelName.equals("PowerFeedIn") && channelIndex < jsonResponse.getAsJsonObject("data").getAsJsonArray("channels").size());
		/*
		 * Read channel with name "PowerFeedIn" in Json-Array
		 */
		if(channelName.equals("PowerFeedIn")) {
			JsonPrimitive powerPrimitive = jsonResponse.getAsJsonObject("data")
					.getAsJsonArray("channels")
					.get(channelIndex - 1)
					.getAsJsonObject()
					.getAsJsonPrimitive("value");
			String power = powerPrimitive.getAsString();
			
			return (int) Float.parseFloat(power);
		}
		/*
		 * channelName not found -> Error
		 */
		else {
			throw new OpenemsException("Could not find channel \"PowerFeedIn\" in JSON object!");
		}
	}
	
	private String sendGetRequest(String endpoint) throws OpenemsNamedException {
		try {
			URL url = new URL(BASE_URL + endpoint);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestProperty("AccessKeyId", this.accessKeyID);
			con.setRequestProperty("AccessKeyValue", this.accessKeyValue);
			con.setRequestMethod("GET");
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			int status = con.getResponseCode();
			String body;
			try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
				// Read HTTP response
				StringBuilder content = new StringBuilder();
				String line;
				while ((line = in.readLine()) != null) {
					content.append(line);
					content.append(System.lineSeparator());
				}
				body = content.toString();
			}
			if (status < 300) {
				return body;
			} else {
				throw new OpenemsException(
						"Error while reading from Solarweb API. Response code: " + status + ". " + body);
			}
		} catch (OpenemsNamedException | IOException e) {
			throw new OpenemsException(
					"Unable to read from Solarweb API. " + e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
}
