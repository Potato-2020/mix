package com.potato.asmmix.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.potato.asmmix.R;

public class GuideOneDialog extends Dialog {

    private Context mContext;
    private TextView bt_next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = getWindow();
        if (null != window) {
            //取出系统自带背景
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = window.getAttributes();
            //设置背景透明度 背景透明
            lp.alpha = 1f;//参数为0到1之间。0表示完全透明，1就是不透明。按需求调整参数
            window.setAttributes(lp);
            //设置dialog在界面中的属性
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }
        //必须再onCreate里面设置数据，否则不显示
        initData();
    }

    /**
     * 构造函数
     */
    public GuideOneDialog(Context context) {
//        super(context, R.style.BottomDialogStyle);
        super(context);
        mContext = context;
        // 设置触摸外面的取消为true
        setCanceledOnTouchOutside(false);
        setCancelable(false);//按返回键不消失
    }

    private void initData() {
//        tvTime.setText(getTimeStr(time));
    }

    public GuideOneListener listener;

    public void setGuideOneListener(GuideOneListener listener) {
        this.listener = listener;
    }

    public void destroy() {
        if (listener != null) listener = null;
    }

    public interface GuideOneListener {
        void guideOneListener();
    }

}
