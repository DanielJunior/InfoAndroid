package com.djia.infoandroid;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

//implemento LocationListener para através dos seus métodos obter longitude e latitude
//implemento onClickListener para pegar o evento do alertDialog, responsável por ligar o GPS
public class MainActivity extends Activity implements LocationListener,
		OnClickListener {
	private LocationManager lm;
	private double longitude, latitude;
	private String provider;
	TelephonyManager tel;
	MyPhoneStateListener MyListener;
	private static int signalAsu;
	private int signaldBm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// pego o gerenciador de localização, com ele faço requisição de
		// localização
		lm = (LocationManager) getSystemService(LOCATION_SERVICE);
		// instancio um MyListener (classe que está dentro desta aqui.... no
		// final do arquivo)
		// com ele vou conseguir obter a potencia de sinal
		MyListener = new MyPhoneStateListener();
		// pego o gerenciador do telefone, como ele obtenho infos como operadora
		tel = (TelephonyManager) this
				.getSystemService(MainActivity.TELEPHONY_SERVICE);
		// crio uma "observador" que irá monitorar o sinal, para isso passo o
		// MyListener e oq quero monitorar
		// neste caso a potencia de sinal...
		tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		// verifico se o GPS está ligado
		if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			//os eventos do alert serão gerenciados pelo metodo onClick implementado pela classe
			AlertDialog.Builder alerta = new AlertDialog.Builder(this);
			alerta.setMessage("Deseja ativar GPS?");
			alerta.setPositiveButton("Sim", this);
			alerta.setNegativeButton("Não", this);
			alerta.show();
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		switch (which) {
		case DialogInterface.BUTTON_NEGATIVE:
			ultimosValores();
			atualizaDados();
			break;
		case DialogInterface.BUTTON_POSITIVE:
			//caso o botão sim foi clicado, abro a activity de configurações para o usuário ligar o gps
			Intent in = new Intent(
					android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivityForResult(in, 1);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		//verifico se o GPS foi ativado, e se não, pego os ultimos valores conhecidos
		if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			ultimosValores();
			atualizaDados();
		}
	}

	public void ultimosValores() {
		Criteria criteria = new Criteria();
		provider = lm.getBestProvider(criteria, false);
		// Retorna a localização com a data da última localização conhecida
		Location location = lm.getLastKnownLocation(provider);
		latitude = location.getLatitude();
		longitude = location.getLongitude();
	}

	// evento do botão atualizar, chamo o onResume para atualizar
	public void atualizar(View view) {

		tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		onResume();
	}

	// aqui obtenho o estado da bateria , o IMEI do dispositivo , a operadora, e
	// com base em outros dados obtidos(lat ,long, pot de sinal)
	// a partir de outros eventos atualizo minha UI
	private void atualizaDados() {
		// TODO Auto-generated method stub
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		// uma intent que recupera a mudança de valor da bateria
		Intent batteryStatus = this.registerReceiver(null, ifilter);
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
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
		// o sinal que obtenho no método onChangedSignalStreght eh na escala
		// ASU, então com essa expressão faço a conversão para
		// dBm
		signaldBm = -113 + (2 * signalAsu);
		sinal.setText("Nível de sinal: " + signaldBm + "dBm");
		lm.removeUpdates(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// método que força a ligação do GPS sem a necessidade de intervenção do
	// usuário
	@SuppressWarnings("unused")
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

		// Aqui você define quando quer que as informações sejam atualizadas, no
		// meu caso eu coloquei a cada 0 segundos ou 0 metros
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this);
		super.onResume();
	}
@Override

//A LINHA ABAIXO FAZ A ACTIVITY NÃO REINICIAR QUANDO GIRAR, JUNTO COMO O UMA INSTRUÇÃO COMENTADA NO MANIFEST
public void onConfigurationChanged(Configuration newConfig) {
	// TODO Auto-generated method stub
	super.onConfigurationChanged(newConfig);
}
	// os proximos 4 métodos são da classe LocationManager
	@Override
	public void onLocationChanged(Location location) {
		longitude = location.getLongitude();
		latitude = location.getLatitude();
		atualizaDados();
	}

	// esse método é chamado caso o gps esteja desligado...
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
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// para o listener para economizar bateria
		tel.listen(MyListener, PhoneStateListener.LISTEN_NONE);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		turnGPSOff();
		super.onStop();
	}

	// o observador ...
	private class MyPhoneStateListener extends PhoneStateListener {
		/*
		 * Get the Signal strength from the provider, each tiome there is an
		 * update
		 */
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			signalAsu = signalStrength.getGsmSignalStrength();
		}
	};
}
