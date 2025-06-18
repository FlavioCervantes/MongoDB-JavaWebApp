package application.service;

import java.time.LocalDate;
import java.util.ArrayList;

import application.model.DoctorRepository;
import application.model.PatientRepository;
import application.model.Prescription;
import application.model.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import view.PrescriptionView;

@Controller
public class ControllerPrescriptionCreate {

	@Autowired
	private PrescriptionRepository prescriptionRepository;

	@Autowired
	private SequenceService sequenceService;

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private DoctorRepository doctorRepository;

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
		//Confirm drug name
		if(p.getDrugName() == null || p.getDrugName().isEmpty()) {
			model.addAttribute("message", "Drug name is required.");
			model.addAttribute("prescription", p);
			return "prescription_create";
		}
		/*
		 * valid doctor name and id
		 */
		if(p.getPatientId() <= 0 || p.getDoctorId() <= 0) {
			model.addAttribute("message", "Patient ID and doctor ID is required.");
			model.addAttribute("prescription", p);
			return "prescription_create";
		}

		if (!patientRepository.existsById(p.getPatientId())) {
			model.addAttribute("message", "Patient does not exist.");
			model.addAttribute("prescription", p);
			return "prescription_create";
		}

		if(!doctorRepository.existsById(p.getDoctorId())) {
			model.addAttribute("message", "Doctor does not exist.");
			model.addAttribute("prescription", p);
			return "prescription_create";
		}

		if(p.getQuantity() <= 0) {
			model.addAttribute("message", "Quantity is required.");
			model.addAttribute("prescription", p);
			return "prescription_create";
		}

		if (p.getRefills() < 0) {
			model.addAttribute("message", "Refills is required.");
			model.addAttribute("prescription", p);
			return "prescription_create";
		}
		//Make RXID
		int rxid = sequenceService.getNextSequence("prescription_sequence");

		//Prescription entity
		Prescription prescription = new Prescription();
		prescription.setRxid(rxid);
		prescription.setDrugName(p.getDrugName());
		prescription.setQuantity(p.getQuantity());
		prescription.setPatientId(p.getPatientId());
		prescription.setDoctorId(p.getDoctorId());
		prescription.setDateCreated(LocalDate.now().toString());
		prescription.setRefills(p.getRefills());
		prescription.setFills(new ArrayList<>());
		//
		//Save into Mongodb
		prescriptionRepository.insert(prescription);

		//update view
		p.setRxid(rxid);
		p.setDateCreated(prescription.getDateCreated());
		model.addAttribute("message", "Prescription created successfully.");
		model.addAttribute("prescription", p);
		return "prescription_show";

	}
}