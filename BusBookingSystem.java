import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class Bus {
    String busNumber, source, destination, busType;
    int price;
    String departureTime, arrivalTime;
    int totalSeats;
    List<Integer> availableSeats;

    public Bus(String busNumber, String source, String destination, String busType, int price, String departureTime, String arrivalTime, int totalSeats) {
        this.busNumber = busNumber;
        this.source = source;
        this.destination = destination;
        this.busType = busType;
        this.price = price;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.totalSeats = totalSeats;
        this.availableSeats = new ArrayList<>();
        for (int i = 1; i <= totalSeats; i++) {
            availableSeats.add(i);
        }
    }

    @Override
    public String toString() {
        return String.format("%s: %s to %s, Type: %s, Price: ₹%d, Departure: %s, Arrival: %s, Seats Available: %d/%d",
                busNumber, source, destination, busType, price, departureTime, arrivalTime, availableSeats.size(), totalSeats);
    }
}

class User {
    String username, password;
    List<Bus> bookedBuses = new ArrayList<>();

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void bookBus(Bus bus, int seatNumber) {
        bookedBuses.add(bus);
        bus.availableSeats.remove(Integer.valueOf(seatNumber));
    }
}

public class BusBookingSystem {
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Login and Signup components
    private JTextField usernameField, passwordField;

    // Bus viewing and booking components
    private JTextField sourceField, destinationField;
    private JList<Bus> busList;
    private DefaultListModel<Bus> busListModel;
    private JComboBox<Integer> seatSelection;
    private JTextArea bookingArea;
    private User loggedInUser;

    private List<Bus> buses = new ArrayList<>();

    private static final String DB_URL = "jdbc:mysql://localhost:3306/BusBooking";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "5260";

    public BusBookingSystem() {
        loadBuses();
        initializeUI();
        createUserTable();
        createBookingTable();
    }

    private void initializeUI() {
        frame = new JFrame("Bus Booking System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        frame.add(mainPanel);

        // Initialize all pages
        mainPanel.add(createSignUpPage(), "SignUp");
        mainPanel.add(createLoginPage(), "Login");
        mainPanel.add(createBusViewPage(), "BusView");
        mainPanel.add(createBookingPage(), "Booking");
        mainPanel.add(createViewBookingsPage(), "ViewBookings");

        frame.setVisible(true);
    }

    private JPanel createSignUpPage() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Sign Up", TitledBorder.CENTER, TitledBorder.TOP));
    
        JTextField signUpUsernameField = new JTextField(15); // Separate field
        JPasswordField signUpPasswordField = new JPasswordField(15); // Separate field
        JButton signUpButton = createButton("Sign Up");
    
        signUpButton.addActionListener(e -> signUp(signUpUsernameField.getText(), signUpPasswordField.getText()));
    
        addComponentToPanel(panel, new JLabel("Username:"), 0, 0, 1);
        addComponentToPanel(panel, signUpUsernameField, 1, 0, 2);
        addComponentToPanel(panel, new JLabel("Password:"), 0, 1, 1);
        addComponentToPanel(panel, signUpPasswordField, 1, 1, 2);
        addComponentToPanel(panel, signUpButton, 0, 2, 2);
    
        JButton switchToLogin = createButton("Go to Login");
        switchToLogin.addActionListener(e -> cardLayout.show(mainPanel, "Login"));
        addComponentToPanel(panel, switchToLogin, 0, 3, 2);
    
        return panel;
    }
    

    private JPanel createLoginPage() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Login", TitledBorder.CENTER, TitledBorder.TOP));
    
        JTextField loginUsernameField = new JTextField(15); // Separate field
        JPasswordField loginPasswordField = new JPasswordField(15); // Separate field
        JButton logInButton = createButton("Log In");
    
        logInButton.addActionListener(e -> logIn(loginUsernameField.getText(), loginPasswordField.getText()));
    
        addComponentToPanel(panel, new JLabel("Username:"), 0, 0, 1);
        addComponentToPanel(panel, loginUsernameField, 1, 0, 2);
        addComponentToPanel(panel, new JLabel("Password:"), 0, 1, 1);
        addComponentToPanel(panel, loginPasswordField, 1, 1, 2);
        addComponentToPanel(panel, logInButton, 0, 2, 2);
    
        JButton switchToSignUp = createButton("Go to Sign Up");
        switchToSignUp.addActionListener(e -> cardLayout.show(mainPanel, "SignUp"));
        addComponentToPanel(panel, switchToSignUp, 0, 3, 2);
    
        return panel;
    }
    

    private JPanel createBusViewPage() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "View Buses", TitledBorder.CENTER, TitledBorder.TOP));

        sourceField = new JTextField(15);
        destinationField = new JTextField(15);
        JButton viewBusesButton = createButton("View Buses");

        viewBusesButton.addActionListener(e -> viewBuses());

        busListModel = new DefaultListModel<>();
        busList = new JList<>(busListModel);
        busList.setBorder(BorderFactory.createTitledBorder("Available Buses"));
        busList.addListSelectionListener(e -> updateSeatSelection());

        seatSelection = new JComboBox<>();
        JButton proceedToBooking = createButton("Proceed to Booking");
        proceedToBooking.addActionListener(e -> cardLayout.show(mainPanel, "Booking"));

        addComponentToPanel(panel, new JLabel("Source:"), 0, 0, 1);
        addComponentToPanel(panel, sourceField, 1, 0, 2);
        addComponentToPanel(panel, new JLabel("Destination:"), 0, 1, 1);
        addComponentToPanel(panel, destinationField, 1, 1, 2);
        addComponentToPanel(panel, viewBusesButton, 0, 2, 2);
        addComponentToPanel(panel, new JScrollPane(busList), 0, 3, 2);
        addComponentToPanel(panel, new JLabel("Select Seat:"), 0, 4, 1);
        addComponentToPanel(panel, seatSelection, 1, 4, 2);
        addComponentToPanel(panel, proceedToBooking, 0, 5, 2);

        return panel;
    }

    private JPanel createBookingPage() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Book Bus", TitledBorder.CENTER, TitledBorder.TOP));

        bookingArea = new JTextArea(10, 30);
        bookingArea.setEditable(false);
        bookingArea.setBorder(BorderFactory.createTitledBorder("Booking Details"));

        JButton bookBusButton = createButton("Book Bus");
        bookBusButton.addActionListener(e -> bookBus());

        JButton backButton = createButton("Back to View Buses");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "BusView"));

        addComponentToPanel(panel, new JScrollPane(bookingArea), 0, 0, 2);
        addComponentToPanel(panel, bookBusButton, 0, 1, 1);
        addComponentToPanel(panel, backButton, 1, 1, 1);

        return panel;
    }

    private JPanel createViewBookingsPage() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "View Bookings", TitledBorder.CENTER, TitledBorder.TOP));

        JTextArea bookingDetailsArea = new JTextArea(10, 30);
        bookingDetailsArea.setEditable(false);
        bookingDetailsArea.setBorder(BorderFactory.createTitledBorder("Your Bookings"));

        JButton viewBookingsButton = createButton("Show My Bookings");
        viewBookingsButton.addActionListener(e -> viewBookings(bookingDetailsArea));

        JButton summaryButton = createButton("Show Ticket Summary");
        summaryButton.addActionListener(e -> showTicketSummary(bookingDetailsArea));

        JButton backButton = createButton("Back to Bus View");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "BusView"));

        addComponentToPanel(panel, new JScrollPane(bookingDetailsArea), 0, 0, 2);
        addComponentToPanel(panel, viewBookingsButton, 0, 1, 1);
        addComponentToPanel(panel, summaryButton, 1, 1, 1);
        addComponentToPanel(panel, backButton, 0, 2, 2);

        return panel;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        return button;
    }

    private void addComponentToPanel(JPanel panel, Component component, int gridx, int gridy, int gridwidth) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = gridwidth;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(component, gbc);
    }

    private void loadBuses() {
        buses.add(new Bus("TN001", "Chennai", "Coimbatore", "AC", 500, "06:00", "10:00", 30));
        buses.add(new Bus("TN002", "Chennai", "Madurai", "Non-AC", 400, "07:00", "11:00", 20));
        buses.add(new Bus("TN003", "Coimbatore", "Madurai", "AC", 600, "08:00", "12:00", 25));
        buses.add(new Bus("TN004", "Chennai", "Tiruchirappalli", "AC", 700, "09:00", "14:00", 40));
        buses.add(new Bus("TN005", "Madurai", "Tirunelveli", "Non-AC", 350, "10:00", "12:30", 30));
        buses.add(new Bus("TN006", "Tiruchirappalli", "Coimbatore", "AC", 550, "11:00", "15:00", 35));
        buses.add(new Bus("TN007", "Chennai", "Kanyakumari", "AC", 800, "12:00", "20:00", 30));
        buses.add(new Bus("TN008", "Madurai", "Dindigul", "Non-AC", 200, "13:00", "14:00", 25));
        buses.add(new Bus("TN009", "Coimbatore", "Erode", "AC", 250, "14:00", "15:30", 40));
        buses.add(new Bus("TN010", "Tirunelveli", "Nagercoil", "AC", 150, "15:00", "16:30", 30));
        buses.add(new Bus("TN011", "Chennai", "Salem", "Non-AC", 450, "16:00", "19:00", 20));
        buses.add(new Bus("TN012", "Tiruchirappalli", "Kodaikanal", "AC", 900, "17:00", "22:00", 30));
        buses.add(new Bus("TN013", "Madurai", "Rameswaram", "Non-AC", 300, "18:00", "20:00", 20));
        buses.add(new Bus("TN014", "Coimbatore", "Pollachi", "AC", 150, "19:00", "20:00", 35));
        buses.add(new Bus("TN015", "Chennai", "Cuddalore", "AC", 400, "20:00", "23:00", 30));
        buses.add(new Bus("TN016", "Tiruchirappalli", "Thanjavur", "Non-AC", 200, "07:30", "09:00", 30));
        buses.add(new Bus("TN017", "Kanyakumari", "Nagercoil", "AC", 100, "08:30", "09:30", 20));
        buses.add(new Bus("TN018", "Madurai", "Kodaikanal", "Non-AC", 350, "09:30", "12:00", 25));
        buses.add(new Bus("TN019", "Coimbatore", "Munnar", "AC", 800, "10:30", "14:00", 30));
        buses.add(new Bus("TN020", "Chennai", "Vellore", "Non-AC", 300, "11:30", "14:00", 20));
        buses.add(new Bus("TN021", "Salem", "Namakkal", "AC", 250, "12:00", "13:30", 30));
        buses.add(new Bus("TN022", "Chennai", "Tiruvannamalai", "AC", 600, "13:30", "17:00", 35));
        buses.add(new Bus("TN023", "Madurai", "Virudhunagar", "Non-AC", 150, "14:30", "15:30", 20));
        buses.add(new Bus("TN024", "Coimbatore", "Dindigul", "AC", 400, "15:00", "16:30", 30));
        buses.add(new Bus("TN025", "Tiruchirappalli", "Karur", "Non-AC", 180, "16:00", "17:00", 20));
        buses.add(new Bus("TN026", "Kanyakumari", "Thiruvananthapuram", "AC", 250, "17:30", "19:00", 30));
        buses.add(new Bus("TN027", "Chennai", "Vellore", "AC", 350, "18:30", "21:00", 25));
        buses.add(new Bus("TN028", "Erode", "Karur", "Non-AC", 180, "19:00", "20:00", 30));
        buses.add(new Bus("TN029", "Tirupur", "Coimbatore", "AC", 200, "20:00", "21:30", 40));
        buses.add(new Bus("TN030", "Namakkal", "Salem", "AC", 250, "21:00", "22:00", 20));
        buses.add(new Bus("TN031", "Kanyakumari", "Madurai", "AC", 450, "22:30", "05:00", 30));
        buses.add(new Bus("TN032", "Pudukkottai", "Thanjavur", "Non-AC", 150, "06:00", "07:00", 25));
        buses.add(new Bus("TN033", "Nagercoil", "Kanyakumari", "AC", 100, "07:00", "08:00", 20));
        buses.add(new Bus("TN034", "Tirunelveli", "Kanyakumari", "Non-AC", 250, "08:00", "10:00", 35));
        buses.add(new Bus("TN035", "Chennai", "Tiruvannamalai", "AC", 600, "09:00", "12:00", 30));
        buses.add(new Bus("TN036", "Dindigul", "Madurai", "Non-AC", 150, "10:00", "11:00", 20));
        buses.add(new Bus("TN037", "Coimbatore", "Kodaikanal", "AC", 500, "11:00", "15:00", 30));
        buses.add(new Bus("TN038", "Karur", "Namakkal", "Non-AC", 200, "12:00", "13:00", 30));
        buses.add(new Bus("TN039", "Tirupur", "Erode", "AC", 150, "13:30", "15:00", 40));
        buses.add(new Bus("TN040", "Madurai", "Rameswaram", "Non-AC", 300, "15:00", "17:00", 30));
        buses.add(new Bus("TN041", "Chennai", "Sivagangai", "AC", 500, "06:00", "10:00", 30));
        buses.add(new Bus("TN042", "Coimbatore", "Nellai", "Non-AC", 600, "07:00", "12:00", 20));
        buses.add(new Bus("TN043", "Tiruchirappalli", "Ramanathapuram", "AC", 400, "08:00", "11:00", 25));
        buses.add(new Bus("TN044", "Madurai", "Pudukkottai", "Non-AC", 300, "09:00", "11:30", 30));
        buses.add(new Bus("TN045", "Kanyakumari", "Kovalam", "AC", 200, "10:00", "11:00", 30));
        buses.add(new Bus("TN046", "Chennai", "Vellore", "Non-AC", 450, "11:00", "13:30", 20));
        buses.add(new Bus("TN047", "Coimbatore", "Ooty", "AC", 700, "12:00", "15:00", 30));
        buses.add(new Bus("TN048", "Tirunelveli", "Tuticorin", "Non-AC", 150, "13:00", "14:00", 20));
        buses.add(new Bus("TN049", "Tiruchirappalli", "Perambalur", "AC", 250, "14:00", "15:30", 35));
        buses.add(new Bus("TN050", "Madurai", "Tirupparankundram", "Non-AC", 100, "15:00", "15:30", 40));
        buses.add(new Bus("TN051", "Kanyakumari", "Nagercoil", "AC", 100, "16:00", "17:00", 20));
        buses.add(new Bus("TN052", "Chennai", "Rameswaram", "Non-AC", 600, "17:00", "20:00", 25));
        buses.add(new Bus("TN053", "Coimbatore", "Panchayat", "AC", 350, "18:00", "20:00", 30));
        buses.add(new Bus("TN054", "Tirupur", "Palani", "Non-AC", 200, "19:00", "20:30", 40));
        buses.add(new Bus("TN055", "Madurai", "Sivaganga", "AC", 300, "20:00", "21:00", 20));
        buses.add(new Bus("TN056", "Dindigul", "Munnar", "Non-AC", 250, "21:00", "23:30", 30));
        buses.add(new Bus("TN057", "Tiruchirappalli", "Karur", "AC", 150, "22:00", "23:00", 20));
        buses.add(new Bus("TN058", "Chennai", "Vellore", "AC", 350, "06:30", "09:00", 25));
        buses.add(new Bus("TN059", "Coimbatore", "Kodaikanal", "Non-AC", 500, "07:00", "11:00", 20));
        buses.add(new Bus("TN060", "Kanyakumari", "Rameswaram", "AC", 250, "08:00", "10:00", 30));
        buses.add(new Bus("TN061", "Madurai", "Nellai", "Non-AC", 150, "09:00", "10:00", 25));
        buses.add(new Bus("TN062", "Tirunelveli", "Tuticorin", "AC", 100, "10:00", "11:00", 20));
        buses.add(new Bus("TN063", "Tiruchirappalli", "Pudukkottai", "Non-AC", 300, "11:00", "12:30", 30));
        buses.add(new Bus("TN064", "Coimbatore", "Namakkal", "AC", 400, "12:00", "13:30", 20));
        buses.add(new Bus("TN065", "Dindigul", "Madurai", "Non-AC", 150, "13:00", "14:00", 25));
        buses.add(new Bus("TN066", "Tirupur", "Erode", "AC", 200, "14:00", "15:00", 30));
        buses.add(new Bus("TN067", "Madurai", "Virudhunagar", "Non-AC", 300, "15:00", "16:00", 20));
        buses.add(new Bus("TN068", "Chennai", "Salem", "AC", 500, "16:00", "18:00", 35));
        buses.add(new Bus("TN069", "Coimbatore", "Tirupur", "Non-AC", 150, "17:00", "18:00", 30));
        buses.add(new Bus("TN070", "Tiruchirappalli", "Tirunelveli", "AC", 400, "18:00", "20:00", 20));
        buses.add(new Bus("TN071", "Kanyakumari", "Nagercoil", "Non-AC", 100, "19:00", "20:00", 25));
        buses.add(new Bus("TN072", "Madurai", "Kodaikanal", "AC", 600, "20:00", "22:30", 30));
        buses.add(new Bus("TN073", "Chennai", "Vellore", "Non-AC", 350, "21:00", "23:00", 20));
        buses.add(new Bus("TN074", "Coimbatore", "Ooty", "AC", 700, "22:00", "23:30", 30));
        buses.add(new Bus("TN075", "Tirunelveli", "Kanyakumari", "Non-AC", 200, "06:00", "07:30", 25));
        buses.add(new Bus("TN076", "Tiruchirappalli", "Dindigul", "AC", 150, "07:00", "08:30", 30));
        buses.add(new Bus("TN077", "Madurai", "Nagercoil", "Non-AC", 400, "08:00", "10:00", 20));
        buses.add(new Bus("TN078", "Chennai", "Kanyakumari", "AC", 800, "09:00", "13:00", 30));
        buses.add(new Bus("TN079", "Coimbatore", "Karur", "Non-AC", 180, "10:00", "11:00", 25));
        buses.add(new Bus("TN080", "Dindigul", "Theni", "AC", 300, "11:00", "12:30", 20));
        buses.add(new Bus("TN081", "Tirupur", "Sivakasi", "Non-AC", 200, "12:00", "13:00", 40));
        buses.add(new Bus("TN082", "Madurai", "Karur", "AC", 250, "13:00", "14:30", 30));
        buses.add(new Bus("TN083", "Kanyakumari", "Tuticorin", "Non-AC", 200, "14:00", "15:30", 25));
        buses.add(new Bus("TN084", "Chennai", "Pudukkottai", "AC", 300, "15:00", "17:00", 20));
        buses.add(new Bus("TN085", "Coimbatore", "Tiruppur", "Non-AC", 150, "16:00", "17:00", 30));
        buses.add(new Bus("TN086", "Tiruchirappalli", "Virudhunagar", "AC", 200, "17:00", "18:00", 30));
        buses.add(new Bus("TN087", "Madurai", "Tirupur", "Non-AC", 400, "18:00", "19:00", 25));
        buses.add(new Bus("TN088", "Dindigul", "Tirunelveli", "AC", 250, "19:00", "20:30", 20));
        buses.add(new Bus("TN089", "Tirupur", "Kanyakumari", "Non-AC", 200, "20:00", "21:30", 40));
        buses.add(new Bus("TN090", "Chennai", "Tiruvannamalai", "AC", 600, "21:00", "23:00", 30));
        buses.add(new Bus("TN091", "Coimbatore", "Tirunelveli", "Non-AC", 250, "06:00", "08:00", 30));
        buses.add(new Bus("TN092", "Madurai", "Thanjavur", "AC", 200, "07:30", "09:00", 20));
        buses.add(new Bus("TN093", "Chennai", "Rameswaram", "Non-AC", 600, "08:00", "12:00", 30));
        buses.add(new Bus("TN094", "Tirunelveli", "Kanyakumari", "AC", 300, "09:00", "10:30", 25));
        buses.add(new Bus("TN095", "Coimbatore", "Karur", "Non-AC", 180, "10:00", "11:00", 40));
        buses.add(new Bus("TN096", "Madurai", "Sivaganga", "AC", 400, "11:30", "12:30", 30));
        buses.add(new Bus("TN097", "Tiruchirappalli", "Kodaikanal", "Non-AC", 550, "12:00", "16:00", 25));
        buses.add(new Bus("TN098", "Chennai", "Sivakasi", "AC", 500, "13:00", "15:30", 30));
        buses.add(new Bus("TN099", "Coimbatore", "Ooty", "Non-AC", 700, "14:30", "16:00", 30));
        buses.add(new Bus("TN100", "Tirupur", "Virudhunagar", "AC", 200, "15:00", "16:00", 40));
    }

    private void createUserTable() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Statement stmt = connection.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "username VARCHAR(255) PRIMARY KEY," +
                    "password VARCHAR(255) NOT NULL" +
                    ");");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createBookingTable() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Statement stmt = connection.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS bookings (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(255)," +
                    "bus_number VARCHAR(255)," +
                    "seat_number INT," +
                    "FOREIGN KEY (username) REFERENCES users(username)" +
                    ");");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void signUp(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter both username and password");
            return;
        }
    
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(frame, "Sign Up successful");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
        }
    }
    

    private void logIn(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter both username and password");
            return;
        }
    
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?");
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
    
            if (rs.next()) {
                loggedInUser = new User(username, password);
                JOptionPane.showMessageDialog(frame, "Log In successful");
                cardLayout.show(mainPanel, "BusView"); // Switch to bus view after login
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid username or password");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
        }
    }
    

    private void viewBuses() {
        busListModel.clear();
        for (Bus bus : buses) {
            busListModel.addElement(bus);
        }
    }

    private void bookBus() {
        Bus selectedBus = busList.getSelectedValue();
        Integer selectedSeat = (Integer) seatSelection.getSelectedItem();
        if (selectedBus == null || selectedSeat == null) {
            JOptionPane.showMessageDialog(frame, "Please select a bus and a seat");
            return;
        }

        if (loggedInUser == null) {
            JOptionPane.showMessageDialog(frame, "Please log in to book a bus");
            return;
        }

        loggedInUser.bookBus(selectedBus, selectedSeat);
        bookingArea.append(String.format("Booked %s on %s for seat %d\n", loggedInUser.username, selectedBus.busNumber, selectedSeat));

        // Log booking to database
        logBooking(selectedBus, selectedSeat);

        viewBuses(); // Refresh the bus list
        updateSeatSelection();
    }

    private void logBooking(Bus bus, int seatNumber) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO bookings (username, bus_number, seat_number) VALUES (?, ?, ?)");
            stmt.setString(1, loggedInUser.username);
            stmt.setString(2, bus.busNumber);
            stmt.setInt(3, seatNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void viewBookings(JTextArea bookingDetailsArea) {
        bookingDetailsArea.setText(""); // Clear previous bookings
        if (loggedInUser == null) {
            bookingDetailsArea.setText("Please log in to view bookings.");
            return;
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM bookings WHERE username = ?");
            stmt.setString(1, loggedInUser.username);
            ResultSet rs = stmt.executeQuery();

            StringBuilder bookings = new StringBuilder();
            while (rs.next()) {
                String busNumber = rs.getString("bus_number");
                int seatNumber = rs.getInt("seat_number");
                bookings.append(String.format("Bus %s, Seat %d\n", busNumber, seatNumber));
            }
            if (bookings.length() == 0) {
                bookings.append("No bookings found.");
            }
            bookingDetailsArea.setText(bookings.toString());
        } catch (SQLException e) {
            bookingDetailsArea.setText("Error retrieving bookings: " + e.getMessage());
        }
    }

    private void showTicketSummary(JTextArea bookingDetailsArea) {
        if (loggedInUser == null) {
            bookingDetailsArea.setText("Please log in to view ticket summary.");
            return;
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM bookings WHERE username = ?");
            stmt.setString(1, loggedInUser.username);
            ResultSet rs = stmt.executeQuery();

            StringBuilder summary = new StringBuilder();
            summary.append("Ticket Summary for ").append(loggedInUser.username).append(":\n");
            while (rs.next()) {
                String busNumber = rs.getString("bus_number");
                int seatNumber = rs.getInt("seat_number");
                summary.append(String.format("Bus: %s, Seat: %d\n", busNumber, seatNumber));
            }
            if (summary.length() == 0) {
                summary.append("No tickets booked.");
            }
            bookingDetailsArea.setText(summary.toString());
        } catch (SQLException e) {
            bookingDetailsArea.setText("Error retrieving ticket summary: " + e.getMessage());
        }
    }

    private void updateSeatSelection() {
        Bus selectedBus = busList.getSelectedValue();
        seatSelection.removeAllItems();
        if (selectedBus != null) {
            for (Integer seat : selectedBus.availableSeats) {
                seatSelection.addItem(seat);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BusBookingSystem::new);
    }
}