package com.yichang.chuanyin.server;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Controler {
   public static void main(String[] args){
       try {
           Server server = new Server();
           while (true) {
               server.connetUser();
           }
       } catch (Exception ex) {
           Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
       }
   }
}
