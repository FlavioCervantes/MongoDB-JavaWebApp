package application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import view.*;
/*
 * Controller class for patient interactions.
 *   register as a new patient.
 *   update patient profile.
 */
@Controller
public class ControllerPatientUpdate {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/*
	 *  Display patient profile for patient id.
	 */
		// TODO search for patient by id
		//  if not found, return to home page using return "index";
		//  else create PatientView and add to model.
	@GetMapping("/patient/edit/{id}")
	public String getUpdateForm(@PathVariable int id, Model model) {
		try {
			String query = "SELECT p.id, p.first_name, p.last_name, p.birth_date, p.ssn, p.street, p.city, p.state, p.zip, d.last_name AS primary_last_name " +
					"FROM patient p JOIN doctor d ON p.doctor_id = d.id WHERE p.id = ?";

			PatientView patient = jdbcTemplate.queryForObject(query, new Object[]{id}, (rs, rowNum) -> {
				PatientView pv = new PatientView();
				pv.setId(rs.getInt("id"));
				pv.setFirst_name(rs.getString("first_name"));
				pv.setLast_name(rs.getString("last_name"));
				pv.setBirthdate(rs.getDate("birth_date").toLocalDate().toString());
				pv.setStreet(rs.getString("street"));
				pv.setCity(rs.getString("city"));
				pv.setState(rs.getString("state"));
				pv.setZipcode(rs.getString("zip"));
				pv.setPrimaryName(rs.getString("primary_last_name"));
				return pv;
			});

			model.addAttribute("patient", patient);
			return "patient_edit";  // return editable form with patient data
		}

		catch (Exception e) {
			// patient not found, redirect to home page
			model.addAttribute("message", "Patient not found");
			return "index";  // return to home page
		}
}
	
	
	/*
	 * Process changes from patient_edit form
	 *  Primary doctor, street, city, state, zip can be changed
	 *  ssn, patient id, name, birthdate, ssn are read only in template.
	 */
	// TODO
	// TODO update patient profile data in database
	@PostMapping("/patient/edit")
	public String updatePatient(PatientView p, Model model) {
		try {
			// validate doctor last name
			String doctorQuery = "SELECT id FROM doctor WHERE last_name = ?";
			Integer doctorId = jdbcTemplate.queryForObject(doctorQuery, new Object[]{p.getPrimaryName()}, Integer.class);

			if (doctorId != null) {
				model.addAttribute("message", "Doctor not found. Check the last name.");
				model.addAttribute("patient", p);
				return "patient_edit";
			}

			// update patient field
			String updateQuery = "UPDATE patient SET doctor_id = ?, street = ?, city = ?, state = ?, zip = ? WHERE id = ?";
			jdbcTemplate.update(updateQuery, p.getStreet(), p.getCity(), p.getState(), p.getZipcode(), doctorId, p.getId());

			model.addAttribute("message", "Patient profile updated successfully.");
			model.addAttribute("patient", p);
			return "patient_show";  // return to patient profile view
		}
		catch (Exception e) {
			// return to edit form with error message
			model.addAttribute("message", "Error updating patient profile: " + e.getMessage());
			model.addAttribute("patient", p);
			return "patient_edit";
		}
	}
}
