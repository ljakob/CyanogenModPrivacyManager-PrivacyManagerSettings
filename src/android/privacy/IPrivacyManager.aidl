package android.privacy;

import android.content.pm.PackageInfo;

/**
 * API for privacy 
 *
 * {@hide}
 */
interface IPrivacyManager {
  IBinder getPrivacyStub(in String service);
  
  PackageInfo filterPackageInfo(in PackageInfo info, in String packageName, in int flags);
  
  int[] filterPackageGids(in int[] gids, in String packageName);
  
  // true if granted
  boolean filterGrantedPermission(in String permName, in String packageName);

  IBinder getConfig(); // requires permission 
  
}
