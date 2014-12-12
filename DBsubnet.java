
package subnet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class DBsubnet {
    
    private Connection con;
    private Statement st;
    private Statement st1;
    private ResultSet rs;
    private ResultSet r1;
    private String query;
    private String query1;
    
    private File file = new File("/home/chiran/Desktop/mylog.log");
    private FileWriter writer;
    private BufferedWriter bwriter;
    
    private final String pw = "mysql";
    
    public DBsubnet() {
        
        while (true){
            
            try {
                
                Class.forName("com.mysql.jdbc.Driver");
                
                con = DriverManager.getConnection("jdbc:mysql://localhost:3306","root",pw);
                st = con.createStatement();
                
                st1 = con.createStatement();
                
                query = "CREATE DATABASE IF NOT EXISTS sshblock";     //Creating DB if doesn't exist      
                st.executeUpdate(query);

                query = "USE sshblock";
                st.executeUpdate(query);
                
                query = "CREATE TABLE IF NOT EXISTS subnets(\n" +
                        "    IPAddress VARCHAR (15) PRIMARY KEY,\n" +
                        "    IPrangeStart VARCHAR (15),\n" +
                        "    IPrangeEnd VARCHAR (15),\n" +
                        "    AddedDate DATE NOT NULL,\n" +
                        "    AddedTime TIME NOT NULL" +
                        ")";
                
                st.executeUpdate(query);
                
                query = "CREATE TABLE IF NOT EXISTS subnetsblock(\n" +
                        "    IPrangeStart VARCHAR (15) PRIMARY KEY,\n" +
                        "    IPrangeEnd VARCHAR (15),\n" +
                        "    blockedDate DATE NOT NULL,\n" +
                        "    blockedTime TIME NOT NULL,\n" +
                        "    releaseDate DATE NOT NULL,\n" +
                        "    releaseTime TIME NOT NULL\n" +
                        ")";
                
                st.executeUpdate(query);

                query = "SET global max_connections = 100000";            
                st.executeUpdate(query);
                
                break;
                
            }catch (Exception ex){
                
                System.out.println(ex);
                
            }
            
        }
        
    }
    
    private static java.sql.Date getCurrentJavaSqlDate() { //http://www.java2s.com/Code/JavaAPI/java.sql/PreparedStatementsetTimeintparameterIndexTimex.htm
        
        java.util.Date date = new java.util.Date();
        return new java.sql.Date(date.getTime());
        
    }

    private static java.sql.Time getCurrentJavaSqlTime() {   //http://www.java2s.com/Code/JavaAPI/java.sql/PreparedStatementsetTimeintparameterIndexTimex.htm
        
        java.util.Date date = new java.util.Date();
        return new java.sql.Time(date.getTime());
    
    }
    
    public void blocking(){
        
    //    while (true){
        
            try{

                query1 = "SELECT  * , COUNT(*) c "
                        + "FROM subnets "
                        + "GROUP BY IPrangeStart HAVING c > 4";

                rs = st1.executeQuery(query1);

                while (rs.next()){

                    String IPAddress = rs.getString("IPAddress");
                    String IPrangeStart = rs.getString("IPrangeStart");
                    String IPrangeEnd = rs.getString("IPrangeEnd");

                    System.out.println(IPAddress + " " + IPrangeStart + " " + " " + IPrangeEnd);
                    
                    //if(!containsRange(IPrangeStart)){
                        
                        query = "INSERT INTO subnetsblock VALUES (?, ?, ?, ?, ?, ?);";

                        PreparedStatement preparedStmt = con.prepareStatement(query);

                        preparedStmt.setString (1, IPrangeStart);
                        preparedStmt.setString (2, IPrangeEnd);



                        java.sql.Time time = getCurrentJavaSqlTime();       //System.out.println(time);
                        java.sql.Date date = getCurrentJavaSqlDate();       //System.out.println(date);

                        String timeSt = time.toString();
                        SimpleDateFormat df = new SimpleDateFormat ("HH:mm:ss"); 
                            //http://stackoverflow.com/questions/9015536/java-how-to-add-10-mins-in-my-time
                        java.util.Date d = df.parse(timeSt);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(d);
                        cal.add(Calendar.SECOND, 20);
                        String newTime = df.format(cal.getTime());
                        long ms = df.parse(newTime).getTime();
                        java.sql.Time time1 = new Time(ms);

                        java.sql.Date date1;

                        long ms1 = df.parse("00:00:00").getTime();
                        java.sql.Time timeA = new Time(ms1);

                        long ms2 = df.parse("00:20:00").getTime();
                        java.sql.Time timeB = new Time(ms2);

                        //If expiry time passes 12 midnight while added time doesn't pass 12 midight , 1 day should be added manually


                        if ((time1.after(timeA) && time1.before(timeB)) || time1.equals(timeA)){

                            String dateSt = date.toString();
                            SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
                            java.util.Date dDay = df1.parse(dateSt);
                            Calendar calDate = Calendar.getInstance();

                            calDate.add(Calendar.DATE, 1);
                            String newDate = df1.format(calDate.getTime());
                            long msDate = df1.parse(newDate).getTime();
                            date1 = new Date(msDate);

                        }else date1 = date;



                        preparedStmt.setDate (3,date);
                        preparedStmt.setTime (4,time);

                        preparedStmt.setDate (5,date1);
                        preparedStmt.setTime (6,time1);  

                        preparedStmt.executeUpdate();




                        //http://www.linuxquestions.org/questions/linux-networking-3/iptables-block-ip-subnets-277040/
                        String myStr = date + "\t" + time + "\tiptables -I INPUT -s " + IPrangeStart + "/" + IPrangeEnd + " -j DROP";

                        System.out.println(myStr);

                        if(!file.exists()){

                            file.createNewFile();

                        }

                        writer = new FileWriter(file.getAbsolutePath(),true);
                        bwriter = new BufferedWriter(writer);
                        bwriter.write(myStr+"\n");
                        bwriter.close();


                        /*query = "DELETE FROM subnets WHERE IPrangeStart = ?";

                        preparedStmt = con.prepareStatement(query);

                        preparedStmt.setString (1, IPrangeStart);

                        preparedStmt.executeUpdate();*/
                    
                    //}

                }

            }catch(Exception ex){

                System.out.println(ex);

            }
            
     //   }
        
    }
    
    public void unblocking (){
        
        while (true){
        
            //System.out.println("ss");
            
            try{
                    
                TimeUnit.MILLISECONDS.sleep(1);
                        
            }catch (Exception ex){
                
                System.out.println(ex);
                
            }
            
        
        }
        
    }
    
    private boolean containsRange (String str) {
        
        try{
        
            query = "SELECT * FROM subnetsblock WHERE IPrangeStart = \'" + str + "\'";

            rs = st.executeQuery(query);
            
            
            
            while (rs.next()){
            
                return true;
            
            }
            
            
            
        }catch (Exception ex){
            
            //System.out.println(ex);
            
        }
        
        return false;
        
    }
    
}
