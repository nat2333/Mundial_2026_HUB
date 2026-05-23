package co.edu.unbosque.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.entity.ReembolsoEntrada;
import co.edu.unbosque.repository.ReembolsoEntradaRepository;
import co.edu.unbosque.service.api.ReembolsoEntradaServiceAPI;
import co.edu.unbosque.utils.GenericServiceImpl;

@Service
public class ReembolsoEntradaServiceImpl extends GenericServiceImpl<ReembolsoEntrada, Long>
        implements ReembolsoEntradaServiceAPI {

    @Autowired
    private ReembolsoEntradaRepository reembolsoEntradaRepository;

    @Override
    public CrudRepository<ReembolsoEntrada, Long> getDao() {
        return reembolsoEntradaRepository;
    }

    @Override
    public List<ReembolsoEntrada> findByIdEntrada(Long idEntrada) {
        return reembolsoEntradaRepository.findByIdEntrada(idEntrada);
    }

    @Override
    public List<ReembolsoEntrada> findByIdUsuario(Long idUsuario) {
        return reembolsoEntradaRepository.findByIdUsuario(idUsuario);
    }

    @Override
    public List<ReembolsoEntrada> findByEstado(String estado) {
        return reembolsoEntradaRepository.findByEstado(estado);
    }

    @Override
    public Optional<ReembolsoEntrada> findByIdTransaccionReembolso(String idTransaccionReembolso) {
        return reembolsoEntradaRepository.findByIdTransaccionReembolso(idTransaccionReembolso);
    }
}