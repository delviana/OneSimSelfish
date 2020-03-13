/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing;

import java.util.Scanner;

/**
 *
 * @author Jarkom
 */
public class Coba {
    
    public static void main(String[] args) {
     Scanner x = new Scanner(System.in);
        int alas;
        int tinggi;
        
        System.out.print("Alas = ");
        int Alas = x.nextInt();
        
        System.out.print("Tinggi = ");
        int Tinggi = x.nextInt();
        
        double total = 0.5 * Alas * Tinggi;
        System.out.println("Hasil : "+ total);
    }
}
