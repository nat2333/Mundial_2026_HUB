package co.edu.unbosque.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.entity.PaqueteLaminas;
import co.edu.unbosque.repository.PaqueteLaminasRepository;
import co.edu.unbosque.service.api.PaqueteLaminasServiceAPI;
import co.edu.unbosque.utils.GenericServiceImpl;

@Service
public class PaqueteLaminasServiceImpl extends GenericServiceImpl<PaqueteLaminas, Long>
        implements PaqueteLaminasServiceAPI {

    @Autowired
    private PaqueteLaminasRepository paqueteLaminasRepository;

    @Override
    public CrudRepository<PaqueteLaminas, Long> getDao() {
        return paqueteLaminasRepository;
    }

    @Override
    public List<PaqueteLaminas> findByIdUsuario(Long idUsuario) {
        return paqueteLaminasRepository.findByIdUsuario(idUsuario);
    }

    @Override
    public List<PaqueteLaminas> findByIdUsuarioAndAbierto(Long idUsuario, byte abierto) {
        return paqueteLaminasRepository.findByIdUsuarioAndAbierto(idUsuario, abierto);
    }
}