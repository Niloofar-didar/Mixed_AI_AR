
package com.arcore.MixedAIAR;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class dataCol implements Runnable {


    private final MainActivity mInstance;


    public dataCol(MainActivity mInstance) {

        this.mInstance = mInstance;

    }

    @Override
    public void run() {




        double maxTrisThreshold = mInstance.orgTrisAllobj;
        double minTrisThreshold=maxTrisThreshold * mInstance.coarse_Ratios[mInstance.coarse_Ratios.length-1];
        double meanThr, totTris;
        double meanDk = 0; // mean current dis
        double meanDkk = 0; // mean of d in the next period-> you need to cal the average of predicted d for all the objects

        meanThr = Camera2BasicFragment.getInstance().getThr();
        totTris = mInstance.total_tris;
        for (int i = 0; i < mInstance.objectCount; i++) {
            meanDk += (double) mInstance.renderArray[i].return_distance();
            meanDkk += mInstance.predicted_distances.get(i).get(0); //  // gets the first time, next 1s of every object, ie. d1 of every obj
        }

        meanDk /= mInstance.objectCount;
        meanDkk /= mInstance.objectCount;
        meanDk = (double) (Math.round((double) (100 * meanDk))) / 100;
        meanDkk = (double) (Math.round((double) (100 * meanDkk))) / 100;
        //meanDk= (double) (Math.round( 100*meanDk  )/100);
        //meanDkk= (double) (Math.round( 100*meanDkk  )/100);

        int variousTris = mInstance.trisMeanThr.keySet().size();
        int tListSize = mInstance.totTrisList.size();
        if (tListSize != 0 && mInstance.totTrisList.get(tListSize - 1) == totTris && mInstance.trisMeanThr.get(totTris).size() == 10) { // we keep inf of last 30 points

            mInstance.trisMeanThr.get(totTris).remove(0);

            if (mInstance.objectCount != 0) {// since we add if objcount != zero to avoid wrong inf from tris=0 -> we won't have re or distance at this situation
            mInstance.trisRe.get(totTris).remove(0);
            mInstance.reParamList.get(totTris).remove(0);
            mInstance.trisMeanDisk.get(totTris).remove(0); //removes from the head (older data) -> to then add to the tail
            mInstance.trisMeanDiskk.get(totTris).remove(0);}
        } else
            mInstance.totTrisList.add(totTris);


        mInstance.trisMeanThr.put(totTris, meanThr);

        if (mInstance.objectCount != 0) {//to avoid wrong inf from tris=0 -> we won't have re or distance at this situation



            int size = mInstance.totTrisList.size();// total points regardless of points with similar tris

            // starting throughput model
            if (variousTris >= 2) {// at least two points to start modeling


                double[] tris = mInstance.totTrisList.stream()
                        .mapToDouble(Double::doubleValue)
                        .toArray();
                double[] x = Arrays.copyOfRange(tris, 0, size);

                double[] throughput = mInstance.trisMeanThr.values().stream()
                        .mapToDouble(Double::doubleValue)
                        .toArray();

                double[] y = Arrays.copyOfRange(throughput, 0, size);
// checks error of the model after new added model
                double sse = 0.0;      //  sum of square error
                double mae = 0.0; // mean of absolute error
                for (int i = 0; i < size; i++) {
                    double fit = mInstance.thSlope * x[i] + mInstance.thIntercept;
                    // sse += (fit - y[i]) * (fit - y[i]);// sum of square error
                    mae += Math.abs((y[i] - fit) / y[i]);
                }
                mae /= size;

                //  double rmse_new= Math.sqrt(sse/size);
                // double inaccuracy= (rmse_new-mInstance.thRmse)/mInstance.thRmse;
                if (mae >= 0.2) { // runs model training
                    LinearRegression lRegression = new LinearRegression(x, y);

                    mInstance.thSlope = lRegression.slope;
                    mInstance.thIntercept = lRegression.intercept;
                    mInstance.thRmse = lRegression.getRmse();
                }
            }






            mInstance.trisMeanDisk.put(totTris, meanDk); //removes from the head (older data) -> to then add to the tail
            mInstance.trisMeanDiskk.put(totTris, meanDkk);

            // starting step 2_1 and 2_2 to check and retrain RE and Thr model
            double sum = 0;
            //for (double objQ : mInstance.lastQuality)////???@@@ this should be changing based on userobj dis -> here not in main
             //   sum += objQ;
            double avgq = calculateMeanQuality();
                    //sum / mInstance.objectCount;

            double PRoAR = (avgq / mInstance.des_Q);
            double PRoAI = (meanThr / mInstance.des_Thr);
            double re = PRoAR / PRoAI;
            //double re = Math.abs(PRoAR-PRoAI);

            mInstance.trisRe.put(totTris, re);
            mInstance.reParamList.put(totTris, Arrays.asList(totTris, meanDk, meanThr, 1.0));


            int reModSize = mInstance.trisRe.size();

            if (reModSize >= 4) { // ignore first 10 point we need to have four known variables to solve an equation with three unknown var
//@@ niloo please add test the trained data and check rmse, if it is above 20% , then retrain

                double[] RE = mInstance.trisRe.values().stream()
                        .mapToDouble(Double::doubleValue)
                        .toArray();

                double[][] reRegParameters = mInstance.reParamList.values().stream()
                        .map(l -> l.stream().mapToDouble(Double::doubleValue).toArray())
                        .toArray(double[][]::new);

                double mae = 0.0;      //  sum of square error
                for (int i = 0; i < reModSize; i++) {
                    double fit = mInstance.reRegalpha * mInstance.total_tris + mInstance.reRegbetta * meanDk + mInstance.reReggamma * meanThr + mInstance.reRegteta;
                    mae += Math.abs((RE[i] - fit) / RE[i]);

                }

                mae /= reModSize;

                if (mae >= 0.2 && variousTris>=3 ) {// we ignore tris=0 them we need points with at least two diff tris in order to generate the line
                    mLinearRegression regression = new mLinearRegression(reRegParameters, RE);
                    System.out.printf("%.2f + %.2f beta1 + %.2f beta2  (R^2 = %.2f)\n",
                            regression.beta(0), regression.beta(1), regression.beta(2), regression.rmse);

                    mInstance.reRegRMSE = regression.rmse;
                    mInstance.reRegalpha = regression.beta(0);
                    mInstance.reRegbetta = regression.beta(1);
                    mInstance.reReggamma = regression.beta(2);
                    mInstance.reRegteta = regression.beta(3);

                }
                    // now we calculate next period tris here :)


                    double deltaRe = 1 - re;

                   // double rePlus= mInstance.reRegalpha * mInstance.total_tris + mInstance.reRegbetta * meanDkk + mInstance.reReggamma * thrPlus + mInstance.reRegteta;

                    if (variousTris>=3 && Math.abs(deltaRe) >= 0.2 && (PRoAR < 0.7 || PRoAI < 0.7))// test and see what is the re range
                    {

                        //double thrKK= mInstance.thSlope * x[i] + mInstance.thIntercept;
                       // double nomin = 1- (mInstance.reRegbetta * meanDkk )-( mInstance.reReggamma * mInstance.thIntercept);
                        double nomin = (mInstance.reRegbetta * meanDkk )-( mInstance.reReggamma * mInstance.thIntercept);
                                //deltaRe - (mInstance.reRegbetta * (meanDkk - meanDk)) - mInstance.reReggamma * mInstance.thIntercept + mInstance.reReggamma * meanThr + mInstance.reRegalpha * totTris;   // REdesired −REk −β(Dk+1 −Dk) - γδ + γ Thrk + α Tk
                        double denom = mInstance.reRegalpha + (mInstance.thSlope * mInstance.reReggamma); //α + ργ
                        double nextTMin = (0.53-  (mInstance.reRegbetta * meanDkk ) -( mInstance.reReggamma * mInstance.thIntercept) - mInstance.reRegteta  )/ denom;
                        double nextTMax = (2.04- (mInstance.reRegbetta * meanDkk ) -( mInstance.reReggamma * mInstance.thIntercept) - mInstance.reRegteta )/ denom;
                        double nextTMid = (1.28- (mInstance.reRegbetta * meanDkk ) -( mInstance.reReggamma * mInstance.thIntercept) - mInstance.reRegteta )/ denom;

                        double nextTris=maxTrisThreshold;

                        if(nextTMin>minTrisThreshold )
                            nextTris=nextTMin;
                        else if (nextTMid>minTrisThreshold)
                            nextTris=nextTMid;
                        else
                            nextTris=nextTMax;

/// this id for test
                        double thrPlus= mInstance.thSlope * nextTris + mInstance.thIntercept;
                        double rePlus= mInstance.reRegalpha * nextTris + mInstance.reRegbetta * meanDkk + mInstance.reReggamma * thrPlus + mInstance.reRegteta;
                       double nextPAI= (thrPlus / mInstance.des_Thr);
/// this id for test


                        if (nextTris <= mInstance.orgTrisAllobj && nextTris > minTrisThreshold && Math.abs(totTris-nextTris)>5000) { // update next tris and call algorithm if and only if the new tris is between a correct range
                            mInstance.nextTris = nextTris;// update nexttris
                            // call main algorithm to distribute triangles
                            mInstance.odraAlg((float) nextTris);

                        }
                    }//if


                // double rmse_new = Math.sqrt(sse / reModSize);
                // double inaccuracy = (rmse_new - mInstance.reRegRMSE) / mInstance.reRegRMSE;
                // if( reRegRMSE== Double.POSITIVE_INFINITY || inaccuracy>=0.2) {

            }
        }// if we have objs on the screen, we start RE model & training



}//run



    public float calculateMeanQuality( ) {

        float sumQual=0;

     for (int i = 0; i < mInstance.objectCount; i++)
        {
            float gamma = mInstance.excel_gamma.get(i);
            float a = mInstance.excel_alpha.get(i);
            float b = mInstance.excel_betta.get(i);
            float c = mInstance.excel_c.get(i);
            float d = mInstance.renderArray[i].return_distance();
            float curQ = mInstance.ratioArray[i] / 100f;
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



