package cn.edu.hrbeu.heu_newsclient;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

@SuppressWarnings("deprecation")
public class MainActivity extends ActivityGroup implements View.OnClickListener {
    private final static Class<?>[] sActivityClasses = {
            NewsTop.class, National.class, International.class, Society.class
    };

    private final static int[] sResIds = {
            R.id.button1, R.id.button2, R.id.button3, R.id.button4
    };

    private final static String[] sActivityIds = {
            "Activity1", "Activity2", "Activity3", "Activity4"
    };

    private RelativeLayout mViewContainer;

    private Button[] mBtns = new Button[sResIds.length];
    
    private int mCurId = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();
        
        ImageView iv = (ImageView) findViewById(R.id.account_center);
        iv.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				Intent in = new Intent();
				in.setClass(MainActivity.this, AccountCenter.class);
				startActivity(in);
			}
        	
        });
    }

    private void setupViews() {
        mViewContainer = (RelativeLayout) findViewById(R.id.container);
        final Button[] btns = mBtns;
        for(int i=0; i< btns.length; i++) {
            btns[i] = (Button) findViewById(sResIds[i]);
            btns[i].setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if(mCurId == id) {
            return ;
        }
        mCurId = id;
        processViews(id);
    }

    private void processViews(int rid) {
        mViewContainer.removeAllViews();
        final int index = getButtonIndex(rid);
        final View tempView = getLocalActivityManager().startActivity(sActivityIds[index],
                new Intent(this, sActivityClasses[index]).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                .getDecorView();
        mViewContainer.addView(tempView);
    }

    private int getButtonIndex(int rid) {
        final int length = sResIds.length;
        for (int i = 0; i < length; i++) {
            if (rid == sResIds[i]) {
                return i;
            }
        }
        return 0;
    }
}
