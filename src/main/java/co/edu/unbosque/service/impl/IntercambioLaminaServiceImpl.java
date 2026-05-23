package co.edu.unbosque.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.entity.IntercambioLamina;
import co.edu.unbosque.repository.IntercambioLaminaRepository;
import co.edu.unbosque.service.api.IntercambioLaminaServiceAPI;
import co.edu.unbosque.utils.GenericServiceImpl;

@Service
public class IntercambioLaminaServiceImpl extends GenericServiceImpl<IntercambioLamina, Long>
        implements IntercambioLaminaServiceAPI {

    @Autowired
    private IntercambioLaminaRepository intercambioLaminaRepository;

    @Override
    public CrudRepository<IntercambioLamina, Long> getDao() {
        return intercambioLaminaRepository;
    }

    @Override
    public List<IntercambioLamina> findByIdUsuarioOferta(Long idUsuarioOferta) {
        return intercambioLaminaRepository.findByIdUsuarioOferta(idUsuarioOferta);
    }

    @Override
    public List<IntercambioLamina> findByIdUsuarioReceptor(Long idUsuarioReceptor) {
        return intercambioLaminaRepository.findByIdUsuarioReceptor(idUsuarioReceptor);
    }

    @Override
    public List<IntercambioLamina> findByEstado(String estado) {
        return intercambioLaminaRepository.findByEstado(estado);
    }
}