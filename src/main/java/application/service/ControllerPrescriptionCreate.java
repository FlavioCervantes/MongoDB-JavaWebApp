package application.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import view.*;

@Controller
public class ControllerPrescriptionCreate {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	/*
	 * Doctor requests blank form for new prescription.
	 */
	@GetMapping("/prescription/new")
	public String getPrescriptionForm(Model model) {
		model.addAttribute("prescription", new PrescriptionView());
		return "prescription_create";
	}

	// process data entered on prescription_create form
	@PostMapping("/prescription")
	public String createPrescription(PrescriptionView p, Model model) {

		System.out.println("createPrescription " + p);

		/*
		 * valid doctor name and id
		 */
		//TODO

		try (Connection con = getConnection()) {
			// Obtain patient_id
			var psPatient = con.prepareStatement("SELECT id FROM patient WHERE first_name=? AND last_name=?");
			psPatient.setString(1, p.getPatientFirstName());
			psPatient.setString(2, p.getPatientLastName());
			var rsPatient = psPatient.executeQuery();
			if (rsPatient.next()) {
				p.setPatient_id(rsPatient.getInt("id"));
			} else {
				model.addAttribute("message", "Patient not found.");
				model.addAttribute("prescription", p);
				return "prescription_create";
			}



			//obtain doctor name and id from session

			// Obtain doctor_id
			var psDoctor = con.prepareStatement("SELECT id FROM doctor WHERE first_name=? AND last_name=?");
			psDoctor.setString(1, p.getDoctorFirstName());
			psDoctor.setString(2, p.getDoctorLastName());
			var rsDoctor = psDoctor.executeQuery();
			if (rsDoctor.next()) {
				p.setDoctor_id(rsDoctor.getInt("id"));
			} else {
				model.addAttribute("message", "Doctor not found.");
				model.addAttribute("prescription", p);
				return "prescription_create";
			}

			/*
			 * validate patient name and id
			 */
			//TODO
			// commenting out because this is overriting the ID being set from the database (it is autoincremented)
//			p.setPatient_id(new Random().nextInt(1000));
			p.setPatientFirstName(p.getPatientFirstName());
			p.setPatientLastName(p.getPatientLastName());

			/*
			 * validate drug name
			 */
			//TODO

			if (p.getDrugName() == null || p.getDrugName().isEmpty()) {
				model.addAttribute("message", "Invalid drug name.");
				model.addAttribute("prescription", p);
				return "prescription_create";
			}

			var psDrug = con.prepareStatement("SELECT id FROM drug WHERE name = ?");
			psDrug.setString(1, p.getDrugName());
			var rsDrug = psDrug.executeQuery();
			int drugId;

			if (rsDrug.next()) {
				drugId = rsDrug.getInt("id");
			} else {
				model.addAttribute("message", "Drug not found.");
				model.addAttribute("prescription", p);
				return "prescription_create";
			}

			/*
			 * insert prescription
			 */
			//TODO


			var psPrescription = con.prepareStatement(
					"INSERT INTO prescription (patient_id, doctor_id, drug_id, date_prescribed, quantity, refill) VALUES (?, ?, ?, ?, ?, ?)",
					new String[] { "RXID" }
			);

			psPrescription.setInt(1, p.getPatient_id());
			psPrescription.setInt(2, p.getDoctor_id());
			psPrescription.setInt(3, drugId);
			psPrescription.setObject(4, LocalDate.now());
			psPrescription.setInt(5, p.getQuantity());
			psPrescription.setInt(6, p.getRefills());
			psPrescription.executeUpdate();



			// Retrieve generated rxid
			var rsPrescription = psPrescription.getGeneratedKeys();
			if (rsPrescription.next()) {
				p.setRxid(rsPrescription.getInt(1));
			}

			model.addAttribute("message", "Prescription created.");
			model.addAttribute("prescription", p);
			return "prescription_show";

		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error: " + e.getMessage());
			model.addAttribute("prescription", p);
			return "prescription_create";
		}
	}

	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

}