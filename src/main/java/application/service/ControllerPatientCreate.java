package application.service;


import java.util.Optional;


import application.model.Doctor;
import application.model.DoctorRepository;
import application.model.Patient;
import application.model.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ControllerPatientCreate {


	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private DoctorRepository doctorRepository;

	@Autowired
	private SequenceService sequenceService;

	/*
	 * Request blank patient registration form.

	//@GetMapping("/patient/new")
//	public String getNewPatientForm(Model model) {
		// return blank form for new patient registration
	//	model.addAttribute("patient", new PatientView());
	//	return "patient_register";
	//}



	/*
	 * Request for new patient registration form.
	 */
	@GetMapping("/patient/register")
	public String getNewPatientForm(Model model) {
		model.addAttribute("patient", new PatientView());
		return "patient_register";
	}

	/*
	 * Process data from the patient_register form
	 */
	// TODO
	@PostMapping("/patient/register")
	public String createPatient(PatientView p, Model model) {
		Optional<Doctor> doctor = doctorRepository.findByLastName(p.getPrimaryName());
		if (doctor.isPresent()) {
			model.addAttribute("message", "Doctor not found. Please check the last name.");
			model.addAttribute("patient", p);
			return "patient_register";
		}

		Patient patient = new Patient();
		patient.setSsn(p.getSsn());
		patient.setLastName(p.getLastName());
		patient.setLastName(p.getLastName());
		patient.setFirstName(p.getFirstName());
		patient.setStreet(p.getStreet());
		patient.setCity(p.getCity());
		patient.setState(p.getState());
		patient.setZipcode(p.getZipcode());
		patient.setBirthdate(patient.getBirthdate() != null ? patient.getBirthdate().toString() : null);
		patientRepository.save(patient);

		model.addAttribute("message", "Registration successful. Patient ID: " + patient.getId());
		model.addAttribute("patient", patient);
		return "patient_show";
	}


	/*
	 * Request blank form to search for patient by id and name
	 */
	@GetMapping("/patient/get")
	public String getSearchForm(Model model) {
		model.addAttribute("patient", new PatientView());
		return "patient_get";
	}

	/*
	 * Perform search for patient by patient id and name.
	 */
	// TODO: search for patient by id and name
	@PostMapping("/patient/get")
	public String showPatient(PatientView patientView, Model model) {
		Patient patient = patientRepository.findByIdAndLastName(patientView.getId(), patientView.getLastName());
		if (patient != null) {
			patientView.setFirstName(patient.getFirstName());
			patientView.setLastName(patient.getLastName());
			patientView.setStreet(patient.getStreet());
			patientView.setCity(patient.getCity());
			patientView.setState(patient.getState());
			patientView.setZipcode(patient.getZipcode());
			patientView.setBirthdate(patient.getBirthdate() != null ? patient.getBirthdate().toString() : null);
			patientView.setPrimaryName(patient.getDoctor().getLastName());

			model.addAttribute("message", "Patient found.");
			model.addAttribute("patient", patientView);
			return "patient_show";
		} else {
			model.addAttribute("message", "Patient not found.");
			model.addAttribute("patient", patientView);
			return "patient_get";
		}

	}


	/*
	 * Request form to update patient details.
	 */
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
			patientView.setBirthdate(patient.getBirthdate() != null ? patient.getBirthdate().toString() : null);
			patientView.setPrimaryName(patient.getDoctor().getLastName());

			model.addAttribute("patient", patientView);
			return "patient_edit";
		} else {
			model.addAttribute("message", "Patient not found.");
			return "patient_get";
		}
	}

	/*
	 * Process patient profile update.
	 */
	@PostMapping("/patient/edit")
	public String updatePatient(PatientView patientView, Model model) {
		Optional<Patient> optionalPatient = patientRepository.findById(patientView.getId());
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
