
package com.arcore.MixedAIAR;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

public class dataCol implements Runnable {


    private final MainActivity mInstance;
    float ref_ratio=0.5f;
    int objC;
    float sensitivity[] ;
    float tris_share[];
    Map <Integer, Float> candidate_obj;
    float []coarse_Ratios=new float[]{1f,0.8f, 0.6f , 0.4f, 0.2f, 0.05f};
    //ArrayList <ArrayList<Float>> F_profit= new ArrayList<>();
    float [][]fProfit;
    float [][] tRemainder;
    int [][] track_obj;
    //float candidate_obj[] = new float[total_obj];
    float tMin[] ;

    public dataCol(MainActivity mInstance) {

        this.mInstance = mInstance;

        objC=mInstance.obj_count;
        sensitivity = new float[objC];
        tris_share = new float[objC];


        //ArrayList <ArrayList<Float>> F_profit= new ArrayList<>();
        fProfit= new float[objC][coarse_Ratios.length];
        tRemainder= new float[objC][coarse_Ratios.length];
        track_obj= new int[objC][coarse_Ratios.length];
        //float candidate_obj[] = new float[total_obj];
        tMin = new float[objC];


    }

    @Override
    public void run() {

        boolean accmodel=true;// this is to check if the trained model for thr is accurate
        //boolean accRe=true;// this is to check if the trained model for re is accurate
        boolean trainedTris=false;
        boolean trainedThr=false;
        boolean trainedRE=false;
        double maxTrisThreshold = mInstance.orgTrisAllobj;
        double minTrisThreshold=maxTrisThreshold * mInstance.coarse_Ratios[mInstance.coarse_Ratios.length-1];
        double meanThr, totTris;
        double meanDk = 0; // mean current dis
        double meanDkk = 0; // mean of d in the next period-> you need to cal the average of predicted d for all the objects

        meanThr =mInstance.getThroughput();
        meanThr= (double)Math.round(meanThr * 100) / 100;
        totTris = mInstance.total_tris;
                ///1000;
        for (int i = 0; i < mInstance.objectCount; i++) {
            meanDk += (double) mInstance.renderArray[i].return_distance();
            meanDkk += mInstance.predicted_distances.get(i).get(0); //  // gets the first time, next 2s-- 3s of every object, ie. d1 of every obj
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
                    int startTris = mInstance.totTrisList.indexOf(totTris);
                    if (mInstance.totTrisList.size() != 0 && ind != -1)
                        mInstance.totTrisList.set(ind + startTris, totTris);
                    else
                        mInstance.totTrisList.add(totTris);

                    mInstance.trisMeanThr.put(totTris, meanThr);
                    mInstance.thParamList.put(totTris, Arrays.asList(totTris, meanDk, 1.0));
                    mInstance.trisMeanDisk.put(totTris, meanDk); //removes from the head (older data) -> to then add to the tail
                    mInstance.trisMeanDiskk.put(totTris, meanDkk);


                    ListMultimap<Double, List<Double>> copythParamList = ArrayListMultimap.create(mInstance.thParamList);// take a copy to then fill it for training up to capacity of 10
                    ListMultimap<Double, Double> copytrisMeanDisk = ArrayListMultimap.create(mInstance.trisMeanDisk);// take a copy to then fill it for training up to capacity of 10
                    ListMultimap<Double, Double> copytrisMeanDiskk = ArrayListMultimap.create(mInstance.trisMeanDiskk);// take a copy to then fill it for training up to capacity of 10
                    ListMultimap<Double, Double> copytrisMeanThr = ArrayListMultimap.create(mInstance.trisMeanThr);// take a copy to then fill it for training up to capacity of 10


                      for (double curT : mInstance.trisMeanThr.keySet()) {
                    if (mInstance.trisMeanDisk.get(curT).size() < 10) {
                        int index = mInstance.trisMeanDisk.get(curT).size();
                        double mmeanTh = 0, mmeanDK = 0;

                        for (int i = 0; i < index; i++) {
                            mmeanTh += mInstance.trisMeanThr.get(curT).get(i);
                            mmeanDK += mInstance.trisMeanDisk.get(curT).get(i);
                            //mmeanDKk+=mInstance.trisMeanDiskk.get(totTris).get(i);
                        }

                        mmeanTh /= index;
                        mmeanDK /= index;
                        //   mmeanDKk/=index;

                        for (int j = index; j < 10; j++) {
                            //   copytrisMeanDisk.put(totTris, mmeanDK);
                            // copytrisMeanDiskk.put(totTris, mmeanDKk);
                            copytrisMeanThr.put(curT, mmeanTh);
                            copythParamList.put(curT, Arrays.asList(curT, mmeanDK, 1.0));

                        }
                    }// if <10
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

                mape = Math.abs((meanThr - predThr)/meanThr);
                if(mape>=0.1)
                    accmodel=false;// after training we check to see if the model is accurate to then cal next triangle


                predThr= (double)Math.round((double)predThr * 100) / 100;
                writeThr(meanThr, predThr, trainedThr);// for the urrent period



            } //  throughput model

//


            //******************  RE modeling *************
           // double sum = 0;
             double avgq = calculateMeanQuality();

            double PRoAR =(double) Math.round((avgq / mInstance.des_Q) * 100) / 100;
            double PRoAI = (double) Math.round((  meanThr / mInstance.des_Thr) * 100) / 100;
            double reMsrd = PRoAR / PRoAI;
            reMsrd= (double)Math.round(reMsrd * 100) / 100;

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

                    for (double curT : mInstance.trisMeanThr.keySet()) {

                        if (curT!=0 &&  mInstance.trisRe.get(curT).size() < 10) {
                            int index = mInstance.trisRe.get(curT).size();
                            double meanRE = 0, mmeanDK = 0, meanPrth = 0;

                            for (int i = 0; i < index; i++) {
                                meanRE += mInstance.trisRe.get(curT).get(i);
                                List<Double> reParL = mInstance.reParamList.get(curT).get(i);
                                mmeanDK += reParL.get(1);
                                meanPrth += reParL.get(2);
                            }

                            meanRE /= index;
                            mmeanDK /= index;
                            meanPrth /= index;

                            for (int j = index; j < 10; j++) {
                                copytrisRe.put(curT, meanRE);
                                copyreParamList.put(curT, Arrays.asList(curT, mmeanDK, meanPrth, 1.0));

                            }
                        }// if <10
                    }// for all the current data

                    double[] RE = copytrisRe.values().stream()
                            .mapToDouble(Double::doubleValue)
                            .toArray();

                    double[][] reRegParameters = copyreParamList.values().stream()
                            .map(l -> l.stream().mapToDouble(Double::doubleValue).toArray())
                            .toArray(double[][]::new);
                    if ( variousTris>=3 ){
                    mLinearRegression regression = new mLinearRegression(reRegParameters, RE);
                    if( ! Double.isNaN( regression.beta(0)))
                        {
                    mInstance.alphaT = regression.beta(0);
                    mInstance.alphaD = regression.beta(1);
                   mInstance.alphaH = regression.beta(2);
                    mInstance.zeta = regression.beta(3);
                    trainedRE=true;}
                    }

                }

                // current period
                double predRE= mInstance.alphaT * totTris + mInstance.alphaD * meanDk + mInstance.alphaH * predThr + mInstance.zeta;

// nill added temp
                //mInstance.odraAlg((float) totTris/2);

                mape = Math.abs((reMsrd - predRE)/reMsrd);
                if(mape>=0.1)
                    accmodel=false;// after training we check to see if the model is accurate to then cal next triangle


                predRE= (double)Math.round((double)predRE * 100) / 100;

         //  if (variousTris>=3 && Math.abs(deltaRe) >= 0.2 && (PRoAR < 0.7 || PRoAI < 0.7))// test and see what is the re range
                double nextTris=totTris;
                 double algNxtTris=totTris; // the accurate triangle count after running the reassignment algorithm

                if (variousTris>=3 && (reMsrd >= 1.2 || (reMsrd <=0.8 && avgq!=1)) )// if re is not balances (or pAR is not close to PAI, we will change the next tris count
                  // the last cond (reMsrd <0.8 && avgq!=1) says that if the AI is working better than AR and AI has not in original quality so that we can increase tot tris
                    mInstance.lastConscCounter++;

                else
                    mInstance.lastConscCounter=0;
// now we calculate next period tris here :)
                if(accmodel  && mInstance.lastConscCounter>=5 ) // the second condition is to skip change in nexttris for the first loop while we just had a change in tot tris
                     {

                        double nomin = 1 -((mInstance.alphaD + (mInstance.alphaH* mInstance.rohD))* meanDkk)
                                - (mInstance.zeta + (mInstance.alphaH* mInstance.delta));
                        double denom = mInstance.alphaT + (mInstance.rohT * mInstance.alphaH); //α + ργ
                         double   tmpnextTris=  ( nomin/denom);


                         // temporarily inactive to not to run algo-> just wanna check nexttris values
                         if (tmpnextTris < mInstance.orgTrisAllobj && tmpnextTris > minTrisThreshold ) { // update next tris and call algorithm if and only if the new tris is between a correct range


                             writeNextTris(mInstance.alphaD, mInstance.alphaH, mInstance.rohD, meanDkk, mInstance.zeta, mInstance.delta,
                                     mInstance.alphaT, mInstance.rohT, nomin, denom, totTris, nextTris);// writes to the file

                             nextTris=tmpnextTris;
                             trainedTris = true;

                             // call main algorithm to distribute triangles
                          odraAlg((float) nextTris);
                            algNxtTris= mInstance.total_tris;
                            // algNxtTris= (double)Math.round((double)algNxtTris * 1000) / 1000;

                         }


                    }//if
                writeRE(reMsrd, predRE, trainedRE,totTris, nextTris,algNxtTris,trainedTris, PRoAR, PRoAI);// writes to the file

              //  writeRE(reMsrd, predRE, trainedRE,totTris, nextTris,trainedTris, PRoAR, PRoAI);// writes to the file


            }            //  RE modeling and next tris
        }// if we have objs on the screen, we start RE model & training

mInstance.prevtotTris=totTris;

}//run


   public void cleanOutArraysRE(double totTris, double meanDk, MainActivity mInstance){

       int index;
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

      // return index;
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


   // public void writeRE(double realRe, double predRe, boolean trainedFlag,double totT, double nextT, boolean trainedT, double pAR, double pAI){
        public void writeRE(double realRe, double predRe, boolean trainedFlag,double totT, double nextT,double algTris, boolean trainedT, double pAR, double pAI){

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
             sb.append(',');  sb.append(algTris);
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



    void odraAlg(float tUP) {


        candidate_obj = new HashMap<>();
        Map<Integer, Float> sortedcandidate_obj = new HashMap<>();
        float sum_org_tris = 0; // sum of all tris of the objects o the screen

        for (int ind = 0; ind < mInstance.objectCount; ind++) {

            sum_org_tris += mInstance.renderArray[ind].orig_tris;// this will ne used to cal min of tris needed at each row (object) in bellow


            float curtris = mInstance.renderArray[ind].orig_tris * mInstance.ratioArray[ind];
            float r1 = mInstance.ratioArray[ind]; // current object decimation ratio
            float r2 = ref_ratio * r1; // wanna compare obj level of sensitivity to see if we decimate object more -> to (ref *curr) ratio, would the current object hurt more than the other ones?

            int indq = mInstance.excelname.indexOf(mInstance.renderArray[ind].fileName);// search in excel file to find the name of current object and get access to the index of current object
            // excel file has all information for the degredation model
            float gamma = mInstance.excel_gamma.get(indq);
            float a = mInstance.excel_alpha.get(indq);
            float b = mInstance.excel_betta.get(indq);
            float c = mInstance.excel_c.get(indq);
            float d_k = mInstance.renderArray[ind].return_distance();// current distance

            float tmper1 = Calculate_deg_er(a, b, c, d_k, gamma, r1); // deg error for current sit
            float tmper2 = Calculate_deg_er(a, b, c, d_k, gamma, r2); // deg error for more decimated obj

            if (tmper2 < 0)
                tmper2 = 0;

            //Qi−Qi,r divided by Ti(1−Rr) = (1-er1) - (1-er2) / ....
            sensitivity[ind] = (abs(tmper2 - tmper1) / (curtris - (ref_ratio * curtris)));
            tris_share[ind] = (curtris / tUP);
            candidate_obj.put(ind, sensitivity[ind] / tris_share[ind]);


        }
        sortedcandidate_obj = sortByValue(candidate_obj, false); // second arg is for order-> ascending or not? NO
        // Up to here, the candidate objects are known


        float updated_sum_org_tris = sum_org_tris; // keeps the last value which is sum_org_tris - tris1-tris2-....
        for (int i : sortedcandidate_obj.keySet()) { // check this gets the candidate object index to calculate min weight
            float sum_org_tris_minus = updated_sum_org_tris - mInstance.renderArray[i].orig_tris; // this is summ of tris for all the objects except the current one
            updated_sum_org_tris = sum_org_tris_minus;
            tMin[i] = coarse_Ratios[coarse_Ratios.length - 1] * sum_org_tris_minus;// minimum tris needs for object i+1 to object n
            ///@@@@ if this line works lonely, delete the extra line for the last object to zero in the alg
        }

        Map.Entry<Integer, Float> entry = sortedcandidate_obj.entrySet().iterator().next();
        int key = entry.getKey(); // get access to the first key -> to see if it is the first object for bellow code

        int prevInd = 0;
        for (int i : sortedcandidate_obj.keySet()){  // line 10 i here is equal to alphai -> the obj with largest candidacy
            // check this gets the candidate object index to maintain its quality
            for (int j = 0; j < coarse_Ratios.length; j++) {

                int indq = mInstance.excelname.indexOf(mInstance.renderArray[i].fileName);// search in excel file to find the name of current object and get access to the index of current object
                float gamma =mInstance. excel_gamma.get(indq);
                float a =mInstance. excel_alpha.get(indq);
                float b = mInstance.excel_betta.get(indq);
                float c =mInstance. excel_c.get(indq);
                float d_k = mInstance.renderArray[i].return_distance();// current distance

                float quality = 1 - Calculate_deg_er(a, b, c, d_k, gamma, coarse_Ratios[j]); // deg error for current sit

                if (i == key && tUP >= mInstance.renderArray[i].getOrg_tris() * coarse_Ratios[j]) { // the first object in the candidate list
                    fProfit[i][j] = quality;// Fα(i),j ←Qα(i),j -> i is alpha i
                    tRemainder[i][j] = tUP - (mInstance.renderArray[i].getOrg_tris() * coarse_Ratios[j]);
                } else //  here is the dynamic programming section
                    for (int s = 0; s < coarse_Ratios.length; s++) {

                        float f = fProfit[prevInd][s] + quality;
                        float t = tRemainder[prevInd][s] - (mInstance.renderArray[i].getOrg_tris() * coarse_Ratios[j]);
                        if (t >= tMin[i] && fProfit[i][j] < f) {

                            fProfit[i][j] = f;
                            tRemainder[i][j] = t;
                            track_obj[i][j] = s;
                        }

                    }//

            }//for j  up to here we reach line 25
            prevInd=i;
        }// for i
/// start with object with least priority

        sortedcandidate_obj = sortByValue(candidate_obj, true); // to iterate through the list from lowest to highest values

        int lowPobjIndx = sortedcandidate_obj.entrySet().iterator().next().getKey(); // line 26
        float tmp=fProfit[lowPobjIndx][0];
        int j=0;
        for  (int maxindex=1;maxindex<coarse_Ratios.length;maxindex++) // line 27
            if(fProfit[lowPobjIndx][maxindex]>tmp)// finds the index of coarse-grain ratio with maximum profit
            {
                tmp = fProfit[lowPobjIndx][maxindex];
                j=maxindex;
            }


        for (int i : sortedcandidate_obj.keySet()) {

            mInstance.total_tris = mInstance.total_tris - (mInstance.ratioArray[i] * mInstance.o_tris.get(i));// total =total -1*objtris
            mInstance.ratioArray[i] = coarse_Ratios[j];

            mInstance.runOnUiThread(() -> mInstance.renderArray[i].decimatedModelRequest(mInstance.ratioArray[i], i, false));
            //Thread.sleep(3);

            mInstance.total_tris = mInstance.total_tris + (mInstance.ratioArray[i] *  mInstance.renderArray[i].orig_tris);// total = total + 0.8*objtris
            j = track_obj[i][j];

        }



    }


    private static Map<Integer, Float> sortByValue(Map<Integer, Float> unsortMap, final boolean order)
    {
        List<Map.Entry<Integer, Float>> list = new LinkedList<>(unsortMap.entrySet());

        // Sorting the list based on values
        list.sort((o1, o2) -> order ? o1.getValue().compareTo(o2.getValue()) == 0
                ? o1.getKey().compareTo(o2.getKey())
                : o1.getValue().compareTo(o2.getValue()) : o2.getValue().compareTo(o1.getValue()) == 0
                ? o2.getKey().compareTo(o1.getKey())
                : o2.getValue().compareTo(o1.getValue()));
        return list.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));

    }




}



