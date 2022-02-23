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

package com.arcore.MixedAIAR;


import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.widget.TextView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.app.Dialog;
import android.app.DialogFragment;
import java.io.File;
import java.io.OutputStreamWriter;
import java.lang.Math;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.DownloadManager;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
//import android.os.Bundle;

//import com.example.android.tflitecamerademo.Camera2BasicFragment;
//import com.google.android.filament.gltfio.AssetLoader;
import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
//import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import java.lang.Math;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.lang.Math.abs;


import android.app.Dialog;
import android.app.DialogFragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
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
import android.os.Handler;
import android.os.HandlerThread;
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
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
//import android.support.v13.app.FragmentCompat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
//AI
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.nnapi.NnApiDelegate;
import java.io.IOException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.lang.Math;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.InputStream;
import java.util.stream.IntStream;
import java.util.concurrent.TimeUnit;
/**
 * Classifies images with Tensorflow Lite.
 */
public abstract class ImageClassifier {
  // Display preferences
  private static final float GOOD_PROB_THRESHOLD = 0.3f;
  private static final int SMALL_COLOR = 0xffddaa88;

  //nill
  boolean usegpu=false;
  boolean usecpu=false;
  boolean usenp=false;

  private float last_response_time=0;
  /** Tag for the {@link Log}. */
  private static final String TAG = "TfLiteCameraDemo";

  /** Number of results to show in the UI. */
  private static final int RESULTS_TO_SHOW = 3;
  int requests=1;
  boolean breakC=false;
  String model="quant";
  /** Dimensions of inputs. */
  private static final int DIM_BATCH_SIZE = 1;

  private static final int DIM_PIXEL_SIZE = 3;

  /** Preallocated buffers for storing image data in. */
  private int[] intValues = new int[getImageSizeX() * getImageSizeY()];
  File time;
  /** Options for configuring the Interpreter. */
  private final Interpreter.Options tfliteOptions = new Interpreter.Options();

  /** The loaded TensorFlow Lite model. */
  private MappedByteBuffer tfliteModel;

  /** An instance of the driver class to run model inference with Tensorflow Lite. */
  protected Interpreter tflite;

  /** Labels corresponding to the output of the vision model. */
  private List<String> labelList;

  /** A ByteBuffer to hold image data, to be feed into Tensorflow Lite as inputs. */
  protected ByteBuffer imgData = null;

  /** multi-stage low pass filter * */
  private float[][] filterLabelProbArray = null;

  private static final int FILTER_STAGES = 3;
  private static final float FILTER_FACTOR = 0.4f;


  private PriorityQueue<Map.Entry<String, Float>> sortedLabels =
          new PriorityQueue<>(
                  RESULTS_TO_SHOW,
                  new Comparator<Map.Entry<String, Float>>() {
                    @Override
                    public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                      return (o1.getValue()).compareTo(o2.getValue());
                    }
                  });

  /** holds a gpu delegate */
  GpuDelegate gpuDelegate = null;
  /** holds an nnapi delegate */
  NnApiDelegate nnapiDelegate = null;

  /** Initializes an {@code ImageClassifier}. */
  ImageClassifier(Activity activity) throws IOException {
    tfliteModel = loadModelFile(activity);
    tflite = new Interpreter(tfliteModel, tfliteOptions);
    labelList = loadLabelList(activity);
    imgData =
            ByteBuffer.allocateDirect(
                    DIM_BATCH_SIZE
                            * getImageSizeX()
                            * getImageSizeY()
                            * DIM_PIXEL_SIZE
                            * getNumBytesPerChannel());
    imgData.order(ByteOrder.nativeOrder());
    filterLabelProbArray = new float[FILTER_STAGES][getNumLabels()];
    Log.d(TAG, "Created a Tensorflow Lite Image Classifier.");
  }

  /** Classifies a frame from the preview stream. */
  //void classifyFrame(Bitmap bitmap, SpannableStringBuilder builder, File time_gpu) {

  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
  String path2=Environment.getExternalStorageDirectory()+"/Android/data/com.arcore.MixedAIAR/files";
  int count=0;





  void classifyFrame2()  {// no access to UI thread
    if (tflite == null) {
      Log.e(TAG, "Image classifier has not been initialized; Skipped.");

    }
    File root = Environment.getExternalStorageDirectory();
    String path= root+ "/chair.jpg";
    Bitmap  bitmap = BitmapFactory.decodeFile(path);
    convertBitmapToByteBuffer(bitmap);
    bitmap.recycle();
    List<Double> rTime= new ArrayList<>();
    List<String> curTime= new ArrayList<>();
    List<String> labels= new ArrayList<>();
    List<Thread> inf_thread = new ArrayList<>();
    final boolean[] onetWrite = {false};
    List<Integer> countL= new ArrayList<>();
    float desired= 16.6f;
    for(int i = 0; i< requests; i++) {//
      //if(breakC)
      // break;
      int finalI = i;
      inf_thread.add(i, new Thread() {//
        @Override
        public void run() {//

          long duration=0;
          try{
            //for(int i = 0; i< num; i++){
            while(true){
              if(breakC) {

                if(!onetWrite[0]) // to write data one time -> not by all the threads!!!
                { try (PrintWriter writer = new PrintWriter(new FileOutputStream(path2 + "/Response_t.csv", true))) {
                  onetWrite[0] =true;
                  int i = 0;
                  while (i < curTime.size() && i< rTime.size() && i< labels.size() && i< countL.size()) {

                    StringBuilder sbb = new StringBuilder();
                    sbb.append(curTime.get(i));
                    sbb.append(',');
                    sbb.append(labels.get(i));
                    sbb.append(',');
                    sbb.append(rTime.get(i));
                    sbb.append(',');
                    sbb.append(requests);
                    sbb.append(',');
                    sbb.append(model);
                    sbb.append(',');
                    sbb.append(countL.get(i));
                    sbb.append('\n');
                    writer.write(sbb.toString());
                    i++;
                  }

                } catch (FileNotFoundException e) {
                  System.out.println(e.getMessage());
                }


                }
                break;
              }


              if(duration<desired && duration != 0){
                // waite for elapse time (16.6 - last_response_time//otherwise, start the inference immidiately.
                long delay =(long) (desired - (float)last_response_time);
                Thread.sleep(delay);
              }

              curTime.add(dateFormat.format(new Date()));

              long startTime = SystemClock.uptimeMillis();
              runInference();
              long endTime = SystemClock.uptimeMillis();

              duration = endTime - startTime;
              countL.add(count);
              rTime.add((double) duration);
              labels.add(deviceUsed());
              count++;

              Log.d(TAG, "Thread ID" + Integer.toString(finalI)+ " "+ count + "#iteration  inference time: " + Long.toString(duration) );





            }
          }
          catch (Exception e)
          {
            System.out.println( "Thread Exception Caught ID " + finalI +": " + e.getMessage());
            int count1 = 0;
            int maxTries = 3;
            while(true) {
              if(breakC) {
                if(!onetWrite[0]) // to write data one time -> not by all the threads!!!
                {
                  onetWrite[0] =true;
                  try (PrintWriter writer = new PrintWriter(new FileOutputStream(path2 + "/Response_t.csv", true))) {

                  int i = 0;
                  while (i < curTime.size() && i< rTime.size() && i< labels.size() && i< countL.size()) {
                    StringBuilder sbb = new StringBuilder();
                    sbb.append(curTime.get(i));
                    sbb.append(',');
                    sbb.append(labels.get(i));
                    sbb.append(',');
                    sbb.append(rTime.get(i));
                    sbb.append(',');
                    sbb.append(requests);
                    sbb.append(',');
                    sbb.append(model);
                    sbb.append(',');
                    sbb.append(countL.get(i));
                    sbb.append('\n');
                    writer.write(sbb.toString());
                    i++;
                  }

                } catch (FileNotFoundException e2) {
                  System.out.println(e2.getMessage());
                }


                }

                break;
              }

              try {
                // long duration=0;

                if(duration<desired && duration != 0){
                  // waite for elapse time (16.6 - last_response_time//otherwise, start the inference immidiately.
                  long delay =(long) (desired - (float)last_response_time);
                  Thread.sleep(delay);
                }

                curTime.add(dateFormat.format(new Date()));
                long startTime = SystemClock.uptimeMillis();
                runInference();
                long endTime = SystemClock.uptimeMillis();
                duration = endTime - startTime;
                countL.add(count);
                rTime.add((double) duration);
                labels.add(deviceUsed());
                count++;

                Log.d(TAG, "Thread ID" + Integer.toString(finalI)+ " "+ count + "#iteration  inference time: " + Long.toString(duration) );

              } catch (Exception e2) {
                if (++count1 == maxTries) {
                  System.out.println( "Three times trial Thread Exception Caught ID " + finalI +": " + e2.getMessage());

                }
              }
            }
          }
        }////nil added all the lines with // after the code
      });//
      if(inf_thread.get(i)!=null)
        inf_thread.get(i).start();//
    }





    //inf_thread.clear();

  }



  void classifyFrame(Bitmap bitmap, SpannableStringBuilder builder)throws InterruptedException {

    if (tflite == null) {
      Log.e(TAG, "Image classifier has not been initialized; Skipped.");
      builder.append(new SpannableString("Uninitialized Classifier."));
    }
    convertBitmapToByteBuffer(bitmap);
    // Here's where the magic happens!!!


    float desired= 16.6f;
    if(last_response_time<desired && last_response_time != 0){
      // waite for elapse time (16.6 - last_response_time//otherwise, start the inference immidiately.
      long delay =(long) (desired - (float)last_response_time);

      Thread.sleep(delay);
    }
    long startTime = SystemClock.uptimeMillis();
    runInference();
    long endTime = SystemClock.uptimeMillis();
    Log.d(TAG, "Timecost to run model inference: " + Long.toString(endTime - startTime));

    last_response_time= endTime - startTime;
    // Smooth the results across frames.
    applyFilter();

    // Print the results.
    String label_accu="";
    //printTopKLabels(builder);
    long duration = endTime - startTime;
    SpannableString span = new SpannableString(duration + " ms");
    span.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, span.length(), 0);
    builder.append(span);


    //nill
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

    String item2 = dateFormat.format(new Date()) + " "+label_accu+ " time " + duration + " ms" + " requests " + requests + " model " + model;


//nil


    String path=Environment.getExternalStorageDirectory()+"/Android/data/com.arcore.MixedAIAR/files";



    try (PrintWriter writer = new PrintWriter(new FileOutputStream(path+  "/Response_t.csv", true))) {

      StringBuilder sbb = new StringBuilder();
      sbb.append(dateFormat.format(new Date())); sbb.append(','); sbb.append(label_accu);
      sbb.append(',');sbb.append(duration);sbb.append(',');
      sbb.append(requests);sbb.append(',');  sbb.append(model);

      // String item2 = dateFormat.format(new Date()) + " "+label_accu+ " time " + duration + " ms" + " requests " + requests + " model " + model;

      sbb.append('\n');
      writer.write(sbb.toString());
      System.out.println("done!");
    } catch (FileNotFoundException e) {
      System.out.println(e.getMessage());
    }

  }

  void applyFilter() {
    int numLabels = getNumLabels();

    // Low pass filter `labelProbArray` into the first stage of the filter.
    for (int j = 0; j < numLabels; ++j) {
      filterLabelProbArray[0][j] +=
              FILTER_FACTOR * (getProbability(j) - filterLabelProbArray[0][j]);
    }
    // Low pass filter each stage into the next.
    for (int i = 1; i < FILTER_STAGES; ++i) {
      for (int j = 0; j < numLabels; ++j) {
        filterLabelProbArray[i][j] +=
                FILTER_FACTOR * (filterLabelProbArray[i - 1][j] - filterLabelProbArray[i][j]);
      }
    }

    // Copy the last stage filter output back to `labelProbArray`.
    for (int j = 0; j < numLabels; ++j) {
      setProbability(j, filterLabelProbArray[FILTER_STAGES - 1][j]);
    }
  }

  private void recreateInterpreter() {
    if (tflite != null) {
      tflite.close();
      tflite = new Interpreter(tfliteModel, tfliteOptions);
    }
  }

  public void useGpu() {

    usegpu=true;


    if (gpuDelegate == null) {
      GpuDelegate.Options options = new GpuDelegate.Options();
      options.setQuantizedModelsAllowed(true);

      gpuDelegate = new GpuDelegate(options);
      tfliteOptions.addDelegate(gpuDelegate);
      recreateInterpreter();
    }
  }

  public void useCPU() {

    usecpu=true;
    recreateInterpreter();
  }

  public void useNNAPI() {
    usenp=true;
    nnapiDelegate = new NnApiDelegate();
    tfliteOptions.addDelegate(nnapiDelegate);
    recreateInterpreter();
  }

  public void setNumThreads(int numThreads) { // this is to ask some threads in paralled work on one frame, while we need a scenario that multiple threads work on diff images and separately send a  reuest
    tfliteOptions.setNumThreads(numThreads);
    recreateInterpreter();
  }
  //nill added for num of requests at a time per frame
  public void setNumRequests(int numRequests) {
    requests=numRequests;
  }


  public void setModel(String inpmodel) {
    model=inpmodel;
  }
  /** Closes tflite to release resources. */
  public void close() {
    tflite.close();
    tflite = null;
    if (gpuDelegate != null) {
      gpuDelegate.close();
      gpuDelegate = null;
    }
    if (nnapiDelegate != null) {
      nnapiDelegate.close();
      nnapiDelegate = null;
    }
    tfliteModel = null;
  }

  /** Reads label list from Assets. */
  private List<String> loadLabelList(Activity activity) throws IOException {
    List<String> labelList = new ArrayList<String>();
    BufferedReader reader =
            new BufferedReader(new InputStreamReader(activity.getAssets().open(getLabelPath())));
    String line;
    while ((line = reader.readLine()) != null) {
      labelList.add(line);
    }
    reader.close();
    return labelList;
  }

  /** Memory-map the model file in Assets. */
  // private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
  private MappedByteBuffer loadModelFile(Activity activity) throws IOException {



    AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(getModelPath());

    FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
    FileChannel fileChannel = inputStream.getChannel();
    long startOffset = fileDescriptor.getStartOffset();
    long declaredLength = fileDescriptor.getDeclaredLength();
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
  }

  /** Writes Image data into a {@code ByteBuffer}. */
  private void convertBitmapToByteBuffer(Bitmap bitmap) {
    if (imgData == null) {
      return;
    }
    imgData.rewind();
    bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
    // Convert the image to floating point.
    int pixel = 0;
    long startTime = SystemClock.uptimeMillis();
    for (int i = 0; i < getImageSizeX(); ++i) {
      for (int j = 0; j < getImageSizeY(); ++j) {
        final int val = intValues[pixel++];
        addPixelValue(val);
      }
    }
    long endTime = SystemClock.uptimeMillis();
    Log.d(TAG, "Timecost to put values into ByteBuffer: " + Long.toString(endTime - startTime));
  }

  /** Prints top-K labels, to be shown in UI as the results.
   * nill */
  //private void printTopKLabels(SpannableStringBuilder builder) {

  private String deviceUsed(){

    String device= "CPU";
    // String device= "GPU";
    if (usegpu==true)
      device="GPU";

    else if (usecpu==true)
      device="CPU";

    else if (usenp==true)
      device="NNAPI";


    return device;
  }

  private String printTopKLabels() {


    String label_prob="";

    for (int i = 0; i < getNumLabels(); ++i) {
      sortedLabels.add(
              new AbstractMap.SimpleEntry<>(labelList.get(i), getNormalizedProbability(i)));
      if (sortedLabels.size() > RESULTS_TO_SHOW) {
        sortedLabels.poll();
      }
    }

    String device= "CPU";
    // String device= "GPU";
    if (usegpu==true)
      device="GPU";

    else if (usecpu==true)
      device="CPU";

    else if (usenp==true)
      device="NNAPI";

    final int size = sortedLabels.size();
    for (int i = 0; i < size; i++) {
      Map.Entry<String, Float> label = sortedLabels.poll();
      //SpannableString span = new SpannableString(String.format("%s: %4.2f\n", label.getKey(), label.getValue()));
      //int color;
      // Make it white when probability larger than threshold.
      if (label.getValue() > GOOD_PROB_THRESHOLD) {
       // color = android.graphics.Color.WHITE;
        //nill
        String accuracy= String.valueOf((float)(Math.round((float)( label.getValue() * 100))) / 100);
        label_prob = label.getKey() +","+ device +","+accuracy ;
      }

//      else {
//        color = SMALL_COLOR;
//      }
//      // Make first item bigger.
//      if (i == size - 1) {
//        float sizeScale = (i == size - 1) ? 1.25f : 0.8f;
//        span.setSpan(new RelativeSizeSpan(sizeScale), 0, span.length(), 0);
//      }
//      span.setSpan(new ForegroundColorSpan(color), 0, span.length(), 0);
      //builder.insert(0, span);
    }

    return label_prob;

  }

  /**
   * Get the name of the model file stored in Assets.
   *
   * @return
   */
  protected abstract String getModelPath();

  /**
   * Get the name of the label file stored in Assets.
   *
   * @return
   */
  protected abstract String getLabelPath();

  /**
   * Get the image size along the x axis.
   *
   * @return
   */
  protected abstract int getImageSizeX();

  /**
   * Get the image size along the y axis.
   *
   * @return
   */
  protected abstract int getImageSizeY();

  /**
   * Get the number of bytes that is used to store a single color channel value.
   *
   * @return
   */
  protected abstract int getNumBytesPerChannel();

  /**
   * Add pixelValue to byteBuffer.
   *
   * @param pixelValue
   */
  protected abstract void addPixelValue(int pixelValue);

  /**
   * Read the probability value for the specified label This is either the original value as it was
   * read from the net's output or the updated value after the filter was applied.
   *
   * @param labelIndex
   * @return
   */
  protected abstract float getProbability(int labelIndex);

  /**
   * Set the probability value for the specified label.
   *
   * @param labelIndex
   * @param value
   */
  protected abstract void setProbability(int labelIndex, Number value);

  /**
   * Get the normalized probability value for the specified label. This is the final value as it
   * will be shown to the user.
   *
   * @return
   */
  protected abstract float getNormalizedProbability(int labelIndex);

  /**
   * Run inference using the prepared input in {@link #imgData}. Afterwards, the result will be
   * provided by getProbability().
   *
   * <p>This additional method is necessary, because we don't have a common base for different
   * primitive data types.
   */
  protected abstract void runInference();

  /**
   * Get the total number of labels.
   *
   * @return
   */
  protected int getNumLabels() {
    return labelList.size();
  }
}
