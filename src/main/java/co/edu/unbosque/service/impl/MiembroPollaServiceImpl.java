package co.edu.unbosque.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.entity.MiembroPolla;
import co.edu.unbosque.repository.MiembroPollaRepository;
import co.edu.unbosque.service.api.MiembroPollaServiceAPI;
import co.edu.unbosque.utils.GenericServiceImpl;

@Service
public class MiembroPollaServiceImpl extends GenericServiceImpl<MiembroPolla, Long>
        implements MiembroPollaServiceAPI {

    @Autowired
    private MiembroPollaRepository miembroPollaRepository;

    @Override
    public CrudRepository<MiembroPolla, Long> getDao() {
        return miembroPollaRepository;
    }

    @Override
    public List<MiembroPolla> findByIdPolla(Long idPolla) {
        return miembroPollaRepository.findByIdPolla(idPolla);
    }

    @Override
    public List<MiembroPolla> findByIdUsuario(Long idUsuario) {
        return miembroPollaRepository.findByIdUsuario(idUsuario);
    }

    @Override
    public Optional<MiembroPolla> findByIdPollaAndIdUsuario(Long idPolla, Long idUsuario) {
        return miembroPollaRepository.findByIdPollaAndIdUsuario(idPolla, idUsuario);
    }

    @Override
    public List<MiembroPolla> findRankingByIdPolla(Long idPolla) {
        return miembroPollaRepository.findByIdPollaOrderByPuntajeTotalDesc(idPolla);
    }
}