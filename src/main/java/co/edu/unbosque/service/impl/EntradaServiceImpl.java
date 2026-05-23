package co.edu.unbosque.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.entity.Entrada;
import co.edu.unbosque.repository.EntradaRepository;
import co.edu.unbosque.service.api.EntradaServiceAPI;
import co.edu.unbosque.utils.GenericServiceImpl;

@Service
public class EntradaServiceImpl extends GenericServiceImpl<Entrada, Long>
        implements EntradaServiceAPI {

    @Autowired
    private EntradaRepository entradaRepository;

    @Override
    public CrudRepository<Entrada, Long> getDao() {
        return entradaRepository;
    }

    @Override
    public List<Entrada> findByIdPartido(Long idPartido) {
        return entradaRepository.findByIdPartido(idPartido);
    }

    @Override
    public List<Entrada> findByIdTitular(Long idTitular) {
        return entradaRepository.findByIdTitular(idTitular);
    }

    @Override
    public List<Entrada> findByEstado(String estado) {
        return entradaRepository.findByEstado(estado);
    }

    @Override
    public List<Entrada> findByIdPartidoAndEstado(Long idPartido, String estado) {
        return entradaRepository.findByIdPartidoAndEstado(idPartido, estado);
    }
}