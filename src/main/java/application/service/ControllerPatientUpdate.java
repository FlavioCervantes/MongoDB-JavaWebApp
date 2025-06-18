package application.service;

import application.model.*;
import view.PatientView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class ControllerPatientUpdate {

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private DoctorRepository doctorRepository;

	@Autowired
	private SequenceService sequence;

	// 🛠 GET fallback to prevent 405 error when someone directly accesses /patient/edit
	@GetMapping("/patient/edit")
	public String handleEditWithoutId() {
		return "redirect:/patient/get";  // or redirect to home or error page
	}

	// ✅ Get form to edit patient by ID
	@GetMapping("/patient/edit/{id}")
	public String getUpdateForm(@PathVariable int id, Model model) {
		Optional<Patient> optionalPatient = patientRepository.findById(id);
		if (optionalPatient.isPresent()) {
			Patient patient = optionalPatient.get();
			PatientView patientView = new PatientView();
			patientView.setId(patient.getId());
			patientView.setFirstName(patient.getFirstName());
			patientView.setLastName(patient.getLastName());
			patientView.setStreet(patient.getStreet());
			patientView.setCity(patient.getCity());
			patientView.setState(patient.getState());
			patientView.setZipcode(patient.getZipcode());
			patientView.setBirthdate(patient.getBirthdate());
			patientView.setPrimaryName(patient.getPrimaryName());

			model.addAttribute("patient", patientView);
			return "patient_edit";
		} else {
			model.addAttribute("message", "Patient not found.");
			return "patient_get";
		}
	}

	// patient update handling
	@PostMapping("/patient/edit")
	public String updatePatient(PatientView patientView, Model model) {
		Optional<Patient> optionalPatient = patientRepository.findById(patientView.getId());
		if (optionalPatient.isPresent()) {
			Patient patient = optionalPatient.get();

			// Update editable fields
			patient.setStreet(patientView.getStreet());
			patient.setCity(patientView.getCity());
			patient.setState(patientView.getState());
			patient.setZipcode(patientView.getZipcode());

			// Update doctor if provided
			Optional<Doctor> doctorOpt = doctorRepository.findByLastName(patientView.getPrimaryName());
			if (doctorOpt.isPresent()) {
				Doctor doctor = doctorOpt.get();
				patient.setPrimaryName(doctor.getLastName());
			} else {
				model.addAttribute("message", "Doctor not found. Update skipped.");
			}

			patientRepository.save(patient);
			model.addAttribute("message", "Update successful.");
			model.addAttribute("patient", patientView);
			return "patient_show";
		} else {
			model.addAttribute("message", "Patient not found.");
			model.addAttribute("patient", patientView);
			return "patient_get";
		}
	}
}
