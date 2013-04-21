package com.jakar.testlocator;

import android.preference.PreferenceManager;
import android.content.*;
import android.os.*;
import android.telephony.*;

public class ReceiverDataSms extends BroadcastReceiver {

		String secPassword;
		String tag = "MD_SMS_RECEIVED2";
		    
    @Override
    public void onReceive(final Context context, Intent intent){
    	Bundle bundle = intent.getExtras(); 

        String recMsgString = "";            
        String fromAddress = "";
        SmsMessage recMsg = null;
        byte[] data = null;
        //Log.i(tag, "Line: 45");
        if (bundle != null)
        {
            //---retrieve the SMS message received---
           Object[] pdus = (Object[]) bundle.get("pdus");
//           recMsg = new SmsMessage[pdus.length];            
//            recMsg = new SmsMessage[pdus.length]; 
            for (int i=0; i<pdus.length; i++){
            	//Log.i(tag, "Line: 53");
                recMsg = SmsMessage.createFromPdu((byte[])pdus[i]);
                
                try {
                	data = recMsg.getUserData();
                } catch (Exception e){

                }
                if (data!=null){
	                for(int index=0; index<data.length; ++index)
	                {
	                       recMsgString += Character.toString((char)data[index]);
	                       //Log.i(tag, "Line: 60");
	                } 
                }
                
                fromAddress = recMsg.getOriginatingAddress();
                //Log.i(tag, "Line: 63");
            }
            Intent i = new Intent(context, LocationService.class);
            i.putExtra("who", fromAddress);
            context.startService(i);
       } // end of if (bundle != null)
    }
}
