package com.autodialer.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.autodialer.app.R
import kotlinx.coroutines.launch
import com.autodialer.app.data.Contact
import com.autodialer.app.databinding.FragmentDataImportBinding
import org.json.JSONObject

class DataImportFragment : Fragment() {

    private var _binding: FragmentDataImportBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var parsedHeaders: List<String> = emptyList()
    private var parsedRows: List<List<String>> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataImportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnDetectColumns.setOnClickListener {
            val rawData = binding.etPastedData.text.toString()
            if (rawData.isNotBlank()) {
                parseData(rawData)
            } else {
                Toast.makeText(context, "Please paste some data first", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnStartDialing.setOnClickListener {
            startDialing()
        }
    }

    private fun parseData(rawData: String) {
        // Simple heuristic: Try to split by tab first (Excel/Sheets default), then comma if tab is not found
        val lines = rawData.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            Toast.makeText(context, "No valid data found", Toast.LENGTH_SHORT).show()
            return
        }

        val delimiter = if (lines[0].contains("\t")) "\t" else ","
        val allRows = lines.map { it.split(delimiter).map { col -> col.trim() } }

        // Assume first row is header
        parsedHeaders = allRows[0]
        parsedRows = allRows.drop(1)

        if (parsedHeaders.size < 2) {
            Toast.makeText(context, "Could not detect at least 2 columns (Name, Phone)", Toast.LENGTH_SHORT).show()
            return
        }

        setupSpinners(parsedHeaders)

        binding.layoutColumnMapping.visibility = View.VISIBLE
        binding.tvPreview.text = "Preview: ${parsedRows.size} contacts found."
        binding.btnDetectColumns.visibility = View.GONE
    }

    private fun setupSpinners(headers: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, headers)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerNameColumn.adapter = adapter
        binding.spinnerPhoneColumn.adapter = adapter

        // Try to auto-select columns based on name heuristics
        val nameIndex = headers.indexOfFirst { it.contains("name", ignoreCase = true) }
        val phoneIndex = headers.indexOfFirst { it.contains("phone", ignoreCase = true) || it.contains("number", ignoreCase = true) }

        if (nameIndex != -1) binding.spinnerNameColumn.setSelection(nameIndex)
        if (phoneIndex != -1) {
            binding.spinnerPhoneColumn.setSelection(phoneIndex)
        } else if (headers.size > 1 && nameIndex == 0) {
            binding.spinnerPhoneColumn.setSelection(1) // Default to second column
        }
    }

    private fun startDialing() {
        val nameIndex = binding.spinnerNameColumn.selectedItemPosition
        val phoneIndex = binding.spinnerPhoneColumn.selectedItemPosition

        if (nameIndex == phoneIndex) {
            Toast.makeText(context, "Name and Phone columns must be different", Toast.LENGTH_SHORT).show()
            return
        }

        if (parsedRows.isEmpty()) {
            Toast.makeText(context, "No data to import", Toast.LENGTH_SHORT).show()
            return
        }

        val contacts = parsedRows.map { row ->
            val name = row.getOrNull(nameIndex) ?: "Unknown"
            val phone = row.getOrNull(phoneIndex) ?: ""

            // Collect other columns as extra data
            val extras = JSONObject()
            parsedHeaders.forEachIndexed { index, header ->
                if (index != nameIndex && index != phoneIndex) {
                    extras.put(header, row.getOrNull(index) ?: "")
                }
            }

            Contact(
                name = name,
                phoneNumber = phone,
                extraData = extras.toString()
            )
        }.filter { it.phoneNumber.isNotBlank() } // Only keep rows with phone numbers

        if (contacts.isEmpty()) {
            Toast.makeText(context, "No valid phone numbers found", Toast.LENGTH_SHORT).show()
            return
        }

        // Use lifecycleScope to run the suspending function
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.insertContacts(contacts)
            findNavController().navigate(R.id.action_dataImportFragment_to_activeDialingFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
