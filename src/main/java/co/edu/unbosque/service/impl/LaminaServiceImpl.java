package co.edu.unbosque.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.entity.Lamina;
import co.edu.unbosque.repository.LaminaRepository;
import co.edu.unbosque.service.api.LaminaServiceAPI;
import co.edu.unbosque.utils.GenericServiceImpl;

@Service
public class LaminaServiceImpl extends GenericServiceImpl<Lamina, Long>
        implements LaminaServiceAPI {

    @Autowired
    private LaminaRepository laminaRepository;

    @Override
    public CrudRepository<Lamina, Long> getDao() {
        return laminaRepository;
    }

    @Override
    public List<Lamina> findBySeleccion(String seleccion) {
        return laminaRepository.findBySeleccion(seleccion);
    }

    @Override
    public List<Lamina> findByCategoria(String categoria) {
        return laminaRepository.findByCategoria(categoria);
    }

    @Override
    public List<Lamina> findByRareza(String rareza) {
        return laminaRepository.findByRareza(rareza);
    }
}