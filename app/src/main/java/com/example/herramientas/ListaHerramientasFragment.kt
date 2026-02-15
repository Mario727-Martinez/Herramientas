package com.example.herramientas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.herramientas.MysqLite.HerramientaDataSource
import com.example.herramientas.databinding.FragmentListaHerramientasBinding

class ListaHerramientasFragment : Fragment() {

    private var _binding: FragmentListaHerramientasBinding? = null
    private val binding get() = _binding!!

    private lateinit var herramientaDataSource: HerramientaDataSource
    private lateinit var adapter: ListaHerramientasAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaHerramientasBinding.inflate(inflater, container, false)
        herramientaDataSource = HerramientaDataSource(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val herramientas = herramientaDataSource.getHerramientasInfo()
        adapter = ListaHerramientasAdapter(herramientas)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}