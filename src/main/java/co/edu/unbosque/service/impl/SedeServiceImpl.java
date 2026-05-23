package co.edu.unbosque.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.entity.Sede;
import co.edu.unbosque.repository.SedeRepository;
import co.edu.unbosque.service.api.SedeServiceAPI;
import co.edu.unbosque.utils.GenericServiceImpl;

@Service
public class SedeServiceImpl extends GenericServiceImpl<Sede, Long>
        implements SedeServiceAPI {

    @Autowired
    private SedeRepository sedeRepository;

    @Override
    public CrudRepository<Sede, Long> getDao() {
        return sedeRepository;
    }

    @Override
    public List<Sede> findByPais(String pais) {
        return sedeRepository.findByPaisIgnoreCase(pais);
    }

    @Override
    public List<Sede> findByCiudad(String ciudad) {
        return sedeRepository.findByCiudadIgnoreCase(ciudad);
    }
}
