package Client.UI;

import Client.Client;
import Client.ClientHandler;
import HTTP.HttpRequest;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//TODO：需求4.1：用户登陆界面，GUI采用我们软工一学的那一套东西
//其实就是利用我们客户端的那一套发送login或者register的报文，格式大概是post xxxxlogin username&password这种
//所以主要任务是完成GUI和报文格式封装发送（用client里已经实现好的接口）
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class LoginUI extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    private JLabel bq_North;
    private JLabel bq_name;
    private JLabel bq_pwd;
    private JButton login;
    private JButton sweep;
    private JButton regist;
    private JTextField text_name;
    private JPasswordField text_pwd;
    private JTabbedPane choose;
    private JCheckBox steal_login;
    private JCheckBox mark_name;
    private JPanel choose1;
    private JPanel login_South;

    public LoginUI() {
        ImageIcon logo = new ImageIcon("image/logo.png");
        logo.setImage(logo.getImage().getScaledInstance(300, 100, 1));
        this.bq_North = new JLabel(logo);
        this.login_South = new JPanel();
        this.login = new JButton();
        ImageIcon login_btn = new ImageIcon("src\\Client\\UI\\img.png");
        login_btn.setImage(login_btn.getImage().getScaledInstance(260, 25, 1));
        this.login.setIcon(login_btn);
        this.login.setBorderPainted(true);
        this.login.setBorder((Border)null);
        this.login.setCursor(Cursor.getPredefinedCursor(12));
        this.login_South.add(this.login);
        this.choose = new JTabbedPane();
        this.choose1 = new JPanel();
        this.choose.add("普通用户", this.choose1);
        this.choose1.setLayout(new GridLayout(3, 3));
        this.bq_name = new JLabel("账号", 0);
        this.bq_pwd = new JLabel("密码", 0);
        this.sweep = new JButton("清除号码");
        this.regist = new JButton("注册账号");
        this.text_name = new JTextField();
        this.text_pwd = new JPasswordField();
        this.regist = new JButton("注册账号");
        this.steal_login = new JCheckBox("系统不错");
        this.mark_name = new JCheckBox("给个好评");
        this.choose1.setLayout(new GridLayout(3, 3));
        this.choose1.add(this.bq_name);
        this.choose1.add(this.text_name);
        this.choose1.add(this.sweep);
        this.choose1.add(this.bq_pwd);
        this.choose1.add(this.text_pwd);
        this.choose1.add(this.regist);
        this.choose1.add(this.steal_login);
        this.choose1.add(this.mark_name);
        this.add(this.choose, "Center");
        this.add(this.bq_North, "North");
        this.add(this.login_South, "South");
        this.login.addActionListener(this);
        this.sweep.addActionListener(this);
        this.regist.addActionListener(this);
//        ImageIcon tubiao = new ImageIcon("img.png");
//        this.setIconImage(tubiao.getImage());
        this.setVisible(true);
        this.setBounds(340, 270, 500, 300);
        this.setResizable(false);
        this.setDefaultCloseOperation(3);
        this.setTitle("登录框");
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.login) {
            String username = this.text_name.getText().trim();
            String password = (new String(this.text_pwd.getPassword())).trim();
            if (!"".equals(username) && username != null) {
                if (!"".equals(password) && password != null) {
                    //TODO: 账号密码输入完毕，发送登录请求（登陆成功就不在GUI界面返回消息了，有点麻烦，就在客户端终端返回就好！

//                    this.setVisible(false);

                    String startLine = "POST /login HTTP/1.1";
                    Map<String,String> headers=new HashMap<>();
                    String bodyStr="username="+username+"&password="+password;
                    headers.put("Content-type","text/plain");
                    headers.put("Content-length", String.valueOf(bodyStr.getBytes().length));
//                    headers.put("Host:","www.XXX.com");
//                    headers.put("User-Agent:","Mozilla/5.0(Windows NT 6.1;rv:15.0) Firefox/15.0");


                    HttpRequest httpRequest=new HttpRequest(startLine,headers,bodyStr.getBytes());
//                    Client client=new Client(8080);
                    Client.run(httpRequest);
                    return;
//                    ClientHandler clientHandler=new ClientHandler(httpRequest.toByteArray(),null);

                }
                JOptionPane.showMessageDialog((Component)null, "密码不能为空！");
                return;
            }
            JOptionPane.showMessageDialog((Component)null, "账号不能为空！");
            return;
        }else if (e.getSource() == this.sweep) {
            this.text_name.setText((String)null);
            this.text_pwd.setText((String)null);
        } else if (e.getSource() == this.regist) {
//            this.setVisible(false);
            new RegisterUI();
            return;
        }

    }

//    public static void main(String[] args) {
//        new LoginUI();
//    }
}