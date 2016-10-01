/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bishwajit
 */

class PairInt{
    int a, b;
    PairInt(int a, int b){
        this.a = a;
        this.b = b;
    }
}

public class ServerJob extends javax.swing.JFrame {

    /**
     * Creates new form ServerJob
     */
    private ServerSocket socketServer;
    private boolean serverIsClosed;
    private Thread serverThread;
    private int serverPortAddress = 52337;
    private static Vector<PlayerInfo> availablePlayerList;
    
    

    public class PlayerData {

        public String playerName;
        public String playerIpAddress;
        public int playerPortAddress;

        PlayerData() {
            playerName = null;
            playerIpAddress = null;
            playerPortAddress = -1;
        }
    }

    public class PlayerInfo implements Runnable {

        /**
         * PlayerData
         */
        private String playerName;
        private String playerIpAddress;
        private int playerPortAddress;
        private int pile1, pile2;
        /**
         * for client
         */

        private PlayerInfo opponent;
        private Socket connection;
        private BufferedReader input;
        private PrintWriter output;
        private Thread playerThread;

//        private GameClass game;
        private boolean isClientOut;
        private boolean isClientBusy;
        private boolean haveOpponent;
        
        public void initPiles(int p1, int p2){
            this.pile1 = p1;
            this.pile2 = p2;
        }
        
        public PairInt getPiles(){
            PairInt ret = new PairInt(pile1, pile2);
            return ret;
        }

        public PlayerInfo(Socket socketConnector) {
            this.connection = socketConnector;
            isClientOut = false;
            isClientBusy = false;
            haveOpponent = false;
            playerIpAddress = this.connection.getInetAddress().getHostAddress();
            playerPortAddress = this.connection.getPort();
            try {
                input = new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
                output = new PrintWriter(this.connection.getOutputStream(), true);

                String token = (String) input.readLine();
                //System.out.println(token);

                if (token.equals("NAME")) {
                    playerName = (String) input.readLine();
                    //System.out.println(playerName);
                }

                printAlist();
            } catch (IOException ex) {
                isClientOut = true;
                Logger.getLogger(ServerJob.class.getName()).log(Level.SEVERE, null, ex);
            }
            playerThread = new Thread(this);
            playerThread.start();
        }

        void printAlist() {
            String token = "Alist";
            output.println(token);
            int total = availablePlayerList.size();
            output.println(total);
            availablePlayerList.stream().map((it) -> {
                output.println(it.playerName);
                return it;
            }).map((it) -> {
                output.println(it.playerIpAddress);
                return it;
            }).forEach((it) -> {
                output.println(it.playerPortAddress);
            });
        }

        void printPlayerInfo() {
            System.out.println(":::::::::::::::::::::");
            availablePlayerList.stream().forEach((it) -> {
                System.out.println(it.playerName + " " + it.playerIpAddress + " " + it.playerPortAddress);
            });
            System.out.println(":::::::::::::::::::::");
        }

        public int stringToInt(String str) {
            int ret = 0;
            int sz = str.length();
            for (int i = 0; i < sz; i++) {
                ret = (ret * 10) + (str.charAt(i) - '0');
                //System.out.println("---> " + (str.charAt(i) - '0'));
            }
            return ret;
        }

        void readPlayerData(PlayerData reqPlayer) {
            try {
                String token = input.readLine();
                reqPlayer.playerName = token;

                token = input.readLine();
                reqPlayer.playerIpAddress = token;

                token = input.readLine();
                reqPlayer.playerPortAddress = stringToInt(token);
                //System.out.println("req player: "+reqPlayer.playerName+" "+reqPlayer.playerIpAddress+" "+reqPlayer.playerPortAddress);

            } catch (IOException ex) {
                Logger.getLogger(ServerJob.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        boolean isPlayerIsFree(PlayerData reqPlayer) {
            for (PlayerInfo it : availablePlayerList) {
                if (it.playerName.equals(reqPlayer.playerName)
                        && it.playerIpAddress.equals(reqPlayer.playerIpAddress)
                        && it.playerPortAddress == reqPlayer.playerPortAddress
                        && it.isClientBusy == false && it.isClientOut == false) {
                    opponent = it;
                    return true;
                }
            }
            return false;
        }

        void sendOpponentReq(PlayerData reqPlayer) {
            Random generator = new Random();
            int pile1 = generator.nextInt() % 20;
            int pile2 = generator.nextInt() % 20;
            pile1 += 20;
            pile2 += 20;
            System.out.println("server initialized, pile1: " + pile1 + " pile2: " + pile2);
            opponent.output.println("RequestToPlay");
            opponent.output.println(playerName);
            Integer p1 = pile1, p2 = pile2;
            opponent.output.println(p1.toString());
            opponent.output.println(p2.toString());
        }

        void removeMe() {
            int total = availablePlayerList.size();
            for (int i = 0; i < total; i++) {
                PlayerInfo it = availablePlayerList.elementAt(i);
                if (it.playerName.equals(playerName) && it.playerIpAddress.equals(playerIpAddress) && it.playerPortAddress == playerPortAddress) {
                    System.out.println(it.playerName + " " + it.playerIpAddress + " " + it.playerPortAddress);
                    availablePlayerList.remove(i);
                    break;
                }
            }
            printPlayerInfo();
        }

        private void disposeThisThread() {
            if (!Thread.interrupted()) {
                this.playerThread.interrupt();
            }
            try {
                this.playerThread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerJob.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
        
        
        private void sendMessage(String message){
            opponent.output.println("Message");
            opponent.output.println(message);
        }

        @Override
        public void run() {
            try {
                Thread.sleep(500);
                while (!isClientOut) {
                    String token = null;
                    try {
                        token = (String) input.readLine();
                        if(!token.equals("Alist"))System.out.println("token in run: "+token+" "+playerName);
                        if (token.equals("Exit")) {
                            isClientOut = true;
                            removeMe();
                            output.println("OkExit");
                            output.flush();
                            output.close();
                            input.close();
                            /**
                             * if client in a game notify opponent
                             */
                        } else if (token.equals("Alist")) {
                            printAlist();
                        } else if (token.equals("PlayRequest")) {
                            if (isClientBusy) {
                                /**
                                 * Can't play :P
                                 */
                            } else {
                                isClientBusy = true;
                                PlayerData reqPlayer = new PlayerData();
                                readPlayerData(reqPlayer);
                                boolean isPlayerFree = isPlayerIsFree(reqPlayer);
                                if (isPlayerFree) {
                                    opponent.isClientBusy = true;
                                    opponent.opponent = this;
                                    sendOpponentReq(reqPlayer);
                                } else {
                                    isClientBusy = false;
                                    output.println("PlayerNotFree");
                                }
                            }
                        } else if (token.equals("RequestAccepted")) {
                            opponent.output.println("OpponentAccepted");
                            token = input.readLine();
                            //int pail1 = (int) Integer.parseInt(token);
                            String token2 = input.readLine();
                            //int pail2 = (int) Integer.parseInt(token2);
                            //opponent.initPiles(pail1, pail2);
                            opponent.output.println(token);
                            opponent.output.println(token2);
                            haveOpponent = true;
                            opponent.haveOpponent = true;
                        } else if (token.equals("RequestDeclined")) {
                            opponent.output.println("OpponentDeclined");
                            isClientBusy = false;
                            opponent.isClientBusy = false;
                        }
                        /**
                         * players on a game
                         */
                        else{
                            if(haveOpponent){
                                if(token.equals("ShipPos")){
                                    System.out.println("hello");
                                    Vector<Integer> vectorVar = new Vector<>();
//                                    readShipPosition(vectorVar);//read from me
//                                    writeShipPosition(vectorVar);//write to opponent
                                } else if(token.equals("Message")){
                                    String newMessage = input.readLine();
                                    String extraLine = input.readLine();
                                    System.out.println(newMessage);
                                    sendMessage(newMessage);
                                } else if(token.equals("MyMove")){
                                    token=input.readLine();
                                    opponent.output.println("OpMove");
                                    opponent.output.println(token);
                                } else if(token.equals("HitShip")){
                                    opponent.output.println("HitShip");
                                } else if(token.equals("MissShip")){
                                    opponent.output.println("MissShip");
                                } else if(token.equals("Lose")){
                                    opponent.output.println("Win");
                                } else if(token.equals("GameOver")){
                                    haveOpponent = false;
                                    isClientBusy = false;
                                    output.println("OkGameOver");
                                }
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(ServerJob.class.getName()).log(Level.SEVERE, null, ex);
                        removeMe();
                        isClientOut = true;
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerJob.class.getName()).log(Level.SEVERE, null, ex);
            }
            finally{
                try {
                    output.close();
                    input.close();
                    connection.close();
                    System.out.println(connection.isConnected());
                } catch (IOException ex) {
                    Logger.getLogger(ServerJob.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public class startServer implements Runnable {

        startServer() {
            availablePlayerList = new Vector<>();
            serverThread = new Thread(this);
            serverThread.start();
        }

        @Override
        public void run() {
            try {
                Thread.sleep((long) 1000);
                if (socketServer == null) {
                    socketServer = new ServerSocket(serverPortAddress, 100);
                }
                while (!serverIsClosed) {
                    try {
                        if (socketServer.isClosed()) {
                            socketServer = new ServerSocket(serverPortAddress, 100);
                        }
                        //System.out.println("hi");
                        String myIpAddress = InetAddress.getLocalHost().getHostAddress();
                        //System.out.println(myIpAddress);

                        PlayerInfo newPlayer = new PlayerInfo(socketServer.accept());
                        availablePlayerList.add(newPlayer);
                        String host = newPlayer.playerIpAddress;
                        //newPlayer.printPlayerInfo();

                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        try {
                            socketServer.close();
                        } catch (IOException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(ServerJob.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public ServerJob() {
        super("WELCOME");
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ServerJob.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        initComponents();
        
        setLocationRelativeTo(null);
        this.serverIsClosed = true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        backgroundPanel = new javax.swing.JPanel();
        startServerButton = new javax.swing.JButton();
        exitServerButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        backgroundPanel.setBackground(new java.awt.Color(255, 0, 0));
        backgroundPanel.setPreferredSize(new java.awt.Dimension(250, 160));

        startServerButton.setText("START");
        startServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startServerButtonActionPerformed(evt);
            }
        });

        exitServerButton.setText("STOP");
        exitServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitServerButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(startServerButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                .addComponent(exitServerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36))
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addGap(58, 58, 58)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exitServerButton)
                    .addComponent(startServerButton))
                .addContainerGap(79, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, 225, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private startServer startServerObject;

    private void startServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startServerButtonActionPerformed
        // TODO add your handling code here:
        System.out.println(evt.getActionCommand());
        if (serverIsClosed) {
            serverIsClosed = false;
            startServerObject = new startServer();
        }
    }//GEN-LAST:event_startServerButtonActionPerformed

    private void exitServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitServerButtonActionPerformed
        // TODO add your handling code here:
        System.out.println(evt.getActionCommand());
        if (!serverIsClosed) {
            serverIsClosed = true;
        }
    }//GEN-LAST:event_exitServerButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton exitServerButton;
    private javax.swing.JButton startServerButton;
    // End of variables declaration//GEN-END:variables

}
