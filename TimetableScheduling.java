
package timetablescheduling;


public class TimetableScheduling {
    private static final String DIR = "Toronto/";//carter_datasets/";
    private static final String files[][] = {   
        {"car-s-91", "35"}, 
        {"car-f-92", "32"}, 
        {"ear-f-83", "24"}, 
        {"hec-s-92", "18"}, 
        {"kfu-s-93", "20"}, 
        {"lse-f-91", "18"}, 
        {"pur-s-93", "42"}, 
        {"rye-s-93", "23"}, 
        {"sta-f-83", "13"}, 
        {"tre-s-92", "23"}, 
        {"uta-s-92", "35"}, 
        {"ute-s-92", "10"},
        {"yor-f-83", "21"},  
    };
    
    public static void main(String[] args) {
        int timeslots;
        Scheduler scheduler;
        String students_file, courses_file, problem_name;
        
        for (String [] line: files){
            problem_name = line[0];
            timeslots = Integer.parseInt(line[1]);
            
            System.out.println("Beginning solution for problem: " + problem_name + " with " + timeslots + " timeslots");
           
            students_file = DIR + problem_name + ".stu";
            courses_file = DIR + problem_name + ".crs";
            
            scheduler = new My_Scheduler(students_file, courses_file, timeslots);
            scheduler.printProblemSynopsis();
            scheduler.schedule();
            
            System.out.println("======================");
        }
        
        for (String [] line: files){
            problem_name = line[0];
            timeslots = Integer.parseInt(line[1]);
            
            System.out.println("Beginning solution for problem: " + problem_name + " with " + timeslots + " timeslots");
           
            students_file = DIR + problem_name + ".stu";
            courses_file = DIR + problem_name + ".crs";
            
            scheduler = new WelschPowell_scheduler(students_file, courses_file, timeslots);
            scheduler.printProblemSynopsis();
            scheduler.schedule();
            
            System.out.println("======================");
        }
    }    
}
