package agent;

import java.io.*;
import java.util.*;

public class Main {
    private static double alpha = 0.5;
    private static List<double[]> weights = new LinkedList<>();
    private static List<Double> thresholds = new LinkedList<>();
    private static List<List<String>> files = new LinkedList<>();     //<language(dir), file>
    private static List<List<String>> testFiles = new LinkedList<>();
    private static List<Integer[]> trainData = new LinkedList<>();
    private static List<Integer[]> testData = new LinkedList<>();
    private static String directory = "trainData";
    private static List<String> classes = new LinkedList<>();
    private static Integer[] testVector = new Integer[27];
    private static String testDirectory;




    //lists all files in given directory
    public static List<List<String>> listFiles(String directory, List<List<String>> fil){
        File folder = new File(directory);
        for(File language : folder.listFiles()){
            if(!language.getName().equals(".DS_Store")) {
                classes.add(language.getName());
                if (language.isDirectory()) {
                    List<String> f = new LinkedList<>();
                    for (File train : language.listFiles()) {
                        if (!train.getName().equals(".DS_Store"))
                            f.add(train.getName());
                    }
                    fil.add(f);
                }
            }
        }
        return fil;
    }


    //adjusts weights
    public static void adjust(Integer[] vector, int predicted, int desired){
        int y = 0;
        int d = 0;
        for(double[] w : weights) {
            System.out.print("weights of " + classes.get(weights.indexOf(w)) + " adjusted to: [ ");
            if(weights.indexOf(w) == predicted) y = 1;
            if(weights.indexOf(w) == desired) d = 1;
            for (int i = 0; i < w.length; i++) {
                w[i] += (d - y) * alpha * vector[i];
                System.out.print(w[i] + " ");
            }
            System.out.print(" ]\n");
            thresholds.set(weights.indexOf(w),w[w.length-1]);
        }
    }



    public static double calculateActivation(Integer[] vector, double[] weights){
        double activation = 0;
        for(int i = 0; i < weights.length; i++){
            activation += vector[i] * weights[i];
        }
        System.out.println("activation: " + activation + ", threshold: " + weights[weights.length-1]);

        return activation;
    }


    public static int predict(Integer[] vector){
        double y;
        double maxActivation = calculateActivation(vector, weights.get(0));
        int index = 0;

        System.out.print("vector: [ ");
        for (int j = 0; j < vector.length; j++) System.out.print(vector[j] + " ");
        System.out.println("]");

        for(double[] weight : weights) {
            y = calculateActivation(vector, weight);
            if(y > maxActivation) {
                maxActivation = y;
                index = weights.indexOf(weight);
            }
            System.out.println("activation: " + y + ", max activation: " + maxActivation);
        }
        return index;
    }


    public static void countLetters(String line, Integer[] vector){
        //remove non-ascii characters
        line = line.replaceAll("[^a-zA-Z ]|\\s", "");
        line = line.toLowerCase();
        System.out.println(line);
        char[] cLine = line.toCharArray();
        int index;

        //create vector with amount of letters in text as attributes
        for(int i = 0; i < cLine.length; i++){
            index = cLine[i] - 'a';
            vector[index]++;
        }
    }


    //creates list of vectors
    public static void createVectors(List<List<String>> files, List<Integer[]> data) throws IOException {
        FileReader read;
        BufferedReader buff;
        for (List entry : files) {
            for (Object file : entry) {
                int index = files.indexOf(entry);
                String lan = classes.get(index);
                Integer[] vector = new Integer[28];     //[28]
                for(int i = 0; i < vector.length; i++){
                    vector[i] = 0;
                }
                vector[vector.length-2] = -1;
                vector[vector.length-1] = index;

                    String fileDir = "trainData/" + lan + "/" + file;
                    read = new FileReader(fileDir);
                    buff = new BufferedReader(read);

                    String line;
                    while ((line = buff.readLine()) != null) {
                        countLetters(line, vector);
                    }
                    data.add(vector);

            }
        }
    }


    public static void main(String[] args) throws IOException {
	// write your code here

        Scanner scan = new Scanner(System.in);
        System.out.println("enter learning rate");
        alpha = scan.nextDouble();
        System.out.println("do you want to enter directory (d) or a simple text (t)");
        String c = scan.next();

        files = listFiles(directory, files);

        for(List entry : files){
            int index = files.indexOf(entry);
            String lan = classes.get(index);
            System.out.println("language: " + lan + ", files: " + entry);
        }
        System.out.println();


        createVectors(files, trainData);

        for(int i = 0; i < classes.size(); i++){
            thresholds.add(2.0);
            double[] w = new double[27];   //[27]
            for (int j = 0; j < w.length - 1; j++) w[j] = 0.1;
            w[w.length - 1] = 2.0;
            weights.add(w);
        }

        System.out.print("thresholds: ");
        for(double th : thresholds) System.out.print(th + " ");


        System.out.println("weights: ");

        for(double[] w : weights) {
            System.out.print("[ ");
            for(int i = 0; i < weights.get(0).length; i++) System.out.print(w[i] + " ");
            System.out.print("]\n");
        }
        double total = 0;
        double correct = 0;

        for(Integer[] vector : trainData){
            int index = vector[vector.length-1];
            String lan = classes.get(index);
            System.out.print("\nlanguage: " + lan + "\n[ ");
            for(int i = 0; i < vector.length; i++){
                System.out.print(vector[i] + " ");
            }
            System.out.println("]");
            vector[vector.length-1] = index;
        }

        Collections.shuffle(trainData);

        for(int i = 0; i < 10; i++){
            for(Integer[] vector : trainData) {
                int predicted = predict(vector);
                int d = vector[vector.length-1];
                if(predicted == d) correct ++;
                total++;
                System.out.println("\npredicted: " + classes.get(predicted) + ", actual: " + classes.get(d) + "\n");
                if (predicted != vector[vector.length-1])
                    adjust(vector, predicted, d);
            }
        }
        double accuracy = correct/total;
        System.out.println("accuracy on train data: " + accuracy + "\n");



        correct = 0;
        total = 0;

        //for directories
        if(c.equals("d")){
            System.out.println("enter name of the directory");
            testDirectory = scan.next();
            listFiles(testDirectory, testFiles);
            createVectors(testFiles, testData);
            for(Integer[] vector : testData) {
                int p = predict(vector);
                if(p == vector[vector.length-1]) correct++;
                total++;
                System.out.println("\npredicted: " + classes.get(p) + ", actual: " + classes.get(vector[vector.length-1])+ "\n");
            }
            accuracy = correct/total;
            System.out.println("accuracy on test data: " + accuracy);
        }

        //for text
        else if(c.equals("t")) {
            while (true) {
                System.out.println("enter text");
                BufferedReader read = new BufferedReader(new InputStreamReader(System.in));


                for (int i = 0; i < testVector.length; i++) testVector[i] = 0;

                String text;
                while (!(text = read.readLine()).equals("")) {
                    countLetters(text, testVector);
                    int pred = predict(testVector);
                    System.out.println("predicted: " + classes.get(pred));
                }
                read.close();
            }
        }

        else System.out.println("wrong value");
    }
}
