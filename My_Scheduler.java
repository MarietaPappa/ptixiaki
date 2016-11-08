package timetablescheduling;

import java.util.ArrayList;

public class My_Scheduler extends Scheduler{
    
    public My_Scheduler(String studentsFilename, String modulesFilename, int totalTimeslots){
        super(studentsFilename, modulesFilename, totalTimeslots);
    }
    
    
    public int[] create_initial_solution(){
       int sol[] = new int[numOfModules]; // gia kathe mathima se poio timeslot tha einai
       int modulesPerTimeslot[][] = new int[totalTimeslots][numOfModules];
       ArrayList<Integer> modules_with_same_number_of_conflicts = new ArrayList();
       ArrayList<Module_Info> conflicting_modules = new ArrayList();
       int unassigned_module, ceiling = numOfModules, max_conflicts, pos_in_modulesPerTimeslot;;

       for (int i = 0 ; i < totalTimeslots ; i++)
           for (int j = 0 ; j < numOfModules ; j++)
               modulesPerTimeslot[i][j] = -1;

       for (int i = 0 ; i < numOfModules ; i++){
           if (msg1) System.out.print("i=" + i + ": ");
           max_conflicts = 0;
           modules_with_same_number_of_conflicts.clear();

           for (int j = 0 ; j < numOfModules ; j++){
               if (conflict_vector[j] > max_conflicts && conflict_vector[j] < ceiling){
                   modules_with_same_number_of_conflicts.clear();
                   modules_with_same_number_of_conflicts.add(j);
                   max_conflicts = conflict_vector[j];
               }
               else if (conflict_vector[j] == max_conflicts){
                   modules_with_same_number_of_conflicts.add(j);
               }
           }
           ceiling = max_conflicts;

           if (msg) System.out.println("There are " + modules_with_same_number_of_conflicts.size() + " modules with " + ceiling + " conflicts each");

           for (int k = 0 ; k < modules_with_same_number_of_conflicts.size() ; ++k){
               unassigned_module = modules_with_same_number_of_conflicts.get(k);
               conflicting_modules.clear(); // poia modules exoun siggrousi me to unassigned_module??

               conflicting_modules = try_assign_module_to_available_timeslot_easy(unassigned_module, sol, modulesPerTimeslot);

               if (!conflicting_modules.isEmpty()){
                   if (msg) { System.out.println("problem at module " + unassigned_module); print_modules_per_timeslot(modulesPerTimeslot); }

                   int timeslot = new_timeslot_after_moving_conflicting_modules(sol, modulesPerTimeslot, conflicting_modules);
                   if (timeslot == -1) { System.out.println("cannot solve"); return null; }
                   else {
                       for (pos_in_modulesPerTimeslot = 0 ; pos_in_modulesPerTimeslot < numOfModules && modulesPerTimeslot[timeslot][pos_in_modulesPerTimeslot] > -1 ; pos_in_modulesPerTimeslot++); // afto prepei na meinei os exei oposdipote!!!
                       assign_module_to_timeslot(timeslot, unassigned_module, pos_in_modulesPerTimeslot, sol, modulesPerTimeslot);
                   }
               }
           }
           i += modules_with_same_number_of_conflicts.size() - 1;
       }

       solution = sol;
       solution_expressed_by_modules_per_timeslot = modulesPerTimeslot;
       System.out.println("Initial solution:");
       print_modules_per_timeslot(modulesPerTimeslot);
       return solution;
    }
    
    private ArrayList<Module_Info> try_assign_module_to_available_timeslot_easy(int unassigned_module, int sol[], int modulesPerTimeslot[][]){
       ArrayList<Module_Info> conflicting_modules = new ArrayList();
       boolean foundTimeslot;
       int assigned_module;

       for (int timeslot = 0 ; timeslot < totalTimeslots ; timeslot++){
           foundTimeslot = true;
           for (assigned_module = 0 ; assigned_module < numOfModules && modulesPerTimeslot[timeslot][assigned_module] > -1 ; assigned_module++){
              if (conflict_matrix[unassigned_module][modulesPerTimeslot[timeslot][assigned_module]] != 0){
                   foundTimeslot = false;
                   conflicting_modules.add(new Module_Info(modulesPerTimeslot[timeslot][assigned_module], timeslot)); 
               }
           }
           if (foundTimeslot == true){
               assign_module_to_timeslot(timeslot, unassigned_module, assigned_module, sol, modulesPerTimeslot);
               conflicting_modules.clear();
               return conflicting_modules;
           }
       }
       return conflicting_modules;
   }
    
    private void assign_module_to_timeslot(int timeslot, int new_module, int pos_in_modulesPerTimeslot, int sol[], int modulesPerTimeslot[][]){
       modulesPerTimeslot[timeslot][pos_in_modulesPerTimeslot] = new_module;
       sol[new_module] = timeslot;
       if (msg1) System.out.println("Module " + new_module + " put on timeslot " + sol[new_module]);
   }
   /*
   Κάποιο μάθημα δεν μπορεί να μπει σε κάνενα χρόνο. Για αυτό έχουμε μαζέψει όλα τα μαθήματα που συγκρούονται με το συγκεκριμένο μάθημα στη μεταβλητή conflicting_modules και θα
   προσπαθήσουμε να δούμε αν μπορούμε να τα μετακινήσουμε σε άλλους χρόνους. Η σκέψη είναι η εξής: ο αλγόριθμος μέχρι στιγμής τοποθετεί κάθε μάθημα στην πρώτη χρονοσιχμή που μπορεί.
   Ίσως κάποιο μάθημα να μπορεί να τοποθετηθεί και σε επόμενες χρονοσχισμές και να ελευθερώουμε την χρονοσχισμή που ήδη βρίσκεται. Για να έχει αποτέλεσμα αυτό πρέπει για μια συγκεκριμένη
   χρονοσχισμή να μαζέψουμε όλα τα μαθήματα που εμφανίζουν σύγκρουση με το συγκεκριμένο και να δούμε αν μπορούμε όλα να τα τοποθετήσουμε σε νέα χρονοσχισμή. Δεν είναι απαραίτητο να
   τοποθετηθούν όλα στην ίδια χρονοσχισμή. Θα πρέπει όμως στην νέα τους χρονοσχισμή να μην δημιουργούν συγκρούσεις.
   */
   private int new_timeslot_after_moving_conflicting_modules(int sol[], int modulesPerTimeslot[][], ArrayList<Module_Info> conflicting_modules){
       ArrayList<Module_Info> helper = new ArrayList();
       ArrayList<Module_Info> new_conflicting_modules = new ArrayList();
       //int min_conflicts = totalTimeslots, timeslot_with_min_conflicts = -1, counter = 1;
       int array[][];// = new int[totalTimeslots][];
       int module_to_change, module2, moved_modules;//, ts; // timeslot
       boolean foundTimeslot = true;//, r = true;

       if (conflicting_modules.isEmpty())System.out.println("Error! Empty conflicting list");
       //print_modules_per_timeslot(modulesPerTimeslot); Collections.sort(conflicting_modules);
       array = get_2D_array_of_conflicting_modules_per_timeslot(conflicting_modules);// make array[timeslot][module_that_conflicts]
       //for (module_info a: conflicting_modules)System.out.println("->" + a.module_number);
       if (msg) { System.out.println("List of conflicts is..."); print_modules_per_timeslot(array); }

       for (int timeslot = 0 ; timeslot < totalTimeslots ; timeslot++){
           moved_modules = 0;
           helper.clear();
           if (array[timeslot] == null) continue;
           for (int j = 0 ; j < array[timeslot].length ; j++ ){
               module_to_change = array[timeslot][j];
               for (int k = timeslot + 1 ; k < totalTimeslots ; k++){
                   new_conflicting_modules.clear();
                   foundTimeslot = true;
                   for (module2 = 0 ; module2 < numOfModules && modulesPerTimeslot[k][module2] > -1 ; module2++){
                      if (conflict_matrix[module_to_change][modulesPerTimeslot[k][module2]] != 0){
                           foundTimeslot = false;
                           new_conflicting_modules.add(new Module_Info(modulesPerTimeslot[k][module2], k));// int module, int timeslot
                           if (msg) {System.out.println("Problem reassigning module " + module_to_change + " from timeslot " + timeslot + " to timeslot " + k + " conflicting with module " + modulesPerTimeslot[k][module2]); }
                           //reassign_module(modulesPerTimeslot, k, modulesPerTimeslot[k][module2]); <--------------------------------------------------------------------
                           //break;
                       }
                   }
                   if (foundTimeslot == true){
                       if (msg) { System.out.println("Module " + module_to_change + " from " + timeslot + " to " + k); }
                       helper.add(new Module_Info(module_to_change, timeslot, k, module2));
                       moved_modules++;
                       break;
                   }
               }
               if (moved_modules == array[timeslot].length) break;
           }

           if (moved_modules == array[timeslot].length) {
               move_list_of_modules_to_their_new_slots(helper , sol,modulesPerTimeslot );
               return timeslot;
           }
       }
       return -1;
   }
   private int[][] get_2D_array_of_conflicting_modules_per_timeslot(ArrayList<Module_Info> conflicting_modules){
       ArrayList<Module_Info> helper = new ArrayList();
       int timeslot, array[][] = new int[totalTimeslots][];

       for (int i = 0 ; i < conflicting_modules.get(0).timeslot ; i++) array[i] = null;

       for (int i = 0 ; i < conflicting_modules.size() ; ){
           timeslot = conflicting_modules.get(i).timeslot;

           while ( i < conflicting_modules.size() && timeslot == conflicting_modules.get(i).timeslot){
               helper.add(conflicting_modules.get(i));
               i++;
           }
           array[timeslot] = new int[helper.size()];
           for (int j = 0 ; j < helper.size() ; j++) array[timeslot][j] = helper.get(j).module_number;
           helper.clear();
       }
       return array;
   }
   private void move_one_module(Module_Info module_to_change_obj, int sol[], int modulesPerTimeslot[][]){
       int module_to_change;

       module_to_change = module_to_change_obj.module_number;
       if (msg) { System.out.println("change module " + module_to_change + " from " + module_to_change_obj.timeslot + " to " + module_to_change_obj.new_timeslot); }
       sol[module_to_change] = module_to_change_obj.new_timeslot;

       while (module_to_change_obj.index_at_modulesPerTimeslot < numOfModules &&
                modulesPerTimeslot[module_to_change_obj.new_timeslot][module_to_change_obj.index_at_modulesPerTimeslot] > -1 )
                    module_to_change_obj.index_at_modulesPerTimeslot++;

       if (module_to_change_obj.index_at_modulesPerTimeslot == numOfModules) {
           System.out.println("Error at moving conflicting module to new position");
           System.exit(1);
       }
        modulesPerTimeslot[module_to_change_obj.new_timeslot][module_to_change_obj.index_at_modulesPerTimeslot] = module_to_change;

       int start_index = 0, end_index;

       while (modulesPerTimeslot[module_to_change_obj.timeslot][start_index] != module_to_change) start_index++;

       end_index = start_index;
       while (modulesPerTimeslot[module_to_change_obj.timeslot][end_index] != -1) end_index++;

        modulesPerTimeslot[module_to_change_obj.timeslot][start_index] = modulesPerTimeslot[module_to_change_obj.timeslot][--end_index];
        modulesPerTimeslot[module_to_change_obj.timeslot][end_index] = -1;
   }
   private void move_list_of_modules_to_their_new_slots(ArrayList<Module_Info> list_of_modules_to_change, int sol[], int modulesPerTimeslot[][] ){
       //int module_to_change;

       if (msg) { System.out.println("All conflicting modules are moved to other slots! array size: " + list_of_modules_to_change.size()); }
       for (int k = 0 ; k < list_of_modules_to_change.size() ; k++){
           move_one_module(list_of_modules_to_change.get(k), sol, modulesPerTimeslot);
       }
   }
   
    
}
