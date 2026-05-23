package co.edu.unbosque.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.entity.LaminaUsuario;
import co.edu.unbosque.repository.LaminaUsuarioRepository;
import co.edu.unbosque.service.api.LaminaUsuarioServiceAPI;
import co.edu.unbosque.utils.GenericServiceImpl;

@Service
public class LaminaUsuarioServiceImpl extends GenericServiceImpl<LaminaUsuario, Long>
        implements LaminaUsuarioServiceAPI {

    @Autowired
    private LaminaUsuarioRepository laminaUsuarioRepository;

    @Override
    public CrudRepository<LaminaUsuario, Long> getDao() {
        return laminaUsuarioRepository;
    }

    @Override
    public List<LaminaUsuario> findByIdUsuario(Long idUsuario) {
        return laminaUsuarioRepository.findByIdUsuario(idUsuario);
    }

    @Override
    public List<LaminaUsuario> findByIdLamina(Long idLamina) {
        return laminaUsuarioRepository.findByIdLamina(idLamina);
    }

    @Override
    public Optional<LaminaUsuario> findByIdUsuarioAndIdLamina(Long idUsuario, Long idLamina) {
        return laminaUsuarioRepository.findByIdUsuarioAndIdLamina(idUsuario, idLamina);
    }

    @Override
    public List<LaminaUsuario> findByIdUsuarioAndEnIntercambio(Long idUsuario, byte enIntercambio) {
        return laminaUsuarioRepository.findByIdUsuarioAndEnIntercambio(idUsuario, enIntercambio);
    }
}