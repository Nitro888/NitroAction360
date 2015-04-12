package com.nitro888.nitroaction360.cardboard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ThumbnailUtils;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nitro888.nitroaction360.R;
import com.nitro888.nitroaction360.utils.FileExplorer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nitro888 on 15. 4. 10..
 */
public class NACardboardOverlayGUIView extends LinearLayout implements SensorEventListener {
    private NACardboardView                 mNACardboardView    = null;
    private final CardboardOverlayEyeView   mLeftView;
    private final CardboardOverlayEyeView   mRightView;
    private AlphaAnimation                  mTextFadeAnimation;
    private Vibrator                        mVibrator;

    public NACardboardOverlayGUIView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);

        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
        params.setMargins(0, 0, 0, 0);

        mLeftView = new CardboardOverlayEyeView(context, attrs, R.id.PlayerL,R.id.BrowserL,R.id.SettingL);
        mLeftView.setLayoutParams(params);
        addView(mLeftView);

        mRightView = new CardboardOverlayEyeView(context, attrs, R.id.PlayerR,R.id.BrowserR,R.id.SettingR);
        mRightView.setLayoutParams(params);
        addView(mRightView);

        // Set some reasonable defaults.
        setDepthOffset(0.016f);
        setColor(Color.rgb(150, 255, 180));
        setVisibility(View.VISIBLE);

        mTextFadeAnimation = new AlphaAnimation(1.0f, 0.0f);
        mTextFadeAnimation.setDuration(5000);

        // init Vibrator
        mVibrator   = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        // init sensor
        initSensor();
    }

    public void setCardboardView(NACardboardView naCardboardView) {
        mNACardboardView    = naCardboardView;
    }

    public void show3DToast(String message) {
        setText(message);
        setTextAlpha(1f);

        mTextFadeAnimation.setAnimationListener(new EndAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                setTextAlpha(0f);
            }
        });
        //startAnimation(mTextFadeAnimation);
    }

    private abstract class EndAnimationListener implements Animation.AnimationListener {
        @Override public void onAnimationRepeat(Animation animation) {}
        @Override public void onAnimationStart(Animation animation) {}
    }

    private void setDepthOffset(float offset) {
        mLeftView.setOffset(offset);
        mRightView.setOffset(-offset);
    }

    private void setText(String text) {
        mLeftView.setText(text);
        mRightView.setText(text);
    }

    private void setTextAlpha(float alpha) {
        mLeftView.setTextViewAlpha(alpha);
        mRightView.setTextViewAlpha(alpha);
    }

    private void setColor(int color) {
        mLeftView.setColor(color);
        mRightView.setColor(color);
    }

    /*
        for head tracking
    */
    private static final String TAG = NACardboardOverlayGUIView.class.getSimpleName();
    private Sensor              mSensor;
    private SensorManager       mSensorManager;
    private int                 mLastAccuracyXZ;
    private static final float  INVALID         = 10000;
    private float               mStartPitch     = INVALID;
    private float               mStartAzimuth   = INVALID;
    private static final float  DEFINITION      = (float) (Math.PI / 180 * 0.2f);

    public void initSensor() {
        mSensorManager  = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        mSensor         = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    private void activateSensor(boolean isActivate) {
        if (mSensor == null)
            return;

        if(isActivate) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
            Log.d(TAG, "Automatic scrolling enabled");
        }
        else {
            mSensorManager.unregisterListener(this);
            Log.d(TAG, "Automatic scrolling disabled");
            mStartPitch      = INVALID;
            mStartAzimuth    = INVALID;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        mLastAccuracyXZ = accuracy;
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] mat         = new float[9];
        float[] orientation = new float[3];

        if (mLastAccuracyXZ == SensorManager.SENSOR_STATUS_UNRELIABLE)
            return;

        SensorManager.getRotationMatrixFromVector(mat, event.values);
        SensorManager.remapCoordinateSystem(mat, SensorManager.AXIS_X, SensorManager.AXIS_Z, mat);
        SensorManager.getOrientation(mat, orientation);

        //http://developer.android.com/intl/ko/reference/android/hardware/SensorManager.html#getOrientation(float[], float[])
        float   azimuth = orientation[0];
        float   pitch   = orientation[1];

        if (mStartPitch == INVALID)    mStartPitch    = pitch;
        if (mStartAzimuth == INVALID)  mStartAzimuth  = azimuth;

        int result_pitch    = (int) ((mStartPitch - pitch) * -1 / DEFINITION);
        int result_azimuth  = (int) ((mStartAzimuth - azimuth) * -1 / DEFINITION);

        mLeftView.moveTo(result_azimuth,result_pitch);
        mRightView.moveTo(result_azimuth,result_pitch);

        update3DToast();
    }

    /*
    UI Control
     */
    public static  final int            GUI_PLAYER_CTRL     = 0;
    public static  final int            GUI_BROWSER_CTRL    = 1;
    public static  final int            GUI_SETTING_CTRL    = 2;
    public static  final int            ITEMS_PER_PAGE      = 6;
    private boolean                     isActivateGUI       = false;
    private int                         mBtnGUI_ID_R        = -1;
    private int                         mBtnGUI_ID_L        = -1;
    private int                         mActivateGUILayerID = GUI_PLAYER_CTRL;
    private String                      mFolder             = "";
    private int                         mFolderPage         = 0;
    private List<String>[]              mFolderFiles;
    private final List<String>          mFolderThumbnails   = new ArrayList<String>();

    public void onCardboardTrigger() {

        mVibrator.vibrate(50);

        if(!isActivateGUI) {
            isActivateGUI   = true;
            activateSensor(isActivateGUI);
            activateGUI(isActivateGUI);
        } else {
            if(mLeftView.getLookAtBtnID()!=-1) {
                onGUIButtonClick(mLeftView.getLookAtBtnID());
            }
        }
    }

    private void activateGUI(boolean isActivate) {
        if(mNACardboardView.isPlaying()&&isActivate)    preparePlayerController();
        else if(isActivate)                             prepareBrowserController();

        mLeftView.activateGUI(isActivate,mActivateGUILayerID);
        mRightView.activateGUI(isActivate,mActivateGUILayerID);
    }

    public void onGUIButtonClick(int btnID) {
        String btnIDAsString    = getResources().getResourceEntryName(btnID);
        String btnBtnIDString   = btnIDAsString.substring(0,btnIDAsString.length()-1);

        char last = btnIDAsString.charAt(btnIDAsString.length()-1);

        if(!((last=='L')||(last=='R')))
            return;

        if(last=='L')   {
            mBtnGUI_ID_L = btnID;
            last='R';
        }
        else {
            mBtnGUI_ID_R = btnID;
            last='L';
        }

        if((mBtnGUI_ID_L!=-1)&&(mBtnGUI_ID_R!=-1)) {
            mBtnGUI_ID_L = -1;
            mBtnGUI_ID_R = -1;
            return;
        }

        int BtnID   = getResources().getIdentifier( btnBtnIDString+last,
                getResources().getResourceTypeName(btnID),
                getResources().getResourcePackageName(btnID));

        final ImageButton btn = ((ImageButton)findViewById(BtnID));

        btn.performClick();
        btn.setPressed(true);
        btn.invalidate();

        btn.postDelayed(new Runnable() {
            public void run() {
                btn.setPressed(false);
                btn.invalidate();
            }
        }, 40);

        processBtn(btnID);
    }

    private void processBtn(int btnID) {
        switch (btnID) {
            case R.id.btn_leftL:
                browserPreviousPage();
                break;
            case R.id.btn_rightL:
                browserNextPage();
                break;
            case R.id.btn_file01L:
                browserSelectItem(0);
                break;
            case R.id.btn_file02L:
                browserSelectItem(1);
                break;
            case R.id.btn_file03L:
                browserSelectItem(2);
                break;
            case R.id.btn_file04L:
                browserSelectItem(3);
                break;
            case R.id.btn_file05L:
                browserSelectItem(4);
                break;
            case R.id.btn_file06L:
                browserSelectItem(5);
                break;
        }
    }

    private void update3DToast() {
        String  msg = "";

        if(mLeftView.getLookAtBtnID()!=-1) {
            switch (mLeftView.getLookAtBtnID()) {
                case R.id.btn_leftL:
                    msg = "Previous Page";
                    break;
                case R.id.btn_rightL:
                    msg = "Next Page";
                    break;
                case R.id.btn_file01L:
                    msg = mFolderFiles[1].get(mFolderPage*ITEMS_PER_PAGE+0);
                    break;
                case R.id.btn_file02L:
                    msg = mFolderFiles[1].get(mFolderPage*ITEMS_PER_PAGE+1);
                    break;
                case R.id.btn_file03L:
                    msg = mFolderFiles[1].get(mFolderPage*ITEMS_PER_PAGE+2);
                    break;
                case R.id.btn_file04L:
                    msg = mFolderFiles[1].get(mFolderPage*ITEMS_PER_PAGE+3);
                    break;
                case R.id.btn_file05L:
                    msg = mFolderFiles[1].get(mFolderPage*ITEMS_PER_PAGE+4);
                    break;
                case R.id.btn_file06L:
                    msg = mFolderFiles[1].get(mFolderPage*ITEMS_PER_PAGE+5);
                    break;
            }
        }

        show3DToast(msg);
    }

    /*
        UI Control  - player controller
    */
    private void preparePlayerController() {
        mNACardboardView.pauseMovie();
        mActivateGUILayerID = GUI_PLAYER_CTRL;
    }

    /*
        UI Control  - browser
    */
    private void prepareBrowserController(){
        browserSelectDir(mFolder);
        mActivateGUILayerID = GUI_BROWSER_CTRL;
    }

    private void browserSelectDir(String folder){
        if(folder.equals(""))
            mFolder = FileExplorer.getRoot();
        else
            mFolder = folder;

        mFolderFiles    = FileExplorer.getDir(mFolder);
        mFolderPage     = 0;
        mFolderThumbnails.clear();

        for(int i=0 ; i < mFolderFiles[0].size() ; i++)
            if(FileExplorer.selectItem(mFolderFiles[0].get(i))==0)
                mFolderThumbnails.add("");
            else if(FileExplorer.selectItem(mFolderFiles[0].get(i))==1)
                mFolderThumbnails.add(mFolderFiles[0].get(i));

        updateBrowserController();
    }

    private void browserNextPage(){
        mFolderPage++;
        int maxPage = (mFolderThumbnails.size()%ITEMS_PER_PAGE)>0?(mFolderThumbnails.size()/ITEMS_PER_PAGE)+1:mFolderThumbnails.size()/ITEMS_PER_PAGE;
        if(mFolderPage>=maxPage) mFolderPage=maxPage-1;
        updateBrowserController();
    }

    private void browserPreviousPage(){
        mFolderPage--;
        if(mFolderPage<0)       mFolderPage=0;
        updateBrowserController();
    }

    private void updateBrowserController() {
        int listStart   = mFolderPage*ITEMS_PER_PAGE;
        int listEnd     = mFolderThumbnails.size()>(listStart+ITEMS_PER_PAGE)?listStart+ITEMS_PER_PAGE:mFolderThumbnails.size();

        final BitmapDrawable[] thumbnails   = new BitmapDrawable[listEnd-listStart];
        for(int i=0 ; i < thumbnails.length ; i++ ) {
            if(mFolderThumbnails.get(i+listStart).equals(""))
                thumbnails[i]   = null;
            else
                thumbnails[i]   = new BitmapDrawable(getResources(),
                        ThumbnailUtils.createVideoThumbnail(
                                mFolderThumbnails.get(i + listStart), MediaStore.Video.Thumbnails.MINI_KIND));
        }

        mLeftView.updateBrowserController(thumbnails);
        mRightView.updateBrowserController(thumbnails);
    }

    private void browserSelectItem(int btnIndex){
        int type = FileExplorer.selectItem(mFolderFiles[0].get(mFolderPage*ITEMS_PER_PAGE+btnIndex));

        switch (type) {
            case 0:
                browserSelectDir(mFolderFiles[0].get(mFolderPage*ITEMS_PER_PAGE+btnIndex));
                break;
            case 1:
                isActivateGUI   = false;
                activateSensor(isActivateGUI);
                activateGUI(isActivateGUI);
                mNACardboardView.playMovie(mFolderFiles[0].get(mFolderPage*ITEMS_PER_PAGE+btnIndex));
                break;
        }
    }

    /**
     * A simple view group containing some horizontally centered text underneath a horizontally
     * centered image.
     *
     * <p>This is a helper class for CardboardOverlayView.
     */
    private class CardboardOverlayEyeView extends ViewGroup {
        private final ImageView     imageView;
        private final TextView      textView;
        private final NCGUIViews    guiViews;
        private float offset;

        public CardboardOverlayEyeView(Context context, AttributeSet attrs, int playerID, int browserID, int settingID) {
            super(context, attrs);

            // add GUI
            guiViews = new NCGUIViews(context, attrs, playerID, browserID, settingID);
            addView(guiViews);
            guiViews.setVisibility(View.INVISIBLE);

            // add imageView
            imageView = new ImageView(context, attrs);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setAdjustViewBounds(true);  // Preserve aspect ratio.
            addView(imageView);

            // add textView
            textView = new TextView(context, attrs);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.0f);
            textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
            textView.setGravity(Gravity.CENTER);
            textView.setShadowLayer(3.0f, 0.0f, 0.0f, Color.DKGRAY);
            addView(textView);
        }

        public void setColor(int color) {
            //imageView.setColorFilter(color);
            textView.setTextColor(color);
        }

        public void setText(String text) {
            textView.setText(text);
        }

        public void setTextViewAlpha(float alpha) {
            textView.setAlpha(alpha);
        }

        public void setOffset(float offset) {
            this.offset = offset;
        }

        public void moveTo(int x, int y) {
            guiViews.moveTo(x,y);
        };

        /*
        UI Control
        */
        public void activateGUI(boolean isActivate, int LayoutID) {
            if(isActivate) {
                guiViews.layoutReset(LayoutID);
                guiViews.setVisibility(View.VISIBLE);
            } else {
                guiViews.setVisibility(View.INVISIBLE);
            }
        }

        public int getLookAtBtnID() {
            return guiViews.getLookAtBtnID();
        }

        public void updateBrowserController(BitmapDrawable[] thumbnails) {
            guiViews.updateBrowserController(thumbnails);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            // Width and height of this ViewGroup.
            final int width     = right - left;
            final int height    = bottom - top;

            // The size of the image, given as a fraction of the dimension as a ViewGroup.
            // We multiply both width and heading with this number to compute the image's bounding
            // box. Inside the box, the image is the horizontally and vertically centered.
            final float imageSize = 0.12f;

            // The fraction of this ViewGroup's height by which we shift the image off the
            // ViewGroup's center. Positive values shift downwards, negative values shift upwards.
            final float verticalImageOffset = -0.07f;

            // Vertical position of the text, specified in fractions of this ViewGroup's height.
            final float verticalTextPos = 0.52f;

            // Layout ImageView
            float imageMargin   = (1.0f - imageSize) / 2.0f;
            float leftMargin    = (int) (width * (imageMargin + offset));
            float topMargin     = (int) (height * (imageMargin + verticalImageOffset));
            imageView.layout(
                    (int) leftMargin, (int) topMargin,
                    (int) (leftMargin + width * imageSize), (int) (topMargin + height * imageSize));

            // Layout TextView
            leftMargin          = offset * width;
            topMargin           = height * verticalTextPos;
            textView.layout(
                    (int) leftMargin, (int) topMargin,
                    (int) (leftMargin + width), (int) (topMargin + height * (1.0f - verticalTextPos)));

            // Layout GUI
            leftMargin          = offset * width;
            guiViews.layout(
                    (int) leftMargin, 0,
                    (int) (leftMargin + width), height);
        }
    }
}
