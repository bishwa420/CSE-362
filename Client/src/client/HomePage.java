/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;

/**
 *
 * @author Bishwajit
 */

class IPAddressValidator{

    private Pattern pattern;
    private Matcher matcher;

    private static final String IPADDRESS_PATTERN =
		"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    public IPAddressValidator(){
	  pattern = Pattern.compile(IPADDRESS_PATTERN);
    }

   /**
    * Validate ip address with regular expression
    * @param ip ip address for validation
    * @return true valid ip address, false invalid ip address
    */
    public boolean validate(final String ip){
	  matcher = pattern.matcher(ip);
	  return matcher.matches();
    }
}

public class HomePage extends javax.swing.JFrame {

    /**
     * Creates new form HomePage
     */
    public String name="";
    public String ip = "";
    IPAddressValidator validator;
    public int serverPort = 52337;
    private ShowListFrom objectShowList;
    private PlayerInfo myPlayerInfo;
    static int pile1, pile2;
    public HomePage() {
        super("WELCOME");
        validator = new IPAddressValidator();
        initComponents();
        init();
    }
    
    void init(){
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }
    
    public class PlayerData {

        public String playerName;
        public String playerIpAddress;
        public int playerPortAddress;

        PlayerData(String pn, String pi, int pp) {
            playerName = pn;
            playerIpAddress = pi;
            playerPortAddress = pp;
        }
    }

    public class PlayerInfo implements Runnable {

        public String playerName;
        public String playerIpAddress;
        public int playerPortAddress;

        public PlayerData opponent;
        public Socket connection;
        public BufferedReader input;
        public PrintWriter output;
        public Thread playerThread;
        public boolean playerIsConnected;
        public boolean isPlaying;
        public Vector<Integer> opShipPos;

        public Vector<PlayerData> availablePlayerList;
        private boolean isFrameOpen;

        public PlayerInfo(Socket socketConnector) {
            isFrameOpen = false;
            isPlaying = false;
            opShipPos = new Vector<>();
            this.connection = socketConnector;
            playerIsConnected = true;
            availablePlayerList = new Vector<>();
            opponent = new PlayerData("", "", -1);

            try {
                this.playerName = name;
                playerIpAddress = this.connection.getInetAddress().getLocalHost().getHostAddress();
                playerPortAddress = this.connection.getLocalPort();

                input = new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
                output = new PrintWriter(this.connection.getOutputStream(), true);

                output.println("NAME");
                output.println(playerName);
                String token = (String) input.readLine();
                //System.out.println(token);
                if (token.equals("Alist")) {
                    readAlist();
                    printPlayerInfo();
                }
            } catch (IOException ex) {
                Logger.getLogger(HomePage.class.getName()).log(Level.SEVERE, null, ex);
            }
            playerThread = new Thread(this);
            playerThread.start();
//            System.out.println("outside Thread");
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

        public void readAlist() {
            String token;
            int total;
            try {
                token = input.readLine();
                total = stringToInt(token);
                //System.out.println("total : " + total);
                availablePlayerList = new Vector<>();
                for (int i = 0; i < total; i++) {
                    PlayerData player = new PlayerData("", "", -1);
                    player.playerName = input.readLine();
                    player.playerIpAddress = input.readLine();
                    token = input.readLine();
                    //System.out.println("token " + token);
                    player.playerPortAddress = stringToInt(token);
                    //System.out.println("***** " + player.playerName + " " + player.playerIpAddress + " " + player.playerPortAddress);
                    availablePlayerList.add(player);
                }
                //printPlayerInfo();
            } catch (IOException ex) {
                Logger.getLogger(HomePage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void removeMe() {
            int total = availablePlayerList.size();
            for (int i = 0; i < total; i++) {
                PlayerData it = availablePlayerList.elementAt(i);
                if (it.playerName.equals(playerName) && it.playerIpAddress.equals(playerIpAddress) && it.playerPortAddress == playerPortAddress) {
                    availablePlayerList.remove(i);
                    playerIsConnected = false;
                    break;
                }
            }
        }

        public void printPlayerInfo() {
            //System.out.println(":::::::::::::::::::::");
            availablePlayerList.stream().forEach((it) -> {
                //System.out.println(it.playerName + " " + it.playerIpAddress + " " + it.playerPortAddress);
            });
            //System.out.println(":::::::::::::::::::::");
        }

        
        private void showSubmitGrid(int p) {
            objectShowList.showSubmitGrid(p);
        }
        

        
        @Override
        public void run() {
            try {
                Thread.sleep(500);
                while (playerIsConnected == true) {
//                    System.out.println("Player is connected!");
                    try {
                        if (isFrameOpen == false) {
                            String message = "You are now connected with the Server.";
                            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                            int width = (int) screenSize.getWidth();
                            int height = (int) screenSize.getHeight();
                            objectShowList = new ShowListFrom(myPlayerInfo);
                            objectShowList.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
                            objectShowList.setLocation((width - objectShowList.getWidth()) / 2, (2 * height / 3 - objectShowList.getHeight()) / 2);
                            JOptionPane.showMessageDialog(rootPane, message);
                            HomePage.this.dispose();
                            objectShowList.setResizable(false);
                            objectShowList.setVisible(true);
                            isFrameOpen = true;
                        }

                        String token = input.readLine();
//                        if (!token.equals("Alist")) {
//                            System.out.println("token in run : " + token);
//                        }
                        if (token.equals("RequestToPlay")) {
                            token = input.readLine();
                            boolean acceptRequest = objectShowList.setRequestToPlay(token);
                            String t1 = input.readLine(), t2 = input.readLine();
                            pile1 = Integer.parseInt(t1);
                            pile2 = Integer.parseInt(t2);
                            if (acceptRequest) {
                                output.println("RequestAccepted");
                                output.println(t1);
                                output.println(t2);
                                objectShowList.playerID = 2;
                                isPlaying = true;
                                //System.out.println("before show submitgrid 1.");
                                showSubmitGrid(0); // opponent's move
                                //System.out.println("after show submitgrid 1.");
                            } else {
                                output.println("RequestDeclined");
                            }
                        } else if (token.equals("OpponentAccepted")) {
                            objectShowList.playerID = 1;
                            isPlaying = true;
                            System.out.println("well this is exactly before reading pile1 and pile2");
                            pile1 = Integer.parseInt(input.readLine());
                            pile2 = Integer.parseInt(input.readLine());
                            
                            //System.out.println("before show submitgrid 2.");
                            showSubmitGrid(1); // my move
                            //System.out.println("after show submitgrid 1.");
                        } else if (token.equals("OpponentDeclined")) {
                            JOptionPane.showMessageDialog(HomePage.this, "Sorry, " + opponent.playerName + " declines your request :(");
                        } else if (token.equals("Alist")) {
                            readAlist();
                        } else if(token.equals("PlayerNotFree")){
                            JOptionPane.showMessageDialog(HomePage.this, "Sorry, " + opponent.playerName + " is on a war :(");
                        }
                        else if (token.equals("OkExit")) {
                            playerIsConnected = false;
                            //System.out.println("**** " + playerIsConnected);
                        }
                        /**
                         * player on a game
                         */
                        else if(token.equals("OpMove")){
                            token=input.readLine();
                            int cellN = stringToInt(token);
                            //objectShowList.myIsHitMessage(cellN);
                        } else if(token.equals("HitShip") || token.equals("MissShip")){
                            /**
                             * call opIsHitMessage(true);
                             */
                            //objectShowList.opIsHitMessage(token.equals("HitShip"));
                        } else if(token.equals("Win")){
                            //objectShowList.winMessage();
                        }
                        else if(token.equals("OkGameOver")){
                            objectShowList.setIsPlaying(false);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(HomePage.class.getName()).log(Level.SEVERE, null, ex);
                        playerIsConnected = false;
                    }
                }
//                //System.out.println("outside while");
            } catch (InterruptedException ex) {
                Logger.getLogger(HomePage.class.getName()).log(Level.SEVERE, null, ex);
                //System.out.println("exception 1");
            } finally {
                try {
                    output.close();
                    input.close();
                    connection.close();
                    //System.out.println("here i'm");
                    //System.out.println(objectShowList.myTimer.isRunning());
                    //System.out.println(connection.isConnected());
                    System.exit(0);
                } catch (IOException ex) {
                    Logger.getLogger(HomePage.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        public void disposeThisThread() {
            if (!Thread.interrupted()) {
                this.playerThread.interrupt();
            }
            try {
                this.playerThread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(HomePage.class.getName()).log(Level.SEVERE, null, ex);
                //System.out.println(this.playerThread.getName() + " " + ex.toString());
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        ipField = new javax.swing.JTextField();
        resetButton = new javax.swing.JButton();
        logInButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 0, 0));

        jLabel1.setForeground(new java.awt.Color(153, 255, 255));
        jLabel1.setText("NAME");

        nameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameFieldActionPerformed(evt);
            }
        });

        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("SERVER IP");

        ipField.setText("192.168.0.102");
        ipField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ipFieldActionPerformed(evt);
            }
        });

        resetButton.setBackground(new java.awt.Color(255, 0, 0));
        resetButton.setForeground(new java.awt.Color(0, 102, 153));
        resetButton.setText("RESET");

        logInButton.setBackground(new java.awt.Color(255, 0, 0));
        logInButton.setForeground(new java.awt.Color(51, 102, 255));
        logInButton.setText("LOG IN");
        logInButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logInButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(54, 54, 54)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(logInButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(resetButton))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(nameField)
                            .addComponent(ipField, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ipField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resetButton)
                    .addComponent(logInButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    
    private void logInButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logInButtonActionPerformed
        // TODO add your handling code here::
        try{
            name = nameField.getText();
            ip = ipField.getText();
            if(validator.validate(ip) == false)
                throw new Exception("IP ADDRESS NOT VALID!");
            if(name.equals(""))
                throw new  Exception("NAME MUST BE AT LEAST ONE CHARACTER");
            Socket connection = new Socket(ip, serverPort);
            myPlayerInfo = new PlayerInfo(connection);
            System.out.println("connected!!!!");
           
        } catch(Exception e){
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }//GEN-LAST:event_logInButtonActionPerformed

    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        // TODO add your handling code here
    }//GEN-LAST:event_nameFieldActionPerformed

    private void ipFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ipFieldActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(HomePage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HomePage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HomePage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HomePage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new HomePage().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField ipField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton logInButton;
    private javax.swing.JTextField nameField;
    private javax.swing.JButton resetButton;
    // End of variables declaration//GEN-END:variables
}
