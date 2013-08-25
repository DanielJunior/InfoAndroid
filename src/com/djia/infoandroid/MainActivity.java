package com.djia.infoandroid;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
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
		bat.setText("Nível de bateria: " + level+"%");
		TextView lat_long = (TextView) findViewById(R.id.lat_long);
		lat_long.setText("Latitude e Longitude: "+latitude+" / "+longitude);
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
@Override
protected void onResume() {
	// TODO Auto-generated method stub
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
		Toast.makeText(this, "Desligado", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Ligado", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}
@Override
protected void onPause() {
	// TODO Auto-generated method stub
	super.onPause();
}
}
