package co.edu.unbosque.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.entity.PreferenciaUsuario;
import co.edu.unbosque.repository.PreferenciaUsuarioRepository;
import co.edu.unbosque.service.api.PreferenciaUsuarioServiceAPI;
import co.edu.unbosque.utils.GenericServiceImpl;

@Service
public class PreferenciaUsuarioServiceImpl extends GenericServiceImpl<PreferenciaUsuario, Long>
        implements PreferenciaUsuarioServiceAPI {

    @Autowired
    private PreferenciaUsuarioRepository preferenciaUsuarioRepository;

    @Override
    public CrudRepository<PreferenciaUsuario, Long> getDao() {
        return preferenciaUsuarioRepository;
    }

    @Override
    public Optional<PreferenciaUsuario> findByIdUsuario(Long idUsuario) {
        return preferenciaUsuarioRepository.findByIdUsuario(idUsuario);
    }
}