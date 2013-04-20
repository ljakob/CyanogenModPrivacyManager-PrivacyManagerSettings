package com.android.server.privacy.impl;

/**
 * API for privacy 
 *
 * {@hide}
 */
interface IPrivacyManagerConfig {
  List<String> getRevokeablePermissions();
  List<String> getConfiguredPackages();
  List<String> getRevokedPermissions(in String packageName);
  void setRevokedPermissions(in String packageName, in List<String> permissions);
  void removePackage(in String packageName);
  void activate();
}