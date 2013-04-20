package com.cyanogenmod.privacymanager.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SelectPackageActivity extends Activity {

	private void updateList() {

		ListView lv = (ListView) this.findViewById(R.id.select_package_list);
		
		List<PackageInfo> all = PackageModel.getAllPackages(getApplicationContext());
		
		final List<PackageInfo> installed;
		if ( true )
		{
			List<PackageInfo> t = new ArrayList<PackageInfo>(all.size());
			for(PackageInfo p : all) {
				if ( p.applicationInfo == null ) continue;
				if ( (p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1 ) continue;
				t.add(p);
 			}
			installed = t;
		}
		else
		{
			installed = all;
		}
		
		Collections.sort(installed, new PackageInfoComparator());
		
		try {
			installed.add(0, getPackageManager().getPackageInfo("com.weite_welt.jakob.privacytestapp", PackageManager.GET_PERMISSIONS));
		} catch (NameNotFoundException e) {
			// ignore
		}
		
		lv.setAdapter(new PackageAdapter(this, installed));
		
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(SelectPackageActivity.this, ConfigurePackageActivity.class);
				intent.putExtra(ConfigurePackageActivity.EXTRA_PACKAGENAME, installed.get(position).packageName);
				startActivityForResult(intent, 0);
			}} );
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		updateList();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.select_package_layout);
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addDataScheme("package");
		registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				updateList();
				PackageModel.resetPackages();
			}}, intentFilter);
		
		updateList();
	}
	
	private static class PackageAdapter extends ArrayAdapter<PackageInfo> {

		private List<PackageInfo> m_objects;

		public PackageAdapter(Context context, List<PackageInfo> objects) {
			super(context, R.layout.select_package_item_layout, objects);
			m_objects = objects;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.select_package_item_layout, null);
			}

			PackageInfo src = m_objects.get(position);

			{
				convertView.setBackgroundColor(PackageModel.getColor(getContext(), src));
			}
			
			{
				if ( src.applicationInfo != null ) {
					Drawable draw = src.applicationInfo.loadIcon(getContext().getPackageManager());
					ImageView iv = (ImageView) convertView.findViewById(R.id.select_package_item_icon);
					iv.setImageDrawable(draw);
				}
			}

			{
				if ( src.applicationInfo != null ) {
					TextView t = (TextView) convertView.findViewById(R.id.select_package_item_name);
					t.setText(getContext().getPackageManager().getApplicationLabel(src.applicationInfo));
				}
			}

			{
				TextView t = (TextView) convertView.findViewById(R.id.select_package_item_package);
				t.setText("pack=" + src.packageName);
			}

			return convertView;
		}
		
	}
	
	private class PackageInfoComparator implements Comparator<PackageInfo>{

		@Override
		public int compare(PackageInfo lhs, PackageInfo rhs) {
			return get(lhs).compareToIgnoreCase(get(rhs));
		}

		private String get(PackageInfo pi) {
			if ( pi.applicationInfo == null ) return pi.packageName;
			return SelectPackageActivity.this.getPackageManager().getApplicationLabel(pi.applicationInfo).toString();
		}
		
	}
	
	
}
