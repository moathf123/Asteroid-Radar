package com.udacity.asteroidradar.main

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding
import com.udacity.asteroidradar.repo.AsteroidRepository

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        val activity = requireNotNull(this.activity) {}
        ViewModelProvider(this, MainViewModelFactory(activity.application))
            .get(MainViewModel::class.java)
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        val adapter = AsteroidAdapter(OnClickListener {
            findNavController().navigate(MainFragmentDirections.actionShowDetail(it))
        })
        binding.asteroidRecycler.adapter = adapter

        viewModel.asteroid.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.show_all_menu -> viewModel.onFilterSelect(AsteroidRepository.Query.SAVED)
            R.id.show_today_menu -> viewModel.onFilterSelect(AsteroidRepository.Query.TODAY)
            R.id.show_week_menu -> viewModel.onFilterSelect(AsteroidRepository.Query.WEEK)
        }
        return true
    }
}
