/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.awt.Dimension;
import java.awt.Toolkit;
import static javax.swing.JFrame.EXIT_ON_CLOSE;

/**
 *
 * @author Bishwajit
 */
public class Server {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        ServerJob objectServer = new ServerJob();
        objectServer.setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int)screenSize.getWidth();
        int height = (int)screenSize.getHeight();
        objectServer.setLocation((width-objectServer.getWidth())/2, (2*height/3-objectServer.getHeight())/2);
        objectServer.setVisible(true);
        objectServer.setResizable(false);
    }
    
}
