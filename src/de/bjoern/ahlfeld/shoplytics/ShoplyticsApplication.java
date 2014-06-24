package de.bjoern.ahlfeld.shoplytics;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;

import de.bjoern.ahlfeld.shoplytics.models.Customer;

public final class ShoplyticsApplication extends Application {

	private static final String TAG = "ShoplyticsApplication";

	private RequestQueue mRequestQueue;

	private static ShoplyticsApplication sInstance;

	private Customer mCustomer;
	
	@Override
	public void onCreate() {
		super.onCreate();

		sInstance = this;
		
		SharedPreferences prefs = getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);
		if(prefs != null && !prefs.getBoolean(getString(R.string.first_launch), true))
		{
			mCustomer = Customer.readFromSharedPreferences(this);
		}
	}

	/**
	 * @return ApplicationController singleton instance
	 */
	public static synchronized ShoplyticsApplication getInstance() {
		return sInstance;
	}

	/**
	 * @return The Volley Request queue, the queue will be created if it is null
	 */
	public RequestQueue getRequestQueue() {
		// lazy initialize the request queue, the queue instance will be
		// created when it is accessed for the first time
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}

		return mRequestQueue;
	}
	
	public Customer getCustomer()
	{
		if(mCustomer == null) 
		{
			SharedPreferences prefs = getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);
			if(prefs != null && !prefs.getBoolean(getString(R.string.first_launch), true))
			{
				mCustomer = Customer.readFromSharedPreferences(this);
			}
		}
		return mCustomer;
	}

	/**
	 * Adds the specified request to the global queue, if tag is specified then
	 * it is used else Default TAG is used.
	 * 
	 * @param req
	 * @param tag
	 */
	public <T> void addToRequestQueue(Request<T> req, String tag) {
		// set the default tag if tag is empty
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

		VolleyLog.d("Adding request to queue: %s", req.getUrl());

		getRequestQueue().add(req);
	}

	/**
	 * Adds the specified request to the global queue using the Default TAG.
	 * 
	 * @param req
	 * @param tag
	 */
	public <T> void addToRequestQueue(Request<T> req) {
		// set the default tag if tag is empty
		req.setTag(TAG);

		getRequestQueue().add(req);
	}

}
