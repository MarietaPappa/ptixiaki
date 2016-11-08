package timetablescheduling;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Module implements Comparator<Module>, Comparable<Module>{
    private final int module;
    private final Set<Module> conflicting_modules;
    private int timeslot;
    private final int conflict_matrix[];
    private final int MODULES;
    private int valence;
    
    public Module(int module_number, int conflict_matrix[]){
        module = module_number;
        this.conflict_matrix = conflict_matrix;   
        conflicting_modules = new HashSet();
        timeslot = -1;
        MODULES = conflict_matrix.length;
    }
    public void compute_conflicting_modules(ArrayList<Module> modules){
        int mdl;
        
        for (int i = 0 ; i < MODULES ; i++){
            mdl = modules.get(i).get_module_number();
            if (module != mdl && conflict_matrix[mdl] != 0){
                add_conflicting_module(modules.get(i));
            }
        }
        valence = conflicting_modules.size();
    }
    
    public Set<Module> get_conflicting_modules(){
        return conflicting_modules;
    }
    public boolean hasTimeslot() { 
        return timeslot > -1;
    }
    public void set_timeslot(int timeslot){
        if (this.timeslot != -1) throw new CannotAssignTimeslotException("Module " + module + " already set to timeslot " + this.timeslot);
        this.timeslot = timeslot;
    }
    public int get_timeslot(){
        return timeslot;
    }
    public int get_valence(){
        return valence;
    }
    public boolean notConnectedToAnyOf(ArrayList<Module>assigned_modules){
        for (Module m : assigned_modules)
            if (conflicting_modules.contains(m))
                return false;
        return true;
    }
    
    
    private void add_conflicting_module(Module conflicting_module){
        conflicting_modules.add(conflicting_module);
        valence = conflicting_modules.size();
    }
    
    public boolean conflicts_module(Module module){
        return conflicting_modules.contains(module);
    }
    
    public int get_module_number(){
        return module;
    }
 
    @Override
    public int compare(Module o1, Module o2) {
        return o1.valence - o2.valence; 
    }

    @Override
    public int compareTo(Module o) {
        return valence - o.valence;
    }

}