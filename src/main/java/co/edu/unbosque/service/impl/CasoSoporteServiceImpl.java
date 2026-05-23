package co.edu.unbosque.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.entity.CasoSoporte;
import co.edu.unbosque.repository.CasoSoporteRepository;
import co.edu.unbosque.service.api.CasoSoporteServiceAPI;
import co.edu.unbosque.utils.GenericServiceImpl;

@Service
public class CasoSoporteServiceImpl extends GenericServiceImpl<CasoSoporte, Long>
        implements CasoSoporteServiceAPI {

    @Autowired
    private CasoSoporteRepository casoSoporteRepository;

    @Override
    public CrudRepository<CasoSoporte, Long> getDao() {
        return casoSoporteRepository;
    }

    @Override
    public List<CasoSoporte> findByIdUsuario(Long idUsuario) {
        return casoSoporteRepository.findByIdUsuario(idUsuario);
    }

    @Override
    public List<CasoSoporte> findByIdAgente(Long idAgente) {
        return casoSoporteRepository.findByIdAgente(idAgente);
    }

    @Override
    public List<CasoSoporte> findByEstado(String estado) {
        return casoSoporteRepository.findByEstado(estado);
    }

    @Override
    public List<CasoSoporte> findByTipoCaso(String tipoCaso) {
        return casoSoporteRepository.findByTipoCaso(tipoCaso);
    }
}