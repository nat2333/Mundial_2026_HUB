package co.edu.unbosque.service.api;

import java.util.List;

import co.edu.unbosque.entity.Sede;
import co.edu.unbosque.utils.GenericServiceAPI;

public interface SedeServiceAPI extends GenericServiceAPI<Sede, Long> {

    List<Sede> findByPais(String pais);

    List<Sede> findByCiudad(String ciudad);
}
