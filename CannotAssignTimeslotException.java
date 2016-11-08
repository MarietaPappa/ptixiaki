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
public class CannotAssignTimeslotException extends RuntimeException {

    /**
     * Creates a new instance of <code>CannotAssignTimeslotException</code>
     * without detail message.
     */
    public CannotAssignTimeslotException() {
        System.out.println("Cannot assign module to entry");
    }

    /**
     * Constructs an instance of <code>CannotAssignTimeslotException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public CannotAssignTimeslotException(String msg) {
        super(msg);
    }
}
