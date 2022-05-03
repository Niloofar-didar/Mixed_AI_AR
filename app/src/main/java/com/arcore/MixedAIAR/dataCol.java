
package com.arcore.MixedAIAR;

import android.util.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class dataCol implements Runnable {


    private final MainActivity mInstance;


    public dataCol(MainActivity mInstance) {

        this.mInstance = mInstance;

    }

    public void writeOutput(double throughput) {
        int size = MainActivity.mList.size();
        SimpleDateFormat format=new SimpleDateFormat("HH.mm.ss.SSSS", Locale.getDefault());
        String currentFolder = mInstance.getExternalFilesDir(null).getAbsolutePath();
        String FILEPATH = currentFolder + File.separator + "data/" +size+"_"+"throughput.csv";

        try (PrintWriter writer = new PrintWriter(new FileOutputStream(FILEPATH, true))) {
            StringBuilder sb = new StringBuilder();
            sb.append(throughput);
            sb.append('\n');
            writer.write(sb.toString());
            System.out.println("done!");
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run() {



        boolean trainedTris=false;
        boolean trainedThr=false;
        boolean trainedRE=false;
        double maxTrisThreshold = mInstance.orgTrisAllobj;
        double minTrisThreshold=maxTrisThreshold * mInstance.coarse_Ratios[mInstance.coarse_Ratios.length-1];
        double meanThr, totTris;
        double meanDk = 0; // mean current dis
        double meanDkk = 0; // mean of d in the next period-> you need to cal the average of predicted d for all the objects

        meanThr = MainActivity.getThroughput();

        Log.d("Throughput", String.valueOf(meanThr));
        writeOutput(meanThr);
        meanThr= (double)Math.round((double)meanThr * 100) / 100;
        totTris = mInstance.total_tris/1000;
        for (int i = 0; i < mInstance.objectCount; i++) {
            meanDk += (double) mInstance.renderArray[i].return_distance();
            meanDkk += mInstance.predicted_distances.get(i).get(0); //  // gets the first time, next 1s of every object, ie. d1 of every obj
        }

        meanDk /= mInstance.objectCount;
        meanDkk /= mInstance.objectCount;
        meanDk = (double) (Math.round((double) (100 * meanDk))) / 100;
        meanDkk = (double) (Math.round((double) (100 * meanDkk))) / 100;

        if(meanDkk==0)
            meanDkk=meanDk;

        int variousTris = mInstance.trisMeanThr.keySet().size();


// nill added 8 april
        int ind=-1;
        if(variousTris < 3) {

           if( mInstance.trisMeanThr.get(totTris).size() == 10) { // we keep inf of last 10 points

              ind= cleanOutArraysThr(totTris, meanDk, mInstance);// cleans out the closest data to the curr one

           }

            mInstance.trisMeanThr.put(totTris, meanThr);
            mInstance.thParamList.put(totTris, Arrays.asList(totTris, meanDk, 1.0));
            int startTris=mInstance.totTrisList.indexOf(totTris);
            if(mInstance.totTrisList.size()!=0 && ind!=-1)
                mInstance.totTrisList.set(ind+startTris,totTris);
            else
                mInstance.totTrisList.add(totTris);
            mInstance.trisMeanDisk.put(totTris, meanDk); //removes from the head (older data) -> to then add to the tail
            mInstance.trisMeanDiskk.put(totTris, meanDkk);


        }


        if (mInstance.objectCount != 0) {//to avoid wrong inf from tris=0 -> we won't have re or distance at this situation

            int size = mInstance.trisMeanThr.size();// total points regardless of points with similar tris
            // starting throughput model
            if (variousTris >= 2) {// at least two points to start modeling


// checks error of the model after new added model
                double mape = 0.0; // mean of absolute error
                double fit = mInstance.rohT * totTris + mInstance.rohD * meanDk + mInstance.delta;
                //double fit = mInstance.thSlope * totTris + mInstance.thIntercept;;
                mape = Math.abs((meanThr - fit)/meanThr);

                ind=-1;
                if ( mInstance.trisMeanThr.get(totTris).size() == 10)
                { // we keep inf of last 10 points
                   ind= cleanOutArraysThr(totTris, meanDk, mInstance);// cleans out the closest data to the curr one

                }


                if (mape >= 0.1 && variousTris>=2  ) {// we ignore tris=0 them we need points with at least two diff tris in order to generate the line
// 8 april
                    // first delete if the bin is full -> look for the index which has the distance very close to the current distance
                   // int tListSize = mInstance.totTrisList.size();
                    int startTris=mInstance.totTrisList.indexOf(totTris);
                    if(mInstance.totTrisList.size()!=0 && ind!=-1)
                        mInstance.totTrisList.set(ind+startTris,totTris);
                    else
                        mInstance.totTrisList.add(totTris);

                    mInstance.trisMeanThr.put(totTris, meanThr);
                    mInstance.thParamList.put(totTris, Arrays.asList(totTris, meanDk, 1.0));
                    mInstance.trisMeanDisk.put(totTris, meanDk); //removes from the head (older data) -> to then add to the tail
                    mInstance.trisMeanDiskk.put(totTris, meanDkk);



                    ListMultimap<Double, List<Double>> copythParamList= ArrayListMultimap.create(mInstance.thParamList);// take a copy to then fill it for training up to capacity of 10
                    ListMultimap<Double, Double> copytrisMeanDisk= ArrayListMultimap.create(mInstance.trisMeanDisk);// take a copy to then fill it for training up to capacity of 10
                    ListMultimap<Double, Double> copytrisMeanDiskk= ArrayListMultimap.create(mInstance.trisMeanDiskk);// take a copy to then fill it for training up to capacity of 10
                    ListMultimap<Double, Double> copytrisMeanThr= ArrayListMultimap.create(mInstance.trisMeanThr);// take a copy to then fill it for training up to capacity of 10

                    if(mInstance.trisMeanDisk.get(totTris).size()<10){
                        int index=mInstance.trisMeanDisk.get(totTris).size();
                        double mmeanTh=0, mmeanDK=0;

                        for (int i=0; i<index;i++){
                            mmeanTh+=mInstance.trisMeanThr.get(totTris).get(i);
                            mmeanDK+=mInstance.trisMeanDisk.get(totTris).get(i);
                            //mmeanDKk+=mInstance.trisMeanDiskk.get(totTris).get(i);
                        }

                        mmeanTh/=index;
                        mmeanDK/=index;
                     //   mmeanDKk/=index;

                        for (int j=index; j<10; j++) {
                         //   copytrisMeanDisk.put(totTris, mmeanDK);
                           // copytrisMeanDiskk.put(totTris, mmeanDKk);
                            copytrisMeanThr.put(totTris, mmeanTh);
                            copythParamList.put(totTris, Arrays.asList(totTris, mmeanDK, 1.0));

                        }
                    }




                    double[] throughput = copytrisMeanThr.values().stream()
                            .mapToDouble(Double::doubleValue)
                            .toArray();

                    double[] y = Arrays.copyOfRange(throughput, 0, throughput.length);
                    double[][] thRegParameters = copythParamList.values().stream()
                            .map(l -> l.stream().mapToDouble(Double::doubleValue).toArray())
                            .toArray(double[][]::new);

                    mLinearRegression regression = new mLinearRegression(thRegParameters, y);
                    mInstance.rohT = regression.beta(0);
                    mInstance.rohD=regression.beta(1);
                    mInstance.delta = regression.beta(2);
                    mInstance.thRmse = regression.rmse;
                    trainedThr=true;
                }

                double predThr=  mInstance.rohT * totTris + mInstance.rohD * meanDk + mInstance.delta;


                predThr= (double)Math.round((double)predThr * 100) / 100;
                writeThr(meanThr, predThr, trainedThr);// for the urrent period

//@@@@@@@@@nill added temporarary 29 march


            } //  throughput model

//


            //******************  RE modeling *************
            double sum = 0;
             double avgq = calculateMeanQuality();

            double PRoAR =(double) Math.round((double)(avgq / mInstance.des_Q) * 100) / 100;
            double PRoAI = (double) Math.round((double)(  meanThr / mInstance.des_Thr) * 100) / 100;
            double reMsrd = PRoAR / PRoAI;
            reMsrd= (double)Math.round((double)reMsrd * 100) / 100;

           // double predThr=  mInstance.thSlope * totTris + mInstance.thIntercept;
            double predThr=  mInstance.rohT * totTris + mInstance.rohD * meanDk + mInstance.delta;

            predThr= (double)Math.round((double)predThr * 100) / 100;// uses predicted thr in current period to model it based on current measured RE
            int reModSize = mInstance.trisRe.size();

            if(reModSize < 4)// april 8{
            {

                cleanOutArraysRE(totTris,meanDk, mInstance);// check to remove extra value in the RE parameters list
                mInstance.trisRe.put(totTris, reMsrd);
                mInstance.reParamList.put(totTris, Arrays.asList(totTris, meanDk, predThr, 1.0));
            }


            if (reModSize >= 4) { // ignore first 10 point we need to have four known variables to solve an equation with three unknown var
//@@ niloo please add test the trained data and check rmse, if it is above 20% , then retrain

                double mape = 0.0;      //  sum of square error
                double fit = mInstance.alphaT * totTris + mInstance.alphaD * meanDk + mInstance.alphaH * predThr + mInstance.zeta;// for current period
                mape = Math.abs((reMsrd - fit)/reMsrd);

                if (mape >= 0.10 && variousTris>=2 ) {// we ignore tris=0 them we need points with at least two diff tris in order to generate the line

                    cleanOutArraysRE(totTris,meanDk, mInstance);
                    mInstance.trisRe.put(totTris, reMsrd); // april 8
                    mInstance.reParamList.put(totTris, Arrays.asList(totTris, meanDk, predThr, 1.0));

                    ListMultimap<Double, List<Double>> copyreParamList= ArrayListMultimap.create(mInstance.reParamList);// take a copy to then fill it for training up to capacity of 10
                    ListMultimap<Double, Double> copytrisRe= ArrayListMultimap.create(mInstance.trisRe);// take a copy to then fill it for training up to capacity of 10

                    if(mInstance.trisRe.get(totTris).size()<10){
                        int index=mInstance.trisRe.get(totTris).size();
                        double meanRE=0, mmeanDK=0, meanPrth=0;

                        for (int i=0; i<index;i++){
                            meanRE+=mInstance.trisRe.get(totTris).get(i);
                            List<Double> reParL=mInstance.reParamList.get(totTris).get(i);
                            mmeanDK+=reParL.get(1);
                            meanPrth+=reParL.get(2);
                        }

                        meanRE/=index;
                        mmeanDK/=index;
                        meanPrth/=index;

                        for (int j=index; j<10; j++) {
                            copytrisRe.put(totTris, meanRE);
                            copyreParamList.put(totTris, Arrays.asList(totTris, mmeanDK, meanPrth, 1.0));

                        }
                    }

                    double[] RE = copytrisRe.values().stream()
                            .mapToDouble(Double::doubleValue)
                            .toArray();

                    double[][] reRegParameters = copyreParamList.values().stream()
                            .map(l -> l.stream().mapToDouble(Double::doubleValue).toArray())
                            .toArray(double[][]::new);
                    if ( variousTris>=3 ){
                    mLinearRegression regression = new mLinearRegression(reRegParameters, RE);
                    mInstance.alphaT = regression.beta(0);
                    mInstance.alphaD = regression.beta(1);
                   mInstance.alphaH = regression.beta(2);
                    mInstance.zeta = regression.beta(3);
                    trainedRE=true;}

                }

                // current period
                double predRE= mInstance.alphaT * totTris + mInstance.alphaD * meanDk + mInstance.alphaH * predThr + mInstance.zeta;

                predRE= (double)Math.round((double)predRE * 100) / 100;


              //  double deltaRe = 1.0 - predRE;

                  //  if (variousTris>=3 && Math.abs(deltaRe) >= 0.2 && (PRoAR < 0.7 || PRoAI < 0.7))// test and see what is the re range
                double nextTris=totTris;

                if (variousTris>=3 && (reMsrd > 1.2 || (reMsrd <0.8 && avgq!=1)) )// if re is not balances (or pAR is not close to PAI, we will change the next tris count
                  // the last cond (reMsrd <0.8 && avgq!=1) says that if the AI is working better than AR and AI has not in original quality so that we can increase tot tris
                    mInstance.lastConscCounter++;

                else
                    mInstance.lastConscCounter=0;
// now we calculate next period tris here :)
                   if(mInstance.lastConscCounter>=5 && mInstance.prevtotTris==totTris) // the second condition is to skip change in nexttris for the first loop while we just had a change in tot tris
                     {

                        double nomin = 1 -((mInstance.alphaD + (mInstance.alphaH* mInstance.rohD))* meanDkk)
                                - (mInstance.zeta + (mInstance.alphaH* mInstance.delta));
                        double denom = mInstance.alphaT + (mInstance.rohT * mInstance.alphaH); //α + ργ
                         nextTris=  ( nomin/denom);

                         nextTris= (double)Math.round((double)nextTris * 1000) / 1000;

                         writeNextTris( mInstance.alphaD,mInstance.alphaH,mInstance.rohD, meanDkk,mInstance.zeta, mInstance.delta,
                                 mInstance.alphaT,mInstance.rohT, nomin, denom,totTris,nextTris );// writes to the file

                           trainedTris=true;


/// this id for test


                       //  double rePlus= mInstance.reRegalpha * nextTris + mInstance.reRegbetta * meanDkk + mInstance.reReggamma * thrPlus + mInstance.reRegteta;

/// this id for test


                        /* temporarily inactive to not to run algo-> just wanna check nexttris values
                        if (nextTris <= mInstance.orgTrisAllobj && nextTris > minTrisThreshold && Math.abs(totTris-nextTris)>5000) { // update next tris and call algorithm if and only if the new tris is between a correct range
                            mInstance.nextTris = nextTris;// update nexttris
                            // call main algorithm to distribute triangles
                            mInstance.odraAlg((float) nextTris);

                        } */
                    }//if

                writeRE(reMsrd, predRE, trainedRE,totTris, nextTris,trainedTris, PRoAR, PRoAI);// writes to the file


            }            //  RE modeling and next tris
        }// if we have objs on the screen, we start RE model & training

mInstance.prevtotTris=totTris;

}//run


   public int cleanOutArraysRE(double totTris, double meanDk, MainActivity mInstance){

       int index=0;
       if ( mInstance.trisRe.get(totTris).size() == 10)
       { // we keep inf of last 10 points
           double []disArray= mInstance.trisMeanDisk.get(totTris).stream()
                   .mapToDouble(Double::doubleValue)
                   .toArray();
            index= findClosest(disArray , meanDk);// the index of value needed to be deleted
           // since we add if objcount != zero to avoid wrong inf from tris=0 -> we won't have re or distance at this situation
           mInstance.trisRe.get(totTris).remove(index);
           mInstance.reParamList.get(totTris).remove(index);
       }

       return index;
   }




    public int cleanOutArraysThr(double totTris, double meanDk, MainActivity mInstance){


        double []disArray= mInstance.trisMeanDisk.get(totTris).stream()
                .mapToDouble(Double::doubleValue)
                .toArray();

        int index= findClosest(disArray , meanDk);// the index of value needed to be deleted
        mInstance.trisMeanThr.get(totTris).remove(index);
        mInstance.thParamList.get(totTris).remove(index);
      //  mInstance.totTrisList.remove(index); // 8 april
        mInstance.trisMeanDisk.get(totTris).remove(index); //removes from the head (older data) -> to then add to the tail
        mInstance.trisMeanDiskk.get(totTris).remove(index);
         return  index;
    }


    public static int findClosest(double[] arr, double target) {
        int idx = 0;
        double dist = Math.abs(arr[0] - target);

        for (int i = 1; i< arr.length; i++) {
            double cdist = Math.abs(arr[i] - target);

            if (cdist < dist) {
                idx = i;
                dist = cdist;
            }
        }

        return idx;
    }


    public void writeThr(double realThr, double predThr, boolean trainedFlag){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        String currentFolder = mInstance.getExternalFilesDir(null).getAbsolutePath();
        String FILEPATH = currentFolder + File.separator + "Throughput.csv";

        try (PrintWriter writer = new PrintWriter(new FileOutputStream(FILEPATH, true))) {
            StringBuilder sb = new StringBuilder();
            sb.append(dateFormat.format(new Date())); sb.append(',');
           sb.append(realThr);sb.append(',');
            sb.append(predThr);
            sb.append(',');  sb.append(trainedFlag);
            sb.append('\n');
            writer.write(sb.toString());
            System.out.println("done!");
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public void writeNextTris(double alphaD, double alphaH,double rohD, double meanDkk,double zeta,double delta,
                              double  alphaT,double rohT,double nomin,double denom, double totTris, double nextTris){

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        String currentFolder = mInstance.getExternalFilesDir(null).getAbsolutePath();
        String FILEPATH = currentFolder + File.separator + "NextTrisParameters.csv";

        try (PrintWriter writer = new PrintWriter(new FileOutputStream(FILEPATH, true))) {
            StringBuilder sb = new StringBuilder();
            sb.append(dateFormat.format(new Date())); sb.append(',');
            sb.append(alphaD);sb.append(',');
            sb.append(alphaH);
            sb.append(',');  sb.append(rohD);
            sb.append(',');  sb.append(meanDkk);
            sb.append(',');  sb.append(zeta);
            sb.append(',');  sb.append(delta);
            sb.append(',');  sb.append(alphaT);
            sb.append(',');  sb.append(rohT);
            sb.append(',');  sb.append(nomin);
            sb.append(',');  sb.append(denom);
            sb.append(',');  sb.append(totTris);
            sb.append(',');  sb.append(nextTris);
            sb.append('\n');
            writer.write(sb.toString());
            System.out.println("done!");
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }


    public void writeRE(double realRe, double predRe, boolean trainedFlag,double totT, double nextT, boolean trainedT, double pAR, double pAI){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        String currentFolder = mInstance.getExternalFilesDir(null).getAbsolutePath();
        String FILEPATH = currentFolder + File.separator + "RE.csv";

        try (PrintWriter writer = new PrintWriter(new FileOutputStream(FILEPATH, true))) {
            StringBuilder sb = new StringBuilder();
            sb.append(dateFormat.format(new Date())); sb.append(',');
            sb.append(realRe);sb.append(',');
            sb.append(predRe);
            sb.append(',');  sb.append(trainedFlag);
            sb.append(',');  sb.append(totT);
            sb.append(',');  sb.append(nextT);
            sb.append(',');  sb.append(trainedT);
            sb.append(',');  sb.append(pAR);
            sb.append(',');  sb.append(pAI);
            sb.append('\n');
            writer.write(sb.toString());
            System.out.println("done!");
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }



    public float calculateMeanQuality( ) {

        float sumQual=0;
     for (int ind = 0; ind < mInstance.objectCount; ind++)
        {
            int i =  mInstance.excelname.indexOf( mInstance.renderArray[ind].fileName);
            float gamma = mInstance.excel_gamma.get(i);
            float a = mInstance.excel_alpha.get(i);
            float b = mInstance.excel_betta.get(i);
            float c = mInstance.excel_c.get(i);
            float d = mInstance.renderArray[ind].return_distance();
            float curQ = mInstance.ratioArray[ind];
            float deg_error = (float) (Math.round((float) (Calculate_deg_er(a, b, c, d, gamma, curQ) * 10000))) / 10000;
        //Nill added
             float max_nrmd = mInstance.excel_maxd.get(i);
             float cur_degerror = deg_error / max_nrmd;
             float quality= 1- cur_degerror;
             sumQual+=quality;


    }


        return sumQual/mInstance.objectCount;



    }

    public float Calculate_deg_er(float a,float b,float creal,float d,float gamma, float r1) {

        float error;
        if(r1==1)
            return  0f;
        error = (float) (((a * Math.pow(r1,2)) + (b * r1) + creal) / (Math.pow(d , gamma)));
        return error;
    }

    }



