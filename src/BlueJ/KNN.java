import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class KNN {
    //public static HashMap<String, University> map;

    //global arraylist of University
    public static ArrayList<University> listOfUni = new ArrayList<University>();
    public static ArrayList<University> oldCentroids = new ArrayList<University>();
    public static ArrayList<University> newCentroids = new ArrayList<University>();
    public static HashMap<University, ArrayList<University>> clusters = new HashMap();

    /**
     * processes the data of universities and attributes into a list of University
     */
    public static void process() {
        boolean flag = false;
        try {
            FileReader input = new FileReader("uniData.csv");
            BufferedReader bufRead = new BufferedReader(input);
            String myLine = null;

            while((myLine = bufRead.readLine()) != null) {
                if(flag == true) {
                    String[] arr = myLine.split(",");

                    if(arr.length < 1) {
                        System.out.println("Bad sized line.");
                        return;
                    }
                    University p = new University(arr[1], arr[2], Integer.parseInt(arr[3]), Integer.parseInt(arr[4]), Integer.parseInt(arr[5]));
                    listOfUni.add(p);
                }
                flag = true;    
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * prints the list
        for(int i = 0; i < listOfUni.size(); i++){
        listOfUni.get(i).printUniversity();
        }*/
    }

    /**
     * gets user input and finds vector distances between all schools and stores them into hashmap
     */

    public static University findMin(University current) {
        double res = 0;
        double min = 10000;
        int i, index = 0;
        for(i = 0; i < oldCentroids.size(); i++ ) {
        	if(!current.getName().equals(oldCentroids.get(i).getName())){
	            res = oldCentroids.get(i).distance(current);
	            if(res < min) {
	                min = res;
	                index = i;
	            }
        	}
        }

        return oldCentroids.get(index);
    }

    public static void processClusters(){
        University target = null;
        ArrayList<University> temp = new ArrayList();
        for(University cur : listOfUni) {
            target = findMin(cur);	
            for(Map.Entry<University, ArrayList<University>> cluster: clusters.entrySet()){
                if(target.equals(cluster.getKey())){
                    temp = cluster.getValue();
                    temp.add(cur);
                    clusters.put(cluster.getKey(),temp);
                }
            }
        }
    }

    public static void initializeClusters() {
        oldCentroids.add(new University("Harvard University", "USA", 1, 1, 1));
        oldCentroids.add(new University("Maastricht University", "Netherlands", 8, 355, 478));
        oldCentroids.add(new University("Otto-von-Guericke University Magdeburg", "Germany", 40, 355, 478));
        oldCentroids.add(new University("Paul Val��ry University Montpellier III", "France", 35, 355, 478));
        oldCentroids.add(new University("Yanbian University", "China", 84, 355, 478));

        /*-----------------------------------------*/
        clusters.put(oldCentroids.get(0), new ArrayList<University>());
        clusters.put(oldCentroids.get(1), new ArrayList<University>());
        clusters.put(oldCentroids.get(2), new ArrayList<University>());        
        clusters.put(oldCentroids.get(3), new ArrayList<University>());
        clusters.put(oldCentroids.get(4), new ArrayList<University>());
    }
    
    /*
     * Loops until centroid difference < .001
     * 
     * Loops through "clusters" HashMap and creates a new centroid for each cluster.
     * Updates the "newCentroids" instance variable.
     * Removes the old clusters in the "clusters" HashMap
     * Calls "processClusters()"
     */
    
    public static void getNewCentroids() {    	
    	while(true) {
    		ArrayList<University> centroids = new ArrayList<University>();
    		
    		//old centroid list
    		ArrayList<University> oldCentroidsToRemove = new ArrayList<University>();
    		
        	for(Map.Entry<University, ArrayList<University>> cluster: clusters.entrySet()){
                ArrayList<University> unis = cluster.getValue();
                University newCentroid = getCentroid(unis);
                centroids.add(newCentroid);
                oldCentroidsToRemove.add(cluster.getKey());
            }
        	
        	newCentroids = centroids;
        	
        	if(getCentroidDifference(oldCentroids, newCentroids) < .001) {
        		break;
        	}

        	
        	/**
        	 * removal done here via use of a new list to avoid concurrent modification exception
        	 */
        	for(University remCentroid: oldCentroidsToRemove){
                clusters.remove(remCentroid);
        	}
        	
        	oldCentroids = newCentroids;
        	
            clusters.put(oldCentroids.get(0), new ArrayList<University>());
            clusters.put(oldCentroids.get(1), new ArrayList<University>());
            clusters.put(oldCentroids.get(2), new ArrayList<University>());        
            clusters.put(oldCentroids.get(3), new ArrayList<University>());
            clusters.put(oldCentroids.get(4), new ArrayList<University>());

        	processClusters();
    	}
    }
    
    
    public static double getCentroidDifference(ArrayList<University> a, ArrayList<University> b) {
    	double totalDiff = 0;
    	
    	for(int i = 0; i < a.size(); i++) {
    		totalDiff = a.get(i).distance(b.get(i));
    	}
    	
    	return totalDiff / a.size();
    }
    /*
     * Takes a list of universities and creates a new "mock" university that
     * holds the values of the new centroid. Its name is defaulted to "centroid"
     * 
     */
    public static University getCentroid(ArrayList<University> unis) {
    	double nationalRankAvg = 0;
    	double qualityOfEducationAvg = 0;
    	double alumniEmploymentAvg = 0;
    	
    	if (unis == null) {
    		System.out.println("Empty List");
    		return null;
    	}
    	
    	for(int i = 0; i < unis.size(); i++) {
    		nationalRankAvg += unis.get(i).getNationalRank();
    		qualityOfEducationAvg += unis.get(i).getQualityOfEducation();
    		alumniEmploymentAvg += unis.get(i).getAlumniEmployment();
    	}
    	
    	return new University("centroid", "centroid", (int)Math.floor(nationalRankAvg / unis.size()), 
    			(int)Math.floor(qualityOfEducationAvg / unis.size()), (int)Math.floor(alumniEmploymentAvg / unis.size())); 
    }
    
    /**
     * takes user input and predicts which cluster that the new school will belong to
     * @param args
     */
    public static void processUserInput(){
    	ArrayList<University> uniList = new ArrayList<University>();
    	String univName = null;
    	String country = null;
    	int nationalRank = 0;
    	int qualityOfEdu = 0;
    	int alumEmployment = 0;
    	try{
    		Scanner keyIn = new Scanner(System.in);

    		while(true){
	        	System.out.println("Press anything to enter university info, q to quit");
	        	if(keyIn.nextLine().equals("q")){
	        		break;
	        	}
    			
    			System.out.print("Please Enter a University Name: ");
		       	univName = keyIn.nextLine();
		       	System.out.print("Please Enter a Country: ");
		       	country = keyIn.next();
		       	System.out.print("Please Enter a National Rank for your University: ");
		       	nationalRank = keyIn.nextInt();
		       	System.out.print("Please Enter a Quality of Education Rank for your University: ");
		       	qualityOfEdu = keyIn.nextInt();
		       	System.out.print("Please Enter a Alumni Employment Rank for your University: ");	
	        	alumEmployment = keyIn.nextInt();
	        	keyIn.nextLine(); // takes the new line char and continues
	        	
	        	University newUni = new University(univName, country, nationalRank, qualityOfEdu, alumEmployment);
	        	uniList.add(newUni);
    		}
	        
    		predictCluster(uniList);
        	
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    /**
     * predicts which cluster the new university is most likely to be in without reclustering using
     * new centroids
     * (Testing set)
     * @param uni
     */
    public static void predictCluster(ArrayList<University> uniList){
    	University target = null;
    	for(University uni: uniList){
    		target = findMin(uni);
    		int indexOfMin = oldCentroids.indexOf(target);
    		System.out.println("University: "+uni.getName()+" will likey fall in cluster "+(indexOfMin+1));
    	}
    }

    public static void main(String[] args) {
        process(); // adds the data into the hashmap
        initializeClusters();
        processClusters();
        //getNewCentroids();
        
        System.out.print("Cluster1 ==> ");
        oldCentroids.get(0).printUniversity();
        System.out.print(" ---- ");
        System.out.println(clusters.get(oldCentroids.get(0)).size());
        System.out.print("Cluster2 ==> ");
        oldCentroids.get(1).printUniversity();
        System.out.print(" ---- ");
        System.out.println(clusters.get(oldCentroids.get(1)).size());
        System.out.print("Cluster3 ==> ");
        oldCentroids.get(2).printUniversity();
        System.out.print(" ---- ");
        System.out.println(clusters.get(oldCentroids.get(2)).size());
        System.out.print("Cluster4 ==> ");
        oldCentroids.get(3).printUniversity();
        System.out.print(" ---- ");
        System.out.println(clusters.get(oldCentroids.get(3)).size());
        System.out.print("Cluster5 ==> ");
        oldCentroids.get(4).printUniversity();
        System.out.print(" ---- ");
        System.out.println(clusters.get(oldCentroids.get(4)).size());
        System.out.println();
        
        processUserInput();
    }
}