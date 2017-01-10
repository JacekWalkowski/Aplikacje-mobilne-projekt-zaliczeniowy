package com.example.blueexpert;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Skroty extends Activity {

	private Button btnWstecz,btnDalej,btnVolUp,btnVolDown, btnPokaz,btnStart;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_skroty);
		btnWstecz = (Button) findViewById(R.id.button1);
		btnDalej = (Button) findViewById(R.id.button2);
		btnVolDown = (Button) findViewById(R.id.button3);
		btnVolUp = (Button) findViewById(R.id.button4);
		btnStart = (Button) findViewById(R.id.button5);
		btnPokaz = (Button) findViewById(R.id.button6);
		
		btnWstecz.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				byte[] a = new byte[1];
				a[0] = (byte) 255;
				MainActivity.blueExpertService.write(a);
			}
		});
		btnDalej.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				byte[] a = new byte[1];
				a[0] = (byte) 254;
				MainActivity.blueExpertService.write(a);
			}
		});
		btnVolDown.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				byte[] a = new byte[1];
				a[0] = (byte) 253;
				MainActivity.blueExpertService.write(a);
			}
		});
		btnVolUp.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				byte[] a = new byte[1];
				a[0] = (byte) 252;
				MainActivity.blueExpertService.write(a);
			}
		});
		btnStart.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				byte[] a = new byte[1];
				a[0] = (byte) 251;
				MainActivity.blueExpertService.write(a);
			}
		});
		btnPokaz.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				byte[] a = new byte[1];
				a[0] = (byte) 250;
				MainActivity.blueExpertService.write(a);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.skroty, menu);
		return true;
	}

}
