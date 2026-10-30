package com.mandalecarbon.service;

import com.mandalecarbon.repository.ClienteJpaRepository;
import com.mandalecarbon.repository.PedidoJpaRepository;
import com.mandalecarbon.repository.ProductoJpaRepository;
import com.mandalecarbon.repository.UsuarioRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock private PedidoJpaRepository pedidos;
    @Mock private ProductoJpaRepository productos;
    @Mock private ClienteJpaRepository clientes;
    @Mock private UsuarioRepository usuarios;
    @Mock private JdbcTemplate db;

    @InjectMocks
    private PedidoService service;

    @Test
    void crearRechazaUnaListaDeProductosVacia() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> crear(List.of(), "Recojo", null, null));

        assertEquals("Selecciona al menos un producto", error.getMessage());
    }

    @Test
    void crearRechazaUnCanalDesconocido() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> crear(List.of(10), "Local", null, null));

        assertEquals("Canal inválido", error.getMessage());
    }

    @Test
    void crearExigeMesaParaPedidoDeSalon() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> crear(List.of(10), "Salon", null, null));

        assertEquals("Selecciona una mesa libre para el pedido de salón", error.getMessage());
    }

    @Test
    void crearExigeDireccionParaDelivery() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> crear(List.of(10), "Delivery", null, null));

        assertEquals("Ingresa la dirección de entrega.", error.getMessage());
    }

    private void crear(List<Integer> productos, String canal, Integer mesa, String direccion) {
        service.crear("Cliente de prueba", "999999999", canal, 1,
                productos, Map.of(), mesa, null, direccion, null);
    }
}
