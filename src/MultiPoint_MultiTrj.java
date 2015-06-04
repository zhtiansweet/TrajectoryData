//Tian Zhang 2015/04/03
//Insert POIs to the original trajectories

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

class input{
	int ID;
	int TimeStamp;
	String Type;
	double Lat;
	double Lng;
	
	public input(int id, int ts, String tp, double lt, double lg){
		this.ID=id;
		this.TimeStamp=ts;
		this.Type=tp;
		this.Lat=lt;
		this.Lng=lg;
	}
}
class poi{
	String Name;
	double Lat;
	double Lng;
	int numberTS;
	
	public poi(String n, double lt, double lg, int nts){
		this.Name = n;
		this.Lat = lt;
		this.Lng = lg;
		this.numberTS = nts;
	}
}

class output{
	int ID;
	String Type;
	int BeginTime;
	double BeginLat;
	double BeginLng;
	int EndTime;
	double EndLat;
	double EndLng;
	
	public output(int id, String tp, int bt, double blt, double blg, int et, double elt, double elg){
		this.ID=id;
		this.Type=tp;
		this.BeginTime=bt;
		this.BeginLat=blt;
		this.BeginLng=blg;
		this.EndTime=et;
		this.EndLat=elt;
		this.EndLng=elg;
	}
}

public class MultiPoint_MultiTrj {
	public static void main (String [] args) throws IOException{
		File inFile = new File ("./Data/2000v2000t.txt");
		File inFile2 = new File ("./Data/POI.txt");
		
		//Read and store trajectories
		ArrayList<ArrayList<input>> insertion=new ArrayList<ArrayList<input>>();
		Scanner sc = new Scanner (inFile);
		String line=sc.nextLine();
		sc.nextLine();
		
		while(sc.hasNextLine()){
			input ip=createInput(sc);
			/*
			if(ip.ID>=2500){
				break;
			}
			*/
			if(ip.Type.equals("newpoint")){
				ArrayList<input> list=new ArrayList<input>();
				list.add(ip);
				insertion.add(list);
			}
			else{
				insertion.get(ip.ID).add(ip);
			}
		}
		sc.close();
		
		//Pick up POIs that could be inserted into each object's trajectory
		Scanner sc2 = new Scanner (inFile2);
		ArrayList<ArrayList<poi>> poiInsert = new ArrayList<ArrayList<poi>>();
		while(sc2.hasNextLine()){
			sc2.nextLine();
			String name=sc2.nextLine();
			String line2 = sc2.nextLine();
	    	String[] k=line2.split(" ");
	    	poi p = new poi(name,Double.parseDouble(k[0]), Double.parseDouble(k[1]), Integer.parseInt(k[2]));
			for(int i=0;i<insertion.size();i++){
				for(int j=0;j<insertion.get(i).size()-1;j++){
					if(isClosed((insertion).get(i).get(j),insertion.get(i).get(j+1), p.Lat, p.Lng)){
						if(i>=poiInsert.size()){
							ArrayList<poi> list = new ArrayList<poi>();
							list.add(p);
							poiInsert.add(list);
						}
						else{
							poiInsert.get(i).add(p);
						}
						
					}
				}
			}
		}
		sc2.close();
		
		
		//Insert POIs
		for(int x=0;x<poiInsert.size();x++){
			//Pick up a random number of POIs for each object
			//number between 0 and 3
			for(int y=1;y<=min(poiInsert.get(x).size()-1, randInt(0,3));y++){
				poi currentPoi = poiInsert.get(x).get(randInt(0,poiInsert.get(x).size()-1));
				int step = 1;
				for(int j=0;j<insertion.get(x).size()-1;j+=step){
					step=1;
					if(isClosed(insertion.get(x).get(j),insertion.get(x).get(j+1),currentPoi.Lat,currentPoi.Lng)){
						for(int q=j+1;q<insertion.get(x).size();q++){
							insertion.get(x).get(q).TimeStamp+=currentPoi.numberTS;
						}
						input ip=new input(insertion.get(x).get(j).ID,insertion.get(x).get(j).TimeStamp+currentPoi.numberTS,currentPoi.Name,currentPoi.Lat,currentPoi.Lng);
						insertion.get(x).add(j+1, ip);
						input ip2=new input(insertion.get(x).get(j).ID,insertion.get(x).get(j).TimeStamp+1,currentPoi.Name,currentPoi.Lat,currentPoi.Lng);
						insertion.get(x).add(j+1, ip2);
						step=currentPoi.numberTS;
						break;
					}
				}
			}
		}
			
		
		//Translate into standard format
		ArrayList<ArrayList<output>> moveStop = new ArrayList<ArrayList<output>>();
		for(int i=0;i<insertion.size();i++){
			int BeginTime=0;
			double BeginLat=insertion.get(i).get(0).Lat;
			double BeginLng=insertion.get(i).get(0).Lng;
			for(int j=0;j<insertion.get(i).size()-1;j++){
				input current=insertion.get(i).get(j);
				input next=insertion.get(i).get(j+1);
				if(isInsertion(current)&&isInsertion(next)&&current.Type.equals(next.Type)){
					output op=new output(current.ID, "move", BeginTime,BeginLat, BeginLng, current.TimeStamp, current.Lat, current.Lng);
					if(BeginTime==0){
						ArrayList<output> list=new ArrayList<output>();
						moveStop.add(list);
					}
					moveStop.get(i).add(op);
					
					double lat=current.Lat;
					double lng=current.Lng;
					
					output op1=new output(current.ID,"stop("+current.Type+")", current.TimeStamp,lat,lng,next.TimeStamp,lat,lng);
					moveStop.get(i).add(op1);
					BeginTime=next.TimeStamp;
					BeginLat=next.Lat;
					BeginLng=next.Lng;
				}
				/*
				else{
						output op=new output(current.ID,"move",BeginTime,BeginLat,BeginLng,next.TimeStamp,next.Lat,next.Lng);
						if(BeginTime==0){
							ArrayList<output> list=new ArrayList<output>();
							moveStop.add(list);
						}
						moveStop.get(i).add(op);
						BeginTime=next.TimeStamp;
						BeginLat=next.Lat;
						BeginLng=next.Lng;
				}
				*/
			}
			input end = insertion.get(i).get(insertion.get(i).size()-1);
			if(BeginTime == 0){	
				int mid = (int) Math.floor(insertion.get(i).size()/2);
				input middle = insertion.get(i).get(mid);
				ArrayList<output> list=new ArrayList<output>();
				moveStop.add(list);
				output op1 = new output(end.ID, "move", BeginTime, BeginLat, BeginLng, middle.TimeStamp, middle.Lat, middle.Lng);
				output op2 = new output(end.ID, "move", middle.TimeStamp, middle.Lat, middle.Lng, end.TimeStamp, end.Lat, end.Lng);
				moveStop.get(i).add(op1);
				moveStop.get(i).add(op2);
			}
			else{
				output op = new output(end.ID, "move", BeginTime, BeginLat, BeginLng, end.TimeStamp, end.Lat, end.Lng);
				moveStop.get(i).add(op);
			}
		}
		
		try{
	    	FileWriter wt = new FileWriter("./Data/Points.txt", true);
	    	BufferedWriter bfwt=new BufferedWriter(wt);
	    	bfwt.write(line);
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
		
		
		
		try{
	    	FileWriter wt = new FileWriter("./Data/MoveStop.txt", true);
	    	BufferedWriter bfwt=new BufferedWriter(wt);
	    	bfwt.write("Object_Id Type BeginTime BeginLat BeginLng EndTime EndLat EndLng");
	    	bfwt.newLine();
	    	for(int i=0;i<moveStop.size();i++){
	    		for(int j=0;j<moveStop.get(i).size();j++){
	    			output current=moveStop.get(i).get(j);
	    			bfwt.write(Integer.toString(current.ID)+" "+current.Type+" "+Integer.toString(current.BeginTime)+" "+Double.toString(current.BeginLat)+" "+Double.toString(current.BeginLng)+" "+Integer.toString(current.EndTime)+" "+Double.toString(current.EndLat)+" "+Double.toString(current.EndLng));
	    			bfwt.newLine();
	    		}
	    	}
	    	bfwt.close();
	    }catch(IOException e){
	    	e.printStackTrace();
	    }
		
	}
	
	public static int min(int i, int j) {
		return (i<=j)?i:j;
	}

	public static boolean isInsertion (input ip){
		if(ip.Type.equals("point")||ip.Type.equals("newpoint")){
			return false;
		}
		return true;
	}
	
	public static input createInput (Scanner sc){
		String line = sc.nextLine();
    	String[] k=line.split(" ");
    	input p=new input(Integer.parseInt(k[0]), Integer.parseInt(k[1]), k[2], Double.parseDouble(k[3]), Double.parseDouble(k[4]));
    	return p;
	}
	
	public static boolean isClosed(input p1, input p2, double lt0, double lg0){
		double lt1=p1.Lat;
		double lg1=p1.Lng;
		double lt2=p2.Lat;
		double lg2=p2.Lng;
		double lat1=(Math.pow((lt2-lt1), 2))*lt0+(lg2-lg1)*(lt2-lt1)*lg0-(lt2*lg1-lt1*lg2)*(lg2-lg1);
		double lat2=Math.pow((lg2-lg1),2)+Math.pow((lt2-lt1), 2);
		double lat=lat1/lat2;
		double lng=-((lt2-lt1)/(lg2-lg1))*lat+((lt2-lt1)/(lg2-lg1))*lt0+lg0;
		double d=distance(lt0,lg0,lat,lng);
		if(isBetween(p1,p2,lat,lng) && d<=0.03){
			return true;
		}
		return false;
	}
	
	public static double distance(double lt1, double lg1, double lt2, double lg2){
		double theta = lg1 - lg2;
		double dist = Math.sin(deg2rad(lt1)) * Math.sin(deg2rad(lt2)) + Math.cos(deg2rad(lt1)) * Math.cos(deg2rad(lt2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		dist = dist * 1.609344;
		return (dist);
	}
	
	private static double deg2rad(double deg) {
		  return (deg * Math.PI / 180.0);
	}

	
	private static double rad2deg(double rad) {
		  return (rad * 180 / Math.PI);
	}
	
	public static boolean isBetween (input p1, input p2, double lt, double lg){
		if(p1.Lat<=lt && p2.Lat>=lt && p1.Lng<=lg && p2.Lng>=lg){
			return true;
		}
		if(p1.Lat<=lt && p2.Lat>=lt && p2.Lng<=lg && p1.Lng>=lg){
			return true;
		}
		if(p2.Lat<=lt && p1.Lat>=lt && p2.Lng<=lg && p1.Lng>=lg){
			return true;
		}
		if(p2.Lat<=lt && p1.Lat>=lt && p1.Lng<=lg && p2.Lng>=lg){
			return true;
		}
		return false;
	}
	
	public static int randInt(int min, int max) {

	    // NOTE: Usually this should be a field rather than a method
	    // variable so that it is not re-seeded every call.
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
}
