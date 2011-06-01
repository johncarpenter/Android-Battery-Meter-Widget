/*
 * Copyright (c) 2011 2linessoftware.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.twolinessoftware.android.batterywidget.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Bitmap.Config;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.BatteryManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.twolinessoftware.android.batterywidget.Defaults;
import com.twolinessoftware.android.batterywidget.R;
import com.twolinessoftware.android.batterywidget.SettingsActivity;

public class BatteryWidgetUpdateService extends Service {

	private static final String LOGNAME = "BatteryWidgetUpdateService";
	private int level;

	BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			if (rawlevel >= 0 && scale > 0) {
				level = (rawlevel * 100) / scale;
			}

			updateWidget();
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startUp(intent);
		return START_STICKY;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		startUp(intent);
	}

	private void startUp(Intent intent) {

		Log.d(LOGNAME, "Updating Widget");

		registerListeners();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(batteryLevelReceiver);
	}

	private void registerListeners() {

		IntentFilter batteryLevelFilter = new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryLevelReceiver, batteryLevelFilter);
	}

	private void updateWidget() {

		RemoteViews v = new RemoteViews(getPackageName(),
				R.layout.battery_widget);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		int color = sp.getInt(Defaults.COLOR_PREFERENCE, Color.WHITE);

		v.setTextColor(R.id.textBatteryLevelSm, color);
		v.setTextViewText(R.id.textBatteryLevelSm, level + "/100");

		v.setTextColor(R.id.textBatteryLevelMed, color);
		v.setTextViewText(R.id.textBatteryLevelMed, level + "/100");

		v.setTextColor(R.id.textBatteryLevelLg, color);
		v.setTextViewText(R.id.textBatteryLevelLg, level + "/100");

		String fontSize = sp.getString(Defaults.FONTSIZE_PREFERENCE,
				getResources().getStringArray(R.array.fontSizesValues)[0]);
		if (fontSize.equals("sm")) {
			v.setViewVisibility(R.id.textBatteryLevelSm, View.VISIBLE);
			v.setViewVisibility(R.id.textBatteryLevelMed, View.GONE);
			v.setViewVisibility(R.id.textBatteryLevelLg, View.GONE);
		} else if (fontSize.equals("med")) {
			v.setViewVisibility(R.id.textBatteryLevelSm, View.GONE);
			v.setViewVisibility(R.id.textBatteryLevelMed, View.VISIBLE);
			v.setViewVisibility(R.id.textBatteryLevelLg, View.GONE);
		} else {
			v.setViewVisibility(R.id.textBatteryLevelSm, View.GONE);
			v.setViewVisibility(R.id.textBatteryLevelMed, View.GONE);
			v.setViewVisibility(R.id.textBatteryLevelLg, View.VISIBLE);
		}

		v.setInt(R.id.separator, "setBackgroundColor", color);

		// v.setProgressBar(R.id.progressBarBattery, 100, level, false);
		//Bitmap imageIndicator = BitmapFactory.decodeResource(getResources(),
		//		R.drawable.level_indicator);
		//v.setImageViewBitmap(R.id.progressBarBattery,proxy);
		v.setInt(R.id.progressBarBattery, "setColorFilter", color);

		Intent intent = new Intent(this, SettingsActivity.class);

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				intent, 0);
		v.setOnClickPendingIntent(R.id.layoutBatteryContainer, pendingIntent);

		// Push update for this widget to the home screen
		ComponentName batteryWidget = new ComponentName(this,
				BatteryWidget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(this);
		manager.updateAppWidget(batteryWidget, v);

		stopSelf();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
