
package application;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class Main extends Application {

    // Database configuration
    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/hospital_db";

    private static final String DB_USER = "root";

    private static final String DB_PASSWORD =
            System.getenv("HOSPITAL_DB_PASSWORD");

    // Patient form fields
    private final TextField patientIdField = new TextField();
    private final TextField nameField = new TextField();
    private final TextField ageField = new TextField();
    private final ComboBox<String> genderBox = new ComboBox<>();
    private final TextField phoneField = new TextField();
    private final TextField diagnosisField = new TextField();
    private final DatePicker admissionDatePicker = new DatePicker();

    // Patient records
    private final TextField searchField = new TextField();
    private final TableView<Patient> patientTable = new TableView<>();

    private final Label statusLabel = new Label("Ready.");

    // JDBC connection
    private Connection getConnection() throws SQLException {
        if (DB_PASSWORD == null) {
            throw new SQLException(
                "Set the HOSPITAL_DB_PASSWORD environment variable."
            );
        }

        return DriverManager.getConnection(
                DB_URL, DB_USER, DB_PASSWORD
        );
    }

    @Override
    public void start(Stage stage) {

        // 1. MAIN: BorderPane
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f7fa;");

        // 2. TOP: Application heading
        Label title = new Label("Hospital Management System");
        title.setStyle(
            "-fx-font-size: 26px; -fx-font-weight: bold;"
        );

        Label subtitle = new Label(
            "Patient Registration & Records"
        );

        VBox header = new VBox(5, title, subtitle);
        header.setPadding(new Insets(0, 0, 20, 0));
        root.setTop(header);

        // 3. LEFT: Patient Details form
        Label formTitle = new Label("Patient Details");
        formTitle.setStyle(
            "-fx-font-size: 18px; -fx-font-weight: bold;"
        );

        patientIdField.setPromptText("Auto-generated");
        patientIdField.setEditable(false);

        nameField.setPromptText("Enter full name");
        ageField.setPromptText("Enter age");
        phoneField.setPromptText("Enter phone number");
        diagnosisField.setPromptText("Reason for visit");

        genderBox.getItems().addAll(
            "Female",
            "Male",
            "Non-binary",
            "Prefer not to say"
        );
        genderBox.setPromptText("Select gender");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(14);
        form.setPadding(new Insets(10));

        addRow(form, 0, "Patient ID:", patientIdField);
        addRow(form, 1, "Full Name:", nameField);
        addRow(form, 2, "Age:", ageField);
        addRow(form, 3, "Gender:", genderBox);
        addRow(form, 4, "Phone:", phoneField);
        addRow(form, 5, "Diagnosis:", diagnosisField);
        addRow(form, 6, "Admission Date:", admissionDatePicker);

        ColumnConstraints labels = new ColumnConstraints(115);
        ColumnConstraints fields = new ColumnConstraints();
        fields.setHgrow(Priority.ALWAYS);

        form.getColumnConstraints().addAll(labels, fields);

        // 4. Form buttons
        Button addButton = new Button("Add Patient");
        Button updateButton = new Button("Update");
        Button deleteButton = new Button("Delete");
        Button clearButton = new Button("Clear");

        HBox firstRow = new HBox(
            10, addButton, updateButton
        );

        HBox secondRow = new HBox(
            10, deleteButton, clearButton
        );

        VBox buttonArea = new VBox(
            10, firstRow, secondRow
        );

        VBox patientForm = new VBox(
            15, formTitle, form, buttonArea
        );

        patientForm.setPrefWidth(420);
        patientForm.setPadding(new Insets(15));
        patientForm.setStyle("-fx-background-color: white;");

        // 5. RIGHT: Patient Records
        Label tableTitle = new Label("Patient Records");
        tableTitle.setStyle(
            "-fx-font-size: 18px; -fx-font-weight: bold;"
        );

        searchField.setPromptText(
            "Search by name or phone"
        );

        Button searchButton = new Button("Search");
        Button showAllButton = new Button("Show All");

        HBox searchArea = new HBox(
            10, searchField, searchButton, showAllButton
        );

        HBox.setHgrow(searchField, Priority.ALWAYS);

        // 6. TableView columns
        TableColumn<Patient, Number> idColumn =
            new TableColumn<>("ID");

        TableColumn<Patient, String> nameColumn =
            new TableColumn<>("Name");

        TableColumn<Patient, Number> ageColumn =
            new TableColumn<>("Age");

        TableColumn<Patient, String> genderColumn =
            new TableColumn<>("Gender");

        TableColumn<Patient, String> phoneColumn =
            new TableColumn<>("Phone");

        TableColumn<Patient, String> diagnosisColumn =
            new TableColumn<>("Diagnosis");

        TableColumn<Patient, String> dateColumn =
            new TableColumn<>("Admission Date");

        idColumn.setCellValueFactory(data ->
            new SimpleIntegerProperty(
                data.getValue().getId()
            )
        );

        nameColumn.setCellValueFactory(data ->
            new SimpleStringProperty(
                data.getValue().getName()
            )
        );

        ageColumn.setCellValueFactory(data ->
            new SimpleIntegerProperty(
                data.getValue().getAge()
            )
        );

        genderColumn.setCellValueFactory(data ->
            new SimpleStringProperty(
                data.getValue().getGender()
            )
        );

        phoneColumn.setCellValueFactory(data ->
            new SimpleStringProperty(
                data.getValue().getPhone()
            )
        );

        diagnosisColumn.setCellValueFactory(data ->
            new SimpleStringProperty(
                data.getValue().getDiagnosis()
            )
        );

        dateColumn.setCellValueFactory(data ->
            new SimpleStringProperty(
                data.getValue().getAdmissionDate().toString()
            )
        );

        patientTable.getColumns().addAll(
            idColumn,
            nameColumn,
            ageColumn,
            genderColumn,
            phoneColumn,
            diagnosisColumn,
            dateColumn
        );

        patientTable.setColumnResizePolicy(
            TableView.CONSTRAINED_RESIZE_POLICY
        );

        // Selecting a row fills the patient form
        patientTable.getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, oldPatient, selectedPatient) -> {

                if (selectedPatient != null) {
                    fillForm(selectedPatient);
                }
            });

        VBox recordsArea = new VBox(
            15, tableTitle, searchArea, patientTable
        );

        recordsArea.setPadding(new Insets(15));
        recordsArea.setStyle(
            "-fx-background-color: white;"
        );

        HBox.setHgrow(recordsArea, Priority.ALWAYS);
        VBox.setVgrow(patientTable, Priority.ALWAYS);

        // 7. CENTER: Form and TableView
        HBox center = new HBox(
            20, patientForm, recordsArea
        );

        root.setCenter(center);

        // 8. BOTTOM: Status
        statusLabel.setPadding(
            new Insets(15, 0, 0, 0)
        );

        root.setBottom(statusLabel);

        // 9. Button events
        addButton.setOnAction(e -> addPatient());

        updateButton.setOnAction(e -> updatePatient());

        deleteButton.setOnAction(e -> deletePatient());

        clearButton.setOnAction(e -> clearForm());

        searchButton.setOnAction(e -> searchPatients());

        showAllButton.setOnAction(e -> loadPatients());

        // 10. Window
        Scene scene = new Scene(root, 1200, 650);

        stage.setTitle("Hospital Management System");
        stage.setScene(scene);
        stage.show();

        // Load existing patients when application starts
        loadPatients();
    }

    // Add labels and controls to GridPane
    private void addRow(
        GridPane grid,
        int row,
        String labelText,
        Control control
    ) {

        Label label = new Label(labelText);
        control.setMaxWidth(Double.MAX_VALUE);

        grid.add(label, 0, row);
        grid.add(control, 1, row);
    }

    // Validate patient form
    private boolean validateForm() {

        String name = nameField.getText().trim();
        String ageText = ageField.getText().trim();
        String phone = phoneField.getText().trim();
        String diagnosis = diagnosisField.getText().trim();

        if (name.isEmpty()
                || ageText.isEmpty()
                || genderBox.getValue() == null
                || phone.isEmpty()
                || diagnosis.isEmpty()
                || admissionDatePicker.getValue() == null) {

            setStatus("Please complete all required fields.", true);
            return false;
        }

        int age;

        try {
            age = Integer.parseInt(ageText);
        } catch (NumberFormatException ex) {
            setStatus("Age must be a whole number.", true);
            return false;
        }

        if (age < 0 || age > 125) {
            setStatus("Age must be between 0 and 125.", true);
            return false;
        }

        // Basic phone number validation
        String digits = phone.replaceAll("\\D", "");

        if (!phone.matches("[+0-9() .-]+")
                || digits.length() < 7
                || digits.length() > 15) {

            setStatus("Enter a valid phone number.", true);
            return false;
        }

        LocalDate admissionDate =
            admissionDatePicker.getValue();

        if (admissionDate.isAfter(LocalDate.now())) {
            setStatus(
                "Admission date cannot be in the future.",
                true
            );
            return false;
        }

        return true;
    }

    // CREATE: Add Patient
    private void addPatient() {

        if (!validateForm()) {
            return;
        }

        String sql =
            "INSERT INTO patients " +
            "(name, age, gender, phone, diagnosis, admission_date) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt =
                 conn.prepareStatement(sql)) {

            stmt.setString(1, nameField.getText().trim());

            stmt.setInt(
                2,
                Integer.parseInt(ageField.getText().trim())
            );

            stmt.setString(3, genderBox.getValue());
            stmt.setString(4, phoneField.getText().trim());
            stmt.setString(5, diagnosisField.getText().trim());

            stmt.setDate(
                6,
                Date.valueOf(admissionDatePicker.getValue())
            );

            int rows = stmt.executeUpdate();

            if (rows == 1) {
                clearFields();
                loadPatients();
                setStatus(
                    "Patient added successfully!", false
                );
            } else {
                setStatus("No patient was added.", true);
            }

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    // READ: Show All Patients
    private void loadPatients() {

        String sql =
            "SELECT * FROM patients ORDER BY patient_id";

        ObservableList<Patient> patients =
            FXCollections.observableArrayList();

        try (Connection conn = getConnection();
             PreparedStatement stmt =
                 conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Patient patient = new Patient(
                    rs.getInt("patient_id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("gender"),
                    rs.getString("phone"),
                    rs.getString("diagnosis"),
                    rs.getDate("admission_date").toLocalDate()
                );

                patients.add(patient);
            }

            patientTable.setItems(patients);
            clearFields();

            setStatus(
                "Loaded " + patients.size() + " patient(s).",
                false
            );

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    // Populate form from selected patient
    private void fillForm(Patient patient) {

        patientIdField.setText(
            String.valueOf(patient.getId())
        );

        nameField.setText(patient.getName());

        ageField.setText(
            String.valueOf(patient.getAge())
        );

        genderBox.setValue(patient.getGender());
        phoneField.setText(patient.getPhone());
        diagnosisField.setText(patient.getDiagnosis());

        admissionDatePicker.setValue(
            patient.getAdmissionDate()
        );
    }

    // UPDATE: Edit Patient
    private void updatePatient() {

        Patient selected =
            patientTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            setStatus("Please select a patient to update.", true);
            return;
        }

        if (!validateForm()) {
            return;
        }

        String sql =
            "UPDATE patients SET " +
            "name = ?, age = ?, gender = ?, phone = ?, " +
            "diagnosis = ?, admission_date = ? " +
            "WHERE patient_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt =
                 conn.prepareStatement(sql)) {

            stmt.setString(1, nameField.getText().trim());

            stmt.setInt(
                2,
                Integer.parseInt(ageField.getText().trim())
            );

            stmt.setString(3, genderBox.getValue());
            stmt.setString(4, phoneField.getText().trim());
            stmt.setString(5, diagnosisField.getText().trim());

            stmt.setDate(
                6,
                Date.valueOf(admissionDatePicker.getValue())
            );

            stmt.setInt(7, selected.getId());

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                loadPatients();
                setStatus(
                    "Patient updated successfully!", false
                );
            } else {
                setStatus("Patient was not found.", true);
            }

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    // DELETE: Remove Patient
    private void deletePatient() {

        Patient selected =
            patientTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            setStatus("Please select a patient to delete.", true);
            return;
        }

        Alert confirmation = new Alert(
            Alert.AlertType.CONFIRMATION
        );

        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete selected patient?");
        confirmation.setContentText(
            "Are you sure you want to delete "
            + selected.getName() + "?"
        );

        Optional<ButtonType> result =
            confirmation.showAndWait();

        if (result.isEmpty()
                || result.get() != ButtonType.OK) {
            setStatus("Delete cancelled.", false);
            return;
        }

        String sql =
            "DELETE FROM patients WHERE patient_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt =
                 conn.prepareStatement(sql)) {

            stmt.setInt(1, selected.getId());

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                loadPatients();
                setStatus(
                    "Patient deleted successfully!", false
                );
            } else {
                setStatus("Patient was not found.", true);
            }

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    // SEARCH: Find by name or phone
    private void searchPatients() {

        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            loadPatients();
            return;
        }

        String sql =
            "SELECT * FROM patients " +
            "WHERE name LIKE ? OR phone LIKE ? " +
            "ORDER BY patient_id";

        ObservableList<Patient> results =
            FXCollections.observableArrayList();

        try (Connection conn = getConnection();
             PreparedStatement stmt =
                 conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Patient patient = new Patient(
                        rs.getInt("patient_id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("phone"),
                        rs.getString("diagnosis"),
                        rs.getDate("admission_date").toLocalDate()
                    );

                    results.add(patient);
                }
            }

            patientTable.setItems(results);
            clearFields();

            setStatus(
                "Found " + results.size() + " patient(s).",
                false
            );

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    // CLEAR: Reset form and selection
    private void clearForm() {

        clearFields();
        setStatus("Form cleared.", false);
    }

    private void clearFields() {

        patientTable.getSelectionModel().clearSelection();

        patientIdField.clear();
        nameField.clear();
        ageField.clear();
        genderBox.getSelectionModel().clearSelection();
        phoneField.clear();
        diagnosisField.clear();
        admissionDatePicker.setValue(null);
    }

    // Display success or error message
    private void setStatus(String message, boolean error) {

        statusLabel.setText(message);

        statusLabel.setStyle(
            error
                ? "-fx-text-fill: #b00020;"
                : "-fx-text-fill: #167545;"
        );
    }

    // Handle database errors
    private void showDatabaseError(SQLException ex) {

        setStatus("Database error: " + ex.getMessage(), true);
        ex.printStackTrace();
    }

    // Patient model for TableView
    public static class Patient {

        private final int id;
        private final String name;
        private final int age;
        private final String gender;
        private final String phone;
        private final String diagnosis;
        private final LocalDate admissionDate;

        public Patient(
            int id,
            String name,
            int age,
            String gender,
            String phone,
            String diagnosis,
            LocalDate admissionDate
        ) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.gender = gender;
            this.phone = phone;
            this.diagnosis = diagnosis;
            this.admissionDate = admissionDate;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public String getGender() {
            return gender;
        }

        public String getPhone() {
            return phone;
        }

        public String getDiagnosis() {
            return diagnosis;
        }

        public LocalDate getAdmissionDate() {
            return admissionDate;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
