package com.mandalecarbon.service;

import com.mandalecarbon.model.Cliente;
import com.mandalecarbon.repository.ClienteJpaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteJpaRepository repo;

    @InjectMocks
    private ClienteService service;

    private Cliente activo;
    private Cliente inactivo;

    @BeforeEach
    void prepararClientes() {
        activo = cliente("Andrea Torres", "987654321", "andrea@example.com", true);
        inactivo = cliente("Luis Pérez", "912345678", "luis@example.com", false);
    }

    @Test
    void listarDevuelveSoloClientesActivos() {
        when(repo.findAll()).thenReturn(List.of(activo, inactivo));
        assertEquals(List.of(activo), service.listar());
    }

    @Test
    void buscarPorEstadoDevuelveInactivos() {
        when(repo.findAll()).thenReturn(List.of(activo, inactivo));
        assertEquals(List.of(inactivo), service.buscar(null, "Inactivos"));
    }

    @Test
    void buscarTodosIncluyeActivosEInactivos() {
        when(repo.findAll()).thenReturn(List.of(activo, inactivo));
        assertEquals(List.of(activo, inactivo), service.buscar(null, "Todos"));
    }

    @Test
    void buscarIgnoraMayusculasYEspaciosAlrededor() {
        when(repo.findAll()).thenReturn(List.of(activo, inactivo));
        assertEquals(List.of(activo), service.buscar("  ANDREA ", "Activos"));
    }

    @Test
    void buscarIgnoraTildesEnNombresDeClientes() {
        when(repo.findAll()).thenReturn(List.of(activo, inactivo));

        assertEquals(List.of(inactivo), service.buscar("Perez", "Inactivos"));
    }

    @Test
    void cambiarEstadoDesactivaUnClienteActivo() {
        when(repo.findById(1)).thenReturn(Optional.of(activo));

        service.cambiarEstado(1);

        assertFalse(activo.isActivo());
        verify(repo).save(activo);
    }

    @Test
    void cambiarEstadoActivaUnClienteInactivo() {
        when(repo.findById(2)).thenReturn(Optional.of(inactivo));

        service.cambiarEstado(2);

        assertTrue(inactivo.isActivo());
        verify(repo).save(inactivo);
    }

    @Test
    void cambiarEstadoDeClienteInexistenteFalla() {
        when(repo.findById(404)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> service.cambiarEstado(404));
    }

    private Cliente cliente(String nombre, String telefono, String correo, boolean activo) {
        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setTelefono(telefono);
        cliente.setCorreo(correo);
        cliente.setActivo(activo);
        return cliente;
    }
}
