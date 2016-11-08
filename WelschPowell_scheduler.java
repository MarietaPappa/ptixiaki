/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package timetablescheduling;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author theo
 */
public class WelschPowell_scheduler extends Scheduler{
    
    public WelschPowell_scheduler(String studentsFilename, String modulesFilename, int totalTimeslots){
        super(studentsFilename, modulesFilename, totalTimeslots);
    }
    
    
         // https://www.kleemans.ch/static/fourcolors/welsh-powell.pdf
    public int[] create_initial_solution(){
        int current_timeslot, min_timeslots;
        Module current_module;
        int sol[] = new int[numOfModules]; 
        ArrayList<Module> all_modules = new ArrayList(numOfModules);
        ArrayList<Module> assigned_modules = new ArrayList(numOfModules);
        ArrayList<Module> list = new ArrayList(numOfModules);
        
        for (int i = 0 ; i < numOfModules ; i++) all_modules.add(new Module(i, conflict_matrix[i]));
        
        list.addAll(all_modules);
        
        for (Module m: all_modules) m.compute_conflicting_modules(all_modules);    
        
        Collections.sort(list);
        
        current_timeslot = 0;
        
        while (list.isEmpty() == false){
            for (int i = list.size() - 1 ; i > -1 ; i--){
                current_module = list.get(i);
                if (current_module.notConnectedToAnyOf(assigned_modules)){
                    current_module.set_timeslot(current_timeslot);
                    assigned_modules.add(current_module);
                }
            }
            current_timeslot++;
            list.removeAll(assigned_modules);
            assigned_modules.clear();
        }

        min_timeslots = current_timeslot;
        
        for (Module m: all_modules) sol[m.get_module_number()] = m.get_timeslot();
        
        solution_expressed_by_modules_per_timeslot = new int[min_timeslots][];
        for (int i = 0 ; i < min_timeslots ; i++){
            list.clear();
            for (Module m: all_modules)
                if (m.get_timeslot() == i)
                    list.add(m);
            
            solution_expressed_by_modules_per_timeslot[i] = new int[numOfModules];
            
            for (int j = 0 ; j < numOfModules ; j++)
                if (j < list.size())
                    solution_expressed_by_modules_per_timeslot[i][j] = list.get(j).get_module_number();
                else 
                    solution_expressed_by_modules_per_timeslot[i][j] = -1;
                    
            
            all_modules.removeAll(list);
        }
        
        return sol;
    }
}
