package inc.os;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import inc.os.bottomentry.BottomEntry;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends Activity implements EasyPermissions.PermissionCallbacks {
    private Context context = this;
    private BottomEntry bottomEntry;
    private static final int RC_CAMERA_AND_RECORD_AUDIO = 10000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        requestPermissions();

    }
    private void initComponent(){

        bottomEntry = findViewById(R.id.bottomEntry);

    }
    private void setListener(){
        bottomEntry.setBottomEntryPayLoadListener(payLoad -> {
            Log.i("aaa" , "输出载荷 : " + payLoad.toString());
        });
    }




    private void requestPermissions() {
        String [] permissions = {Manifest.permission.INTERNET , Manifest.permission.ACCESS_WIFI_STATE , Manifest.permission.ACCESS_NETWORK_STATE , Manifest.permission.VIBRATE , Manifest.permission.RECORD_AUDIO , Manifest.permission.READ_EXTERNAL_STORAGE ,Manifest.permission.WRITE_EXTERNAL_STORAGE };
        if(EasyPermissions.hasPermissions(context , permissions)){
            initComponent();
            setListener();
        }else{
            EasyPermissions.requestPermissions(this, "写上你需要用权限的理由, 是给用户看的", RC_CAMERA_AND_RECORD_AUDIO, permissions);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        initComponent();
        setListener();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }



}

