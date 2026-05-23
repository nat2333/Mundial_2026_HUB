package co.edu.unbosque.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.entity.DiscrepanciaDatos;
import co.edu.unbosque.repository.DiscrepanciaDatosRepository;
import co.edu.unbosque.service.api.DiscrepanciaDatosServiceAPI;
import co.edu.unbosque.utils.GenericServiceImpl;

@Service
public class DiscrepanciaDatosServiceImpl extends GenericServiceImpl<DiscrepanciaDatos, Long>
        implements DiscrepanciaDatosServiceAPI {

    @Autowired
    private DiscrepanciaDatosRepository discrepanciaDatosRepository;

    @Override
    public CrudRepository<DiscrepanciaDatos, Long> getDao() {
        return discrepanciaDatosRepository;
    }

    @Override
    public List<DiscrepanciaDatos> findByIdPartido(Long idPartido) {
        return discrepanciaDatosRepository.findByIdPartido(idPartido);
    }

    @Override
    public List<DiscrepanciaDatos> findByResuelto(byte resuelto) {
        return discrepanciaDatosRepository.findByResuelto(resuelto);
    }
}