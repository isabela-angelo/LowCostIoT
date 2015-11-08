package com.allaboutee.httphelper_teste;

/**
 * Created by bela_ on 03-Nov-15.
 */
public class Globals {
    private static Globals instance;

    // Global variable
    private String resposta = "";
    private Boolean foi = false;

    // Restrict the constructor from being instantiated
    private Globals(){}

    public void setData(String d){
        this.resposta=d;
    }
    public void setData(Boolean d){
        this.foi=d;
    }
    public String getData(int n){
        if (n == 1) {
            return this.resposta;
        }
        if (n == 0) {
            return Boolean.toString(this.foi);
        }
        return "ERRO";
    }

    public static synchronized Globals getInstance(){
        if(instance==null){
            instance=new Globals();
        }
        return instance;
    }
}
