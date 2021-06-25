package Client.UI;

import Client.Client;
import HTTP.HttpRequest;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//TODO：需求4.2：用户注册界面，GUI采用我们软工一学的那一套东西，可增加接口，具体见Login

import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class RegisterUI extends JFrame{
    RegisterUI () {
        init();
    }
    void init() {
        JFrame frame = new JFrame("注册管理员账号");
        frame.setLayout(null);

        JLabel nameStr = new JLabel("用户名:");
        nameStr.setBounds(50, 50, 100, 25);
        frame.add(nameStr);

        JLabel passwordStr = new JLabel("密码:");
        passwordStr.setBounds(50, 100, 100, 25);
        frame.add(passwordStr);

        JLabel confrimStr = new JLabel("确认密码:");
        confrimStr.setBounds(50, 150, 100, 30);
        frame.add(confrimStr);

        JTextField userName = new JTextField();
        userName.setBounds(120, 50, 150, 25);
        frame.add(userName);


        JPasswordField password = new JPasswordField();
        password.setBounds(120, 100, 150, 25);
        frame.add(password);

        JPasswordField confrimPassword = new JPasswordField();
        confrimPassword.setBounds(120, 150, 150, 25);
        frame.add(confrimPassword);

        JButton buttonregister = new JButton("注册");
        buttonregister.setBounds(150, 250, 70, 25);
        frame.add(buttonregister);



        frame.setBounds(400, 100, 500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        //为注册按钮增加监听器
        buttonregister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = userName.getText();
                String passwd = new String (password.getPassword());
                String confrimpasswd = new String (confrimPassword.getPassword());


                if(!passwd.equals(confrimpasswd))
                {
                    JOptionPane.showMessageDialog((Component)null, "密码不一致");
                    return;
                }
                frame.setVisible(false);
                //TODO: 发送http报文进行注册
                String startLine = "POST /register HTTP/1.1";
                Map<String,String> headers=new HashMap<>();
                String bodyStr="username="+name+"&password="+passwd+"&confrimpassword"+confrimpasswd;
                headers.put("Content-type","text/plain");
                headers.put("Content-length", String.valueOf(bodyStr.getBytes().length));
                HttpRequest httpRequest=new HttpRequest(startLine,headers,bodyStr.getBytes());
//                Client client=new Client(8080);
                Client.run(httpRequest);
                return;


            }

        });
    }

}

