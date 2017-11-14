package com.vannakittikun.alphafitness;

/**
 * Created by Rule on 11/13/2017.
 */

public class User {
    private String name;
    private String gender;
    private int weight;

    public User(String n, String g, int w){
        this.name = n;
        this.gender = g;
        this.weight = w;
    }

    public User(){
        super();
    }

    public void setName(String name){
        this.name = name;
    }

    public void setGender(String gender){
        this.gender = gender;
    }

    public void setWeight(int weight){
        this.weight = weight;
    }

    public String getName(){
        return this.name;
    }

    public String getGender(){
        return this.gender;
    }

    public int getWeight(){
        return this.weight;
    }
}
