//By Tian Zhang
//April 2015
//Merge the trajectories with same number of vehicles to the ones with more time units

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Scanner;

public class Aggeragate_t {
	public static void main (String [] args) throws IOException{
		File inFile1 = new File ("./Data/500v500t_1.txt");
		File inFile2 = new File ("./Data/500v500t_2.txt");
		File inFile3 = new File ("./Data/500v500t_3.txt");
		File inFile4 = new File ("./Data/500v500t_4.txt");
		File inFile5 = new File ("./Data/500v500t_5.txt");

		ArrayList<ArrayList<input>> insertion= new ArrayList<>();
		Scanner sc1 = new Scanner (inFile1);
		String line=sc1.nextLine();
		sc1.nextLine();
		
		while(sc1.hasNextLine()){
			input ip=createInput(sc1);
			if(ip.Type.equals("newpoint")){
				ArrayList<input> list= new ArrayList<>();
				list.add(ip);
				insertion.add(list);
			}
			else{
				insertion.get(ip.ID).add(ip);
			}
		}
		sc1.close();
		
		Scanner sc2 = new Scanner(inFile2);
		connect(sc2, insertion);
		System.out.println(inFile2.getName());
		
		Scanner sc3 = new Scanner(inFile3);
		connect(sc3, insertion);
		System.out.println(inFile3.getName());
		
		Scanner sc4 = new Scanner(inFile4);
		connect(sc4, insertion);
		System.out.println(inFile4.getName());
		
		Scanner sc5 = new Scanner(inFile5);
		connect(sc5, insertion);
		System.out.println(inFile5.getName());
		
		for (int i=0;i<insertion.size();i++){
			ArrayList<input> current=insertion.get(i);
			input last=current.get(current.size()-1);
			/*
			if(last.TimeStamp<999){
				last.Type="disappearpoint";
			}
			
			else if(last.TimeStamp>999){
				int n=current.size()-1;
				for(int j=1000;j<=n;j++){
					current.remove(current.size()-1);
				}
			}
			*/
			
		}
		
		try{
	    	FileWriter wt = new FileWriter("./Data/merged_t.txt", true);
	    	BufferedWriter bfwt=new BufferedWriter(wt);
	    	bfwt.write(line);
	    	bfwt.newLine();
	    	bfwt.newLine();
	    	for(int i=0;i<insertion.size();i++){
	    		for(int j=0;j<insertion.get(i).size();j++){
	    			bfwt.write(Integer.toString(insertion.get(i).get(j).ID)+" "+Integer.toString(insertion.get(i).get(j).TimeStamp)+" "+insertion.get(i).get(j).Type+" "+Double.toString(insertion.get(i).get(j).Lat)+" "+Double.toString(insertion.get(i).get(j).Lng));
	    			bfwt.newLine();
	    		}
	    	}
	    	bfwt.close();
	    }catch(IOException e){
	    	e.printStackTrace();
	    }
	}
	
	public static input createInput (Scanner sc){
		String line = sc.nextLine();
    	String[] k=line.split(" ");
    	input p=new input(Integer.parseInt(k[0]), Integer.parseInt(k[1]), k[2], Double.parseDouble(k[3]), Double.parseDouble(k[4]));
    	return p;
	}
	
	public static void connect(Scanner sc, ArrayList<ArrayList<input>> insertion){
		sc.nextLine();
		sc.nextLine();
		ArrayList<Integer> offset= new ArrayList<Integer>();
		
		while(sc.hasNextLine()){
			String line = sc.nextLine();
	    	String[] k=line.split(" ");
	    	
	    	int ID=Integer.parseInt(k[0]);
			if(k[2].equals("newpoint")){
		    	double firstLat=Double.parseDouble(k[3]);
		    	double firstLng=Double.parseDouble(k[4]);
		    	ArrayList<input> currentList=insertion.get(ID);
		    	input last=currentList.get(currentList.size()-1);
		    	if(last.Type.equals("disappearpoint")){
		    		last.Type="point";
		    	}
		    	double stepLat=(firstLat-last.Lat)/30;
		    	double stepLng=(firstLng-last.Lng)/30;
		    	for(int t=1;t<=29;t++){//insert 30 points;
		    		input ip=new input(ID,last.TimeStamp+t,"point",round(last.Lat+stepLat*t,7),round(last.Lng+stepLng*t,7));
		    		insertion.get(ID).add(ip);
		    	}
		    	offset.add(last.TimeStamp+30);
		    	input ip=new input(ID,last.TimeStamp+30,"point",firstLat,firstLng);
		    	insertion.get(ID).add(ip);
			}
			else{
				input ip=new input(ID,Integer.parseInt(k[1])+offset.get(ID).intValue(),k[2],Double.parseDouble(k[3]),Double.parseDouble(k[4]));
				insertion.get(ID).add(ip);
			}
		}
		sc.close();
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();
	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
}
