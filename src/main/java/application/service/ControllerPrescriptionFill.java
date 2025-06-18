package application.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import application.model.*;
import view.PrescriptionView;

@Controller
public class ControllerPrescriptionFill {

	@Autowired
	private PrescriptionRepository prescriptionRepository;

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private PharmacyRepository pharmacyRepository;

	@Autowired
	private SequenceService sequenceService;

	@GetMapping("/prescription/fill")
	public String getFillForm(Model model) {
		model.addAttribute("prescription", new PrescriptionView());
		return "prescription_fill";
	}

	@PostMapping("/prescription/fill")
	public String processFillForm(PrescriptionView p, Model model) {
		// validate prescription ID and patient last name
		if (p.getRxid() <= 0 || p.getPatientLastName() == null || p.getPatientLastName().isBlank()) {
			model.addAttribute("message", "Missing prescription ID or patient last name.");
			model.addAttribute("prescription", p);
			return "prescription_fill";
		}

		// find prescription
		Optional<Prescription> presOpt = prescriptionRepository.findById(p.getRxid());
		if (presOpt.isEmpty()) {
			model.addAttribute("message", "Prescription not found.");
			model.addAttribute("prescription", p);
			return "prescription_fill";
		}
		Prescription prescription = presOpt.get();

		// validate patient last name
		Optional<Patient> patientOpt = patientRepository.findById(prescription.getPatientId());
		if (patientOpt.isEmpty() || !patientOpt.get().getLastName().equalsIgnoreCase(p.getPatientLastName())) {
			model.addAttribute("message", "Invalid patient last name for this prescription.");
			model.addAttribute("prescription", p);
			return "prescription_fill";
		}

		// check if refills remain
		if (prescription.getFills().size() >= prescription.getRefills() + 1) {
			model.addAttribute("message", "No refills left for this prescription.");
			model.addAttribute("prescription", p);
			return "prescription_show";
		}

		// trim pharmacy input fields
		String name = p.getPharmacyName() != null ? p.getPharmacyName().trim() : "";
		String address = p.getPharmacyAddress() != null ? p.getPharmacyAddress().trim() : "";

		// Look up pharmacy
		Pharmacy pharmacy = pharmacyRepository.findByNameAndAddress(name, address);
		if (pharmacy == null) {
			model.addAttribute("message", "Pharmacy not found.");
			model.addAttribute("prescription", p);
			return "prescription_fill";
		}

		// TODO: determine cost based off pharmacy?
		Prescription.FillRequest fill = new Prescription.FillRequest();
		fill.setPharmacyID(pharmacy.getId());
		fill.setDateFilled(LocalDate.now().toString());
		fill.setCost("0.00");
		prescription.getFills().add(fill);
		prescriptionRepository.save(prescription);

		model.addAttribute("message", "Prescription filled successfully.");
		model.addAttribute("prescription", p);
		return "prescription_show";
	}
}
