package de.bjoern.ahlfeld.shoplytics.api;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.estimote.sdk.Beacon;

import de.bjoern.ahlfeld.shoplytics.ShoplyticsApplication;
import de.bjoern.ahlfeld.shoplytics.models.Customer;


public class ApiService
{
	private static final String TAG = ApiService.class.getName();

	
	/**
	 * Tries to get the locations from the endpoints
	 */
	public static void notifyCustomer(final Beacon beacon)
	{
		final Customer customer = ShoplyticsApplication.getInstance().getCustomer();
		JSONObject obj = createMessage(beacon, customer);
		JsonObjectRequest req = new JsonObjectRequest(Endpoints.NOTIFY_ME, obj, new Response.Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject result) {
				
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				if(error != null && error.getLocalizedMessage() != null)
				{
					Log.e(TAG, error.getLocalizedMessage() );
				}
				else
				{
					Log.e(TAG, "Volley error ");
				}
			}
		});
		
		ShoplyticsApplication.getInstance().addToRequestQueue(req);
	}
	
	private static JSONObject createMessage(Beacon beacon, final Customer customer) {
		final JSONObject obj = new JSONObject();
		try {
			obj.put("customer_id", customer.getCustomerUUID().toString());
			obj.put("uuid", beacon.getProximityUUID());
			obj.put("major_value", beacon.getMajor());
			obj.put("minor_value", beacon.getMinor());
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
		}
		return obj;
	}

	public static void addCustomer(final Customer customer, final Context ctx)
	{
		JsonObjectRequest req = new JsonObjectRequest(Endpoints.ADD_CUSTOMER, customer.toJson(), new Response.Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject result) {
				Log.d(TAG, "RESULT");
				customer.writeToSharedPreferences(ctx);
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				if(error != null && error.getLocalizedMessage() != null)
				{
					Log.e(TAG, error.getLocalizedMessage() );
				}
				else
				{
					Log.e(TAG, "Volley error ");
				}
			}
		});
		
		ShoplyticsApplication.getInstance().addToRequestQueue(req);
	}
}
