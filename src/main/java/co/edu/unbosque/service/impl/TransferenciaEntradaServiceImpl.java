package co.edu.unbosque.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import co.edu.unbosque.entity.TransferenciaEntrada;
import co.edu.unbosque.repository.TransferenciaEntradaRepository;
import co.edu.unbosque.service.api.TransferenciaEntradaServiceAPI;
import co.edu.unbosque.utils.GenericServiceImpl;

@Service
public class TransferenciaEntradaServiceImpl extends GenericServiceImpl<TransferenciaEntrada, Long>
        implements TransferenciaEntradaServiceAPI {

    @Autowired
    private TransferenciaEntradaRepository transferenciaEntradaRepository;

    @Override
    public CrudRepository<TransferenciaEntrada, Long> getDao() {
        return transferenciaEntradaRepository;
    }

    @Override
    public List<TransferenciaEntrada> findByIdEntrada(Long idEntrada) {
        return transferenciaEntradaRepository.findByIdEntrada(idEntrada);
    }

    @Override
    public List<TransferenciaEntrada> findByIdUsuarioOrigen(Long idUsuarioOrigen) {
        return transferenciaEntradaRepository.findByIdUsuarioOrigen(idUsuarioOrigen);
    }

    @Override
    public List<TransferenciaEntrada> findByIdUsuarioDestino(Long idUsuarioDestino) {
        return transferenciaEntradaRepository.findByIdUsuarioDestino(idUsuarioDestino);
    }
}