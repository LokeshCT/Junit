package com.rsqe.database.utils.export;

import com.rsqe.database.utils.DatabaseValues;
import com.rsqe.database.utils.FileLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseCsvExporter {

    public static void main(String[] args) throws Exception {
        final DatabaseValues databaseValues = new DatabaseValues(DatabaseValues.Direction.export);
        //List<String> names = FileLoader.asList("sqe_tables.txt");
        //new OracleStreamingCsvExporter().export(names, databaseValues, databaseValues.getPath());
        final Map<String, List<String>> names = FileLoader.asMultipleList("sqe_tables.txt");
        for(final String st : names.keySet()){
            //new OracleStreamingCsvExporter().export(names.get(st), databaseValues, databaseValues.getPath());
            new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try{
                            System.out.println("Thread {"+ Thread.currentThread().getName()+"}, Started!" );
                            //new OracleStreamingCsvExporter().export(names.get(st), databaseValues, databaseValues.getPath());

                            //To print out table names by set one table per list
                            for(String tableName:names.get(st)){
                                try{

                                    List tempL = new ArrayList<String>();
                                    tempL.add(tableName);
                                    new OracleStreamingCsvExporter().export(tempL, databaseValues, databaseValues.getPath());
                                }catch (Exception e){
                                    synchronized (DatabaseCsvExporter.class){
                                        System.out.println("===============================================");
                                        System.out.println("Thread {"+ Thread.currentThread().getName()+"}, Wrong Table name: "+tableName);
                                        System.out.println(""+e.getMessage());
                                        System.out.println("===============================================");
                                    }
                                }
                            }
                            System.out.println("Thread {"+ Thread.currentThread().getName()+"}, Completed!" );
                        }
                        catch(Exception e){
                            throw new RuntimeException(e);
                        }
                    }
                }, "processing: "+st
            ).start();
        }
    }
}