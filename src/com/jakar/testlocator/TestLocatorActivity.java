package com.jakar.testlocator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TestLocatorActivity extends Activity {
    /** Called when the activity is first created. */
	
	
	Context context;
	final int CHECK_SETTINGS = 3;
	EditText locate_num;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        context = this;
        
        
        
        Button getMyLocation = (Button)findViewById(R.id.getmylocation);
        Button findSomeone = (Button)findViewById(R.id.findsomeone);
        locate_num = (EditText)findViewById(R.id.locate_num);
        
        getMyLocation.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Toast.makeText(context, "Getting your location. This may take some time.", Toast.LENGTH_LONG).show();
				getMylocation();
			}
		});
        
        findSomeone.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if (!locate_num.getText().toString().equals("")&&locate_num.getText().toString()!=null){
					Toast.makeText(context, "Locationg "+ locate_num.getText().toString()+". This may take a few minutes.", Toast.LENGTH_LONG).show();
					getTheirLocation(locate_num.getText().toString());
				} else {
					Toast.makeText(context, "Please enter a phone number to locate!", Toast.LENGTH_LONG).show();
				}
			}
		});
        Button getContact = (Button)findViewById(R.id.getContact);
        getContact.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);  
				startActivityForResult(intent, 1);
			}
		});
        
        android.location.LocationManager locMan = (android.location.LocationManager)getSystemService(Context.LOCATION_SERVICE);
		 
		 boolean gpsOff = locMan.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
		 boolean networkOff = locMan.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
		
	     if (!gpsOff || !networkOff){
				 showDialog(CHECK_SETTINGS);
	     }
    }
    @Override
    public void onResume(){
    	super.onResume();
    	Intent intent = getIntent();
    	if (intent.hasExtra("lat")&&intent.hasExtra("longitude")){
    		showLocation(intent);
    	}
    }
    public void getMylocation(){
    	registerReceiver(new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				showLocation(intent);
			}
    		
    	}, new IntentFilter(context.getPackageName()+".LocationReceived"));
    	
    	Intent i = new Intent(context, LocationService.class);
    	i.putExtra("who", "me");
    	context.startService(i);
    	
	}
    
	public void getTheirLocation(String num){
		SmsManager smsMan = SmsManager.getDefault();
		short port = 32767;
		String sendString = "findloc";
		try {
		smsMan.sendDataMessage(num, null, port, sendString.getBytes(), null, null);}
		catch (Exception e){
			Toast.makeText(context, "Sending Failed", Toast.LENGTH_LONG);
		}
	}
	
	protected Dialog onCreateDialog(int id){
		
		switch (id){
			case CHECK_SETTINGS:
				AlertDialog checkSettingsDia = new AlertDialog.Builder(context)
				.setMessage("GPS or Network location services are disabled. To get the full functionality, both should be enabled.\n\n" +
						"Would you like to open the settings and enable both now?")
				.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
					
					
					public void onClick(DialogInterface arg0, int arg1) {
						Intent myIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS );
					    startActivity(myIntent);
					}
				})
				.setNeutralButton("No", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface arg0, int arg1) {
						
					}
				})
				.show();
				
				return checkSettingsDia;
		}
		return null;
	}
	public void showLocation(Intent intent){

		TextView location = (TextView)findViewById(R.id.location);
		final String lat = intent.getStringExtra("lat");
		final String longitude = intent.getStringExtra("longitude");
		String accuracy = intent.getStringExtra("accuracy");
		if (accuracy.equals("false")){
			accuracy = "Unkown";
		}
		//String address = intent.getStringExtra("address");
		location.setText(longitude+","+lat+"\n");
		
		new AlertDialog.Builder(context)
		.setMessage("Longitude: "+longitude+"\nLatitude: "+lat+"\nThe location is accurate within "+accuracy+"m")
		.setPositiveButton("Open Map", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				Uri url = Uri.parse("http://maps.google.com/maps?q=loc:"+lat+","+longitude);
				Intent map = new Intent(Intent.ACTION_VIEW, url);
				startActivity(map);
			}
		})
		.setNegativeButton("Done", null)
		.show();
	}
	@Override  
	public void onActivityResult(int reqCode, int resultCode, Intent data) {  
	    super.onActivityResult(reqCode, resultCode, data);  
	    if (resultCode == Activity.RESULT_OK) {  
	        Uri contactData = data.getData();  
	        Cursor c =  managedQuery(contactData, null, null, null, null);   
	        c.moveToFirst();
	        String number = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
	        locate_num.setText(number);
	    }  
	}
}