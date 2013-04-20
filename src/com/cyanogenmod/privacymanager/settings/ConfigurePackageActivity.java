package com.cyanogenmod.privacymanager.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.server.privacy.impl.IPrivacyManagerConfig;

public class ConfigurePackageActivity extends Activity {

	private static final String TAG = "Privacy";

	public static final String EXTRA_PACKAGENAME = "package";
	private String m_packageName;
	private IPrivacyManagerConfig m_cfg;
	private Set<String> m_revokeAble;
	private Set<String> m_revoked;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.configure_package_layout);

		m_packageName = getIntent().getExtras().getString(EXTRA_PACKAGENAME);

		{
			// setup config
			try {
				m_cfg = PackageModel.getConfig(getApplicationContext());

				m_revokeAble = new HashSet<String>(m_cfg.getRevokeablePermissions());

				List<String> revoked = m_cfg.getRevokedPermissions(m_packageName);
				if (revoked == null) { // no setting
					revoked = Collections.emptyList();
				}
				m_revoked = new TreeSet<String>(revoked);

			} catch (Exception e) {

				Log.e(TAG, "error", e);
				Toast.makeText(this, e.getClass().getName() + ":" + e.getMessage(), Toast.LENGTH_LONG).show();
				finish();
				return;
			}

		}

		{
			TextView tv = (TextView) findViewById(R.id.configure_package_packagename);
			tv.setText(m_packageName);
		}

		ListView lv = (ListView) findViewById(R.id.configure_package_list);
		PackageInfo pi;
		try {
			pi = getPackageManager().getPackageInfo(m_packageName, PackageManager.GET_PERMISSIONS);
		} catch (NameNotFoundException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		List<String> perms = pi.requestedPermissions == null ? Collections.<String>emptyList() : Arrays.asList(pi.requestedPermissions);
		lv.setAdapter(new PermissionAdapter(this, perms));

	}

	@Override
	public void finish() {
		super.finish();

		if (m_cfg == null)
			return;

		try {
			m_cfg.setRevokedPermissions(m_packageName, new ArrayList<String>(m_revoked));
			Toast.makeText(this, "saved for " + m_packageName, Toast.LENGTH_SHORT).show();
		} catch (RemoteException e) {
			Log.e(TAG, "error", e);
			Toast.makeText(this, e.getClass().getName() + ":" + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		{
			MenuItem item = menu.add("Activate Globally");
			item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

				@Override
				public boolean onMenuItemClick(MenuItem item) {
					try {
						m_cfg.activate();
						Toast.makeText(ConfigurePackageActivity.this, "OK", Toast.LENGTH_SHORT).show();
					} catch (RemoteException e) {
						Log.e(TAG, "error", e);
						Toast.makeText(ConfigurePackageActivity.this, e.getClass().getName() + ":" + e.getMessage(), Toast.LENGTH_LONG).show();
					}
					return true;
				}
			});
		}

		return super.onCreateOptionsMenu(menu);
	}

	private class PermissionAdapter extends ArrayAdapter<String> {

		public PermissionAdapter(Context context, List<String> objects) {
			super(context, R.layout.configure_package_item_layout, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.configure_package_item_layout, null);

				CheckBox granted = (CheckBox) convertView.findViewById(R.id.configure_package_item_grant);
				granted.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						String perm = (String) v.getTag();
						CheckBox box = (CheckBox) v;
						if (box.isChecked()) {
							m_revoked.remove(perm);
						} else {
							m_revoked.add(perm);
						}
					}
				});
			}

			String perm = getItem(position);

			{
				TextView t = (TextView) convertView.findViewById(R.id.configure_package_item_description);
				t.setText("perm=" + perm);
			}

			{
				CheckBox granted = (CheckBox) convertView.findViewById(R.id.configure_package_item_grant);
				granted.setTag(perm);

				if (m_revokeAble.contains(perm)) {
					granted.setEnabled(true);
					granted.setChecked(!m_revoked.contains(perm));
				} else {
					granted.setChecked(true);
					granted.setEnabled(false);
					m_revoked.remove(perm);
				}
			}

			return convertView;
		}

	}

}
