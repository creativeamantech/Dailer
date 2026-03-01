package com.autodialer.app.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.autodialer.app.R
import com.autodialer.app.data.Contact
import com.autodialer.app.databinding.FragmentActiveDialingBinding
import kotlinx.coroutines.launch
import org.json.JSONObject

class ActiveDialingFragment : Fragment() {

    private var _binding: FragmentActiveDialingBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var currentContactIndex = 0
    private var contactsList: List<Contact> = emptyList()
    private var isCallActive = false

    private val telephonyManager by lazy {
        requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    private val callPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            makeCall()
        } else {
            Toast.makeText(context, "Permissions required to dial automatically", Toast.LENGTH_SHORT).show()
        }
    }

    private val phoneStateListener = object : PhoneStateListener() {
        @Deprecated("Deprecated in Java")
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
            when (state) {
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    isCallActive = true
                    // Call started
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    if (isCallActive) {
                        isCallActive = false
                        // Call ended, show outcome layout
                        requireActivity().runOnUiThread {
                            showOutcomeLayout()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActiveDialingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOutcomesSpinner()

        viewLifecycleOwner.lifecycleScope.launch {
            contactsList = sharedViewModel.getAllContactsSync().filter { !it.hasBeenCalled }
            if (contactsList.isEmpty()) {
                finishSession()
            } else {
                displayContact(contactsList[currentContactIndex])
            }
        }

        binding.btnCall.setOnClickListener {
            checkPermissionsAndCall()
        }

        binding.btnSkip.setOnClickListener {
            handleSkip()
        }

        binding.btnEndSession.setOnClickListener {
            finishSession()
        }

        binding.btnSaveOutcome.setOnClickListener {
            saveOutcomeAndNext()
        }

        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun setupOutcomesSpinner() {
        val outcomes = listOf("Answered", "No Answer", "Busy", "Left Voicemail", "Callback Requested")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, outcomes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerOutcome.adapter = adapter
    }

    private fun displayContact(contact: Contact) {
        binding.tvProgress.text = "Calling ${currentContactIndex + 1} of ${contactsList.size}"
        binding.tvContactName.text = contact.name
        binding.tvPhoneNumber.text = contact.phoneNumber

        val extraDataStr = StringBuilder()
        try {
            val json = JSONObject(contact.extraData)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                extraDataStr.append("<b>$key</b>: ${json.getString(key)}<br>")
            }
        } catch (e: Exception) {
            extraDataStr.append("No extra data.")
        }

        binding.tvExtraInfo.text = android.text.Html.fromHtml(extraDataStr.toString(), android.text.Html.FROM_HTML_MODE_COMPACT)

        binding.layoutCallControls.visibility = View.VISIBLE
        binding.layoutOutcome.visibility = View.GONE
        binding.etNotes.text.clear()
        binding.spinnerOutcome.setSelection(0)
    }

    private fun checkPermissionsAndCall() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            callPermissionLauncher.launch(
                arrayOf(Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE)
            )
        } else {
            makeCall()
        }
    }

    private fun makeCall() {
        val contact = contactsList[currentContactIndex]
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:${contact.phoneNumber}")
        startActivity(intent)
    }

    private fun showOutcomeLayout() {
        binding.layoutCallControls.visibility = View.GONE
        binding.layoutOutcome.visibility = View.VISIBLE
    }

    private fun saveOutcomeAndNext() {
        val contact = contactsList[currentContactIndex]
        contact.hasBeenCalled = true
        contact.callOutcome = binding.spinnerOutcome.selectedItem.toString()
        contact.notes = binding.etNotes.text.toString()

        sharedViewModel.updateContact(contact)
        moveToNext()
    }

    private fun handleSkip() {
        val contact = contactsList[currentContactIndex]
        contact.hasBeenCalled = true
        contact.callOutcome = "Skipped"
        sharedViewModel.updateContact(contact)
        moveToNext()
    }

    private fun moveToNext() {
        currentContactIndex++
        if (currentContactIndex < contactsList.size) {
            displayContact(contactsList[currentContactIndex])
        } else {
            finishSession()
        }
    }

    private fun finishSession() {
        Toast.makeText(context, "Session Complete", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_activeDialingFragment_to_sessionSummaryFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        _binding = null
    }
}
