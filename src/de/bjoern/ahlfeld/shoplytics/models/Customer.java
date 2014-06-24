package de.bjoern.ahlfeld.shoplytics.models;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import de.bjoern.ahlfeld.shoplytics.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Customer implements Parcelable{


	public UUID getCustomerUUID() {
		return customerUUID;
	}

	public void setCustomerUUID(UUID customerUUID) {
		this.customerUUID = customerUUID;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	private static final String TAG = Customer.class.getName();
   
	
	private UUID customerUUID;
	
	private String token;
	
	public Customer(final String token)
	{
		this.customerUUID = UUID.randomUUID();
		this.token = token;
	}
	
	private Customer(Parcel in)
	{
		this.customerUUID = UUID.fromString(in.readString());
		this.token = in.readString();
	}

	public Customer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int describeContents() {
		
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(customerUUID.toString());
		dest.writeString(this.token);
	}
	
	public static final Parcelable.Creator<Customer> CREATOR =
            new Parcelable.Creator<Customer>(){

             @Override
             public Customer createFromParcel(Parcel source) {
                 return new Customer(source);
             }

             @Override
             public Customer[] newArray(int size) {
                 return new Customer[size];
             }
   };

	
	public JSONObject toJson()
	{
		final JSONObject obj = new JSONObject();
		try
		{
			obj.put("customner_id", this.customerUUID.toString());
			obj.put("token", this.token);
		}catch (JSONException e)
		{
			Log.e(TAG, e.getMessage());
		}
		
		return obj;
	}

	public void writeToSharedPreferences(Context ctx) {
		SharedPreferences.Editor edit = ctx.getApplicationContext().getSharedPreferences(ctx.getString(R.string.preferences),  Context.MODE_MULTI_PROCESS).edit();
		edit.putString("customer_id", this.customerUUID.toString());
		edit.putString("token", this.token);
		edit.commit();
	}
	
	public static Customer readFromSharedPreferences(Context ctx)
	{
		SharedPreferences prefs = ctx.getApplicationContext().getSharedPreferences(ctx.getString(R.string.preferences), Context.MODE_MULTI_PROCESS);
		Customer customer = new Customer();
		customer.customerUUID = UUID.fromString(prefs.getString("customer_id", null));
		customer.token = prefs.getString("token", null);
		if(customer.customerUUID == null || customer.token == null)
		{
			Log.e(TAG, "Error in customer opbject");
			throw new IllegalStateException("Error in customer opbject");
		}
		return customer;
		
		
	}
}
