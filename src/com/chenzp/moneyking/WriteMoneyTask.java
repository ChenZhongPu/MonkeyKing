package com.chenzp.moneyking;

import java.util.Calendar;


import android.content.SharedPreferences;
import android.os.AsyncTask;
/**
 * 用于异步完成写入分数工作
 * @author 中普
 *
 */
public class WriteMoneyTask extends AsyncTask<Calendar, Integer, Integer>{
	
	SharedPreferences sharedPreferences;
	int money;
	public WriteMoneyTask(SharedPreferences sharedPreferences, int money){
		this.sharedPreferences = sharedPreferences;
        this.money = money;
	}

	@Override
	protected Integer doInBackground(Calendar... params) {
		Calendar calendar = params[0];
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt("YEAR", calendar.get(Calendar.YEAR));
		editor.putInt("MONTH", calendar.get(Calendar.MONTH));
		editor.putInt("DAY", calendar.get(Calendar.DAY_OF_MONTH));
		editor.putInt("MONEY", money);
		editor.commit();
		return null;
	}

}
