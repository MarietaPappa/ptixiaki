/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package timetablescheduling;

/**
 *
 * @author theo
 */
public class Module_Info {
   public int module_number;
   public int timeslot;
   public int new_timeslot;
   public int index_at_modulesPerTimeslot;

   public Module_Info(int module, int timeslot){
       module_number = module;
       this.timeslot = timeslot;
       new_timeslot = 0;
   }

   public Module_Info(int module, int timeslot, int new_timeslot, int index_at_modulesPerTimeslot){
       module_number = module;
       this.timeslot = timeslot;
       this.new_timeslot = new_timeslot;
       this.index_at_modulesPerTimeslot = index_at_modulesPerTimeslot;
   }
   /*
       @Override
       public int compare(Module_Info t1, Module_Info t2) {
           return t1.timeslot - t2.timeslot;//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
       }

       @Override
       public int compareTo(Module_Info t) {
           return timeslot - t.timeslot;//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
       }
       */
}
