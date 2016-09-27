package com.ko.nearbuildings.utils;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

public class PermissionUtil {

    static private AppCompatActivity mAppCompatActivity;

    public static PermissionObject with(AppCompatActivity activity) {
        mAppCompatActivity = activity;
        return new PermissionObject();
    }

    public static class SinglePermission {

        private String mPermissionName;
        private boolean mRationalNeeded = false;
        private String mReason;

        public SinglePermission(String permissionName) {
            mPermissionName = permissionName;
        }

        public SinglePermission(String permissionName, String reason) {
            mPermissionName = permissionName;
            mReason = reason;
        }

        public boolean isRationalNeeded() {
            return mRationalNeeded;
        }

        public void setRationalNeeded(boolean rationalNeeded) {
            mRationalNeeded = rationalNeeded;
        }

        public String getReason() {
            return mReason == null ? "" : mReason;
        }

        public void setReason(String reason) {
            mReason = reason;
        }

        public String getPermissionName() {
            return mPermissionName;
        }

        public void setPermissionName(String permissionName) {
            mPermissionName = permissionName;
        }
    }

    public static class PermissionObject {

        public PermissionRequestObject request(String permissionName) {
            return new PermissionRequestObject(new String[]{permissionName});
        }

        public PermissionRequestObject request(String... permissionNames) {
            return new PermissionRequestObject(permissionNames);
        }
    }

    public interface OnPermission {
        void call();
    }

    public interface OnResult {
        void call(int requestCode, String permissions[], int[] grantResults);
    }

    abstract public class OnRational {
        protected abstract void call(String permissionName);
    }

    static public class PermissionRequestObject {

        private static final String TAG = PermissionObject.class.getSimpleName();

        private ArrayList<SinglePermission> permissions;
        private int requestCode;
        private OnPermission grantAllPermission;
        private OnPermission denyPermission;
        private OnResult onResult;
        private OnRational onRational;
        private String[] permissionNames;

        public PermissionRequestObject(String[] permissionNames) {
            this.permissionNames = permissionNames;
        }

        /**
         * Execute the permission request with the given Request Code
         *
         * @param reqCode a unique request code in your activity
         */
        public PermissionRequestObject ask(int reqCode) {
            requestCode = reqCode;
            int length = permissionNames.length;
            permissions = new ArrayList<>(length);
            for (String mPermissionName : permissionNames) {
                permissions.add(new SinglePermission(mPermissionName));
            }

            if (needToAsk()) {
                Log.i(TAG, "Asking for permission");
                ActivityCompat.requestPermissions(mAppCompatActivity, permissionNames, reqCode);
            } else {
                Log.i(TAG, "No need to ask for permission");
                if (grantAllPermission != null) grantAllPermission.call();
            }
            return this;
        }

        private boolean needToAsk() {
            ArrayList<SinglePermission> neededPermissions = new ArrayList<>(permissions);
            for (int i = 0; i < permissions.size(); i++) {
                SinglePermission perm = permissions.get(i);
                int checkRes = ContextCompat.checkSelfPermission(mAppCompatActivity, perm.getPermissionName());
                if (checkRes == PackageManager.PERMISSION_GRANTED) {
                    neededPermissions.remove(perm);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(mAppCompatActivity, perm.getPermissionName())) {
                        perm.setRationalNeeded(true);
                    }
                }
            }
            permissions = neededPermissions;
            permissionNames = new String[permissions.size()];
            for (int i = 0; i < permissions.size(); i++) {
                permissionNames[i] = permissions.get(i).getPermissionName();
            }
            return permissions.size() != 0;
        }

        /**
         * Called for the first denied permission if there is need to show the rational
         */
        public PermissionRequestObject onRational(OnRational rationalFunc) {
            onRational = rationalFunc;
            return this;
        }

        /**
         * Called if all the permissions were granted
         */
        public PermissionRequestObject onAllGranted(OnPermission grantOnPermission) {
            this.grantAllPermission = grantOnPermission;
            return this;
        }

        /**
         * Called if there is at least one denied permission
         */
        public PermissionRequestObject onAnyDenied(OnPermission denyOnPermission) {
            this.denyPermission = denyOnPermission;
            return this;
        }

        /**
         * Called with the original operands from {@link AppCompatActivity#onRequestPermissionsResult(int, String[], int[])
         * onRequestPermissionsResult} for any result
         */
        public PermissionRequestObject onResult(OnResult resultFunc) {
            onResult = resultFunc;
            return this;
        }

        /**
         * This Method should be called from {@link AppCompatActivity#onRequestPermissionsResult(int, String[], int[])
         * onRequestPermissionsResult} with all the same incoming operands
         * <pre>
         * {@code
         *
         * public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
         *      if (mStoragePermissionRequest != null)
         *          mStoragePermissionRequest.onRequestPermissionsResult(requestCode, permissions,grantResults);
         * }
         * }
         * </pre>
         */
        public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

            if (permissions.length > 0 && grantResults.length > 0) {
                Log.i(TAG, String.format("ReqCode: %d, ResCode: %d, PermissionName: %s", requestCode, grantResults[0], permissions[0]));
            }

            if (this.requestCode == requestCode) {
                if (onResult != null) {
                    Log.i(TAG, "Calling Results function");
                    onResult.call(requestCode, permissions, grantResults);
                    return;
                }

                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        if (this.permissions.get(i).isRationalNeeded()) {
                            if (onRational != null) {
                                Log.i(TAG, "Calling Rational function");
                                onRational.call(this.permissions.get(i).getPermissionName());
                            }
                        }
                        if (denyPermission != null) {
                            Log.i(TAG, "Calling Deny function");
                            denyPermission.call();
                        } else Log.e(TAG, "NUll DENY FUNCTIONS");

                        // terminate if there is at least one deny
                        return;
                    }
                }

                // there has not been any deny
                if (grantAllPermission != null) {
                    Log.i(TAG, "Calling Grant onDenied");
                    grantAllPermission.call();
                } else Log.e(TAG, "NUll GRANT FUNCTIONS");
            }
        }
    }

}
