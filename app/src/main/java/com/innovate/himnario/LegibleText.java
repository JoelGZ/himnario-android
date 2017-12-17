package com.innovate.himnario;

/**
 * Created by Joel on 09-Aug-15.
 */
public class LegibleText {
    public static String tonalidad;
    public static String velocidad;

    public static void setTonalidad(String tonalidad, int flag) {
    /* when flag is 0 resultsActivity is calling
       when flag is 1 detailActivity is calling
     */

        switch (tonalidad){
            case "C":
                if (flag == 0){
                    LegibleText.tonalidad = "Do";
                } else {
                    LegibleText.tonalidad = "Do(C)";
                }
                break;
            case "Eb":
                if (flag == 0){
                    LegibleText.tonalidad = "Mi Bemol";
                } else {
                    LegibleText.tonalidad = "Mi Bemol(Eb)";
                }
                break;
            case "F":
                if (flag == 0){
                    LegibleText.tonalidad = "Fa";
                } else {
                    LegibleText.tonalidad = "Fa(F)";
                }
                break;
            case "G":
                if (flag == 0){
                    LegibleText.tonalidad = "Sol";
                } else {
                    LegibleText.tonalidad = "Sol(G)";
                }
                break;
            case "Ab":
                if (flag == 0){
                    LegibleText.tonalidad = "La Bemol";
                } else {
                    LegibleText.tonalidad = "La Bemol(Ab)";
                }
                break;
            case "Bb":
                if (flag == 0){
                    LegibleText.tonalidad = "Si Bemol";
                } else {
                    LegibleText.tonalidad = "Si Bemol(Bb)";
                }
                break;
            case "Do":
                LegibleText.tonalidad = "C";
                break;
            case "Mib":
                LegibleText.tonalidad = "Eb";
                break;
            case "Fa":
                LegibleText.tonalidad = "F";
                break;
            case "Sol":
                LegibleText.tonalidad = "G";
                break;
            case "Sib":
                LegibleText.tonalidad = "Bb";
                break;
            default:
                LegibleText.tonalidad = "Ton.";
        }

    }

    public static void setVelocidad(String velocidad) {
        switch (velocidad){
            case "R":
                LegibleText.velocidad = "Rapido";
                break;
            case "M":
                LegibleText.velocidad = "Medio";
                break;
            case "L":
                LegibleText.velocidad = "Lento";
                break;
        }
    }

    public static String getTonalidad() {
        return tonalidad;
    }

    public static String getVelocidad() {
        return velocidad;
    }
}
