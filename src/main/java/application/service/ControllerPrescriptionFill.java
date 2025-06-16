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
public class ControllerPrescriptionFill {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	/*
	 * Patient requests form to fill prescription.
	 */
	@GetMapping("/prescription/fill")
	public String getfillForm(Model model) {
		model.addAttribute("prescription", new PrescriptionView());
		return "prescription_fill";
	}

	// process data from prescription_fill form
	@PostMapping("/prescription/fill")
	public String processFillForm(PrescriptionView p, Model model)
	{

		/*
		 * valid pharmacy name and address, get pharmacy id and phone
		 */
		// TODO

		//validate pharmacy name and address

		if (p.getPharmacyName() == null || p.getPharmacyName().isEmpty()
				||
				p.getPharmacyAddress() == null || p.getPharmacyAddress()
				.isEmpty()) {
			model.addAttribute("message",
					"Invalid pharmacy name or address.");
			model.addAttribute("prescription", p);
			return "prescription_fill";
		}

		int pharmacyId = -1;

		try (Connection con = getConnection()) {
			var ps = con.prepareStatement(
					"SELECT id FROM pharmacy WHERE name=? AND address=?");
			ps.setString(1, p.getPharmacyName());
			ps.setString(2, p.getPharmacyAddress());
			var rs = ps.executeQuery();

			if (rs.next()) {
				pharmacyId = rs.getInt("id");
			}
			else {
				model.addAttribute("message", "Invalid pharmacy name.");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}

			// validate RxID
			var psPrescription = con.prepareStatement(
					"SELECT pr.refill, COUNT(r.id) AS refill_count, pt.last_name AS last_name " +
							"FROM prescription pr " +
							"JOIN patient pt ON pr.patient_id = pt.id " +
							"LEFT JOIN refill r ON r.RXID = pr.RXID " +
							"WHERE pr.RXID = ? " +
							"GROUP BY pr.refill, pt.last_name"
			);

			psPrescription.setInt(1, p.getRxid());
			var rsPrescription = psPrescription.executeQuery();

			// TODO: figure out why this does not say invalid ID
			if (!rsPrescription.next()) {
				model.addAttribute("message", "Invalid RxID");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}

			int allowedRefills = rsPrescription.getInt("refill");
			int fillCount = rsPrescription.getInt("refill_count");
			String actualLastName = rsPrescription.getString(
					"last_name");

			if (!p.getPatientLastName().equalsIgnoreCase(actualLastName)) {
				model.addAttribute("message",
						"Patient last name does not match the prescription.");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}

			if (fillCount >= allowedRefills) {
				model.addAttribute("message",
						"No refills left for this prescription.");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}

			// check doctor info
			if (p.getDoctorFirstName() == null
					|| p.getDoctorLastName() == null || p.getDoctor_id() <= 0) {
				model.addAttribute("message", "Invalid doctor information.");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}

			// cost
			double cost = p.getQuantity() * Double.parseDouble(p.getCost());
			if (cost <= 0) {
				model.addAttribute("message", "Invalid cost.");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}

			// Insert new refill record
			var psInsert = con.prepareStatement(
					"INSERT INTO refill (RXID, pharmacy_id, fill_date, quantity_dispensed, price_charged) VALUES (?, ?, ?, ?, ?)"
			);
			psInsert.setInt(1, p.getRxid());
			psInsert.setInt(2, pharmacyId);
			psInsert.setObject(3, LocalDate.now());
			psInsert.setInt(4, p.getQuantity());
			psInsert.setDouble(5, cost);
			psInsert.executeUpdate();

			model.addAttribute("message", "Prescription filled.");
			model.addAttribute("prescription", p);
			return "prescription_show";
		}
		catch (SQLException e) {
			model.addAttribute("message", "SQL Error: " + e.getMessage());
			model.addAttribute("prescription", p);
			return "prescription_fill";
		}
	}
//
//		// TODO find the patient information
//
//		//validate the rxid value entered on the fill form and veriy patient last nm.
//		//ensure it matches ptn. last nm. for prescription
//		if (p.getRxid() <= 0 || !p.getPatientLastName().equalsIgnoreCase(getPatientLastNameFromPrescription(p.getRxid()))) {
//			model.addAttribute("message", "Invalid Rxid or patient last name does not match.");
//			return "prescription_fill";
//		}
//
//		// TODO find the prescription
//
//		//Validate that the number of refills has not been exceeded for the prescription.
//
//		int maxRefills = p.getRefillCount() + 1; // Total allowed fills
//		if (p.getFillCount() >= maxRefills) {
//			model.addAttribute("message", "No refills left for this prescription.");
//			return "prescription_show";
//		}
//		/*
//		 * have we exceeded the number of allowed refills
//		 * the first fill is not considered a refill.
//		 */
//
//		// TODO
//		/*
//		 * get doctor information
//		 */
//		// TODO
//		if (p.getDoctorFirstName() == null || p.getDoctorLastName() == null || p.getDoctor_id() <= 0) {
//			model.addAttribute("message", "Invalid doctor information.");
//			return "prescription_fill";
//		}
//
//		/*
//		 * calculate cost of prescription
//		 */
//		// TODO
//		double cost = p.getQuantity() * Double.parseDouble(p.getCost());
//		if (cost <= 0) {
//			model.addAttribute("message", "Invalid cost for the prescription.");
//			return "prescription_fill";
//		}
//
//		// TODO save updated prescription
//
//		try (Connection con = getConnection();) {
//			String sql = "UPDATE prescription SET pharmacy_id=?, pharmacy_name=?, pharmacy_address=?, fill_count=fill_count+1, date_filled=?, cost=? WHERE id=?";
//			var ps = con.prepareStatement(sql);
//			ps.setInt(1, p.getPharmacyID());
//			ps.setString(2, p.getPharmacyName());
//			ps.setString(3, p.getPharmacyAddress());
//			ps.setObject(4, LocalDate.now());
//			ps.setDouble(5, cost);
//			ps.setInt(6, p.getRxid());
//			ps.executeUpdate();
//		} catch (SQLException e) {
//			model.addAttribute("message", "SQL Error: " + e.getMessage());
//			return "prescription_fill";
//		}
//
//		// show the updated prescription with the most recent fill information
//		model.addAttribute("message", "Prescription filled.");
//		model.addAttribute("prescription", p);
//		return "prescription_show";
//	}

//	private String getPatientLastNameFromPrescription(int rxId) {
//		try (Connection con = getConnection();) {
//			var ps = con.prepareStatement("SELECT patient_last_name FROM prescription WHERE id=?");
//			ps.setInt(1, rxId);
//			var rs = ps.executeQuery();
//			if (rs.next()) {
//				return rs.getString("patient_last_name");
//			} else {
//				return null; // Prescription not found
//			}
//		} catch (SQLException e) {
//			System.out.println("SQL error in getPatientLastNameFromPrescription: " + e.getMessage());
//			return null;
//		}
//
//	}

	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

}