package application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import application.model.*;
import view.PatientView;

@Controller
public class ControllerPatientUpdate {

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private DoctorRepository doctorRepository;

	@Autowired
	private SequenceService sequence;

	@GetMapping("/patient/update/{id}")
	public String getUpdateForm(@PathVariable int id, Model model) {
		Patient patient = patientRepository.findById(id).orElse(null);

		if (patient == null) {
			model.addAttribute("message", "Patient not found");
			return "index"; // return to home page
		}

		PatientView pv = new PatientView();
		pv.setId(patient.getId());
		pv.setFirstName(patient.getFirstName());
		pv.setLastName(patient.getLastName());
		pv.setBirthdate(patient.getBirthdate());
		pv.setSsn(patient.getSsn());
		pv.setStreet(patient.getStreet());
		pv.setCity(patient.getCity());
		pv.setState(patient.getState());
		pv.setZipcode(patient.getZipcode());
		pv.setPrimaryName(patient.getPrimaryName());

		model.addAttribute("patient", pv);
		return "patient_edit";
	}

	@PostMapping("/patient/update")
	public String updatePatient(PatientView p, Model model) {
		Doctor doctor = doctorRepository.findByLastName(p.getPrimaryName()).orElse(null);

		if (doctor == null) {
			model.addAttribute("message", "Doctor not found. Check the last name.");
			model.addAttribute("patient", p);
			return "patient_edit";
		}

		Patient patient = patientRepository.findById(p.getId()).orElse(null);
		if (patient == null) {
			model.addAttribute("message", "Patient not found.");
			return "index";
		}

		patient.setDoctorId(doctor.getId());
		patient.setStreet(p.getStreet());
		patient.setCity(p.getCity());
		patient.setState(p.getState());
		patient.setZipcode(p.getZipcode());
		patient.setBirthdate(p.getBirthdate());
		patient.setFirstName(p.getFirstName());
		patient.setLastName(p.getLastName());
		patient.setSsn(p.getSsn());



		patientRepository.save(patient);

		model.addAttribute("message", "Patient profile updated successfully.");
		model.addAttribute("patient", p);
		return "patient_show";
	}

}