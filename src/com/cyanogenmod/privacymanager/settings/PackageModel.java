package com.cyanogenmod.privacymanager.settings;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.IBinder;
import android.os.RemoteException;
import android.privacy.IPrivacyManager;

import com.android.server.privacy.impl.IPrivacyManagerConfig;

public class PackageModel {

	private static IPrivacyManagerConfig m_cfg;
	
	public static IPrivacyManagerConfig getConfig(Context applicationContext) {
		if ( m_cfg != null ) return m_cfg;
		try {
			Class<?> sm = Class.forName("android.os.ServiceManager");
			Method gs = sm.getDeclaredMethod("getService", String.class);
			IBinder ipm_binder = (IBinder) gs.invoke(null, "PrivacyManager");
			IPrivacyManager ipm = IPrivacyManager.Stub.asInterface(ipm_binder);
			IBinder cfg_bnd = ipm.getConfig();
			return m_cfg = IPrivacyManagerConfig.Stub.asInterface(cfg_bnd);
		} catch (Exception e) {
			throw new RuntimeException("no PrivacyManager installed?", e);
		}
	}

	public static void resetPackages() {
		m_cfg = null;
	}

	public static List<PackageInfo> getAllPackages(Context ctx) {
		return ctx.getPackageManager().getInstalledPackages(PackageManager.GET_PERMISSIONS | PackageManager.GET_PERMISSIONS);
	}

	public static int getColor(Context context, PackageInfo src) {
		
		if ( src.requestedPermissions == null ) return Color.BLACK;
		
		Set<String> granted = new HashSet<String>(Arrays.asList(src.requestedPermissions));
		
		try {
			List<String> rperms = getConfig(context).getRevokedPermissions(src.packageName);
			if ( rperms != null ) {
				granted.removeAll(rperms);
			}
		} catch (RemoteException e) {
			// ignore
		}
		
		if ( !granted.contains(android.Manifest.permission.INTERNET) && !granted.contains(android.Manifest.permission.RECEIVE_BOOT_COMPLETED) ) {
			return Color.BLACK;
		}

		if ( granted.contains(android.Manifest.permission.READ_CONTACTS) ||
				granted.contains(android.Manifest.permission.READ_CALENDAR) ||
				granted.contains(android.Manifest.permission.READ_SMS) ||
				granted.contains(android.Manifest.permission.GET_ACCOUNTS) 
				) {
			
			return 0xFFFF0000; // redish
		}

		if ( granted.contains(android.Manifest.permission.ACCESS_COARSE_LOCATION) || 
				granted.contains(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
			
			return 0x80FF0000; // redish
		}
		
		return 0x80804040; // orange
	}
	
	
}
