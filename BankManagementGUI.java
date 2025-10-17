import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class BankManagementGUI extends JFrame {
    private static final String URL = "jdbc:mysql://localhost:3306/bankdb";
    private static final String USER = "root";
    private static final String PASSWORD = "your_password_here";
    private Connection con;

    private JTextField accNoField, nameField, amountField;
    private JTextArea outputArea;

    public BankManagementGUI() {
        setTitle("Bank Management System");
        setSize(500, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.add(new JLabel("Account No:"));
        accNoField = new JTextField();
        inputPanel.add(accNoField);

        inputPanel.add(new JLabel("Holder Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Amount:"));
        amountField = new JTextField();
        inputPanel.add(amountField);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        JButton createBtn = new JButton("Create");
        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton balanceBtn = new JButton("Check Balance");
        JButton displayBtn = new JButton("Display All");
        JButton exitBtn = new JButton("Exit");

        buttonPanel.add(createBtn);
        buttonPanel.add(depositBtn);
        buttonPanel.add(withdrawBtn);
        buttonPanel.add(balanceBtn);
        buttonPanel.add(displayBtn);
        buttonPanel.add(exitBtn);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setBorder(BorderFactory.createTitledBorder("Output"));

        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(new JScrollPane(outputArea), BorderLayout.SOUTH);

        createBtn.addActionListener(e -> createAccount());
        depositBtn.addActionListener(e -> depositMoney());
        withdrawBtn.addActionListener(e -> withdrawMoney());
        balanceBtn.addActionListener(e -> checkBalance());
        displayBtn.addActionListener(e -> displayAllAccounts());
        exitBtn.addActionListener(e -> System.exit(0));

        connectDB();
        setVisible(true);
    }

    private void connectDB() {
        try {
            con = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Connection Failed: " + e.getMessage());
        }
    }

    private void createAccount() {
        try {
            String accNo = accNoField.getText();
            String name = nameField.getText();
            double balance = Double.parseDouble(amountField.getText());

            String sql = "INSERT INTO accounts VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, accNo);
            ps.setString(2, name);
            ps.setDouble(3, balance);
            ps.executeUpdate();
            outputArea.setText("Account Created Successfully!");
        } catch (SQLException ex) {
            outputArea.setText("Error: " + ex.getMessage());
        }
    }

    private void depositMoney() {
        try {
            String accNo = accNoField.getText();
            double amt = Double.parseDouble(amountField.getText());
            String query = "UPDATE accounts SET balance = balance + ? WHERE account_no = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setDouble(1, amt);
            ps.setString(2, accNo);
            int rows = ps.executeUpdate();
            outputArea.setText(rows > 0 ? "Deposit Successful!" : "Account Not Found!");
        } catch (SQLException ex) {
            outputArea.setText("Error: " + ex.getMessage());
        }
    }

    private void withdrawMoney() {
        try {
            String accNo = accNoField.getText();
            double amt = Double.parseDouble(amountField.getText());
            String check = "SELECT balance FROM accounts WHERE account_no = ?";
            PreparedStatement psCheck = con.prepareStatement(check);
            psCheck.setString(1, accNo);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next()) {
                double balance = rs.getDouble("balance");
                if (amt > 0 && amt <= balance) {
                    String update = "UPDATE accounts SET balance = balance - ? WHERE account_no = ?";
                    PreparedStatement ps = con.prepareStatement(update);
                    ps.setDouble(1, amt);
                    ps.setString(2, accNo);
                    ps.executeUpdate();
                    outputArea.setText("Withdrawal Successful!");
                } else {
                    outputArea.setText("Insufficient Balance!");
                }
            } else {
                outputArea.setText("Account Not Found!");
            }
        } catch (SQLException ex) {
            outputArea.setText("Error: " + ex.getMessage());
        }
    }

    private void checkBalance() {
        try {
            String accNo = accNoField.getText();
            String query = "SELECT holder_name, balance FROM accounts WHERE account_no = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, accNo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                outputArea.setText("Account Holder: " + rs.getString("holder_name") +
                        "\nCurrent Balance: ₹" + rs.getDouble("balance"));
            } else {
                outputArea.setText("Account Not Found!");
            }
        } catch (SQLException ex) {
            outputArea.setText("Error: " + ex.getMessage());
        }
    }

    private void displayAllAccounts() {
        try {
            String query = "SELECT * FROM accounts";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);
            StringBuilder sb = new StringBuilder("All Accounts:\n");
            while (rs.next()) {
                sb.append("Account No: ").append(rs.getString("account_no"))
                        .append("\nHolder Name: ").append(rs.getString("holder_name"))
                        .append("\nBalance: ₹").append(rs.getDouble("balance"))
                        .append("\n---------------------------\n");
            }
            outputArea.setText(sb.toString());
        } catch (SQLException ex) {
            outputArea.setText("Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BankManagementGUI::new);
    }
}
// Note: Ensure you have the MySQL JDBC driver in your classpath and a database named 'bankdb' with a table 'accounts'.