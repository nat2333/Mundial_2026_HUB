package co.edu.unbosque.service.api;

import java.util.List;

import co.edu.unbosque.entity.Lamina;
import co.edu.unbosque.utils.GenericServiceAPI;

public interface LaminaServiceAPI extends GenericServiceAPI<Lamina, Long> {
    List<Lamina> findBySeleccion(String seleccion);
    List<Lamina> findByCategoria(String categoria);
    List<Lamina> findByRareza(String rareza);
}