//By Tian Zhang
//April 2015
//Merge the trajectories with same number of time units to the ones with more vehicles


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Aggeragate_v {
	public static void main (String [] args) throws IOException{
	File inFile1 = new File ("./Data/500v500t_1.txt");
	File inFile2 = new File ("./Data/500v500t_2.txt");

	ArrayList<ArrayList<input>> insertion=new ArrayList<ArrayList<input>>();
	Scanner sc1 = new Scanner (inFile1);
	String line=sc1.nextLine();
	sc1.nextLine();
	
	while(sc1.hasNextLine()){
		input ip=createInput(sc1);
		if(ip.Type.equals("newpoint")){
			ArrayList<input> list=new ArrayList<input>();
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

	try{
    	FileWriter wt = new FileWriter("./Data/merged_v.txt", true);
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
	
	public static void connect(Scanner sc,ArrayList<ArrayList<input>> insertion){
		sc.nextLine();
		sc.nextLine();
		int offset=insertion.get(insertion.size()-1).get(0).ID+1;
		
		while(sc.hasNextLine()){
			String line=sc.nextLine();
			String[] k=line.split(" ");
			input p=new input(Integer.parseInt(k[0])+offset, Integer.parseInt(k[1]), k[2], Double.parseDouble(k[3]), Double.parseDouble(k[4]));
			if(p.Type.equals("newpoint")){
				ArrayList<input> list=new ArrayList<input>();
				list.add(p);
				insertion.add(list);
			}
			else{
				insertion.get(p.ID).add(p);
			}
		}
	}
}

