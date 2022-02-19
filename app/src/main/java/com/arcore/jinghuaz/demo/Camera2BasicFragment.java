/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.arcore.jinghuaz.demo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Button;
import android.widget.CompoundButton;
//import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.app.Activity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v13.app.FragmentCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

/** Basic fragments for the Camera. */
public class Camera2BasicFragment extends Fragment

  implements FragmentCompat.OnRequestPermissionsResultCallback {

  /** Tag for the {@link Log}. */
  private static final String TAG = "TfLiteCameraDemo";

 // private static final String FRAGMENT_DIALOG = "dialog";

  private static final String HANDLE_THREAD_NAME = "CameraBackground";
   int requests=1;
   String model="quant";
  private static final int PERMISSIONS_REQUEST_CODE = 1;

  private final Object lock = new Object();
  private boolean runClassifier = false;
  private boolean checkedPermissions = false;
  private TextView textView;
  private NumberPicker np;
  private ImageClassifier classifier;
  private ListView deviceView;
  private ListView modelView;
  //File time_gpu=new File(getActivity().getExternalFilesDir(null), "time_gpu.txt");;
   boolean breakC=false;
  //List<Thread> inf_thread = new ArrayList<>();

  /** Max preview width that is guaranteed by Camera2 API */
  private static final int MAX_PREVIEW_WIDTH = 1920;

  /** Max preview height that is guaranteed by Camera2 API */
  private static final int MAX_PREVIEW_HEIGHT = 1080;



  // Model parameter constants.
  private String gpu;
  private String cpu;
  private String nnApi;
  private String mobilenetV1Quant;
  private String mobilenetV1Float;



//  /** ID of the current {@link CameraDevice}. */
//  private String cameraId;

  /** An {@link AutoFitTextureView} for camera preview. */
  private AutoFitTextureView textureView;


  /** The {@link android.util.Size} of camera preview. */
  private Size previewSize;



  private ArrayList<String> deviceStrings = new ArrayList<String>();
  private ArrayList<String> modelStrings = new ArrayList<String>();

  /** Current indices of device and model. */
  int currentDevice = -1;

  int currentModel = -1;

  int currentNumThreads = -1;

  /** An additional thread for running tasks that shouldn't block the UI. */
  private HandlerThread backgroundThread;

  /** A {@link Handler} for running tasks in the background. */
  private Handler backgroundHandler;



  /**
   * Shows a {@link Toast} on the UI thread for the classification results.
   *
   *
   */
  private void showToast(String s) {
    SpannableStringBuilder builder = new SpannableStringBuilder();
    SpannableString str1 = new SpannableString(s);
    builder.append(str1);
    showToast(builder);
  }

  private void showToast(SpannableStringBuilder builder) {
    final Activity activity = getActivity();
    if (activity != null) {
      activity.runOnUiThread(
          new Runnable() {
            @Override
            public void run() {
              textView.setText(builder, TextView.BufferType.SPANNABLE);
            }
          });
    }
  }



  public static Camera2BasicFragment newInstance() {

    return new Camera2BasicFragment();
  }

  /** Layout the preview and buttons. */
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_camera2_basic, container, false);


  }

// nill -> manually changed the options in the menu fr device and models
  private void updateActiveModel() {
    // Get UI information before delegating to background
    //Nil changed
    final int modelIndex =modelView.getCheckedItemPosition();
   // Gpu=1
    final int deviceIndex = deviceView.getCheckedItemPosition();
      int  numThreads=  np.getValue();

      switch (numThreads) {
          case 1:
              numThreads = 1;
              break;
          case 2:
              numThreads = 3;
              break;
          case 3:
              numThreads = 6;
              break;
      }
    //nill added


      int finalNumThreads = numThreads;
      backgroundHandler.post(
        () -> {
          if (modelIndex == currentModel
              && deviceIndex == currentDevice
              && finalNumThreads == currentNumThreads) {
            return;
          }
          currentModel = modelIndex;
          currentDevice =
                  deviceIndex;
          currentNumThreads = 1;

          // Disable classifier while updating
          if (classifier != null) {
            classifier.close();
            classifier = null;
          }

          // Lookup names of parameters.
          String model = modelStrings.get(modelIndex);
          String device = deviceStrings.get(deviceIndex);

          Log.i(TAG, "Changing model to " + model + ", device " + device   );

          // Try to load model.
          try {
            if (model.equals(mobilenetV1Quant)) {
              classifier = new ImageClassifierQuantizedMobileNet( getActivity());

            } else if (model.equals(mobilenetV1Float)) {
              classifier = new ImageClassifierFloatMobileNet(getActivity());
            } else {
              showToast("Failed to load model");
            }
          } catch (IOException e) {
            Log.d(TAG, "Failed to load", e);
            classifier = null;
          }

          // Customize the interpreter to the type of device we want to use.
          if (classifier == null) {
            return;
          }

            if( classifier != null)
            {
                if(modelIndex==0)
                    model="quant";// to store in a file
                else
                    model="float";
                classifier.setModel(model);// to store inf to a file
            }

            classifier.requests= finalNumThreads; // this is to request more more than one operations at a time
          classifier.setNumThreads(1); // this refers to the num of threads in the original app, to do one operation in parallell

          if (device.equals(cpu)) {
          } else if (device.equals(gpu)) {
            classifier.useGpu();

          } else if (device.equals(nnApi)) {
            classifier.useNNAPI();
          }

          classifier.classifyFrame2();


        });
  }

  /** Connect the buttons to their event handler. */
    /** Connect the buttons to their event handler. */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
  public void onViewCreated(final View view, Bundle savedInstanceState) {
    gpu = getString(R.string.gpu);
    cpu = getString(R.string.cpu);
    nnApi = getString(R.string.nnapi);
    mobilenetV1Quant = getString(R.string.mobilenetV1Quant);
    mobilenetV1Float = getString(R.string.mobilenetV1Float);

    // Get references to widgets. Nil
    textureView = (AutoFitTextureView) view.findViewById(R.id.texture);
    //textureView = (AutoFitTextureView) view.findViewById(R.id.sceneform_fragment);
    textView = (TextView) view.findViewById(R.id.text);
    deviceView = (ListView) view.findViewById(R.id.device);
    modelView = (ListView) view.findViewById(R.id.model);

    //nil

      //create button listener for object placer
      Button stop = (Button) view.findViewById(R.id.stop);
      stop.setOnClickListener(new View.OnClickListener() {
          public void onClick(View view) {

            System.out.println("stop"); // this is to stop active classifier
              if (classifier != null)// to stop while thread loop of classifier -> it first comes here, then goes above
              {
                  classifier.breakC = true;
                  try {
                      Thread.sleep(1000);
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
              }

              if ( classifier != null && classifier.breakC==true){ // to stop classifier after stopping the threads

                  classifier.close();
                  classifier = null;
              }
          }

      });


    // Build list of models
    modelStrings.add(mobilenetV1Quant);
    modelStrings.add(mobilenetV1Float);

    // Build list of devices
    int defaultModelIndex = 0;
    deviceStrings.add(cpu);
    deviceStrings.add(gpu);
    deviceStrings.add(nnApi);

    deviceView.setAdapter(
        new ArrayAdapter<String>(
            getContext(), R.layout.listview_row, R.id.listview_row_text, deviceStrings));
    deviceView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    deviceView.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

              // first stop the current thread and then update the model
              if (classifier != null)// to stop while thread loop of classifier -> it first comes here, then goes above
              {
                  classifier.breakC = true;
                  try {
                      Thread.sleep(6000);
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
              }
              if ( classifier != null && classifier.breakC==true){ // to stop classifier after stopping the threads
                  classifier.close();
                  classifier = null;
              }
              // first stop the current thread and then update the model


            updateActiveModel();
          }
        });
    deviceView.setItemChecked(0, true);



    modelView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    ArrayAdapter<String> modelAdapter =
        new ArrayAdapter<>(
            getContext(), R.layout.listview_row, R.id.listview_row_text, modelStrings);
    modelView.setAdapter(modelAdapter);
    modelView.setItemChecked(defaultModelIndex, true);
    modelView.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


          }
        });

    np = (NumberPicker) view.findViewById(R.id.np);// gets value for threads num/ num of requests
      String[] numbers = new String[]{"1", "3", "6"};
     np.setMinValue(1);
    np.setMaxValue(3);
    np.setDisplayedValues(numbers);
    np.setWrapSelectorWheel(true);
    np.setOnValueChangedListener(
        new NumberPicker.OnValueChangeListener() {
          @Override
          public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

              // does nothing
          }
        });

    // Start initial model.
  }

  /** Load the model and labels. */
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    startBackgroundThread();
  }

  @Override
  public void onResume() {
    super.onResume();
    startBackgroundThread();


  }

  @Override
  public void onPause() {
   // closeCamera();
    stopBackgroundThread();
    super.onPause();
  }

  @Override
  public void onDestroy() {
    if (classifier != null) {
      classifier.close();
    }
    super.onDestroy();
  }



  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }



  /** Starts a background thread and its {@link Handler}. */
  public void startBackgroundThread() {
    backgroundThread = new HandlerThread(HANDLE_THREAD_NAME);
    backgroundThread.start();
    backgroundHandler = new Handler(backgroundThread.getLooper());
    // Start the classification train & load an initial model.
    synchronized (lock) {
      runClassifier = true;
    }
  //  backgroundHandler.post(periodicClassify);
   // updateActiveModel();
  }

  /** Stops the background thread and its {@link Handler}. */
  private void stopBackgroundThread() {
    backgroundThread.quitSafely();
    try {
      backgroundThread.join();
      backgroundThread = null;
      backgroundHandler = null;
      synchronized (lock) {
        runClassifier = false;
      }
    } catch (InterruptedException e) {
      Log.e(TAG, "Interrupted when stopping background thread", e);
    }
  }

  //nill added multi tasking
  /** Takes photos and classify them periodically. */
  private Runnable periodicClassify =


            new Runnable() {
        @Override
        public void run() {

            synchronized (lock) {
                if (runClassifier) {

//                    try {
//                      //  classifyFrame();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
            }
            backgroundHandler.post(periodicClassify);

        }
    };

  /** Classifies a frame from the preview stream. */
  private void classifyFrame() throws InterruptedException {

    //Nil comment out this to not read from the camera frame, instead we read from a jpg file
    if (classifier == null || getActivity() == null) {// || cameraDevice == null) {

      return;
    }


    SpannableStringBuilder textToShow = new SpannableStringBuilder();

    //nil
    File root = Environment.getExternalStorageDirectory();

      Bitmap  bitmap = BitmapFactory.decodeFile( root+ "/mouse.jpg");
     // classifier.classifyFrame2(bitmap);
   // classifier.classifyFrame(bitmap, textToShow);

    bitmap.recycle();
    //nill commented/uncommented
    showToast(textToShow);

  }

}
