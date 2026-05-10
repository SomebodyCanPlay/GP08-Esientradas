package edu.esi.ds.esientradas.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.esi.ds.esientradas.model.Configuracion;

@Repository
public interface ConfiguracionDao extends JpaRepository<Configuracion, Long> {
}
