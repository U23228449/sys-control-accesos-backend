package com.estaciona.api.common.dto;

import java.util.List;

/**
 * Wrapper genérico para respuestas paginadas.
 */
public class PageResponse<T> {

    private List<T> contenido;
    private int pagina;
    private int tamanio;
    private long totalElementos;
    private int totalPaginas;
    private boolean ultima;

    public PageResponse() {
    }

    public PageResponse(List<T> contenido, int pagina, int tamanio,
                        long totalElementos, int totalPaginas, boolean ultima) {
        this.contenido = contenido;
        this.pagina = pagina;
        this.tamanio = tamanio;
        this.totalElementos = totalElementos;
        this.totalPaginas = totalPaginas;
        this.ultima = ultima;
    }

    public List<T> getContenido() { return contenido; }
    public int getPagina() { return pagina; }
    public int getTamanio() { return tamanio; }
    public long getTotalElementos() { return totalElementos; }
    public int getTotalPaginas() { return totalPaginas; }
    public boolean isUltima() { return ultima; }
}
