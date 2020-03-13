/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

/**
 *
 * @author jarkom
 */
public class Marker {

    private static Marker myinstance = null;
    public static final String MARK_PREFIX = "markPrefix";
    public static String prefix;

    public Marker() {
        Settings s = new Settings("Marker");
        prefix = s.getSetting(MARK_PREFIX);
    }

    public String getMarkPrefix() {
        return prefix;
    }

    /**
     * Returns the SimScenario instance and creates one if it doesn't exist yet
     */
    public static Marker getInstance() {
        if (myinstance == null) {
            myinstance = new Marker();
        }
        return myinstance;
    }
}
