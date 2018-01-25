package com.rnopentok;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.opentok.android.OpentokError;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;

public class RNOpenTokPublisherView extends RNOpenTokView implements PublisherKit.PublisherListener {
    private Publisher mPublisher;
	
    private Boolean mAudioEnabled;
    private Boolean mVideoEnabled;
	
	//default value if user have not assign
	private Publisher.CameraCaptureFrameRate cameraFrameRatePublisher.CameraCaptureFrameRate.FPS_15;
    private Publisher.CameraCaptureResolution cameraCaptureResulation= Publisher.CameraCaptureResolution.MEDIUM;
	
	
	 public void setCameraCaptureResulation(int resulationMode) {
        switch (resulationMode) {
            case 0:
                cameraCaptureResulation = Publisher.CameraCaptureResolution.LOW;
                break;
            case 1:
                cameraCaptureResulation = Publisher.CameraCaptureResolution.MEDIUM;
                break;
            case 2:
                cameraCaptureResulation = Publisher.CameraCaptureResolution.HIGH;
                break;
        }
    }

    public void setCameraFrameRate(int frameRate) {
        switch (frameRate) {
            case 1:
                cameraFrameRate = Publisher.CameraCaptureFrameRate.FPS_1;
                break;
            case 7:
                cameraFrameRate = Publisher.CameraCaptureFrameRate.FPS_7;
                break;
            case 15:
                cameraFrameRate = Publisher.CameraCaptureFrameRate.FPS_15;
                break;
            case 30:
                cameraFrameRate = Publisher.CameraCaptureFrameRate.FPS_30;
                break;
        }
    }

    public RNOpenTokPublisherView(ThemedReactContext context) {
        super(context);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        RNOpenTokSessionManager.getSessionManager().setPublisherListener(mSessionId, this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        RNOpenTokSessionManager.getSessionManager().removePublisherListener(mSessionId);
    }

    public void setAudio(Boolean enabled) {
        if (mPublisher != null) {
            mPublisher.setPublishAudio(enabled);
        }

        mAudioEnabled = enabled;
    }

    public void setVideo(Boolean enabled) {
        if (mPublisher != null) {
            mPublisher.setPublishVideo(enabled);
        }

        mVideoEnabled = enabled;
    }

    public void cycleCamera() {
        if (mPublisher != null) {
            mPublisher.cycleCamera();
        }
    }

    private void startPublishing() {
       
		Publisher.Builder builder = new Publisher.Builder(getContext());
        builder.resolution(cameraCaptureResulation);
        builder.frameRate(cameraFrameRate);
        mPublisher = builder.build();
		
        mPublisher.setPublisherListener(this);

        mPublisher.setPublishAudio(mAudioEnabled);
        mPublisher.setPublishVideo(mVideoEnabled);

        Session session = RNOpenTokSessionManager.getSessionManager().getSession(mSessionId);
        session.publish(mPublisher);

        attachPublisherView();
    }

    private void attachPublisherView() {
        addView(mPublisher.getView(), new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        requestLayout();
    }

    private void cleanUpPublisher() {
        removeView(mPublisher.getView());
        mPublisher.destroy();
        mPublisher = null;
    }

    public void onConnected(Session session) {
        startPublishing();
    }

    /** Publisher listener **/

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        sendEvent(Events.EVENT_PUBLISH_START, Arguments.createMap());
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        sendEvent(Events.EVENT_PUBLISH_STOP, Arguments.createMap());
        cleanUpPublisher();
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        WritableMap payload = Arguments.createMap();
        payload.putString("connectionId", opentokError.toString());

        sendEvent(Events.EVENT_PUBLISH_ERROR, payload);
        cleanUpPublisher();
    }
}
