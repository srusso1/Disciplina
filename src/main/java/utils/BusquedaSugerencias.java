package utils;

import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BusquedaSugerencias {

    private BusquedaSugerencias() {
    }

    public static <T> void configurar(
            TextField campo,
            ContextMenu menu,
            List<T> fuente,
            int minimoCaracteres,
            int maxResultados,
            Function<T, String> textoBusqueda,
            Function<T, String> textoMenu,
            Function<T, String> textoSeleccion,
            Consumer<T> onSelect,
            Runnable onTextoCorto
    ) {

        campo.textProperty().addListener((obs, oldText, newText) -> {
            String entrada = newText == null ? "" : newText.trim();

            if (entrada.length() < minimoCaracteres) {
                menu.hide();
                if (onTextoCorto != null) {
                    onTextoCorto.run();
                }
                return;
            }

            String filtro = entrada.toUpperCase(Locale.ROOT);

            List<T> resultados = fuente.stream()
                    .filter(item -> {
                        String valor = textoBusqueda.apply(item);
                        return valor != null && valor.toUpperCase(Locale.ROOT).contains(filtro);
                    })
                    .limit(maxResultados)
                    .collect(Collectors.toList());

            if (resultados.isEmpty()) {
                menu.hide();
                Validaciones.agregarPopOver(campo, "No hay coincidencias");
                return;
            }

            Validaciones.ocultarPopOver(campo);
            List<MenuItem> items = new ArrayList<>();

            for (T item : resultados) {
                MenuItem opcion = new MenuItem(textoMenu.apply(item));
                opcion.setOnAction(e -> {
                    campo.setText(textoSeleccion.apply(item));
                    if (onSelect != null) {
                        onSelect.accept(item);
                    }
                    menu.hide();
                });
                items.add(opcion);
            }

            menu.getItems().setAll(items);
            if (!menu.isShowing()) {
                menu.show(campo, Side.BOTTOM, 0, 0);
            }
        });

        campo.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) {
                menu.hide();
                Validaciones.ocultarPopOver(campo);
            }
        });
    }
}

