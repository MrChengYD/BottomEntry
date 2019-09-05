package inc.os.bottomentry.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class AudioRecorderUtils {
    public static AudioRecorderUtils utils;
    private Context context;
    private AudioRecorderUtils(Context context) {
        this.context = context;
    }
    public synchronized static AudioRecorderUtils getInstance(Context context){
        if(utils==null){
            utils=new AudioRecorderUtils(context);
        }
        return utils;
    }
    private MediaRecorder mMediaRecorder;
    private MediaPlayer mMediaPlayer;
    private boolean isPlayer=false;
    private final String TAG = "MediaRecord";
    public static final int MAX_LENGTH = 1000 * 60 * 10;// 最大录音时长1000*60*10;

    private OnAudioStatusUpdateListener audioStatusUpdateListener;
    private OnMediaPlayerCompletionListen mediaPlayerCompletionListen;
    private long startTime;
    private long endTime;
    private String currentFilePath="";

    /**
     * 开始录音 使用amr格式
     * 录音文件
     *
     * @return
     */
    public String startRecord() {
        String filePath = getFilePath();
        File file = new File(filePath);
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 开始录音
        /* ①Initial：实例化MediaRecorder对象 */
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        try {
            /* ②setAudioSource/setVideoSource */
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置麦克风
            /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 */
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            /*
             * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
             * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
             */
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            /* ③准备 */
            currentFilePath=filePath;
            mMediaRecorder.setOutputFile(filePath);
            mMediaRecorder.setMaxDuration(MAX_LENGTH);
            mMediaRecorder.prepare();
            /* ④开始 */
            mMediaRecorder.start();
            // AudioRecord audioRecord.
            /* 获取开始时间* */
            startTime = System.currentTimeMillis();
            updateMicStatus();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            return filePath;
        }
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        try {
            if (mMediaRecorder == null)
                return ;
            mMediaRecorder.stop();
            initMediaRecorder();
        }catch (Exception e){
            initMediaRecorder();
        }
    }

    private void initMediaRecorder(){
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
    }

    private final Handler mHandler = new Handler();

    private Runnable mUpdateMicStatusTimer = this::updateMicStatus;

    /**
     * 更新话筒状态
     */
    private int BASE = 1;
    private int SPACE = 50;// 间隔取样时间

    public void setOnAudioStatusUpdateListener(OnAudioStatusUpdateListener audioStatusUpdateListener) {
        this.audioStatusUpdateListener = audioStatusUpdateListener;
    }

    private void updateMicStatus() {
        if (mMediaRecorder != null) {
            double ratio = (double) mMediaRecorder.getMaxAmplitude() / BASE;
            if (ratio > 1) {
                if (null != audioStatusUpdateListener) {
                    long recordTime = new Date().getTime() - startTime;
                    if(recordTime <= 60 * 1000){
                        audioStatusUpdateListener.onDecibelUpdate(20 * Math.log10(ratio));
                        audioStatusUpdateListener.recordTime(recordTime);
                    }else{
                        stopRecord();
                    }
                }
            }
            mHandler.postDelayed(mUpdateMicStatusTimer, SPACE);
        }
    }

    /**
     * 取消录制
     * **/
    public void cancelRecord(){
        stopRecord();
        File file = new File(currentFilePath);
        if(file.exists()){
            file.delete();
        }
    }

    public interface OnAudioStatusUpdateListener {
        void onDecibelUpdate(double db);
        void recordTime(long recordTime_ms);
    }
    public interface OnMediaPlayerCompletionListen{
        void onCompletion();
    }

    public void setMediaPlayerCompletionListen(OnMediaPlayerCompletionListen mediaPlayerCompletionListen) {
        this.mediaPlayerCompletionListen = mediaPlayerCompletionListen;
    }

    public void playerStart(String filePath) {
        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
            }
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.prepare();
            playerCompletion();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.start();
        isPlayer=true;
    }

    public void playerCompletion(){
        if(mMediaPlayer==null)return;
        mMediaPlayer.setOnCompletionListener(mp -> {
            isPlayer=false;
            if (mediaPlayerCompletionListen!=null) mediaPlayerCompletionListen.onCompletion();
        });
    }

    public boolean playerStop() {
        if (mMediaPlayer == null) return false;
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
        isPlayer=false;
        return true;
    }
    public String getFilePath(){

        return context.getExternalCacheDir().toString()+"/voice/"+ new Date().getTime() +".amr";
    }

    public String getCurrentFilePath() {
        return currentFilePath;
    }

    public boolean isPlayer() {
        return isPlayer;
    }
}
