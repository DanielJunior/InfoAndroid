package com.djia.infoandroid;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("ServiceCast")
public class MainActivity extends Activity implements LocationListener {
	private LocationManager lm;
	private double longitude, latitude;
	private String provider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		lm = (LocationManager) getSystemService(LOCATION_SERVICE);
	}

	private void atualizaDados() {
		// TODO Auto-generated method stub
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = this.registerReceiver(null, ifilter);
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		TelephonyManager tel = (TelephonyManager) this
				.getSystemService(MainActivity.TELEPHONY_SERVICE);
		String id = tel.getDeviceId();
		String operadora = tel.getSimOperatorName();
		TextView disp = (TextView) findViewById(R.id.dispositivo);
		disp.setText("ID: " + id);
		TextView bat = (TextView) findViewById(R.id.bateria);
		bat.setText("Nível de bateria: " + level + "%");
		TextView lat = (TextView) findViewById(R.id.lat);
		lat.setText("Latitude : " + latitude);
		TextView longi = (TextView) findViewById(R.id.longi);
		longi.setText("Longitude: " + longitude);
		TextView op = (TextView) findViewById(R.id.operadora);
		op.setText("Operadora: " + operadora);
		TextView sinal = (TextView) findViewById(R.id.sinal);
		sinal.setText("Nível de sinal: ");
		lm.removeUpdates(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void turnGPSOn() {
		// se o gps está desligado, então ligo
		provider = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if (!provider.contains("gps")) {
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings",
					"com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			sendBroadcast(poke);
		}
	}

	private void turnGPSOff() {
		provider = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		// se o gps está ligado , então o desligo
		if (provider.contains("gps")) {
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings",
					"com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			sendBroadcast(poke);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		//Estou ligando o  gps forçadamente sem o consentimento do utilizador;
		turnGPSOn();
		//caso queira que ele msm faça esta ação devo utilizar:
				/*
				Intent in = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);   
				startActivity(in);
				*/
		// Aqui você define quando quer que as informações sejam atualizadas, no
		// meu caso eu coloquei a cada 0 segundos ou 0 metros
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this);
		super.onResume();
	}

	@Override
	public void onLocationChanged(Location location) {
		longitude = location.getLongitude();
		latitude = location.getLatitude();
		atualizaDados();
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		turnGPSOff();
		super.onStop();
	}
}
