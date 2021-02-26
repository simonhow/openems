package io.openems.edge.ess.fronius;

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
import io.openems.edge.common.sum.GridMode;

public class SolarwebAPIClientESS {
	
	private static final String BASE_URL = "https://api.solarweb.com/swqapi/pvsystems/";
	
	private String accessKeyID, accessKeyValue, pvSystemID, lastResponseFlowdata;
	
	public SolarwebAPIClientESS(String accessKeyID, String accessKeyValue, String pvSystemID) {
		this.accessKeyID = accessKeyID;
		this.accessKeyValue = accessKeyValue;
		this.pvSystemID = pvSystemID;
	}
	
	public int getCapacity() throws OpenemsNamedException {
		String endpoint = this.pvSystemID + "/devices/";
		String response = this.sendGetRequest(endpoint);
		
		/*
		 *  Parse Json object
		 */
		Gson gson = new GsonBuilder().create();
		JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
		
		/*
		 * Find device of type "Battery" in Json-Array, 
		 * increase index while it is smaller than the size of the array
		 */
		String deviceType;
		int channelIndex = 0;
		do {
			deviceType = jsonResponse.getAsJsonArray("devices")
					.get(channelIndex)
					.getAsJsonObject()
					.getAsJsonPrimitive("deviceType")
					.getAsString();
			channelIndex ++;
		}while (!deviceType.equals("Battery") && channelIndex < jsonResponse.getAsJsonArray("devices").size());
		/*
		 * Read channel with name "BattSOC" in Json-Array
		 */
		if(deviceType.equals("Battery")) {
			JsonPrimitive capacityPrimitive = jsonResponse.getAsJsonArray("devices")
					.get(channelIndex - 1)
					.getAsJsonObject()
					.getAsJsonPrimitive("capacity");
			int capacity = capacityPrimitive.getAsInt();
			return capacity;
		}
		/*
		 * channelName not found -> Error
		 */
		else {
			throw new OpenemsException("Could not find device of type \"Battery\" in JSON object!");
		}
	}
	
	public void updateFlowdata() throws OpenemsNamedException {
		String endpoint = this.pvSystemID + "/flowdata";
		String response = this.sendGetRequest(endpoint);
		this.lastResponseFlowdata = response;
	}
	
	public int getSoC() throws OpenemsException {
		/*
		 *  Parse Json object
		 */
		Gson gson = new GsonBuilder().create();
		JsonObject jsonResponse = gson.fromJson(this.lastResponseFlowdata, JsonObject.class);
		
		/*
		 * Find channel with name "BattSOC" in Json-Array, 
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
		}while (!channelName.equals("BattSOC") && channelIndex < jsonResponse.getAsJsonObject("data").getAsJsonArray("channels").size());
		/*
		 * Read channel with name "BattSOC" in Json-Array
		 */
		if(channelName.equals("BattSOC")) {
			JsonPrimitive socPrimitive = jsonResponse.getAsJsonObject("data")
					.getAsJsonArray("channels")
					.get(channelIndex - 1)
					.getAsJsonObject()
					.getAsJsonPrimitive("value");
			String soc = socPrimitive.getAsString();
			
			return (int) Float.parseFloat(soc);
		}
		/*
		 * channelName not found -> Error
		 */
		else {
			throw new OpenemsException("Could not find channel \"BattSOC\" in JSON object!");
		}
	}
	
	public int getActivePower() throws OpenemsException { // negative: charge, positive: discharge
		/*
		 *  Parse Json object
		 */
		Gson gson = new GsonBuilder().create();
		JsonObject jsonResponse = gson.fromJson(this.lastResponseFlowdata, JsonObject.class);
		
		/*
		 * Find channel with name "PowerBattCharge" in Json-Array, 
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
		}while (!channelName.equals("PowerBattCharge") && channelIndex < jsonResponse.getAsJsonObject("data").getAsJsonArray("channels").size());
		/*
		 * Read channel with name "PowerBattCharge" in Json-Array
		 */
		if(channelName.equals("PowerBattCharge")) {
			JsonPrimitive powerPrimitive = jsonResponse.getAsJsonObject("data")
					.getAsJsonArray("channels")
					.get(channelIndex - 1)
					.getAsJsonObject()
					.getAsJsonPrimitive("value");
			int chargingValue = powerPrimitive.getAsInt();
			return chargingValue;
		}
		/*
		 * channelName not found -> Error
		 */
		else {
			throw new OpenemsException("Could not find channel \"PowerBattCharge\" in JSON object!");
		}
	}
	
	public GridMode getGridMode(){
		/*
		 *  Parse Json object
		 */
		Gson gson = new GsonBuilder().create();
		JsonObject jsonResponse = gson.fromJson(this.lastResponseFlowdata, JsonObject.class);
		
		/*
		 *  read battery mode
		 */
		JsonPrimitive battModePrimitive = jsonResponse.getAsJsonObject("status")
				.getAsJsonPrimitive("battMode");
		int battModeSolarweb = battModePrimitive.getAsInt();
		
		/*
		 * Translate to enum GridMode
		 */
		switch (battModeSolarweb) {
		case 0: 
			return GridMode.OFF_GRID;
		case 1:
			return GridMode.ON_GRID;
		default:
			return GridMode.UNDEFINED;
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
