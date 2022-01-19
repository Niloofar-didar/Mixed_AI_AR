package GPU_File;
import java.io.*;
import java.util.Date;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

public class Main_Affan{

    public static void main(String[] args)throws Exception{
        //System.out.println("Hello");
        File file = new File("tris_num.txt");

        BufferedReader br = new BufferedReader(new FileReader(file));
        System.out.println();

        String st;
        String[] initialSplit ={};
        String[] timeSplit = {};
        int counterTris = 0;
        int counterPercentage = 0;


        int index = 0;
        Path path = Paths.get("tris_num.txt");
        long lineCount = Files.lines(path).count();
        System.out.println("lines is :"+ lineCount);
        
        
        double[] finalTime = new double[(int) lineCount +1];
        String[] finalTris = new String[(int) lineCount+1 ];

        String[] stringTime = new String[(int) lineCount +1];
       

        
    

        int finalTimeCounter = 0;
        int finalTrisCounter = 1;


        double temp = 0;
        finalTris[0] = "0";


        while ((st = br.readLine()) != null){


            //splitting the strings into individual components
            initialSplit = st.split(" "); //splitting the line based off whitespace
            timeSplit = initialSplit[1].split(":"); //splitting the time based off :


            temp = (Double.parseDouble(timeSplit[0]) + Double.parseDouble(timeSplit[1]) + Double.parseDouble(timeSplit[2]));
            finalTime[finalTimeCounter] = temp;
            finalTris[finalTrisCounter] = initialSplit[5];
            stringTime[finalTrisCounter-1] =  initialSplit[0]+" "+initialSplit[1];


            System.out.println("FINAL: " +"initial split 1  "+ stringTime[finalTrisCounter]+ " temp : "+ temp + "    " + timeSplit[0] + " " + timeSplit[1] + " " + timeSplit[2] + " " + timeSplit[3] + " " + initialSplit[5]);

            //System.out.println("Index: " + index + "    -   Time: " + initialSplit[1] + "     -   num of tris: " + initialSplit[5]);
            index++;
            finalTimeCounter++;
            finalTrisCounter++;
            counterTris++;
        }




         index = 0;
         file = new File("myfile.txt");
         br = new BufferedReader(new FileReader(file));

         Path path2 = Paths.get("myfile.txt");
         long lineCount2 = Files.lines(path2).count();
         
        String[] firstSplit = {};
        double[] listOfPercent = new double[(int) ((lineCount2/2) +1)];
        double[] listOfTime = new double[(int) ((lineCount2/2) +1)];
        String[] stringTime2 = new String[(int) ((lineCount2/2)) ];
        
        
        int listOfPercentCounter = 0;
        int listOfTimeCounter = 0;

        String[] timeSeperate = {};
        double tempValue = 0;


        while ((st = br.readLine()) != null){

            firstSplit = st.split(" ");

            if (index % 2 == 0){ //inserting time into array for checking
            	
            	stringTime2[index/2]= st;
            	//System.out.println("stringTime2 is "+ stringTime2[index/2]);
            	
                timeSeperate = firstSplit[1].split(":");
                tempValue = Double.parseDouble(timeSeperate[0]) + Double.parseDouble(timeSeperate[1]) + Double.parseDouble(timeSeperate[2]);


               // System.out.println("Index: " + index + " " + firstSplit[1] + " " + tempValue);
                listOfTime[listOfTimeCounter] = tempValue;
                //System.out.println("listOfTime is "+ listOfTime[listOfTimeCounter]);
                //System.out.println();
                listOfTimeCounter++;
                counterPercentage++;

            }
            else if (index % 2 != 0){ //inserting percentage into array

               // System.out.println("Index: " + index + " " + firstSplit[0]);
                listOfPercent[listOfPercentCounter] = Double.parseDouble(firstSplit[0]);
                listOfPercentCounter++;
            }

            index++;
        }
//Nil
        int j;
for( int i=0;i<listOfPercent.length;i++)
     //   System.out.println(listOfPercent[i]);
	 j=1;

        double holdValue = 0;
        double newValue = 0;
        double test = 0;
        double divideCounter = 0;
        int x = 0;
        double percentageSum []= new double[counterPercentage];;
        double percentageSumCopy = 0;
       // System.out.println(counterTris);

        
        SimpleDateFormat sdate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");  
        

        
        
        int myVariable = 0;
        FileWriter myWriter = new FileWriter("out.txt");
        
        Date[] date2 = new Date[stringTime2.length];
        int indx=0;
        while (indx<stringTime2.length ){
        	date2[indx]= (Date)sdate.parse(stringTime2[indx]);
        	
        	indx++;}
        
        
        for(int i =0; i < counterTris; i++){
          
            divideCounter = 1;
            holdValue = finalTime[i];
            
            Date date1=new Date();
            date1= sdate.parse(stringTime[i]);// new date of tris file, head of gpu file (at most)
            System.out.println("date1 is "+ sdate.format(date1));
           
            //date1.setSeconds(date1.getSeconds()+2);
           // date1.s
            System.out.printf("long: date1 is  "+ date1.getTime()+ "\n");
            System.out.printf("x is  "+ x+ "\n");
         
            //while(date2[x].before(date1)){
            while(date2[x].getTime() - date1.getTime()<=2000){
                divideCounter++;
                
                percentageSum[i] = listOfPercent[x] + percentageSum[i];
                x++;
            }
                
            // Date old_date=date1;   
               
            percentageSum[i]/=(divideCounter-1);
            
                   myWriter.write("Time:" + sdate.format(date1) +"  Tris  " + finalTris[i]  +"    Mean_GPU%: " + (Math.round(percentageSum[i] * 100.0) / 100.0)+ "\n");
                 
            }
            

        

        myWriter.close();
        
        /*int i=0;
        while (i<percentageSum.length)
        {System.out.printf("%15s %-15s %15s %-15s %15s %-15s %n", "Time: " ,stringTime[i] ,"      # of Triangles:  " , finalTris[i] , "    Mean GPU %: " , (Math.round(percentageSum[i] * 100.0) / 100.0));
       i++;
        }
        */
        
    }
    /// time is here means that before this time we had this amount of gpu percentage







}


