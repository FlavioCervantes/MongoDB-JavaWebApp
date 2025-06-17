package application.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import application.model.Prescription;
import application.model.PrescriptionRepository;
import view.PrescriptionView;

import static java.time.LocalDate.*;

@Controller
public class ControllerPrescriptionCreate {

	@Autowired
	private PrescriptionRepository prescriptionRepository;

	@Autowired
	private SequenceService sequenceService;

	@GetMapping("/prescription/new")
	public String getPrescriptionForm(Model model) {
		model.addAttribute("prescription", new PrescriptionView());
		return "prescription_create";
	}

	@PostMapping("/prescription")
	public String createPrescription(PrescriptionView p, Model model) {
		System.out.println("createPrescription " + p);

		if (p.getDrugName() == null || p.getDrugName().isEmpty()) {
			model.addAttribute("message", "Invalid drug name.");
			model.addAttribute("prescription", p);
			return "prescription_create";
		}

		int prescriptionId = sequenceService.getNextSequence("prescription");

		Prescription prescription = new Prescription();
		prescription.setPatientId(p.getPatientId());
		prescription.setDoctorId(p.getDoctorId());
			// Replace with actual drug ID lookup logic
		prescription.setQuantity(p.getQuantity());
		prescriptionRepository.save(prescription);

		model.addAttribute("message", "Prescription created.");
		model.addAttribute("prescription", p);
		return "prescription_show";
	}
}