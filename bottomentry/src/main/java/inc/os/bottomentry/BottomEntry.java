package inc.os.bottomentry;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import inc.os.bottomentry.entity.PayLoad;
import inc.os.bottomentry.util.AudioRecorderUtils;
import inc.os.bottomentry.util.CurosrUtil;
import inc.os.bottomentry.util.VirtualkeyUtils;
import inc.os.bottomentry.view.PicPickedItem;

import static inc.os.bottomentry.PAYLOADTYPE.PHOTOS;
import static inc.os.bottomentry.PAYLOADTYPE.TEXT;
import static inc.os.bottomentry.view.PicPickedItem.CANCEL;
import static inc.os.bottomentry.view.PicPickedItem.PICK;

public class BottomEntry extends LinearLayout implements AudioRecorderUtils.OnAudioStatusUpdateListener, PicPickedItem.OnPicPickedItemClick {
    private Context context;

    private AlphaAnimation alphaAnimation;
    private final int duration = 400;
    private boolean spread = true;
    private View rootView;//当前activity的根布局

    private View entry_view;
    private RelativeLayout sheet_voice_dialog;
    private TextView mic_tip_success;
    private TextView mic_tip_cancel;
    private TextView mic_tip_tomeTooShort;
    private View mic_voice_dialog_close;
    private RelativeLayout mic_voice_dialog_decibel;
    private LinearLayout mic_decibel_layout;
    private TextView mic_voice_dialog_record_time;
    private LinearLayout menus_layout;
    private LinearLayout bottomSheet_menus;
    private ImageView menu_moreFunction;
    private ImageView menu_camera;
    public final static int CAMERA_REQUEST_CODE = 111;
    private ImageView menu_photo;
    private ImageView menu_mic;
    private ImageView menu_switch;
    private EditText bottomSheet_edit;


    private ImageView sheet_emoji;
    private LinearLayout menu_send;
    private String sendShow;

    private LinearLayout sheet_photo;
    private FrameLayout sheet_photo_dialog;// 可提拉的布局
    private LinearLayout sheet_photo_pull;
    private GridLayout sheet_photo_gridLayout;
    private int layout_width;

    private int menus_layout_height;

    private boolean voiceRecordCancel = false;//记录 录音状态
    private String voiceRecordFilePath = "";

    private AudioRecorderUtils audioRecorderUtils;

    //内部的全局变量 维护一个选中的照片的额路径
    private List<String> pickedPhotoPaths;
    /** 自定义的属性 **/
    //最多可有选择多少张照片
    private int maxPhotoNum;
    private Drawable themeColor;

    private Handler handler = new Handler();

    private int keyBoardHeight = 1000;
    //回调接口
    private BottomEntryPayLoadListener bottomEntryPayLoadListener;



    public BottomEntry(Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        setOrientation(VERTICAL);
        this.context = context;


        TypedArray typedArray = context.obtainStyledAttributes(attributeSet , R.styleable.BottomEntry);
        if(null != typedArray){
            maxPhotoNum = typedArray.getInt(R.styleable.BottomEntry_maxPhotoNum , 4);
            if(maxPhotoNum < 1) {
                maxPhotoNum = 1;
            }else if(maxPhotoNum > 9){
                maxPhotoNum = 9;
            }
            themeColor = typedArray.getDrawable(R.styleable.BottomEntry_themeColor);
            sendShow = typedArray.getString(R.styleable.BottomEntry_sendShow);
            if(null == sendShow || "".equals(sendShow)){
                sendShow = "Send";
            }else{
                if(sendShow.length() > 1){
                    sendShow = sendShow.substring(0 , 2);
                }

            }
            //回收TypedArray，以便后面重用。在调用这个函数后，你就不能再使用这个TypedArray。
            typedArray.recycle();
        }

        initComponent();
        setListener();
        initConstant();
    }
    private void initComponent(){
        rootView = findActivity(context).getWindow().getDecorView().findViewById(android.R.id.content);
        entry_view = LayoutInflater.from(context).inflate(R.layout.entry_view , this , true);
        sheet_voice_dialog = entry_view.findViewById(R.id.sheet_voice_dialog);
        mic_tip_success = entry_view.findViewById(R.id.mic_tip_success);
        mic_tip_cancel = entry_view.findViewById(R.id.mic_tip_cancel);
        mic_tip_tomeTooShort = entry_view.findViewById(R.id.mic_tip_tomeTooShort);
        mic_voice_dialog_close = entry_view.findViewById(R.id.mic_voice_dialog_close);
        mic_voice_dialog_decibel = entry_view.findViewById(R.id.mic_voice_dialog_decibel);
        mic_decibel_layout = entry_view.findViewById(R.id.mic_decibel_layout);
        mic_voice_dialog_record_time = entry_view.findViewById(R.id.mic_voice_dialog_record_time);
        menus_layout = entry_view.findViewById(R.id.menus_layout);
        if(themeColor != null){
            menus_layout.setBackground(themeColor);
        }
        bottomSheet_menus = entry_view.findViewById(R.id.bottomSheet_menus);
        menu_moreFunction = entry_view.findViewById(R.id.menu_moreFunction);
        menu_camera = entry_view.findViewById(R.id.menu_camera);
        menu_photo = entry_view.findViewById(R.id.menu_photo);
        menu_mic = entry_view.findViewById(R.id.menu_mic);
        menu_switch = entry_view.findViewById(R.id.menu_switch);
        iconSwitchToContract();
        bottomSheet_edit = entry_view.findViewById(R.id.bottomSheet_edit);
        bottomSheet_edit.requestFocus();
        sheet_emoji = entry_view.findViewById(R.id.sheet_emoji);
        menu_send = entry_view.findViewById(R.id.menu_send);
        ((TextView)menu_send.getChildAt(0)).setText(sendShow);
        sheet_photo = entry_view.findViewById(R.id.sheet_photo);
        sheet_photo_dialog = entry_view.findViewById(R.id.sheet_photo_dialog);
        sheet_photo_pull= entry_view.findViewById(R.id.sheet_photo_pull);
        sheet_photo_gridLayout = entry_view.findViewById(R.id.sheet_photo_gridLayout);
    }
    private void initConstant(){
        //初始化
        pickedPhotoPaths = new ArrayList<>();

        bottomSheet_menus.post(() -> {
            layout_width = bottomSheet_menus.getWidth();//688
            if(0 >= layout_width){
                layout_width = 688;//688w为 三星手机note8上的宽度
            }
        });



    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListener(){
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() ->{
            Rect rect = new Rect();
            findActivity(context).getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            //获取屏幕的高度
            int screen_height = findActivity(context).getWindow().getDecorView().getRootView().getHeight();

            int virtualHeight = VirtualkeyUtils.getNavigationBarHeight(findActivity(context));
            if(screen_height - rect.bottom - virtualHeight != 0){
                keyBoardHeight = screen_height - rect.bottom - virtualHeight;
            }
        });




        entry_view.setOnTouchListener((view, motionEvent) -> {
            entry_view.setFocusable(true);
            entry_view.setFocusableInTouchMode(true);
            entry_view.requestFocus();
            return false;
        });
        menu_moreFunction.setOnClickListener((v) -> {
            if(sheet_photo.getVisibility() == View.VISIBLE){
                bottomSheet_edit.setFocusableInTouchMode(true);
                bottomSheet_edit.requestFocus();

                sheet_photo.setVisibility(GONE);
                findActivity(context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });
        menu_camera.setOnClickListener(v ->{
            //打开相机
            Intent intent_camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            findActivity(context).startActivity(intent_camera);
        });
        //打开相册
        menu_photo.setOnClickListener(v ->{
            sheet_photo.setVisibility(VISIBLE);
            keyBoardDisplayMode(false);
            bottomSheet_edit.clearFocus();
            FrameLayout.LayoutParams linearLayoutParams = new FrameLayout.LayoutParams(sheet_photo.getLayoutParams());
            linearLayoutParams.height = keyBoardHeight;

            menus_layout_height = menus_layout.getHeight();

            linearLayoutParams.topMargin = menus_layout_height;
            sheet_photo.setLayoutParams(linearLayoutParams);
            sheet_photo_gridLayout.removeAllViews();

            List<String> pathList = CurosrUtil.getPhonePicPathList(context);
            pathList.sort(Comparator.reverseOrder());//使用java8提供的方法 倒序排列
            pathList.forEach(item  -> {
                PicPickedItem picPickedItem = new PicPickedItem(context , null);
                picPickedItem.setOnPicPickedItemClick(this);
                picPickedItem.show(item);
                sheet_photo_gridLayout.addView(picPickedItem);
            });
            setMenuClickable(false);
            setEmojiAble(false);
            setSendAble(false);
        });
        menu_mic.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN : {
                    if(!menu_mic.isClickable()){
                        break;
                    }
                    sheet_voice_dialog.setVisibility(VISIBLE);
                    if(null == audioRecorderUtils){
                        audioRecorderUtils = AudioRecorderUtils.getInstance(context);
                        mic_tip_success.setVisibility(VISIBLE);
                        mic_tip_tomeTooShort.setVisibility(GONE);
                        mic_tip_cancel.setVisibility(GONE);
                        viewMove(mic_voice_dialog_close , 0 , -0);
                        viewMove(mic_voice_dialog_decibel , 0, 0);
                        voiceRecordFilePath = audioRecorderUtils.startRecord();//启动 返回文件位置
                        audioRecorderUtils.setOnAudioStatusUpdateListener(this);
                    }
                    break;
                }
                case MotionEvent.ACTION_UP :{
                    if(!menu_mic.isClickable()){
                        break;
                    }
                    //根据状态 决定要执行的操作
                    if(voiceRecordCancel){
                        if(null != audioRecorderUtils){
                            audioRecorderUtils.cancelRecord();
                            audioRecorderUtils = null;
                        }
                        handler.post(setMicDialogMissRunnable);
                    }else{
                        //停止录制
                        if(null != audioRecorderUtils){
                            audioRecorderUtils.stopRecord();
                            audioRecorderUtils = null;
                        }

                        if(recordTime < 1000 ){
                            mic_tip_success.setVisibility(GONE);
                            mic_tip_tomeTooShort.setVisibility(VISIBLE);
                            handler.postDelayed(setMicDialogMissRunnable , 1000);
                        }else{
                            handler.post(setMicDialogMissRunnable);

                            if(null != bottomEntryPayLoadListener){
                                PayLoad payLoad = new PayLoad();
                                payLoad.setType(PAYLOADTYPE.VOICE);
                                payLoad.setFilePathList(Arrays.asList(voiceRecordFilePath));
                                payLoad.setVoiceRecordTime(recordTime);
                                //更新接口
                                bottomEntryPayLoadListener.payload(payLoad);
                            }
                        }
                    }
                    break;
                }
                case MotionEvent.ACTION_MOVE:{
                    if(!menu_mic.isClickable()){
                        break;
                    }
                    float position_x = motionEvent.getX();//监听点击坐标
                    float position_y = motionEvent.getY();
                    changeDialogFactor(position_x , position_y);
                    break;
                }
            }
            return false;
        });
        menu_switch.setOnClickListener(v -> {
            if(spread){
                handler.post(contractMenuRunnable);
            }else{
                handler.post(spreadMenuRunnable);
            }
        });
        bottomSheet_edit.setOnFocusChangeListener((view, b) -> {
            if(!b){
                closeKeyBoard(view);
                if(!spread){
                    spreadMenu();
                    iconSwitchToContract();
                }
            }else{
                keyBoardDisplayMode(false);

                handler.postDelayed(() -> {
                    sheet_photo.setVisibility(GONE);
                    keyBoardDisplayMode(true);
                }, 200);
                if(spread){
                    contractMenu();
                    iconSwitchToSpread();
                }

                setMenuClickable("".equals(bottomSheet_edit.getText().toString()));
                setEmojiAble("".equals(bottomSheet_edit.getText().toString()));
                setSendAble(!"".equals(bottomSheet_edit.getText().toString()));
            }
        });
        bottomSheet_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                if("".equals(editable.toString())){
                    setMenuClickable(true);
                    setSendAble(false);
                    if(!spread){
                        handler.post(spreadMenuRunnable);
                    }
                }else{
                    setMenuClickable(false);
                    setSendAble(true);
                    if(spread){
                        handler.post(contractMenuRunnable);
                    }
                }
            }
        });
        menu_send.setOnClickListener((v) -> {
            if(null != bottomEntryPayLoadListener){
                //图片
                if(pickedPhotoPaths.size() > 0 ){
                    PayLoad picPayLoad = new PayLoad();
                    picPayLoad.setType(PHOTOS);
                    picPayLoad.setFilePathList(pickedPhotoPaths);
                    bottomEntryPayLoadListener.payload(picPayLoad);
                    clearPhotoPick();
                }else if(!"".equals(bottomSheet_edit.getText().toString())){
                    //文字
                    PayLoad textPayLoad = new PayLoad();
                    textPayLoad.setType(TEXT);
                    textPayLoad.setTextContain(bottomSheet_edit.getText().toString());
                    bottomEntryPayLoadListener.payload(textPayLoad);
                }
            }
            setMenuClickable(true);
            setSendAble(false);
            bottomSheet_edit.setText("");
            bottomSheet_edit.requestFocus();

        });
        sheet_photo_pull.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN : {
                    break;
                }
                case MotionEvent.ACTION_UP : {
                    if(null == sheet_photo_layoutParam){
                        sheet_photo_layoutParam = new FrameLayout.LayoutParams(sheet_photo.getLayoutParams());
                    }

                    if(sheet_photo_layoutParam.height > 1.5 * keyBoardHeight){
                        sheet_photo_layoutParam.height = 2 * keyBoardHeight;
                        sheet_photo_layoutParam.topMargin = 0;
                    }else {
                        sheet_photo_layoutParam.height = keyBoardHeight;
                        sheet_photo_layoutParam.topMargin = menus_layout.getHeight();
                    }
                    sheet_photo.setLayoutParams(sheet_photo_layoutParam);
                    break;
                }
                case MotionEvent.ACTION_MOVE : {
                    //监听点击坐标
                    float position_y = motionEvent.getY();
                    pullSheetPhoto( (int) position_y);

                    break;
                }
            }
            return false;
        });
    }

    Runnable setMicDialogMissRunnable = this::micDialogMiss;
    Runnable spreadMenuRunnable = () -> {
        spreadMenu();
        iconSwitchToContract();
    };

    Runnable contractMenuRunnable = () ->{
        contractMenu();
        iconSwitchToSpread();
    };

    private void setMenuClickable(boolean clickable){
        menu_camera.setImageResource(clickable ? R.drawable.icon_camera : R.drawable.icon_camera_un);
        menu_camera.setClickable(clickable);
        menu_photo.setImageResource(clickable ? R.drawable.icon_photo : R.drawable.icon_photo_un);
        menu_photo.setClickable(clickable);
        menu_mic.setImageResource(clickable ? R.drawable.icon_mic : R.drawable.icon_mic_un);
        menu_mic.setClickable(clickable);

    }
    private void setEmojiAble(boolean emojiAble){
        sheet_emoji.setImageResource(emojiAble ? R.drawable.icon_emoji : R.drawable.icon_emoji_un);
        sheet_emoji.setClickable(emojiAble);
    }
    private void setSendAble(boolean sendAble){
        menu_send.setBackgroundResource(sendAble ? R.drawable.shape_corner_send  : R.drawable.shape_corner_send_un);
        menu_send.setClickable(sendAble);
    }


    private void micDialogMiss(){
        sheet_voice_dialog.setVisibility(GONE);
        //清楚 分贝记录布局中的数据
        mic_decibel_layout.removeAllViews();
    }
    private boolean hasBig = false;
    private void changeDialogFactor(float position_x , float position_y){
        if(position_y > 0){
            position_y = 0;
        }
        if(position_y < -470f){
            position_y = -470f;
        }
        if(position_y < -376 && !hasBig){
            voiceRecordCancel = true;
            Vibrator vibrator = (Vibrator)context.getSystemService(context.VIBRATOR_SERVICE);
            vibrator.vibrate(20);
            mic_tip_success.setVisibility(GONE);
            mic_tip_cancel.setVisibility(VISIBLE);

            mic_voice_dialog_close.setScaleX(1.1f);
            mic_voice_dialog_close.setScaleY(1.1f);
            hasBig = true;
        }else if(position_y >= -376 && hasBig){
            voiceRecordCancel = false;
            mic_tip_success.setVisibility(VISIBLE);
            mic_tip_cancel.setVisibility(GONE);

            mic_voice_dialog_close.setScaleX(1.0f);
            mic_voice_dialog_close.setScaleY(1.0f);
            hasBig = false;
        }

        viewMove(mic_voice_dialog_close , position_x , -position_y);
        viewMove(mic_voice_dialog_decibel , position_x, position_y);

    }
    // 展开
    private void spreadMenu(){
        if(null == bottomSheet_menus){
            return;
        }
        if(null != alphaAnimation){
            alphaAnimation.cancel();
        }
        alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        bottomSheet_menus.startAnimation(alphaAnimation);
        //展开
        ValueAnimator valueAnimator = createValueAnimator(bottomSheet_menus, 0, layout_width);
        valueAnimator.setDuration(duration);
        valueAnimator.start();

        spread = true;
    }
    //收缩
    private void contractMenu(){
        if(null == bottomSheet_menus){
            return;
        }
        if(null != alphaAnimation){
            alphaAnimation.cancel();
        }

        alphaAnimation = new AlphaAnimation(1f, 0.0f);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        bottomSheet_menus.startAnimation(alphaAnimation);
        //收缩
        ValueAnimator valueAnimator = createValueAnimator(bottomSheet_menus, layout_width, 0);
        valueAnimator.setDuration(duration);
        valueAnimator.start();
        spread = false;
    }

    private void iconSwitchToSpread(){
        menu_switch.setImageResource(R.drawable.icon_spread);
    }
    private void iconSwitchToContract(){
        menu_switch.setImageResource(R.drawable.icon_contract);
    }

    private ValueAnimator createValueAnimator(final View view , int x_start , int x_end){
        ValueAnimator valueAnimator = ValueAnimator.ofInt(x_start, x_end);
        valueAnimator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = value;
            view.setLayoutParams(layoutParams);
        });
        return valueAnimator;
    }

    private void viewMove(View view , float x , float y){
        if(null == view)return;
        view.setTranslationX(x);
        view.setTranslationY(y);
    }

    @Override
    public void onDecibelUpdate(double db) {
        //更新 音频的显示
        int decibel = (int) db;
        reFreshDecibel(decibel);
    }

    private String record_ms_str = "";
    private long recordTime;
    @Override
    public void recordTime(long recordTime_ms) {
        recordTime = recordTime_ms;
        record_ms_str = recordTime_ms / 1000 + " '";
        mic_voice_dialog_record_time.setText(record_ms_str);
        if(recordTime_ms > 50 * 1000 ){
            record_ms_str = "还可录制 " + ( 60 - recordTime_ms / 1000 ) + " '";
            mic_voice_dialog_record_time.setText(record_ms_str);
            mic_voice_dialog_record_time.setTextColor(getResources().getColor(R.color.google_red));
        }
    }

    private void reFreshDecibel(int decibel){
        if(mic_decibel_layout.getChildCount() >= 100 ){
            mic_decibel_layout.removeViewAt(0);
        }
        insertView(decibel * decibel / 50);
    }
    private void insertView(int decibel){
        View view = new View(context);
        view.setBackgroundColor( getResources().getColor(R.color.google_blue));
        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(2 , decibel);
        ll.setMarginStart(2);
        view.setLayoutParams(ll);
        mic_decibel_layout.addView(view);
    }
    private FrameLayout.LayoutParams sheet_photo_layoutParam;
    private void pullSheetPhoto(int position_y){
        if(null == sheet_photo_layoutParam){
            sheet_photo_layoutParam = new FrameLayout.LayoutParams(sheet_photo.getLayoutParams());
        }

        if(menus_layout_height == 0 ){
            menus_layout_height = menus_layout.getHeight();
        }
        // 手指还未超过 菜单布局
        if(sheet_photo_layoutParam.height - position_y >= keyBoardHeight && keyBoardHeight + menus_layout_height > sheet_photo_layoutParam.height - position_y){
            //设置 sheet_photo的marginTop的值
            sheet_photo_layoutParam.topMargin = keyBoardHeight + menus_layout_height - (sheet_photo_layoutParam.height - position_y);
            sheet_photo_layoutParam.height = sheet_photo_layoutParam.height - position_y;
        }else{
            if(sheet_photo_layoutParam.height - position_y <= keyBoardHeight){
                sheet_photo_layoutParam.height = keyBoardHeight;
                sheet_photo_layoutParam.topMargin = menus_layout.getHeight();
            }else if(sheet_photo_layoutParam.height - position_y >= keyBoardHeight * 2){
                sheet_photo_layoutParam.height = keyBoardHeight * 2;
            }else{
                sheet_photo_layoutParam.height = sheet_photo_layoutParam.height - position_y;
            }
        }

        sheet_photo.setLayoutParams(sheet_photo_layoutParam);
    }


    //选中照片的回调接口
    @Override
    public void onItem(String optionType , String pic_path) {
        if(PICK.equals(optionType)){
            if(pickedPhotoPaths.indexOf(pic_path) < 0){
                pickedPhotoPaths.add(pic_path);
            }
        }else if(CANCEL.equals(optionType)){
            if(pickedPhotoPaths.indexOf(pic_path) > -1){
                pickedPhotoPaths.remove(pic_path);
            }
        }
        checkPickedPhoneNumAndSetPickable();
    }
    private void checkPickedPhoneNumAndSetPickable(){
        //只有当临近限制的最大数量时， 进行设置
        if(pickedPhotoPaths.size() > maxPhotoNum - 2){
            int count = sheet_photo_gridLayout.getChildCount();
            for(int i = 0 ; i < count ; i ++){
                ((PicPickedItem)(sheet_photo_gridLayout.getChildAt(i))).setPickable(pickedPhotoPaths.size() < maxPhotoNum);
            }
        }
        setSendAble(pickedPhotoPaths.size() > 0);
        setEmojiAble(pickedPhotoPaths.size() < 1);
    }
    private void clearPhotoPick(){
        pickedPhotoPaths.clear();//清空
        int count = sheet_photo_gridLayout.getChildCount();
        for(int i = 0 ; i < count ; i ++){
            ((PicPickedItem)(sheet_photo_gridLayout.getChildAt(i))).clearPickState();
        }
    }

    public interface BottomEntryPayLoadListener{
        void payload(PayLoad payLoad);
    }
    public void setBottomEntryPayLoadListener(BottomEntryPayLoadListener bottomEntryPayLoadListener){
        this.bottomEntryPayLoadListener = bottomEntryPayLoadListener;
    }


    private static Activity findActivity(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        }
        if (context instanceof ContextWrapper) {
            ContextWrapper wrapper = (ContextWrapper) context;
            return findActivity(wrapper.getBaseContext());
        } else {
            return null;
        }
    }

    private void closeKeyBoard(View v){
        /*关闭小键盘*/
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        Objects.requireNonNull(imm).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    private void keyBoardDisplayMode(boolean resize){
        findActivity(context).getWindow().setSoftInputMode(resize ? WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE : WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }
}
