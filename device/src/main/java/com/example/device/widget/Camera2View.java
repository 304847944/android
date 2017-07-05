package com.example.device.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.example.device.util.BitmapUtil;
import com.example.device.util.Utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2View extends TextureView {
	private static final String TAG = "Camera2View";
	private Context mContext;
	private Handler mHandler;
	private HandlerThread mThreadHandler;
	private CaptureRequest.Builder mPreviewBuilder;
	private CameraCaptureSession mCameraSession;
	private ImageReader mImageReader;
	private Size mPreViewSize;
	private int mCameraType = CameraCharacteristics.LENS_FACING_FRONT;
	private int mTakeType = 0;

	public Camera2View(Context context) {
		this(context, null);
	}

	public Camera2View(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mThreadHandler = new HandlerThread("camera2");
		mThreadHandler.start();
		mHandler = new Handler(mThreadHandler.getLooper());
	}
	
	public void open(int camera_type) {
		mCameraType = camera_type;
		setSurfaceTextureListener(mSurfacetextlistener);
	}
	
	private String mPhotoPath;
	public String getPhotoPath() {
		return mPhotoPath;
	}

	public void takePicture() {
		Log.d(TAG, "正在拍照");
		mTakeType = 0;
		try {
			CaptureRequest.Builder builder = mCameraSession.getDevice()
					.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			builder.addTarget(mImageReader.getSurface());
			builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
			builder.set(CaptureRequest.CONTROL_AF_TRIGGER,
					CameraMetadata.CONTROL_AF_TRIGGER_START);
			builder.set(CaptureRequest.JPEG_ORIENTATION, 90);
            mCameraSession.capture(builder.build(), null, mHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	private ArrayList<String> mShootingArray;
	public ArrayList<String> getShootingList() {
		Log.d(TAG, "mShootingArray.size()="+mShootingArray.size());
		return mShootingArray;
	}
	
	public void startShooting(int duration) {
		Log.d(TAG, "正在连拍");
		mTakeType = 1;
		mShootingArray = new ArrayList<String>();
		try {
			CaptureRequest.Builder builder = mCameraSession.getDevice()
					.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			builder.addTarget(mImageReader.getSurface());
			builder.set(CaptureRequest.CONTROL_AF_MODE,
					CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
			builder.set(CaptureRequest.CONTROL_AF_TRIGGER,
					CameraMetadata.CONTROL_AF_TRIGGER_START);
			builder.set(CaptureRequest.JPEG_ORIENTATION, 90);
            mCameraSession.setRepeatingRequest(builder.build(), null, mHandler);
            //duration小等于0时，表示持续连拍，此时外部要调用stopShooting方法来结束连拍
            if (duration > 0) {
                mHandler.postDelayed(mStop, duration);
            }
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	public void stopShooting() {
        try {
			mCameraSession.stopRepeating();
			mCameraSession.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

    private Runnable mStop = new Runnable() {
		@Override
		public void run() {
			stopShooting();
		}
    };

    //TextureView准备就绪后，开启相机
	private SurfaceTextureListener mSurfacetextlistener = new SurfaceTextureListener() {
		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
			CameraManager cm = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            String cameraid = mCameraType + "";
			try {
				// 获取可用相机设备列表
				CameraCharacteristics cc = cm.getCameraCharacteristics(cameraid);
				// 检查相机硬件的支持级别
				// CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL表示完全支持
				// CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED表示有限支持
				// CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY表示遗留的
				//int level = cc.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizeByArea());
                mPreViewSize = map.getOutputSizes(SurfaceTexture.class)[0];
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, 10);
                mImageReader.setOnImageAvailableListener(onImageAvaiableListener, mHandler);
				cm.openCamera(cameraid, mDeviceStateCallback, mHandler);
			} catch (CameraAccessException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
			return false;
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		}
	};

	//相机准备就绪后，开启捕捉影像的会话
	private CameraDevice.StateCallback mDeviceStateCallback = new CameraDevice.StateCallback() {
		@Override
		public void onOpened(CameraDevice camera) {
			SurfaceTexture texture = getSurfaceTexture();
			texture.setDefaultBufferSize(mPreViewSize.getWidth(), mPreViewSize.getHeight());
			Surface surface = new Surface(texture);
			try {
				mPreviewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
				mPreviewBuilder.addTarget(surface);
				camera.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
						mSessionStateCallback, mHandler);
			} catch (CameraAccessException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onDisconnected(CameraDevice camera) {
		}

		@Override
		public void onError(CameraDevice camera, int error) {
		}
	};

	//影像配置就绪后，将预览画面呈现到手机屏幕上
	private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
		@Override
		public void onConfigured(CameraCaptureSession session) {
			try {
				Log.d(TAG, "onConfigured");
				mCameraSession = session;
				mCameraSession.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler);
			} catch (CameraAccessException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onConfigureFailed(CameraCaptureSession session) {
		}
	};

	//一旦有图像数据生成，立刻触发onImageAvailable事件
    private ImageReader.OnImageAvailableListener onImageAvaiableListener = new OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
			Log.d(TAG, "onImageAvailable");
            mHandler.post(new ImageSaver(imageReader.acquireNextImage()));
        }
    };

    private class ImageSaver implements Runnable {
        private Image mImage;
        public ImageSaver(Image reader) {
        	mImage = reader;
        }

        @Override
        public void run() {
			String path = String.format("%s%s.jpg", BitmapUtil.getCachePath(mContext), Utils.getNowDateTime());
            Log.d(TAG, "正在保存图片 path="+path);
            BitmapUtil.saveBitmap(path, mImage.getPlanes()[0].getBuffer(), 4, "JPEG", 80);
            if (mImage != null) {
            	mImage.close();
            }
            if (mTakeType == 0) {
            	mPhotoPath = path;
            } else {
            	mShootingArray.add(path);
            }
            Log.d(TAG, "完成保存图片 path="+path);
        }
    }

    private class CompareSizeByArea implements java.util.Comparator<Size> {
		@Override
		public int compare(Size lhs, Size rhs) {
			return Long.signum((long) lhs.getWidth() * lhs.getHeight()
					- (long) rhs.getWidth() * rhs.getHeight());
		}
	}

}
