/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Database;

import Login.Login;
import static com.sun.corba.se.spi.presentation.rmi.StubAdapter.request;
import com.sun.faces.context.RequestParameterMap;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Rikysamuel
 */
@ManagedBean(name="Posting", eager = true)
@RequestScoped
public class PostingDatabase {
    
    Login login;
    @ManagedProperty(value ="#{param.throwedid}")
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public PostingDatabase(){
        login = new Login();
    }
    
    public Connection makeConnection() throws ClassNotFoundException, SQLException{
        Connection con;
        Class.forName("com.mysql.jdbc.Driver");
        String url = "jdbc:mysql://localhost/blog";
        String user = "root";
        String password = "";
        con = DriverManager.getConnection(url,user,password);
        return con;
    }
    
    public List<Post> getPost() throws ClassNotFoundException{
        ResultSet rs;
        Connection con;
        List<Post> records = new ArrayList<>();
        try {
          con = makeConnection();
          Statement stmt = con.createStatement();
          String query = "Select * from post where status=\"Published\"";
          rs = stmt.executeQuery(query);

          while(rs.next()){
              Post post = new Post();
              post.setId(rs.getInt(1));
              post.setJudul(rs.getString(2));
              post.setTanggal(rs.getString(3));
              post.setContent(rs.getString(4));
              post.setAuthor(rs.getString(5));
              post.setStatus(rs.getString(6));
              records.add(post);
           }
          con.close();
        } catch (SQLException e) {
           System.err.println(e);
        }
        return records;
   }
    
    public List<Post> getAuthorPost() throws ClassNotFoundException{
        ResultSet rs;
        Connection con;
        List<Post> records = new ArrayList<>();
        try {
          con = makeConnection();
          Statement stmt = con.createStatement();
          String query = "Select * from post where (author=\""+login.getUserCookie().getValue()+"\" and status=\"unpublished\") or status=\"published\";";
          System.out.println("qeuery sekarang: " + query);
          rs = stmt.executeQuery(query);

          while(rs.next()){
              Post post = new Post();
              post.setId(rs.getInt(1));
              post.setJudul(rs.getString(2));
              post.setTanggal(rs.getString(3));
              post.setContent(rs.getString(4));
              post.setAuthor(rs.getString(5));
              post.setStatus(rs.getString(6));
              records.add(post);
           }
          con.close();
        } catch (SQLException e) {
           System.err.println(e);
        }
        return records;
   }
   
    public void addPost() throws ClassNotFoundException, SQLException, IOException, ParseException{
        HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String Judul = request.getParameter("Judul");
        String Tanggal = request.getParameter("Tanggal");
        String Konten = request.getParameter("Konten");
        ResultSet rs;
          Connection con = makeConnection();
          Statement stmt = con.createStatement();
          String query = "Select COUNT(Id) from post";
          SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
          java.util.Date parsed = format.parse(Tanggal);
          System.out.println(parsed);
          java.sql.Date datesql = new java.sql.Date(parsed.getTime());
          
          rs = stmt.executeQuery(query);
          PreparedStatement ps;
          int countsumId = 0;
          while(rs.next()){
             countsumId = rs.getInt(1);
           }
            String query2 = "INSERT INTO post (Judul, Tanggal, Content, Author, Status) VALUES (?,?,?,?,?)";
            ps= con.prepareStatement(query2);
            ps.setString(1,Judul);
            ps.setDate(2,datesql);
            ps.setString(3,Konten);
            ps.setString(4,login.getUserCookie().getValue());
            ps.setString(5,"unpublished");
            int i = ps.executeUpdate();
            ExternalContext extcon = FacesContext.getCurrentInstance().getExternalContext();
            extcon.redirect("Owner.xhtml");
    }

    public void deletePost() throws ClassNotFoundException, SQLException, IOException, ParseException{
          System.out.println(id+"PINGGGGGGGGG");
          Connection con = makeConnection();
          Statement stmt = con.createStatement();
          String query = "Update post Set status=\"deleted\" WHERE ID="+id;
          int rs;
          rs = stmt.executeUpdate(query);
          PreparedStatement ps;
            ExternalContext extcon = FacesContext.getCurrentInstance().getExternalContext();
            extcon.redirect("Owner.xhtml");
    }
    
    public String addUserOwner() throws ClassNotFoundException, SQLException{
        
        HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String Name = request.getParameter("Name");
        String Username = request.getParameter("Username");
        String Email = request.getParameter("Email");
        String Password = request.getParameter("Password");
        
        try (Connection con = makeConnection()) {
            PreparedStatement ps;
            String query = "INSERT INTO `user` (`Username`,`Password`, `Name`, `email`, `Role`) VALUES (?,?,?,?,?)";
            ps= con.prepareStatement(query);
            ps.setString(1,Username);
            ps.setString(2,Password);
            ps.setString(3,Name);
            ps.setString(4,Email);
            ps.setString(5,"Owner");
            int i = ps.executeUpdate();
        }
        return "Home.xhtml";
    }
    
    public void setLoginOnLoad() throws ClassNotFoundException, SQLException, IOException{
        ExternalContext extCont = FacesContext.getCurrentInstance().getExternalContext();
        Cookie cUsername = login.getUserCookie();
        Cookie cPassword = login.getPassCookie();
        System.out.println("teeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeesssssssssssssssssssssssssssssssssssss");
        if (cUsername!=null && cPassword!=null){
            ResultSet rs;
            Connection con;
            con = makeConnection();
            int existUser=0;
            Statement stmt = con.createStatement();
            String query = "Select COUNT(Username) from user where Username=\""+cUsername.getValue()+"\" and Password=\""+cPassword.getValue()+"\";";
            rs = stmt.executeQuery(query);

            while(rs.next()){
                existUser = rs.getInt(1);
                System.out.println("exist user: " + existUser);
            }
            if (existUser>0){
                extCont.redirect("/SImpleBlog/Role/Owner.xhtml");
            }
            else{
                extCont.redirect("/SImpleBlog/Home.xhtml");
            }
            con.close();
        }
    }
    
    public void Login() throws ClassNotFoundException, SQLException, IOException{
        ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String Username = request.getParameter("login_username");
        String Password = request.getParameter("login_password");

        ResultSet rs;
        int existUser;
        Connection con;
        con = makeConnection();
        Statement stmt = con.createStatement();
        String query = "Select COUNT(Username) from user where Username=\""+Username+"\" and Password=\""+Password+"\";";
        rs = stmt.executeQuery(query);

        while(rs.next()){
            existUser = rs.getInt(1);
            if(existUser>0){
                login.setCookie(Username, Password);
                extContext.redirect("/SImpleBlog/Role/Owner.xhtml");
            } else{
                extContext.redirect("/SImpleBlog/Home.xhtml");
            }
            System.out.println("exist user: ada " + existUser);
        }
        con.close();
    }
    
    public String getActiveUser() throws ClassNotFoundException, SQLException{
        ExternalContext extCont = FacesContext.getCurrentInstance().getExternalContext();
        Cookie cUsername = login.getUserCookie();
        System.out.println("value : " + cUsername.getValue());
        String activeUser = null;
        
        ResultSet rs;
        Connection con;
        con = makeConnection();
        Statement stmt = con.createStatement();
        String query = "Select Name from user where Username=\""+cUsername.getValue()+"\";";
        System.out.println("query : " + query);
        rs = stmt.executeQuery(query);
        System.out.println("execute");
        while(rs.next()){
            activeUser = rs.getString("Name");
         }
        System.out.println("active User: " + activeUser);
        return activeUser;
    }
    
    public void setLogout() throws ClassNotFoundException, SQLException, IOException{
        ExternalContext extCont = FacesContext.getCurrentInstance().getExternalContext();
        System.out.println("kepanggil");
        login.delUserCookie();
        login.delPassCookie();
        extCont.redirect("/SImpleBlog/");
    }
    
}
