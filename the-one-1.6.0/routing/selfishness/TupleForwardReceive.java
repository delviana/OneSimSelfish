/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.selfishness;

/**
 *
 * @author Jarkom
 */
public class TupleForwardReceive {

    public ListPastForwards_O foward_o;

    /**
     * The end value
     */
    public ListPastReceive_I receive_i ;/**
             * Standard constructor that assigns s to start and e to end.
             *
             * @param s Initial start value
             * @param e Initial end value
             */

    public TupleForwardReceive(ListPastForwards_O o, ListPastReceive_I i) {
        foward_o = o;
        receive_i = i;
    }
}
