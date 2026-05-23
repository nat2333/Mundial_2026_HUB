package co.edu.unbosque.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.entity.Polla;
import co.edu.unbosque.repository.PollaRepository;
import co.edu.unbosque.service.api.PollaServiceAPI;
import co.edu.unbosque.utils.GenericServiceImpl;

@Service
public class PollaServiceImpl extends GenericServiceImpl<Polla, Long>
        implements PollaServiceAPI {

    @Autowired
    private PollaRepository pollaRepository;

    @Override
    public CrudRepository<Polla, Long> getDao() {
        return pollaRepository;
    }

    @Override
    public Optional<Polla> findByCodigoInvitacion(String codigoInvitacion) {
        return pollaRepository.findByCodigoInvitacion(codigoInvitacion);
    }

    @Override
    public List<Polla> findByIdCreador(Long idCreador) {
        return pollaRepository.findByIdCreador(idCreador);
    }

    @Override
    public List<Polla> findByEstado(String estado) {
        return pollaRepository.findByEstado(estado);
    }
}