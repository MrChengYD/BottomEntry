# BottomEntry  

## 介绍

### 适用

聊天页面  

### 功能  
 - 选择图片 ，自定义选择上限 . 默认4 ， 最小 1 ， 最大 9
 - 录制音频 ，监听音频分贝值 ， 松开发送，上滑取消  
 - 动画效果  
 
### 效果  
 - 
![](https://i.imgur.com/3m5y5Ru.jpg)   ![](https://i.imgur.com/rfP2TFt.jpg)  
 -   
![](https://i.imgur.com/ijxXul5.jpg)   ![](https://i.imgur.com/lGYTntK.png)  


使用简单，方便 ， Activity实现对应的接口即可获取数据

### 使用 

### step.1 Add it in your root build.gradle at the end of repositories:  

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

### step.2 Add the dependency  

	dependencies {
	        implementation 'com.github.MrChengYD:BottomEntry:v0.8.1'
	}

### step.3 Edit your layout.xml  

	<?xml version="1.0" encoding="utf-8"?>
	<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
	    android:layout_width="match_parent"
	    android:layout_height="match_parent"
	    xmlns:app="http://schemas.android.com/apk/res-auto"
	    android:clipChildren="false">

		<!-- 自定义属性
			maxPhotoNum: 最大选择张数 ， 默认 4 ，最小 1 ， 最大 9 
			themeColor: 面板颜色 , reference
			sendShow： 发送按钮的显示文字,默认 Send;自定义：String.subString(0 , 2);
		 -->
	    <inc.os.bottomentry.BottomEntry
		android:id="@+id/bottomEntry"
		android:layout_width="match_parent"
		android:layout_height="wrap_content"
		android:clipChildren="false"
		android:layout_alignParentBottom="true"
		app:maxPhotoNum="6"/>

	</RelativeLayout>`  

### step.4 初始化  

在此之前，需要先申请对应的权限， 比如 录制权限，麦克风权限，相册权限

	public class MainActivity extends Activity implements EasyPermissions.PermissionCallbacks {
	    private Context context = this;
	    private BottomEntry bottomEntry;

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
	}

##  更新日志  

### v0.8.1  第一版 基础功能

## tips 

 - 调用相机将在之后的版本中完善   
 - 部分动画出现异常，同样在之后的版本中完善 
 
cheng9112@yahoo.com
