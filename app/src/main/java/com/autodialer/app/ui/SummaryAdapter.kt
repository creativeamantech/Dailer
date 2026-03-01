package com.autodialer.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.autodialer.app.data.Contact
import com.autodialer.app.databinding.ItemSummaryContactBinding

class SummaryAdapter(private var contacts: List<Contact>) :
    RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder>() {

    class SummaryViewHolder(val binding: ItemSummaryContactBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
        val binding = ItemSummaryContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SummaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
        val contact = contacts[position]
        holder.binding.tvItemName.text = contact.name
        holder.binding.tvItemPhone.text = contact.phoneNumber
        holder.binding.tvItemOutcome.text = "Outcome: ${contact.callOutcome ?: "Not Called"}"
        holder.binding.tvItemNotes.text = "Notes: ${contact.notes ?: ""}"
    }

    override fun getItemCount() = contacts.size

    fun updateData(newContacts: List<Contact>) {
        this.contacts = newContacts
        notifyDataSetChanged()
    }
}
