package com.chenzp.moneyking;

import android.R.integer;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

@SuppressWarnings("rawtypes")
public class UpdatePeachTask extends AsyncTask{

	Context context;
	public UpdatePeachTask(Context context){
		this.context = context;
	}
	/**
	 * 后台方法<br/>
	 * 	int newMoney = (Integer) arg0[0];<br/>
		int newBPeach = (Integer) arg0[1];<br/>
		int newSPeach = (Integer) arg0[2];
	 */
	@Override
	protected Object doInBackground(Object... arg0) {
		int newMoney = (Integer) arg0[0];
		int newBPeach = (Integer) arg0[1];
		int newSPeach = (Integer) arg0[2];
		
		SharedPreferences sharedPreferences
		   = context.getSharedPreferences(MainGameActivity.LASTSIGN, Context.MODE_PRIVATE);
		
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt("MONEY", newMoney);
		editor.putInt("BIGNUM", newBPeach);
	    editor.putInt("SMALLNUM",newSPeach);
	    editor.commit();	
	    
		return null;
	}

}
