package application.service;

import application.model.Patient;
import application.model.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import view.PatientView;

import java.util.Optional;

@Controller
public class ControllerPatientCreate {

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private SequenceService sequenceService;

	// Request for new patient registration form
	@GetMapping("/patient/new")
	public String getNewPatientForm(Model model) {
		model.addAttribute("patient", new PatientView());
		return "patient_register";
	}
	// Process patient registration

	@PostMapping("/patient/register")
	public String createPatient(PatientView p, Model model) {
		System.out.println("Received patient data: " + p);
		Patient patient = new Patient();

		patient.setId(sequenceService.getNextSequence("PATIENT_SEQUENCE"));
		patient.setSsn(p.getSsn());
		patient.setLastName(p.getLastName());
		patient.setFirstName(p.getFirstName());
		patient.setStreet(p.getStreet());
		patient.setCity(p.getCity());
		patient.setState(p.getState());
		patient.setZipcode(p.getZipcode());
		patient.setBirthdate(p.getBirthdate());
		patient.setPrimaryName(p.getPrimaryName());

		//save the patient to the repository
		patientRepository.save(patient);

		// display message and patient information
		model.addAttribute("message", "Registration successful. Patient ID: " + patient.getId());
		model.addAttribute("patient", patient);
		return "patient_show";
	}

	// Request for patient search form
	@GetMapping("/patient/get")
	public String getSearchForm(Model model) {
		model.addAttribute("patient", new PatientView());
		return "patient_get";
	}

	// Process patient search
	@PostMapping("/patient/get")
	public String showPatient(PatientView patientView, Model model) {
		Optional<Patient> optionalPatient = patientRepository.findById(patientView.getId());
		if (optionalPatient.isPresent()) {
			Patient patient = optionalPatient.get();
			patientView.setFirstName(patient.getFirstName());
			patientView.setLastName(patient.getLastName());
			patientView.setStreet(patient.getStreet());
			patientView.setCity(patient.getCity());
			patientView.setState(patient.getState());
			patientView.setZipcode(patient.getZipcode());
			patientView.setBirthdate(patient.getBirthdate());
			patientView.setPrimaryName(patient.getPrimaryName());
			// Add patient details to the model and output the view
			model.addAttribute("message", "Patient found.");
			model.addAttribute("patient", patientView);
			return "patient_show";
		} else {
			// If patient not found, add a message and return to the search form
			model.addAttribute("message", "Patient not found.");
			model.addAttribute("patient", patientView);
			return "patient_get";
		}
	}

// Request for patient update form
	@GetMapping("/patient/edit/{id}")
	public String getUpdateForm(@PathVariable int id, Model model) {
		Optional<Patient> optionalPatient = patientRepository.findById(id);
		// If patient exists, prepare the view for editing
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

			// Add patient details to the model for editing
			model.addAttribute("patient", patientView);
			return "patient_edit";
		} else {
			// If patient not found, add a message and return to the search form
			model.addAttribute("message", "Patient not found.");
			return "patient_get";
		}
	}
// Process patient update
	@PostMapping("/patient/edit")
	public String updatePatient(PatientView patientView, Model model) {
		Optional<Patient> optionalPatient = patientRepository.findById(patientView.getId());
		//if patient exists, update the details
		if (optionalPatient.isPresent()) {
			Patient patient = optionalPatient.get();
			patient.setStreet(patientView.getStreet());
			patient.setCity(patientView.getCity());
			patient.setState(patientView.getState());
			patient.setZipcode(patientView.getZipcode());
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