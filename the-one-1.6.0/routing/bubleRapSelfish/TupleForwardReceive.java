/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.bubleRapSelfish;

/**
 *
 * @author Jarkom
 */
public class TupleForwardReceive {

    public ListPastForwards_O forward_o;

    /**
     * The end value
     */
    public ListPastReceive_I receive_i ;/**
             * Standard constructor that assigns o to forward and i to receive.
             *
             * @param o Initial forward value
             * @param i Initial receive value
             */

    public TupleForwardReceive(ListPastForwards_O o, ListPastReceive_I i) {
        forward_o = o;
        receive_i = i;
    }
}
