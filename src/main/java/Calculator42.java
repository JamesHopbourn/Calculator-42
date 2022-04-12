import java.io.*;
import java.awt.*;
import java.net.*;
import javax.swing.*;
import java.util.Date;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.SimpleDateFormat;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import com.alibaba.fastjson.JSONObject;

public class Calculator42 extends JFrame implements ActionListener {
    // 计算器偏好设置
    public static final String title = "Calculator 42";
    public static final String customFont = "Serif";
    public static final String FileName = "src/data.txt";
    // 汇率转换 API key
    private static final String APIkey = System.getenv("APIKey");
    // 坐标定位
    public static final int FrameWidth = 300;
    public static final int FrameHeight = 400;
    public static final int ScreenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
    public static final int ScreenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
    public static final int FrameX = (ScreenWidth - FrameWidth) / 2;
    public static final int FrameY = (ScreenHeight - FrameHeight) / 2;
    // 正则匹配和字符集
    Pattern topOperator = Pattern.compile("[C±%]|[a-z]{3}");
    Pattern sideOperator = Pattern.compile("[÷x‒+=]|42");
    String[] button_text = {"bin", "dec", "hex", "42", "C", "±", "%", "÷", "7", "8", "9", "x",
            "4", "5", "6", "‒", "1", "2", "3", "+", ".", "0", "DEL", "="};
    // 主要控件
    private final JPanel JP_north = new JPanel();
    private final JPanel JP_center = new JPanel();
    private final JTextField label = new JTextField();
    // 构造方法
    public Calculator42() throws HeadlessException {
        init();
        addNorthElements();
        addCenterElements();
    }
    // 初始化界面
    public void init() {
        this.setTitle(title);
        this.setSize(FrameWidth, FrameHeight);
        this.setLayout(new BorderLayout());
        this.setLocation(FrameX, FrameY);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    // 北面元素
    public void addNorthElements() {
        label.setOpaque(true);
        label.setForeground(Color.white);
        label.setCaretColor(Color.decode("#373838"));
        label.setBackground(Color.decode("#373838"));
        label.setHorizontalAlignment(JTextField.RIGHT);
        label.setFont(new Font(customFont, Font.PLAIN, 40));
        label.setPreferredSize(new Dimension(300, 60));
        label.addActionListener(e -> {
            finish = true;
            try {
                label.setText(currencyConvert(label.getText()));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        label.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                copyToClipboard(label);
                label.setCaretColor(Color.white);
                if (finish) label.setText("");
            }

            public void focusLost(FocusEvent e) {
                copyToClipboard(label);
                label.setCaretColor(Color.decode("#373838"));
                if (finish) label.setText("");
            }
        });
        JP_north.add(label);
        this.add(JP_north, BorderLayout.NORTH);
    }
    // 中间元素
    public void addCenterElements() {
        JP_center.setLayout(new GridLayout(button_text.length / 4, 4));
        JP_center.setBackground(Color.decode("#6f6f6f"));
        for (String s : button_text) {
            JButton button = new JButton(s);
            if (topOperator.matcher(s).find()) {
                button.setBackground(Color.decode("#504e50"));
            } else if (sideOperator.matcher(s).find()) {
                button.setBackground(Color.decode("#ff9f0a"));
            } else {
                button.setBackground(Color.decode("#6f6f6f"));
            }
            button.setOpaque(true);
            button.setForeground(Color.white);
            button.setFont(new Font(customFont, Font.BOLD, 22));
            button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            button.addActionListener(this);
            JP_center.add(button);
        }
        this.add(JP_center, BorderLayout.CENTER);
    }

    public static void main(String[] args) throws IOException{
        // 初始化存储文件
        File file = new File(FileName);
        Files.deleteIfExists(file.toPath());
        // 初始化计算器
        Calculator42 calculator = new Calculator42();
        calculator.setVisible(true);
    }

    private String firstInput = null;
    private String operator = null;
    private boolean finish = false;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (finish) {
            finish = false;
            label.setText("");
            label.setFont(new Font(customFont, Font.PLAIN, 40));
        }
        String actionCommand = e.getActionCommand();
        if (actionCommand.equals("42")){
            // 版权信息
            copyrights();
        } else if (actionCommand.equals("C")) {
            // AC 归零
            label.setText("");
        } else if (actionCommand.equals("±")) {
            // 正负号
            if (label.getText().startsWith("-")){
                label.setText(label.getText().replaceFirst("-", ""));
            } else {
                label.setText("-" + label.getText());
            }
        } else if (actionCommand.equals("%")) {
            // 百分率
            finish = true;
            double a = Double.parseDouble(label.getText()) / 100;
            if (a % 1 == 0.0) {
                label.setText(String.valueOf((int) a));
            } else {
                label.setText(String.valueOf(a));
            }
        } else if (actionCommand.equals("DEL")) {
            // DEL
            label.setText(label.getText().substring(0, label.getText().length() - 1));
        } else if (".0123456789".contains(actionCommand)) {
            // 数字操作
            label.setText(label.getText() + actionCommand);
        } else if (actionCommand.matches("[÷x‒+]")) {
            // 运算符号
            operator = actionCommand;
            firstInput = label.getText();
            label.setText("");
        } else if (actionCommand.equals("=")) {
            // 等式计算
            finish = true;
            Double a = Double.parseDouble(firstInput);
            Double b = Double.parseDouble(label.getText());
            Double result = null;
            switch (operator) {
                case "+" -> result = a + b;
                case "‒" -> result = a - b;
                case "x" -> result = a * b;
                case "÷" -> {
                    if (b != 0) result = a / b;
                }
            }
            if (result == null) {
                label.setText("ERROR!");
            } else if (Double.toString(result).length() > 10) {
                label.setText(String.format("%.12f", result));
            } else if (result % 1 == 0.0) {
                label.setText(String.valueOf(result.intValue()));
            } else {
                label.setText(result.toString());
            }
            try {
                saveData(a, b, operator, result != null ? result : "NULL");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (actionCommand.equals("bin")) {
            // 十进制转为二进制
            finish = true;
            // 缩小字体 防止显示不全
            label.setFont(new Font(customFont, Font.PLAIN, 30));
            label.setText(bin(Integer.parseInt(label.getText())));
        } else if (actionCommand.equals("dec")){
            // 二进制、十六进制转为十进制
            finish = true;
            String number = label.getText().substring(2);
            if (label.getText().contains("0b")) {
                label.setText(Integer.valueOf(number, 2).toString());
            } else if (label.getText().contains("0x")){
                label.setText(String.valueOf(Integer.parseInt(number, 16)));
            }
        } else if (actionCommand.equals("hex")){
            // 转为十六进制
            finish = true;
            label.setText(Integer.toHexString(Integer.parseInt(label.getText())).toUpperCase());
        }
    }

    // 重写二进制转换 每隔四位空一格
    public static String bin(int num) {
        int index = 0;
        int[] binary = new int[40];
        while (num > 0) {
            binary[index++] = num % 2;
            num = num / 2;
        }
        StringBuilder result = new StringBuilder();
        for (int i = index - 1; i >= 0; i--) {
            result.append(binary[i]);
            if (i % 4 == 0) result.append(" ");
        }
        return result.toString();
    }

    // 汇率转换入口
    public static String currencyConvert(String s) throws IOException {
        // CNYPattern: 10 to usd
        // OTH == other currency
        // OTHPattern: 10 usd to twd
        Pattern CNYPattern = Pattern.compile("(\\d+)\\s+to\\s+(\\w+)");
        Pattern OTHPattern = Pattern.compile("(\\d+)\\s+(\\w+)\\s+to\\s+(\\w+)");
        Pattern resultPattern = Pattern.compile("(\\d+(|\\.\\d+) \\w{3}|转换失败)");
        Matcher CNY = CNYPattern.matcher(s);
        Matcher OTH = OTHPattern.matcher(s);
        Matcher result = resultPattern.matcher(s);
        if (CNY.find()) return getRateCNY(CNY);
        if (OTH.find()) return getRateOTH(OTH);
        if (result.find()) return "";
        return "转换失败";
    }

    // 人民币对其他币种
    public static String getRateCNY(Matcher matcher) throws IOException {
        JSONObject data = getData("CNY");
        float rate = Float.parseFloat(data.getString(matcher.group(2).toUpperCase()));
        String text = String.format("%.2f", Integer.parseInt(matcher.group(1)) * rate);
        text += " " + matcher.group(2).toUpperCase();
        copyToClipboard(text);
        return text;
    }

    // 指定币种对指定币种
    public static String getRateOTH(Matcher matcher) throws IOException {
        JSONObject data = getData(matcher.group(2));
        float rate = Float.parseFloat(data.getString(matcher.group(3).toUpperCase()));
        String text = String.format("%.2f", Integer.parseInt(matcher.group(1)) * rate);
        text += " " + matcher.group(3).toUpperCase();
        copyToClipboard(text);
        return text;
    }

    // 获取实时汇率数据
    public static JSONObject getData(String curr) throws IOException {
        // GET 请求汇率转换 API
        URL obj = new URL("https://v6.exchangerate-api.com/v6/" + APIkey + "/latest/" + curr);
        HttpURLConnection connect = (HttpURLConnection) obj.openConnection();
        connect.setRequestMethod("GET");
        // 写入数据
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connect.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null)
            response.append(line);
        bufferedReader.close();
        // 返回 JSONObject 对象的 JSON 数据
        return JSONObject.parseObject(response.toString()).getJSONObject("conversion_rates");
    }

    // 计算完成后将结果复制到剪切板
    public static <T> void copyToClipboard(T text){
        StringSelection stringSelection = null;
        if (text instanceof String){
            stringSelection = new StringSelection((String) text);
        } else if (text instanceof JLabel){
            stringSelection = new StringSelection(((JLabel) text).getText());
        }
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    // 版权信息按钮
    public static void copyrights(){
        Object[] objects = {"GitHub", "关闭"};
        ImageIcon imageIcon = new ImageIcon(((new ImageIcon("src/main/resources/icon.png"))
                .getImage()).getScaledInstance(60, 60, Image.SCALE_SMOOTH));
        int result = JOptionPane.showOptionDialog(null, """
                    made by James Hopbourn
                    
                    软工 203 胡金栋
                    
                    2022/04/07""", "版权信息",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,
                imageIcon, objects, null);
        if (result == JOptionPane.YES_OPTION) {
            try {
                Runtime.getRuntime().exec(new String[]{"/usr/bin/open", "-a",
                        "/Applications/Google Chrome.app",
                        "https://github.com/JamesHopbourn/Calculator-42"});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 保存计算结果
    public static <T> void saveData(T a, T b, String operator, T result) throws IOException{
        FileWriter fileWriter = new FileWriter(FileName, true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        bufferedWriter.append(ft.format(new Date())).append("\t\t")
                .append(String.valueOf(a)).append(" ")
                .append(operator).append(" ")
                .append(String.valueOf(b)).append(" = ")
                .append(String.valueOf(result));
        bufferedWriter.newLine();
        bufferedWriter.close();
    }
}
