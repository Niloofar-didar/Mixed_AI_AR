package com.arcore.jinghuaz.demo;

import android.net.Uri;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;

import java.util.*;
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

import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

import java.lang.Math;

import static java.lang.Math.abs;


/*TODO: constant update distance to file
  see if we can update AR capabilities -- find out pointer operation (why will it not draw past 2 meters or whateverz
  update menu popups for simplified files -- thumbnails have to be 64x64
  compare anchor and hit position in place object


*/
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private ArFragment fragment;
    private PointerDrawable pointer = new PointerDrawable();
    private boolean isTracking;
    private boolean isHitting;
    private baseRenderable renderArray[] = new baseRenderable[20];
    private int objectCount = 0;
    private String[] assetList = null;
    private String currentModel = null;
    private boolean referenceObjectSwitchCheck = false;
    private int nextID = 0;
    ArFragment arFragment = (ArFragment)
            getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);


    private DecimalFormat posFormat = new DecimalFormat("###.##");
    private final int SEEKBAR_INCREMENT = 10;
    File dateFile;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;

    private ArrayList<Float> timeLog = new ArrayList<>();
    private float timeInSec = 0;

    private ArrayList<ArrayList<Float> > current = new ArrayList<ArrayList<Float> >();
/*
    private ArrayList<ArrayList<Float> > predicted05 = new ArrayList<ArrayList<Float> >();
    private ArrayList<ArrayList<Float> > predicted10 = new ArrayList<ArrayList<Float> >();
    private ArrayList<ArrayList<Float> > predicted15 = new ArrayList<ArrayList<Float> >();
    private ArrayList<ArrayList<Float> > predicted20 = new ArrayList<ArrayList<Float> >();
    private ArrayList<ArrayList<Float> > predicted25 = new ArrayList<ArrayList<Float> >();

    private ArrayList<ArrayList<Float> > margin05 = new ArrayList<ArrayList<Float> >();
    private ArrayList<ArrayList<Float> > margin10 = new ArrayList<ArrayList<Float> >();
    private ArrayList<ArrayList<Float> > margin15 = new ArrayList<ArrayList<Float> >();
    private ArrayList<ArrayList<Float> > margin20 = new ArrayList<ArrayList<Float> >();
    private ArrayList<ArrayList<Float> > margin25 = new ArrayList<ArrayList<Float> >();


    private ArrayList<ArrayList<Float> > error05 = new ArrayList<ArrayList<Float> >();
    private ArrayList<ArrayList<Float> > error10 = new ArrayList<ArrayList<Float> >();
    private ArrayList<ArrayList<Float> > error15 = new ArrayList<ArrayList<Float> >();
    private ArrayList<ArrayList<Float> > error20 = new ArrayList<ArrayList<Float> >();
    private ArrayList<ArrayList<Float> > error25 = new ArrayList<ArrayList<Float> >();

    private ArrayList<ArrayList<Float> > bool05 = new ArrayList<>();
    private ArrayList<ArrayList<Float> > bool10 = new ArrayList<>();
    private ArrayList<ArrayList<Float> > bool15 = new ArrayList<>();
    private ArrayList<ArrayList<Float> > bool20 = new ArrayList<>();
    private ArrayList<ArrayList<Float> > bool25 = new ArrayList<>();*/

    private HashMap<Integer, ArrayList<ArrayList<Float>>> prmap=new HashMap<Integer, ArrayList<ArrayList<Float>>>();
    private HashMap<Integer, ArrayList<ArrayList<Float>>> marginmap=new HashMap<Integer, ArrayList<ArrayList<Float>>>();
    private HashMap<Integer, ArrayList<ArrayList<Float>>> errormap=new HashMap<Integer, ArrayList<ArrayList<Float>>>();
    private HashMap<Integer, ArrayList<ArrayList<Float>>> booleanmap=new HashMap<Integer, ArrayList<ArrayList<Float>>>();


    private float objX, objZ;

    private float alpha = 0.7f;


    //recieves messages from modelrequest manager
    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message inputMessage)
        {
            Log.d("ModelRequest", "Recieved the message!");
            for(int i = 0; i < objectCount; i++)
            {
                if(renderArray[i].getID() == ((ModelRequest) inputMessage.obj).getID())
                {
                    renderArray[i].redraw();
                }
            }
        }
    };

    public Handler getHandler()
    {
        return handler;
    }

    //used for abstraction of reference renderable and decimated renderable
    public abstract class baseRenderable
    {
        public TransformableNode baseAnchor;
        public String fileName;
        private int ID;



        public void setAnchor(TransformableNode base)
        {
            baseAnchor = base;
        }

        public String getFileName() { return fileName; }

        public int getID() { return ID; }

        public void setID(int mID) { ID = mID; }

        public abstract void redraw();

        public abstract void decimatedModelRequest(float percentageReduction);

        public abstract void print(AdapterView<?> parent, int pos);

        public void detach()
        {
            try
            {
                baseAnchor.getScene().onRemoveChild(baseAnchor.getParent());
                baseAnchor.setRenderable(null);
                baseAnchor.setParent(null);
            }
            catch(Exception e)
            {
                Log.w("Detach", e.getMessage());
            }

        }
    }

    //reference renderables, cannot be changed when decimation percentage is selected
    private class refRenderable extends baseRenderable
    {
        refRenderable(String filename)
        {
            this.fileName = filename;
            setID(nextID);
            nextID++;
        }

        public void decimatedModelRequest(float percentageReduction) { return; }

        public void redraw()
        {
            return;
        }

        public void print(AdapterView<?> parent, int pos)
        {
            Frame frame = fragment.getArSceneView().getArFrame();
            String item = "--REFERENCE OBJECT-- --" + fileName + "--\n" +
                    "User Score: " + parent.getItemAtPosition(pos).toString() + "\n" +
                    "Time: " + new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss", java.util.Locale.getDefault()).format(new Date()) + "\n";

            item += "Camera position: " +
                    "[" +  posFormat.format(frame.getCamera().getPose().tx()) +
                    "], [" + posFormat.format(frame.getCamera().getPose().ty()) +
                    "], [" + posFormat.format(frame.getCamera().getPose().tz()) +
                    "]\n";

            item += ("Object position: ["
                    + posFormat.format(baseAnchor.getWorldPosition().x) +
                    "], [" + posFormat.format(baseAnchor.getWorldPosition().y) +
                    "], [" + posFormat.format(baseAnchor.getWorldPosition().z) +
                    "]\n");

            item += ("Distance from camera: "
                    + Math.sqrt((Math.pow((baseAnchor.getWorldPosition().x - frame.getCamera().getPose().tx()), 2)
                    + Math.pow((baseAnchor.getWorldPosition().y - frame.getCamera().getPose().ty()), 2)
                    + Math.pow((baseAnchor.getWorldPosition().z - frame.getCamera().getPose().tz()), 2)))
                    + " m\n");

            item += "\n\n";


            try {
                FileOutputStream os = new FileOutputStream(dateFile, false);
                os.write(item.getBytes());
                os.close();
            } catch (IOException e) {
                Log.e("StatWriting", e.getMessage());
            }
        }
    }


    //Decimated renderable -- has the ability to redraw and make model request from the manager
    private class decimatedRenderable extends baseRenderable
    {
        decimatedRenderable(String filename)
        {
            this.fileName = filename;
            setID(nextID);
            nextID++;
        }

        public void decimatedModelRequest(float percentageReduction)
        {
            ModelRequestManager.getInstance().add(new ModelRequest(fileName, percentageReduction, getApplicationContext(), MainActivity.this, this.getID()));
        }

        public void redraw()
        {
            Log.d("ServerCommunication", "Redraw waiting is done");
            Frame frame = fragment.getArSceneView().getArFrame();

            if (frame != null) {
                CompletableFuture<Void> renderableFuture =
                        ModelRenderable.builder()
                                .setSource(fragment.getContext(), Uri.fromFile(new File(getExternalFilesDir(null), "/decimated" + fileName + ".sfb")))
                                .build()
                                .thenAccept(renderable -> baseAnchor.setRenderable(renderable))
                                .exceptionally((throwable -> {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                                    builder.setMessage(throwable.getMessage())
                                            .setTitle("Codelab error!");
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                    return null;
                                }));
            }
        }

        public void print(AdapterView<?> parent, int pos)
        {
            Frame frame = fragment.getArSceneView().getArFrame();
            SeekBar seekBar = (SeekBar) findViewById(R.id.simpleBar);
            String item =
                    "==DECIMATED OBJECT== ==" + fileName + "==\n" +
                            "Simplification Percentage: " + seekBar.getProgress() + "%" +
                            " - User Score: " + parent.getItemAtPosition(pos).toString() + "\n" +
                            "Date & time: " + new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss", java.util.Locale.getDefault()).format(new Date()) + "\n";

            item += "Camera position: " +
                    "[" +  posFormat.format(frame.getCamera().getPose().tx()) +
                    "], [" + posFormat.format(frame.getCamera().getPose().ty()) +
                    "], [" + posFormat.format(frame.getCamera().getPose().tz()) +
                    "]\n";

            item += ("Object position: ["
                    + posFormat.format(baseAnchor.getWorldPosition().x) +
                    "], [" + posFormat.format(baseAnchor.getWorldPosition().y) +
                    "], [" + posFormat.format(baseAnchor.getWorldPosition().z) +
                    "]\n");

            item += ("Distance from camera: "
                    + Math.sqrt((Math.pow((baseAnchor.getWorldPosition().x - frame.getCamera().getPose().tx()), 2)
                    + Math.pow((baseAnchor.getWorldPosition().y - frame.getCamera().getPose().ty()), 2)
                    + Math.pow((baseAnchor.getWorldPosition().z - frame.getCamera().getPose().tz()), 2)))
                    + " m\n");

            item += "\n\n";


            try {
                FileOutputStream os = new FileOutputStream(dateFile, false);
                os.write(item.getBytes());
                os.close();
            } catch (IOException e) {
                Log.e("StatWriting", e.getMessage());
            }
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //create the file to store user score data
        dateFile = new File(getExternalFilesDir(null),
                (new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss", java.util.Locale.getDefault()).format(new Date())) + ".txt");

        //user score setup
        Spinner ratingSpinner = (Spinner) findViewById(R.id.userScoreSpinner);
        ratingSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> ratingAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.user_score));
        ratingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ratingSpinner.setAdapter(ratingAdapter);

        fragment = (ArFragment)
                getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);
        fragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            fragment.onUpdate(frameTime);
            onUpdate();
        });
        sensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);



        for ( int i=0; i<5; i++) {
            prmap.put(i, new ArrayList<ArrayList<Float>>());
            marginmap.put(i, new ArrayList<ArrayList<Float>>());
            errormap.put(i, new ArrayList<ArrayList<Float>>());
            booleanmap.put(i, new ArrayList<ArrayList<Float>>());
        }

/*
        margin05.add(new ArrayList<Float>(Arrays.asList(0.3f, 0.3f)));
        margin10.add(new ArrayList<Float>(Arrays.asList(0.3f, 0.3f)));
        margin15.add(new ArrayList<Float>(Arrays.asList(0.3f, 0.3f)));
        margin20.add(new ArrayList<Float>(Arrays.asList(0.3f, 0.3f)));
        margin25.add(new ArrayList<Float>(Arrays.asList(0.3f, 0.3f)));*/

        //float  j=0.5f;
        for ( int i=0; i<5; i++) {

            marginmap.get(i).add(new ArrayList<Float>(Arrays.asList(0.3f, 0.3f)));

            errormap.get(i).add(new ArrayList<Float>(Arrays.asList(0f, 0f)));

        }
/*
        error05.add(new ArrayList<Float>(Arrays.asList(0f, 0f)));
        error10.add(new ArrayList<Float>(Arrays.asList(0f, 0f)));
        error15.add(new ArrayList<Float>(Arrays.asList(0f, 0f)));
        error20.add(new ArrayList<Float>(Arrays.asList(0f, 0f)));
        error25.add(new ArrayList<Float>(Arrays.asList(0f, 0f)));

*/
        SensorEventListener sensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

              }


            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        // Register the listener
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorListener, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        //get the asset list for model select
        try
        {
            //get list of .sfb's from assets
            assetList = getAssets().list("models");
            //take off .sfb from every string for use with server communication
            for(int i = 0; i < assetList.length; i++)
            {
                assetList[i] = assetList[i].substring(0, assetList[i].length() - 4);
            }
            //Log.d("AssetList", Arrays.toString(assetList));
        }
        catch(IOException e)
        {
            Log.e("AssetReading", e.getMessage());
        }

        //setup the model drop down menu
        Spinner modelSpinner = (Spinner) findViewById(R.id.modelSelect);
        modelSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> modelSelectAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, assetList);
        modelSelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelSpinner.setAdapter(modelSelectAdapter);



        //switch on whether or not we're making a model that can be changed
        Switch referenceObjectSwitch = (Switch) findViewById(R.id.refSwitch);
        referenceObjectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                if(b) { referenceObjectSwitchCheck = true; }
                else { referenceObjectSwitchCheck = false; }
            }
        });

        //create button listener for object placer
        Button placeObjectButton = (Button) findViewById(R.id.placeObjButton);
        placeObjectButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                if(referenceObjectSwitchCheck == true)
                {
                    renderArray[objectCount] = new refRenderable(modelSpinner.getSelectedItem().toString());
                    addObject(Uri.parse("models/" + currentModel + ".sfb"), renderArray[objectCount]);
                }
                else
                {
                    renderArray[objectCount] = new decimatedRenderable(modelSpinner.getSelectedItem().toString());
                    addObject(Uri.parse("models/" + currentModel + ".sfb"), renderArray[objectCount]);
                }

            }
        });

        //Clear all objects button setup
        Button clearButton = (Button) findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                for(int i = 0; i < objectCount; i++)
                {
                    renderArray[i].detach();
                }
                objectCount = 0;
            }
        });

        //seekbar setup
        SeekBar simpleBar = (SeekBar) findViewById(R.id.simpleBar);
        simpleBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                progress = ((int)Math.round(progress/SEEKBAR_INCREMENT ))*SEEKBAR_INCREMENT;
                seekBar.setProgress(progress);
                TextView simpleBarText = (TextView) findViewById(R.id.simpleBarText);
                simpleBarText.setText(progress + "%");
                int val = (progress * (seekBar.getWidth() - 2 * seekBar.getThumbOffset())) / seekBar.getMax();
                simpleBarText.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("ServerCommunication", "Tracking Stopped, redrawing...");
                //arFragment.getTransformationSystem().getSelectedNode()
                for(int i = 0; i < objectCount; i++)
                {
                    if(renderArray[i].baseAnchor.isSelected())
                        renderArray[i].decimatedModelRequest(seekBar.getProgress()/100f);
                }
            }
        });


        //initialized gallery is not used any more, but I didn't want to break anything so it's still here
        initializeGallery();

        Timer t = new Timer();
        t.scheduleAtFixedRate(
                new TimerTask() {
                    public void run() {
                        if(objectCount>= 1) {
                          Frame frame = fragment.getArSceneView().getArFrame();
                          current.add(new ArrayList<Float>(Arrays.asList(frame.getCamera().getPose().tx(), frame.getCamera().getPose().ty(), frame.getCamera().getPose().tz())));
                          timeLog.add(timeInSec);
                          timeInSec = timeInSec + 0.5f;
                         /* predicted05.add(predictNextError(0.5f));
                          predicted10.add(predictNextError(1f));
                          predicted15.add(predictNextError(1.5f));
                          predicted20.add(predictNextError(2f));
                          predicted25.add(predictNextError(2.5f));
*/
                          //int i=0;

                           float  j=0.5f;
                          for ( int i=0; i<5; i++)
                          { prmap.get(i).add(predictNextError2(j, i));
                              j+=0.5f;}

                       }

                    }

                },
                0,      // run first occurrence immediatetly
                500);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //for user score selection
    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        switch(parent.getId())
        {
            case R.id.modelSelect:
                currentModel = parent.getItemAtPosition(pos).toString();
                break;
            case R.id.userScoreSpinner:
                for(int i = 0; i < objectCount; i++)
                {
                    if(renderArray[i].baseAnchor.isSelected())
                        renderArray[i].print(parent, pos);
                }
                break;
        }

    }

    @Override
    public void onPause() {
        super.onPause();


        String currentFolder = getExternalFilesDir(null).getAbsolutePath();
        String FILEPATH = currentFolder + File.separator + "output.txt";
        Toast.makeText(this,"FILE PATH: " + FILEPATH, Toast.LENGTH_LONG).show();
        PrintWriter fileOut = null;
        PrintStream streamOut = null;
        int size = current.size();
        errorAnalysis2(size);
        try {
            fileOut = new PrintWriter(new FileOutputStream(FILEPATH, false));


            double t=0;
            for (int j = 0; j < 5; j++)
            {
                t+=0.5;
                fileOut.println();
            fileOut.println("Predicted Confidence Area for "+ (t));
            for (int i = 0; i < size; i++) {
                fileOut.println(timeLog.get(i) + " " + current.get(i).get(0) + " " + current.get(i).get(1) + " " + current.get(i).get(2) + " " +
                        (timeLog.get(i) + t) + " " + prmap.get(j).get(i).get(0) + " " + prmap.get(j).get(i).get(1) + " " + prmap.get(j).get(i).get(2) + " " + prmap.get(j).get(i).get(3) + " " + prmap.get(j).get(i).get(4) + " " + prmap.get(j).get(i).get(5) + " " + prmap.get(j).get(i).get(6) + " " + prmap.get(j).get(i).get(7) + " " + prmap.get(j).get(i).get(8) + " " + prmap.get(j).get(i).get(9) + " " + marginmap.get(j).get(i).get(0) + " " + marginmap.get(j).get(i).get(1) + " " + errormap.get(j).get(i).get(0) + " " + errormap.get(j).get(i).get(1));
            }

              }
           /*
            fileOut.println();
            fileOut.println("Predicted Confidence Area: 1.0 ");
            for (int i = 0; i < size; i++) {

            ///    float lenght= Math.abs(predicted10.get(i).get(4)- predicted10.get(i).get(6));
              ///  float width=Math.abs(predicted10.get(i).get(3)- predicted10.get(i).get(5));
                ///float area =lenght*width;

                fileOut.println(timeLog.get(i) + " " + current.get(i).get(0) + " " + current.get(i).get(1) + " " + current.get(i).get(2) + " " +
                       (timeLog.get(i)+1) + " " + predicted10.get(i).get(0) + " " +predicted10.get(i).get(1) + " " +predicted10.get(i).get(2) + " "+ predicted10.get(i).get(3) + " " + predicted10.get(i).get(4) + " " + predicted10.get(i).get(5) + " " + predicted10.get(i).get(6) + " " + predicted10.get(i).get(7) + " " + predicted10.get(i).get(8) + " " + predicted10.get(i).get(9) + " " + margin10.get(i).get(0) + " " + margin10.get(i).get(1) + " " + error10.get(i).get(0) + " " + error10.get(i).get(1) + " area " + area);


            }


            fileOut.println();



            fileOut.println("Predicted Confidence Area: 1.5");
            for (int i = 0; i < size; i++) {
                fileOut.println(timeLog.get(i) + " " + current.get(i).get(0) + " " + current.get(i).get(1) + " " + current.get(i).get(2) + " " +
                        (timeLog.get(i)+1.5) + " "+ predicted15.get(i).get(0) + " " +predicted15.get(i).get(1) + " " +predicted15.get(i).get(2) + " " + predicted15.get(i).get(3) + " " + predicted15.get(i).get(4) + " " + predicted15.get(i).get(5) + " " + predicted15.get(i).get(6) + " " + predicted15.get(i).get(7) + " " + predicted15.get(i).get(8) + " " + predicted15.get(i).get(9) + " " + margin15.get(i).get(0) + " " + margin15.get(i).get(1) + " " + error15.get(i).get(0) + " " + error15.get(i).get(1));
            }

            fileOut.println();
            fileOut.println("Predicted Confidence Area: 2.0");
            for (int i = 0; i < size; i++) {
                fileOut.println(timeLog.get(i) + " " + current.get(i).get(0) + " " + current.get(i).get(1) + " " + current.get(i).get(2) + " " +
                        (timeLog.get(i)+2) + " "+ predicted20.get(i).get(0) + " " +predicted20.get(i).get(1) + " " +predicted20.get(i).get(2) + " " + predicted20.get(i).get(3) + " " + predicted20.get(i).get(4) + " " + predicted20.get(i).get(5) + " " + predicted20.get(i).get(6) + " " + predicted20.get(i).get(7) + " " + predicted20.get(i).get(8) + " " + predicted20.get(i).get(9) + " " + margin20.get(i).get(0) + " " + margin20.get(i).get(1) + " " + error20.get(i).get(0) + " " + error20.get(i).get(1));
            }

            fileOut.println();
            fileOut.println("Predicted Confidence Area: 2.5");
            for (int i = 0; i < size; i++) {
                fileOut.println(timeLog.get(i) + " " + current.get(i).get(0) + " " + current.get(i).get(1) + " " + current.get(i).get(2) + " " +
                        (timeLog.get(i)+2.5) + " "+ predicted25.get(i).get(0) + " " +predicted25.get(i).get(1) + " " +predicted25.get(i).get(2) + " " + predicted25.get(i).get(3) + " " + predicted25.get(i).get(4) + " " + predicted25.get(i).get(5) + " " +  predicted25.get(i).get(6) + " " + predicted25.get(i).get(7) + " " + predicted25.get(i).get(8) + " " + predicted25.get(i).get(9) + " " + margin25.get(i).get(0) + " " + margin25.get(i).get(1) + " " + error25.get(i).get(0) + " " + error25.get(i).get(1));
            }


            */
            fileOut.println();
            fileOut.println("Error Analysis: ");
            for (int j = 0; j < size - 5; j++) {
                fileOut.println((timeLog.get(j) + 0.5) + " " + booleanmap.get(0).get(j).get(0) + " " + booleanmap.get(0).get(j).get(1) + " " +
                        (timeLog.get(j) + 1.0) + " " + booleanmap.get(1).get(j).get(0) + " " + booleanmap.get(1).get(j).get(1) + " " +
                        (timeLog.get(j) + 1.5) + " " + booleanmap.get(2).get(j).get(0) + " " + booleanmap.get(2).get(j).get(1) + " " +
                        (timeLog.get(j) + 2.0) + " " + booleanmap.get(3).get(j).get(0) + " " + booleanmap.get(3).get(j).get(1) + " " +
                        (timeLog.get(j) + 2.5) + " " + booleanmap.get(4).get(j).get(0) + " " + booleanmap.get(4).get(j).get(1));
            }

            fileOut.close();
        } catch (IOException e) {
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void onUpdate() {
        boolean trackingChanged = updateTracking();
        View contentView = findViewById(android.R.id.content);
        if (trackingChanged) {
            if (isTracking) {
                contentView.getOverlay().add(pointer);
            } else {
                contentView.getOverlay().remove(pointer);
            }
            contentView.invalidate();
        }

        if (isTracking) {
            boolean hitTestChanged = updateHitTest();
            if (hitTestChanged) {
                pointer.setEnabled(isHitting);
                contentView.invalidate();
            }
        }
    }


    private boolean updateTracking() {
        Frame frame = fragment.getArSceneView().getArFrame();
        boolean wasTracking = isTracking;
        isTracking = frame != null &&
                frame.getCamera().getTrackingState() == TrackingState.TRACKING;
        TextView posText = (TextView) findViewById(R.id.cameraPosition);
        posText.setText( "Camera Position: " +
                "[" +  posFormat.format(frame.getCamera().getPose().tx()) +
                "], [" + posFormat.format(frame.getCamera().getPose().ty()) +
                "], [" + posFormat.format(frame.getCamera().getPose().tz()) +
                "]" + "\n");
        return isTracking != wasTracking;
    }

    private boolean updateHitTest() {
        Frame frame = fragment.getArSceneView().getArFrame();
        android.graphics.Point pt = getScreenCenter();
        List<HitResult> hits;
        boolean wasHitting = isHitting;
        isHitting = false;
        if (frame != null) {
            hits = frame.hitTest(pt.x, pt.y);
            for (HitResult hit : hits) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane &&
                        ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    isHitting = true;
                    break;
                }
            }
        }
        return wasHitting != isHitting;
    }

    private android.graphics.Point getScreenCenter() {
        View vw = findViewById(android.R.id.content);
        return new android.graphics.Point(vw.getWidth()/2, vw.getHeight()/2);
    }

    //initialized gallery is not used any more, but I didn't want to break anything so it's still here
    //This also creates a file in the apps internal directory to help me find it better, to be honest.
    private void initializeGallery() {
        //LinearLayout galleryR1 = findViewById(R.id.gallery_layout_r1);
        RelativeLayout galleryr2 = findViewById(R.id.gallery_layout);

        //row 1

        File file = new File(this.getExternalFilesDir(null), "/andy1k.sfb");



    }


    //this came with the app, it sends out a ray to a plane and wherever it hits, it makes an anchor
    //then it calls placeobject
    private void addObject(Uri model, baseRenderable renderArrayObj) {
        Frame frame = fragment.getArSceneView().getArFrame();
        android.graphics.Point pt = getScreenCenter();
        List<HitResult> hits;
        if (frame != null) {
            hits = frame.hitTest(pt.x, pt.y);
            for (HitResult hit : hits) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane &&
                        ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    Anchor newAnchor = hit.createAnchor();
                    placeObject(fragment, newAnchor, model, renderArrayObj);
                    break;
                }
            }
        }
    }


    //placeObject creates the renderable on the anchor then calls addNodeToScene
    private void placeObject(ArFragment fragment, Anchor anchor, Uri model, baseRenderable renderArrayObj) { ;
        CompletableFuture<Void> renderableFuture =
                ModelRenderable.builder()
                        .setSource(fragment.getContext(), model)
                        .build()
                        .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable, renderArrayObj))
                        .exceptionally((throwable -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage(throwable.getMessage())
                                    .setTitle("Codelab error!");
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return null;
                        }));
    }

    //takes both the renderable and anchor and actually adds it to the scene.
    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable, baseRenderable renderArrayObj) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        //anchorArray[anchorCount] = node;
        //anchorCount++;
        renderArrayObj.setAnchor(node);
        objectCount++;
        objX = renderArray[0].baseAnchor.getWorldPosition().x;
        objZ = renderArray[0].baseAnchor.getWorldPosition().z;

        fragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
    }

    private float nextPoint(float x1, float x2, float y1, float y2, float time)
    {
        float slope = (y2 - y1)/(x2 - x1);
        float y3 = slope*(x2 + time) -(slope*x1) + y1;
        return y3;
    }

    private float nextPointEstimation(float actual, float predicted)
    {
        return (alpha * actual) + ((1 - alpha) * predicted);
    }

    private float[] rotate_point(double rad_angle, float x, float z)
    {
        float[] rotated = new float[2];

        rotated[0] = x* (float)Math.cos(rad_angle) - z * (float)Math.sin(rad_angle);
        rotated[1] = x* (float)Math.sin(rad_angle) + z * (float)Math.cos(rad_angle);

        return rotated;
    }

    private float[] rotate_around_point(double rad_angle, float x, float z, float orgX, float orgZ)
    {
        float[] rotated = new float[2];

        rotated[0] = (x - orgX)* (float)Math.cos(rad_angle) - (z - orgZ) * (float)Math.sin(rad_angle) + orgX;
        rotated[1] = (x - orgX)* (float)Math.sin(rad_angle) + (z - orgZ) * (float)Math.cos(rad_angle) + orgZ;

        return rotated;
    }




    private ArrayList<Float> predictNextError2(float time, int ind)
    {
        ArrayList<Float> predictedValues = new ArrayList<>();
        ArrayList<Float> margin = new ArrayList<>();
        ArrayList<Float> error = new ArrayList<>();
        int curr_size = current.size();
        float predictedX = 0f;
        float predictedZ = 0f;
        float actual_errorX = 0f;
        float actual_errorZ = 0f;
        float predict_diffX, predict_diffZ;
       // System.out.println("current: " + curr_size + "0.5: " + predicted05.size());

               // ind 0,   1,  2,  3,  4,
                //time 0.5, 1, 1.5, 2, 2.5
       // currsize - i1  2 , 3,  4,   5,   6
        //prmap.get(0) is equall to predicted05
        int i1 = ind +2;

        if(curr_size >1 )
        {

            float marginx = 0.3f, marginz = 0.3f;

                if (curr_size >5) {
                    predict_diffX =  prmap.get(ind).get(curr_size - i1).get(0) - current.get(curr_size - i1).get(0);
                    predict_diffZ = prmap.get(ind).get(curr_size - i1).get(1) - current.get(curr_size - i1).get(2);
                    float actual_diffX = current.get(curr_size - 1).get(0) - current.get(curr_size - i1).get(0);
                    float actual_diffZ = current.get(curr_size - 1).get(2) - current.get(curr_size - i1).get(2);
                    predictedX = nextPointEstimation(actual_diffX,predict_diffX) + current.get(curr_size - 1).get(0);
                    predictedZ = nextPointEstimation(actual_diffZ,predict_diffZ)+ current.get(curr_size - 1).get(2);
                }
                else{
                    predictedX = nextPoint(timeLog.get(curr_size - 2), timeLog.get(curr_size - 1), current.get(curr_size - 2).get(0), current.get(curr_size - 1).get(0), time);
                    predictedZ = nextPoint(timeLog.get(curr_size - 2), timeLog.get(curr_size - 1), current.get(curr_size - 2).get(2), current.get(curr_size - 1).get(2), time);
                }

                if (curr_size > i1) {
                    actual_errorX = abs(current.get(curr_size - 1).get(0) - prmap.get(ind).get(curr_size - i1).get(0));
                    actual_errorZ = abs(current.get(curr_size - 1).get(2) - prmap.get(ind).get(curr_size - i1).get(1));
                    float margin_x = abs(nextPointEstimation(actual_errorX, marginmap.get(ind).get(curr_size-2).get(0)));
                    float margin_z = abs(nextPointEstimation(actual_errorZ, marginmap.get(ind).get(curr_size-2).get(1)));

                    if(margin_x < marginmap.get(ind).get(curr_size-2).get(0))
                        marginx = marginmap.get(ind).get(curr_size-2).get(0);
                    else
                        marginx = margin_x;

                    if(margin_z < marginmap.get(ind).get(curr_size-2).get(1))
                        marginz = marginmap.get(ind).get(curr_size-2).get(1);
                    else
                        marginz = margin_z;
                }
                margin.add(marginx);
                margin.add(marginz);
            marginmap.get(ind).add(margin);
                error.add(actual_errorX);
                error.add(actual_errorZ);
            errormap.get(ind).add(error);

            // time =0.5

            double tan_val = (double)((predictedZ-current.get(curr_size - 1).get(2))/(predictedX-current.get(curr_size - 1).get(0)));
            double angle = Math.atan(tan_val);
            predictedValues.add(predictedX); //predicted X value
            predictedValues.add(predictedZ); //predicted Z value


            float[] rotated = rotate_point(angle, marginx,marginz);
            predictedValues.add(predictedX + rotated[0]); //Confidence area X coordinate 1
            predictedValues.add(predictedZ + rotated[1]); //Confidence area Y coordinate 1
            //float[] val1 = rotate_around_point(theta,predictedX + rotated[0], predictedZ + rotated[1],current.get(curr_size - 1).get(0), current.get(curr_size - 1).get(2));

            rotated = rotate_point(angle, marginx,-marginz);
            predictedValues.add(predictedX + rotated[0]); //Confidence area X coordinate 2
            predictedValues.add(predictedZ + rotated[1]); //Confidence area Y coordinate 2

            rotated = rotate_point(angle, -marginx,-marginz);
            predictedValues.add(predictedX + rotated[0]); //Confidence area  X coordinate 3
            predictedValues.add(predictedZ + rotated[1]); //Confidence area Y coordinate 3

            rotated = rotate_point(angle, -marginx,marginz);
            predictedValues.add(predictedX + rotated[0]); //Confidence area X coordinate 4
            predictedValues.add(predictedZ + rotated[1]); //Confidence area Y coordinate 4

        }
        else {
            int count=0;
            for (count=0; count<=9; count++)
                predictedValues.add(0f);

        }
        return predictedValues;
    }

/*
    private ArrayList<Float> predictNextError(float time)
    {
        ArrayList<Float> predictedValues = new ArrayList<>();
        ArrayList<Float> margin = new ArrayList<>();
        ArrayList<Float> error = new ArrayList<>();
        int curr_size = current.size();
        float predictedX = 0f;
        float predictedZ = 0f;
        float actual_errorX = 0f;
        float actual_errorZ = 0f;
        float predict_diffX, predict_diffZ;
        System.out.println("current: " + curr_size + "0.5: " + predicted05.size());
        if(curr_size >1 )
        {
            float marginx = 0.3f, marginz = 0.3f;
            if(time == 0.5) {
                if (curr_size >5) {
                    predict_diffX = predicted05.get(curr_size - 2).get(0) - current.get(curr_size - 2).get(0);
                    predict_diffZ = predicted05.get(curr_size - 2).get(1) - current.get(curr_size - 2).get(2);
                    float actual_diffX = current.get(curr_size - 1).get(0) - current.get(curr_size - 2).get(0);
                    float actual_diffZ = current.get(curr_size - 1).get(2) - current.get(curr_size - 2).get(2);
                    predictedX = nextPointEstimation(actual_diffX,predict_diffX) + current.get(curr_size - 1).get(0);
                    predictedZ = nextPointEstimation(actual_diffZ,predict_diffZ)+ current.get(curr_size - 1).get(2);
                }
                else{
                    predictedX = nextPoint(timeLog.get(curr_size - 2), timeLog.get(curr_size - 1), current.get(curr_size - 2).get(0), current.get(curr_size - 1).get(0), time);
                    predictedZ = nextPoint(timeLog.get(curr_size - 2), timeLog.get(curr_size - 1), current.get(curr_size - 2).get(2), current.get(curr_size - 1).get(2), time);
                }

                if (curr_size > 2) {
                    actual_errorX = abs(current.get(curr_size - 1).get(0) - predicted05.get(curr_size - 2).get(0));
                    actual_errorZ = abs(current.get(curr_size - 1).get(2) - predicted05.get(curr_size - 2).get(1));
                    float margin_x = abs(nextPointEstimation(actual_errorX, margin05.get(curr_size-2).get(0)));
                    float margin_z = abs(nextPointEstimation(actual_errorZ, margin05.get(curr_size-2).get(1)));

                    if(margin_x < margin05.get(curr_size-2).get(0))
                        marginx = margin05.get(curr_size-2).get(0);
                    else
                    marginx = margin_x;

                    if(margin_z < margin05.get(curr_size-2).get(1))
                        marginz = margin05.get(curr_size-2).get(1);
                    else
                        marginz = margin_z;
                }
                margin.add(marginx);
                margin.add(marginz);
                margin05.add(margin);
                error.add(actual_errorX);
                error.add(actual_errorZ);
                error05.add(error);
            }
            else if(time == 1) {
                if (curr_size > 5) {
                    predict_diffX = predicted10.get(curr_size - 3).get(0) - current.get(curr_size - 3).get(0);
                    predict_diffZ = predicted10.get(curr_size - 3).get(1) - current.get(curr_size - 3).get(2);
                    float actual_diffX = current.get(curr_size - 1).get(0) - current.get(curr_size - 3).get(0);
                    float actual_diffZ = current.get(curr_size - 1).get(2) - current.get(curr_size - 3).get(2);
                    predictedX = nextPointEstimation(actual_diffX,predict_diffX) + current.get(curr_size - 1).get(0);
                    predictedZ = nextPointEstimation(actual_diffZ,predict_diffZ)+ current.get(curr_size - 1).get(2);
                }
                else
                {
                    predictedX = nextPoint(timeLog.get(curr_size - 2), timeLog.get(curr_size - 1), current.get(curr_size - 2).get(0), current.get(curr_size - 1).get(0), time);
                    predictedZ = nextPoint(timeLog.get(curr_size - 2), timeLog.get(curr_size - 1), current.get(curr_size - 2).get(2), current.get(curr_size - 1).get(2), time);
                }

                if(curr_size> 3) {
                    actual_errorX = abs(current.get(curr_size - 1).get(0) - predicted10.get(curr_size - 3).get(0));
                    actual_errorZ = abs(current.get(curr_size - 1).get(2) - predicted10.get(curr_size - 3).get(1));
                    float margin_x = abs(nextPointEstimation(actual_errorX, margin10.get(curr_size-2).get(0)));
                    float margin_z = abs(nextPointEstimation(actual_errorZ, margin10.get(curr_size-2).get(1)));
                    if(margin_x < margin10.get(curr_size-2).get(0))
                        marginx = margin10.get(curr_size-2).get(0);
                    else
                        marginx = margin_x;

                    if(margin_z < margin10.get(curr_size-2).get(1))
                        marginz = margin10.get(curr_size-2).get(1);
                    else
                        marginz = margin_z;
                }
                margin.add(marginx);
                margin.add(marginz);
                margin10.add(margin);
                error.add(actual_errorX);
                error.add(actual_errorZ);
                error10.add(error);
            }
            else if(time == 1.5) {
                if (curr_size > 5) {
                    predict_diffX = predicted15.get(curr_size - 4).get(0) - current.get(curr_size - 4).get(0);
                    predict_diffZ = predicted15.get(curr_size - 4).get(1) - current.get(curr_size - 4).get(2);
                    float actual_diffX = current.get(curr_size - 1).get(0) - current.get(curr_size - 4).get(0);
                    float actual_diffZ = current.get(curr_size - 1).get(2) - current.get(curr_size - 4).get(2);
                    predictedX = nextPointEstimation(actual_diffX,predict_diffX) + current.get(curr_size - 1).get(0);
                    predictedZ = nextPointEstimation(actual_diffZ,predict_diffZ)+ current.get(curr_size - 1).get(2);
                }
                else {
                    predictedX = nextPoint(timeLog.get(curr_size - 2), timeLog.get(curr_size - 1), current.get(curr_size - 2).get(0), current.get(curr_size - 1).get(0), time);
                    predictedZ = nextPoint(timeLog.get(curr_size - 2), timeLog.get(curr_size - 1), current.get(curr_size - 2).get(2), current.get(curr_size - 1).get(2), time);
                }
                if(curr_size> 4) {
                    actual_errorX = abs(current.get(curr_size - 1).get(0) - predicted15.get(curr_size - 4).get(0));
                    actual_errorZ = abs(current.get(curr_size - 1).get(2) - predicted15.get(curr_size - 4).get(1));
                    float margin_x = abs(nextPointEstimation(actual_errorX, margin15.get(curr_size-2).get(0)));
                    float margin_z = abs(nextPointEstimation(actual_errorZ, margin15.get(curr_size-2).get(1)));
                    if(margin_x < margin15.get(curr_size-2).get(0))
                        marginx = margin15.get(curr_size-2).get(0);
                    else
                        marginx = margin_x;

                    if(margin_z < margin15.get(curr_size-2).get(1))
                        marginz = margin15.get(curr_size-2).get(1);
                    else
                        marginz = margin_z;
                }

                margin.add(marginx);
                margin.add(marginz);
                margin15.add(margin);
                error.add(actual_errorX);
                error.add(actual_errorZ);
                error15.add(error);
            }
            else if(time == 2) {

                if (curr_size > 6) {
                    predict_diffX = predicted20.get(curr_size - 5).get(0) - current.get(curr_size - 5).get(0);
                    predict_diffZ = predicted20.get(curr_size - 5).get(1) - current.get(curr_size - 5).get(2);
                    float actual_diffX = current.get(curr_size - 1).get(0) - current.get(curr_size - 5).get(0);
                    float actual_diffZ = current.get(curr_size - 1).get(2) - current.get(curr_size - 5).get(2);
                    predictedX = nextPointEstimation(actual_diffX,predict_diffX) + current.get(curr_size - 1).get(0);
                    predictedZ = nextPointEstimation(actual_diffZ,predict_diffZ)+ current.get(curr_size - 1).get(2);
                }
                else
                {
                    predictedX = nextPoint(timeLog.get(curr_size - 2), timeLog.get(curr_size - 1), current.get(curr_size - 2).get(0), current.get(curr_size - 1).get(0), time);
                    predictedZ = nextPoint(timeLog.get(curr_size - 2), timeLog.get(curr_size - 1), current.get(curr_size - 2).get(2), current.get(curr_size - 1).get(2), time);
                }
                if(curr_size> 5) {
                    actual_errorX = abs(current.get(curr_size - 1).get(0) - predicted20.get(curr_size - 5).get(0));
                    actual_errorZ = abs(current.get(curr_size - 1).get(2) - predicted20.get(curr_size - 5).get(1));
                    float margin_x = abs(nextPointEstimation(actual_errorX, margin20.get(curr_size-2).get(0)));
                    float margin_z = abs(nextPointEstimation(actual_errorZ, margin20.get(curr_size-2).get(1)));
                    if(margin_x < margin20.get(curr_size-2).get(0))
                        marginx = margin20.get(curr_size-2).get(0);
                    else
                        marginx = margin_x;

                    if(margin_z < margin20.get(curr_size-2).get(1))
                        marginz = margin20.get(curr_size-2).get(1);
                    else
                        marginz = margin_z;
                }

                margin.add(marginx);
                margin.add(marginz);
                margin20.add(margin);
                error.add(actual_errorX);
                error.add(actual_errorZ);
                error20.add(error);
            }
            else if(time == 2.5) {
                if (curr_size > 6) {
                    predict_diffX = predicted25.get(curr_size - 6).get(0) - current.get(curr_size - 6).get(0);
                    predict_diffZ = predicted25.get(curr_size - 6).get(1) - current.get(curr_size - 6).get(2);
                    float actual_diffX = current.get(curr_size - 1).get(0) - current.get(curr_size - 6).get(0);
                    float actual_diffZ = current.get(curr_size - 1).get(2) - current.get(curr_size - 6).get(2);
                    predictedX = nextPointEstimation(actual_diffX,predict_diffX) + current.get(curr_size - 1).get(0);
                    predictedZ = nextPointEstimation(actual_diffZ,predict_diffZ)+ current.get(curr_size - 1).get(2);;
                }
                else
                {
                    predictedX = nextPoint(timeLog.get(curr_size - 2), timeLog.get(curr_size - 1), current.get(curr_size - 2).get(0), current.get(curr_size - 1).get(0), time);
                    predictedZ = nextPoint(timeLog.get(curr_size - 2), timeLog.get(curr_size - 1), current.get(curr_size - 2).get(2), current.get(curr_size - 1).get(2), time);
                    marginx = 0.3f;
                    marginz = 0.3f;
                }
                if(curr_size> 6) {
                    actual_errorX = abs(current.get(curr_size - 1).get(0) - predicted25.get(curr_size - 6).get(0));
                    actual_errorZ = abs(current.get(curr_size - 1).get(2) - predicted25.get(curr_size - 6).get(1));
                    float margin_x = abs(nextPointEstimation(actual_errorX, margin25.get(curr_size-2).get(0)));
                    float margin_z = abs(nextPointEstimation(actual_errorZ, margin25.get(curr_size-2).get(1)));
                    if(margin_x < margin25.get(curr_size-2).get(0))
                        marginx = margin25.get(curr_size-2).get(0);
                    else
                        marginx = margin_x;

                    if(margin_z < margin25.get(curr_size-2).get(1))
                        marginz = margin25.get(curr_size-2).get(1);
                    else
                        marginz = margin_z;
                }

                margin.add(marginx);
                margin.add(marginz);
                margin25.add(margin);
                error.add(actual_errorX);
                error.add(actual_errorZ);
                error25.add(error);
            }

            double tan_val = (double)((predictedZ-current.get(curr_size - 1).get(2))/(predictedX-current.get(curr_size - 1).get(0)));
            double angle = Math.atan(tan_val);
            predictedValues.add(predictedX); //predicted X value
            predictedValues.add(predictedZ); //predicted Z value



            float[] rotated = rotate_point(angle, marginx,marginz);
            predictedValues.add(predictedX + rotated[0]); //Confidence area X coordinate 1
            predictedValues.add(predictedZ + rotated[1]); //Confidence area Y coordinate 1
            //float[] val1 = rotate_around_point(theta,predictedX + rotated[0], predictedZ + rotated[1],current.get(curr_size - 1).get(0), current.get(curr_size - 1).get(2));

            rotated = rotate_point(angle, marginx,-marginz);
            predictedValues.add(predictedX + rotated[0]); //Confidence area X coordinate 2
            predictedValues.add(predictedZ + rotated[1]); //Confidence area Y coordinate 2

            rotated = rotate_point(angle, -marginx,-marginz);
            predictedValues.add(predictedX + rotated[0]); //Confidence area  X coordinate 3
            predictedValues.add(predictedZ + rotated[1]); //Confidence area Y coordinate 3

            rotated = rotate_point(angle, -marginx,marginz);
            predictedValues.add(predictedX + rotated[0]); //Confidence area X coordinate 4
            predictedValues.add(predictedZ + rotated[1]); //Confidence area Y coordinate 4

        }
        else {
            int count=0;
            for (count=0; count<=9; count++)
                   predictedValues.add(0f);

        }
        return predictedValues;
    }
*/





    /*
    private ArrayList<Float> predictNextError(float time)
    {
        ArrayList<Float> predictedValues = new ArrayList<>();
        ArrayList<Float> margin = new ArrayList<>();
        int curr_size = current.size();
        float predictedX = 0f;
        float predictedZ = 0f;
        float predict_diffX, predict_diffZ;
        System.out.println("current: " + curr_size + "0.5: " + predicted05.size());
        if(curr_size >3 )
        {
            float marginx = 0f, marginz = 0f;
            if(time == 0.5) {
                if (predicted05.size() >= 1) {
                    predict_diffX = predicted05.get(curr_size - 2).get(0) - current.get(curr_size - 1).get(0);
                    predict_diffZ = predicted05.get(curr_size - 2).get(2) - current.get(curr_size - 1).get(2);
                    float actual_errorX = abs(current.get(curr_size - 1).get(0) - predicted05.get(curr_size - 2).get(0));
                    float actual_errorZ = abs(current.get(curr_size - 1).get(2) - predicted05.get(curr_size - 2).get(2));
                    marginx = abs(nextPointEstimation(actual_errorX, margin05.get(curr_size-2).get(0)));
                    marginz = abs(nextPointEstimation(actual_errorZ, margin05.get(curr_size-2).get(1)));
                }
                else
                {
                    predict_diffX = 0.05f;
                    predict_diffZ = 0.35f;
                    marginx = 0.01f;
                    marginz = 0.04f;
                    marginx = 0.3f;
                    marginz = 0.3f;
                }
                float actual_diffX = current.get(curr_size - 1).get(0) - current.get(curr_size - 2).get(0);
                float actual_diffZ = current.get(curr_size - 1).get(2) - current.get(curr_size - 2).get(2);
                predictedX = nextPointEstimation(actual_diffX,predict_diffX) + current.get(curr_size - 1).get(0);
                predictedZ = nextPointEstimation(actual_diffZ,predict_diffZ)+ current.get(curr_size - 1).get(2);
                margin.add(marginx);
                margin.add(marginz);
                margin05.add(margin);
            }
            else if(time == 1) {
                if (predicted10.size() >= 2) {
                    predict_diffX = predicted10.get(curr_size - 3).get(0) - current.get(curr_size - 1).get(0);
                    predict_diffZ = predicted10.get(curr_size - 3).get(2) - current.get(curr_size - 1).get(2);
                    float actual_errorX = abs(current.get(curr_size - 1).get(0) - predicted10.get(curr_size - 3).get(0));
                    float actual_errorZ = abs(current.get(curr_size - 1).get(2) - predicted10.get(curr_size - 3).get(2));
                    marginx = abs(nextPointEstimation(actual_errorX, margin10.get(curr_size-2).get(0)));
                    marginz = abs(nextPointEstimation(actual_errorZ, margin10.get(curr_size-2).get(1)));
                }
                else
                {
                    predict_diffX = 0.15f;
                    predict_diffZ = 0.65f;
                    marginx = 0.02f;
                    marginz = 0.05f;
                    marginx = 0.3f;
                    marginz = 0.3f;
                }
                float actual_diffX = current.get(curr_size - 1).get(0) - current.get(curr_size - 2).get(0);
                float actual_diffZ = current.get(curr_size - 1).get(2) - current.get(curr_size - 2).get(2);
                predictedX = nextPointEstimation(actual_diffX,predict_diffX) + current.get(curr_size - 1).get(0);
                predictedZ = nextPointEstimation(actual_diffZ,predict_diffZ)+ current.get(curr_size - 1).get(2);
                margin.add(marginx);
                margin.add(marginz);
                margin10.add(margin);
            }
            else if(time == 1.5) {
                if (predicted15.size() >= 3) {
                    predict_diffX = predicted15.get(curr_size - 4).get(0) - current.get(curr_size - 1).get(0);
                    predict_diffZ = predicted15.get(curr_size - 4).get(2) - current.get(curr_size - 1).get(2);
                    float actual_errorX = abs(current.get(curr_size - 1).get(0) - predicted15.get(curr_size - 4).get(0));
                    float actual_errorZ = abs(current.get(curr_size - 1).get(2) - predicted15.get(curr_size - 4).get(2));
                    marginx = abs(nextPointEstimation(actual_errorX, margin15.get(curr_size-2).get(0)));
                    marginz = abs(nextPointEstimation(actual_errorZ, margin15.get(curr_size-2).get(1)));
                }
                else {
                    predict_diffX = 0.25f;
                    predict_diffZ = 1.0f;
                    marginx = 0.02f;
                    marginz = 0.02f;
                    marginx = 0.3f;
                    marginz = 0.3f;
                }
                float actual_diffX = current.get(curr_size - 1).get(0) - current.get(curr_size - 2).get(0);
                float actual_diffZ = current.get(curr_size - 1).get(2) - current.get(curr_size - 2).get(2);
                predictedX = nextPointEstimation(actual_diffX,predict_diffX) + current.get(curr_size - 1).get(0);
                predictedZ = nextPointEstimation(actual_diffZ,predict_diffZ)+ current.get(curr_size - 1).get(2);

                margin.add(marginx);
                margin.add(marginz);
                margin15.add(margin);
            }
            else if(time == 2) {
                if (predicted20.size() >= 4) {
                    predict_diffX = predicted20.get(curr_size - 5).get(0) - current.get(curr_size - 1).get(0);
                    predict_diffZ = predicted20.get(curr_size - 5).get(2) - current.get(curr_size - 1).get(2);
                    float actual_errorX = abs(current.get(curr_size - 1).get(0) - predicted20.get(curr_size - 5).get(0));
                    float actual_errorZ = abs(current.get(curr_size - 1).get(2) - predicted20.get(curr_size - 5).get(2));
                    marginx = abs(nextPointEstimation(actual_errorX, margin20.get(curr_size-2).get(0)));
                    marginz = abs(nextPointEstimation(actual_errorZ, margin20.get(curr_size-2).get(1)));

                }
                else
                {
                    predict_diffX = 0.35f;
                    predict_diffZ = 1.5f;
                    marginx = 0.04f;
                    marginz = 0.04f;
                    marginx = 0.3f;
                    marginz = 0.3f;
                }
                float actual_diffX = current.get(curr_size - 1).get(0) - current.get(curr_size - 2).get(0);
                float actual_diffZ = current.get(curr_size - 1).get(2) - current.get(curr_size - 2).get(2);
                predictedX = nextPointEstimation(actual_diffX,predict_diffX) + current.get(curr_size - 1).get(0);
                predictedZ = nextPointEstimation(actual_diffZ,predict_diffZ)+ current.get(curr_size - 1).get(2);
                margin.add(marginx);
                margin.add(marginz);
                margin20.add(margin);
            }
            else if(time == 2.5) {
                if (predicted25.size() >= 5) {
                    predict_diffX = predicted25.get(curr_size - 6).get(0) - current.get(curr_size - 1).get(0);
                    predict_diffZ = predicted25.get(curr_size - 6).get(2) - current.get(curr_size - 1).get(2);
                    float actual_errorX = abs(current.get(curr_size - 1).get(0) - predicted25.get(curr_size - 6).get(0));
                    float actual_errorZ = abs(current.get(curr_size - 1).get(2) - predicted25.get(curr_size - 6).get(2));
                    marginx = abs(nextPointEstimation(actual_errorX, margin25.get(curr_size-2).get(0)));
                    marginz = abs(nextPointEstimation(actual_errorZ, margin25.get(curr_size-2).get(1)));
                    margin.add(marginx);
                    margin.add(marginz);
                    margin25.add(margin);
                }
                else
                {
                    predict_diffX = 0.45f;
                    predict_diffZ = 2.5f;
                    marginx = 0.05f;
                    marginz = 0.07f;
                    marginx = 0.3f;
                    marginz = 0.3f;
                }
                float actual_diffX = current.get(curr_size - 1).get(0) - current.get(curr_size - 2).get(0);
                float actual_diffZ = current.get(curr_size - 1).get(2) - current.get(curr_size - 2).get(2);
                predictedX = nextPointEstimation(actual_diffX,predict_diffX) + current.get(curr_size - 1).get(0);
                predictedZ = nextPointEstimation(actual_diffZ,predict_diffZ)+ current.get(curr_size - 1).get(2);
                margin.add(marginx);
                margin.add(marginz);
                margin25.add(margin);
            }

            double tan_val = (double)((predictedZ-current.get(curr_size - 1).get(2))/(predictedX-current.get(curr_size - 1).get(0)));
            double angle = -Math.atan(tan_val);
            predictedValues.add(predictedX); //predicted X value
            predictedValues.add(predictedZ); //predicted Z value


            //comment
            predictedValues.add(predictedX + marginx);
            predictedValues.add(predictedZ + marginz);
            predictedValues.add(predictedX + marginx);
            predictedValues.add(predictedZ - marginz);
            predictedValues.add(predictedX - marginx);
            predictedValues.add(predictedZ - marginz);
            predictedValues.add(predictedX - marginx);
            predictedValues.add(predictedZ + marginz);





            float[] rotated = rotate_point(angle, marginx,marginz);
            predictedValues.add(predictedX + rotated[0]); //Confidence area X coordinate 1
            predictedValues.add(predictedZ + rotated[1]); //Confidence area Y coordinate 1

            rotated = rotate_point(angle, marginx,-marginz);
            predictedValues.add(predictedX + rotated[0]); //Confidence area X coordinate 2
            predictedValues.add(predictedZ + rotated[1]); //Confidence area Y coordinate 2

            rotated = rotate_point(angle, -marginx,-marginz);
            predictedValues.add(predictedX + rotated[0]); //Confidence area  X coordinate 3
            predictedValues.add(predictedZ + rotated[1]); //Confidence area Y coordinate 3

            rotated = rotate_point(angle, -marginx,marginz);
            predictedValues.add(predictedX + rotated[0]); //Confidence area X coordinate 4
            predictedValues.add(predictedZ + rotated[1]); //Confidence area Y coordinate 4



        }
        else {
            predictedValues.add(0f);
            predictedValues.add(0f);
            predictedValues.add(0f);
            predictedValues.add(0f);
            predictedValues.add(0f);
            predictedValues.add(0f);
            predictedValues.add(0f);
            predictedValues.add(0f);
            predictedValues.add(0f);
            predictedValues.add(0f);
        }
        return predictedValues;
    } */

    private float distanceToObj(ArrayList<Float> coord)
    {
        float distance;
        if(coord.get(0) == 0 && coord.get(2) == 0)
            distance = 0f;
        else
            distance = (float)Math.sqrt(Math.pow(objX - coord.get(0), 2) + Math.pow((objZ - coord.get(2)),2));
        return distance;
    }
    private float[] predictDistances(Pose newPose)
    {
        float[] distances = new float[20];
        for(int i = 0; i < 20; i++)
        {
            distances[i] = (float)Math.sqrt((Math.pow((renderArray[i].baseAnchor.getWorldPosition().x - newPose.tx()), 2)
                    + Math.pow((renderArray[i].baseAnchor.getWorldPosition().y - newPose.ty()), 2)
                    + Math.pow((renderArray[i].baseAnchor.getWorldPosition().z - newPose.tz()), 2)));
        }
        return distances;
    }

    private float area_tri(float x1, float y1, float x2, float y2, float x3, float y3)
    {
        return (float)Math.abs((x1*(y2-y3) + x2*(y3-y1)+ x3*(y1-y2))/2.0);
    }

    private ArrayList<Float> check_rect(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, float x, float y)
    {
        ArrayList<Float> bool_val;
        /* Calculate area of rectangle ABCD */
        float A = area_tri(x1, y1, x2, y2, x3, y3) + area_tri(x1, y1, x4, y4, x3, y3);

        /* Calculate area of triangle PAB */
        float A1 = area_tri(x, y, x1, y1, x2, y2);

        /* Calculate area of triangle PBC */
        float A2 = area_tri(x, y, x2, y2, x3, y3);

        /* Calculate area of triangle PCD */
        float A3 = area_tri(x, y, x3, y3, x4, y4);

        /* Calculate area of triangle PAD */
        float A4 = area_tri(x, y, x1, y1, x4, y4);

        /* Check if sum of A1, A2, A3 and A4  is same as A */
        float sum = A1 + A2 + A3 + A4;
        if(Math.abs(A - sum) < 1e-3)
            bool_val = new ArrayList<Float>(Arrays.asList(1f, A));
        else
            bool_val = new ArrayList<Float>(Arrays.asList(0f, A));

        return bool_val;
    }

    private void errorAnalysis2(int size)
    {
        float area = 0f;
        for(int i = 0; i < size - 5; i++) {
            for (int k = 0; k < 5; k++) {

                booleanmap.get(k).add(check_rect(prmap.get(k).get(i).get(2), prmap.get(k).get(i).get(3), prmap.get(k).get(i).get(4), prmap.get(k).get(i).get(5),
                        prmap.get(k).get(i).get(6), prmap.get(k).get(i).get(7), prmap.get(k).get(i).get(8), prmap.get(k).get(i).get(9),
                        current.get(i + 1 + k).get(0), current.get(i + 1+ k).get(2)));

                /*bool10.add(check_rect(predicted10.get(i).get(2), predicted10.get(i).get(3), predicted10.get(i).get(4), predicted10.get(i).get(5),
                        predicted10.get(i).get(6), predicted10.get(i).get(7), predicted10.get(i).get(8), predicted10.get(i).get(9),
                        current.get(i + 2).get(0), current.get(i + 2).get(2)));

                bool15.add(check_rect(predicted15.get(i).get(2), predicted15.get(i).get(3), predicted15.get(i).get(4), predicted15.get(i).get(5), predicted15.get(i).get(6),
                        predicted15.get(i).get(7), predicted15.get(i).get(8), predicted15.get(i).get(9),
                        current.get(i + 3).get(0), current.get(i + 3).get(2)));

                bool20.add(check_rect(predicted20.get(i).get(2), predicted20.get(i).get(3), predicted20.get(i).get(4), predicted20.get(i).get(5), predicted20.get(i).get(6),
                        predicted20.get(i).get(7), predicted20.get(i).get(8), predicted20.get(i).get(9),
                        current.get(i + 4).get(0), current.get(i + 4).get(2)));

                bool25.add(check_rect(predicted25.get(i).get(2), predicted25.get(i).get(3), predicted25.get(i).get(4), predicted25.get(i).get(5), predicted25.get(i).get(6),
                        predicted25.get(i).get(7), predicted25.get(i).get(8), predicted25.get(i).get(9),
                        current.get(i + 5).get(0), current.get(i + 5).get(2))); */
            }
        }
    }


    private boolean isObjectVisible(Vector3 worldPosition)
    {
        float[] var2 = new float[16];
        Frame frame = fragment.getArSceneView().getArFrame();
        Camera camera = frame.getCamera();

        float[] projmtx = new float[16];
        camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

        float[] viewmtx = new float[16];
        camera.getViewMatrix(viewmtx, 0);
        Matrix.multiplyMM(var2,0,projmtx,0, viewmtx, 0);

        float var5= worldPosition.x;
        float var6 = worldPosition.y;
        float var7 = worldPosition.z;

        float var8 = var5 * var2[3]+ var6 * var2[7] + var7 * var2[11] + 1.0f * var2[15];
        if (var8 < 0f) {
            return false;
        }

        Vector3 var9 = new Vector3();
        var9.x = var5 * var2[0] + var6 * var2[4] + var7 * var2[8] + 1.0f * var2[12];
        var9.x = var9.x / var8;
        if (var9.x < -1f || var9.x > 1f) {
            return false;
        }

        var9.y = var5 * var2[1] + var6 * var2[5] + var7 * var2[9] + 1.0f * var2[13];
        var9.y = var9.y / var8;
        if (var9.y < -1f || var9.y > 1f) {
            return false;
        }

        return true;
    }

}
