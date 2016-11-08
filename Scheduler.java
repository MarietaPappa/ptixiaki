
package timetablescheduling;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;


abstract public class Scheduler {
    protected int totalTimeslots;
    protected final String studentsFilename;
    protected final String modulesFilename;
    protected final int[][] modulesData; // [module_id][number of students] [module:1][students attending: 20]
    protected final int[][] studentsData; //line->studentID: moduleID1 moduleID2 etc
    protected final int[][] studentsPerModule; //[module][student]
    protected final int numOfModules;
    protected final int numOfStudents;
    protected final int[][] conflict_matrix;
    protected final int[] conflict_vector; // αριθμός συγκρούσεων με άλλες εξετάσεις
    protected final int[] conflict_vector_s; // αριθμός συγκρούσεων φοιτητών για την εξέταση ενός μαθήματος με όλα τα άλλα μαθήματα
    protected int [] solution;
    protected double threshold; // orio metavasis
    protected int solution_expressed_by_modules_per_timeslot[][] ;
    protected static final boolean msg = false;
    protected static final boolean msg1 = false;

    public Scheduler(String studentsFilename, String modulesFilename, int totalTimeslots){
        this.totalTimeslots = totalTimeslots;
        this.modulesFilename = modulesFilename;
        this.studentsFilename = studentsFilename;
        modulesData = loadModules();
        studentsData = loadStudents();
        
        numOfModules = modulesData.length;
        numOfStudents = studentsData.length;
        
        conflict_vector_s = new int[numOfModules];
        conflict_vector = new int[numOfModules];
        
        conflict_matrix = new int[numOfModules][numOfModules];
        
        studentsPerModule = create_students_per_module_array();
    }
    
    
    public int[] schedule(){
        int first_solution_expressed_by_modules_per_timeslot[][];
        double cost, cost_next, first_round_cost;
        int iteration = 0, local_solution[], solution_next[], first_round_best_solution[];        
        
        create_conflict_matrix();
        local_solution = create_initial_solution();
        
        if (totalTimeslots < solution_expressed_by_modules_per_timeslot.length)
            System.out.println("Cannot solve using " + totalTimeslots + " timeslots, used " + solution_expressed_by_modules_per_timeslot.length + " instead");
        
        totalTimeslots = solution_expressed_by_modules_per_timeslot.length;
        
        print_modules_per_timeslot(solution_expressed_by_modules_per_timeslot);
        
        cost = computeCost(local_solution);
        
        if (cost == Double.MAX_VALUE) {
            System.out.println("Cannot create initial solution");
            System.exit(1);
            return null;
        }
        
        threshold = cost / 4;
        System.out.println("Iteration: " + iteration + ", threshold: " + threshold + ", solution cost: " + cost);
        
        while (threshold > 0.8){
            iteration++;
            
            //solution_next = make_new_solution_by_module_swap(local_solution);
            solution_next = make_new_solution_by_timeslot_swap(local_solution);
            create_conflict_matrix();
            cost_next = computeCost(solution_next);
            
            if (cost_next - threshold < cost) {
                local_solution = solution_next;
                cost = cost_next;
            }           
            
            if (iteration % 100 == 0) System.out.println("Iteration: " + iteration + ", threshold: " + threshold + ", solution cost: " + cost);
            if (threshold > 0 && cost < Double.MAX_VALUE) threshold -= threshold * 0.002;
        }
        System.out.println("Finished First Phase (Simulated Annealing)...");
        for (int i = 0 ; i < 1000 ; i++){
            solution_next = make_new_solution_by_timeslot_swap(local_solution);
            create_conflict_matrix();
            cost_next = computeCost(solution_next);
            
            if (cost_next < cost) {
                local_solution = solution_next;
                cost = cost_next;
            }  
            if (i % 100 == 0) System.out.println("Iteration: " + i + ", threshold: " + threshold + ", solution cost: " + cost);
        }
        System.out.println("Finished Second Phase (Simulated Annealing) with threshold 0...\nSolution cost: " + cost);
         
        first_round_best_solution = local_solution;
        first_round_cost = cost;
        first_solution_expressed_by_modules_per_timeslot = make_deep_copy(solution_expressed_by_modules_per_timeslot);
        
        print_modules_per_timeslot(solution_expressed_by_modules_per_timeslot);
        
        System.out.println("Continue with simple module move...");
        threshold = cost / 4;
        
        while (threshold > 0.8){
            iteration++;
            
            //solution_next = make_new_solution_by_module_swap(local_solution);
            solution_next = make_new_solution_by_module_move(local_solution);
            create_conflict_matrix();
            cost_next = computeCost(solution_next);
            
            if (cost_next - threshold < cost) {
                local_solution = solution_next;
                cost = cost_next;
            }           
            
            if (iteration % 100 == 0) System.out.println("Iteration: " + iteration + ", threshold: " + threshold + ", solution cost: " + cost);
            if (threshold > 0 && cost < Double.MAX_VALUE) threshold -= threshold * 0.002;
        }  
        
        if (first_round_cost < cost){
            local_solution = first_round_best_solution;
            cost = first_round_cost;
            solution_expressed_by_modules_per_timeslot = first_solution_expressed_by_modules_per_timeslot;
        }
        
        System.out.println("Finished, solution cost: " + cost);
        System.out.println("Final solution:");
        print_modules_per_timeslot(solution_expressed_by_modules_per_timeslot);
        return local_solution;
    }
     // https://www.kleemans.ch/static/fourcolors/welsh-powell.pdf
    abstract public int[] create_initial_solution();
    
  
    private int[][] make_deep_copy(int [][]a){
        int [][]b = new int[a.length][];
        
        for (int i = 0 ; i < a.length ; i++)
            b[i] = Arrays.copyOf(a[i], a[i].length);
                
        
        return b;
    }
    protected int[] make_new_solution_by_timeslot_swap(int []solution){
        int i, j, temp[];
        
        do{
            i = (int)Math.floor(Math.random() * solution_expressed_by_modules_per_timeslot.length); 
            j = (int)Math.floor(Math.random() * solution_expressed_by_modules_per_timeslot.length); 
        }while (i == j); 
        
        temp = solution_expressed_by_modules_per_timeslot[i];
        
        solution_expressed_by_modules_per_timeslot[i] = solution_expressed_by_modules_per_timeslot[j];
        solution_expressed_by_modules_per_timeslot[j] = temp;
        
        for (int ii = 0 ; ii < solution_expressed_by_modules_per_timeslot[i].length ; ii++){
            if (solution_expressed_by_modules_per_timeslot[i][ii] > -1){
                solution[ solution_expressed_by_modules_per_timeslot[i][ii] ] = i;
            }
            else break;
        }
        
        for (int jj = 0 ; jj < solution_expressed_by_modules_per_timeslot[j].length ; jj++){
            if (solution_expressed_by_modules_per_timeslot[j][jj] > -1){
                solution[ solution_expressed_by_modules_per_timeslot[j][jj] ] = j;
            }
            else break;
        }
        
        return solution;
    }
    protected int[] make_new_solution_by_module_move(int []solution){
        int module, from, to, temp, i, j;
        
        do{
            module = (int)Math.floor(Math.random() * solution.length); 
            from = solution[ module ];
            to = (int)Math.floor(Math.random() * totalTimeslots); 
        }while (from == to); 
        
        solution[module] = to;
        
        // delete from old place
        i = 0;
        while (solution_expressed_by_modules_per_timeslot[from][i] != module) i++;
        
        j = i + 1;
        while (solution_expressed_by_modules_per_timeslot[from][j] > -1) j++; 
        j--;
        
        solution_expressed_by_modules_per_timeslot[from][i] = solution_expressed_by_modules_per_timeslot[from][j];
        
        solution_expressed_by_modules_per_timeslot[from][j] = -1;
        
        // write to new place
        i = 0; 
        while (solution_expressed_by_modules_per_timeslot[to][i] != -1) i++;
        solution_expressed_by_modules_per_timeslot[to][i] = module;
        
        return solution;
    }
    /* Πόσοι φοιτητές είναι κοινοί σε δύο μαθήματα */
    protected void create_conflict_matrix(){
        int conflicts;
        
        for (int i = 0 ; i < numOfModules - 1; i++){            
            for (int j = i + 1 ; j < numOfModules ; j++){
                conflicts = 0;
                conflict_matrix[i][j] = conflicts;
                conflict_matrix[j][i] = conflicts;
                for (int ii = 0 ; ii < studentsPerModule[i].length ; ii++){
                    for (int jj = 0 ; jj < studentsPerModule[j].length ; jj++){
                        if (studentsPerModule[i][ii] == studentsPerModule[j][jj]) conflicts++;
                    }
                }
                conflict_matrix[i][j] = conflicts;
                conflict_matrix[j][i] = conflicts;
            }
        }
        generateCV();
    }
    protected void generateCV() {
        int M = numOfModules;

        for (int i = 0; i < M; i++) {
            int sum = 0;
            int cnt = 0;
            for (int j = 0; j < M; j++) {
                sum = sum + conflict_matrix[i][j];
                if (conflict_matrix[i][j] > 0)
                        cnt++;
            }
            conflict_vector_s[i] = sum;
            conflict_vector[i] = cnt;
        }
    }
    protected double computeCost(int[] solution) {
        if (!isFeasible(solution)) {
            //System.out.println("impossible!");
            return Double.MAX_VALUE;            
        }
        
        //int w[] = { 512, 256, 128, 32, 16, 8, 4, 2, 1 };
        int w[] = { 16, 8, 4, 2, 1 };
        
        double sum = 0.0;
        for (int exam_id1 = 0; exam_id1 < solution.length; exam_id1++) {
            int period_exam_id1 = solution[exam_id1];
            for (int exam_id2 = 0; exam_id2 < solution.length; exam_id2++) {
                int period_exam_id2 = solution[exam_id2];
                int d = conflict_matrix[exam_id1][exam_id2];
                if ((d > 0) && (period_exam_id1 < period_exam_id2)
                                && (period_exam_id2 <= period_exam_id1 + 5)) {
                        sum = sum + w[(period_exam_id2 - period_exam_id1) - 1] * d;
                    //sum += (period_exam_id2 - period_exam_id1) * d / 10; 
                }
            }
        }
        return sum / numOfStudents;
    }
    protected boolean isFeasible(int[] solution) {
        boolean result = true;
        for (int i = 0; i < solution.length; i++)
            for (int j = 0; j < i /*solution.length && j != i*/; j++) {
                int d = conflict_matrix[i][j];
                if ((solution[i] == solution[j]) && (d > 0)) {
                    //System.out.println("isFeasible: module " + i + " conflicting with module " + j);
                    //result = false;
                    return false;
                }
            }
        return result;//true;
    }
    public void print_conflicts_of_modules(int mod1, int mod2){
        System.out.println(conflict_matrix[mod1][mod2]);
    }
    protected void print_modules_per_timeslot(int modulesPerTimeslot[][]){
        //System.out.println("printing modulesPerTimeslot array");
        for (int i = 0 ; i < modulesPerTimeslot.length ; i++){
            System.out.print("Timeslot: " + i + ", modules: ");
            if (modulesPerTimeslot[i] == null) {
                System.out.println("");
                continue;
            }
            for (int j = 0 ; j < modulesPerTimeslot[i].length && modulesPerTimeslot[i][j] > -1 ; j++) System.out.print(" " + modulesPerTimeslot[i][j] );
            System.out.println("");
        }
    }
    public void print_students_of_module(int module){
        /*
        Στα αρχεία με τους φοιτητές και τα μαθήματα ο πρώτος αριθμός είναι το 1.
        Στους πίνακες το πρώτο στοιχείο είναι το 0. Επομένως ο πρώτος φοιτητής στα
        αρχεία έχει αριθμό 1, ενώ στον πίνακα βρίσκεται στη θέση 0. Αυτό πρέπει
        να το λαμβάνω πάντα υπόψιν μου κατά την επεξεργασία και την εμφάνιση των
        αποτελεσμάτων.        
        */
        System.out.println("Students for module " + (1+module));
        for (int st : studentsPerModule[module])
            System.out.print((1+st) + " ");
        System.out.println("");
    }
    protected int[][] create_students_per_module_array(){
        int spm[][] = new int[numOfModules][];
        ArrayList<Integer> []modules = new ArrayList[numOfModules]; // student id in arraylist of module [i]
        
        for (int i = 0 ; i < numOfModules ; i++) modules[i] = new ArrayList();

        for (int j = 0 ; j < numOfStudents ; j++){
            for (int module_id : studentsData[j]) { 
                modules[module_id].add(j);
            }            
        }
        
        for (int i = 0 ; i < numOfModules ; i++) {
            spm[i] = new int[modules[i].size()];
            for (int j = 0 ; j < modules[i].size() ; j++)
                spm[i][j] = modules[i].get(j);
        }
        return spm;
    }    
    public void print_solution(int sol[]){
        printProblemSynopsis();
        System.out.println("Solution: ");
        for (int i = 0 ; i < numOfModules ; i++) System.out.println(i + " " + sol[i]);
    }
    public void printProblemSynopsis(){
        System.out.println("modules:" + numOfModules + ", students: " + numOfStudents + ", total timeslots: " + totalTimeslots);
    }
    public void printData(){
        System.out.println("modules:" + modulesData.length + ", students: " + studentsData.length);
        System.out.println("Students data is:");
        for (int a[] : studentsData){
            for (int b: a)
                System.out.print(" "+b);
            System.out.println("");
        }
        System.out.println("Mofules data is:");
        for (int a[] : modulesData){
            for (int b: a)
                System.out.print(" " + b);
            System.out.println("");
        }
    }    
    protected int[][] loadModules(){
        int a[][];
        
        // πρώτα εξάγουμε κάθε γραμμή του αρχείου μαθημάτων σε ξεχωριστή εγγραφή ενός ArrayList
        ArrayList<String> contentsList = extract_file_contents_to_arraylist(modulesFilename);
         
        //System.out.println("modules:" + contentsList.size());
        
        // κάθε γραμμή του ArrayList μπαίνει σε πίνακα ακεραίων 
        a = extract_string_data_to_2D_array_of_ints(contentsList, true);
        
        return a;
    }    
    protected int[][] loadStudents(){
        int a[][];
        
        // πρώτα εξάγουμε κάθε γραμμή του αρχείου φοιτητών σε ξεχωριστή εγγραφή ενός ArrayList
        ArrayList<String> contentsList = extract_file_contents_to_arraylist(studentsFilename);
         
        //System.out.println("modules:" + contentsList.size());
        
         // κάθε γραμμή του ArrayList μπαίνει σε πίνακα ακεραίων 
        a = extract_string_data_to_2D_array_of_ints(contentsList, true);
        
        return a;
    }
    protected ArrayList<String> extract_file_contents_to_arraylist(String filename){
        // κατασκευάζω κενό ArrayList
        ArrayList<String> contentsList = new ArrayList();
        //String line;
        // ο χειρισμός αρχείων γίνεται σε try...catch για την σύλληψη εξαιρέσεων
        try{
            // άνοιγμα του αρχείου για ανάγνωση 
            Scanner fileScanner = new Scanner(new File(filename));
            
            // όσο δεν έχω φτάσει στο τέλος του αρχείου...
            while (fileScanner.hasNextInt()){
                // προσθέτω στη λίστα την γραμμή του αρχείου
                contentsList.add(fileScanner.nextLine());
            }  
            // κλείσιμο του αρχείου
            fileScanner.close();
        }
        // σύλληψη εξαίρεσης (αν δεν υπάρχει αρχείο κτλ)
        catch (IOException e){
            System.out.println(e.getStackTrace());
        }
        // επιστρέφω την λίστα
        return contentsList;
    }    
    // την λίστα θέλω να την μετατρέψω σε πίνακα ακεραίων δύο διαστάσεων
    protected int[][] extract_string_data_to_2D_array_of_ints(ArrayList<String> contentsList, boolean decrease_integer){
        String stringArray[];
        int a[][];
        int x;
        int lines = contentsList.size();
        
        // οι γραμμές του πίνακα είναι όσες και τα στοιχεία της λίστας
        a = new int[lines][];
        
        // για κάθε γραμμή...
        for (int i = 0 ; i < lines ; i++){
            // ο πίνακας stringArray είναι τύπου String. Πρώτα σε αυτόν κρατάω ως String τους αριθμούς κάθε γραμμής
            stringArray = contentsList.get(i).split(" ");
            
            // στην γραμμή i ο πίνακας ακεραίων a θα πρέπει να έχει τόσες στήλες όσες και ο πίνακας stringArray
            a[i] = new int[stringArray.length];
            
            // πρέπει κάθε στοιχείο του πίνακα stringArray να το μετατρέψω σε ακέραιο και να τον αποθηκεύσω στην αντίστοιχη στήλη του a
            for (int j = 0 ; j < stringArray.length ; j++){
                // εφόσον υπάρχει στοιχείο και δεν είναι κενό (η τελευταία γραμμή των αρχείων είναι κενό, και κρατάει μια κενή σειρά!)
                if (stringArray[j].length() > 0){
                    // μετατρέπω το String σε int
                    x = Integer.parseInt(stringArray[j]);               
                    if (x == 2824) System.out.println("here " +i + " " + studentsFilename);
                    // αποθηκεύω τον ακέραιο στον πίνακα ακεραίων
                    if (decrease_integer == true) a[i][j] = x - 1;// <----------- προσοχή στο -1 για διόρθωση
                    else a[i][j] = x;// <----------- προσοχή στο -1 για διόρθωση
                }
            }
        }
        return a;    
    }    
}
  