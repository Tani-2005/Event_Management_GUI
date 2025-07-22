package sys;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime; // For Session start_time, end_time
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.text.DecimalFormat; // For formatting price/amount

@SuppressWarnings("serial")
public class EventManagementApp extends JFrame {

    // --- Database Configuration (UPDATE THESE) ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/evntmgmtsys";
    private static final String USER = "root"; // e.g., "root"
    private static final String PASS = "Tani2005$$$"; // e.g., "password"
    // ---------------------------------------------

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private Stack<String> panelHistory; // To manage 'Back' button
    private String currentPanelName;

    // Top navigation buttons
    private JButton backButton;
    private JButton homeButton;
    private JToggleButton sideMenuButton; // Changed to JToggleButton for state
    private JPanel topBarPanel;
    private JPanel sideMenuPanel; // Panel for side menu options

    public EventManagementApp() {
        setTitle("Event Management System");
        setSize(1200, 800); // Increased size for more content
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame

        panelHistory = new Stack<>();
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        add(mainPanel, BorderLayout.CENTER);

        // --- Top Bar Panel ---
        topBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topBarPanel.setBackground(new Color(60, 63, 65)); // Dark background

        sideMenuButton = new JToggleButton("☰ Menu");
        homeButton = new JButton("🏠 Home");
        backButton = new JButton("← Back");

        // Styling buttons
        sideMenuButton.setForeground(Color.WHITE);
        homeButton.setForeground(Color.WHITE);
        backButton.setForeground(Color.WHITE);

        sideMenuButton.setBackground(new Color(75, 110, 175)); // A pleasant blue
        homeButton.setBackground(new Color(75, 110, 175));
        backButton.setBackground(new Color(75, 110, 175));

        sideMenuButton.setFocusPainted(false);
        homeButton.setFocusPainted(false);
        backButton.setFocusPainted(false);

        topBarPanel.add(sideMenuButton);
        topBarPanel.add(homeButton);
        topBarPanel.add(backButton);
        add(topBarPanel, BorderLayout.NORTH);

        // --- Side Menu Panel ---
        sideMenuPanel = new JPanel();
        sideMenuPanel.setLayout(new BoxLayout(sideMenuPanel, BoxLayout.Y_AXIS));
        sideMenuPanel.setBackground(new Color(80, 83, 85)); // Slightly lighter than top bar
        sideMenuPanel.setPreferredSize(new Dimension(220, getHeight())); // Fixed width for side menu
        sideMenuPanel.setVisible(false); // Initially hidden
        add(sideMenuPanel, BorderLayout.WEST);

        // --- Populate Side Menu with Dashboard Buttons ---
        addSideMenuButton("Add New Event", "add_events_panel");
        addSideMenuButton("Register New User", "register_attendee_panel");
        addSideMenuButton("View All Events", "view_events_panel");
        addSideMenuButton("Apply for an Event", "apply_for_event_panel");
        addSideMenuButton("Process Event Payment", "pay_for_event_panel");
        addSideMenuButton("View Your Booked Events", "view_booked_events_panel");
        addSideMenuButton("Manage Speakers", "manage_speakers_panel"); // New
        addSideMenuButton("Manage Sessions", "manage_sessions_panel"); // New
        addSideMenuButton("Manage Staff", "manage_staff_panel");     // New


        // --- Action Listeners for Top Bar Buttons ---
        homeButton.addActionListener(e -> navigateTo("dashboard_panel"));
        backButton.addActionListener(e -> navigateBack());
        sideMenuButton.addActionListener(e -> sideMenuPanel.setVisible(sideMenuButton.isSelected()));

        // --- Add Panels to CardLayout ---
        mainPanel.add(new DashboardPanel(), "dashboard_panel");
        mainPanel.add(new AddEventPanel(), "add_events_panel");
        mainPanel.add(new RegisterAttendeePanel(), "register_attendee_panel");
        mainPanel.add(new ViewEventsPanel(), "view_events_panel");
        mainPanel.add(new ApplyForEventPanel(), "apply_for_event_panel");
        mainPanel.add(new PayForEventPanel(), "pay_for_event_panel");
        mainPanel.add(new ViewBookedEventsPanel(), "view_booked_events_panel");
        mainPanel.add(new ManageSpeakersPanel(), "manage_speakers_panel"); // New
        mainPanel.add(new ManageSessionsPanel(), "manage_sessions_panel"); // New
        mainPanel.add(new ManageStaffPanel(), "manage_staff_panel");     // New


        // Set initial view to Dashboard
        navigateTo("dashboard_panel");
        setVisible(true);
    }

    private void addSideMenuButton(String text, String panelName) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT); // Center align in BoxLayout
        button.setMaximumSize(new Dimension(200, 40)); // Max width for buttons
        button.setMinimumSize(new Dimension(200, 40));
        button.setPreferredSize(new Dimension(200, 40));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(95, 120, 195)); // Slightly darker blue
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Padding
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.addActionListener(e -> {
            navigateTo(panelName);
            sideMenuButton.setSelected(false); // Close side menu after selection
            sideMenuPanel.setVisible(false);
        });
        sideMenuPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacing
        sideMenuPanel.add(button);
    }

    private void navigateTo(String panelName) {
        if (!panelHistory.isEmpty() && panelHistory.peek().equals(panelName)) {
            // Avoid adding duplicate consecutive entries if already on the same panel
            // This can happen if a button points to the current panel
        } else if (currentPanelName != null && !currentPanelName.equals(panelName)) {
            panelHistory.push(currentPanelName);
        }
        cardLayout.show(mainPanel, panelName);
        currentPanelName = panelName;
        updateNavigationButtons();
    }

    private void navigateBack() {
        if (!panelHistory.isEmpty()) {
            currentPanelName = panelHistory.pop();
            cardLayout.show(mainPanel, currentPanelName);
            updateNavigationButtons();
        }
    }

    private void updateNavigationButtons() {
        backButton.setEnabled(!panelHistory.isEmpty());
        // Home button is always enabled unless on dashboard
        homeButton.setEnabled(!currentPanelName.equals("dashboard_panel"));
    }

    public static void main(String[] args) {
        // Ensure database connection is valid
        Connection testConn = DBConnection.getConnection();
        if (testConn == null) {
            JOptionPane.showMessageDialog(null, "Failed to connect to the database. Please check your MySQL server and credentials.", "Database Connection Error", JOptionPane.ERROR_MESSAGE);
            return; // Exit if DB connection fails
        } else {
            try {
                testConn.close(); // Close test connection
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        SwingUtilities.invokeLater(EventManagementApp::new);
    }

    // --- DBConnection Class ---
    private static class DBConnection {
        public static Connection getConnection() {
            Connection conn = null;
            try {
                conn = DriverManager.getConnection(DB_URL, USER, PASS);
            } catch (SQLException e) {
                e.printStackTrace();
                // In a real app, log this or show a more user-friendly error
            }
            return conn;
        }
    }

    // --- Model Classes ---
    // (For better data handling between UI and DAO)

    private static class Event {
        int event_id;
        String event_name;
        LocalDate event_date;
        int venue_id;
        String description;
        String venue_name; // To display in UI

        public Event(int event_id, String event_name, LocalDate event_date, int venue_id, String description, String venue_name) {
            this.event_id = event_id;
            this.event_name = event_name;
            this.event_date = event_date;
            this.venue_id = venue_id;
            this.description = description;
            this.venue_name = venue_name;
        }

        @Override
        public String toString() {
            return event_name + " (" + event_date + ")";
        }
    }

    private static class Venue {
        int venue_id;
        String venue_name;

        public Venue(int venue_id, String venue_name) {
            this.venue_id = venue_id;
            this.venue_name = venue_name;
        }

        @Override
        public String toString() {
            return venue_name;
        }
    }

    private static class Attendee {
        int attendee_id;
        String attendee_name;
        String email;

        public Attendee(int attendee_id, String attendee_name, String email) {
            this.attendee_id = attendee_id;
            this.attendee_name = attendee_name;
            this.email = email;
        }

        @Override
        public String toString() {
            return attendee_name + " (" + email + ")";
        }
    }

    private static class Registration {
        int registration_id;
        int event_id;
        int attendee_id;
        Timestamp registration_date;
        String event_name; // For display
        String attendee_name; // For display

        public Registration(int registration_id, int event_id, int attendee_id, Timestamp registration_date, String event_name, String attendee_name) {
            this.registration_id = registration_id;
            this.event_id = event_id;
            this.attendee_id = attendee_id;
            this.registration_date = registration_date;
            this.event_name = event_name;
            this.attendee_name = attendee_name;
        }
    }

    private static class Ticket {
        int ticket_id;
        int registration_id;
        String ticket_type;
        double price;

        public Ticket(int ticket_id, int registration_id, String ticket_type, double price) {
            this.ticket_id = ticket_id;
            this.registration_id = registration_id;
            this.ticket_type = ticket_type;
            this.price = price;
        }
    }

    // --- NEW MODEL CLASSES ---
    private static class Speaker {
        int speaker_id;
        String speaker_name;
        String bio;

        public Speaker(int speaker_id, String speaker_name, String bio) {
            this.speaker_id = speaker_id;
            this.speaker_name = speaker_name;
            this.bio = bio;
        }

        @Override
        public String toString() {
            return speaker_name;
        }
    }

    private static class Staff {
        int staff_id;
        String staff_name;
        String role;
        String contact_email;

        public Staff(int staff_id, String staff_name, String role, String contact_email) {
            this.staff_id = staff_id;
            this.staff_name = staff_name;
            this.role = role;
            this.contact_email = contact_email;
        }

        @Override
        public String toString() {
            return staff_name + " (" + role + ")";
        }
    }

    private static class Session {
        int session_id;
        String session_title;
        int event_id;
        int speaker_id;
        LocalTime start_time;
        LocalTime end_time;
        String event_name; // For display
        String speaker_name; // For display

        public Session(int session_id, String session_title, int event_id, int speaker_id,
                       LocalTime start_time, LocalTime end_time, String event_name, String speaker_name) {
            this.session_id = session_id;
            this.session_title = session_title;
            this.event_id = event_id;
            this.speaker_id = speaker_id;
            this.start_time = start_time;
            this.end_time = end_time;
            this.event_name = event_name;
            this.speaker_name = speaker_name;
        }

        @Override
        public String toString() {
            return session_title + " (" + event_name + ")";
        }
    }


    // --- DAO Classes ---

    private static class EventDAO {
        public static List<Event> getAllEvents() {
            List<Event> events = new ArrayList<>();
            String sql = "SELECT e.event_id, e.event_name, e.event_date, e.venue_id, e.description, v.venue_name " +
                         "FROM Events e JOIN Venues v ON e.venue_id = v.venue_id ORDER BY e.event_date DESC";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    events.add(new Event(
                            rs.getInt("event_id"),
                            rs.getString("event_name"),
                            rs.getDate("event_date").toLocalDate(),
                            rs.getInt("venue_id"),
                            rs.getString("description"),
                            rs.getString("venue_name")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return events;
        }

        public static List<Venue> getAllVenues() {
            List<Venue> venues = new ArrayList<>();
            String sql = "SELECT venue_id, venue_name FROM Venues ORDER BY venue_name";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    venues.add(new Venue(rs.getInt("venue_id"), rs.getString("venue_name")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return venues;
        }

        public static boolean addEvent(Event event) {
            String sql = "INSERT INTO Events (event_name, event_date, venue_id, description) VALUES (?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, event.event_name);
                pstmt.setDate(2, Date.valueOf(event.event_date));
                pstmt.setInt(3, event.venue_id);
                pstmt.setString(4, event.description);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public static boolean updateEvent(Event event) {
            String sql = "UPDATE Events SET event_name = ?, event_date = ?, venue_id = ?, description = ? WHERE event_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, event.event_name);
                pstmt.setDate(2, Date.valueOf(event.event_date));
                pstmt.setInt(3, event.venue_id);
                pstmt.setString(4, event.description);
                pstmt.setInt(5, event.event_id);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public static boolean deleteEvent(int eventId) {
            String sql = "DELETE FROM Events WHERE event_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, eventId);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                // Handle foreign key constraint violation (e.g., event has registrations)
                JOptionPane.showMessageDialog(null, "Cannot delete event: It might have associated registrations, sessions, or other related data. Please delete them first.", "Deletion Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
    }

    private static class AttendeeDAO {
        public static List<Attendee> getAllAttendees() {
            List<Attendee> attendees = new ArrayList<>();
            String sql = "SELECT attendee_id, attendee_name, email FROM Attendees ORDER BY attendee_name";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    attendees.add(new Attendee(
                            rs.getInt("attendee_id"),
                            rs.getString("attendee_name"),
                            rs.getString("email")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return attendees;
        }

        public static Attendee getAttendeeByEmail(String email) {
            String sql = "SELECT attendee_id, attendee_name, email FROM Attendees WHERE email = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return new Attendee(rs.getInt("attendee_id"), rs.getString("attendee_name"), rs.getString("email"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        public static boolean addAttendee(Attendee attendee) {
            String sql = "INSERT INTO Attendees (attendee_name, email) VALUES (?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, attendee.attendee_name);
                pstmt.setString(2, attendee.email);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) { // Duplicate entry for UNIQUE key
                    JOptionPane.showMessageDialog(null, "Error: An attendee with this email already exists.", "Duplicate Email", JOptionPane.ERROR_MESSAGE);
                } else {
                    e.printStackTrace();
                }
                return false;
            }
        }
    }

    private static class RegistrationDAO {
        public static boolean addRegistration(int eventId, int attendeeId) {
            String sql = "INSERT INTO Registrations (event_id, attendee_id) VALUES (?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, eventId);
                pstmt.setInt(2, attendeeId);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) { // Duplicate entry for UNIQUE (event_id, attendee_id)
                    JOptionPane.showMessageDialog(null, "Error: This attendee is already registered for this event.", "Duplicate Registration", JOptionPane.ERROR_MESSAGE);
                } else {
                    e.printStackTrace();
                }
                return false;
            }
        }

        public static List<Registration> getRegistrationsByAttendeeId(int attendeeId) {
            List<Registration> registrations = new ArrayList<>();
            String sql = "SELECT r.registration_id, r.event_id, r.attendee_id, r.registration_date, e.event_name, a.attendee_name " +
                         "FROM Registrations r " +
                         "JOIN Events e ON r.event_id = e.event_id " +
                         "JOIN Attendees a ON r.attendee_id = a.attendee_id " +
                         "WHERE r.attendee_id = ? ORDER BY e.event_date DESC";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, attendeeId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        registrations.add(new Registration(
                                rs.getInt("registration_id"),
                                rs.getInt("event_id"),
                                rs.getInt("attendee_id"),
                                rs.getTimestamp("registration_date"),
                                rs.getString("event_name"),
                                rs.getString("attendee_name")
                        ));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return registrations;
        }

        public static boolean deleteRegistration(int registrationId) {
            // First delete dependent records (e.g., from Payments, Tickets)
            // This is a cascade operation, but if DB is not configured for CASCADE DELETE,
            // we must do it manually or user has to delete them manually.
            // For simplicity, we'll assume DB handles cascading or tell user.
            // A more robust solution would delete payments/tickets first.
            String sql = "DELETE FROM Registrations WHERE registration_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, registrationId);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Cannot cancel registration: It might have associated payments or tickets. Please remove those first if necessary, or configure database for cascade delete.", "Deletion Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        public static List<Registration> getAllRegistrationsWithEventAndAttendeeNames() {
            List<Registration> registrations = new ArrayList<>();
            String sql = "SELECT r.registration_id, r.event_id, r.attendee_id, r.registration_date, e.event_name, a.attendee_name " +
                         "FROM Registrations r " +
                         "JOIN Events e ON r.event_id = e.event_id " +
                         "JOIN Attendees a ON r.attendee_id = a.attendee_id ORDER BY r.registration_date DESC";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    registrations.add(new Registration(
                            rs.getInt("registration_id"),
                            rs.getInt("event_id"),
                            rs.getInt("attendee_id"),
                            rs.getTimestamp("registration_date"),
                            rs.getString("event_name"),
                            rs.getString("attendee_name")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return registrations;
        }
    }

    private static class PaymentDAO {
        public static boolean addPayment(int registrationId, double amount, String paymentMethod) {
            String sql = "INSERT INTO Payments (registration_id, amount, payment_method) VALUES (?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, registrationId);
                pstmt.setDouble(2, amount);
                pstmt.setString(3, paymentMethod);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public static List<Registration> getUnpaidRegistrations() {
            List<Registration> registrations = new ArrayList<>();
            // Select registrations that do not have a corresponding payment record
            String sql = "SELECT r.registration_id, e.event_name, a.attendee_name " +
                         "FROM Registrations r " +
                         "JOIN Events e ON r.event_id = e.event_id " +
                         "JOIN Attendees a ON r.attendee_id = a.attendee_id " +
                         "LEFT JOIN Payments p ON r.registration_id = p.registration_id " +
                         "WHERE p.registration_id IS NULL ORDER BY e.event_date ASC, a.attendee_name ASC";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    registrations.add(new Registration(
                            rs.getInt("registration_id"),
                            0, // event_id not needed for this list
                            0, // attendee_id not needed for this list
                            null, // registration_date not needed
                            rs.getString("event_name"),
                            rs.getString("attendee_name")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return registrations;
        }
    }

    // --- NEW DAO CLASSES ---

    private static class SpeakerDAO {
        public static List<Speaker> getAllSpeakers() {
            List<Speaker> speakers = new ArrayList<>();
            String sql = "SELECT speaker_id, speaker_name, bio FROM Speakers ORDER BY speaker_name";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    speakers.add(new Speaker(
                            rs.getInt("speaker_id"),
                            rs.getString("speaker_name"),
                            rs.getString("bio")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return speakers;
        }

        public static boolean addSpeaker(Speaker speaker) {
            String sql = "INSERT INTO Speakers (speaker_name, bio) VALUES (?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, speaker.speaker_name);
                pstmt.setString(2, speaker.bio);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public static boolean updateSpeaker(Speaker speaker) {
            String sql = "UPDATE Speakers SET speaker_name = ?, bio = ? WHERE speaker_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, speaker.speaker_name);
                pstmt.setString(2, speaker.bio);
                pstmt.setInt(3, speaker.speaker_id);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public static boolean deleteSpeaker(int speakerId) {
            String sql = "DELETE FROM Speakers WHERE speaker_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, speakerId);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Cannot delete speaker: They might be assigned to sessions. Please remove them from sessions first.", "Deletion Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
    }

    private static class StaffDAO {
        public static List<Staff> getAllStaff() {
            List<Staff> staffList = new ArrayList<>();
            String sql = "SELECT staff_id, staff_name, role, contact_email FROM Staff ORDER BY staff_name";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    staffList.add(new Staff(
                            rs.getInt("staff_id"),
                            rs.getString("staff_name"),
                            rs.getString("role"),
                            rs.getString("contact_email")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return staffList;
        }

        public static boolean addStaff(Staff staff) {
            String sql = "INSERT INTO Staff (staff_name, role, contact_email) VALUES (?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, staff.staff_name);
                pstmt.setString(2, staff.role);
                pstmt.setString(3, staff.contact_email);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) { // Duplicate entry for UNIQUE email
                    JOptionPane.showMessageDialog(null, "Error: Staff member with this email already exists.", "Duplicate Email", JOptionPane.ERROR_MESSAGE);
                } else {
                    e.printStackTrace();
                }
                return false;
            }
        }

        public static boolean updateStaff(Staff staff) {
            String sql = "UPDATE Staff SET staff_name = ?, role = ?, contact_email = ? WHERE staff_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, staff.staff_name);
                pstmt.setString(2, staff.role);
                pstmt.setString(3, staff.contact_email);
                pstmt.setInt(4, staff.staff_id);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) { // Duplicate entry for UNIQUE email
                    JOptionPane.showMessageDialog(null, "Error: Staff member with this email already exists.", "Duplicate Email", JOptionPane.ERROR_MESSAGE);
                } else {
                    e.printStackTrace();
                }
                return false;
            }
        }

        public static boolean deleteStaff(int staffId) {
            String sql = "DELETE FROM Staff WHERE staff_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, staffId);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Cannot delete staff member: They might be assigned to tasks. Please remove them from tasks first.", "Deletion Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
    }

    private static class SessionDAO {
        public static List<Session> getAllSessions() {
            List<Session> sessions = new ArrayList<>();
            String sql = "SELECT s.session_id, s.session_title, s.event_id, s.speaker_id, s.start_time, s.end_time, " +
                         "e.event_name, sp.speaker_name " +
                         "FROM Sessions s " +
                         "JOIN Events e ON s.event_id = e.event_id " +
                         "LEFT JOIN Speakers sp ON s.speaker_id = sp.speaker_id ORDER BY e.event_date DESC, s.start_time ASC";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sessions.add(new Session(
                            rs.getInt("session_id"),
                            rs.getString("session_title"),
                            rs.getInt("event_id"),
                            rs.getInt("speaker_id"),
                            rs.getTime("start_time") != null ? rs.getTime("start_time").toLocalTime() : null,
                            rs.getTime("end_time") != null ? rs.getTime("end_time").toLocalTime() : null,
                            rs.getString("event_name"),
                            rs.getString("speaker_name")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return sessions;
        }

        public static boolean addSession(Session session) {
            String sql = "INSERT INTO Sessions (session_title, event_id, speaker_id, start_time, end_time) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, session.session_title);
                pstmt.setInt(2, session.event_id);
                // Handle nullable speaker_id
                if (session.speaker_id == 0) { // Assuming 0 means no speaker selected
                    pstmt.setNull(3, Types.INTEGER);
                } else {
                    pstmt.setInt(3, session.speaker_id);
                }
                pstmt.setTime(4, session.start_time != null ? Time.valueOf(session.start_time) : null);
                pstmt.setTime(5, session.end_time != null ? Time.valueOf(session.end_time) : null);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public static boolean updateSession(Session session) {
            String sql = "UPDATE Sessions SET session_title = ?, event_id = ?, speaker_id = ?, start_time = ?, end_time = ? WHERE session_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, session.session_title);
                pstmt.setInt(2, session.event_id);
                if (session.speaker_id == 0) {
                    pstmt.setNull(3, Types.INTEGER);
                } else {
                    pstmt.setInt(3, session.speaker_id);
                }
                pstmt.setTime(4, session.start_time != null ? Time.valueOf(session.start_time) : null);
                pstmt.setTime(5, session.end_time != null ? Time.valueOf(session.end_time) : null);
                pstmt.setInt(6, session.session_id);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public static boolean deleteSession(int sessionId) {
            String sql = "DELETE FROM Sessions WHERE session_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, sessionId);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Cannot delete session: It might have related data.", "Deletion Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
    }


    // --- UI Panels ---

    private class DashboardPanel extends JPanel {
        public DashboardPanel() {
            setLayout(new GridBagLayout());
            setBackground(new Color(230, 235, 240)); // Light grey/blue background

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(15, 15, 15, 15);
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;

            JLabel title = new JLabel("Welcome to EventSphere!", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 36));
            title.setForeground(new Color(50, 70, 90));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            add(title, gbc);

            JPanel buttonPanel = new JPanel(new GridLayout(4, 2, 20, 20)); // 4 rows, 2 columns, with gaps
            buttonPanel.setBackground(new Color(230, 235, 240));

            addButton(buttonPanel, "Add New Event", "add_events_panel");
            addButton(buttonPanel, "Register New User", "register_attendee_panel");
            addButton(buttonPanel, "View All Events", "view_events_panel");
            addButton(buttonPanel, "Apply for an Event", "apply_for_event_panel");
            addButton(buttonPanel, "Process Event Payment", "pay_for_event_panel");
            addButton(buttonPanel, "View Your Booked Events", "view_booked_events_panel");
            addButton(buttonPanel, "Manage Speakers", "manage_speakers_panel"); // New
            addButton(buttonPanel, "Manage Sessions", "manage_sessions_panel"); // New
            addButton(buttonPanel, "Manage Staff", "manage_staff_panel");     // New

            gbc.gridy = 1;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(30, 50, 30, 50); // More padding for button panel
            add(buttonPanel, gbc);
        }

        private void addButton(JPanel panel, String text, String targetPanelName) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.BOLD, 18));
            button.setBackground(new Color(100, 140, 220)); // Blue color
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createRaisedBevelBorder());
            button.addActionListener(e -> navigateTo(targetPanelName));
            panel.add(button);
        }
    }

    private class AddEventPanel extends JPanel {
        private JTextField eventNameField;
        private JTextField eventDateField; // YYYY-MM-DD
        private JComboBox<Venue> venueComboBox;
        private JTextArea descriptionArea;
        private List<Venue> venues;

        public AddEventPanel() {
            setLayout(new BorderLayout(20, 20));
            setBackground(new Color(240, 248, 255)); // AliceBlue

            JLabel title = new JLabel("Add New Event", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 28));
            title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            add(title, BorderLayout.NORTH);

            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(new Color(240, 248, 255));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Event Name
            gbc.gridx = 0;
            gbc.gridy = 0;
            formPanel.add(new JLabel("Event Name:"), gbc);
            gbc.gridx = 1;
            eventNameField = new JTextField(20);
            formPanel.add(eventNameField, gbc);

            // Event Date
            gbc.gridx = 0;
            gbc.gridy = 1;
            formPanel.add(new JLabel("Event Date (YYYY-MM-DD):"), gbc);
            gbc.gridx = 1;
            eventDateField = new JTextField(20);
            formPanel.add(eventDateField, gbc);

            // Venue
            gbc.gridx = 0;
            gbc.gridy = 2;
            formPanel.add(new JLabel("Venue:"), gbc);
            gbc.gridx = 1;
            venueComboBox = new JComboBox<>();
            populateVenues();
            formPanel.add(venueComboBox, gbc);

            // Description
            gbc.gridx = 0;
            gbc.gridy = 3;
            formPanel.add(new JLabel("Description:"), gbc);
            gbc.gridx = 1;
            descriptionArea = new JTextArea(5, 20);
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(descriptionArea);
            formPanel.add(scrollPane, gbc);

            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 2;
            JButton addButton = new JButton("Add Event");
            addButton.setFont(new Font("Arial", Font.BOLD, 16));
            addButton.setBackground(new Color(60, 179, 113)); // MediumSeaGreen
            addButton.setForeground(Color.WHITE);
            addButton.setFocusPainted(false);
            addButton.addActionListener(e -> addEvent());
            formPanel.add(addButton, gbc);

            add(formPanel, BorderLayout.CENTER);
        }

        private void populateVenues() {
            venues = EventDAO.getAllVenues();
            venueComboBox.removeAllItems();
            for (Venue venue : venues) {
                venueComboBox.addItem(venue);
            }
        }

        private void addEvent() {
            String eventName = eventNameField.getText().trim();
            String eventDateStr = eventDateField.getText().trim();
            Venue selectedVenue = (Venue) venueComboBox.getSelectedItem();
            String description = descriptionArea.getText().trim();

            if (eventName.isEmpty() || eventDateStr.isEmpty() || selectedVenue == null) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields (Event Name, Date, Venue).", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                LocalDate eventDate = LocalDate.parse(eventDateStr);
                Event newEvent = new Event(0, eventName, eventDate, selectedVenue.venue_id, description, selectedVenue.venue_name);

                if (EventDAO.addEvent(newEvent)) {
                    JOptionPane.showMessageDialog(this, "Event added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    // Clear fields
                    eventNameField.setText("");
                    eventDateField.setText("");
                    descriptionArea.setText("");
                    populateVenues(); // Refresh venues in case list changed
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add event.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Date Format Error", JOptionPane.WARNING_MESSAGE);
            }
        }
        @Override
        public void addNotify() {
            super.addNotify();
            // This is called when the panel is added to a parent or becomes visible indirectly
            // Ensures venues are populated when panel is shown
            populateVenues();
        }
    }

    private class RegisterAttendeePanel extends JPanel {
        private JTextField nameField;
        private JTextField emailField;

        public RegisterAttendeePanel() {
            setLayout(new BorderLayout(20, 20));
            setBackground(new Color(240, 248, 255));

            JLabel title = new JLabel("Register New User (Attendee)", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 28));
            title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            add(title, BorderLayout.NORTH);

            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(new Color(240, 248, 255));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Name
            gbc.gridx = 0;
            gbc.gridy = 0;
            formPanel.add(new JLabel("Attendee Name:"), gbc);
            gbc.gridx = 1;
            nameField = new JTextField(20);
            formPanel.add(nameField, gbc);

            // Email
            gbc.gridx = 0;
            gbc.gridy = 1;
            formPanel.add(new JLabel("Email:"), gbc);
            gbc.gridx = 1;
            emailField = new JTextField(20);
            formPanel.add(emailField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            JButton registerButton = new JButton("Register Attendee");
            registerButton.setFont(new Font("Arial", Font.BOLD, 16));
            registerButton.setBackground(new Color(60, 179, 113));
            registerButton.setForeground(Color.WHITE);
            registerButton.setFocusPainted(false);
            registerButton.addActionListener(e -> registerAttendee());
            formPanel.add(registerButton, gbc);

            add(formPanel, BorderLayout.CENTER);
        }

        private void registerAttendee() {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();

            if (name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in both name and email.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Invalid Email", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Attendee newAttendee = new Attendee(0, name, email);
            if (AttendeeDAO.addAttendee(newAttendee)) {
                JOptionPane.showMessageDialog(this, "Attendee registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                nameField.setText("");
                emailField.setText("");
            } else {
                // Error message already handled by DAO for duplicate email
                // JOptionPane.showMessageDialog(this, "Failed to register attendee. Email might already exist.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class ViewEventsPanel extends JPanel {
        private JTable eventTable;
        private DefaultTableModel tableModel;
        private JButton updateButton;
        private JButton deleteButton;
        private JButton registerForEventButton;

        // Fields for editing
        private JTextField editEventNameField;
        private JTextField editEventDateField;
        private JComboBox<Venue> editVenueComboBox;
        private JTextArea editDescriptionArea;
        private int selectedEventId = -1; // To store the ID of the selected event for update/delete

        public ViewEventsPanel() {
            setLayout(new BorderLayout(10, 10));
            setBackground(new Color(240, 248, 255));

            JLabel title = new JLabel("All Available Events", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 28));
            title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
            add(title, BorderLayout.NORTH);

            // Table setup
            String[] columnNames = {"ID", "Event Name", "Date", "Venue", "Description"};
            tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Make cells non-editable
                }
            };
            eventTable = new JTable(tableModel);
            eventTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Only one row selectable
            eventTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && eventTable.getSelectedRow() != -1) {
                    int selectedRow = eventTable.getSelectedRow();
                    selectedEventId = (int) tableModel.getValueAt(selectedRow, 0); // Get ID
                    populateEditFields(selectedRow);
                    updateButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                    registerForEventButton.setEnabled(true);
                } else {
                    selectedEventId = -1;
                    clearEditFields();
                    updateButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                    registerForEventButton.setEnabled(false);
                }
            });
            JScrollPane scrollPane = new JScrollPane(eventTable);
            add(scrollPane, BorderLayout.CENTER);

            // --- Edit/CRUD Panel ---
            JPanel crudPanel = new JPanel(new BorderLayout(10, 10));
            crudPanel.setBorder(BorderFactory.createTitledBorder("Selected Event Details (Edit/Delete)"));
            crudPanel.setBackground(new Color(240, 248, 255));

            JPanel editFormPanel = new JPanel(new GridBagLayout());
            editFormPanel.setBackground(new Color(240, 248, 255));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Fields for editing
            gbc.gridx = 0; gbc.gridy = 0; editFormPanel.add(new JLabel("Event Name:"), gbc);
            gbc.gridx = 1; editEventNameField = new JTextField(20); editFormPanel.add(editEventNameField, gbc);

            gbc.gridx = 0; gbc.gridy = 1; editFormPanel.add(new JLabel("Event Date (YYYY-MM-DD):"), gbc);
            gbc.gridx = 1; editEventDateField = new JTextField(20); editFormPanel.add(editEventDateField, gbc);

            gbc.gridx = 0; gbc.gridy = 2; editFormPanel.add(new JLabel("Venue:"), gbc);
            gbc.gridx = 1; editVenueComboBox = new JComboBox<>(); editFormPanel.add(editVenueComboBox, gbc);
            populateEditVenues(); // Populate venue combo box for editing

            gbc.gridx = 0; gbc.gridy = 3; editFormPanel.add(new JLabel("Description:"), gbc);
            gbc.gridx = 1; editDescriptionArea = new JTextArea(3, 20);
            editDescriptionArea.setLineWrap(true);
            editDescriptionArea.setWrapStyleWord(true);
            JScrollPane descScrollPane = new JScrollPane(editDescriptionArea);
            editFormPanel.add(descScrollPane, gbc);

            crudPanel.add(editFormPanel, BorderLayout.CENTER);

            // Buttons for actions
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            buttonPanel.setBackground(new Color(240, 248, 255));

            updateButton = new JButton("Update Event");
            deleteButton = new JButton("Delete Event");
            registerForEventButton = new JButton("Register for this Event");

            updateButton.setFont(new Font("Arial", Font.BOLD, 14));
            deleteButton.setFont(new Font("Arial", Font.BOLD, 14));
            registerForEventButton.setFont(new Font("Arial", Font.BOLD, 14));

            updateButton.setBackground(new Color(255, 165, 0)); // Orange
            deleteButton.setBackground(new Color(220, 20, 60)); // Crimson
            registerForEventButton.setBackground(new Color(30, 144, 255)); // DodgerBlue

            updateButton.setForeground(Color.WHITE);
            deleteButton.setForeground(Color.WHITE);
            registerForEventButton.setForeground(Color.WHITE);

            updateButton.setFocusPainted(false);
            deleteButton.setFocusPainted(false);
            registerForEventButton.setFocusPainted(false);

            updateButton.setEnabled(false);
            deleteButton.setEnabled(false);
            registerForEventButton.setEnabled(false);

            updateButton.addActionListener(e -> updateEvent());
            deleteButton.addActionListener(e -> deleteEvent());
            registerForEventButton.addActionListener(e -> {
                if (selectedEventId != -1) {
                    // Navigate to ApplyForEventPanel and pre-select the event
                    // Find the ApplyForEventPanel instance to call preSelectEvent
                    for (Component comp : ((EventManagementApp) getTopLevelAncestor()).mainPanel.getComponents()) {
                        if (comp instanceof ApplyForEventPanel) {
                            ((ApplyForEventPanel) comp).preSelectEvent(selectedEventId);
                            break;
                        }
                    }
                    ((EventManagementApp) getTopLevelAncestor()).navigateTo("apply_for_event_panel");
                }
            });

            buttonPanel.add(updateButton);
            buttonPanel.add(deleteButton);
            buttonPanel.add(registerForEventButton);

            crudPanel.add(buttonPanel, BorderLayout.SOUTH);
            add(crudPanel, BorderLayout.SOUTH);
        }

        private void populateEditVenues() {
            List<Venue> venues = EventDAO.getAllVenues();
            editVenueComboBox.removeAllItems();
            for (Venue venue : venues) {
                editVenueComboBox.addItem(venue);
            }
        }

        private void populateTable() {
            tableModel.setRowCount(0); // Clear existing data
            List<Event> events = EventDAO.getAllEvents();
            for (Event event : events) {
                tableModel.addRow(new Object[]{
                    event.event_id,
                    event.event_name,
                    event.event_date,
                    event.venue_name, // Display venue name
                    event.description
                });
            }
        }

        private void populateEditFields(int rowIndex) {
            editEventNameField.setText((String) tableModel.getValueAt(rowIndex, 1));
            editEventDateField.setText(tableModel.getValueAt(rowIndex, 2).toString()); // LocalDate converts to String
            String venueName = (String) tableModel.getValueAt(rowIndex, 3);
            for (int i = 0; i < editVenueComboBox.getItemCount(); i++) {
                if (editVenueComboBox.getItemAt(i).venue_name.equals(venueName)) {
                    editVenueComboBox.setSelectedIndex(i);
                    break;
                }
            }
            editDescriptionArea.setText((String) tableModel.getValueAt(rowIndex, 4));
        }

        private void clearEditFields() {
            editEventNameField.setText("");
            editEventDateField.setText("");
            editVenueComboBox.setSelectedIndex(-1); // Clear selection
            editDescriptionArea.setText("");
        }

        private void updateEvent() {
            if (selectedEventId == -1) {
                JOptionPane.showMessageDialog(this, "Please select an event to update.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String eventName = editEventNameField.getText().trim();
            String eventDateStr = editEventDateField.getText().trim();
            Venue selectedVenue = (Venue) editVenueComboBox.getSelectedItem();
            String description = editDescriptionArea.getText().trim();

            if (eventName.isEmpty() || eventDateStr.isEmpty() || selectedVenue == null) {
                JOptionPane.showMessageDialog(this, "Please fill in all event details for update.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                LocalDate eventDate = LocalDate.parse(eventDateStr);
                Event updatedEvent = new Event(selectedEventId, eventName, eventDate, selectedVenue.venue_id, description, selectedVenue.venue_name);

                if (EventDAO.updateEvent(updatedEvent)) {
                    JOptionPane.showMessageDialog(this, "Event updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    populateTable(); // Refresh table
                    clearEditFields();
                    updateButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                    registerForEventButton.setEnabled(false);
                    eventTable.clearSelection();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update event.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Date Format Error", JOptionPane.WARNING_MESSAGE);
            }
        }

        private void deleteEvent() {
            if (selectedEventId == -1) {
                JOptionPane.showMessageDialog(this, "Please select an event to delete.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this event? This action might affect related data.", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (EventDAO.deleteEvent(selectedEventId)) {
                    JOptionPane.showMessageDialog(this, "Event deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    populateTable(); // Refresh table
                    clearEditFields();
                    updateButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                    registerForEventButton.setEnabled(false);
                    eventTable.clearSelection();
                } else {
                    // Error message already handled by DAO for foreign key constraint
                    // JOptionPane.showMessageDialog(this, "Failed to delete event. It might have associated data (e.g., registrations).", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        @Override
        public void addNotify() {
            super.addNotify();
            // This is called when the panel is added to a parent or becomes visible indirectly
            // Ensures table is populated when panel is shown
            populateTable();
            populateEditVenues();
            clearEditFields();
            updateButton.setEnabled(false);
            deleteButton.setEnabled(false);
            registerForEventButton.setEnabled(false);
            eventTable.clearSelection(); // Clear any previous selection
        }
    }

    private class ApplyForEventPanel extends JPanel {
        private JComboBox<Event> eventComboBox;
        private JComboBox<Attendee> attendeeComboBox;

        public ApplyForEventPanel() {
            setLayout(new BorderLayout(20, 20));
            setBackground(new Color(240, 248, 255));

            JLabel title = new JLabel("Apply / Register for an Event", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 28));
            title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            add(title, BorderLayout.NORTH);

            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(new Color(240, 248, 255));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Select Event
            gbc.gridx = 0;
            gbc.gridy = 0;
            formPanel.add(new JLabel("Select Event:"), gbc);
            gbc.gridx = 1;
            eventComboBox = new JComboBox<>();
            formPanel.add(eventComboBox, gbc);

            // Select Attendee
            gbc.gridx = 0;
            gbc.gridy = 1;
            formPanel.add(new JLabel("Select Attendee:"), gbc);
            gbc.gridx = 1;
            attendeeComboBox = new JComboBox<>();
            formPanel.add(attendeeComboBox, gbc);

            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            JButton applyButton = new JButton("Register for Event");
            applyButton.setFont(new Font("Arial", Font.BOLD, 16));
            applyButton.setBackground(new Color(30, 144, 255)); // DodgerBlue
            applyButton.setForeground(Color.WHITE);
            applyButton.setFocusPainted(false);
            applyButton.addActionListener(e -> applyForEvent());
            formPanel.add(applyButton, gbc);

            add(formPanel, BorderLayout.CENTER);
        }

        private void populateComboBoxes() {
            eventComboBox.removeAllItems();
            List<Event> events = EventDAO.getAllEvents();
            for (Event event : events) {
                eventComboBox.addItem(event);
            }

            attendeeComboBox.removeAllItems();
            List<Attendee> attendees = AttendeeDAO.getAllAttendees();
            for (Attendee attendee : attendees) {
                attendeeComboBox.addItem(attendee);
            }
        }

        public void preSelectEvent(int eventId) {
            populateComboBoxes(); // Ensure comboboxes are populated first
            for (int i = 0; i < eventComboBox.getItemCount(); i++) {
                if (eventComboBox.getItemAt(i).event_id == eventId) {
                    eventComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }


        private void applyForEvent() {
            Event selectedEvent = (Event) eventComboBox.getSelectedItem();
            Attendee selectedAttendee = (Attendee) attendeeComboBox.getSelectedItem();

            if (selectedEvent == null || selectedAttendee == null) {
                JOptionPane.showMessageDialog(this, "Please select both an event and an attendee.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (RegistrationDAO.addRegistration(selectedEvent.event_id, selectedAttendee.attendee_id)) {
                JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Error message already handled by DAO for duplicate registration
                // JOptionPane.showMessageDialog(this, "Failed to register. Attendee might already be registered for this event.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        @Override
        public void addNotify() {
            super.addNotify();
            // This is called when the panel is added to a parent or becomes visible indirectly
            // Ensures comboboxes are populated when panel is shown
            populateComboBoxes();
        }
    }

    private class PayForEventPanel extends JPanel {
        private JComboBox<Registration> registrationComboBox;
        private JTextField amountField;
        private JComboBox<String> paymentMethodComboBox;
        private DecimalFormat df = new DecimalFormat("#.00"); // For consistent display of currency

        public PayForEventPanel() {
            setLayout(new BorderLayout(20, 20));
            setBackground(new Color(240, 248, 255));

            JLabel title = new JLabel("Process Event Payment", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 28));
            title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            add(title, BorderLayout.NORTH);

            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(new Color(240, 248, 255));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Select Registration
            gbc.gridx = 0;
            gbc.gridy = 0;
            formPanel.add(new JLabel("Select Registration (Unpaid):"), gbc);
            gbc.gridx = 1;
            registrationComboBox = new JComboBox<>();
            formPanel.add(registrationComboBox, gbc);

            // Amount
            gbc.gridx = 0;
            gbc.gridy = 1;
            formPanel.add(new JLabel("Amount:"), gbc);
            gbc.gridx = 1;
            amountField = new JTextField(20);
            formPanel.add(amountField, gbc);

            // Payment Method
            gbc.gridx = 0;
            gbc.gridy = 2;
            formPanel.add(new JLabel("Payment Method:"), gbc);
            gbc.gridx = 1;
            paymentMethodComboBox = new JComboBox<>(new String[]{"Credit Card", "PayPal", "Bank Transfer", "Cash"});
            formPanel.add(paymentMethodComboBox, gbc);

            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            JButton payButton = new JButton("Record Payment");
            payButton.setFont(new Font("Arial", Font.BOLD, 16));
            payButton.setBackground(new Color(255, 165, 0)); // Orange
            payButton.setForeground(Color.WHITE);
            payButton.setFocusPainted(false);
            payButton.addActionListener(e -> recordPayment());
            formPanel.add(payButton, gbc);

            add(formPanel, BorderLayout.CENTER);
        }

        private void populateRegistrations() {
            registrationComboBox.removeAllItems();
            // Get registrations that haven't been paid yet
            List<Registration> unpaidRegistrations = PaymentDAO.getUnpaidRegistrations();
            for (Registration reg : unpaidRegistrations) {
                // Display in a user-friendly way
                registrationComboBox.addItem(new Registration(reg.registration_id, 0, 0, null, reg.event_name, reg.attendee_name) {
                    @Override
                    public String toString() {
                        return "Reg ID " + registration_id + ": " + attendee_name + " for " + event_name;
                    }
                });
            }
        }

        private void recordPayment() {
            Registration selectedRegistration = (Registration) registrationComboBox.getSelectedItem();
            String amountStr = amountField.getText().trim();
            String paymentMethod = (String) paymentMethodComboBox.getSelectedItem();

            if (selectedRegistration == null || amountStr.isEmpty() || paymentMethod == null) {
                JOptionPane.showMessageDialog(this, "Please select a registration, enter amount, and select payment method.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Amount must be positive.", "Input Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (PaymentDAO.addPayment(selectedRegistration.registration_id, Double.parseDouble(df.format(amount)), paymentMethod)) {
                    JOptionPane.showMessageDialog(this, "Payment recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    amountField.setText("");
                    populateRegistrations(); // Refresh the list of unpaid registrations
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to record payment.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount. Please enter a numeric value (e.g., 100.00).", "Input Error", JOptionPane.WARNING_MESSAGE);
            }
        }

        @Override
        public void addNotify() {
            super.addNotify();
            // This is called when the panel is added to a parent or becomes visible indirectly
            populateRegistrations();
            amountField.setText(""); // Clear previous amount
        }
    }

    private class ViewBookedEventsPanel extends JPanel {
        private JTable bookedEventsTable;
        private DefaultTableModel tableModel;
        private JTextField attendeeEmailField;
        private JButton searchButton;
        private JButton cancelButton; // Button to cancel registration
        private int selectedRegistrationId = -1; // To store registration ID for cancellation

        public ViewBookedEventsPanel() {
            setLayout(new BorderLayout(10, 10));
            setBackground(new Color(240, 248, 255));

            JLabel title = new JLabel("Your Booked Events", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 28));
            title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
            add(title, BorderLayout.NORTH);

            // Search Panel
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            searchPanel.setBackground(new Color(240, 248, 255));
            attendeeEmailField = new JTextField(25);
            searchButton = new JButton("Search by Attendee Email");
            searchButton.setFont(new Font("Arial", Font.BOLD, 14));
            searchButton.setBackground(new Color(75, 110, 175));
            searchButton.setForeground(Color.WHITE);
            searchButton.setFocusPainted(false);
            searchButton.addActionListener(e -> loadBookedEvents());
            searchPanel.add(new JLabel("Attendee Email:"));
            searchPanel.add(attendeeEmailField);
            searchPanel.add(searchButton);
            add(searchPanel, BorderLayout.NORTH);

            // Table setup
            String[] columnNames = {"Registration ID", "Event Name", "Attendee Name", "Registration Date"};
            tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            bookedEventsTable = new JTable(tableModel);
            bookedEventsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            bookedEventsTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && bookedEventsTable.getSelectedRow() != -1) {
                    int selectedRow = bookedEventsTable.getSelectedRow();
                    selectedRegistrationId = (int) tableModel.getValueAt(selectedRow, 0); // Get Registration ID
                    cancelButton.setEnabled(true);
                } else {
                    selectedRegistrationId = -1;
                    cancelButton.setEnabled(false);
                }
            });
            JScrollPane scrollPane = new JScrollPane(bookedEventsTable);
            add(scrollPane, BorderLayout.CENTER);

            // Action Button Panel
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            actionPanel.setBackground(new Color(240, 248, 255));
            cancelButton = new JButton("Cancel Registration");
            cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
            cancelButton.setBackground(new Color(220, 20, 60)); // Crimson
            cancelButton.setForeground(Color.WHITE);
            cancelButton.setFocusPainted(false);
            cancelButton.setEnabled(false);
            cancelButton.addActionListener(e -> cancelRegistration());
            actionPanel.add(cancelButton);
            add(actionPanel, BorderLayout.SOUTH);
        }

        private void loadBookedEvents() {
            String email = attendeeEmailField.getText().trim();
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an attendee email.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Attendee attendee = AttendeeDAO.getAttendeeByEmail(email);
            if (attendee == null) {
                JOptionPane.showMessageDialog(this, "No attendee found with this email.", "Not Found", JOptionPane.INFORMATION_MESSAGE);
                tableModel.setRowCount(0); // Clear table
                return;
            }

            tableModel.setRowCount(0); // Clear existing data
            List<Registration> registrations = RegistrationDAO.getRegistrationsByAttendeeId(attendee.attendee_id);
            if (registrations.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No events booked by " + attendee.attendee_name + ".", "No Bookings", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            for (Registration reg : registrations) {
                tableModel.addRow(new Object[]{
                    reg.registration_id,
                    reg.event_name,
                    reg.attendee_name,
                    reg.registration_date
                });
            }
        }

        private void cancelRegistration() {
            if (selectedRegistrationId == -1) {
                JOptionPane.showMessageDialog(this, "Please select a registration to cancel.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel this registration? This might affect related payments or tickets.", "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (RegistrationDAO.deleteRegistration(selectedRegistrationId)) {
                    JOptionPane.showMessageDialog(this, "Registration cancelled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadBookedEvents(); // Refresh the table
                    cancelButton.setEnabled(false);
                    bookedEventsTable.clearSelection();
                } else {
                    // Error message handled by DAO for foreign key constraint
                    // JOptionPane.showMessageDialog(this, "Failed to cancel registration. It might have associated payments or tickets.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        @Override
        public void addNotify() {
            super.addNotify();
            // Clear fields and table when panel is shown
            attendeeEmailField.setText("");
            tableModel.setRowCount(0);
            cancelButton.setEnabled(false);
            bookedEventsTable.clearSelection();
            selectedRegistrationId = -1;
        }
    }


    // --- NEW UI PANELS FOR EXTENSION ---

    private class ManageSpeakersPanel extends JPanel {
        private JTable speakerTable;
        private DefaultTableModel tableModel;
        private JTextField speakerNameField;
        private JTextArea bioArea;
        private JButton addButton;
        private JButton updateButton;
        private JButton deleteButton;
        private int selectedSpeakerId = -1;

        public ManageSpeakersPanel() {
            setLayout(new BorderLayout(10, 10));
            setBackground(new Color(240, 248, 255));

            JLabel title = new JLabel("Manage Speakers", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 28));
            title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
            add(title, BorderLayout.NORTH);

            // Table
            String[] columnNames = {"ID", "Speaker Name", "Bio"};
            tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            speakerTable = new JTable(tableModel);
            speakerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            speakerTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && speakerTable.getSelectedRow() != -1) {
                    int selectedRow = speakerTable.getSelectedRow();
                    selectedSpeakerId = (int) tableModel.getValueAt(selectedRow, 0);
                    speakerNameField.setText((String) tableModel.getValueAt(selectedRow, 1));
                    bioArea.setText((String) tableModel.getValueAt(selectedRow, 2));
                    addButton.setEnabled(false); // Can't add if selecting for update/delete
                    updateButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                } else {
                    selectedSpeakerId = -1;
                    clearFields();
                    addButton.setEnabled(true);
                    updateButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                }
            });
            JScrollPane scrollPane = new JScrollPane(speakerTable);
            add(scrollPane, BorderLayout.CENTER);

            // CRUD Panel
            JPanel crudPanel = new JPanel(new BorderLayout(10, 10));
            crudPanel.setBorder(BorderFactory.createTitledBorder("Speaker Details"));
            crudPanel.setBackground(new Color(240, 248, 255));

            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(new Color(240, 248, 255));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Speaker Name:"), gbc);
            gbc.gridx = 1; speakerNameField = new JTextField(25); formPanel.add(speakerNameField, gbc);

            gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Bio:"), gbc);
            gbc.gridx = 1; bioArea = new JTextArea(4, 25);
            bioArea.setLineWrap(true);
            bioArea.setWrapStyleWord(true);
            JScrollPane bioScrollPane = new JScrollPane(bioArea);
            formPanel.add(bioScrollPane, gbc);

            crudPanel.add(formPanel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            buttonPanel.setBackground(new Color(240, 248, 255));

            addButton = new JButton("Add Speaker");
            updateButton = new JButton("Update Speaker");
            deleteButton = new JButton("Delete Speaker");

            addButton.setFont(new Font("Arial", Font.BOLD, 14));
            updateButton.setFont(new Font("Arial", Font.BOLD, 14));
            deleteButton.setFont(new Font("Arial", Font.BOLD, 14));

            addButton.setBackground(new Color(60, 179, 113));
            updateButton.setBackground(new Color(255, 165, 0));
            deleteButton.setBackground(new Color(220, 20, 60));

            addButton.setForeground(Color.WHITE);
            updateButton.setForeground(Color.WHITE);
            deleteButton.setForeground(Color.WHITE);

            addButton.setFocusPainted(false);
            updateButton.setFocusPainted(false);
            deleteButton.setFocusPainted(false);

            addButton.addActionListener(e -> addSpeaker());
            updateButton.addActionListener(e -> updateSpeaker());
            deleteButton.addActionListener(e -> deleteSpeaker());

            buttonPanel.add(addButton);
            buttonPanel.add(updateButton);
            buttonPanel.add(deleteButton);
            crudPanel.add(buttonPanel, BorderLayout.SOUTH);
            add(crudPanel, BorderLayout.SOUTH);
        }

        private void populateTable() {
            tableModel.setRowCount(0);
            List<Speaker> speakers = SpeakerDAO.getAllSpeakers();
            for (Speaker speaker : speakers) {
                tableModel.addRow(new Object[]{speaker.speaker_id, speaker.speaker_name, speaker.bio});
            }
        }

        private void clearFields() {
            speakerNameField.setText("");
            bioArea.setText("");
            speakerTable.clearSelection();
            selectedSpeakerId = -1;
        }

        private void addSpeaker() {
            String name = speakerNameField.getText().trim();
            String bio = bioArea.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Speaker Name cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Speaker newSpeaker = new Speaker(0, name, bio);
            if (SpeakerDAO.addSpeaker(newSpeaker)) {
                JOptionPane.showMessageDialog(this, "Speaker added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                populateTable();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add speaker.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void updateSpeaker() {
            if (selectedSpeakerId == -1) {
                JOptionPane.showMessageDialog(this, "Please select a speaker to update.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String name = speakerNameField.getText().trim();
            String bio = bioArea.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Speaker Name cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Speaker updatedSpeaker = new Speaker(selectedSpeakerId, name, bio);
            if (SpeakerDAO.updateSpeaker(updatedSpeaker)) {
                JOptionPane.showMessageDialog(this, "Speaker updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                populateTable();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update speaker.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void deleteSpeaker() {
            if (selectedSpeakerId == -1) {
                JOptionPane.showMessageDialog(this, "Please select a speaker to delete.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this speaker? This might affect associated sessions.", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (SpeakerDAO.deleteSpeaker(selectedSpeakerId)) {
                    JOptionPane.showMessageDialog(this, "Speaker deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    populateTable();
                    clearFields();
                } else {
                    // Error handled by DAO
                }
            }
        }
        @Override
        public void addNotify() {
            super.addNotify();
            populateTable();
            clearFields();
            addButton.setEnabled(true);
            updateButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }

    private class ManageStaffPanel extends JPanel {
        private JTable staffTable;
        private DefaultTableModel tableModel;
        private JTextField staffNameField;
        private JTextField roleField;
        private JTextField contactEmailField;
        private JButton addButton;
        private JButton updateButton;
        private JButton deleteButton;
        private int selectedStaffId = -1;

        public ManageStaffPanel() {
            setLayout(new BorderLayout(10, 10));
            setBackground(new Color(240, 248, 255));

            JLabel title = new JLabel("Manage Staff", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 28));
            title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
            add(title, BorderLayout.NORTH);

            // Table
            String[] columnNames = {"ID", "Staff Name", "Role", "Email"};
            tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            staffTable = new JTable(tableModel);
            staffTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            staffTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && staffTable.getSelectedRow() != -1) {
                    int selectedRow = staffTable.getSelectedRow();
                    selectedStaffId = (int) tableModel.getValueAt(selectedRow, 0);
                    staffNameField.setText((String) tableModel.getValueAt(selectedRow, 1));
                    roleField.setText((String) tableModel.getValueAt(selectedRow, 2));
                    contactEmailField.setText((String) tableModel.getValueAt(selectedRow, 3));
                    addButton.setEnabled(false);
                    updateButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                } else {
                    selectedStaffId = -1;
                    clearFields();
                    addButton.setEnabled(true);
                    updateButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                }
            });
            JScrollPane scrollPane = new JScrollPane(staffTable);
            add(scrollPane, BorderLayout.CENTER);

            // CRUD Panel
            JPanel crudPanel = new JPanel(new BorderLayout(10, 10));
            crudPanel.setBorder(BorderFactory.createTitledBorder("Staff Details"));
            crudPanel.setBackground(new Color(240, 248, 255));

            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(new Color(240, 248, 255));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Staff Name:"), gbc);
            gbc.gridx = 1; staffNameField = new JTextField(25); formPanel.add(staffNameField, gbc);

            gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Role:"), gbc);
            gbc.gridx = 1; roleField = new JTextField(25); formPanel.add(roleField, gbc);

            gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Contact Email:"), gbc);
            gbc.gridx = 1; contactEmailField = new JTextField(25); formPanel.add(contactEmailField, gbc);

            crudPanel.add(formPanel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            buttonPanel.setBackground(new Color(240, 248, 255));

            addButton = new JButton("Add Staff");
            updateButton = new JButton("Update Staff");
            deleteButton = new JButton("Delete Staff");

            addButton.setFont(new Font("Arial", Font.BOLD, 14));
            updateButton.setFont(new Font("Arial", Font.BOLD, 14));
            deleteButton.setFont(new Font("Arial", Font.BOLD, 14));

            addButton.setBackground(new Color(60, 179, 113));
            updateButton.setBackground(new Color(255, 165, 0));
            deleteButton.setBackground(new Color(220, 20, 60));

            addButton.setForeground(Color.WHITE);
            updateButton.setForeground(Color.WHITE);
            deleteButton.setForeground(Color.WHITE);

            addButton.setFocusPainted(false);
            updateButton.setFocusPainted(false);
            deleteButton.setFocusPainted(false);

            addButton.addActionListener(e -> addStaff());
            updateButton.addActionListener(e -> updateStaff());
            deleteButton.addActionListener(e -> deleteStaff());

            buttonPanel.add(addButton);
            buttonPanel.add(updateButton);
            buttonPanel.add(deleteButton);
            crudPanel.add(buttonPanel, BorderLayout.SOUTH);
            add(crudPanel, BorderLayout.SOUTH);
        }

        private void populateTable() {
            tableModel.setRowCount(0);
            List<Staff> staffList = StaffDAO.getAllStaff();
            for (Staff staff : staffList) {
                tableModel.addRow(new Object[]{staff.staff_id, staff.staff_name, staff.role, staff.contact_email});
            }
        }

        private void clearFields() {
            staffNameField.setText("");
            roleField.setText("");
            contactEmailField.setText("");
            staffTable.clearSelection();
            selectedStaffId = -1;
        }

        private void addStaff() {
            String name = staffNameField.getText().trim();
            String role = roleField.getText().trim();
            String email = contactEmailField.getText().trim();

            if (name.isEmpty() || role.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Invalid Email", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Staff newStaff = new Staff(0, name, role, email);
            if (StaffDAO.addStaff(newStaff)) {
                JOptionPane.showMessageDialog(this, "Staff member added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                populateTable();
                clearFields();
            } else {
                // Error handled by DAO
            }
        }

        private void updateStaff() {
            if (selectedStaffId == -1) {
                JOptionPane.showMessageDialog(this, "Please select a staff member to update.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String name = staffNameField.getText().trim();
            String role = roleField.getText().trim();
            String email = contactEmailField.getText().trim();

            if (name.isEmpty() || role.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required for update.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Invalid Email", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Staff updatedStaff = new Staff(selectedStaffId, name, role, email);
            if (StaffDAO.updateStaff(updatedStaff)) {
                JOptionPane.showMessageDialog(this, "Staff member updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                populateTable();
                clearFields();
            } else {
                // Error handled by DAO
            }
        }

        private void deleteStaff() {
            if (selectedStaffId == -1) {
                JOptionPane.showMessageDialog(this, "Please select a staff member to delete.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this staff member? This might affect assigned tasks.", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (StaffDAO.deleteStaff(selectedStaffId)) {
                    JOptionPane.showMessageDialog(this, "Staff member deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    populateTable();
                    clearFields();
                } else {
                    // Error handled by DAO
                }
            }
        }
        @Override
        public void addNotify() {
            super.addNotify();
            populateTable();
            clearFields();
            addButton.setEnabled(true);
            updateButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }

    private class ManageSessionsPanel extends JPanel {
        private JTable sessionTable;
        private DefaultTableModel tableModel;
        private JTextField sessionTitleField;
        private JComboBox<Event> eventComboBox;
        private JComboBox<Speaker> speakerComboBox; // Nullable
        private JTextField startTimeField; // HH:MM:SS
        private JTextField endTimeField;   // HH:MM:SS
        private JButton addButton;
        private JButton updateButton;
        private JButton deleteButton;
        private int selectedSessionId = -1;

        public ManageSessionsPanel() {
            setLayout(new BorderLayout(10, 10));
            setBackground(new Color(240, 248, 255));

            JLabel title = new JLabel("Manage Sessions", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 28));
            title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
            add(title, BorderLayout.NORTH);

            // Table
            String[] columnNames = {"ID", "Title", "Event", "Speaker", "Start Time", "End Time"};
            tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            sessionTable = new JTable(tableModel);
            sessionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            sessionTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && sessionTable.getSelectedRow() != -1) {
                    int selectedRow = sessionTable.getSelectedRow();
                    selectedSessionId = (int) tableModel.getValueAt(selectedRow, 0);
                    sessionTitleField.setText((String) tableModel.getValueAt(selectedRow, 1));
                    String eventName = (String) tableModel.getValueAt(selectedRow, 2);
                    String speakerName = (String) tableModel.getValueAt(selectedRow, 3);
                    startTimeField.setText(tableModel.getValueAt(selectedRow, 4) != null ? tableModel.getValueAt(selectedRow, 4).toString() : "");
                    endTimeField.setText(tableModel.getValueAt(selectedRow, 5) != null ? tableModel.getValueAt(selectedRow, 5).toString() : "");

                    // Set Event ComboBox
                    for (int i = 0; i < eventComboBox.getItemCount(); i++) {
                        if (eventComboBox.getItemAt(i).event_name.equals(eventName)) {
                            eventComboBox.setSelectedIndex(i);
                            break;
                        }
                    }
                    // Set Speaker ComboBox (handle nullable)
                    if (speakerName != null && !speakerName.isEmpty()) {
                        for (int i = 0; i < speakerComboBox.getItemCount(); i++) {
                            if (speakerComboBox.getItemAt(i).speaker_name.equals(speakerName)) {
                                speakerComboBox.setSelectedIndex(i);
                                break;
                            }
                        }
                    } else {
                        speakerComboBox.setSelectedItem(null); // Explicitly set to null for 'no speaker'
                    }

                    addButton.setEnabled(false);
                    updateButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                } else {
                    selectedSessionId = -1;
                    clearFields();
                    addButton.setEnabled(true);
                    updateButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                }
            });
            JScrollPane scrollPane = new JScrollPane(sessionTable);
            add(scrollPane, BorderLayout.CENTER);

            // CRUD Panel
            JPanel crudPanel = new JPanel(new BorderLayout(10, 10));
            crudPanel.setBorder(BorderFactory.createTitledBorder("Session Details"));
            crudPanel.setBackground(new Color(240, 248, 255));

            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(new Color(240, 248, 255));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Session Title:"), gbc);
            gbc.gridx = 1; sessionTitleField = new JTextField(25); formPanel.add(sessionTitleField, gbc);

            gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Event:"), gbc);
            gbc.gridx = 1; eventComboBox = new JComboBox<>(); populateEvents(); formPanel.add(eventComboBox, gbc);

            gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Speaker:"), gbc);
            gbc.gridx = 1; speakerComboBox = new JComboBox<>(); populateSpeakers(); formPanel.add(speakerComboBox, gbc);
            speakerComboBox.setRenderer(new DefaultListCellRenderer() { // Allow null/empty string as selection
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value == null) {
                        setText("-- Select Speaker (Optional) --");
                    }
                    return this;
                }
            });


            gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Start Time (HH:MM:SS):"), gbc);
            gbc.gridx = 1; startTimeField = new JTextField(10); formPanel.add(startTimeField, gbc);

            gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("End Time (HH:MM:SS):"), gbc);
            gbc.gridx = 1; endTimeField = new JTextField(10); formPanel.add(endTimeField, gbc);

            crudPanel.add(formPanel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            buttonPanel.setBackground(new Color(240, 248, 255));

            addButton = new JButton("Add Session");
            updateButton = new JButton("Update Session");
            deleteButton = new JButton("Delete Session");

            addButton.setFont(new Font("Arial", Font.BOLD, 14));
            updateButton.setFont(new Font("Arial", Font.BOLD, 14));
            deleteButton.setFont(new Font("Arial", Font.BOLD, 14));

            addButton.setBackground(new Color(60, 179, 113));
            updateButton.setBackground(new Color(255, 165, 0));
            deleteButton.setBackground(new Color(220, 20, 60));

            addButton.setForeground(Color.WHITE);
            updateButton.setForeground(Color.WHITE);
            deleteButton.setForeground(Color.WHITE);

            addButton.setFocusPainted(false);
            updateButton.setFocusPainted(false);
            deleteButton.setFocusPainted(false);

            addButton.addActionListener(e -> addSession());
            updateButton.addActionListener(e -> updateSession());
            deleteButton.addActionListener(e -> deleteSession());

            buttonPanel.add(addButton);
            buttonPanel.add(updateButton);
            buttonPanel.add(deleteButton);
            crudPanel.add(buttonPanel, BorderLayout.SOUTH);
            add(crudPanel, BorderLayout.SOUTH);
        }

        private void populateEvents() {
            eventComboBox.removeAllItems();
            List<Event> events = EventDAO.getAllEvents();
            for (Event event : events) {
                eventComboBox.addItem(event);
            }
        }

        private void populateSpeakers() {
            speakerComboBox.removeAllItems();
            speakerComboBox.addItem(null); // Option for no speaker
            List<Speaker> speakers = SpeakerDAO.getAllSpeakers();
            for (Speaker speaker : speakers) {
                speakerComboBox.addItem(speaker);
            }
        }

        private void populateTable() {
            tableModel.setRowCount(0);
            List<Session> sessions = SessionDAO.getAllSessions();
            for (Session session : sessions) {
                tableModel.addRow(new Object[]{
                    session.session_id,
                    session.session_title,
                    session.event_name,
                    session.speaker_name,
                    session.start_time,
                    session.end_time
                });
            }
        }

        private void clearFields() {
            sessionTitleField.setText("");
            eventComboBox.setSelectedIndex(-1);
            speakerComboBox.setSelectedItem(null); // Set to the null option
            startTimeField.setText("");
            endTimeField.setText("");
            sessionTable.clearSelection();
            selectedSessionId = -1;
        }

        private void addSession() {
            String title = sessionTitleField.getText().trim();
            Event selectedEvent = (Event) eventComboBox.getSelectedItem();
            Speaker selectedSpeaker = (Speaker) speakerComboBox.getSelectedItem(); // Can be null
            String startTimeStr = startTimeField.getText().trim();
            String endTimeStr = endTimeField.getText().trim();

            if (title.isEmpty() || selectedEvent == null || startTimeStr.isEmpty() || endTimeStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Session Title, Event, Start Time, and End Time are required.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                LocalTime startTime = LocalTime.parse(startTimeStr);
                LocalTime endTime = LocalTime.parse(endTimeStr);

                int speakerId = (selectedSpeaker != null) ? selectedSpeaker.speaker_id : 0; // 0 for null speaker_id

                Session newSession = new Session(0, title, selectedEvent.event_id, speakerId, startTime, endTime, null, null);
                if (SessionDAO.addSession(newSession)) {
                    JOptionPane.showMessageDialog(this, "Session added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    populateTable();
                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add session.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid time format. Please use HH:MM:SS.", "Time Format Error", JOptionPane.WARNING_MESSAGE);
            }
        }

        private void updateSession() {
            if (selectedSessionId == -1) {
                JOptionPane.showMessageDialog(this, "Please select a session to update.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String title = sessionTitleField.getText().trim();
            Event selectedEvent = (Event) eventComboBox.getSelectedItem();
            Speaker selectedSpeaker = (Speaker) speakerComboBox.getSelectedItem();
            String startTimeStr = startTimeField.getText().trim();
            String endTimeStr = endTimeField.getText().trim();

            if (title.isEmpty() || selectedEvent == null || startTimeStr.isEmpty() || endTimeStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Session Title, Event, Start Time, and End Time are required for update.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                LocalTime startTime = LocalTime.parse(startTimeStr);
                LocalTime endTime = LocalTime.parse(endTimeStr);

                int speakerId = (selectedSpeaker != null) ? selectedSpeaker.speaker_id : 0;

                Session updatedSession = new Session(selectedSessionId, title, selectedEvent.event_id, speakerId, startTime, endTime, null, null);
                if (SessionDAO.updateSession(updatedSession)) {
                    JOptionPane.showMessageDialog(this, "Session updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    populateTable();
                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update session.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid time format. Please use HH:MM:SS.", "Time Format Error", JOptionPane.WARNING_MESSAGE);
            }
        }

        private void deleteSession() {
            if (selectedSessionId == -1) {
                JOptionPane.showMessageDialog(this, "Please select a session to delete.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this session?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (SessionDAO.deleteSession(selectedSessionId)) {
                    JOptionPane.showMessageDialog(this, "Session deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    populateTable();
                    clearFields();
                } else {
                    // Error handled by DAO
                }
            }
        }
        @Override
        public void addNotify() {
            super.addNotify();
            populateEvents();
            populateSpeakers();
            populateTable();
            clearFields();
            addButton.setEnabled(true);
            updateButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }
}
