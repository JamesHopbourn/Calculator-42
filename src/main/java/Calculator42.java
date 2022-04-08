import java.io.*;
import java.awt.*;
import java.net.*;
import javax.swing.*;
import java.util.Date;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.awt.event.FocusEvent;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.awt.event.FocusListener;
import java.awt.event.ActionListener;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import com.alibaba.fastjson.JSONObject;

public class Calculator42 extends JFrame implements ActionListener {
    // 计算器偏好设置
    public static final String title = "Calculator 42";
    public static final String customFont = "Serif";
    public static final String dataFileName = "src/data.txt";
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
    Pattern sideOperator = Pattern.compile("[÷x−+=]|42");
    String[] button_text = {"bin", "dec", "hex", "42", "C", "±", "%", "÷", "7", "8", "9", "x",
            "4", "5", "6", "−", "1", "2", "3", "+", ".", "0", "DEL", "="};
    // 主要控件
    private final JPanel JP_north = new JPanel();
    private final JPanel JP_center = new JPanel();
    private final JTextField label = new JTextField();

    public Calculator42() throws HeadlessException {
        init();
        addNorthElements();
        addCenterElements();
    }

    public void init() {
        this.setTitle(title);
        this.setSize(FrameWidth, FrameHeight);
        this.setLayout(new BorderLayout());
        this.setLocation(FrameX, FrameY);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void addNorthElements() {
        label.setOpaque(true);
        label.setForeground(Color.white);
        label.setCaretColor(Color.decode("#373838"));
        label.setBackground(Color.decode("#373838"));
        label.setHorizontalAlignment(JTextField.RIGHT);
        label.setFont(new Font(customFont, Font.PLAIN, 40));
        label.setPreferredSize(new Dimension(300, 60));
        label.addActionListener(e -> {
            try {
                label.setText(currencyConvert(label.getText()));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            finish = true;
        });
        label.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                copyToClipboard(label);
                if (finish) label.setText("");
            }

            public void focusLost(FocusEvent e) {
                copyToClipboard(label);
                if (finish) label.setText("");
            }

            public static void copyToClipboard(JTextField label){
                StringSelection stringSelection = new StringSelection(label.getText());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
            }
        });
        JP_north.add(label);
        this.add(JP_north, BorderLayout.NORTH);
    }

    public void addCenterElements() {
        JP_center.setLayout(new GridLayout(6, 4));
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
        File file = new File(dataFileName);
        Files.deleteIfExists(file.toPath());
        // 初始化计算器
        Calculator42 calculator = new Calculator42();
        calculator.setVisible(true);
    }

    public static void saveData(double a, double b, String operator, double result) throws IOException{
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        FileWriter fileWriter = new FileWriter(dataFileName, true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.append(ft.format(date)).append("\t\t")
                    .append(String.valueOf(a)).append(" ")
                    .append(operator).append(" ")
                    .append(String.valueOf(b)).append(" = ")
                    .append(String.valueOf(result));
        bufferedWriter.newLine();
        bufferedWriter.close();
    }

    private String firstInput = null;
    private String operator = null;
    private boolean finish = false;

    @Override
    // TODO 正负取反
    // TODO 数字操作
    // TODO 逻辑运算
    // TODO 等式计算
    // TODO bin
    // TODO dec
    public void actionPerformed(ActionEvent e) {
        if (finish) {
            finish = false;
            label.setText("");
            label.setFont(new Font(customFont, Font.PLAIN, 40));
        }
        String actionCommand = e.getActionCommand();
        if (actionCommand.equals("42")){
            Object[] options1 = {"GitHub", "Quit"};
            ImageIcon imageIcon = new ImageIcon(((new ImageIcon("src/main/resources/icon.png"))
                    .getImage()).getScaledInstance(60, 60, Image.SCALE_SMOOTH));
            int result = JOptionPane.showOptionDialog(null, """
                    made by James Hopbourn
                    
                    软工 203 胡金栋
                    
                    2022/04/07""", "版权信息",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    imageIcon, options1, null);
            if (result == JOptionPane.YES_OPTION) {
                Desktop desk = Desktop.getDesktop();
                try {
                    desk.browse(new URI("https://github.com/JamesHopbourn/Calculator-42"));
                } catch (IOException | URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        } else if (actionCommand.equals("C")) {
            // AC 归零
            label.setText("");
            firstInput = null;
        } else if (actionCommand.equals("±")) {
            // 正负取反
            if (label.getText().startsWith("-")){
                label.setText(label.getText().replaceFirst("-", ""));
            } else {
                label.setText("-" + label.getText());
            }
            finish = true;
        } else if (actionCommand.equals("%")) {
            // 百分率
            double a = Double.parseDouble(label.getText()) / 100;
            if (a % 1 == 0.0) {
                label.setText(String.valueOf((int) a));
            } else {
                label.setText(String.valueOf(a));
            }
            finish = true;
        } else if (actionCommand.equals("DEL")) {
            // DEL
            label.setText(label.getText().substring(0, label.getText().length() - 1));
        } else if (".0123456789".contains(actionCommand)) {
            // 数字操作
            label.setText(label.getText() + actionCommand);
        } else if (actionCommand.matches("[÷x−+]")) {
            // 运算符号
            operator = actionCommand;
            firstInput = label.getText();
            label.setText("");
        } else if (actionCommand.equals("=")) {
            // 等式计算
            Double a = 0.0;
            if (firstInput.startsWith("-")) {
                a = (Double.parseDouble(firstInput.replace("-", ""))) * -1;
            } else {
                a = Double.valueOf(firstInput);
            }
            Double b = Double.valueOf(label.getText());
            Double result = null;
            switch (operator) {
                case "+" -> result = a + b;
                case "−" -> result = a - b;
                case "x" -> result = a * b;
                case "÷" -> {
                    if (b != 0) result = a / b;
                }
            }
            if (result == null) {
                label.setText("被除数不能为 0！");
            } else if (Double.toString(result).length() > 10) {
                label.setText(String.format("%.12f", result));
            } else if (result % 1 == 0.0) {
                label.setText(String.valueOf(result.intValue()));
            } else { // 格式化处理 防止显示不全
                label.setText(result.toString());
            }
            try {
                saveData(a, b, operator, result != null ? result : 0.00);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            finish = true;
            // 进制转换
        } else if (actionCommand.equals("bin")) {
            // 缩小字体 防止显示不全
            label.setFont(new Font(customFont, Font.PLAIN, 30));
            label.setText(bin(Integer.parseInt(label.getText())));
            finish = true;
        } else if (actionCommand.equals("dec")){
            String binNumber = label.getText().substring(1);
            label.setText(Integer.valueOf(binNumber, 2).toString());
            finish = true;
        } else if (actionCommand.equals("hex")){
            label.setText(Integer.toHexString(Integer.parseInt(label.getText())).toUpperCase());
            finish = true;
        }
    }

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

    public static String currencyConvert(String s) throws IOException {
        // CNYPattern: 10 to usd
        // OTH == other currency
        // OTHPattern: 10 usd to twd
        Pattern CNYPattern = Pattern.compile("(\\d+)\\s+to\\s+(\\w+)");
        Pattern OTHPattern = Pattern.compile("(\\d+)\\s+(\\w+)\\s+to\\s+(\\w+)");
        Pattern successPattern = Pattern.compile("\\d+(|\\.\\d+) \\w{3}");
        Matcher CNY = CNYPattern.matcher(s);
        Matcher OTH = OTHPattern.matcher(s);
        Matcher success = successPattern.matcher(s);
        if (CNY.find()) return getRateCNY(CNY);
        if (OTH.find()) return getRateOTH(OTH);
        if (success.find()) return "";
        return "转换失败";
    }

    // 人民币对其他币种
    public static String getRateCNY(Matcher matcher) throws IOException {
        JSONObject data = getData("cny");
        float rate = Float.parseFloat(data.getString(matcher.group(2).toUpperCase()));
        String text = String.format("%.2f", Integer.parseInt(matcher.group(1)) * rate);
        return text + " " + matcher.group(2).toUpperCase();
    }

    // 指定币种对指定币种
    public static String getRateOTH(Matcher matcher) throws IOException {
        JSONObject data = getData(matcher.group(2));
        float rate = Float.parseFloat(data.getString(matcher.group(3).toUpperCase()));
        String text = String.format("%.2f", Integer.parseInt(matcher.group(1)) * rate);
        return text + " " + matcher.group(3).toUpperCase();
    }

    public static JSONObject getData(String curr) throws IOException {
        // GET 请求汇率转换 API
        URL obj = new URL("https://v6.exchangerate-api.com/v6/" + APIkey + "/latest/" + curr);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        // 写入数据
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null)
            response.append(line);
        bufferedReader.close();
        // 返回 JSONObject 对象的 JSON 数据
        return JSONObject.parseObject(response.toString()).getJSONObject("conversion_rates");
    }
}
