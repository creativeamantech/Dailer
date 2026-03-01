package com.autodialer.app.ui

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.autodialer.app.R
import com.autodialer.app.data.Contact
import com.autodialer.app.databinding.FragmentSessionSummaryBinding
import com.opencsv.CSVWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileWriter

class SessionSummaryFragment : Fragment() {

    private var _binding: FragmentSessionSummaryBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var summaryAdapter: SummaryAdapter
    private var allContacts: List<Contact> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSessionSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        viewLifecycleOwner.lifecycleScope.launch {
            allContacts = sharedViewModel.getAllContactsSync()
            updateUI(allContacts)
        }

        binding.btnExport.setOnClickListener {
            exportToCSV()
        }

        binding.btnNewSession.setOnClickListener {
            findNavController().navigate(R.id.action_sessionSummaryFragment_to_dataImportFragment)
        }
    }

    private fun setupRecyclerView() {
        summaryAdapter = SummaryAdapter(emptyList())
        binding.rvSummary.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSummary.adapter = summaryAdapter
    }

    private fun updateUI(contacts: List<Contact>) {
        summaryAdapter.updateData(contacts)
        binding.tvTotalCalls.text = "Total Contacts: ${contacts.size}"

        val breakdown = contacts.groupingBy { it.callOutcome ?: "Not Called" }.eachCount()
        val breakdownText = StringBuilder()
        breakdown.forEach { (outcome, count) ->
            breakdownText.append("$outcome: $count\n")
        }
        binding.tvBreakdown.text = breakdownText.toString().trim()
    }

    private fun exportToCSV() {
        if (allContacts.isEmpty()) {
            Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, "autodialer_summary_${System.currentTimeMillis()}.csv")

                val writer = CSVWriter(FileWriter(file))

                // Determine all unique JSON keys from extra data to use as headers
                val extraKeys = mutableSetOf<String>()
                allContacts.forEach { contact ->
                    if (contact.extraData.isNotBlank()) {
                        try {
                            val json = JSONObject(contact.extraData)
                            json.keys().forEach { extraKeys.add(it) }
                        } catch (e: Exception) { }
                    }
                }

                // Headers
                val headerRow = mutableListOf("Name", "Phone Number", "Called", "Outcome", "Notes")
                headerRow.addAll(extraKeys)
                writer.writeNext(headerRow.toTypedArray())

                // Data Rows
                allContacts.forEach { contact ->
                    val row = mutableListOf(
                        contact.name,
                        contact.phoneNumber,
                        contact.hasBeenCalled.toString(),
                        contact.callOutcome ?: "",
                        contact.notes ?: ""
                    )

                    val extraJson = try {
                        JSONObject(contact.extraData)
                    } catch (e: Exception) {
                        JSONObject()
                    }

                    extraKeys.forEach { key ->
                        row.add(extraJson.optString(key, ""))
                    }

                    writer.writeNext(row.toTypedArray())
                }

                writer.close()

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Exported to Downloads: ${file.name}", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
