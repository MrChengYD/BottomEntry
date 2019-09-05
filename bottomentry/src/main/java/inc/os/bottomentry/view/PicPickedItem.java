package inc.os.bottomentry.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;

import inc.os.bottomentry.R;


public class PicPickedItem extends RelativeLayout {
    private Context context;
    public static final String PICK = "pick";
    public static final String CANCEL = "cancel";
    private View item_view;
    private ImageView picked_pic;
    private ImageView picked_icon;
    private String pic_path;
    private Boolean checked = false;
    private int screen_width;
    private boolean pickable = true;
    private Handler handler = new Handler();
    private OnPicPickedItemClick onPicPickedItemClick;

    public PicPickedItem(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screen_width = dm.widthPixels;
        initComponent();
        setListener();
    }
    private void initComponent(){
        item_view = LayoutInflater.from(context).inflate(R.layout.layout_pic_picked, this);
        picked_pic = item_view.findViewById(R.id.picked_pic);
        picked_icon = item_view.findViewById(R.id.picked_icon);
    }
    public void show(String pic_path){


        this.pic_path = pic_path;
        handler.post(showRunnable);
    }
    Runnable showRunnable = this::showR;
    private void showR(){
        if( null == this.pic_path || "".equals(this.pic_path)){
            return;
        }

        RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override((screen_width ) / 3 , (screen_width ) / 3 ).centerCrop().skipMemoryCache(true);
        if(this.pic_path.startsWith("http")){
            //加载网络图片
            Glide.with(context).load(this.pic_path).apply(requestOptions).into(picked_pic);
        }else{
            //本地图片
            File picFile = new File(this.pic_path);
            Glide.with(context).load(picFile).apply(requestOptions).into(picked_pic);
        }
    }
    private void setListener(){
        item_view.setOnClickListener(v -> {
            if(checked){
                picked_icon.setVisibility(GONE);

                if(null != onPicPickedItemClick){
                    onPicPickedItemClick.onItem( CANCEL , this.pic_path);
                }
                checked = false;
            }else if(pickable){
                picked_icon.setVisibility(VISIBLE);
                if(null != onPicPickedItemClick){
                    onPicPickedItemClick.onItem( PICK , this.pic_path);
                }
                checked = true;
            }

        });
    }

    // 设置是否可以选中
    public void setPickable(boolean pickable){
        this.pickable = pickable;
    }
    //清空选中
    public void clearPickState(){
        this.pickable = true;
        this.checked = false;
        picked_icon.setVisibility(GONE);
    }
    public interface OnPicPickedItemClick{
        void onItem(String optionType , String pic_path);
    }
    public void setOnPicPickedItemClick(OnPicPickedItemClick onPicPickedItemClick){
        this.onPicPickedItemClick = onPicPickedItemClick;
    }
}
