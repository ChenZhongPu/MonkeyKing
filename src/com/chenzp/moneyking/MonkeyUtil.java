package com.chenzp.moneyking;

import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * 工具类
 * @author 中普
 *
 */
public class MonkeyUtil {

	/**
	 * 新的时间是否比旧的时间晚1天（及以上）
	 * @param newCalendar
	 * @param oldCalendar
	 * @return
	 */
	public static boolean isAfterADay(Calendar newCalendar, Calendar oldCalendar)
	{
		Log.d("TEST", "run isAferDay");
		if(newCalendar.get(Calendar.YEAR) > oldCalendar.get(Calendar.YEAR))
			return true;
		if(newCalendar.get(Calendar.YEAR) == oldCalendar.get(Calendar.YEAR) 
				&& newCalendar.get(Calendar.MONTH) > oldCalendar.get(Calendar.MONTH))
			return true;
		if(newCalendar.get(Calendar.YEAR) == oldCalendar.get(Calendar.YEAR)
				&& newCalendar.get(Calendar.MONTH) == oldCalendar.get(Calendar.MONTH)
				&& newCalendar.get(Calendar.DAY_OF_MONTH) > oldCalendar.get(Calendar.DAY_OF_MONTH))
			return true;
		return false;
	}
	
	/**
	 * 从sharedPreference取得数据
	 * @param sharedKey
	 * @param key
	 * @param context
	 * @return
	 */
	public static int getDataFromShared(String sharedKey, String key, Context context) {
		
		SharedPreferences sharedPreferences = context.getSharedPreferences(sharedKey, Context.MODE_PRIVATE);
		
		return sharedPreferences.getInt(key, 0);
	}
}
